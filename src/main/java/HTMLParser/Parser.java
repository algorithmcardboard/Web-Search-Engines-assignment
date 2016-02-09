package HTMLParser;

import java.io.IOException;
import java.nio.file.Path;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Parser {
	private static Path file;

	public Parser(Path path) {
		this.file = path;
	}

	public ParsedDocument parse() throws IOException {
		Document doc = Jsoup.parse(this.file.toFile(), "UTF-8");
		String title = doc.getElementsByTag("title").text();
		String body = doc.getElementsByTag("body").text();
		return new ParsedDocument(title, body);
	}
}