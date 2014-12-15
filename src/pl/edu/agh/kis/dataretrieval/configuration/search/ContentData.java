package pl.edu.agh.kis.dataretrieval.configuration.search;

import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;


public class ContentData extends FindNodeData{
	private Integer benchmarkNo;
	private String linkType;

	private LinkData nextPageLink;
	
	private Integer crawledSites;
	

	public Integer getCrawledSites() {
		return crawledSites;
	}

	public void setCrawledSites(Integer crawledSites) {
		this.crawledSites = crawledSites;
	}

	public Integer getBenchmarkNo() {
		return benchmarkNo;
	}

	public void setBenchmarkNo(Integer benchmarkNo) {
		this.benchmarkNo = benchmarkNo;
	}
	
	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public LinkData getNextPageLink() {
		return nextPageLink;
	}

	public void setNextPageLink(LinkData nextPageLink) {
		this.nextPageLink = nextPageLink;
	}
}
