import java.io.Serializable;
import java.util.*;

public class Reference implements Serializable
{
	public String reftext = new String();
	public LinkedHashMap<String, String> authorsurnamemap = new LinkedHashMap<String, String>();
	public ArrayList<String []> contexts = new ArrayList<String []>();
	public String year;
	public String titletext;
	public String citedid;
	public ArrayList<Integer> sentencenumbers = new ArrayList<Integer>();
	
}
