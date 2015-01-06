package pl.edu.agh.kis.dataretrieval.database;

import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;

public class DbFieldData {
	private String dbColName;
	private String dbColType;
	private boolean array;
	private Object value;
	
	public DbFieldData(RetrievingData nodeData, Object value) {
		super();
		this.dbColName = nodeData.getDbColName();
		this.dbColType = nodeData.getDbColType();
		this.array = nodeData.isArray();
		this.value = value;
	}
	
	public DbFieldData(String dbFieldname,
			String dbColumnType, boolean array, Object value) {
		super();
		this.dbColName = dbFieldname;
		this.dbColType = dbColumnType;
		this.array = array;
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
}
