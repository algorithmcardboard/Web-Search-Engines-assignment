package HTMLParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parser {
	private Path file;

	public Parser(Path path) {
		this.file = path;
	}

	public ParsedDocument parse() throws IOException {
		Document doc = Jsoup.parse(this.file.toFile(), "UTF-8");
		
		String title = "", body;
		
		Elements titleElements = doc.getElementsByTag("title");
		if(titleElements.size() == 0){
			System.out.println("No title.  Using h1");
			titleElements = doc.getElementsByTag("h1");
		}
		
		if(titleElements.size() > 0){
			title = titleElements.first().text();
		}
		body = doc.getElementsByTag("body").text();
		return new ParsedDocument(title, body);
	}
	
	private int getScore(String link, String query){
		Random r = new Random();
		int score = r.nextInt(10- 1 + 1)+1;
		return score;
	}
	
	public Map<Integer, List<String>> getLinksAndScores(URL currentURL, String query) throws IOException{
		Document doc = Jsoup.parse(this.file.toFile(), "UTF-8");
		
		Map<Integer, List<String>> scoreToURLs = new TreeMap<Integer, List<String>>();
		
		Elements anchors = doc.select("a[href]");
		for (Element element : anchors) {
			String link = element.attr("href");
			
			URL url = new URL(currentURL, link);
			
			int score = getScore(link, query);
			
			if(!scoreToURLs.containsKey(score)){
				scoreToURLs.put(score, new ArrayList<String>());
			}
			scoreToURLs.get(score).add(url.toString());
		}
		
		return scoreToURLs;
	}
}