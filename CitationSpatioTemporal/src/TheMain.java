import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMain 
{

	public static void main(String[] args) throws IOException
	{
		ArrayList<AANPaper> AANPapers = AANOperations.readAANMetadata();
		HashMap<Integer, SparseMultigraph<Integer, String>> CoAuthorshipNetwork = AANOperations.formCoAuthorshipNetwork(AANPapers);
			
		for(Map.Entry<Integer, SparseMultigraph<Integer, String>> g : CoAuthorshipNetwork.entrySet())
		{
			System.out.println(g.getKey() + " : " + g.getValue().toString());
		}
	}
}