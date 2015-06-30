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
		// TODO Auto-generated method stub

		String name = "[A-Z][A-Za-z]+([ \\n][A-Za-z][-A-Za-z\n\\.]+)+";
		String whitespace = "[ \\n]";
		String regex = name + "(,?" + whitespace + name + ")*,?" + whitespace + "?(and)?" + whitespace + "?" + "(" + name + ")?\\." + whitespace + "[0-9]{4}";
				
//		String content = "Jean Mulder, Kate Burridge, Stanley Simoes, and\nCaroline\nThomas. 2001.";
//		String content = "Simon Simon, Quintin Cutts, Sally Fincher, Patricia Haden, Anthony Robins, Ken Sutton, Bob Baker, Ilona\nBox, Michael de Raadt, John Hamer, Margaret Hamil-\nton, Raymond Lister, Marian Petre, Denise Tolhurst, and Jodi Tutty. 2006.";
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		
		for (Map.Entry<String, Paper> entry : papers.entrySet())
		{
//			String filepath = "C:/Abhishek_Narwekar/Papers,Datasets/Citation Polarity/Datasets/AAN/aan/papers_text/" + entry.getValue().id + ".txt";
			String filepath = "Datasets/AAN/testpaper.txt";
			File f = new File(filepath);
			
			if (f.exists())
			{
				String content = new String(Files.readAllBytes(Paths.get(filepath)), StandardCharsets.UTF_8);
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(content);
				while(matcher.find())
				{
					System.out.println("matched \"" + matcher.group() + "\" at position " + matcher.start());
//					matchedAt = matcher.start();
				}
			}		
		}
//		int matchedAt=0;
		
	}

}
