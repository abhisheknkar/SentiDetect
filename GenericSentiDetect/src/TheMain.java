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
		
		doAmjadOperations();
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}

	public static void doAmjadOperations() throws IOException
	{
		ArrayList<AANPaper> AANPapers = new ArrayList<AANPaper>();
		ArrayList<AmjadCitation> Citations = new ArrayList<AmjadCitation>();
		ArrayList<OpinionFinderWord> Words = new ArrayList<OpinionFinderWord>();
		HashMap<String, double[]> SentiWordnetWords = new HashMap <String, double[]>();
		
		Citations = AmjadOperations.readDataSet();
//		Words = OpinionFinder.readDataset_OpinionFinder();
//		AANPapers = AANOperations.readAANMetadata();
//		AmjadFeatures.getRefCountandIsSeparate(Citations);	
//		AmjadFeatures.getSelfCitations(Citations, AANPapers);	
//		AmjadFeatures.computeSentenceScore_OpinionFinder(Citations, Words);	

		SentiWordnetWords = SentiWordnet.readDataset_SentiWordnet();
		AmjadFeatures.computeSentenceScore_SentiWordnet(Citations, SentiWordnetWords);
	}	
}