package pl.edu.agh.kis.dataretrieval.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;


public class RetrieverDao {
	
	private Connection connection;

	
	
	public RetrieverDao(String configPath) throws ParserConfigurationException, SAXException, IOException, RetrievalException, SQLException{
		setConnection(new RetrievingConfigurationReader().loadDbData(configPath));
	}
	
	public RetrieverDao(DatabaseData dbData) throws SQLException{
		setConnection(dbData);
	}
	
	private void setConnection(DatabaseData dbData) throws SQLException{
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://"+dbData.getHost()+":"+dbData.getPort()+"/"+dbData.getDbname(), dbData.getUser(), dbData.getPassword());
		} catch (ClassNotFoundException e) {
			throw new SQLException("Can't find appropriate JDBC Driver");
		}
	}
	
	public void closeConnection() throws SQLException{
		connection.close();
	}
	
	public void createTable(List<RetrievingData> crawlingDataList) throws SQLException{
		Map<String, List<RetrievingData>> tablesData = split4Tables(crawlingDataList);
		
		for(Entry<String, List<RetrievingData>> entry: tablesData.entrySet()){
			createTable(entry.getKey(), entry.getValue());
		}
	}
	
	public void createTable(String tableName, List<RetrievingData> siteData) throws SQLException{
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = md.getTables(null, null, tableName, null);
		boolean exists = false;
		while (rs.next()) {
		  if (rs.getString("TABLE_NAME").equals(tableName)){
			  exists = true;
		  }
		}
		if (siteData.get(0).isDbOverwrite() || !exists){
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE IF EXISTS " + tableName);
			
			StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + "( url text PRIMARY KEY,");
			for (RetrievingData columnData: siteData){
				sql.append(columnData.getDbColName() + " " + columnData.getDbColType() + " " + columnData.getDbConstraints() + ", ");
			}
			sql.setCharAt(sql.length() - 2, ')');
			sql.append(";");
			statement.execute(sql.toString());
		}
	}
	
	public void addSiteData(String tableName, List<DbFieldData> siteData) throws SQLException{
		PreparedStatement statement = null;
		try {
			while (statement == null){
				statement = connection.prepareStatement(prepareSql4AddSiteData(tableName, siteData));
			}
			prepareStatement(statement, siteData);
			statement.execute();
		} catch (SQLException e) {
			if (!e.getSQLState().equals("23505")){	//DUPLICATE KEY mo¿e byæ próba wsadzania tego samego rakordu ponownie, która jest ignorowana, poniewa¿ w kilku wyszukaniach mo¿e byæ ten sam rekord
				throw e;
			}else{
				updateSiteData(tableName, siteData);
			}
		}
	}
	
	public void updateSiteData(String tableName, List<DbFieldData> siteData) throws SQLException{
		PreparedStatement statement = null;
		while (statement == null){
			statement = connection.prepareStatement(prepareSql4UpdateSiteData(tableName, siteData));
		}
		prepareStatement(statement, siteData);
		statement.execute();
	}
	
	private String prepareSql4AddSiteData(String tableName, List<DbFieldData> siteData){
		StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "( ");
		StringBuilder sqlArgs = new StringBuilder(") VALUES(");
		for (DbFieldData field: siteData){
			sql.append(field.getDbColName() + ", ");
			sqlArgs.append("?, ");
		}
		sql.deleteCharAt(sql.length() - 2);
		sqlArgs.setCharAt(sqlArgs.length() - 2, ')');
		sql.append(sqlArgs.toString());
		return sql.toString();
	}
	
	private String prepareSql4UpdateSiteData(String tableName, List<DbFieldData> siteData){
		StringBuilder sql = new StringBuilder("UPDATE " + tableName + "SET ");
		String url = new String();
		for (DbFieldData field: siteData){
			if (field.getDbColName().equals("url")){
				url = (String) field.getValue();
			}
			sql.append(field.getDbColName() + "=?, ");
		}
		sql.deleteCharAt(sql.length() - 2);
		sql.append("WHERE url=" + url);
		return sql.toString();
	}
	
	private void prepareStatement(PreparedStatement statement, List<DbFieldData> data) throws SQLException{
		int i = 1;
		for (DbFieldData arg: data){
			if(arg.isArray()){
				statement.setArray(i, connection.createArrayOf(arg.getDbColType().toLowerCase().substring(0, arg.getDbColType().indexOf('[')), ((List<Object>) arg.getValue()).toArray()));
			}else{
				statement.setObject(i, arg.getValue());
			}
			i++;
		}
	}
	
	private Map<String, List<RetrievingData>> split4Tables(List<RetrievingData> crawlingDataList){
		Map<String, List<RetrievingData>> tables = new HashMap<String, List<RetrievingData>>();
		for (RetrievingData crawlingData: crawlingDataList){
			if (crawlingData.getDataType().equals("link")){
				Map<String, List<RetrievingData>> linkNodes = split4Tables(crawlingData.getUnderLink());
				for (Entry<String, List<RetrievingData>> entry: linkNodes.entrySet()){
					if(tables.containsKey(entry.getKey())){
						tables.get(entry.getKey()).addAll(entry.getValue());
					}else{
						tables.put(entry.getKey(), entry.getValue());
					}
				}
			}else{
				if (tables.containsKey(crawlingData.getDbTableName())){
					tables.get(crawlingData.getDbTableName()).add(crawlingData);
				}else{
					List<RetrievingData> newList =  new LinkedList<RetrievingData>();
					newList.add(crawlingData);
					tables.put(crawlingData.getDbTableName(), newList);
				}
			}
		}
		return tables;
	}
}
