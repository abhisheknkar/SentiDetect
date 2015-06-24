import java.io.*;
import java.util.*;

import javax.print.DocFlavor.STRING;

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
		System.out.println("Yearwise coauthorship network obtained!");
		return coauthorshipnetwork;
	}
	
	public static CitationNetworkYW formCitationNetworkYW(HashMap <String, Paper> papers, String dataset) throws IOException
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
		SparseGraph<String, Integer> citationnetworkall = null;
		switch(dataset)
		{
		case ("AAN"):
			citationnetworkall = DatasetReader.getAANCitations();
			break;
		case("DBLP"):
			citationnetworkall = DatasetReader.getDBLPCitations(papers);
			break;
		}

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
		System.out.println("Yearwise citation network obtained!");
		return citationnetworkYW;
	}	

	public static CoAuthorshipNetwork AuthorNetworkTill(int year, CoAuthorshipNetworkYW coauthorshipnetworkYW, HashMap<String, Paper> papers) throws IOException
	{
		CoAuthorshipNetwork G = new CoAuthorshipNetwork();
		HashMap<String, Integer> authorhashmap = getAuthorHashMap(papers);
		
		for (Integer C : authorhashmap.values())
		{
			G.network.addVertex(C);
		}

		for (int curryear : coauthorshipnetworkYW.network.keySet())
		{
			if (curryear <= year)
			{
//				System.out.println("Curryear = " + curryear + "; Year = " + year + "\n");
				for (CoAuthorshipLink E : coauthorshipnetworkYW.network.get(curryear).network.getEdges())
				{
					G.network.addEdge(E, coauthorshipnetworkYW.network.get(curryear).network.getEndpoints(E).getFirst(), coauthorshipnetworkYW.network.get(curryear).network.getEndpoints(E).getSecond(), EdgeType.DIRECTED);
				}
			}
		}
		return G;
	}

	public static TreeMap<Integer, List<Double>> getCiterDistanceDataByYearDiff(CoAuthorshipNetworkYW coauthorshipnetworkYW, CitationNetworkYW citationnetworkYW, HashMap <String, Paper> papers, int earliest, String Dataset) throws IOException
	{
		/*
		 * For all citations, we compute the corresponding coauthorship distance and classify
		 * by the difference in years of citation and publication
		 */
		//The files containing precomputed data
		File fout1 = new File("Outputs/" + Dataset + "/CoAuthorshipDistanceMap.tmp");
		File fout2 = new File("Outputs/" + Dataset + "/YearDiffvsDistanceList.tmp");
		File fout3 = new File("Outputs/" + Dataset + "/CitationNetworkYearWise.tmp");

		int PathLength, currLength;
		int count = 0;
		String[] AuthorSource;
		String[] AuthorDestination;
		String AuthorComboKey;
		TreeMap<Integer, List<Double>> yeardiffVSdist = new TreeMap<Integer, List<Double>>();
		HashMap<String, Integer> authorhashmap = getAuthorHashMap(papers);
		HashMap<String,Integer> coauthorshipdistancemap = null;

		if (fout1.exists()) coauthorshipdistancemap = FileOperations.readObject(fout1);//new HashMap<String,Integer>(); 
		else coauthorshipdistancemap = new HashMap<String,Integer>(); 
						
		System.out.println("Iterating over edges in yearwise citation network...");
		for (Map.Entry<Integer, CitationNetwork> E : citationnetworkYW.network.entrySet())
		{
			CitationNetwork gCitation = E.getValue();
			CoAuthorshipNetwork gCoAuthor = AuthorNetworkTill(E.getKey(), coauthorshipnetworkYW, papers);			
			
			DijkstraShortestPath<Integer,CoAuthorshipLink> alg = new DijkstraShortestPath(gCoAuthor.network);	

			for (CitationLink C : gCitation.network.getEdges())
			{
				//Get the citer and the cited
				Pair<CitationNode> P = gCitation.network.getEndpoints(C);
				if(papers.get(P.getSecond().ID).year > earliest)
				{
					//Get the author lists
					AuthorSource = papers.get(P.getFirst().ID).authors;
					AuthorDestination = papers.get(P.getSecond().ID).authors;
					
					PathLength = Integer.MAX_VALUE;
					currLength = Integer.MAX_VALUE;
					if ((count % 10) == 0)System.out.println(count + " edges read.");
					++count;
					for (int i = 0; i < AuthorSource.length; ++i)
					{
						for (int j = 0; j < AuthorDestination.length; ++j)
						{
							//For each pair of authors, get their shortest path in the coauthorship network
							//Store the paths in a Map
							AuthorComboKey = AuthorSource[i] + ";" + AuthorDestination[j] + ";" + Integer.toString(gCitation.network.getEndpoints(C).getFirst().year);
							
							if (!coauthorshipdistancemap.containsKey(AuthorComboKey))
							{
								//Check if both nodes are present
								if(authorhashmap.containsKey(AuthorSource[i]) && authorhashmap.containsKey(AuthorDestination[j]))
								{
									List<CoAuthorshipLink> L = alg.getPath(authorhashmap.get(AuthorSource[i]),authorhashmap.get(AuthorDestination[j]));
	//								System.out.println(L.toString());
									if (L.size() > 0) currLength = L.size();
									else currLength = Integer.MAX_VALUE;
	
									coauthorshipdistancemap.put(AuthorComboKey, currLength);
								}
							}
							else 
							{
								currLength = coauthorshipdistancemap.get(AuthorComboKey);
							}
							
							if (currLength < PathLength) PathLength = currLength;						
						}
					}
					C.AuthorshipDistance = PathLength;
					
					if(!yeardiffVSdist.containsKey(C.yeardiff)) yeardiffVSdist.put(C.yeardiff, new ArrayList<Double>());
					yeardiffVSdist.get(C.yeardiff).add((double)PathLength);
					
					C.AuthorshipDistance = PathLength;
				}
			}
			E.setValue(gCitation);
			gCoAuthor = null;
			gCitation = null;
		}

		System.out.println("All edges read!\nStoring outputs and distance maps...");
		File outputfolder = new File("Outputs/" + Dataset);
		outputfolder.mkdirs();

		FileOperations.writeObject(coauthorshipdistancemap, fout1);
		FileOperations.writeObject(yeardiffVSdist, fout2);
//		FileOperations.writeObject(CitationNetwork, fout3);
		
		return yeardiffVSdist;
	}

	public static void plotCiterDistanceSummaryALL(String dataset, String methodid, String mode, int infToConsider, int defaultpathlength) throws IOException
	{
		//Get mean, median and plot
		File fin = new File("Outputs/" + dataset + "/YearDiffvsDistanceList.tmp");
		TreeMap<Integer, List<Double>> yeardiffVSdist  = FileOperations.readObject(fin);
		
		String scope = dataset;		
		String meantitle = mode + " distribution - " + scope;
		String mediantitle = "Median distribution - " + scope;
		String meansavepath = "Outputs/" + dataset + "/" + mode +"Profiles/Method" + methodid + "/Mean_" + scope + ".jpg";
		String mediansavepath = "Outputs/" + dataset + "/MedianProfiles/Method" + methodid + "/Median_" + scope + ".jpg";
		
		System.out.println("Obtaining summaries...");
		File fin2 = new File("Outputs/" + dataset + "/" + mode + "Profiles/Method" + methodid);
		fin2.mkdirs();
		fin2 = new File("Outputs/" + dataset + "/MedianProfiles/Method" + methodid);
		fin2.mkdirs();
		
		getProfileFromRawData(yeardiffVSdist, meantitle, mediantitle, meansavepath, mediansavepath, infToConsider, defaultpathlength, mode);
		System.out.println("Summaries saved!");
	}

	public static void getProfileFromRawData(TreeMap<Integer, List<Double>> yeardiffVSdist, String meantitle, String mediantitle, String meansavepath, String mediansavepath, int infToConsider, int defaultpathlength, String mode) throws IOException
	{
		int infthresh = 100;
				
		double mean=0,median=0;
		TreeMap<Integer,Double> Means = new TreeMap<Integer,Double>();
		TreeMap<Integer,Double> Medians = new TreeMap<Integer,Double>();
			
		for(Map.Entry<Integer, List<Double>> E : yeardiffVSdist.entrySet())
		{
			if (mode.equals("Mean"))
			{
				mean = GeneralOperations.getMeanOfList(E.getValue(), infthresh, infToConsider, defaultpathlength);
				median = GeneralOperations.getMedianOfList(E.getValue());
			}
			else
			{
				mean = GeneralOperations.getSumOfList(E.getValue(), infthresh, infToConsider, defaultpathlength);
				median = GeneralOperations.getMedianOfList(E.getValue());
			}	
			
			Means.put(E.getKey(), mean);
			Medians.put(E.getKey(), median);			
		}

		LineChartClass P1 = new LineChartClass(Means, meantitle, "Year Difference", "Distance");
//		P1.plot(); 
		P1.save(new File(meansavepath));
		
		LineChartClass P2 = new LineChartClass(Medians, mediantitle, "Year Difference", "Distance");
//		P2.plot(); 
		P2.setYRange(0, 15);
		P2.save(new File(mediansavepath));
	}

	public static void getIndividualCitationProfile(HashMap<String, Paper> papers, int iterations, String dataset, String mode, int citthresh, int infToConsider, int defaultpathlength, String Method, int earliest, int bucketsize, double alpha) throws IOException
	{
		Process p;
		int bucketnumber;
		HashMap<Integer, SparseGraph<CitationNode, CitationLink>> CitationNetwork = FileOperations.readObject(new File("Outputs/AAN/CitationNetworkYearWise.tmp"));
		
		ArrayList<String> paperIDs = new ArrayList<String>(papers.keySet());
				
		int randomIndex, citcount;
		String randomID;
		String meantitle, mediantitle, meansavepath, mediansavepath, scope = "Individual", meanfolder, medianfolder;

		for (int i = 0; i < iterations; ++i)
		{
			TreeMap<Integer, List<Double>> YearDiffvsDist = new TreeMap<Integer, List<Double>>();		
			randomIndex = (int) Math.round(Math.random()*paperIDs.size());	//Strange, but works
			
			randomID = paperIDs.get(randomIndex);
			if (papers.get(randomID).year > earliest)
			{
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
	
				//Get total citations, see if they exceed citation threshold
				citcount = 0;
				for(Map.Entry<Integer, List<Double>> entry : YearDiffvsDist.entrySet())
				{
					citcount += entry.getValue().size();
				}
				
				if (citcount >= citthresh)
				{
					System.out.println("Iteration number - " + i);
					meantitle = "Mean distribution - " + scope;
					mediantitle = "Median distribution - " + scope;
					
					bucketnumber = citcount / bucketsize;
//					bucketnumber = getBucketNumberAlpha(YearDiffvsDist, alpha, citcount);
								
					meanfolder = "Outputs/" + dataset + "/" + mode + "Profiles/Method" + Method + "/" + bucketnumber;
					medianfolder = "Outputs/" + dataset + "/MedianProfiles/Method" + Method + "/" + bucketnumber;
					
					meansavepath = meanfolder + "/Mean_" + randomID + ".jpg";
					mediansavepath = medianfolder + "/Median_" + randomID + ".jpg";
		
					File file1 = new File(meanfolder); 
					if (!file1.exists()) file1.mkdirs();
					File file2 = new File(medianfolder); 
					if (!file2.exists()) file2.mkdirs();
					
					getProfileFromRawData(YearDiffvsDist, meantitle, mediantitle, meansavepath, mediansavepath, infToConsider, defaultpathlength, mode);
				}
			}
		}
	}
	
	public static int getDiameter(File SPFile) throws IOException
	{
		int diameter = 0;
		HashMap<String,Integer> coauthorshipdistancemap = FileOperations.readObject(SPFile);
		
		for (Map.Entry<String, Integer> e : coauthorshipdistancemap.entrySet())
		{
			if ((e.getValue() > diameter) && e.getValue()<100) diameter = e.getValue();
		}
		return diameter;
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