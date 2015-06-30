import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;


public class ContextOperations {
//Get the author names which occur in the paper
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		
		for (Map.Entry<String, Paper> entry : papers.entrySet())
		{
			String filepath = "C:/Abhishek_Narwekar/Papers,Datasets/Citation Polarity/Datasets/AAN/aan/papers_text/" + entry.getValue().id + ".txt";
			File f = new File(filepath);
			
			if (f.exists())
			{
				String content = new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
//				System.out.println("File: " + entry.getValue().id + "\n" + content);
				refStart = getRefSectionStart(content);
				getPotentialAuthors(content);
			}
		}
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms.");		
	}
	

	public static int getRefSectionStart(String content)
	{
//		String regex = "[Rr].?[Ee].?[Ff].?[Ee].?[Rr].?[Ee].?[Nn].?[Cc].?[Ee].?[Ss]";
		String regex = "(?i)r.?e.?f.?e.?r.?e.?n.?c.?e.?s";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		
		int matchedAt=0;
		
		while(matcher.find())
		{
//			System.out.println("matched \"" + matcher.group() + "\" at position " + matcher.start());
			matchedAt = matcher.start();
		}
		return matchedAt;
	}

	public static void getPotentialAuthors(String content) throws IOException
	{
		String name = "[A-Z][A-Za-z]+([ \\n][A-Za-z][-A-Za-z\n\\.]+)+";
		String whitespace = "[ \\n]";
		String regex = name + "(,?" + whitespace + name + ")*,?" + whitespace + "?(and)?" + whitespace + "?" + "(" + name + ")?\\." + whitespace + "[0-9]{4}";
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while(matcher.find())
		{
			System.out.println("matched \"" + matcher.group() + "\" at position " + matcher.start());
		}
	}
	
}

//(\w+). [0-9]{4}[A-Za-z]?.
//1212: \w+(\b\w+\b)+. [0-9]{4}[A-Za-z]?.
//([A-Z][a-z]+)( [A-Z][a-z]+)+
//Can get two authors before year: ([A-Z][a-z]+ [A-Z][a-z]+)+,? (and)? ([A-Z][a-z]+)( [A-Z][a-z]+)+\.[ \n]*[0-9]{4}
//([A-Z][a-z]+[ \n][A-Z][a-z]+)+,?[ \n](and)?[ \n]([A-Z][a-z]+[ \n][A-Z][a-z]+)+\.[ \n]*[0-9]{4}: Gets newlines too
//