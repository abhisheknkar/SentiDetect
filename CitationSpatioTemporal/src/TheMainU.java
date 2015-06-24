import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMainU 
{
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();

		HashMap<String, Paper> papers = DatasetReader.readAANMetadata();
//		HashMap<String, Integer> authorhashmap = DatasetOperations.getAuthorHashMap(papers);
//		CoAuthorshipNetworkYW coauthorshipnetwork = DatasetOperations.formCoAuthorshipNetworkYW(papers);
		CitationNetworkYW cn = DatasetOperations.formCitationNetworkYW(papers);
		
		for(Map.Entry<Integer, CitationNetwork> e : cn.network.entrySet())
		{
			for (CitationLink c : e.getValue().network.getEdges())
			{
//				System.out.println(e.getValue().network.getEndpoints(c).toString());
			}
		}
/*		
		for (Map.Entry<Integer, CoAuthorshipNetwork> e : coauthorshipnetwork.network.entrySet())
		{
			System.out.println("Year: " + e.getKey() + " " + e.getValue().network.toString());// + ", Map: " + e.getValue());
		}
*/	
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}

}
