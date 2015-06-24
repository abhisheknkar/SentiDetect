import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	
	
	public static ArrayList<Citation> readAmjadCitations() throws IOException
	{		
		File fin = new File("./Datasets/Amjad/annotated_sentences.txt");
		File fout = new File("./Outputs/AmjadCitationList.tmp");
		int saveFlag = 0;
		
		BufferedReader in = null;
		in = new BufferedReader(new FileReader(fin));
		
		String line = null;
		String[] temp1 = null;
		
		ArrayList<Citation> citations = new ArrayList<Citation>();
		
		while((line = in.readLine()) != null)
		{
			temp1 = line.split("\t");			
			citations.add(new Citation(temp1));			
		}
		
		if (saveFlag != 0) 
		{
			FileOperations.writeObject(citations, fout);
		}
		return citations;
	}

}
