import java.io.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AANOperations
{
	public static HashMap <String, AANPaper> readAANMetadata() throws IOException
	{
		File fin = null;
		File fout = null;
		ObjectOutputStream oos = null;
		BufferedReader in = null;
		
		int saveFlag = 0;
		String line;
		int count = 0;
		String[] Metadata = new String[5];
		HashMap <String, AANPaper> AANPapers = new HashMap <String, AANPaper>();
		
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
					AANPapers.put(Paper.ID, Paper);
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
	
	public static HashMap <String, AANPaper> readAANObj(File fin) throws IOException
	{
		FileInputStream finstream = null;
		ObjectInputStream objinstream = null;
		HashMap <String, AANPaper> Papers = null;
		try
		{
			finstream = new FileInputStream(fin);
			objinstream = new ObjectInputStream(finstream);
			Papers = (HashMap <String, AANPaper>) objinstream.readObject(); 
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

	public static void getEarliest(HashMap <String, AANPaper> AANPapers)
	{
		int earliest = Integer.MAX_VALUE;
		for (Map.Entry<String, AANPaper> X: AANPapers.entrySet())
		{
			if (X.getValue().year < earliest) earliest = X.getValue().year;			
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

	public static HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> formCoAuthorshipNetwork(HashMap <String, AANPaper> Papers) throws IOException
	{
		//Nodes are of type CoAuthorNode, edges are of type CoAuthorEdge
		//Returns the yearwise coauthorship edgelist
		//For each paper in the metadata, for all authors in that paper, form cliques between them
		//For authors not detected in author list, NO NODES ARE CREATED
		
		HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork = new HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>>();
		HashMap<String, Integer> AuthorHashMap = getAuthorHashMap();
		String[] Authors;

		HashMap <String, CoAuthorshipNode> AuthorNodeHashMap = getAuthorNodeHashMap();

		for (Map.Entry<String, AANPaper> Paper : Papers.entrySet())
		{
			//Get authors
			Authors = Paper.getValue().Authors;
			
			//If graph for the year doesn't exist, put it
			if(!CoAuthorshipNetwork.containsKey(Paper.getValue().year)) CoAuthorshipNetwork.put(Paper.getValue().year, new SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>());
			
			//Form an edge between authors with edge label as paper ID in the graph of the corresponding year
			for (int i = 0; i < Authors.length; ++i)
			{
				for (int j = i+1; j < Authors.length; ++j )
				{
//					System.out.println(Paper.getValue().ID + " : " + Authors[i] + "; " + Authors[j]);
					if (AuthorNodeHashMap.containsKey(Authors[i]) && AuthorNodeHashMap.containsKey(Authors[j]) ) 
					{
						CoAuthorshipNetwork.get(Paper.getValue().year).addEdge(new CoAuthorshipLink(Paper.getValue().ID), AuthorNodeHashMap.get(Authors[i]), AuthorNodeHashMap.get(Authors[j]), EdgeType.DIRECTED);
					}
				}
			}
		}
		
		return CoAuthorshipNetwork;
	}	

	public static HashMap<Integer, SparseGraph<CitationNode, CitationLink>> formCitationNetwork(HashMap <String, AANPaper> Papers) throws IOException
	{	
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = new HashMap<Integer, SparseGraph<CitationNode, CitationLink>>();	
		HashMap<String, CitationNode> CitationNodeHashMap = new HashMap<String, CitationNode>();

		File fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		int year;
		
		for (Map.Entry<String, AANPaper> Paper : Papers.entrySet())
		{
			CitationNodeHashMap.put(Paper.getValue().ID, new CitationNode(Paper.getValue().ID, Paper.getValue().year));
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
				
				CitationNetwork.get(year).addEdge(new CitationLink(true), CitationNodeHashMap.get(linesplit[0]),CitationNodeHashMap.get(linesplit[1]), EdgeType.DIRECTED);
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

	public static SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink> AuthorNetworkTill(int year, HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork) throws IOException
	{
		SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink> G = new SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>();
		HashMap<String, CoAuthorshipNode> AuthorNodeHashMap = getAuthorNodeHashMap();
		
		for (CoAuthorshipNode C : AuthorNodeHashMap.values())
		{
//			G.addVertex(C);
		}

		for (int curryear : CoAuthorshipNetwork.keySet())
		{
			if (curryear <= year)
			{
//				System.out.println("Curryear = " + curryear + "; Year = " + year + "\n");
				for (CoAuthorshipLink E : CoAuthorshipNetwork.get(curryear).getEdges())
				{
					G.addEdge(E, CoAuthorshipNetwork.get(curryear).getEndpoints(E).getFirst(), CoAuthorshipNetwork.get(curryear).getEndpoints(E).getSecond(), EdgeType.DIRECTED);
				}
			}
		}
		return G;
	}
	public static HashMap<Integer, SparseGraph<CitationNode, CitationLink>> runAlgo01(HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork, HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork, HashMap <String, AANPaper> AANPapers) throws IOException
	{
		String[] AuthorSource;
		String[] AuthorDestination;
		int PathLength, currLength;
		HashMap<String, CoAuthorshipNode> AuthorNodeHashMap = getAuthorNodeHashMap();
/*		for (CoAuthorshipNode D : AuthorNodeHashMap.values())
		{
			System.out.println(D.toString());
		}
*/		
		HashMap<String,Integer> CoAuthorshipDistanceMap = new HashMap<String,Integer>(); 
		
		for (Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> E : CitationNetwork.entrySet())
		{
			SparseGraph<CitationNode, CitationLink> gCitation = E.getValue();
			SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink> gCoAuthor = AuthorNetworkTill(E.getKey(), CoAuthorshipNetwork);
			System.out.println(gCoAuthor.toString());

//			System.out.println(gCoAuthor.getVertices().toString());
//			System.out.println(gCoAuthor.containsVertex(AuthorNodeHashMap.get(Integer.toString(10159))) + "," + gCoAuthor.containsVertex(AuthorNodeHashMap.get(Integer.toString(13467))));
			
			
			DijkstraShortestPath<CoAuthorshipNode,CoAuthorshipNode> alg = new DijkstraShortestPath(gCoAuthor);	

			for (CitationLink C : gCitation.getEdges())
			{
				//Get the citer and the cited
				Pair<CitationNode> P = gCitation.getEndpoints(C);
				
				//Get the author lists
				AuthorSource = AANPapers.get(P.getFirst().ID).Authors;
				AuthorDestination = AANPapers.get(P.getSecond().ID).Authors;
				
				PathLength = Integer.MAX_VALUE;
				currLength = Integer.MAX_VALUE;
				for (int i = 0; i < AuthorSource.length; ++i)
				{
					for (int j = 0; j < AuthorDestination.length; ++j)
					{
						//For each pair of authors, get their shortest path in the coauthorship network
						if (!CoAuthorshipDistanceMap.containsKey(AuthorSource[i] + "," + AuthorDestination[j]))
						{
							if(AuthorNodeHashMap.containsKey(AuthorSource[i]) && AuthorNodeHashMap.containsKey(AuthorDestination[j]))
							{
								System.out.println(AuthorNodeHashMap.get(AuthorSource[i]).toString() + "," + AuthorNodeHashMap.get(AuthorDestination[j]).toString());
								System.out.println(gCoAuthor.containsVertex(AuthorNodeHashMap.get(AuthorSource[i])) + "," + gCoAuthor.containsVertex(AuthorNodeHashMap.get(AuthorDestination[j])));
//								System.in.read();
								List<CoAuthorshipNode> L = alg.getPath(AuthorNodeHashMap.get(AuthorSource[i]),AuthorNodeHashMap.get(AuthorDestination[j]));
								currLength = L.size();
								CoAuthorshipDistanceMap.put(AuthorSource[i] + "," + AuthorDestination[j], currLength);
							}
						}
						else 
						{
							currLength = CoAuthorshipDistanceMap.get(AuthorSource[i] + "," + AuthorDestination[j]);
						}
						
						if (currLength < PathLength) PathLength = currLength;						
					}
				}
				C.AuthorshipDistance = PathLength;
				CoAuthorshipDistanceMap.put(P.getFirst().ID + "," + P.getSecond().ID, PathLength);

				E.setValue(gCitation);
			}
		}
		return CitationNetwork;
	}
	
	public static void GraphTest()
	{
		SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink> gCoAuthor = new SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>();
		
		CoAuthorshipNode v1 = new CoAuthorshipNode(1);
		CoAuthorshipNode v2 = new CoAuthorshipNode(2);
		CoAuthorshipLink e1 = new CoAuthorshipLink("E1");
		gCoAuthor.addVertex(v1);
		gCoAuthor.addVertex(v2);
//		gCoAuthor.addEdge(e1, v1, v2 , EdgeType.UNDIRECTED);

		DijkstraShortestPath<CoAuthorshipNode,CoAuthorshipNode> alg = new DijkstraShortestPath(gCoAuthor);
		
//		System.out.println("Edge source and dest: " + gCoAuthor.getEndpoints(e1).getFirst() + "," + gCoAuthor.getEndpoints(e1).getSecond());
		List<CoAuthorshipNode> L = alg.getPath(v1, v2);
		System.out.println("Shortest Path: " + L.toString());
		
	}
	
	
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
		return String.valueOf(AuthorshipDistance);
	}
}
