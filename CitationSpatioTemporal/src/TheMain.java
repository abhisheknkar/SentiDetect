import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMain 
{

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();

		HashMap <String, AANPaper> AANPapers = AANOperations.readAANMetadata();
		HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork = AANOperations.formCoAuthorshipNetwork(AANPapers);
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = AANOperations.formCitationNetwork(AANPapers);
		
		HashMap<Integer, List<Double>> YearDiffvsDistance = null;
		YearDiffvsDistance = AANOperations.runAlgo01Part1(CoAuthorshipNetwork, CitationNetwork, AANPapers);
		AANOperations.runAlgo01Part2(YearDiffvsDistance,1);

//		AANOperations.getIndividualCitationProfile(100, 25);
		
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}