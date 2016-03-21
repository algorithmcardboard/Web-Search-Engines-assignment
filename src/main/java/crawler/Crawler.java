package crawler;

import java.io.IOException;
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
	private int maxPages = Integer.MAX_VALUE;
	private int maxDepth = 1;

	Crawler() {
		executor = new ThreadPoolExecutor(2, 6, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	private void shutdown() {
		this.executor.shutdown();
	}

	private CrawlResult tryCrawl(final String query) throws InterruptedException, ExecutionException {
		Future<CrawlResult> submit = executor.submit(new Callable<CrawlResult>() {
			@Override
			public CrawlResult call() throws Exception {
				return new CrawlTask(prioQueue.poll()).fetchAndSavePage(query);
			}
		});
		return submit.get();
	}

	public void crawl(String seedURL, String query) throws IOException, InterruptedException, ExecutionException {

		offerToQueue(seedURL, 0, 1);

		while (prioQueue.size() > 0) {
			System.out.println("fetching and crawling");

			CrawlResult crawlResult = tryCrawl(query);

			if (crawlResult == null) {
				System.out.println("Result is none");
				continue;
			}

			if (crawlResult.getLink().getDepth() >= maxDepth) {
				System.out.println("depth exceeded");
				continue;
			}

			if (this.crawledPages.size() > this.maxPages) {
				break;
			}

			if (crawlResult == null || crawlResult.getScoresAndLinks() == null) {
				System.out.println("no response");
				continue;
			}

			for (Entry<Integer, List<String>> entry : crawlResult.getScoresAndLinks().entrySet()) {
				int curScore = entry.getKey();
				for (String urlString : entry.getValue()) {
					offerToQueue(urlString, curScore, crawlResult.getLink().getDepth() + 1);
					queueBack(urlString);
				}
			}
		}

		System.out.println("out of loop");

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

	private void offerToQueue(String url, int priority, int depth) {
		if (crawledPages.containsKey(url)) {
			this.crawledPages.get(url).addScore(priority);
			queueBack(url);
		} else {
			Link link = new Link(url, priority, depth);
			prioQueue.offer(link);
			this.crawledPages.put(url, link);
		}
	}

	public static void main(String[] args) {
//		String seedURL = "https://en.wikipedia.org/wiki/Wikipedia:Vandalismusmeldung";
		String seedURL = "http://cs.nyu.edu/courses/spring16/CSCI-GA.2580-001/MarineMammal/PolarBear.html";
		
		Crawler c = new Crawler();
		try {
			c.crawl(seedURL, "Cleopetra");
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.shutdown();
	}
}
