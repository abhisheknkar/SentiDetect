import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.dcoref.sievepasses.PronounMatch;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
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
	private static String nearestPOS = "-1";
	private static final String[] PronounList = {"i", "me", "my", "mine", "we", "us", "we", "our", "ours",
			"he", "she", "it", "they", "him", "her", "them","his", "her", "its", "their", "theirs"}; 
	private static final String[] contrastList = {"yet", "and yet", "nevertheless", "nonetheless", 
			"after all", "however", "though", "otherwise", "on the contrary", 
			"in contrast", "notwithstanding", "on the other hand", "at the same time",
			"although this may be true", "in spite of", "be that as it may", "then again",
			"in reality", "after all", "albeit", "although", "whereas", "despite", "regardless"};
	
	private final static String[] POSadv = {"RB","RBR","RBS", "WRB"};
	private final static String[] POSverb = {"VB","VBD","VBG","VBN","VBP","VBZ"};
	private final static String[] POSadj = {"JJ","JJR","JJS"};
	private static final String[] demonstratives = {"this", "that", "these", "those"};
	
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

	public static ArrayList<Citation> getFeature6(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * To detect presence of contrast expressions
		 */
//		int count = 0;
		boolean found = false;
		for(Citation citation : citations)
		{
			for (int i = 0; i < citation.Sentence.length; ++i)
			{
				if(citation.SentenceScore[i] != 0)
				{
					for (int j = 0; j < contrastList.length; ++j)
					{
						found = citation.Sentence[i].matches("(?i).*\\b"+ contrastList[j] +"\\b.*");
						if (found) 
						{
//							System.out.println(count++ + "," + contrastList[j]);
							citation.Features[6]=1;
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
	
	public static ArrayList<Citation> getFeature8(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Closest subjectivity cue
		 * Start from TREF, go left first, then right in search of subjective words.
		 * For the closest word, get the word and the score
		 */
		String[] sentencesplit;
		int TREFindex = -1;
		int mindist = Integer.MAX_VALUE;
		int minindex = -1;
		int score = 0;
		String type, priorpolarity;

		ArrayList<Integer> subjIndices = new ArrayList<Integer>();
		
		HashMap<String, OpinionFinderWord> subjwords = DatasetReader.readDataset_OpinionFinder();
//		System.out.println(subjwords.keySet().toString());
		
		for (Citation citation : citations)
		{
			TREFindex = -1;
			subjIndices.clear();
			sentencesplit = citation.Sentence[1].split(" ");

			for(int i = 0; i < sentencesplit.length; ++i)
			{
				if(sentencesplit[i].contains("<TREF>"))
				{
					TREFindex = i;
				}
				else
				{
					//Check if subjective
//					System.out.println(sentencesplit[i]);
					if(subjwords.containsKey(sentencesplit[i].toLowerCase()))
					{
						subjIndices.add(i);
					}
				}
			}
			//Find closest one
			mindist = Integer.MAX_VALUE;
			minindex = -1;
			if(TREFindex != -1)
			{
				for (Integer index : subjIndices)
				{
					if(Math.abs(TREFindex - index) < mindist)
					{
						minindex = index;
						mindist = Math.abs(TREFindex - index);
					}
				}
				if(minindex != -1)
				{
					score = 1;
					type = subjwords.get(sentencesplit[minindex].toLowerCase()).Type;
					priorpolarity = subjwords.get(sentencesplit[minindex].toLowerCase()).PriorPolarity;
					if(type.equals("strongsubj")) score *= 2;
					
					if(priorpolarity.equals("negative")) score = -score;
					else if(priorpolarity.equals("neutral")) score = 0;

//					System.out.println("Score: " + citation.Sentence[1]);
//					System.out.println("Closest Subj Word: " + sentencesplit[minindex]);
				}
				else score = 0;
				citation.Features[8] = score;
//				System.out.println(score);
			}
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
		File POS3file = new File("Outputs/Amjad/POS3.tmp");

		HashMap<String, Integer> depmap = new HashMap<String, Integer>();
		HashMap<String, Integer> POS3map = new HashMap<String, Integer>();
		if(depfile.exists()) depmap = FileOperations.readObject(depfile);
		if(POS3file.exists()) POS3map = FileOperations.readObject(POS3file);
		
		String source_string, dest_string, type_string, to_write;
		int source_till, dest_till, count=0;
		int distance = Integer.MAX_VALUE;
		String text;
		ArrayList<ArrayList<String>> targetPOS = new ArrayList<ArrayList<String>>();
		ArrayList<String> a = new ArrayList<String>(Arrays.asList(POSadv));
		ArrayList<String> b = new ArrayList<String>(Arrays.asList(POSverb));
		ArrayList<String> c = new ArrayList<String>(Arrays.asList(POSadj));
		targetPOS.add(a);targetPOS.add(b);targetPOS.add(c);

		Properties props = new Properties();
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
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

            		to_write = edge.getRelation().toString() + "_" + source_string + "_" + dest_string;
            		if (!depmap.containsKey(to_write)) depmap.put(to_write, depmap.size()); 
            	}
            	IndexedWord source = dependencies.getNodeByWordPattern("<TREF>");
            	
            	nearestPOS = "-1";
            	distance = getBFSDistanceto3POS(dependencies,source,targetPOS,new ArrayList<IndexedWord>(), Integer.MAX_VALUE);

            	//Lemmatize
            	if(!nearestPOS.equals("-1"))
            	{
            		String unlemmatized = nearestPOS.substring(0, nearestPOS.indexOf('-'));
            		for(CoreLabel token: sentence.get(TokensAnnotation.class))
                    {       
                        String word = token.get(TextAnnotation.class);      
                        if(!word.equals(unlemmatized)) continue;
                        String lemma = token.get(LemmaAnnotation.class); 
                        nearestPOS = lemma;
//                      System.out.println("Word: " + unlemmatized + ", lemmatized version :" + lemma);
                    }            		
            	}
            	
            	if (!POS3map.containsKey(nearestPOS)) POS3map.put(nearestPOS, POS3map.size());           	

        		citation.Features[9] = POS3map.get(nearestPOS);
//        		System.out.println("Sentence: " + citation.Sentence[1] + "\nClosest POS: (" + nearestPOS + ", " + POS3map.get(nearestPOS) +")\n");
            	count++;
        		if(count%10 == 0) System.out.println(count + " out of " + citations.size());
//            	System.out.println(dependencies.edgeListSorted().toString());
//            	System.out.println("Distance between TREF and required POS tag is: " + distance);            	
    	    }	        	
            citation.Features[10] = distance;
        }
        FileOperations.writeObject(depmap, depfile);
        FileOperations.writeObject(POS3map, POS3file);
        return citations;
	}	

	public static ArrayList<Citation> getContextFeature0and2(ArrayList<Citation> citations) throws IOException
	{
		boolean found = false;
		for (Citation citation : citations)
		{
			for(int i = 0; i < 4; ++i)
			{
				citation.ContextFeatures[i][2] = i - 1;
//				System.out.println(citation.ContextFeatures[i][2]);
				
				for (int j = 0; j < demonstratives.length; ++j)
				{
					found = citation.Sentence[i].toLowerCase().contains(demonstratives[j]);
					if (found) 
					{
						citation.ContextFeatures[i][0]=1;
//						System.out.println("Demonstrative: " + demonstratives[j] + ", Sentence: " + citation.Sentence[i]);
						break;
					}
				}
			}
		}
		return citations;
	}

	public static ArrayList<Citation> getContextFeature4and5(ArrayList<Citation> citations) throws IOException
	{
		/*
		 * Bi- and tri- grams
		 */
		boolean readfromfile = false;
		File bigramfile = new File("Outputs/bigramfeatures.tmp");
		File trigramfile = new File("Outputs/trigramfeatures.tmp");

		HashMap<String, Integer> bigrams = new HashMap<String, Integer>();
		HashMap<String, Integer> trigrams = new HashMap<String, Integer>();
		
		if(readfromfile)
		{
			bigrams = FileOperations.readObject(bigramfile);
			trigrams = FileOperations.readObject(trigramfile);
		}
		
		String bigram, trigram;
		String[] sentencesplit;
		
		for (Citation citation : citations)
		{
			for(int i = 0; i < 4; ++i)
			{
				sentencesplit = citation.Sentence[i].toLowerCase().split(" ");
				if(sentencesplit.length >= 2) 
				{
					bigram = sentencesplit[0] + " " + sentencesplit[1];
				}
				else bigram = "-1";

				if(sentencesplit.length >= 3) 
				{
					trigram = bigram + " " + sentencesplit[2];
				}
				else trigram = "-1";
				
				if(!bigrams.containsKey(bigram)) bigrams.put(bigram, bigrams.size());
				if(!trigrams.containsKey(trigram)) trigrams.put(trigram, trigrams.size());

				citation.ContextFeatures[i][4] = bigrams.get(bigram);
				citation.ContextFeatures[i][5] = trigrams.get(trigram);

//				System.out.println("Bigram: " + citation.ContextFeatures[i][4] + ", " + bigram + "\nTrigram: " + citation.ContextFeatures[i][5] + ", " + trigram);
			}
		}
		return citations;
	}

	public static ArrayList<Citation> getContextFeature6_7and8(ArrayList<Citation> citations) throws IOException
	{
		for (Citation citation : citations)
		{
			for(int i = 0; i < 4; ++i)
			{
				if(citation.Sentence[i].contains("<REF>"))
				{
					citation.ContextFeatures[i][6] = 1;
//					System.out.println("Sentence: " + citation.Sentence[i] + "\nContains other ref: " + citation.ContextFeatures[i][6]);
				}
				else citation.ContextFeatures[i][6] = 0;
				
				if(citation.Sentence[i].contains("<TREF>"))
				{
					citation.ContextFeatures[i][7] = 1;
//					System.out.println("Sentence: " + citation.Sentence[i] + "\nContains other ref: " + citation.ContextFeatures[i][6]);
				}
				else citation.ContextFeatures[i][7] = 0;
				
				if(findNoOfOccurrences(citation.Sentence[i], "<REF>") + findNoOfOccurrences(citation.Sentence[i], "<TREF>") > 1)
				{
					citation.ContextFeatures[i][8] = 1;
//					System.out.println("Contains multiple refs: " + citation.ContextFeatures[i][8] + "\n");
				}
				else citation.ContextFeatures[i][8] = 0;
			}
		}
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

	public static void writeToARFF_Polarity(ArrayList<Citation> citations, File file) throws IOException
	{
		int features = 10;	//Including outputs
		try 
		{
			// if file doesn't exists, then create it
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
					+ "@attribute contrary {0,1}\n"
					+ "@attribute headline {0,1,2,3,4}\n"
					+ "@attribute subjectivity {-2,-1,0,1,2}\n"
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
						if(citation.PolarityIndex == -99) content += "?\n";
						else content += citation.PolarityIndex + "\n";
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
	
	public static void writeToARFF_Context(ArrayList<Citation> citations)
	{
		int features = 8;	//Including outputs

		int[] featurestowrite = {0,2,4,5,6,7,8};
		
		try 
		{
			File file = new File("Outputs/Amjad/features_context.arff");
 
			// if file doesnt exists, then create it
			if (!file.exists()) 
			{
				file.createNewFile();
			}
			String initialization = "@relation context\n\n"
					+ "@attribute demonstrative {0,1}\n"
					+ "@attribute position {-1,0,1,2}\n"
					+ "@attribute bigram real\n"
					+ "@attribute trigram real\n"
					+ "@attribute refOther {0,1}\n"
					+ "@attribute refTarget {0,1}\n"
					+ "@attribute refMultiple {0,1}\n"
					+ "@attribute context {0,1}\n\n"
					+ "@data\n";
			
			String content;
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			int count = 0;
			bw.write(initialization);
			for (Citation citation : citations)
			{
				for (int j = 0; j < 4; ++j)
				{
					content = "";
				 	for (int i = 0; i < features; ++i)
					{
						if (i!=features-1)
						{
							content += citation.ContextFeatures[j][featurestowrite[i]] + ",";
						}
						else 
						{
							content += citation.SentenceScore[j] + "\n";
						}
					}
					bw.write(content);
//					System.out.println(count++);
				}	
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
						if(child.toString().contains(pos)) 
						{
							nearestPOS = child.toString();
							return 1;
						}
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