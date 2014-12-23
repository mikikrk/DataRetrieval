package pl.edu.agh.kis.dataretrieval.retriever.webprocessors.exceptions;

public class NoSiteRetrievedException extends Exception {
	private int linkCount;
	private String message;
	
	public NoSiteRetrievedException(int linkCount, String message) {
		super();
		this.linkCount = linkCount;
		this.message = message;
	}

	public int getLinkCount() {
		return linkCount;
	}

	public String getMessage() {
		return message;
	}
	
}
