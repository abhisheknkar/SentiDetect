import java.io.*;
import java.util.*;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;


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
		int earliest = 3000;
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

	public static void printCoAuthorshipNetwork(HashMap<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> CoAuthorshipNetwork, int waitforkeypress) throws IOException
	{
		for(Map.Entry<Integer, SparseMultigraph<CoAuthorshipNode, CoAuthorshipLink>> g : CoAuthorshipNetwork.entrySet())
		{
			System.out.println(g.getKey() + " : " + g.getValue().toString());
			if (waitforkeypress == 1) System.in.read();
		}
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
