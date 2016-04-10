package pagerank;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Page {
	private double base;
	private double score;
	private int wordCount;
	private Path path;
	private String text;
	private Document doc;
	
	public Page(Path path) throws IOException{
		this.path = path;
		
		this.doc = Jsoup.parse(path.toFile(), "UTF-8", this.path.toString());
		this.text = this.doc.select("body").text().trim().replaceAll(" +", " ");
		this.wordCount = text.split(" ").length;
		this.base = Math.log(this.wordCount)/Math.log(2);
		this.score = this.base;
	}

	/**
	 * @return the wordCount
	 */
	public int getWordCount() {
		return wordCount;
	}

	/**
	 * @return the base
	 */
	public double getBase() {
		return base;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @return the path
	 */
	public Path getPath() {
		return path;
	}

	public void calculateScore(double sum) {
		this.score = this.score/sum; 
	}

	public boolean hasOutboundLinks() {
		return this.doc.select("a").size() > 0;
	}

	public String getPageName() {
		return this.path.getFileName().toString();
	}

	public Map<Path, Integer> getOutboundLinks() {
		Map<Path, Integer> outboundLinks = new HashMap<>();
		for (Element element : this.doc.select("a[href]")) {
			Path path = Paths.get(element.attr("href"));
			System.out.println("Path got is "+path + " END");
			if(!outboundLinks.containsKey(path)){
				outboundLinks.put(path, 0);
			}
			int score = outboundLinks.get(path) + 1; //add one as presence increases the score by one.
			
			if(anchorInImportantTags(element)){
				score = score + 1;
			}
			
			outboundLinks.put(path, score);
		}
		return outboundLinks;
	}

	private boolean anchorInImportantTags(Element element) {
		for (Element elem : element.parents()) {
			String tagName = elem.tagName().toLowerCase();
			if(tagName.equals("h1") || tagName.equals("h2") || tagName.equals("h3") || tagName.equals("h4") || tagName.equals("em") || tagName.equals("b")){
				return true;
			}
		}
		return false;
	}
}
