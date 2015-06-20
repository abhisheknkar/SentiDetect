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
		File fout1 = new File("Outputs/CoAuthorshipDistanceMap.tmp");
		File fout2 = new File("Outputs/YearDiffvsDistanceList.tmp");
		File fout3 = new File("Outputs/CitationNetworkYearWise.tmp");
		//Gets distance in the coauthorship vs difference in the years of citation and publication
		String[] AuthorSource;
		String[] AuthorDestination;
		int PathLength, currLength;
		HashMap<String, Integer> AuthorNodeHashMap = getAuthorNodeHashMap();

		HashMap<String,Integer> CoAuthorshipDistanceMap = null;
		if (fout1.exists()) CoAuthorshipDistanceMap = FileOperations.readObject(fout1);//new HashMap<String,Integer>(); 
		else CoAuthorshipDistanceMap = new HashMap<String,Integer>(); 
				
		HashMap<Integer, List<Double>> YearDiffvsDist = new HashMap<Integer, List<Double>>();
		
		String AuthorComboKey;
		
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
				if ((count % 1000) == 0)System.out.println(count);
				++count;
				for (int i = 0; i < AuthorSource.length; ++i)
				{
					for (int j = 0; j < AuthorDestination.length; ++j)
					{
						//For each pair of authors, get their shortest path in the coauthorship network
						//Store the paths in a Map
						AuthorComboKey = AuthorSource[i] + ";" + AuthorDestination[j] + ";" + Integer.toString(gCitation.getEndpoints(C).getFirst().year);
						
						if (!CoAuthorshipDistanceMap.containsKey(AuthorComboKey))
						{
							//Check if both nodes are present
							if(AuthorNodeHashMap.containsKey(AuthorSource[i]) && AuthorNodeHashMap.containsKey(AuthorDestination[j]))
							{
								List<CoAuthorshipLink> L = alg.getPath(AuthorNodeHashMap.get(AuthorSource[i]),AuthorNodeHashMap.get(AuthorDestination[j]));
//								System.out.println(L.toString());
								if (L.size() > 0) currLength = L.size();
								else currLength = Integer.MAX_VALUE;
								CoAuthorshipDistanceMap.put(AuthorComboKey, currLength);
							}
						}
						else 
						{
							currLength = CoAuthorshipDistanceMap.get(AuthorComboKey);
						}
						
						if (currLength < PathLength) PathLength = currLength;						
					}
				}
				C.AuthorshipDistance = PathLength;
				
				if(!YearDiffvsDist.containsKey(C.yeardiff)) YearDiffvsDist.put(C.yeardiff, new ArrayList<Double>());
				YearDiffvsDist.get(C.yeardiff).add((double)PathLength);
				
				C.AuthorshipDistance = PathLength;
			}
			E.setValue(gCitation);
			gCoAuthor = null;
			gCitation = null;
		}
		
		FileOperations.writeObject(CoAuthorshipDistanceMap, fout1);
		FileOperations.writeObject(YearDiffvsDist, fout2);
		FileOperations.writeObject(CitationNetwork, fout3);
		
		return YearDiffvsDist;
	}

	public static void runAlgo01Part2(HashMap<Integer, List<Double>> YearDiffvsDistance, int toRead) throws IOException
	{
		//Get mean, median and plot
		File fin = new File("Outputs/YearDiffvsDistanceList.tmp");
		
		if(toRead == 1) YearDiffvsDistance = FileOperations.readObject(fin);
		String scope = "AAN";		
		String meantitle = "Mean distribution - " + scope;
		String mediantitle = "Median distribution - " + scope;
		String meansavepath = "Outputs\\Mean_" + scope + ".jpg";
		String mediansavepath = "Outputs\\Median_" + scope + ".jpg";
		
		getProfileFromRawData(YearDiffvsDistance, meantitle, mediantitle, meansavepath, mediansavepath);
	}

	public static void getProfileFromRawData(HashMap<Integer, List<Double>> YearDiffvsDistance, String meantitle, String mediantitle, String meansavepath, String mediansavepath) throws IOException
	{
		int infthresh = 100;
		double mean=0,median=0;
		TreeMap<Integer,Double> Means = new TreeMap<Integer,Double>();
		TreeMap<Integer,Double> Medians = new TreeMap<Integer,Double>();
			
		for(Map.Entry<Integer, List<Double>> E : YearDiffvsDistance.entrySet())
		{
			mean = GeneralOperations.getMeanOfList(E.getValue(), infthresh);
			median = GeneralOperations.getMedianOfList(E.getValue());
			
			Means.put(E.getKey(), mean);
			Medians.put(E.getKey(), median);			
		}

		LineChartClass P1 = new LineChartClass(Means, meantitle, "Year Difference", "Distance");
//		P1.plot(); 
		P1.save(new File(meansavepath));
		
		LineChartClass P2 = new LineChartClass(Medians, mediantitle, "Year Difference", "Distance");
//		P2.plot(); 
		P2.setYRange(0, 10);
		P2.save(new File(mediansavepath));
	}
	
	public static void getIndividualCitationProfile(int iterations) throws IOException
	{
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = FileOperations.readObject(new File("Outputs\\CitationNetworkYearWise.tmp"));
		
		HashMap<String, AANPaper> papermap = readAANMetadata();
		ArrayList<String> paperIDs = new ArrayList<String>(papermap.keySet());
				
		int randomIndex;
		String randomID;
		String meantitle, mediantitle, meansavepath, mediansavepath, scope = "Individual";

		for (int i = 0; i < iterations; ++i)
		{
			System.out.println("Iteration number - " + i);
			HashMap<Integer, List<Double>> YearDiffvsDist = new HashMap<Integer, List<Double>>();		
			randomIndex = (int) Math.round(Math.random()*paperIDs.size());	//Strange, but works
			
			randomID = paperIDs.get(randomIndex);

			for (Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> entry : CitationNetwork.entrySet())
			{
				for(CitationNode n : entry.getValue().getVertices())
				{
					if(n.ID.equals(randomID))
					{
						for (CitationLink c : entry.getValue().getIncidentEdges(n))
						{
							if (!YearDiffvsDist.containsKey(c.yeardiff)) YearDiffvsDist.put(c.yeardiff, new ArrayList<Double>());
							YearDiffvsDist.get(c.yeardiff).add((double)c.AuthorshipDistance); 
						}
					}
				}
			}
/*			for (Map.Entry<Integer, List<Double>> e : YearDiffvsDist.entrySet())
			{
				System.out.println(e.getKey() + " - " + e.getValue().toString());
			}
*/			
			meantitle = "Mean distribution - " + scope;
			mediantitle = "Median distribution - " + scope;
			meansavepath = "Outputs\\MeanProfiles\\Mean_" + randomID + ".jpg";
			mediansavepath = "Outputs\\MedianProfiles\\Median_" + randomID + ".jpg";

			getProfileFromRawData(YearDiffvsDist, meantitle, mediantitle, meansavepath, mediansavepath);
		}
		
	}
	
	public static void getCitationNetworkStatistics() throws IOException
	{
		int count1=0, count2=0;
		HashMap <Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = FileOperations.readObject(new File("Outputs\\CitationNetworkYearWise.tmp"));
		for (Map.Entry<Integer, SparseGraph<CitationNode, CitationLink>> E : CitationNetwork.entrySet())
		{
			for (CitationLink C : E.getValue().getEdges())
			{
				++count2;
				if (C.AuthorshipDistance < 100)	
				{
//					System.out.println(C.toString());
					++count1;
				}
			}
		}
		System.out.println(count1 + " out of " + count2 + " edges have finite coauthorship distance.");
	}



}

class CoAuthorshipLink implements Serializable
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

class CitationNode implements Serializable
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

class CitationLink implements Serializable
{
	boolean EdgeLabel;
	public Integer AuthorshipDistance = Integer.MAX_VALUE;
	public Integer yeardiff;
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