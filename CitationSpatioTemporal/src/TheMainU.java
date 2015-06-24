import java.util.*;
import java.io.*;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class TheMainU 
{
	private static final String METHODID = "6";
	private static final String MODE = "Sum";
	private static final int ITERATIONS = 5000;
	private static final int INFTOCONSIDER = 1;
	private static final int DEFAULTPATHLENGTH = 22;
	private static final int EARLIEST = 1990;
	private static final int CITTHRESH = 25;
	private static final int BUCKETSIZE = 25;
	private static final double ALPHA = 25;
	
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();

		String dataset = "AAN";
//		String dataset = "DBLP";

		HashMap<String, Paper> papers = null;		
		switch (dataset)
		{
			case "AAN":
				papers = DatasetReader.readAANMetadata();
				break;
			case "DBLP":
				papers = DatasetReader.readDBLPMetadata();
				break;
		}

		//Function calls go here:						
		DatasetOperations.getIndividualCitationProfile(papers, ITERATIONS, dataset, MODE, CITTHRESH, INFTOCONSIDER, DEFAULTPATHLENGTH, METHODID, EARLIEST, BUCKETSIZE, ALPHA);
		
		long endTime   = System.currentTimeMillis();		
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}


/*
		CoAuthorshipNetworkYW coauthorshipnetworkYW = DatasetOperations.formCoAuthorshipNetworkYW(papers);
		CitationNetworkYW citationnetworkYW = DatasetOperations.formCitationNetworkYW(papers);
		TreeMap<Integer, List<Double>> t = DatasetOperations.getCiterDistanceDataByYearDiff(coauthorshipnetworkYW, citationnetworkYW, papers, EARLIEST, dataset);
		DatasetOperations.plotCiterDistanceSummaryALL(dataset, METHODID, MODE, INFTOCONSIDER, DEFAULTPATHLENGTH);
		DatasetOperations.getIndividualCitationProfile(papers, ITERATIONS, dataset, MODE, CITTHRESH, INFTOCONSIDER, DEFAULTPATHLENGTH, METHODID, EARLIEST, BUCKETSIZE, ALPHA);
		System.out.println(DatasetOperations.getDiameter(new File("Outputs/" + dataset + "/CoAuthorshipDistanceMap.tmp")));
		
		//To print citation network
		for (Map.Entry<Integer, CitationNetwork> e : citationnetworkYW.network.entrySet())
		{
			System.out.println(e.getValue().network.toString());
		}

		//To print papers
		for (Map.Entry<String, Paper> paper : papers.entrySet())
		{
			paper.getValue().printPaper();
		}

*/