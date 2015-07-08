import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLP 
{	
    public static void main(String[] args) throws IOException
    {
		long startTime = System.currentTimeMillis();

		String text = "These problems formulations are similar to those studied in <REF>Ramshaw and Marcus, 1995</REF> and <TREF>Church, 1988</TREF>; <REF>Argamon et al , 1998</REF>, respectively"; // Add your text here!

    	Properties props = new Properties();
     // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
        props.put("annotators", "tokenize, ssplit, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
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
		  Tree tree = sentence.get(TreeAnnotation.class);
		  System.out.println(tree.toString());
		
		  // this is the Stanford dependency graph of the current sentence
		  SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		  System.out.println(dependencies.toCompactString());
		  System.out.println(getClassofNodebyString(dependencies, "<TREF>"));
     }	
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
    }

	public static String getClassofNodebyString(SemanticGraph dependencies, String input)
	{
		String output = "-1";
		List<IndexedWord> result = dependencies.getAllNodesByWordPattern(input);
		if (result.size() > 0)
		{
			output = result.get(0).toString();
			output = output.substring(output.lastIndexOf('-') + 1, output.length());
		}
		return output;
	}

	public static String getClassofNode(IndexedWord input)
	{
		String output = "-1";
		output = input.toString();
		output = output.substring(output.lastIndexOf('-') + 1, output.length());
		return output;
	}
}



