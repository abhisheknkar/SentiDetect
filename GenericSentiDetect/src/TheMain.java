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
		
//		doAmjadOperations();
		getTestFeatures();
		
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}

	public static void getTestFeatures() throws IOException
	{
		/*
		 * This function reads the file containing the contexts into an arraylist of citations. 
		 */
		int start = 501;
		int end = 600;

		File fout_features_pol_test = new File("Outputs/Amjad/test_features/f" + start + "_" + end + ".arff");
		
		HashMap<String, Paper> papers = DatasetReader.readAANMetadata();
		ArrayList<Citation> citations = DatasetReader.readTestCitations(start, end);
		citations = AmjadFeatures.cleanCitations(citations);
		citations = AmjadFeatures.getFeatures0and1(citations);	
		citations = AmjadFeatures.getFeature2(citations, papers);	
		citations = AmjadFeatures.getFeature3(citations);
		citations = AmjadFeatures.getFeature4(citations);
		citations = AmjadFeatures.getFeature6(citations);
		citations = AmjadFeatures.getFeature5(citations);
		citations = AmjadFeatures.getFeature7(citations);
		citations = AmjadFeatures.getFeature8(citations);
		AmjadFeatures.writeToARFF_Polarity(citations, fout_features_pol_test);

	}
	
	public static void doAmjadOperations() throws IOException
	{
		boolean readfromfile = false;
		
		File fin = new File("Outputs/Amjad/Citations.tmp");
		File fout_features_pol = new File("Outputs/Amjad/features_polarity.arff");
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
		AmjadFeatures.writeToARFF_Polarity(citations, fout_features_pol);

		
		
		AmjadFeatures.writeToARFF_Context(citations);
		
//		FileOperations.writeObject(citations, new File("Outputs/Amjad/Citations.tmp"));

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

		citations = AmjadFeatures.getContextFeature0and2(citations);	
		citations = AmjadFeatures.getContextFeature4and5(citations);	
		citations = AmjadFeatures.getContextFeature6and8(citations);	

		AmjadFeatures.getNegationCues();
		AmjadFeatures.getSpeculationCues(new File("Datasets/bioscope/full_papers.xml"));
		AmjadFeatures.getSpeculationCues(new File("Datasets/bioscope/abstracts.xml"));

		AmjadFeatures.writeToARFF_Polarity(citations);
		AmjadFeatures.computeSentenceScore_OpinionFinder(Citations, Words);	
		AmjadFeatures.plotPolarityProfiles(citations, papers);

//		Classify
//		AmjadFeatures.writeToARFF(citations);


SentiWordnetWords = SentiWordnet.readDataset_SentiWordnet();
AmjadFeatures.computeSentenceScore_SentiWordnet(Citations, SentiWordnetWords);
AmjadFeatures.computeSentenceScore_VaderSentiment(Citations);
*/
