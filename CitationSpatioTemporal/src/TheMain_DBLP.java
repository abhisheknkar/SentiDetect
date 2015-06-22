import java.io.*;
import java.util.HashMap;


public class TheMain_DBLP {

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		
		HashMap<String, DBLPPaper> DBLPDataset = DBLPOperations.readDBLPDataset();	
//		DBLPOperations.printDBLPDatasetfromFile();

//		File fin = new File("./Datasets/DBLP/DBLPDataset.tmp");
//		HashMap<String, DBLPPaper> DBLPDataset = FileOperations.readObject(fin);

		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		

	}

}
