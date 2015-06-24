import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

//All members are static functions
public class AmjadFeatures 
{
	private final String[] PronounList = {"i", "me", "my", "mine", "we", "us", "we", "our", "ours",
			"he", "she", "it", "they", "him", "her", "them","his", "her", "its", "their", "theirs"}; 
	public static void getFeatures1and2(ArrayList<Citation> citations)
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

	public static void getFeature3(ArrayList<Citation> citations, HashMap<String, Paper> papers)
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

}