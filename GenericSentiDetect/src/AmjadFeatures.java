import java.io.*;
import java.util.ArrayList;

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
