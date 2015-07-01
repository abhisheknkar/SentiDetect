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
//		String oneCapital = "[a-z]*[A-Z][a-z]*";
		String nametype1 = "[A-Z][A-Za-z]*([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}";	//<FN> <LN>
		String nametype2 = "[A-Z][A-Za-z]*,([ \r\n]+[A-Za-z]\\.?[-A-Za-z\n]+){1,5}"; //<LN>, <FN>			
		String nametype3 = "([A-Z][\\. \r\n]+)+([A-Za-z][-A-Za-z\n]+){1,5}"; //I. <LN>			
		String nametype4 = "([A-Za-z][-A-Za-z\n]+)+[, \r\n]+([A-Z][\\. \r\n])+"; //<LN>, I.			

		String name = "(" + nametype1 + "|" + nametype2 + "|" + nametype3 + "|" + nametype4 + ")";
		String whitespace = "[ \r\n]*";
		
		String nameoccurrence1 = name;
		String nameoccurrencex = "(,?" + whitespace + name + ")*";
		String nameoccurrencelast = ",?" + whitespace + "(and)?" + whitespace + "(" + name + ")?[\\.]" + whitespace;
		String yearoccurrence = "[0-9]{4}[A-Za-z]*"; 		
		
		String regex = nameoccurrence1 + nameoccurrencex + nameoccurrencelast + yearoccurrence;
				
//		String content = "Jean Mulder, Kate Burridge, Stanley Simoes, and\nCaroline\nThomas. 2001.";
//		String content = "Simon Simon, Quintin Cutts, Sally Fincher, Patricia Haden, Anthony Robins, Ken Sutton, Bob Baker, Ilona\nBox, Michael de Raadt, John Hamer, Margaret Hamil-\nton, Raymond Lister, Marian Petre, Denise Tolhurst, and Jodi Tutty. 2006.";
		HashMap <String, Paper> papers = DatasetReader.readAANMetadata();
		int refStart = 0;
		
//		for (Map.Entry<String, Paper> entry : papers.entrySet())
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
					System.out.println("matched \"" + matcher.group(0) + "\"");// at position " + matcher.start());
//					matchedAt = matcher.start();
				}
			}		
		}
	
	}
}