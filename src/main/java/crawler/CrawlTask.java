package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

public class CrawlTask implements Comparable<CrawlTask>, Callable<CrawlResult> {

	private Integer priority;
	private String url;
	private String requestMethod;
	private static final String absPath = "docs/";
	
	public CrawlTask(int priority, String url, String requestMethod) {
		System.out.println("Creating object");
		this.priority = priority;
		this.url = url;
		if(requestMethod.equals("HEAD") || requestMethod.equals("GET")){
			this.requestMethod = requestMethod;
		}else{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public int compareTo(CrawlTask o) {
		return this.priority.compareTo(o.priority); 
	}

	@Override
	public CrawlResult call() throws Exception {
		System.out.println("Call task called");
		URL url = new URL(this.url);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(this.requestMethod);
		int responseCode = connection.getResponseCode();
		if (responseCode / 100 != 2) {
			return new CrawlResult(responseCode);
		}
		
		String file = CrawlTask.absPath + url.getHost() + url.getPath();
		new File(new File(file).getParent()).mkdirs();
		
		ReadableByteChannel channel = Channels.newChannel(url.openStream());
		FileOutputStream fos = new FileOutputStream(new File(file));
		long transferFrom = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		fos.close();
		
		return new CrawlResult(responseCode, transferFrom);
	}

}
