import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.dcoref.sievepasses.PronounMatch;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

//All members are static functions
public class AmjadFeatures 
{
	private static final String[] PronounList = {"i", "me", "my", "mine", "we", "us", "we", "our", "ours",
			"he", "she", "it", "they", "him", "her", "them","his", "her", "its", "their", "theirs"}; 
	
	private final static String[] POSadv = {"RB","RBR","RBS", "WRB"};
	private final static String[] POSverb = {"VB","VBD","VBG","VBN","VBP","VBZ"};
	private final static String[] POSadj = {"JJ","JJR","JJS"};

	public static ArrayList<Citation> cleanCitations(ArrayList<Citation> citations)
	{
		String REF = "<REF>.*?</REF>";
		String TREF = "<TREF>.*?</TREF>";

		Pattern pattern1 = Pattern.compile(REF);
		Pattern pattern2 = Pattern.compile(TREF);

		Matcher matcher1, matcher2;
		
		for (Citation citation : citations)
		{
			for (int i = 0; i < 4; ++i)
			{
//				System.out.println("Before: " + citation.Sentence[i]);
				matcher1 = pattern1.matcher(citation.Sentence[i]);
				citation.Sentence[i] = matcher1.replaceAll("<REF>");
				
				if(i == 1)
				{
					matcher2 = pattern2.matcher(citation.Sentence[i]);
					citation.Sentence[i] = matcher2.replaceAll("<TREF>");
				}
//				System.out.println("After: " + citation.Sentence[i]);
			}
		}
		
		return citations;
	}
	public static ArrayList<Citation> getFeatures0and1(ArrayList<Citation> citations)
	{
		/*
		 * Number of references and if the target reference is separate from the rest
		 * Handles the first two features
		 * Assuming cleaned citations
		 */
		int count, delta;
		for (Citation citation : citations)
		{
			count = 0;
			citation.Features[1] = 1;
			for(int i = 0; i < 4; ++i)
			{
				if(citation.SentenceScore[i] == 1)
				{
					delta = findNoOfOccurrences(citation.Sentence[i], "REF>");
					if ((i == 1) && (delta > 1))
					{
						citation.Features[1] = 0;
					}
					count += delta;
				}
			}
//			System.out.println(count);
			citation.Features[0] = count;
		}
        return citations;
	}

	public static ArrayList<Citation> getFeature2(ArrayList<Citation> citations, HashMap<String, Paper> papers)
	{
		/*
		 * Get the self citations
		 * Get the citer and the cited papers' authors, find intersection
		 */
		String[] citerauthors = null;
		String[] citedauthors = null;
		String[] commonauthors = null;
		
		for (Citation citation : citations)
		{
			if (!(papers.containsKey(citation.Citer) && papers.containsKey(citation.Cited))) continue;
			citerauthors = papers.get(citation.Citer).authors;
			citedauthors = papers.get(citation.Cited).authors;
			
			//Get intersection
			Set<String> s1 = new HashSet<String>(Arrays.asList(citerauthors));
			Set<String> s2 = new HashSet<String>(Arrays.asList(citedauthors));
			s1.retainAll(s2);				
			
			if (s1.size() > 0)
			{
				citation.Features[2] = 1;
			}
			s1 = null; s2 = null;
		}
        return citations;
	}
	
	public static ArrayList<Citation> getFeature3(ArrayList<Citation> citations)
	{
		/*
		 * 1/3 PP pronoun
		 */
		boolean found = false;
		for(Citation citation : citations)
		{
			for (int i = 0; i < citation.Sentence.length; ++i)
			{
				if(citation.SentenceScore[i] != 0)
				{
					for (int j = 0; j < PronounList.length; ++j)
					{
						found = citation.Sentence[i].matches("(?i).*\\b"+ PronounList[j] +"\\b.*");
						if (found) 
						{
							citation.Features[3]=1;
//							System.out.println("Pronoun: " + PronounList[j] + ", Sentence: " + citation.Sentence[i]);
							break;
						}
					}
					if (found) break;
				}
			}
		}		
        return citations;
	}
	public static ArrayList<Citation> getFeature4(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Negation
		 */
		File fin = new File("Outputs/negationcues.tmp");
		HashSet<String> negations = FileOperations.readObject(fin);
		boolean found = false;
		for(Citation citation : citations)
		{
			for (int i = 0; i < citation.Sentence.length; ++i)
			{
				if(citation.SentenceScore[i] != 0)
				{
					for (String negation : negations)
					{
						found = citation.Sentence[i].matches("(?i).*\\b"+ negation +"\\b.*");
						if (found) 
						{
							citation.Features[4]=1;
//							System.out.println("Pronoun: " + PronounList[j] + ", Sentence: " + citation.Sentence[i]);
							break;
						}
					}
					if (found) break;
				}
			}
		}		
        return citations;
	}
	
	public static ArrayList<Citation> getFeature5(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Speculation
		 */
		File fin = new File("Outputs/speculationcues.tmp");
		HashSet<String> speculations = FileOperations.readObject(fin);
		boolean found = false;
		for(Citation citation : citations)
		{
			for (int i = 0; i < citation.Sentence.length; ++i)
			{
				if(citation.SentenceScore[i] != 0)
				{
					for (String speculation : speculations)
					{
						found = citation.Sentence[i].matches("(?i).*\\b"+ speculation +"\\b.*");
						if (found) 
						{
							citation.Features[5]=1;
							break;
						}
					}
					if (found) break;
				}
			}
		}		
        return citations;
	}

	public static ArrayList<Citation> getFeature7(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Headline
		 */
		String[] sections = new String[4];
		boolean foundflag;
		sections[0] = "(I.?[Nn].?[Tt].?[Rr].?[Oo].?[Dd].?[Uu].?[Cc].?[Tt].?[Ii].?[Oo].?[Nn])|"
				+ "([Mm].?[Oo].?[Tt].?[Ii].?[Vv].?[Aa].?[Tt].?[Ii].?[Oo].?[Nn])";
		sections[1] = "(B.?[Aa].?[Cc].?[Kk].?[Gg].?[Rr].?[Oo].?[Uu].?[Nn].?[Dd])|"
				+ "((P.?[Rr].?[Ii].?[Oo].?[Rr])|([Rr].?[Ee].?[Ll].?[Aa].?[Tt].?[Ee].?[Dd])|(P.?[Rr].?[Ee].?[Vv].?[Ii].?[Oo].?[Uu].?[Ss]).?[Ww].?[Oo].?[Rr].?[Kk])";
		sections[2] = "(E.?[Xx].?[Pp].?[Ee].?[Rr].?[Ii].?[Mm].?[Ee].?[Nn].?[Tt].?[Ss]?)|"
				+ "(D.?[Aa].?[Tt].?[Aa])|"
				+ "(E.?[Vv].?[Aa].?[Ll].?[Uu].?[Aa].?[Tt].?[Ii].?[Oo].?[Nn])|"
				+ "(R.?[Ee].?[Ss].?[Uu].?[Ll].?[Tt].?[Ss])";
		sections[3] = "(D.?[Ii].?[Ss].?[Cc].?[Uu].?[Ss].?[Ss].?[Ii].?[Oo].?[Nn])|"
				+ "(C.?[Oo].?[Nn].?[Cc].?[Ll].?[Uu].?[Ss].?[Ii].?[Oo].?[Nn])|"
				+ "(F.?[Uu].?[Tt].?[Uu].?[Rr].?[Ee].?[Ww].?[Oo].?[Rr].?[Kk])";		
		
		Pattern[] patterns = new Pattern[4];		
		Matcher[] matcher = new Matcher[4];
		String context = "";

		for(int i = 0; i < 4; ++i) 
		{
			patterns[i] = Pattern.compile(sections[i]);
		}

		for (Citation citation : citations)
        {
			foundflag = false;
			context = "";
			for(int i = 0; i < 4; ++i)
        	{
        		context += citation.Sentence[i];
        	}
        	for(int i = 0; i < 4; ++i)
        	{
        		matcher[i] = patterns[i].matcher(context);
        		while(matcher[i].find())
        		{
        			citation.Features[7] = i;
        			foundflag = true;
        			break;
        		}
        		if(foundflag) 
        		{        			
        			break;
        		}
        	}
        	if(!foundflag)
        	{
        		citation.Features[7] = 4;
        	}
//			System.out.println("Section: " + citation.Features[7]);
        }
		return citations;
	}
	
	public static ArrayList<Citation> getFeature9and10(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Closest verb/adjective/adverb and
		 * dependency relations
		 */
		
		File depfile = new File("Outputs/Amjad/dependencies.tmp");
		HashMap<String, Integer> depmap = new HashMap<String, Integer>();
		if(depfile.exists()) depmap = FileOperations.readObject(depfile);
		
		String source_string, dest_string, type_string, to_write;
		int source_till, dest_till;
		int distance = Integer.MAX_VALUE;
		String text;
		ArrayList<ArrayList<String>> targetPOS = new ArrayList<ArrayList<String>>();
		ArrayList<String> a = new ArrayList<String>(Arrays.asList(POSadv));
		ArrayList<String> b = new ArrayList<String>(Arrays.asList(POSverb));
		ArrayList<String> c = new ArrayList<String>(Arrays.asList(POSadj));
		targetPOS.add(a);targetPOS.add(b);targetPOS.add(c);

		Properties props = new Properties();
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        props.put("annotators", "tokenize, ssplit, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
        for (Citation citation : citations)
        {
        	text = citation.Sentence[1];
            // create an empty Annotation just with the given text
            Annotation document = new Annotation(text);

            // run all Annotators on this text
            pipeline.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            for(CoreMap sentence: sentences) 
    	    {
            	// this is the parse tree of the current sentence
            	// Tree tree = sentence.get(TreeAnnotation.class);
            	// System.out.println(tree.toString());
    			
            	// this is the Stanford dependency graph of the current sentence
            	SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

            	for(SemanticGraphEdge edge : dependencies.edgeListSorted())
            	{
            		source_string = edge.getSource().toString();
            		source_till = source_string.indexOf('-');
            		source_string = source_string.substring(0, source_till).toLowerCase();
            		
            		dest_string = edge.getTarget().toString();
            		dest_till = dest_string.indexOf('-');
            		dest_string = dest_string.substring(0, dest_till).toLowerCase();

//            		System.out.println(source_string + "->" + dest_string + "," + edge.getRelation().toString());
            		to_write = edge.getRelation().toString() + "_" + source_string + "_" + dest_string;
            		if (!depmap.containsKey(to_write)) depmap.put(to_write, depmap.size()); 
            	}
            	IndexedWord source = dependencies.getNodeByWordPattern("<TREF>");
            	distance = getBFSDistanceto3POS(dependencies,source,targetPOS,new ArrayList<IndexedWord>(), Integer.MAX_VALUE);
//            	System.out.println(dependencies.toString());
//            	System.out.println(dependencies.edgeListSorted().toString());
            	System.out.println("Distance between TREF and required POS tag is: " + distance);            	
    	    }	        	
            citation.Features[10] = distance;
        }
        FileOperations.writeObject(depmap, depfile);
        return citations;
	}	

	public static int findNoOfOccurrences(String str, String findStr)
	{
		int lastIndex = 0;
		int count = 0;

		while(lastIndex != -1)
		{
			lastIndex = str.indexOf(findStr,lastIndex);
			if( lastIndex != -1)
			{
				count ++;
				lastIndex+=findStr.length();
			}
		}
//		System.out.println(count);	//		for (Citation Citation : citations)
		return count;
	}

	public static void computeSentenceScore_OpinionFinder(ArrayList<Citation> citations, ArrayList<OpinionFinderWord> Words)
	{
		String Sentence;
		int PolarityThreshold = 1;
		int DisplayIndividualScores = 0;
		int NoPrintFlag = 1;
		
		for (Citation Citation : citations)
		{
			for (int i = 0; i < 4; ++i)
			{
				if (Citation.SentenceScore[i] != 0)
				{
					Sentence = Citation.Sentence[i];
					if (DisplayIndividualScores == 1) System.out.println(Sentence);
					Citation.OpinionFinderScore[i] = OpinionFinder.computeScore_OpinionFinder(Words, Sentence, PolarityThreshold, DisplayIndividualScores, NoPrintFlag);
					
				}
			}
		}		
	}

	public static void computeSentenceScore_SentiWordnet(ArrayList<Citation> citations, HashMap<String, double[]> SentiWordnetWords)
	{
		String Sentence;
		int PolarityThreshold = 1;
		int DisplayIndividualScores = 0;
		int NoPrintFlag = 1;
		
		for (Citation Citation : citations)
		{
			for (int i = 0; i < 4; ++i)
			{
				if (Citation.SentenceScore[i] != 0)
				{
					Sentence = Citation.Sentence[i];
					if (DisplayIndividualScores == 1) System.out.println(Sentence);
					Citation.SentiWordnetScore[i] = SentiWordnet.computeScore_SentiWordnet(SentiWordnetWords, Sentence, PolarityThreshold, DisplayIndividualScores, NoPrintFlag);
					
				}
			}
		}		
	}

	public static void computeSentenceScore_StanfordNLP(ArrayList<Citation> citations)
	{
        String[] sentimentText = { "Very Negative","Negative", "Neutral", "Positive", "Very Positive"};
        
        int score;
        
		String Sentence;
		int DisplayScore = 1;
		for (Citation Citation : citations)
		{
			Properties props = new Properties();
	        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	        Sentence = "";
			for(int i = 0; i < 4; ++i)
			{
				if (Citation.SentenceScore[i] != 0) Sentence = Sentence + Citation.Sentence[i];
			}
			Annotation annotation = pipeline.process(Sentence);
	        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) 
	        {
	        	Tree tree = sentence.get(SentimentCoreAnnotations.AnnotatedTree.class);
	        	score = RNNCoreAnnotations.getPredictedClass(tree);

	        	Citation.StanfordNLPScore_explicit = score;

	        	if (DisplayScore == 1) 
				{
					System.out.println(Sentence + ": " + sentimentText[score]);
				}
	        }
		}

	}
	
	public static void computeSentenceScore_VaderSentiment(ArrayList<Citation> citations) throws IOException
	{
		String Sentence;
		int DisplayScore = 0;
		String s;
		int count = 0;
		for (Citation Citation : citations)
		{
			for (int i = 0; i < 4; ++i)
			{
				if (Citation.SentenceScore[i] != 0)
				{
					Sentence = Citation.Sentence[i];
					if (DisplayScore == 1) System.out.println(Sentence);
		            Process p = Runtime.getRuntime().exec("python vaderSentiment_test.py \"" + Sentence + "\"");
		             
		            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	            	System.out.println(count++);
		            if (DisplayScore == 1)
		            {
		            	while ((s = stdInput.readLine()) != null) 
		            	{
		            		System.out.println(s);
		            	}
		                while ((s = stdError.readLine()) != null) {
		                    System.out.println(s);
		                }
		            }				
				}
			}
		}
	}

	public static void writeToARFF(ArrayList<Citation> citations)
	{
		int features = 7;	//Including outputs
		try 
		{
			File file = new File("Outputs/Amjad/features_train.arff");
 
			// if file doesnt exists, then create it
			if (!file.exists()) 
			{
				file.createNewFile();
			}
			String initialization = "@relation polarity\n\n"
					+ "@attribute refCount real\n"
					+ "@attribute isSeparate {0,1}\n"
					+ "@attribute selfCitation {0,1}\n"
					+ "@attribute PP_1or3 {0,1}\n"
					+ "@attribute negation {0,1}\n"
					+ "@attribute speculation {0,1}\n"
					+ "@attribute polarity {1,2,3}\n\n"
					+ "@data\n";
			
			String content;
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(initialization);
			for (Citation citation : citations)
			{
				content = "";
				for (int i = 0; i < features; ++i)
				{
					if (i!=features-1)
					{
						content += citation.Features[i] + ",";
					}
					else 
					{
						content += citation.PolarityIndex + "\n";
					}
				}
				bw.write(content);
			}
			
			bw.close();
			System.out.println("ARFF File written");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public static void plotPolarityProfiles(ArrayList<Citation> citations, HashMap<String, Paper> papers) throws IOException
	{
		/*
		 * An arraylist (an entry for each polarity) of hashmaps (an entry for each paper) of yearwise citation polarity counts
		 */
		ArrayList<HashMap<String, TreeMap<Integer, Double>>> paperprofiles = new ArrayList<HashMap<String, TreeMap<Integer, Double>>>();
		for (int i = 0; i < 3; ++i)
		{
			paperprofiles.add(new HashMap<String, TreeMap<Integer, Double>>());
		}
		
		int yearpub, yeardiff;
		int polarityscore;
		double currval;
		
		for (Citation citation : citations)
		{
			if (!paperprofiles.get(citation.PolarityIndex-1).containsKey(citation.Cited)) paperprofiles.get(citation.PolarityIndex-1).put(citation.Cited, new TreeMap<Integer, Double>());
			yearpub = papers.get(citation.Cited).year;
			yeardiff = citation.year - yearpub;
			
			polarityscore = citation.PolarityIndex; 
			if (polarityscore < 1 || polarityscore > 3) polarityscore = 1;

			//If that year's entry isn't there
			if (!paperprofiles.get(citation.PolarityIndex-1).get(citation.Cited).containsKey(yeardiff)) 
			{
				paperprofiles.get(citation.PolarityIndex-1).get(citation.Cited).put(yeardiff, 0.0);
			}
			currval = paperprofiles.get(citation.PolarityIndex-1).get(citation.Cited).get(yeardiff);
			paperprofiles.get(citation.PolarityIndex-1).get(citation.Cited).put(yeardiff, currval +1);
		}
		
		String Xtitle = "Year Difference";
		String Ytitle = "Citations";
		//Now average and plot		
		int count = 0; int totalcitations;
		for (Map.Entry<String, TreeMap<Integer, Double>> e : paperprofiles.get(0).entrySet())
		{
			totalcitations = 0;
			for (int i = 0; i < 3; ++i) if(paperprofiles.get(i).containsKey(e.getKey())) totalcitations += paperprofiles.get(i).get(e.getKey()).size();
			
			String title = "Polarity Profile for " + e.getKey() + " (pub." + papers.get(e.getKey()).year + "), Citations = " + totalcitations;
			File fout = new File("Outputs/Amjad/PolarityProfiles/polprof_" + e.getKey() + ".jpg"); 
			LineChartClass lcc = new LineChartClass(title, Xtitle, Ytitle);
			
			System.out.println((count++) +" - " + e.getKey() + ", citations = " + totalcitations);
			
			lcc.addToDataset(paperprofiles.get(0).get(e.getKey()), "Neutral");
			if(paperprofiles.get(1).containsKey(e.getKey())) lcc.addToDataset(paperprofiles.get(1).get(e.getKey()), "Positive");
			if(paperprofiles.get(2).containsKey(e.getKey())) lcc.addToDataset(paperprofiles.get(2).get(e.getKey()), "Negative");
			lcc.makePlot();
			lcc.save(fout);
			lcc.clearDataset();
		}
		
	}
	

	public static int getBFSDistanceto3POS(SemanticGraph G, IndexedWord source, ArrayList<ArrayList<String>> targetPOS, ArrayList<IndexedWord> visitednodes, int distance)
	{
		int BFSDistance;
		visitednodes.add(source);
//		for(IndexedWord child : G.getChildren(source))
		if(G.containsVertex(source))
		{
			for (IndexedWord child : G.getParentList(source))
			{
				if(visitednodes.contains(child)) continue;
	//			if(child.equals(dest)) return 1;
				for(ArrayList<String> list : targetPOS)
				{
					for(String pos : list)
					{
						if(child.toString().contains(pos)) return 1;
					}
				}
			}
			
	//		for(IndexedWord child : G.getChildren(source))
			for(IndexedWord child : G.getParentList(source))
			{
				if(visitednodes.contains(child)) continue;
				visitednodes.add(child);
				BFSDistance = getBFSDistanceto3POS(G, child, targetPOS, visitednodes, distance);
				if(BFSDistance != Integer.MAX_VALUE) distance = Math.min(BFSDistance + 1, distance);
			}
		}
		return distance;
	}
	
	public static void getNegationCues() throws IOException
	{
		String[] negprefix = {"in", "un", "non", "de", "dis", "a", "anti", "im", "il", "ir"};
		String[] negsuffix = {"n't"};
		List<String> negprefixlist = Arrays.asList(negprefix);
		List<String> negsuffixlist = Arrays.asList(negsuffix);
		
		File fin = new File("Datasets/SEM-2012-SharedTask-CD-SCO-training-09032012.txt");
		File negationfile = new File("Outputs/negationcues.tmp");
		HashSet<String> negations = new HashSet<String>();
		
		if (negationfile.exists()) negations = FileOperations.readObject(negationfile);
		
		String line;
		String[] linesplit;
		int linesize, maxnegations; 
		
		BufferedReader in = new BufferedReader(new FileReader(fin));
		
		while((line = in.readLine()) != null)
		{
			linesplit = line.split("\t");
			linesize = linesplit.length;
			
			maxnegations = (linesize - 8)/3 + 1;
			
			for(int i = 0; i < maxnegations; ++i)
			{
				if(linesplit[7+3*i].equals("***") || linesplit[7+3*i].equals("_")) continue;
				else	//Negation found!
				{
//					if(linesplit[7+3*i].equals("at")) System.out.println(line);
					
					if(negprefixlist.contains(linesplit[7+3*i])) negations.add((linesplit[7+3*i] + linesplit[8+3*i]).toLowerCase());
					else if(negsuffixlist.contains(linesplit[7+3*i])) negations.add((linesplit[8+3*i] + linesplit[7+3*i]).toLowerCase());
					else negations.add(linesplit[7+3*i].toLowerCase());
				}					
			}			
		}
		System.out.println(negations.toString());
			
		FileOperations.writeObject(negations, negationfile);
	}

	public static void getSpeculationCues(File fin) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(fin));
		File fout = new File("Outputs/speculationcues.tmp");
		
		HashSet<String> speculations = new HashSet<String>();
		if(fout.exists()) speculations = FileOperations.readObject(fout);
		
		String line;
		int matchedAt, matchEnd;
		String regex = "<cue type=\"speculation\" ref=\".+\">";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = null;
		String cue;
		
		while((line = in.readLine())!=null)
		{
			matcher = pattern.matcher(line);
			while(matcher.find())
			{
				matchedAt = matcher.start() + matcher.group().length();
				matchEnd = line.substring(matchedAt, line.length()).indexOf('<');
				cue = line.substring(matchedAt, matchedAt + matchEnd);
				
				speculations.add(cue.toLowerCase());
			}
		}
		FileOperations.writeObject(speculations, fout);
		System.out.println(speculations.toString());
		System.out.println(speculations.size());
		
	}
}