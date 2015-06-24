import java.io.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class DatasetOperations 
{
	public static HashMap<String, Integer> getAuthorHashMap(HashMap<String, Paper> papers) throws IOException
	{
		HashMap<String, Integer> authorhashmap = new HashMap<String, Integer>();
		int count = 0;
		
		for (Paper paper : papers.values())
		{
			for (int i = 0; i < paper.authors.length; ++i)
			{
				if (!authorhashmap.containsKey(paper.authors[i]))
				{
					authorhashmap.put(paper.authors[i], count++);
				}
			}
		}
		
		return authorhashmap;
	}
	
	public static CoAuthorshipNetworkYW formCoAuthorshipNetworkYW(HashMap <String, Paper> papers) throws IOException
	{
		/* Description: 
		 * Returns the yearwise coauthorship network
		 * For each paper in the metadata, for all authors in that paper, forms cliques between them
		 * For authors not detected in author list, NO NODES ARE CREATED
		 */

		//Get the author to index hashmap, initialize coauthorship network
		CoAuthorshipNetworkYW coauthorshipnetwork = new CoAuthorshipNetworkYW();
		HashMap <String, Integer> authornodehashmap = getAuthorHashMap(papers);
		String[] Authors;

		//Iterate through all papers, create edges between coauthors
		for (Map.Entry<String, Paper> paper : papers.entrySet())
		{
			//Get authors
			Authors = paper.getValue().authors;
			
			//If graph for the year doesn't exist, put it
			if(!coauthorshipnetwork.network.containsKey(paper.getValue().year)) coauthorshipnetwork.network.put(paper.getValue().year, new CoAuthorshipNetwork());
			
			//Form an edge between authors with edge label as paper ID in the graph of the corresponding year
			for (int i = 0; i < Authors.length; ++i)
			{
				for (int j = i+1; j < Authors.length; ++j )
				{
//					System.out.println(Paper.getValue().ID + " : " + Authors[i] + "; " + Authors[j]);
					if (authornodehashmap.containsKey(Authors[i]) && authornodehashmap.containsKey(Authors[j]) ) 
					{
						coauthorshipnetwork.network.get(paper.getValue().year).network.addEdge(new CoAuthorshipLink(paper.getValue().id), authornodehashmap.get(Authors[i]), authornodehashmap.get(Authors[j]), EdgeType.DIRECTED);
					}
				}
			}
		}
		return coauthorshipnetwork;
	}
	
	public static CitationNetworkYW formCitationNetworkYW(HashMap <String, Paper> papers) throws IOException
	{	
		/*
		 * Forms yearwise citation network for the dataset
		 * Nodes are papers. If a paper cites another, an edge is formed between their corresponding nodes
		 */
		
		int yearcite,yearpub;

		CitationNetworkYW citationnetworkYW = new CitationNetworkYW();	
		HashMap<String, CitationNode> citationnodehashmap = new HashMap<String, CitationNode>();

		//Create a hashmap of nodes to use in the citation network
		for (Map.Entry<String, Paper> paper : papers.entrySet())
		{
			citationnodehashmap.put(paper.getValue().id, new CitationNode(paper.getValue().id, paper.getValue().year));
		}
	
		SparseGraph<String, Integer> citationnetworkall = DatasetReader.readAANCitations();

		/*
		 * Now iterate over citationnetworkall, and for each edge there, 
		 * add the corresponding edge in the yearwise citation network
		 */
		for (Integer edge : citationnetworkall.getEdges())
		{
			//Get endpoints of the edge
			Pair<String> citercited = citationnetworkall.getEndpoints(edge);
			
			//Check if both papers are in the paper hashmap
			if (citationnodehashmap.containsKey(citercited.getFirst()) && citationnodehashmap.containsKey(citercited.getSecond()))
			{
				yearcite = citationnodehashmap.get(citercited.getFirst()).year;
				yearpub = citationnodehashmap.get(citercited.getSecond()).year;
				
				//If graph for the year doesn't exist, put it
				if(!citationnetworkYW.network.containsKey(yearcite)) citationnetworkYW.network.put(yearcite, new CitationNetwork());
				
				citationnetworkYW.network.get(yearcite).network.addEdge(new CitationLink(yearcite-yearpub), citationnodehashmap.get(citercited.getFirst()),citationnodehashmap.get(citercited.getSecond()), EdgeType.DIRECTED);
			}
		}
		return citationnetworkYW;
	}	
}

class CoAuthorshipNetworkYW implements Serializable
{
	HashMap<Integer, CoAuthorshipNetwork> network = null;
	public CoAuthorshipNetworkYW()
	{
		network = new HashMap<Integer, CoAuthorshipNetwork>();
	}
	
}

class CoAuthorshipNetwork implements Serializable
{
	SparseMultigraph<Integer, CoAuthorshipLink> network = null;
	public CoAuthorshipNetwork()
	{
		network = new SparseMultigraph<Integer, CoAuthorshipLink>();
	}
	
}

class CitationNetworkYW implements Serializable
{
	HashMap<Integer, CitationNetwork> network = new HashMap<Integer, CitationNetwork>();

	public CitationNetworkYW()
	{
		network = new HashMap<Integer, CitationNetwork>();
	}
}

class CitationNetwork implements Serializable
{
	SparseGraph<CitationNode, CitationLink> network = new SparseGraph<CitationNode, CitationLink>();

	public CitationNetwork()
	{
		network = new SparseGraph<CitationNode, CitationLink>();
	}
}

class CoAuthorshipLink implements Serializable
{
	String edgelabel;
	public CoAuthorshipLink(String edgelabel)
	{
		this.edgelabel = edgelabel;
	}
	public String toString()
	{
		return edgelabel;
	}
}

class CitationNode implements Serializable
{
	public String id;
	public int year;
	public CitationNode(String id, int year)
	{
		this.id = id;
		this.year = year;
	}
	public String toString()
	{
		return "V" + id;
	}
}

class CitationLink implements Serializable
{
	public Integer authorshipdistance = Integer.MAX_VALUE;
	public Integer yeardiff;
	public CitationLink(int yeardiff)
	{		
		this.yeardiff = yeardiff;
	}
	public String toString()
	{
		return String.valueOf(authorshipdistance)+","+String.valueOf(yeardiff);
	}
}
