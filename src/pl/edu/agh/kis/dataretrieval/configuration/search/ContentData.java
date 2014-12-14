package pl.edu.agh.kis.dataretrieval.configuration.search;

import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;


public class ContentData extends FindNodeData{
	private int benchmarkNo;
	private LinkData nextPageLink;

	public int getBenchmarkNo() {
		return benchmarkNo;
	}

	public void setBenchmarkNo(int benchmarkNo) {
		this.benchmarkNo = benchmarkNo;
	}

	public LinkData getNextPageLink() {
		return nextPageLink;
	}

	public void setNextPageLink(LinkData nextPageLink) {
		this.nextPageLink = nextPageLink;
	}
}
