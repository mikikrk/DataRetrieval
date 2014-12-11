package pl.edu.agh.kis.dataretrieval.configuration.crawl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;

public class CrawlingData extends FindNodeData{
	private String dbTableName;
	private String dbColName;
	private String dbColType;
	private String dbConstraints;
	
	private ArrayList<Integer> benchmarkNo = new ArrayList<Integer>();
	
	private String dataPattern;
	
	private boolean array;

	private ArrayList<CrawlingData> underLink = new ArrayList<CrawlingData>();
	
	public String getDbColName() {
		return dbColName;
	}
	public void setDbColName(String dbFieldname) {
		this.dbColName = dbFieldname;
	}
	public ArrayList<Integer> getBenchmarkNo() {
		return benchmarkNo;
	}
	public void addBenchmarkNo(int no){
		benchmarkNo.add(no);
	}
	public String getDataPattern() {
		return dataPattern;
	}
	public void setDataPattern(String dataPattern) {
		this.dataPattern = dataPattern;
	}
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
	public String getDbTableName() {
		return dbTableName;
	}
	public void setDbTableName(String dbTableName) {
		this.dbTableName = dbTableName;
	}
	public ArrayList<CrawlingData> getUnderLink() {
		return underLink;
	}
	public void setUnderLink(ArrayList<CrawlingData> underLink) {
		this.underLink = underLink;
	}
	public void addUnderLink(CrawlingData node){
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
