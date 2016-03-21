package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Crawler {

	private ExecutorService executor = null;
	private Map<String, Link> crawledPages = new ConcurrentHashMap<>();
	private PriorityBlockingQueue<Link> prioQueue = new PriorityBlockingQueue<Link>();
	private static int maxPages = 10;
	private int maxDepth = 10;

	public static boolean verbose = true;

	Crawler() {
		executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	private void shutdown() {
		this.executor.shutdown();
	}

	private CrawlResult tryCrawl(final List<String> query) throws InterruptedException, ExecutionException {
		Future<CrawlResult> submit = executor.submit(new Callable<CrawlResult>() {
			@Override
			public CrawlResult call() throws Exception {
				return new CrawlTask(prioQueue.poll()).fetchAndSavePage(query);
			}
		});
		return submit.get();
	}

	public void crawl(String seedURL, List<String> query) throws IOException, InterruptedException, ExecutionException {

		offerToQueue(seedURL, 0, 1, 0);

		int totalDownloaded = 0;
		while (prioQueue.size() > 0) {
			System.out.println();
			CrawlResult crawlResult = tryCrawl(query);

			if (crawlResult == null) {
				System.out.println("Result is none");
				continue;
			}

			totalDownloaded++;
			if(totalDownloaded >= Crawler.maxPages){
				break;
			}
			if (crawlResult.getLink().getDepth() >= maxDepth) {
				System.out.println("depth exceeded");
				continue;
			}

			if (crawlResult == null || crawlResult.getScoresAndLinks() == null) {
				System.out.println("no response");
				continue;
			}

			for (Entry<Integer, List<String>> entry : crawlResult.getScoresAndLinks().entrySet()) {
				int curScore = entry.getKey();
				for (String urlString : entry.getValue()) {
					offerToQueue(urlString, curScore, crawlResult.getLink().getDepth() + 1, curScore);
					queueBack(urlString);
				}
			}
		}

		return;
	}

	private void queueBack(String urlString) {
		Link l = this.crawledPages.get(urlString);
		if (l == null) {
			return;
		}
		boolean remove = prioQueue.remove(l);
		if (remove) {
			prioQueue.offer(l);
		}
	}

	private void offerToQueue(String url, int priority, int depth, int curScore) {
		if (crawledPages.containsKey(url)) {
			this.crawledPages.get(url).addScore(priority);
			queueBack(url);
		} else {
			System.out.println("Adding to queue: " + url + " Score = " + curScore);
			Link link = new Link(url, priority, depth);
			prioQueue.offer(link);
			this.crawledPages.put(url, link);
		}
	}

	public static void main(String[] args) {
		// String seedURL =
		// "https://en.wikipedia.org/wiki/Wikipedia:Vandalismusmeldung";
		String seedURL = "http://cs.nyu.edu/courses/spring16/CSCI-GA.2580-001/MarineMammal/Whale.html";

		List<String> query = new ArrayList<String>();
		query.add("species");
		query.add("whale");
		query.add("whales");

		if (Crawler.verbose) {
			System.out.println("Crawling for " + Crawler.maxPages + " pages relevant to \"" + query
					+ "\" starting from " + seedURL);
		}

		Crawler c = new Crawler();
		try {
			c.crawl(seedURL, query);
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.shutdown();
	}
}
