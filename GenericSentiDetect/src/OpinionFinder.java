import java.io.*;
import java.util.*;

/**
 * @author abhishek
 *
 */
public class OpinionFinder 
{
	public static void main(String[] args) throws IOException
	{
		Scanner in = new Scanner(System.in);
		System.out.println("Enter string:");
		String InputSentence = in.nextLine();		
		
		int PolarityThreshold = 1;
		int DisplayIndividualScores = 1;
		int NoPrintFlag = 0;
		
		long startTime = System.currentTimeMillis();
		int saveFlag = 1;
		int sentenceScore;
		File fin = new File("./Datasets/OpinionFinder/senti.txt");
		File fout = new File("./Outputs/OpinionFinderWordList.tmp");

		ArrayList<OpinionFinderWord> Words = new ArrayList<OpinionFinderWord>();
		Words = readDataset_OpinionFinder(fin,fout, saveFlag);	//Create the list of words
		//Given sentence, parse each word, see if it exists in the list. If it does, add its score to aggregate.

		sentenceScore = computeScore_OpinionFinder(Words, InputSentence, PolarityThreshold, DisplayIndividualScores, NoPrintFlag);
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Total time: " + totalTime + "ms.");		
	}
	
	public static  ArrayList<OpinionFinderWord> readDataset_OpinionFinder(File fin, File fout, int saveFlag) throws IOException
	{		
		BufferedReader in = null;
		in = new BufferedReader(new FileReader(fin));
		
		String line = null;
		String[] temp1;
		String[] temp2;
		String[] temp3 = new String[6];
		
		ArrayList<OpinionFinderWord> Words = new ArrayList<OpinionFinderWord>();
		
		while((line = in.readLine()) != null)
		{
			temp1 = line.split(" ");
			
			for(int i=0; i<6; ++i)
			{
				temp2 = temp1[i].split("=");
				temp3[i] = temp2[1];
			}
			Words.add(new OpinionFinderWord(temp3[0],temp3[1],temp3[2],temp3[3],temp3[4],temp3[5]));			
		}
		if (saveFlag != 0) 
		{
			FileOutputStream foutStream = new FileOutputStream(fout);
			ObjectOutputStream oos = new ObjectOutputStream(foutStream);
			oos.writeObject(Words);		
		}
		return Words;
	}

	public static  ArrayList<OpinionFinderWord> readDataset_OpinionFinder() throws IOException
	{
		File fin = new File("./Datasets/OpinionFinder/senti.txt");
		File fout = new File("./Outputs/OpinionFinderWordList.tmp");
		int saveFlag = 1;
		ArrayList<OpinionFinderWord> Words = readDataset_OpinionFinder(fin,fout,saveFlag);
		return Words;
		
	}

	public static int computeScore_OpinionFinder(ArrayList<OpinionFinderWord> Words, String InputSentence, int PolarityThreshold, int DisplayIndividualScores, int NoPrintFlag)
	{
		int Score = 0;
		int Add2Score = 0;
		int Sign = 0;
		String[] temp = InputSentence.split(" ");
		
		if (DisplayIndividualScores != 0) System.out.println("Term:  Score:");
		
		for (int i = 0; i < temp.length; ++i)
		{
			for (ListIterator<OpinionFinderWord> iter = Words.listIterator(); iter.hasNext(); ) 
			{
			    OpinionFinderWord X = iter.next();

				Add2Score = 0;
				Sign = 0;
				if (temp[i].equalsIgnoreCase(X.Term))
				{
					if (X.Type.equalsIgnoreCase("strongsubj")) Add2Score += 2;
					else if (X.Type.equalsIgnoreCase("weaksubj")) Add2Score += 1;

					if (X.PriorPolarity.equalsIgnoreCase("positive")) Sign = 1;
					else if (X.PriorPolarity.equalsIgnoreCase("negative")) Sign = -1;
					else Sign = 0;
					
					Add2Score *= Sign;
					Score += Add2Score;
						
					if (DisplayIndividualScores != 0) System.out.println(X.Term + "  " + Add2Score);
						
					break;
				}
			}
		}
		
		if (NoPrintFlag == 0)
		{
			System.out.println("Sentence Score is " + Score);
			if (Score > PolarityThreshold) System.out.println("The sentence is positive.");
			else if (Score < -PolarityThreshold) System.out.println("The sentence is negative.");
			else System.out.println("The sentence is neutral.");
		}
		return Score;
	}
}