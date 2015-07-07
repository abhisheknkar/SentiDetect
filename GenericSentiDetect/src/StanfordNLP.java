import java.util.ArrayList;
import java.util.Properties;
import java.io.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLP 
{	
    public static void main(String[] args) throws IOException
    {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
		ArrayList<Citation> Citations = AmjadOperations.readDataSet();
		AmjadFeatures.computeSentenceScore_StanfordNLP(Citations);
    }
}
