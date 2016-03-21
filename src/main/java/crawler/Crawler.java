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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Crawler {

	@Parameter(names = { "-url", "-u" }, required = true)
	private String seedURL;

	@Parameter(names = { "-query", "-q" }, required = true, variableArity = true)
	private List<String> query;

	@Parameter(names = { "-docs" }, required = true)
	static String docs;

	@Parameter(names = { "-maxPages", "-m" })
	private int maxPages = 100;

	@Parameter(names = { "-trace", "-t" })
	private boolean debug = false;
	
	public static boolean verbose = false;
	
	private ExecutorService executor = null;
	private Map<String, Link> crawledPages = new ConcurrentHashMap<>();
	private PriorityBlockingQueue<Link> prioQueue = new PriorityBlockingQueue<Link>();
	private int maxDepth = 10;


	Crawler() {
		executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		Crawler.verbose = this.debug;
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

	public void crawl() throws IOException, InterruptedException, ExecutionException {

		System.out
				.println("Crawling for " + maxPages + " pages relevant to \"" + query + "\" starting from " + seedURL);

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
			if (totalDownloaded >= maxPages) {
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
		Crawler c = new Crawler();
		new JCommander(c, args);
		try {
			c.crawl();
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.shutdown();
	}
}
