import java.io.*;
import java.util.*;

//All members are static functions
public class AmjadFeatures 
{
	public static void getRefCountandIsSeparate(ArrayList<AmjadCitation> Citations)
	{
		//Handles the first two features
		int count, delta;
		for (AmjadCitation Citation : Citations)
		{
			count = 0;
			Citation.Features[1] = 1;
			for(int i = 0; i < 4; ++i)
			{
				if(Citation.SentenceScore[i] == 1)
				{
					delta = findNoOfOccurrences(Citation.Sentence[i], "REF>") / 2;
					if (delta > 1)
					{
						Citation.Features[1] = 0;
					}
					count += delta;
				}
			}
//			System.out.println(count);
			Citation.Features[0] = count;
		}
	}

	public static void getSelfCitations(ArrayList<AmjadCitation> Citations, ArrayList<AANPaper> Papers)
	{
		String[] CiterAuthors = null;
		String[] CitedAuthors = null;
		ArrayList<String> CommonAuthorsList = new ArrayList<String>();
		
		int CiterFound=0, CitedFound=0;
		for (AmjadCitation Citation : Citations)
		{
			for (AANPaper Paper : Papers)
			{
				if (CitedFound == 0)
					if (Paper.ID.equals(Citation.Cited))
					{
						CitedFound = 1;
						CitedAuthors = Paper.Authors;
					}
				if (CiterFound == 0)
					if (Paper.ID.equals(Citation.Citer))
					{
						CiterFound = 1;
						CiterAuthors = Paper.Authors;
					}
			}
			//Get intersection
			for (int i = 0; i < CitedAuthors.length; ++i)
			{
				//Create ArrayList of Cited Authors
				if (Arrays.asList(CiterAuthors).contains(CitedAuthors[i]))
				{
					CommonAuthorsList.add(CitedAuthors[i]);
				}
			}	
			if (CommonAuthorsList.size() > 0)
			{
				Citation.Features[2] = 1;
/*				for (String CommonAuthor : CommonAuthorsList)
				{						
					System.out.println(CommonAuthor);
				}
*/
				CommonAuthorsList.clear();
			}
		}
		
	}
	
	public static int findNoOfOccurrences(String str, String findStr)
	{
		int lastIndex = 0;
		int count = 0;

		while(lastIndex != -1)
		{
			lastIndex = str.indexOf(findStr,lastIndex);
			if( lastIndex != -1)
			{
				count ++;
				lastIndex+=findStr.length();
			}
		}
//		System.out.println(count);	//		for (AmjadCitation Citation : Citations)
		return count;
	}

}

/*		Citations2 = AmjadFeatures.readObj(fout);
for (AmjadCitation X : Citations2)
{
	System.out.println(X.Sentence[0]);
}
*/		
