package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import HTMLParser.Parser;

public class CrawlTask {

	private String url;
	private Link link;
	public static String absPath = "docs/";

	public CrawlTask(Link link) {
		this.url = link.getUrl();
		this.link = link;
	}

	public static String getAbspath() {
		return absPath;
	}

	public CrawlResult fetchAndSavePage(List<String> query) throws Exception {

		URL url = new URL(this.url);

		if (this.link.wasCrawled() || !RobotsTxt.isSafe(url)) {
			return null;
		}

		this.link.doneCrawling();

		System.out.println("Downloading: " + this.url + ". Score = " + this.link.getPriority());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("HEAD");
		int responseCode = connection.getResponseCode();
		if (responseCode / 100 != 2) {
			return new CrawlResult(this.link, responseCode);
		}

		String file = CrawlTask.absPath + url.getHost() + url.getPath();
		File f = new File(new File(file).getParent());
		f.mkdirs();

		connection.disconnect();
		connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		ReadableByteChannel channel = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(new File(file));
		long transferFrom = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		fos.close();

		if (Crawler.verbose) {
			System.out.println("Received: " + this.url);
		}

		Parser parser = new Parser(Paths.get(file));
		Map<Integer, List<String>> linksAndScores = parser.getLinksAndScores(url, query);

		return new CrawlResult(link, responseCode, transferFrom, linksAndScores);
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
}
