import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileOperations {
	public  void readFile1(File fin, File fout) throws IOException	
	{
		FileInputStream in = null;
		FileOutputStream out = null;		
		try
		{
			in = new FileInputStream(fin);
			out = new FileOutputStream(fout);
			int c;
			while((c=in.read())!= -1)
			{
				out.write(c);
			}
		}
		finally
		{
			if(in!=null) in.close();
			if (out != null) out.close();
		}	
	}

	public  void readFile2(File fin, File fout) throws IOException	
	{
		BufferedReader in = null;
		BufferedWriter out = null;

		in = new BufferedReader(new FileReader(fin));
		out = new BufferedWriter(new FileWriter(fout));
		
		String line = null;
		
		while((line = in.readLine()) != null)
		{
			out.write(line);
			out.newLine();
		}
	}	
	

}
