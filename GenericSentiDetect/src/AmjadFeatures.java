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
	public static void getRefCountandIsSeparate(ArrayList<AmjadCitation> Citations)
	{
		//Handles the first two features
		int count, delta;
		for (AmjadCitation Citation : Citations)
		{
			count = 0;
			Citation.Features[1] = 1;
			for(int i = 0; i < 4; ++i)
			{
				if(Citation.SentenceScore[i] == 1)
				{
					delta = findNoOfOccurrences(Citation.Sentence[i], "REF>") / 2;
					if (delta > 1)
					{
						Citation.Features[1] = 0;
					}
					count += delta;
				}
			}
//			System.out.println(count);
			Citation.Features[0] = count;
		}
	}

	public static void getSelfCitations(ArrayList<AmjadCitation> Citations, ArrayList<AANPaper> Papers)
	{
		String[] CiterAuthors = null;
		String[] CitedAuthors = null;
		ArrayList<String> CommonAuthorsList = new ArrayList<String>();
		
		int CiterFound=0, CitedFound=0;
		for (AmjadCitation Citation : Citations)
		{
			for (AANPaper Paper : Papers)
			{
				if (CitedFound == 0)
					if (Paper.ID.equals(Citation.Cited))
					{
						CitedFound = 1;
						CitedAuthors = Paper.Authors;
					}
				if (CiterFound == 0)
					if (Paper.ID.equals(Citation.Citer))
					{
						CiterFound = 1;
						CiterAuthors = Paper.Authors;
					}
			}
			//Get intersection
			for (int i = 0; i < CitedAuthors.length; ++i)
			{
				//Create ArrayList of Cited Authors
				if (Arrays.asList(CiterAuthors).contains(CitedAuthors[i]))
				{
					CommonAuthorsList.add(CitedAuthors[i]);
				}
			}	
			if (CommonAuthorsList.size() > 0)
			{
				Citation.Features[2] = 1;
				for (String CommonAuthor : CommonAuthorsList)
				{						
					System.out.println(CommonAuthor);
				}

				CommonAuthorsList.clear();
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
//		System.out.println(count);	//		for (AmjadCitation Citation : Citations)
		return count;
	}

	public static void computeSentenceScore_OpinionFinder(ArrayList<AmjadCitation> Citations, ArrayList<OpinionFinderWord> Words)
	{
		String Sentence;
		int PolarityThreshold = 1;
		int DisplayIndividualScores = 0;
		int NoPrintFlag = 1;
		
		for (AmjadCitation Citation : Citations)
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

	public static void computeSentenceScore_SentiWordnet(ArrayList<AmjadCitation> Citations, HashMap<String, double[]> SentiWordnetWords)
	{
		String Sentence;
		int PolarityThreshold = 1;
		int DisplayIndividualScores = 0;
		int NoPrintFlag = 1;
		
		for (AmjadCitation Citation : Citations)
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

	public static void computeSentenceScore_StanfordNLP(ArrayList<AmjadCitation> Citations)
	{
        String[] sentimentText = { "Very Negative","Negative", "Neutral", "Positive", "Very Positive"};
        
        int score;
        
		String Sentence;
		int DisplayScore = 1;
		for (AmjadCitation Citation : Citations)
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
	
	public static void computeSentenceScore_VaderSentiment(ArrayList<AmjadCitation> Citations) throws IOException
	{
		String Sentence;
		int DisplayScore = 1;
		String s;
		for (AmjadCitation Citation : Citations)
		{
			for (int i = 0; i < 4; ++i)
			{
				if (Citation.SentenceScore[i] != 0)
				{
					Sentence = Citation.Sentence[i];
					if (DisplayScore == 1) System.out.println(Sentence);
		            Process p = Runtime.getRuntime().exec("python VaderSentiment.py \"" + Sentence + "\"");
		             
		            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
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