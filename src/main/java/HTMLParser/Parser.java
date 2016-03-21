package HTMLParser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
		if (titleElements.size() == 0) {
			titleElements = doc.getElementsByTag("h1");
		}

		if (titleElements.size() > 0) {
			title = titleElements.first().text();
		}
		body = doc.getElementsByTag("body").text();
		return new ParsedDocument(title, body);
	}

	private int getScore(Element anchor, Document doc, List<String> query) {
		if (query == null || query.size() == 0) {
			return 0;
		}

		int anchorTextScore = getAnchorScore(anchor, query);
		if (anchorTextScore > 0) {
			return anchorTextScore;
		}

		int hrefAttributeScore = getAttributeScore(anchor, query);
		if (hrefAttributeScore > 0) {
			return hrefAttributeScore;
		}

		int numOccurences = getNumOccurences(doc, query);
		int numNearbyOccurence = getNearbyOccurence(doc, anchor, query, 5);
		
		int score = 4 * numNearbyOccurence + Math.abs(numNearbyOccurence - numOccurences);
		
//		System.out.println(anchor.text() + " NumOccurence " + numOccurences + " numNearby " + numNearbyOccurence + " " + score);

		return score;
	}

	private int getNearbyOccurence(Document doc, Element anchor, List<String> query, int distance) {
		int nearbyOccurence = 0;

		List<String> neighbours = getNeighboursToLeft(anchor, distance);
		neighbours.addAll(getNeighboursToRight(anchor, distance));

		for (String q : query) {
			if (neighbours.contains(q)) {
				nearbyOccurence++;
			}
		}

		return nearbyOccurence;
	}

	private List<String> getNeighboursToRight(Element anchor, int distance) {
		List<String> neighboursToRight = new ArrayList<>();

		Node nextSibling = anchor.nextSibling();
		while (neighboursToRight.size() < distance) {
			String data = getData(nextSibling);
			if (data == null) {
				break;
			}
			String[] split = data.split(" ");
			for (int i = 0; i < split.length && neighboursToRight.size() < distance;) {
				String word = split[i];
				if (word.matches("^\\W+$") || word.matches("\\z")) {
					i++;
					continue;
				}
				word = word.replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
				neighboursToRight.add(word.toLowerCase());
				i++;
			}

			nextSibling = nextSibling.nextSibling();
		}

		return neighboursToRight;
	}

	private String getData(Node node) {
		if (node == null) {
			return null;
		}
		if (node instanceof TextNode) {
			return ((TextNode) node).text();
		}
		return ((Element) node).text();
	}

	private List<String> getNeighboursToLeft(Element anchor, int distance) {
		List<String> neighboursToLeft = new ArrayList<>();

		Node previousSibling = anchor.previousSibling();
		while (neighboursToLeft.size() < distance) {
			String data = getData(previousSibling);
			if (data == null) {
				break;
			}
			String[] split = data.split(" ");
			for (int i = split.length - 1; i >= 0 && neighboursToLeft.size() < distance;) {
				String word = split[i];
				if (word.matches("^\\W+$") || word.matches("\\z")) {
					i--;
					continue;
				}
				word = word.replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
				neighboursToLeft.add(word.toLowerCase());
				i--;
			}

			previousSibling = previousSibling.previousSibling();
		}

		return neighboursToLeft;
	}

	private int getNumOccurences(Document doc, List<String> query) {
		int numOccurences = 0;

		String rawText = doc.text().toLowerCase();
		List<String> rawList = new ArrayList<>();
		for (String str : rawText.split(" ")) {
			str = str.replaceAll("[^\\p{Alpha}\\p{Digit}]+", "");
			rawList.add(str.toLowerCase());
		}
		for (String q : query) {
			if (rawList.contains(q)) {
				numOccurences++;
			}
		}
		return numOccurences;
	}

	private int getAttributeScore(Element anchor, List<String> query) {
		String attr = anchor.attr("href").toLowerCase();
		boolean present = false;
		for (String q : query) {
			if (attr.contains(q)) {
				present = true;
				break;
			}
		}
		return present ? 40 : 0;
	}

	private int getAnchorScore(Element anchor, List<String> query) {
		int K = 0;
		for (String q : query) {
			if (anchor.text().toLowerCase().contains(q)) {
				K++;
			}
		}
		return K * 50;
	}

	public Map<Integer, List<String>> getLinksAndScores(URL currentURL, List<String> query) throws IOException {
		Document doc = Jsoup.parse(this.file.toFile(), "UTF-8", currentURL.toString());

		Map<Integer, List<String>> scoreToURLs = new TreeMap<Integer, List<String>>(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		});

		Elements anchors = doc.select("a[href]");
		for (Element element : anchors) {
			String link = element.attr("href");

			URL url = new URL(currentURL, link);

			int score = getScore(element, doc, query);

			if (!scoreToURLs.containsKey(score)) {
				scoreToURLs.put(score, new ArrayList<String>());
			}
			scoreToURLs.get(score).add(url.toString());
		}

		return scoreToURLs;
	}
}