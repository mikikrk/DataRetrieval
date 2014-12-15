package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

public class NoSiteRetrievedException extends Exception {
	private int linkCount;

	public NoSiteRetrievedException(int linkCount) {
		super();
		this.linkCount = linkCount;
	}

	public int getLinkCount() {
		return linkCount;
	}
	
}
