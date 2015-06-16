import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMain 
{

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();

		ArrayList<AANPaper> AANPapers = AANOperations.readAANMetadata();
		HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork = AANOperations.formCoAuthorshipNetwork(AANPapers);
//		AANOperations.printCoAuthorshipNetwork(CoAuthorshipNetwork, 0);
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		

	}
}