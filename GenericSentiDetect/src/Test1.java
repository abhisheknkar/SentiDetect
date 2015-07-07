import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test1 {

	public static void main(String[] args) throws IOException
	{
		String a = "I am a look";
		String b = "I am a crook";
		
		int dist = GeneralOperations.getLevenshteinDistance(a, b);
		System.out.println("Levenshtein distance between " + a + " and " + b + " is: " + dist);
	}
}