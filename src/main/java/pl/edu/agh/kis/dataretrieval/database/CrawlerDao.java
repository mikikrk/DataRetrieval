package pl.edu.agh.kis.dataretrieval.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;


public class CrawlerDao {
	
	private Connection connection;

	
	
	public CrawlerDao(String configPath) throws ParserConfigurationException, SAXException, IOException, RetrievalException{
		setConnection(new CrawlingConfigurationReader().loadDbData(configPath));
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
	
	public void createTable(String tableName, List<DbFieldData> siteData) throws SQLException{
		if (siteData.get(0).isDbOverride() || /*!*/connection.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"}) != null){ //TODO .getString("TABLE_NAME").equals(tableName)){
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE IF EXISTS " + tableName);
			
			StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + "(");
			for (DbFieldData columnData: siteData){
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
			}
		}
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
}
