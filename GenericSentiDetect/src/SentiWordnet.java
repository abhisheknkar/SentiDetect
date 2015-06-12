import java.io.*;
import java.util.*;


public class SentiWordnet {
	
	public static HashMap<String, double[]> readDataset_SentiWordnet() throws IOException
	{
		String Phrase;
		
		HashMap <String, double[]> SentiWordnetWords = new HashMap<String, double[]>();
		
		File fin = new File("./Datasets/SentiWordnet/SentiWordnet.txt");
		File fout = new File("./Outputs/SentiWordnet.tmp");

		String [] temp1;
		
		BufferedReader in = null;
		in = new BufferedReader(new FileReader(fin));

		String line = null;
		while((line = in.readLine()) != null)
		{
			temp1 = line.split("\t");
			if (temp1.length == 3)
			{
				double [] Scores = new double[2];
				Scores[0] = Double.parseDouble(temp1[1]);
				Scores[1] = Double.parseDouble(temp1[2]);
				Phrase = temp1[0].replace("_", " ");

				SentiWordnetWords.put(Phrase, Scores);
			}
		}
		
//		for (Map.Entry<String, double[]> entry : SentiWordnetWords.entrySet())
//		{
//			System.out.println(entry.getKey() + ": " + entry.getValue()[0] + "," + entry.getValue()[1]);
//			if ((Scores[0] + Scores[1]) > 0) break;
//		}
		return SentiWordnetWords;
		
	}

	public static double[] computeScore_SentiWordnet(HashMap<String, double[]> SentiWordnetWords, String InputSentence, int PolarityThreshold, int DisplayIndividualScores, int NoPrintFlag)
	{
		double[] Scores = new double[2];
		String[] temp = InputSentence.split(" ");
		
		if (DisplayIndividualScores != 0) System.out.println("Term  PosScore	NegScore");
		
		for (int i = 0; i < temp.length; ++i)
		{
			if (SentiWordnetWords.containsKey(temp[i]))
			{
				if (DisplayIndividualScores != 0) System.out.println(temp[i] + "\t" + SentiWordnetWords.get(temp[i])[0] +"\t" + SentiWordnetWords.get(temp[i])[1]);
				Scores[0] += SentiWordnetWords.get(temp[i])[0];
				Scores[1] += SentiWordnetWords.get(temp[i])[1];
			}
		}
		
		if (NoPrintFlag == 0)
		{
			System.out.println("Sentence Scores are: Pos: " + Scores[0] + "; Neg: " + Scores[1]);
			if ((Scores[0] - Scores[1]) > PolarityThreshold) System.out.println("The sentence is positive.");
			else if ((Scores[0] - Scores[1]) < -PolarityThreshold) System.out.println("The sentence is negative.");
			else System.out.println("The sentence is neutral.");
		}
		return Scores;
	}
	
	
}
