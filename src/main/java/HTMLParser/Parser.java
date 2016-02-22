package HTMLParser;

import java.io.IOException;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
}