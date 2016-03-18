package crawler;

public class CrawlResult{

	private int responseCode;
	private long totalTransferred;

	public CrawlResult(int responseCode) {
		this.responseCode = responseCode;
	}

	public CrawlResult(int responseCode, long totalTransferred) {
		this.responseCode = responseCode;
		this.totalTransferred = totalTransferred;
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

}
