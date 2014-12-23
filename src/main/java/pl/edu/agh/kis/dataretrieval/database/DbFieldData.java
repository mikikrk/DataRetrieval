package pl.edu.agh.kis.dataretrieval.database;

import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;

public class DbFieldData {
	private String dbColName;
	private String dbColType;
	private String dbConstraints;
	private String dataType;
	private boolean array;
	private boolean dbOverwrite;
	private Object value;
	
	public DbFieldData(CrawlingData nodeData, Object value) {
		super();
		this.dbColName = nodeData.getDbColName();
		this.dbColType = nodeData.getDbColType();
		this.dbConstraints = nodeData.getDbConstraints();
		this.dataType = nodeData.getDataType();
		this.array = nodeData.isArray();
		this.dbOverwrite = nodeData.isDbOverwrite();
		this.value = value;
	}
	
	public DbFieldData(String dbFieldname,
			String dbColumnType, String dbConstraints, String dataType,
			boolean array, boolean dbOverride, Object value) {
		super();
		this.dbColName = dbFieldname;
		this.dbColType = dbColumnType;
		this.dbConstraints = dbConstraints;
		this.dataType = dataType;
		this.array = array;
		this.dbOverwrite = dbOverride;
		this.value = value;
	}
	
	public String getDbColName() {
		return dbColName;
	}
	public void setDbColName(String dbFieldname) {
		this.dbColName = dbFieldname;
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
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isDbOverwrite() {
		return dbOverwrite;
	}

	public void setDbOverwrite(boolean dbOverwrite) {
		this.dbOverwrite = dbOverwrite;
	}
	
	
}
