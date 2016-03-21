package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;

public class RobotsTxt {

	private static final String USER_AGENT = "User-agent: *";
	private static final String DISALLOW_STRING = "Disallow: ";
	private static Map<String, Boolean> robotsPresent = new HashMap<>();

	static {
		File f = new File(CrawlTask.absPath + "/robots");
		f.mkdirs();
	}

	public static boolean isSafe(URL url) throws IOException {
		
		boolean isSafe = true;
		
		if (!robotsPresent.containsKey(url.getHost())) {
			robotsPresent.put(url.getHost(), fetchAndSave(url));
		}

		if (robotsPresent.get(url.getHost()) == false) {
			return isSafe;
		}

		String file = getFileName(url);
		boolean parseRules = false;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				if (line.startsWith("#")) {
					continue;
				}
				int endIndex = line.indexOf('#') == -1?line.length():line.indexOf('#');
				line = line.substring(0, endIndex).trim();
				
				if(line.length() <= 0){continue;}
				
				if(parseRules && line.startsWith("User-agent: ")){
					parseRules = false;
					continue;
				}
				
				if (line.equalsIgnoreCase(USER_AGENT)) {
					parseRules = true;
					continue;
				}
				
				if(parseRules && !isSafeURL(url, line)){
					isSafe = false;
					break;
				}
			}
		}

		return isSafe;
	}

	private static boolean isSafeURL(URL url, String line) {
		int startIndex = line.length() > DISALLOW_STRING.length()? DISALLOW_STRING.length(): 0;
		if(startIndex == 0){
			return true;
		}
		String disallowString = line.substring(startIndex, line.length());
		if(url.getPath().startsWith(disallowString) || disallowString.startsWith(url.getPath())){
			return false;
		}
		
		return true;
	}

	private static String getFileName(URL url) {
		return CrawlTask.absPath + "/robots/" + url.getHost();
	}

	private static boolean fetchAndSave(URL url) throws IOException {
		String newUrl = url.getProtocol() + "://" + url.getHost() + ((url.getPort() != -1) ? ":" + url.getPort() : "")
				+ "/robots.txt";
		URL nURL = new URL(newUrl);

		HttpURLConnection connection = (HttpURLConnection) nURL.openConnection();
		connection.setRequestMethod("HEAD");

		if (connection.getResponseCode() / 100 != 2) {
			return false;
		}

		connection = (HttpURLConnection) nURL.openConnection();
		connection.setRequestMethod("GET");

		ReadableByteChannel channel = Channels.newChannel(nURL.openStream());
		FileOutputStream fos = new FileOutputStream(new File(getFileName(nURL)));
		fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		fos.close();
		
		return true;
	}

}
