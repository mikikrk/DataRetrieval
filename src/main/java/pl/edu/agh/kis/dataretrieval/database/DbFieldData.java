package pl.edu.agh.kis.dataretrieval.database;

import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;

public class DbFieldData {
	private String dbColName;
	private String dbColType;
	private boolean array;
	private Object value;
	private boolean primaryKey;
	
	public DbFieldData(RetrievingData nodeData, Object value) {
		super();
		this.dbColName = nodeData.getDbColName();
		this.dbColType = nodeData.getDbColType();
		this.array = nodeData.isArray();
		this.value = value;
		primaryKey = checkPrimaryKey(nodeData.getDbConstraints(), nodeData.getDbTableConstraints());
	}
	
	public DbFieldData(String dbFieldname,
			String dbColumnType, boolean array, String constraints, String tableConstraints, Object value) {
		super();
		this.dbColName = dbFieldname;
		this.dbColType = dbColumnType;
		this.array = array;
		this.value = value;
		this.primaryKey = checkPrimaryKey(constraints, tableConstraints);
	}
	
	private boolean checkPrimaryKey(String colConstraints, String tabConstraints){
		if (colConstraints != null && colConstraints.toUpperCase().contains("PRIMARY KEY")){
			return true;
		}else{
			if (tabConstraints != null && tabConstraints.toUpperCase().contains("PRIMARY KEY")){
				int indexOfPK = tabConstraints.indexOf("PRIMARY KEY");
				int indexOfPKBegin = tabConstraints.indexOf("(", indexOfPK);
				int indexOfPKEnd = tabConstraints.indexOf(")", indexOfPK);
				String primaryKeys = tabConstraints.substring(indexOfPKBegin + 1, indexOfPKEnd - 1);
				if (primaryKeys.toUpperCase().contains(dbColName.toUpperCase())){
					return true;	
				}
			}
		}
		return false;
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

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

}
