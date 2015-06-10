import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

//Class to perform operations on the Amjad dataset
public class TheMain {

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		
		ArrayList<AANPaper> AANPapers = new ArrayList<AANPaper>();
		AANPapers = AANOperations.readAANMetadata();
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
}