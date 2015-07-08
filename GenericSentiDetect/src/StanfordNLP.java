import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.*;

import javax.security.auth.kerberos.DelegationPermission;

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
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
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
import edu.uci.ics.jung.graph.SparseGraph;

public class StanfordNLP 
{	
	private final static String[] POSadv = {"RB","RBR","RBS", "WRB"};
	private final static String[] POSverb = {"VB","VBD","VBG","VBN","VBP","VBZ"};
	private final static String[] POSadj = {"JJ","JJR","JJS"};
	
/*	public static void main(String[] args) throws IOException
    {		
		long startTime = System.currentTimeMillis();
		int distance;
		ArrayList<ArrayList<String>> targetPOS = new ArrayList<ArrayList<String>>();
		ArrayList<String> a = new ArrayList<String>(Arrays.asList(POSadv));
		ArrayList<String> b = new ArrayList<String>(Arrays.asList(POSverb));
		ArrayList<String> c = new ArrayList<String>(Arrays.asList(POSadj));
		targetPOS.add(a);targetPOS.add(b);targetPOS.add(c);

		String text = "The noun phrase parser identifies simple non-recursive noun phrases such as DetAdjN or NN The method used for this process involves an algorithm of the type described in <TREF>Church 1988</TREF>"; // Add your text here!
//		String text = "<TREF> kicked some crap!";

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
        	// Tree tree = sentence.get(TreeAnnotation.class);
        	// System.out.println(tree.toString());
			
        	// this is the Stanford dependency graph of the current sentence
        	SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
        	IndexedWord source = dependencies.getNodeByWordPattern("<TREF>");
        	distance = getBFSDistanceto3POS(dependencies,source,targetPOS,new ArrayList<IndexedWord>(), Integer.MAX_VALUE);
//        	System.out.println(dependencies.toString());
        	System.out.println("Distance between TREF and required POS tag is: " + distance);
	     }	
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
    }
*/
	
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
}