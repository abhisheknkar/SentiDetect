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
		HashMap<Integer, List<Double>> YearDiffvsDistance = AANOperations.runAlgo01Part1(CoAuthorshipNetwork, CitationNetwork, AANPapers);
		AANOperations.runAlgo01Part2(YearDiffvsDistance);
//		AANOperations.GraphTest();
//		AANOperations.printCitationNetwork(CitationNetwork, 0);
		
		File fin = new File("Outputs/CoAuthorshipDistanceMap.tmp");
		HashMap<String,Integer> CoAuthorshipDistanceMap = FileOperations.readObject(fin);
		for (Map.Entry<String, Integer> C : CoAuthorshipDistanceMap.entrySet())
		{
			System.out.println(C.getKey() + "---" + Integer.toString(C.getValue()));
		}
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}