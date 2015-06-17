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
		HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork = AANOperations.formCoAuthorshipNetwork(AANPapers);
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = AANOperations.formCitationNetwork(AANPapers);

//		AANOperations.printCoAuthorshipNetwork(CoAuthorshipNetwork, 0);
		AANOperations.printCitationNetwork(CitationNetwork, 0);
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}