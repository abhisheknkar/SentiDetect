import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uci.ics.jung.graph.SparseGraph;


public class Test1 
{
	public static void main(String[] args) throws IOException
	{
		String content = "Are you a (hunter?";
		Pattern p = Pattern.compile("[\\(]+");
		Matcher m = p.matcher(content);
						
		if (m.find())
		{
			System.out.println("Matched : \"" + m.group() + "\" at location: " + m.start());
		}
	}
}