package pl.edu.agh.kis.dataretrieval.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import pl.edu.agh.kis.dataretrieval.journal.configuration.Configuration;

public class CrawlerDao {
	
	private Connection connection;

	public CrawlerDao(){
		setConnection(new Configuration().loadDbData("config.xml"));
	}
	
	public CrawlerDao(String configPath){
		setConnection(new Configuration().loadDbData(configPath));
	}
	
	public CrawlerDao(DatabaseData dbData){
		setConnection(dbData);
	}
	
	private void setConnection(DatabaseData dbData){
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://"+dbData.getHost()+":"+dbData.getPort()+"/"+dbData.getDbname(), dbData.getUser(), dbData.getPassword());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println("Can't find appropriate JDBC Driver.");
			e.printStackTrace();
		}
	}
	
	public void closeConnection(){
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createTable(String tableName, List<FieldData> siteData){
		
		try {
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE IF EXISTS " + tableName);
		
			StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + "( id SERIAL PRIMARY KEY,");
			for (FieldData columnData: siteData){
				sql.append(columnData.getDbColName() + " " + columnData.getDbColType() + " " + columnData.getDbConstraints() + ", ");
			}
			sql.setCharAt(sql.length() - 2, ')');
			sql.append(";");
			statement.execute(sql.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSiteData(String tableName, List<FieldData> siteData){
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(prepareSql4AddSiteData(tableName, siteData));
			prepareStatement(statement, siteData);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String prepareSql4AddSiteData(String tableName, List<FieldData> siteData){
		StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + "( ");
		StringBuilder sqlArgs = new StringBuilder(") VALUES(");
		for (FieldData field: siteData){
			sql.append(field.getDbColName() + ", ");
			sqlArgs.append("?, ");
		}
		sql.deleteCharAt(sql.length() - 2);
		sqlArgs.setCharAt(sqlArgs.length() - 2, ')');
		sql.append(sqlArgs.toString());
		return sql.toString();
	}
	
	private void prepareStatement(PreparedStatement statement, List<FieldData> data){
		try {
			int i = 1;
			for (FieldData arg: data){
				if(arg.isArray()){
					statement.setArray(i, connection.createArrayOf(arg.getDbColType().toLowerCase().substring(0, arg.getDbColType().indexOf('[')), ((List<Object>) arg.getValue()).toArray()));
				}else{
					statement.setObject(i, arg.getValue());
				}
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
