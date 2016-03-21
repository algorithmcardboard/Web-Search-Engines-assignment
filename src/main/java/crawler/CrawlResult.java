package crawler;

import java.util.List;
import java.util.Map;

public class CrawlResult {

	private int responseCode;
	private long totalTransferred;
	private Map<Integer, List<String>> scoresAndLinks;
	private Link link;
	private String url;

	public CrawlResult(Link link, int responseCode) {
		this.responseCode = responseCode;
		this.link = link;
		this.url = link.getUrl();
	}

	public CrawlResult(Link link, int responseCode, long totalTransferred, Map<Integer, List<String>> scoresAndLinks) {
		this.link = link;
		this.url = link.getUrl();
		this.responseCode = responseCode;
		this.totalTransferred = totalTransferred;
		this.scoresAndLinks = scoresAndLinks;
	}

	/**
	 * @return the linksAndScores
	 */
	public Map<Integer, List<String>> getScoresAndLinks() {
		return scoresAndLinks;
	}

	/**
	 * @return the totalTransferred
	 */
	public long getTotalTransferred() {
		return totalTransferred;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	public String getUrl() {
		return this.url;
	}

	public Link getLink() {
		// TODO Auto-generated method stub
		return this.link;
	}

}
