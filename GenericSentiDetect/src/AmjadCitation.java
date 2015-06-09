import java.io.*;

public class AmjadCitation implements Serializable
{
	String Citer;
	String Cited;
	int year;
	String[] Sentence = new String[4];
	int [] SentenceScore = new int[4];
	int FunctionIndex;
	int PolarityIndex;
	
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
