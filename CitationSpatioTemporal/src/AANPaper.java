import java.io.Serializable;


public class AANPaper implements Serializable
{
	public String ID;
	public String[] Authors;
	public String Title;
	public String Venue;
	public int year;
	
	public AANPaper(String[] Metadata)
	{
		ID = Metadata[0].substring(Metadata[0].indexOf("{")+1, Metadata[0].indexOf("}"));
		Metadata[1] = Metadata[1].replaceAll("; ",";");
		Authors = Metadata[1].substring(Metadata[1].indexOf("{")+1, Metadata[1].indexOf("}")).split(";");		
		Title = Metadata[2].substring(Metadata[2].indexOf("{")+1, Metadata[2].indexOf("}"));
		Venue = Metadata[3].substring(Metadata[3].indexOf("{")+1, Metadata[3].indexOf("}"));
		year = Integer.parseInt(Metadata[4].substring(Metadata[4].indexOf("{")+1, Metadata[4].indexOf("}")));	
	}

}
