import java.io.Serializable;
import java.util.*;

public class Reference implements Serializable
{
	public String reftext = new String();
	public TreeMap<String, String> authorsurnamemap = new TreeMap<String, String>();
	public ArrayList<String []> contexts = new ArrayList<String []>();
	public String year;
	public String titletext;
	public String citedid;
	
}
