import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMain 
{

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();

		int diameter = 22;
		
//		HashMap <String, AANPaper> AANPapers = AANOperations.readAANMetadata();
//		HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork = AANOperations.formCoAuthorshipNetwork(AANPapers);
//		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = AANOperations.formCitationNetwork(AANPapers);
		
//		TreeMap<Integer, List<Double>> YearDiffvsDistance = null;
//		YearDiffvsDistance = AANOperations.runAlgo01Part1(CoAuthorshipNetwork, CitationNetwork, AANPapers, 1965);
//		AANOperations.runAlgo01Part2(YearDiffvsDistance,1, 1, diameter);
		AANOperations.getIndividualCitationProfile(1000, 25, 1, diameter, "Method06/", 1990, 25, 0.5);
	
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}