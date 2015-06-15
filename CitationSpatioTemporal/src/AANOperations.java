import java.io.*;
import java.util.*;
import edu.uci.ics.jung.graph.*;


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
			if (in!= null)
			{
				in.close();
			}
			if (oos != null)
			{
				oos.close();
			}
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
	
	public static HashMap<Integer, SparseGraph<Integer, Integer>> formCoAuthorshipNetwork()
	{
		//Returns the yearwise edgelist
		
		HashMap<Integer, SparseGraph<Integer, Integer>> CoAuthorshipNetwork = null;
		

		
		
		return CoAuthorshipNetwork;
	}
	
}
