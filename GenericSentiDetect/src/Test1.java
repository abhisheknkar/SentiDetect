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
		String source = "3";
		String dest = "7";
		SparseGraph<String, Integer> G = GeneralOperations.readGraph(new File("Datasets/graphinput.txt"), " ");
		int distance = GeneralOperations.getBFSDistance(G, source, dest, new ArrayList<String>(), Integer.MAX_VALUE);
		System.out.println("BFS Distance between " + source + " and "  + dest + " is " + distance);
		
	}

}