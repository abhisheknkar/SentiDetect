import java.io.*;
import java.util.*;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class DBLPOperations 
{
	public static HashMap<String, DBLPPaper> readDBLPDataset() throws IOException
	{
		HashMap<String, DBLPPaper> DBLPDataset = new HashMap<String, DBLPPaper>();
		File fin = null;
		File fout = null;
		BufferedReader in = null;
		
		int saveFlag = 0; //Don't set to 1! Takes a much longer time to save
		int count=0;
		String line;
		ArrayList<String> Metadata = new ArrayList<String>();
		
		try
		{
			fin = new File("./Datasets/DBLP/DBLPOnlyCitationOct19.txt");
			fout = new File("./Datasets/DBLP/DBLPDataset.tmp");
			in = new BufferedReader(new FileReader(fin));
	
			while((line = in.readLine()) != null)
			{
				if (line.length() > 0)
				{
					if(line.charAt(0) == '#') 
					{
						Metadata.add(line);
					}
				}
				else 
				{
					if(Metadata.size() > 0)
					{
						++count;
						if(count % 10000 == 0) System.out.println(count + " papers read.");
						DBLPPaper paper = new DBLPPaper(Metadata);
						DBLPDataset.put(paper.id, paper);
					}
					Metadata.clear();				
				}

			}
			if(saveFlag == 1)
			{
				FileOperations.writeObject(DBLPDataset, fout);
			}
		}
		finally
		{
			if (in!= null) in.close();
		}		
		return DBLPDataset;
	}

	public static void printDBLPDatasetfromFile() throws IOException
	{
		File fin = new File("./Datasets/DBLP/DBLPDataset.tmp");
		HashMap<String, DBLPPaper> DBLPDataset = FileOperations.readObject(fin);
		
		for (Map.Entry<String, DBLPPaper> paper : DBLPDataset.entrySet())
		{
			paper.getValue().printDBLPPaper();
		}
	}
}
