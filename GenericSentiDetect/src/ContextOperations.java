import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.lang.StringBuffer;

public class ContextOperations 
{
	private static final boolean verbose = false;
	private static final String[] refnametypes = new String[4];
	private static final int TOTAL_CITATION_NAMETYPES = 4;
	static
	{
		refnametypes[0] = "[A-Z][A-Za-z]*([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}";	//<FN> <LN>
		refnametypes[1] = "[A-Z][A-Za-z]*,([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}"; //<LN>, <FN>			
		refnametypes[2] = "([A-Z][\\. \r\n]+)+([A-Za-z][-A-Za-z\n]+){1,5}"; //I. <LN>			
		refnametypes[3] = "([A-Z][-A-Za-z\n]+)+[, \r\n]+([A-Z][\\. \r\n])+"; //<LN>, I.		
	}
	private static final String name = "(" + refnametypes[0] + "|" + refnametypes[1] + "|" + refnametypes[2] + "|" + refnametypes[3] + ")";
	private static final String whitespace = "[ \r\n]*";
	private static final String EtAl = whitespace + "[Ee][Tt][ \\.]+[Aa][Ll][ \\.]*";
	private static final String COMMA = "[,;]?";
	
	private static final String nameoccurrence1 = name;
	private static final String nameoccurrencex = "(,?" + whitespace + name + ")*";
	private static final String nameoccurrencelast = ",?" + whitespace + "(and)?" + whitespace + "(" + name + ")?[\\.]" + whitespace;
	private static final String yearoccurrence = "[0-9]{4}[A-Za-z]*\\.?"; 		
	private static final String regex = nameoccurrence1 + nameoccurrencex + nameoccurrencelast + yearoccurrence;
//	private static final String regex = nameoccurrence1;// + nameoccurrencex;// + nameoccurrencelast;//+ yearoccurrence;

//************************Main function*****************************************	
	public static void main(String[] args) throws IOException
	{
		boolean test = false;
		boolean append = true;
		int count=0, paperlimit = 50;
		int paperstart = 15;
		
		long startTime = System.currentTimeMillis();
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		HashMap<String,String> authorsurnamemap = new HashMap<String,String>();
		HashMap<String, ArrayList<Reference>> references = null;
		String filepath;
		File fin = new File("Outputs/Contexts/AAN.tmp");
		
		if(append) if(fin.exists()) references = FileOperations.readObject(fin);
		if (references == null) references = new HashMap<String, ArrayList<Reference>>();
		
		for (Map.Entry<String, Paper> entry : papers.entrySet())
		{
			if(!test) 
			{
				++count; if(count > paperlimit) break;
				if(count < paperstart) continue;
				filepath = "C:/Abhishek_Narwekar/Papers,Datasets/Citation Polarity/Datasets/AAN/aan/papers_text/" + entry.getValue().id + ".txt";
				System.out.println("Extracting reference from paper " + entry.getKey() + ", count = " + count);
			}
			else filepath = "Datasets/AAN/testpaper.txt";
			File f = new File(filepath);
			
			if (f.exists())
			{
				if (test)references.put("test", new ArrayList<Reference>());
				else references.put(entry.getKey(), new ArrayList<Reference>());
				String content = new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
				refStart = getRefSectionStart(content);
				System.out.println("Refstart" + refStart);
				
				if (refStart > 0)
				{
					if(test)references = getPotentialAuthors("test", content.substring(refStart, content.length()), references);		
					else references = getPotentialAuthors(entry.getKey(), content.substring(refStart, content.length()), references);
					
					for (Map.Entry<String, ArrayList<Reference>> e : references.entrySet())
					{					
						for (Reference r : e.getValue())
						{
							r = getContextofReference(content.substring(0, refStart), r);
							r.citedid = getIDofCitedPaper(r, papers);
	//						System.out.println(r.citedid);
						}						
					}
				}
				content = null;
				if(test) break;
			}
		}
		FileOperations.writeObject(references, fin);
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
//*********************End of main***********************************************	

	public static int getRefSectionStart(String content)
	{
//		String regex = "(?i)(r.?e.?f.?e.?r.?e.?n.?c.?e.?s)|(b.?i.?b.?l.?i.?o.?g.?r.?a.?p.?h.?y.?)";
		String regex = "(?i)(r.?e.?f.?e.?r.?e.?n.?c.?e.?s)";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		
		int matchedAt=-1;
		
		while(matcher.find())
		{
//			System.out.println("matched \"" + matcher.group() + "\" at position " + matcher.start());
			matchedAt = matcher.start() + matcher.group().length();
		}
		return matchedAt;
	}

	public static HashMap<String, ArrayList<Reference>> getPotentialAuthors(String paperid, String content, HashMap<String, ArrayList<Reference>> references) throws IOException
	{
	/*
	 * This function obtains the line in the references where the authors' names occur, and also
	 * gets their surnames and the publication year of the referred papers.
	 */
		Pattern pattern = Pattern.compile(regex);
		Pattern pattern2 = Pattern.compile("[. \r\n]*" + yearoccurrence);
		Matcher matcher = pattern.matcher(content);
		Matcher matcher2;
		int titlestart, titleend;
		
		String allauthors;
		String[] authorarray;
		String[] surnamearray;
		String year = "";

		ArrayList<Reference> rList = new ArrayList<Reference>();
		while(matcher.find())
		{
			Reference r = new Reference();

			allauthors = matcher.group();

			//Get the year from the string
			matcher2 = pattern2.matcher(allauthors);
			while(matcher2.find())
			{
				year = matcher2.group().replaceAll("[. \r\n]+", "");
//				System.out.println(year);
				allauthors = allauthors.substring(0,matcher2.start());
			}	

			titlestart = matcher.end();
			titleend = content.indexOf('.', titlestart + 1);
			r.titletext = content.substring(titlestart, titleend);
			r.titletext = cleanString(r.titletext, false);
			
			if(verbose) System.out.println("***TITLETEXT***\n" + r.titletext);
			
			//In the part of the author list except the year, replace all the "and"s, repeated blanks and fullstops with 1 space
			allauthors = cleanString(allauthors, true);
			authorarray = allauthors.split(",[ ]*");
			r.reftext = allauthors;
			r.year = year;
			int lastoccur = 0;

			//Getting the surname: Identify the type of name
			surnamearray = new String[authorarray.length];
			for (int i = 0; i < authorarray.length; ++i)
			{
//				System.out.println(authorarray[i]);
				for (int j = 0; j < refnametypes.length; ++j)
				{
					if (authorarray[i].matches(refnametypes[j]))
					{
						switch(j)
						{
							case 0:
								lastoccur = authorarray[i].lastIndexOf(' ');
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(lastoccur+1, authorarray[i].length());
								break;
							case 1:
							case 3:
								lastoccur = authorarray[i].indexOf(',');
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(0, lastoccur);
								break;
							case 2:
								lastoccur = Math.max(authorarray[i].lastIndexOf('.'),authorarray[i].lastIndexOf(' '));
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(lastoccur+1, authorarray[i].length());
								break;
						}

						if (!r.authorsurnamemap.containsKey(authorarray[i])) 
								r.authorsurnamemap.put(authorarray[i], surnamearray[i]);
						break;
					}
				}
			}
			rList.add(r);
		}
		references.put(paperid, rList);

		if (verbose)
			for (Reference r1 : rList)
			{
				System.out.println("***\nRef Text: " + r1.reftext + ".\nAuthors(surnames):\n");
				for (Map.Entry<String, String> e : r1.authorsurnamemap.entrySet())
					System.out.println(e.getKey() + "-" + e.getValue());
			}

		return references;
	}
	
	public static Reference getContextofReference(String content, Reference r) throws IOException
	{
		String[] surnames = new String[r.authorsurnamemap.size()];
		String contentrev;
		int[] fullstoplocs = new int[5];
		int temploc;
		surnames = r.authorsurnamemap.values().toArray(surnames);

//		System.out.println(r.reftext);
		
		String yearbracket = "[\\(]?" + whitespace + r.year + whitespace + "[\\)]?";
		String[] citationnametype = new String[TOTAL_CITATION_NAMETYPES];
		for (int i = 0; i < TOTAL_CITATION_NAMETYPES; ++i) citationnametype[i] = "";
		
		Pattern p = null;
		Matcher m = null;
		
		if (surnames.length > 0) citationnametype[0] = surnames[0] + COMMA + EtAl + whitespace + yearbracket; //<A1> et al (year)
		if (surnames.length > 1)
		{ 
			citationnametype[1] = surnames[0] + whitespace + "[Aa][Nn][Dd]" + whitespace + surnames[1] + COMMA + whitespace + yearbracket;	//A1 and A2 (year)
		}
		if (surnames.length > 2)
		{
			for (int i = 0; i < surnames.length; ++i)
			{
				citationnametype[2] += surnames[i] + COMMA + whitespace + yearbracket;	//A1, ...AN (year)
			}
		}
		if (surnames.length > 0) citationnametype[3] = surnames[0] + COMMA + whitespace + yearbracket;	//A1 (year)
	
		for (int i = 0; i < TOTAL_CITATION_NAMETYPES; ++i)
		{
			if (citationnametype[i].length() > 0) 
			{	
				p = Pattern.compile(citationnametype[i]);
				m = p.matcher(content);
								
				while (m.find())
				{
					if (verbose) System.out.println("Matched : \"" + m.group() + "\" at location: " + m.start());
					
					r.contexts.add(new String[4]);
					StringBuffer temp = new StringBuffer(content.substring(0, m.start()));
					contentrev = temp.reverse().toString();
					
					temploc = contentrev.indexOf('.', 0);
					fullstoplocs[1] = temp.length() - temploc;
					fullstoplocs[0] = temp.length() - contentrev.indexOf('.', temploc+1);
					
					temploc = m.start();
					for (int k = 2; k < 5; ++k)
					{
						fullstoplocs[k] = content.indexOf('.',temploc);
						temploc = fullstoplocs[k]+1;
					}
					
					for (int k = 0; k < 4; ++k)
					{
						r.contexts.get(Math.max(0,r.contexts.size()-1))[k] = content.substring(fullstoplocs[k]+1, fullstoplocs[k+1]+1);
					}
					
					if (verbose)
					{
						System.out.println("Context: ");
						for (int l = 0; l < 4; ++l) System.out.println(l + " - " + r.contexts.get(r.contexts.size()-1)[l]);
					}
				}
			}
		}
		return r;
	}
	
	public static String cleanString(String input, boolean authorflag)
	{
		String output = input;
		output = output.replaceAll("[\r\n]", " ");
		if(authorflag) output = output.replaceAll("and", ",");
		output = output.replaceAll("[ ]+", " ");
		output = output.replaceAll(" +[,;]? +", ",");			

		return output;
	}
	
	public static String getIDofCitedPaper(Reference r, HashMap <String, Paper> papers) throws IOException
	{
		/*
		 * Go through all papers, find those with auhtors having the same surname as the 
		 * current reference. If multiple such papers exist, just choose the one with 
		 * the closest match.
		 */
		int matchcount = 0;
		ArrayList<Paper> matchedpaper = new ArrayList<Paper>();
		String IDtoreturn = "-2";
		int levenshteinscore = 0;
		int mindist = Integer.MAX_VALUE;
		for (Paper paper : papers.values())
		{
			for (String surname : r.authorsurnamemap.values())
			{
//				System.out.println(surname);
				for(int i = 0; i < paper.authors.length; ++i)
				{
					if( paper.authors[i]!=null && surname!=null)
						if(paper.authors[i].contains(surname))
						{
							++matchcount;
							break;
						}
				}
			}
			if (matchcount == r.authorsurnamemap.size()) matchedpaper.add(paper);
		}
		
		if (matchedpaper.size() == 0) return "-1";
		else if (matchedpaper.size() == 1) return matchedpaper.get(0).id;
		else
		{
			
			for (Paper paper : matchedpaper)
			{
				levenshteinscore = GeneralOperations.getLevenshteinDistance(paper.id, r.titletext);
				if (levenshteinscore < mindist)
				{
					mindist = levenshteinscore;
					IDtoreturn = paper.id;
				}
			}
			return IDtoreturn;
		}
	}
	
}