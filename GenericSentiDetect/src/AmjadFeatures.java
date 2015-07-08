import java.io.*;
import java.util.*;
import edu.stanford.nlp.dcoref.sievepasses.PronounMatch;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
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

	public static void getFeatures0and1(ArrayList<Citation> citations)
	{
		/*
		 * Number of references and if the target reference is separate from the rest
		 * Handles the first two features
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
					delta = findNoOfOccurrences(citation.Sentence[i], "REF>") / 2;
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
	}

	public static void getFeature2(ArrayList<Citation> citations, HashMap<String, Paper> papers)
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
	}
	
	public static void getFeature3(ArrayList<Citation> citations)
	{
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
	}
	
	public static void getFeature8(ArrayList<Citation> citations)
	{
		int distance;
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
            	IndexedWord source = dependencies.getNodeByWordPattern("<TREF>");
            	distance = StanfordNLP.getBFSDistanceto3POS(dependencies,source,targetPOS,new ArrayList<IndexedWord>(), Integer.MAX_VALUE);
//            	System.out.println(dependencies.toString());
            	System.out.println("Distance between TREF and required POS tag is: " + distance);
    	     }	
        	
        }
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
		int features = 5;	//Including outputs
		try 
		{
			File file = new File("Outputs/Amjad/4features.arff");
 
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

/*		for (Map.Entry<String, TreeMap<Integer, Double>> e : paperprofiles.get(0).entrySet())
		{
			System.out.println("Paper: " + e.getKey());
			for (int j = 0; j < 3; ++j)
			{
				System.out.println("For j = " + j + ", Profile:\n" + paperprofiles.get(j).get(e.getKey()));
			}
		}
*/		
		
	}

}