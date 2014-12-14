package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.List;

public class SearchingData {
	private String url;
	private int bulkRecords;
	
	private int crawledSites;

	private List<Object> flowDataList = new ArrayList<Object>();
	private ContentData contentFinder;
	
	private int index = 0;
	
	public SearchingData() {
		super();
	}
	
	public SearchingData(List<Object> flowList, ContentData contentFinder) {
		super();
		this.flowDataList = flowList;
		this.contentFinder = contentFinder;
	}
	
	public SearchingData(String url, List<Object> flowDataList,
			ContentData contentFinder) {
		super();
		this.url = url;
		this.flowDataList = flowDataList;
		this.contentFinder = contentFinder;
	}

	public SearchingData(String url, int bulkRecords,
			List<Object> flowDataList, ContentData contentFinder) {
		super();
		this.url = url;
		this.bulkRecords = bulkRecords;
		this.flowDataList = flowDataList;
		this.contentFinder = contentFinder;
	}

	public List<Object> getFlowDataList() {
		return flowDataList;
	}

	public void setFlowDataList(List<Object> flowDataList) {
		this.flowDataList = flowDataList;
	}

	public ContentData getContentFinder() {
		return contentFinder;
	}

	public void setContentFinder(ContentData contentFinder) {
		this.contentFinder = contentFinder;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getBulkRecords() {
		return bulkRecords;
	}

	public void setBulkRecords(int bulkRecords) {
		this.bulkRecords = bulkRecords;
	}
	
	public void addFlowData(Object flowData){
		if (flowData instanceof LinkData || flowData instanceof FormData){
			flowDataList.add(flowData);
		}else{
			throw new IllegalArgumentException();
		}
	}
	
	public boolean hasNextFlowData(){
		return flowDataList.get(index + 1) == null ? false : true;
	}
	
	public Object nextFlowData(){
		return flowDataList.get(index++);
	}
	
	public void reset(){
		index = 0;
	}

	public int getCrawledSites() {
		return crawledSites;
	}

	public void setCrawledSites(int crawledSites) {
		this.crawledSites = crawledSites;
	}
	
}
