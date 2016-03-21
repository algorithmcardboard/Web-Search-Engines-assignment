package crawler;

public class Link implements Comparable<Link>{
	
	private static int counter = 1;
	
	private int id; 
	private String url;
	private int priority = 0;
	private int responseCode;
	private boolean hasCrawled = false;
	private int depth = 0;

	public Link(String urlString, int priority, int depth){
		this.url = urlString;
		this.priority = priority;
		this.depth = depth;
		this.id = counter++;
	}
	
	public void doneCrawling(){
		this.hasCrawled = true;
	}
	
	public boolean wasCrawled(){
		return this.hasCrawled;
	}
	
	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	public void addScore(int priority){
		if(hasCrawled){
			return;
		}
		this.priority += priority;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @return the score
	 */
	public int getPriority() {
		return priority;
	}
	
	public int getDepth() {
		return depth;
	}
	
	@Override
	public int compareTo(Link o) {
		if(new Integer(o.priority).compareTo(this.priority) == 0){
			return new Integer(this.id).compareTo(o.id);
		}
		return new Integer(o.priority).compareTo(this.priority);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + priority;
		result = prime * result + responseCode;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (priority != other.priority)
			return false;
		if (responseCode != other.responseCode)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
