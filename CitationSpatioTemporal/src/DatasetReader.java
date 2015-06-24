import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class DatasetReader 
{
	public static HashMap <String, Paper> readAANMetadata() throws IOException
	{
		File fin = null;
		File fout = null;
		BufferedReader in = null;
		
		int saveFlag = 0;
		String line;
		int count = 0;
		String[] Metadata = new String[5];
		HashMap <String, Paper> papers = new HashMap <String, Paper>();
		
		try
		{
			fin = new File("./Datasets/AAN/acl-metadata.txt");
			fout = new File("./Outputs/AAN/AANMetadata.tmp");
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
					Paper paper = new Paper();
					paper.id = Metadata[0].substring(Metadata[0].indexOf("{")+1, Metadata[0].indexOf("}"));
//					Metadata[1] = Metadata[1].replaceAll("; ",";");
					paper.authors = Metadata[1].substring(Metadata[1].indexOf("{")+1, Metadata[1].indexOf("}")).split("; ");		
					paper.title = Metadata[2].substring(Metadata[2].indexOf("{")+1, Metadata[2].indexOf("}"));
					paper.venue = Metadata[3].substring(Metadata[3].indexOf("{")+1, Metadata[3].indexOf("}"));

					if (Metadata[4].indexOf("}") < 0) paper.year = Integer.parseInt(Metadata[4].substring(Metadata[4].indexOf("{")+1, Metadata[4].length()));
					else paper.year = Integer.parseInt(Metadata[4].substring(Metadata[4].indexOf("{")+1, Metadata[4].indexOf("}")));	

					papers.put(paper.id, paper);
					count = 0;
				}
			}			
		}

		if (saveFlag != 0) 
		{
			FileOperations.writeObject(papers, fout);
		}

		}
		finally
		{
			if (in!= null) in.close();
		}
		return papers;
	}
	
	public static SparseGraph<String, Integer> readAANCitations() throws IOException
	{
		SparseGraph<String, Integer> citationgraph = new SparseGraph<String, Integer>();
		File fin = null;
		BufferedReader in = null;
		String line;
		String[] linesplit;
		int count = 0;
		
		try
		{
			fin = new File("./Datasets/AAN/acl.txt");
			in = new BufferedReader(new FileReader(fin));

			while((line = in.readLine()) != null)
			{
				linesplit = line.split(" ==> ");
				
//				citationgraph.addVertex(linesplit[0]);
//				citationgraph.addVertex(linesplit[1]);

				citationgraph.addEdge(count++, linesplit[0], linesplit[1], EdgeType.DIRECTED);
			}
		}
		
		finally
		{
			if (in!= null) in.close();
		}

		return citationgraph;
	}
	
	public static HashMap<String, Paper> readDBLPMetadata() throws IOException
	{
		HashMap<String, Paper> papers = new HashMap<String, Paper>();
		File fin = null;
		File fout = null;
		BufferedReader in = null;
		
		int saveFlag = 0; //Don't set to 1! Takes a much longer time to save
		int count=0;
		String line0;
		ArrayList<String> Metadata = new ArrayList<String>();
		
		try
		{
			fin = new File("./Datasets/DBLP/DBLPOnlyCitationOct19.txt");
			fout = new File("./Datasets/DBLP/DBLPDataset.tmp");
			in = new BufferedReader(new FileReader(fin));
	
			while((line0 = in.readLine()) != null)
			{
				if (line0.length() > 0)
				{
					if(line0.charAt(0) == '#') 
					{
						Metadata.add(line0);
					}
				}
				else 
				{
					if(Metadata.size() > 0)
					{
						++count;
//						if(count % 10000 == 0) System.out.println(count + " papers read.");

						Paper paper = new Paper();
						char second;
						String[] authorlist;
						
						for (String line : Metadata)
						{
							second = line.charAt(1);
							switch(second)
							{
								case '*': paper.title = line.substring(2, line.length());break;
								case '@': paper.authors = line.substring(2, line.length()).split(",");break;
								case 't': paper.year = Integer.valueOf(line.substring(2, line.length()));break;
								case 'c': paper.venue = line.substring(2, line.length());break;
								case 'i': paper.id = line.substring(6, line.length());break;
								case '!': paper.abstractpaper= line.substring(2, line.length());break;
								case '1': paper.continent = line.substring(2, line.length()).split(",");break;
								case '%': paper.referenceids.add(line.substring(2, line.length())); break;
								default: break;
							}
						}

						papers.put(paper.id, paper);
					}
					Metadata.clear();				
				}
			}
			System.out.println("DBLP Dataset read!");
			
			if(saveFlag == 1)
			{
				FileOperations.writeObject(papers, fout);
			}
		}
		finally
		{
			if (in!= null) in.close();
		}		
		return papers;
	}

}
