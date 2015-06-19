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
	
	public static HashMap<String, Integer> getAuthorNodeHashMap() throws IOException
	{
		//Create nodes for authors
		HashMap<String, Integer> AuthorNodeHashMap = new HashMap<String, Integer>();
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
				AuthorNodeHashMap.put(linesplit[1], Integer.parseInt(linesplit[0]));				
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		
		return AuthorNodeHashMap;
	}

	public static HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> formCoAuthorshipNetwork(HashMap <String, AANPaper> Papers) throws IOException
	{
		//Nodes are of type CoAuthorNode, edges are of type CoAuthorEdge
		//Returns the yearwise coauthorship edgelist
		//For each paper in the metadata, for all authors in that paper, form cliques between them
		//For authors not detected in author list, NO NODES ARE CREATED
		
		HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork = new HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>>();
		HashMap<String, Integer> AuthorHashMap = getAuthorHashMap();
		String[] Authors;

		HashMap <String, Integer> AuthorNodeHashMap = getAuthorNodeHashMap();

		for (Map.Entry<String, AANPaper> Paper : Papers.entrySet())
		{
			//Get authors
			Authors = Paper.getValue().Authors;
			
			//If graph for the year doesn't exist, put it
			if(!CoAuthorshipNetwork.containsKey(Paper.getValue().year)) CoAuthorshipNetwork.put(Paper.getValue().year, new SparseMultigraph<Integer, CoAuthorshipLink>());
			
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
		int yearcite,yearpub;
		
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
//				System.out.println(linesplit[0] + "," + linesplit[1]);
				
				if (CitationNodeHashMap.containsKey(linesplit[0]) && CitationNodeHashMap.containsKey(linesplit[1])) 
				{
					yearcite = CitationNodeHashMap.get(linesplit[0]).year;
					yearpub = CitationNodeHashMap.get(linesplit[1]).year;
					//If graph for the year doesn't exist, put it
					if(!CitationNetwork.containsKey(yearcite)) CitationNetwork.put(yearcite, new SparseGraph<CitationNode, CitationLink>());
					
					CitationNetwork.get(yearcite).addEdge(new CitationLink(yearcite-yearpub), CitationNodeHashMap.get(linesplit[0]),CitationNodeHashMap.get(linesplit[1]), EdgeType.DIRECTED);

				}
			}
		}
		finally
		{
			if (in!= null) in.close();
		}
		return CitationNetwork;
	}	

	public static void printCoAuthorshipNetwork(HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork, int waitforkeypress) throws IOException
	{
		for(Map.Entry<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> g : CoAuthorshipNetwork.entrySet())
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

	public static SparseMultigraph<Integer, CoAuthorshipLink> AuthorNetworkTill(int year, HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork) throws IOException
	{
		SparseMultigraph<Integer, CoAuthorshipLink> G = new SparseMultigraph<Integer, CoAuthorshipLink>();
		HashMap<String, Integer> AuthorNodeHashMap = getAuthorNodeHashMap();
		
		for (Integer C : AuthorNodeHashMap.values())
		{
			G.addVertex(C);
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
	public static HashMap<Integer, List<Double>> runAlgo01Part1(HashMap<Integer, SparseMultigraph<Integer, CoAuthorshipLink>> CoAuthorshipNetwork, HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork, HashMap <String, AANPaper> AANPapers) throws IOException
	{
		//Gets distance in the coauthorship vs difference in the years of citation and publication
		String[] AuthorSource;
		String[] AuthorDestination;
		int PathLength, currLength;
		HashMap<String, Integer> AuthorNodeHashMap = getAuthorNodeHashMap();
		HashMap<String,Integer> CoAuthorshipDistanceMap = new HashMap<String,Integer>(); 
		HashMap<Integer, List<Double>> YearDiffvsDist = new HashMap<Integer, List<Double>>();
		
		int count = 0;
		
		for (Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> E : CitationNetwork.entrySet())
		{
			SparseGraph<CitationNode, CitationLink> gCitation = E.getValue();
			SparseMultigraph<Integer, CoAuthorshipLink> gCoAuthor = AuthorNetworkTill(E.getKey(), CoAuthorshipNetwork);			
			
			DijkstraShortestPath<Integer,CoAuthorshipLink> alg = new DijkstraShortestPath(gCoAuthor);	

			for (CitationLink C : gCitation.getEdges())
			{
				//Get the citer and the cited
				Pair<CitationNode> P = gCitation.getEndpoints(C);
				
				//Get the author lists
				AuthorSource = AANPapers.get(P.getFirst().ID).Authors;
				AuthorDestination = AANPapers.get(P.getSecond().ID).Authors;
				
				PathLength = Integer.MAX_VALUE;
				currLength = Integer.MAX_VALUE;
//				if ((count % 1000) == 0)System.out.println(count);
				++count;
				for (int i = 0; i < AuthorSource.length; ++i)
				{
					for (int j = 0; j < AuthorDestination.length; ++j)
					{
						//For each pair of authors, get their shortest path in the coauthorship network
						if (!CoAuthorshipDistanceMap.containsKey(AuthorSource[i] + "," + AuthorDestination[j]))
						{
							//Check if both nodes are present
							if(AuthorNodeHashMap.containsKey(AuthorSource[i]) && AuthorNodeHashMap.containsKey(AuthorDestination[j]))
							{
								List<CoAuthorshipLink> L = alg.getPath(AuthorNodeHashMap.get(AuthorSource[i]),AuthorNodeHashMap.get(AuthorDestination[j]));
//								System.out.println(L.toString());
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
				
				if(!YearDiffvsDist.containsKey(C.yeardiff)) YearDiffvsDist.put(C.yeardiff, new ArrayList<Double>());
				YearDiffvsDist.get(C.yeardiff).add((double)PathLength);
				
				CoAuthorshipDistanceMap.put(P.getFirst().ID + "," + P.getSecond().ID, PathLength);
			}
			E.setValue(gCitation);
			gCoAuthor = null;
			gCitation = null;
		}
		return YearDiffvsDist;
	}

	public static void runAlgo01Part2(HashMap<Integer, List<Double>> YearDiffvsDistance) throws IOException
	{
		//Get mean, median and plot
		int infthresh = 100;
		double tempmean=0,tempmedian=0;
		TreeMap<Integer,Double> Means = new TreeMap<Integer,Double>();
		TreeMap<Integer,Double> Medians = new TreeMap<Integer,Double>();
		Object[] temp;
		
		for(Map.Entry<Integer, List<Double>> E : YearDiffvsDistance.entrySet())
		{
//			System.out.println(E.getKey() + "," + E.getValue());
			temp = E.getValue().toArray();
			Arrays.sort(temp);
			for(int i = 0; i < temp.length; ++i)
			{
				tempmean = 0;
				if((Double) temp[i] < infthresh) tempmean += (Double)temp[i];					
			}
			if(temp.length > 0) tempmean /= (double)temp.length;
			if((temp.length)%2 == 1) tempmedian = (double) temp[(temp.length - 1)/2];
			else tempmedian = 0.5 * ( (double) (temp[temp.length/2])  + (double) temp[temp.length/2-1]);			
			
			Means.put(E.getKey(), tempmean);
			Medians.put(E.getKey(), tempmedian);
			
		}
		LineChart_AWT.Plot(Means, "Mean distribution", "Mean distribution", "Year Difference", "Distance");
		LineChart_AWT.Plot(Medians, "Median distribution", "Median distribution", "Year Difference", "Distance");
	
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
	public int AuthorshipDistance = Integer.MAX_VALUE;
	public int yeardiff;
	public CitationLink(int yeardiff)
	{		
		this.EdgeLabel = true;
		this.yeardiff = yeardiff;
	}
	public String toString()
	{
		return String.valueOf(AuthorshipDistance)+","+String.valueOf(yeardiff);
	}
}
