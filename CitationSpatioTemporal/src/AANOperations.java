import java.io.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AANOperations
{
	public static ArrayList<AANPaper> readAANMetadata() throws IOException
	{
		File fin = null;
		File fout = null;
		ObjectOutputStream oos = null;
		BufferedReader in = null;
		
		int saveFlag = 0;
		String line;
		int count = 0;
		String[] Metadata = new String[5];
		ArrayList<AANPaper> AANPapers = new ArrayList<AANPaper>();
		
		try
		{
			fin = new File("./Datasets/AAN/acl-metadata.txt");
			fout = new File("./Outputs/AANMetadata.tmp");
			in = new BufferedReader(new FileReader(fin));

		while((line = in.readLine()) != null)
		{
			if (count == 0)
			{
				if (line.contains("id"))
				{
					Metadata[0] = line;
					++count;
				}
			}
			else if (count < 5)
			{
				Metadata[count] = line;
				++count;
				if (count == 5)
				{
					AANPaper Paper = new AANPaper(Metadata);
					AANPapers.add(Paper);
					count = 0;
				}
			}
			
		}

		if (saveFlag != 0) 
		{
			FileOutputStream foutStream = new FileOutputStream(fout);
			oos = new ObjectOutputStream(foutStream);
			oos.writeObject(AANPapers);		
		}

		}
		finally
		{
			if (in!= null) in.close();
			if (oos != null) oos.close();
		}
		return AANPapers;
	}
	
	public static ArrayList<AANPaper> readAANObj(File fin) throws IOException
	{
		FileInputStream finstream = null;
		ObjectInputStream objinstream = null;
		ArrayList<AANPaper> Papers = null;
		try
		{
			finstream = new FileInputStream(fin);
			objinstream = new ObjectInputStream(finstream);
			Papers = (ArrayList<AANPaper>) objinstream.readObject(); 
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if (finstream != null)
			{
				finstream.close();
				objinstream.close();
			}
		}
		return Papers;
	}

	public static void getEarliest(ArrayList<AANPaper> AANPapers)
	{
		int earliest = Integer.MAX_VALUE;
		for (AANPaper X: AANPapers)
		{
			if (X.year < earliest) earliest = X.year;			
		}
		System.out.println(earliest);
	}
	
	public static HashMap<String, Integer> getAuthorHashMap() throws IOException
	{
		HashMap<String, Integer> AuthorHashMap = new HashMap<String, Integer>();
		File fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		try
		{
			fin = new File("./Datasets/AAN/author_ids.txt");
			in = new BufferedReader(new FileReader(fin));

			while((line = in.readLine()) != null)
			{
				linesplit = line.split("\t");
				AuthorHashMap.put(linesplit[1], Integer.parseInt(linesplit[0]));				
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		
		return AuthorHashMap;
	}
	
	public static HashMap<Integer, String> getReverseAuthorHashMap() throws IOException
	{
		HashMap<Integer, String> AuthorHashMap = new HashMap<Integer, String>();
		FileInputStream fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		try
		{
			fin = new FileInputStream("./Datasets/AAN/author_ids.txt");
			in = new BufferedReader(new InputStreamReader(fin, "UTF-16"));
			
			while((line = in.readLine()) != null)
			{
				linesplit = line.split("\t");
				AuthorHashMap.put(Integer.parseInt(linesplit[0]), linesplit[1]);				
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		
		return AuthorHashMap;
	}
	
	public static HashMap<String, CoAuthorshipNode> getAuthorNodeHashMap() throws IOException
	{
		//Create nodes for authors
		HashMap<String, CoAuthorshipNode> AuthorNodeHashMap = new HashMap<String, CoAuthorshipNode>();
		File fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		try
		{
			fin = new File("./Datasets/AAN/author_ids.txt");
			in = new BufferedReader(new FileReader(fin));

			while((line = in.readLine()) != null)
			{
				linesplit = line.split("\t");
				AuthorNodeHashMap.put(linesplit[1], new CoAuthorshipNode(Integer.parseInt(linesplit[0])));				
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		
		return AuthorNodeHashMap;
	}

	public static HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> formCoAuthorshipNetwork(ArrayList<AANPaper> Papers) throws IOException
	{
		//Nodes are of type CoAuthorNode, edges are of type CoAuthorEdge
		//Returns the yearwise coauthorship edgelist
		//For each paper in the metadata, for all authors in that paper, form cliques between them
		//For authors not detected in author list, NO NODES ARE CREATED
		
		HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork = new HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>>();
		HashMap<String, Integer> AuthorHashMap = getAuthorHashMap();
		String[] Authors;

		HashMap <String, CoAuthorshipNode> AuthorNodeHashMap = getAuthorNodeHashMap();
		for (AANPaper Paper : Papers)
		{
			//Get authors
			Authors = Paper.Authors;
			
			//If graph for the year doesn't exist, put it
			if(!CoAuthorshipNetwork.containsKey(Paper.year)) CoAuthorshipNetwork.put(Paper.year, new SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>());
			
			//Form an edge between authors with edge label as paper ID in the graph of the corresponding year
			for (int i = 0; i < Authors.length; ++i)
			{
				for (int j = i+1; j < Authors.length; ++j )
				{
//					System.out.println(Paper.ID + " : " + Authors[i] + "; " + Authors[j]);
					if (AuthorNodeHashMap.containsKey(Authors[i]) && AuthorNodeHashMap.containsKey(Authors[j]) ) 
					{
						CoAuthorshipNetwork.get(Paper.year).addEdge(new CoAuthorshipLink(Paper.ID), AuthorNodeHashMap.get(Authors[i]), AuthorNodeHashMap.get(Authors[j]), EdgeType.UNDIRECTED);
					}
				}
			}
		}
		
		return CoAuthorshipNetwork;
	}	

	public static HashMap<Integer, SparseGraph<CitationNode, CitationLink>> formCitationNetwork(ArrayList<AANPaper> Papers) throws IOException
	{	
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = new HashMap<Integer, SparseGraph<CitationNode, CitationLink>>();	
		HashMap<String, CitationNode> CitationNodeHashMap = new HashMap<String, CitationNode>();

		File fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		int year;
		
		for (AANPaper Paper : Papers)
		{
			CitationNodeHashMap.put(Paper.ID, new CitationNode(Paper.ID, Paper.year));
		}

		try
		{
			fin = new File("./Datasets/AAN/acl.txt");
			in = new BufferedReader(new FileReader(fin));

			while((line = in.readLine()) != null)
			{
				linesplit = line.split(" ==> ");
				year = CitationNodeHashMap.get(linesplit[0]).year;
				
				//If graph for the year doesn't exist, put it
				if(!CitationNetwork.containsKey(year)) CitationNetwork.put(year, new SparseGraph<CitationNode, CitationLink>());
				
				CitationNetwork.get(year).addEdge(new CitationLink(true), CitationNodeHashMap.get(linesplit[0]),CitationNodeHashMap.get(linesplit[1]));
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		return CitationNetwork;
	}	

	public static void printCoAuthorshipNetwork(HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork, int waitforkeypress) throws IOException
	{
		for(Map.Entry<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> g : CoAuthorshipNetwork.entrySet())
		{
			System.out.println(g.getKey() + " : " + g.getValue().toString());
			if (waitforkeypress == 1) System.in.read();
		}
	}
	
	public static void printCitationNetwork(HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork, int waitforkeypress) throws IOException
	{
		for(Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> g : CitationNetwork.entrySet())
		{
			System.out.println(g.getKey() + " : " + g.getValue().toString());
			if (waitforkeypress == 1) System.in.read();
		}
	}
/*
	public static void runAlgo01(HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork, HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork, ArrayList<AANPaper> AANPapers)
	{
		String[] AuthorSource;
		String[] AuthorDestination;
		
		for (Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> E : CitationNetwork.entrySet())
		{
			SparseGraph<CitationNode, CitationLink> gCitation = E.getValue();
			SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink> gCoAuthor = CoAuthorshipNetwork.get(E.getKey());

			DijkstraShortestPath<CitationNode,CitationLink> alg = new DijkstraShortestPath(gCoAuthor);			

			for (CitationLink C : gCitation.getEdges())
			{
				//Get the citer and the cited
				Pair<CitationNode> P = gCitation.getEndpoints(C);
				
				//Get the author lists
//				AuthorSource = AANPapers.
				List<CitationLink> L = alg.getPath(P.getFirst(), P.getSecond());
			}

		}
	}
*/
}

class CoAuthorshipNode
{
	int id;
	public CoAuthorshipNode(int id)
	{
		this.id = id;
	}
	public String toString()
	{
		return "V" + id;
	}
}

class CoAuthorshipLink
{
	String EdgeLabel;
	public CoAuthorshipLink(String EdgeLabel)
	{
		this.EdgeLabel = EdgeLabel;
	}
	public String toString()
	{
		return EdgeLabel;
	}
}

class CitationNode
{
	public String ID;
	public int year;
	public CitationNode(String ID, int year)
	{
		this.ID = ID;
		this.year = year;
	}
	public String toString()
	{
		return "V" + ID;
	}
}

class CitationLink
{
	boolean EdgeLabel;
	public int AuthorshipDistance;
	public CitationLink(boolean EdgeLabel)
	{
		this.EdgeLabel = EdgeLabel;
	}
	public String toString()
	{
		return String.valueOf(EdgeLabel);
	}
}
