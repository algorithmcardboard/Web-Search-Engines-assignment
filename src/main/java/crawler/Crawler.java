package crawler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Crawler {

	private ThreadPoolExecutor executor = null;

	Crawler() {
		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(32);
		executor = new ThreadPoolExecutor(4, 6, 10, TimeUnit.SECONDS, queue);
	}
	
	public void push(String url, int score) throws InterruptedException, ExecutionException{
		CrawlTask crawlTask = new CrawlTask(score, url, "HEAD");
		Future<CrawlResult> resultFuture = executor.submit(crawlTask);
		CrawlResult crawlResult = resultFuture.get();
		System.out.println(crawlResult.getResponseCode());
	}

	public static void main(String[] args) throws IOException {
		String seedURL = "http://docs.oracle.com/javase/7/docs/api/java/io/File.html";
		Crawler c = new Crawler();
		try {
			c.push(seedURL, 0);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
