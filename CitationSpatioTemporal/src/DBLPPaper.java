import java.io.Serializable;
import java.util.ArrayList;


public class DBLPPaper implements Serializable
{
	public String id;
	public String[] authors;
	public String title;
	public String[] continent;
	public String venue;
	public int year;
//	public String[] relatedfields;
	public ArrayList<String> referenceids = new ArrayList<String>();
	public String abstractpaper;
		
	public DBLPPaper(ArrayList<String> Metadata)
	{
		char second;
		String[] authorlist;
		
		for (String line : Metadata)
		{
			second = line.charAt(1);
			switch(second)
			{
				case '*': title = line.substring(2, line.length());break;
				case '@': authors = line.substring(2, line.length()).split(",");break;
				case 't': year = Integer.valueOf(line.substring(2, line.length()));break;
				case 'c': venue = line.substring(2, line.length());break;
				case 'i': id = line.substring(6, line.length());break;
				case '!': abstractpaper= line.substring(2, line.length());break;
				case '1': continent = line.substring(2, line.length()).split(",");break;
				case '%': referenceids.add(line.substring(2, line.length())); break;
				default: break;
			}
		}
	}
	
	public void printDBLPPaper()
	{
		System.out.println(this.id);
		System.out.println(this.title + "\n");
	}

}
