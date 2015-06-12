import java.io.*;

public class AmjadCitation implements Serializable
{
	String Citer;
	String Cited;
	int year;
	String[] Sentence = new String[4];
	int [] SentenceScore = new int[4];
	int [] OpinionFinderScore = new int[4];
	double [][] SentiWordnetScore = new double[4][2];
	int FunctionIndex;
	int PolarityIndex;
	int[] Features = new int[11];
	int StanfordNLPScore_explicit;
	/*
	 * 0: Ref count
	 * 1: Is Separate (1 means is separate)
	 * 2: Self Citation
	 * 3: Contains 1/3 PP
	 * 4. Contains negation
	 * 5. Contains speculation
	 * 6: Contains contrary expression
	 * 7: Headline of the sections which appear
	 * 
	 * ?: Closest subjective clue
	 * ?: Closest Verb / Adjective
	 * ?: Dependency Relations
	 */

	public AmjadCitation(String[] Input)
	{
		 Citer = Input[0];
		 Cited = Input[1];
		 year = Integer.parseInt(Input[2]);
		 for (int i = 0; i < 4; ++i)
		 {
			 Sentence[i] = Input[3+2*i];
			 SentenceScore[i] = Integer.parseInt(Input[4+2*i]);
			 FunctionIndex = Integer.parseInt(Input[11]);
			 PolarityIndex = Integer.parseInt(Input[12]);
		 }
		 
	}

}
