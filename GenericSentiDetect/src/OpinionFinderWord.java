import java.io.*;

public class OpinionFinderWord implements Serializable
{
	 String Type;
	 String Len;
	 String Term;
	 String Pos;
	 String Stemmed;
	 String PriorPolarity;
	public OpinionFinderWord(String str1, String str2, String str3, String str4, String str5, String str6)
	{
		Type = str1;
		Len = str2;
		Term = str3;
		Pos = str4;
		Stemmed = str5;
		PriorPolarity = str6;
	}
/*	
	public  void SetWord(String str1, String str2, String str3, String str4, String str5, String str6)
	{
		Type = str1;
		Len = str2;
		Term = str3;
		Pos = str4;
		Stemmed = str5;
		PriorPolarity = str6;
	}
*/
}
