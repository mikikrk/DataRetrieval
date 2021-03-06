package pl.edu.agh.kis.dataretrieval.configuration.searching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;

public class SearchingData {
	private String url;
	private int maxRecords;
	private int startSite;

	private List<ConfigData> flowDataList = new ArrayList<ConfigData>();

	private int index = 0;
	
	public SearchingData() {
		super();
	}
	
	public List<ConfigData> getFlowDataList() {
		return flowDataList;
	}

	public void setFlowDataList(List<ConfigData> flowDataList) {
		this.flowDataList = flowDataList;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMaxRecords() {
		return maxRecords;
	}

	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}
	
	public void addFlowData(ConfigData flowData){
		if (flowData instanceof LinkData || flowData instanceof FormData || flowData instanceof SwitchData || flowData instanceof ContentData){
			flowDataList.add(flowData);
		}else{
			throw new IllegalArgumentException();
		}
	}
	
	public boolean hasNextFlowData(){
		return flowDataList.size() > index ? true : false;
	}
	
	public Object nextFlowData(){
		return flowDataList.get(index++);
	}
	
	public void reset(){
		index = 0;
	}

	public int getStartSite() {
		return startSite;
	}

	public void setStartSite(int startSite) {
		this.startSite = startSite;
	}
	
}
