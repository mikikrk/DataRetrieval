package pl.edu.agh.kis.dataretrieval.journal.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class NodeData {
	private String dbTableName;
	private String dbColName;
	private String dbColType;
	private String dbConstraints;
	
	private String benchmark;
	private String benchmarkType;
	private ArrayList<Integer> benchmarkNo = new ArrayList<Integer>();
	private String benchmarkAttr;
	
	private String dataType;
	private String dataPattern;
	
	private String searchPath;
	private String searchDataType;
	
	private boolean array;
	private String searchNextPath;
	
	private String terminatorNode;
	private String terminatorNodeType;
	private String terminatorPath;
	
	private LinkedList<String> searchPathNodes;
	private LinkedList<String> searchNextNodes;
	private LinkedList<String> searchTerminatorNodes;

	private ArrayList<NodeData> underLink = new ArrayList<NodeData>();
	
	public String getDbColName() {
		return dbColName;
	}
	public void setDbColName(String dbFieldname) {
		this.dbColName = dbFieldname;
	}
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
	public ArrayList<Integer> getBenchmarkNo() {
		return benchmarkNo;
	}
	public void addBenchmarkNo(int no){
		benchmarkNo.add(no);
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDataPattern() {
		return dataPattern;
	}
	public void setDataPattern(String dataPattern) {
		this.dataPattern = dataPattern;
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
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
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
	public String getDbTableName() {
		return dbTableName;
	}
	public void setDbTableName(String dbTableName) {
		this.dbTableName = dbTableName;
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
	public ArrayList<NodeData> getUnderLink() {
		return underLink;
	}
	public void setUnderLink(ArrayList<NodeData> underLink) {
		this.underLink = underLink;
	}
	public void addUnderLink(NodeData node){
		underLink.add(node);
	}
	public String getDbColType() {
		return dbColType;
	}
	public void setDbColType(String dbColumnType) {
		this.dbColType = dbColumnType;
	}
	public String getDbConstraints() {
		return dbConstraints;
	}
	public void setDbConstraints(String dbConstraints) {
		this.dbConstraints = dbConstraints;
	}
}
