package pl.edu.agh.kis.dataretrieval.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;

public class FindNodeData implements ConfigData{
	protected String benchmark;
	protected String benchmarkType;
	protected String benchmarkAttr;
	
	protected String dataType;
	
	protected String searchPath;
	protected String searchDataType;
	
	protected String searchNextPath;
	
	protected String terminatorNode;
	protected String terminatorNodeType;
	protected String terminatorPath;
	
	protected LinkedList<String> searchPathNodes = new LinkedList<String>();
	protected LinkedList<String> searchNextNodes = new LinkedList<String>();
	protected LinkedList<String> searchTerminatorNodes = new LinkedList<String>();
	
	public String getBenchmark() {
		return benchmark;
	}
	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}
	public String getBenchmarkType() {
		return benchmarkType;
	}
	public void setBenchmarkType(String benchmarkType) {
		this.benchmarkType = benchmarkType;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getSearchPath() {
		return searchPath;
	}
	public void setSearchPath(String searchPath) {
		searchPathNodes = new LinkedList<String>(Arrays.asList(searchPath.split("/")));
		if (searchPathNodes.isEmpty()){
			searchPathNodes.add(searchPath);
		}
		int i = 0;
		int size = searchPathNodes.size();
		while (i<size){
			String nodeType = searchPathNodes.poll();
			if (nodeType.matches("\\d+\\..+")){
				String[] n = nodeType.split("\\.");
				if (n.length == 2){
					for (int j=0; j<Integer.valueOf(n[0]); j++){
						searchPathNodes.add(n[1]);
					}
				}
			}else{
				searchPathNodes.add(nodeType);
			}
			i++;
		}
		this.searchPath = searchPath;
	}
	public void setSearchDataType(String searchDataType) {
		this.searchDataType = searchDataType;
	}
	public String getSearchNextPath() {
		return searchNextPath;
	}
	public void setSearchNextPath(String searchNextPath) {
		searchNextNodes = new LinkedList<String>(Arrays.asList(searchNextPath.split("/")));
		int i = 0;
		int size = searchNextNodes.size();
		while (i<size){
			String nodeType = searchNextNodes.poll();
			if (nodeType.matches("\\d+\\..+")){
				String[] n = nodeType.split("\\.");
				if (n.length == 2){
					for (int j=0; j<Integer.valueOf(n[0]); j++){
						searchNextNodes.add(n[1]);
					}
				}
			}else{
				searchNextNodes.add(nodeType);
			}
			i++;
		}
		this.searchNextPath = searchNextPath;
	}
	public String getTerminatorNode() {
		return terminatorNode;
	}
	public void setTerminatorNode(String terminatorNode) {
		this.terminatorNode = terminatorNode;
	}
	public String getTerminatorNodeType() {
		return terminatorNodeType;
	}
	public void setTerminatorNodeType(String terminatorNodeType) {
		this.terminatorNodeType = terminatorNodeType;
	}
	public LinkedList<String> getSearchPathNodes() {
		return new LinkedList<String>(searchPathNodes);
	}
	public LinkedList<String> getSearchNextNodes() {
		return new LinkedList<String>(searchNextNodes);
	}
	public String getBenchmarkAttr() {
		return benchmarkAttr;
	}
	public void setBenchmarkAttr(String benchmarkAttr) {
		this.benchmarkAttr = benchmarkAttr;
	}
	public String getTerminatorPath() {
		return terminatorPath;
	}
	public void setTerminatorPath(String terminatorPath) {
		searchTerminatorNodes = new LinkedList<String>(Arrays.asList(terminatorPath.split("/")));
		int i = 0;
		int size = searchTerminatorNodes.size();
		while (i<size){
			String nodeType = searchTerminatorNodes.poll();
			if (nodeType.matches("\\d+\\..+")){
				String[] n = nodeType.split("\\.");
				if (n.length == 2){
					for (int j=0; j<Integer.valueOf(n[0]); j++){
						searchTerminatorNodes.add(n[1]);
					}
				}
			}else{
				searchTerminatorNodes.add(nodeType);
			}
			i++;
		}
		this.terminatorPath = terminatorPath;
	}
	public String getSearchDataType() {
		return searchDataType;
	}
	public LinkedList<String> getSearchTerminatorNodes() {
		return searchTerminatorNodes;
	}
}
