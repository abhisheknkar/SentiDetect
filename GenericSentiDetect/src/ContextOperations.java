import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.lang.StringBuffer;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class ContextOperations 
{
	private static final boolean verbose = false;
	private static final boolean test = false;
	private static final String[] refnametypes = new String[6];
	private static final int TOTAL_CITATION_NAMETYPES = 4;
	static
	{
		refnametypes[0] = "[A-Z][-A-Za-z]*[ \r\n]+[a-z]+[ \r\n]+[A-Z][A-Za-z]*";//<FN> <small> <LN>
		refnametypes[1] = "[A-Z][-A-Za-z]*([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}";	//<FN> <LN>
		refnametypes[2] = "[A-Z][-A-Za-z]*,([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}"; //<LN>, <FN>			
		refnametypes[3] = "([A-Z][\\. \r\n]+)+([A-Za-z][-A-Za-z\n]+){1,5}"; //I. <LN>			
		refnametypes[4] = "([A-Z][-A-Za-z\n]+)+[, \r\n]+([A-Z][\\. \r\n])+"; //<LN>, I.		
		refnametypes[5] = "[A-Z][-A-Za-z]*[ \r\n]+[A-Z][\\. \r\n]+[A-Z][-A-Za-z]*"; //<FN> M. <LN>
	}
	private static final String name = "(" + refnametypes[0] + "|" + refnametypes[1] + "|" + refnametypes[2] + "|" + refnametypes[3] + "|" + refnametypes[4] + ")";
	private static final String whitespace = "[ \r\n]*";
	private static final String EtAl = whitespace + "[Ee][Tt][ \\.]+[Aa][Ll][, \\.]*";
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
		/*
		 * In main(), we iterate over the papers specified in search of citation contexts
		 * First, we check if the paper exists.
		 * For all those which do: 
		 * 1. We create an entry in the map "references" with the paper id as the key and the list of references as the values
		 * 2. Then we get the content of each paper from the file
		 * 3. Then we get the location where the references start
		 * 4. If it is a valid location, we try to get the attributes of all the references beyond that point: the author names, their surnames, the paper title, the year of pub.
		 * 5. Now we try to get the context of each of those references in the text of the paper.
		 * 6. For that, we first split the sentence upto refstart into 
		 *  
		 */
		boolean append = true;
		int count=0; 
		int paperstart = 501;
		int paperlimit = 99;
		
		long startTime = System.currentTimeMillis();
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		HashMap<String,String> authorsurnamemap = new HashMap<String,String>();
		HashMap<String, ArrayList<Reference>> references = null;
		String filepath, finalexpression;
		File fin = new File("Outputs/Contexts/references.tmp");
		File ferror = new File("Outputs/Contexts/errorlog.txt");
		
//		if(append) if(fin.exists()) references = FileOperations.readObject(fin);
		if (references == null) references = new HashMap<String, ArrayList<Reference>>();

		for (Map.Entry<String, Paper> entry : papers.entrySet())
		{
			try
			{
				if(test) 
				{	
					++count;
					if(count > 1) break;
					filepath = "Datasets/AAN/testpaper.txt";
				}
				else 
				{
					++count; if(count > paperstart + paperlimit) break;
					if(count < paperstart) continue;
					filepath = "C:/Abhishek_Narwekar/Papers,Datasets/Citation Polarity/Datasets/AAN/aan/papers_text/" + entry.getValue().id + ".txt";
					System.out.println("Extracting reference from paper " + entry.getKey() + ", count = " + count);
				}
				File f = new File(filepath);
				
				if (f.exists())
				{
					if (test)references.put("test", new ArrayList<Reference>());
					else references.put(entry.getKey(), new ArrayList<Reference>());
					String content = new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
					
					//Clean content
					content = cleanString(content, false);
									
					refStart = getRefSectionStart(content);
					
					if (refStart > 0)
					{
						if(test)references = getPotentialAuthors("test", content.substring(refStart, content.length()), references);		
						else references = getPotentialAuthors(entry.getKey(), content.substring(refStart, content.length()), references);
						
/*
						for(Map.Entry<String, ArrayList<Reference>> reflist : references.entrySet())

						{
							System.out.println("In paper: " + reflist.getKey() + ", reference: ");
							for(Reference ref : reflist.getValue())
							{ 
								System.out.println("\nReference---" + ref.reftext + "---has authors: ");
								for(String surname : ref.authorsurnamemap.values())
								{
									System.out.println(surname + ",");
								}
							}
						}
*/						
						//***Split content into sentences!
						ArrayList<String> contentsplit = splitToSentence(content.substring(0, refStart));
						for (Reference r : references.get(entry.getKey()))
						{
//							r = getContextofReference(content.substring(0, refStart), r);
							r = getContextofReference2(contentsplit, r);
							r.citedid = getIDofCitedPaper(r, papers);
							

							for (int j = 0; j < r.contexts.size(); ++j)
							{
								//Write to txt file
								finalexpression = "";
								finalexpression += entry.getKey() + "\t" + r.citedid + "\t" + r.year ;
								for (int i = 0; i < 4; ++i) 
								{
									finalexpression += "\t" + r.contexts.get(j)[i];
								}
							 
								if(r.contexts.size() > 0 && !r.citedid.equals("-1"))
								{
									try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Outputs/Contexts/r" + paperstart + "_" + (paperstart+paperlimit) + ".txt", true)))) 
									{
	//									System.out.print(finalexpression);
						 			    out.println(finalexpression);
						 			}
									catch (IOException e2) {}
								}
							}
						}						
						//Write outputs
						if(count % 10  == 0) 
						{
							System.out.print("Writing outputs...\n");
							FileOperations.writeObject(references, fin);
						}
					}
					content = null;
					if(test) break;
				}
			}
			catch(Exception e)
			{
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Outputs/Contexts/errorlog.txt", true)))) {
	 			    out.println("Error in count " + count + "; Message: " + e);
	 			}catch (IOException e2) {
	 			    //exception handling left as an exercise for the reader
	 			}
			}
		}
		FileOperations.writeObject(references, fin);
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
//*********************End of main***********************************************	

	public static ArrayList<String> splitToSentence(String content) 
	{		
		String s;
		Reader reader = new StringReader(content);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		ArrayList<String> sentenceList = new ArrayList<String>();

		for (List<HasWord> sentence : dp) {
		   String sentenceString = Sentence.listToString(sentence);
		   s = sentenceString.toString();
		   s = s.replaceAll("-LRB-[ ]*", "(");
		   s = s.replaceAll("[ ]*-RRB-", ")");
		   s = s.replaceAll("-LSB-[ ]*", "[");
		   s = s.replaceAll("[ ]*-RSB-", "]");
		   s = s.replaceAll("-LCB-[ ]*", "{");
		   s = s.replaceAll("[ ]*-RCB-", "}");
		   sentenceList.add(s);
		}
		return sentenceList;
		
	}

	public static int getRefSectionStart(String content)
	{
		String regex = "(?i)(r.?e.?f.?e.?r.?e.?n.?c.?e.?s)|(?i)(b.?i.?b.?l.?i.?o.?g.?r.?a.?p.?h.?y.?)";
//		String regex = "(?i)(r.?e.?f.?e.?r.?e.?n.?c.?e.?s)";

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
//		System.out.println(matcher.find());
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
			r.year = year.substring(0, 4);	//Ignore the alphabet subscript.
			int lastoccur = 0;
			
			//Getting the surname: Identify the type of name
			surnamearray = new String[authorarray.length];
			for (int i = 0; i < authorarray.length; ++i)
			{
				for (int j = 0; j < refnametypes.length ; ++j)
				{
					if (authorarray[i].matches(refnametypes[j]))
					{
						switch(j)
						{
							case 1:
							case 5:
								lastoccur = authorarray[i].lastIndexOf(' ');
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(lastoccur+1, authorarray[i].length());
								break;
							case 2:
							case 4:
								lastoccur = authorarray[i].indexOf(',');
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(0, lastoccur);
								break;
							case 3:
								lastoccur = Math.max(authorarray[i].lastIndexOf('.'),authorarray[i].lastIndexOf(' '));
								if(lastoccur<0) break;
								surnamearray[i] = authorarray[i].substring(lastoccur+1, authorarray[i].length());
								break;
							case 0:
								String[] namesplit = authorarray[i].split("[ \r\n]+");
								if(namesplit.length > 2)
								surnamearray[i] = namesplit[namesplit.length-2] + " " + namesplit[namesplit.length-1];
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
				System.out.println("***\nRef Text: " + r1.reftext + ".\nAuthors(surnames):");
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

		//Replace the newlines
		content.replaceAll("\n", " ");
		
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

/*
		System.out.println("\nLooking for the following name patterns:\n");
		for (int i = 0; i < TOTAL_CITATION_NAMETYPES; ++i)
		{
			System.out.println(citationnametype[i]);
		}
*/		

		
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
						//Replace the newlines
//						r.contexts.get(Math.max(0,r.contexts.size()-1))[k].replaceAll("\n", " ");
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
	
	public static Reference getContextofReference2(ArrayList<String> contentsplit, Reference r) throws IOException
	{
		String currentsentence;
		String[] surnames = new String[r.authorsurnamemap.size()];
		surnames = r.authorsurnamemap.values().toArray(surnames);
	
		//Divide entire content into sentences
		
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

/*
		System.out.println("\nLooking for the following name patterns:\n");
		for (int i = 0; i < TOTAL_CITATION_NAMETYPES; ++i)
		{
			System.out.println(citationnametype[i]);
		}
*/		

		
		for (int i = 0; i < TOTAL_CITATION_NAMETYPES; ++i)
		{
			if (citationnametype[i].length() > 0) 
			{	
				//Search in each sentence
				for (int j = 0; j < contentsplit.size(); ++j)
				{
					p = Pattern.compile(citationnametype[i]);
					m = p.matcher(contentsplit.get(j));
									
					if (m.find())
					{
						if (verbose) System.out.println("Matched : \"" + m.group() + "\" at location: " + m.start());
						
						r.contexts.add(new String[4]);
												
						for (int k = 0; k < 4; ++k)
						{
							if(j + k - 1 < 0 || j + k - 1 > contentsplit.size()-1) continue;
							r.contexts.get(Math.max(0,r.contexts.size()-1))[k] = contentsplit.get(j+k-1);
						}
						
						if (verbose)
						{
							System.out.println("Context: ");
							for (int l = 0; l < 4; ++l) System.out.println(l + " - " + r.contexts.get(r.contexts.size()-1)[l]);
						}
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
		output = output.replaceAll("\t", " ");
		if(authorflag) output = output.replaceAll("\\band\\b", ",");
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