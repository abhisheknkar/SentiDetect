import java.util.ArrayList;


public class Paper 
{
	public String id;
	public String[] authors;
	public String title;
	public String[] continent;
	public String venue;
	public int year;
	public String[] relatedfields;
	public ArrayList<String> referenceids = new ArrayList<String>();
	public String abstractpaper;
	
	public void printPaper()
	{
		System.out.println(id);
		System.out.println(title + "\n");
	}

}
