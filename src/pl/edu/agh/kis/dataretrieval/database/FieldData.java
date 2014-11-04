package pl.edu.agh.kis.dataretrieval.database;

import pl.edu.agh.kis.dataretrieval.journal.configuration.NodeData;

public class FieldData {
	private String dbColName;
	private String dbColType;
	private String dbConstraints;
	private String dataType;
	private boolean array;
	private Object value;
	
	public FieldData(NodeData nodeData, Object value) {
		super();
		this.dbColName = nodeData.getDbColName();
		this.dbColType = nodeData.getDbColType();
		this.dbConstraints = nodeData.getDbConstraints();
		this.dataType = nodeData.getDataType();
		this.array = nodeData.isArray();
		this.value = value;
	}
	
	public FieldData(String dbFieldname,
			String dbColumnType, String dbConstraints, String dataType,
			boolean array, Object value) {
		super();
		this.dbColName = dbFieldname;
		this.dbColType = dbColumnType;
		this.dbConstraints = dbConstraints;
		this.dataType = dataType;
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
	
	
}
