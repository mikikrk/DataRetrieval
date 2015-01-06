package pl.edu.agh.kis.dataretrieval.configuration.retrieving;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;

public class RetrievingData extends FindNodeData{
	private String dbTableName;
	private String dbColName;
	private String dbColType;
	private String dbConstraints;
	private boolean dbOverwrite;
	
	private ArrayList<Integer> benchmarkNo = new ArrayList<Integer>();
	
	private String dataPattern;
	
	private boolean array;

	private ArrayList<RetrievingData> underLink = new ArrayList<RetrievingData>();
	
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
	public ArrayList<RetrievingData> getUnderLink() {
		return underLink;
	}
	public void setUnderLink(ArrayList<RetrievingData> underLink) {
		this.underLink = underLink;
	}
	public void addUnderLink(RetrievingData node){
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
	public boolean isDbOverwrite() {
		return dbOverwrite;
	}
	public void setDbOverwrite(boolean dbOverwrite) {
		this.dbOverwrite = dbOverwrite;
	}
}
