import java.io.*;

public class Citation implements Serializable
{
	String Citer;
	String Cited;
	int year;
	String[] Sentence = new String[4];
	int [] SentenceScore = new int[4];
//	int [] OpinionFinderScore = new int[4];
//	double [][] SentiWordnetScore = new double[4][2];
	int FunctionIndex;
	int PolarityIndex;
	int[] Features = new int[11];
	int[][] ContextFeatures = new int[4][9];
	int StanfordNLPScore_explicit;
	/*
	 * Polarity Features:
	 * 0: Ref count
	 * 1: Is Separate (1 means is separate)
	 * 2: Self Citation
	 * 3: Contains 1/3 PP
	 * 4. Contains negation
	 * 5. Contains speculation
	 * 6: Contains contrary expression
	 * 7: Headline of the sections which appear
	 * 
	 * 8: Closest subjective clue
	 * 9: Closest Verb / Adjective / Adverb
	 * 10: Dependency Relations
	 * 
	 * Context Features:
	 * 0: Demonstrative Determiners
	 * 1: Conjunctive Adverbs
	 * 2: Position
	 * 3: Contains closest noun phrase
	 * 4: 1st bigram
	 * 5: 1st trigram
	 * 6: References other than the target
	 * 7: Mention of target reference (explicit or anaphoric)
	 * 8: Multiple references
	 */

	public Citation(String[] Input)
	{
		 Citer = Input[0];
		 Cited = Input[1];
		 year = Integer.parseInt(Input[2]);
		 for (int i = 0; i < 4; ++i)
		 {
			 Sentence[i] = Input[3+2*i];
			 SentenceScore[i] = Integer.parseInt(Input[4+2*i]);
			 FunctionIndex = Integer.parseInt(Input[11]);
			 PolarityIndex = Integer.parseInt(Input[12]); if (PolarityIndex < 1 || PolarityIndex > 3) PolarityIndex = 1;
		 }		 
	}
}