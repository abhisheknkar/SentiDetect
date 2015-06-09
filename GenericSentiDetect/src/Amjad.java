import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

//Class to perform operations on the Amjad dataset
public class Amjad {

	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
//		Scanner in = new Scanner(System.in);
//		System.out.println("Enter string:");
//		String InputSentence = in.nextLine();		
		
		File fin = new File("./Datasets/Amjad/annotated_sentences.txt");
		File fout = new File("./Outputs/AmjadCitationList.tmp");

		int saveFlag = 1;

		ArrayList<AmjadCitation> Citations = new ArrayList<AmjadCitation>();
		Citations = readDataSet(fin,fout, saveFlag);	//Create the list of words
		getPolarityDistribution(Citations);
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
	public static ArrayList<AmjadCitation> readDataSet(File fin, File fout, int saveFlag) throws IOException
	{		
		BufferedReader in = null;
		in = new BufferedReader(new FileReader(fin));
		
		String line = null;
		String[] temp1 = null;
		
		ArrayList<AmjadCitation> Citations = new ArrayList<AmjadCitation>();
		
		while((line = in.readLine()) != null)
		{
			temp1 = line.split("\t");			
			Citations.add(new AmjadCitation(temp1));			
		}
		
		if (saveFlag != 0) 
		{
			FileOutputStream foutStream = new FileOutputStream(fout);
			ObjectOutputStream oos = new ObjectOutputStream(foutStream);
			oos.writeObject(Citations);		
		}
		return Citations;
	}

	public static void getPolarityDistribution(ArrayList<AmjadCitation> Citations) throws IOException
	{
		int[] PolarityStats = new int[4];
		
		for (AmjadCitation iter : Citations ) 
		{	
			if (iter.PolarityIndex > 0 && iter.PolarityIndex <= 3) 
				++PolarityStats[iter.PolarityIndex-1];
			else ++PolarityStats[3];
		}
		
		int TotalSentences = PolarityStats[0] + PolarityStats[1] + PolarityStats[2] + PolarityStats[3];
		
		System.out.println("Neutral Sentences: " + PolarityStats[0] + " (" + 100*PolarityStats[0]/TotalSentences + "%)"); 
		System.out.println("Positive Sentences: " + PolarityStats[1] + " (" + 100*PolarityStats[1]/TotalSentences + "%)"); 
		System.out.println("Negative Sentences: " + PolarityStats[2] + " (" + 100*PolarityStats[2]/TotalSentences + "%)"); 
		System.out.println("Invalid Sentences: " + PolarityStats[3] + " (" + 100*PolarityStats[3]/TotalSentences + "%)"); 
	}
	
}