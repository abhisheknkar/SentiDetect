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
public class TheMain 
{
	private static final String[] PronounList = {"i", "me", "my", "mine", "we", "us", "we", "our", "ours",
		"he", "she", "it", "they", "him", "her", "them","his", "her", "its", "their", "theirs"}; 
	
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		
		doAmjadOperations();
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}

	public static void doAmjadOperations() throws IOException
	{
		boolean readfromfile = false;
		
		File fin = new File("Outputs/Amjad/Citations.tmp");
		ArrayList<Citation> citations = new ArrayList<Citation>();
		
		HashMap<String, Paper> papers = DatasetReader.readAANMetadata();

		if(readfromfile)
		{
			if(fin.exists()) citations = FileOperations.readObject(fin);
			else citations = DatasetReader.readAmjadCitations();
		}

		else citations = DatasetReader.readAmjadCitations();
		//Extract features here
		citations = AmjadFeatures.cleanCitations(citations);
		citations = AmjadFeatures.getFeatures0and1(citations);	
		citations = AmjadFeatures.getFeature2(citations, papers);	
		citations = AmjadFeatures.getFeature3(citations);
		citations = AmjadFeatures.getFeature4(citations);
		citations = AmjadFeatures.getFeature6(citations);
		citations = AmjadFeatures.getFeature5(citations);
		citations = AmjadFeatures.getFeature7(citations);
		citations = AmjadFeatures.getFeature8(citations);
		AmjadFeatures.writeToARFF(citations);

		FileOperations.writeObject(citations, new File("Outputs/Amjad/Citations.tmp"));
	}	
}

/*
		HashMap<String, Paper> papers = DatasetReader.readAANMetadata();
		ArrayList<OpinionFinderWord> Words = new ArrayList<OpinionFinderWord>();
		HashMap<String, double[]> SentiWordnetWords = new HashMap <String, double[]>();

		Words = OpinionFinder.readDataset_OpinionFinder();

		AANPapers = AANOperations.readAANMetadata();
		citations = AmjadFeatures.cleanCitations(citations);
		citations = AmjadFeatures.getFeatures0and1(citations);	
		citations = AmjadFeatures.getFeature2(citations, papers);	
		citations = AmjadFeatures.getFeature3(citations);
		citations = AmjadFeatures.getFeature4(citations);
		citations = AmjadFeatures.getFeature6(citations);
		citations = AmjadFeatures.getFeature5(citations);
		citations = AmjadFeatures.getFeature7(citations);
		citations = AmjadFeatures.getFeature8(citations);
		citations = AmjadFeatures.getFeature9and10(citations);

		AmjadFeatures.getNegationCues();
		AmjadFeatures.getSpeculationCues(new File("Datasets/bioscope/full_papers.xml"));
		AmjadFeatures.getSpeculationCues(new File("Datasets/bioscope/abstracts.xml"));

		AmjadFeatures.writeToARFF(citations);
		AmjadFeatures.computeSentenceScore_OpinionFinder(Citations, Words);	
		AmjadFeatures.plotPolarityProfiles(citations, papers);

//		Classify
//		AmjadFeatures.writeToARFF(citations);


SentiWordnetWords = SentiWordnet.readDataset_SentiWordnet();
AmjadFeatures.computeSentenceScore_SentiWordnet(Citations, SentiWordnetWords);
AmjadFeatures.computeSentenceScore_VaderSentiment(Citations);
*/
