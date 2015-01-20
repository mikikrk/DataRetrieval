package pl.edu.agh.kis.dataretrieval.configuration.retrieving;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;
import pl.edu.agh.kis.dataretrieval.database.DatabaseData;

/**
 * Klasa zajmująca się parsowaniem i wczytywaniem pliku konfiguracyjnego 
 * @author Mikołaj Nowak
 *
 */
public class RetrievingConfigurationReader extends ConfigurationReader{
	private ArrayList<RetrievingData> configNodes;
	
	/**
	 * Wczytanie pliku konfiguracyjnego
	 * @param configFilePath - sciezka do pliku 
	 * @return lista przetworzonych wezlow odpowiadajacych poszczegolnym danym do wyciagniecia ze strony www
	 * @throws RetrievalException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public List<RetrievingData> load(String configFilePath) throws RetrievalException, ParserConfigurationException, SAXException, IOException {
		configNodes = new ArrayList<RetrievingData>();
		Document dom = loadDom(configFilePath);

		String message = parse(dom);
		if (!message.isEmpty()){
			throw new RetrievalException("Error occured while parsing retrieving configuration: \n" + message);	
		}
		
		Node tableNode = dom.getFirstChild().getFirstChild();
		
		while (tableNode != null) {	
			loadTableNode(tableNode);
			tableNode = tableNode.getNextSibling();
		}
		return configNodes;
	}
	
	/**
	 * Wczytywanie danych do laczenia z baza danych
	 * @param configFilePath
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws RetrievalException 
	 */
	public DatabaseData loadDbData(String configFilePath) throws ParserConfigurationException, SAXException, IOException, RetrievalException {
		DatabaseData dbData = new DatabaseData();
		Document dom = loadDom(configFilePath);
		StringBuilder message = new StringBuilder();
		Node configNode = dom.getFirstChild();
		parseConfigNode(configNode, message);
		if (message.length() != 0){
			throw new RetrievalException("Error while parsing database data\n" + message.toString());
		}
		
		NamedNodeMap attrs = configNode.getAttributes();
		dbData.setHost(attrs.getNamedItem("host").getNodeValue());
		dbData.setPort(Integer.valueOf(attrs.getNamedItem("port").getNodeValue()));
		dbData.setDbname(attrs.getNamedItem("dbname").getNodeValue());
		dbData.setUser(attrs.getNamedItem("user").getNodeValue());
		dbData.setPassword(attrs.getNamedItem("password").getNodeValue());
		return dbData;
	}
	
	private void loadTableNode(Node tableNode){
		String dbTableName = tableNode.getNodeName();
		NamedNodeMap attrs = tableNode.getAttributes();
		boolean overwrite;
		boolean addUrl;
		boolean urlAsPrimaryKey;
		String tableConstraints = new String();
		if (attrs.getNamedItem("overwrite") != null){
			overwrite = Boolean.valueOf(attrs.getNamedItem("overwrite").getNodeValue());
		}else{
			overwrite = false;
		}
		if (attrs.getNamedItem("addUrl") != null){
			addUrl = Boolean.valueOf(attrs.getNamedItem("addUrl").getNodeValue());
		}else{
			addUrl = false;
		}
		if (attrs.getNamedItem("urlAsPrimaryKey") != null){
			urlAsPrimaryKey = Boolean.valueOf(attrs.getNamedItem("urlAsPrimaryKey").getNodeValue());
		}else{
			urlAsPrimaryKey = false;
		}
		if (attrs.getNamedItem("constraints") != null) {
			tableConstraints = attrs.getNamedItem("constraints").getNodeValue();
		}
		Node node = tableNode.getFirstChild();
		while (node != null) {
			RetrievingData configNode= loadColumnNode(node, dbTableName, tableConstraints, overwrite, addUrl, urlAsPrimaryKey);
			configNodes.add(configNode);
			node = node.getNextSibling();
		}
	}
	
	private RetrievingData loadColumnNode(Node node, String dbTableName, String tableConstraints, boolean overwrite, boolean addUrl, boolean urlAsPrimaryKey){
		RetrievingData data = new RetrievingData();
		data.setDbOverwrite(overwrite);
		data.setDbAddUrl(addUrl);
		data.setDbTableConstraints(tableConstraints);
		data.setDbUrlAsPrimaryKey(urlAsPrimaryKey);
		data.setDbTableName(dbTableName);
		if(node.getNodeName().equals("link")){
			data.setDataType("link");
			data.setBenchmark(node.getAttributes().getNamedItem("value").getNodeValue());
			data.setBenchmarkType(node.getAttributes().getNamedItem("type").getNodeValue());
			if (node.getAttributes().getNamedItem("attr") != null){
				data.setBenchmarkAttr(node.getAttributes().getNamedItem("attr").getNodeValue());
			}
			Node linkNode = node.getFirstChild();
			while (linkNode != null){
				data.addUnderLink(loadColumnNode(linkNode, dbTableName, tableConstraints, overwrite, addUrl, urlAsPrimaryKey));
				linkNode = linkNode.getNextSibling();
			}
		}else{
			data.setDbColName(node.getNodeName());
			data.setDbColType(getColumnType(node));
			data.setDbConstraints(node.getAttributes().getNamedItem("constraints")!=null ? node.getAttributes().getNamedItem("constraints").getNodeValue() : "");
			
			if (node.hasChildNodes()){
				data.setOnlyDbData(false);
				node = node.getFirstChild();
				loadFirstChildData(node, data);
		
				node = node.getNextSibling();
				loadLastChildData(node, data);
			}else{
				data.setOnlyDbData(true);
			}
			
			node = node.getParentNode().getNextSibling();
		}
		return data;
	}
	
	private void loadFirstChildData(Node node, RetrievingData data){
		data.setBenchmarkType(node.getNodeName());
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			if (attrs.getNamedItem("array") != null
					&& attrs.getNamedItem("array").getNodeValue()
							.equals("true")) {
				data.setArray(true);
			}
			if (attrs.getNamedItem("no") != null) {
				if (attrs.getNamedItem("no").getNodeValue()
						.matches("\\d+")) {
					data.addBenchmarkNo(Integer.valueOf(attrs
							.getNamedItem("no").getNodeValue()));
				} else if (attrs.getNamedItem("no").getNodeValue()
						.matches("\\d+(, \\d+)+")) {
					data.setArray(true);
					String[] nos = attrs.getNamedItem("no")
						.getNodeValue().split(", ");
					for (String strNo : nos) {
						data.addBenchmarkNo(Integer.valueOf(strNo));
					}
				} else if (attrs.getNamedItem("no").getNodeValue()
						.matches("\\d+-\\d+")) {
					data.setArray(true);
					String[] range = attrs.getNamedItem("no")
							.getNodeValue().split("-");
					for (int i = Integer.valueOf(range[0]); i < Integer
							.valueOf(range[1]); i++) {
						data.addBenchmarkNo(i);
					}
				}
			} 
			if (attrs.getNamedItem("attr") != null) {
				data.setBenchmarkAttr(attrs.getNamedItem("attr")
						.getNodeValue());
			}
		}
		data.setBenchmark(node.getFirstChild().getNodeValue());
	}
	
	private void loadLastChildData(Node node, RetrievingData data){
		data.setDataType(node.getNodeName());
		NamedNodeMap attrs = node.getAttributes();
		data.setSearchPath(attrs.getNamedItem("path").getNodeValue()
				.toLowerCase());
		data.setSearchDataType(attrs.getNamedItem("type")
				.getNodeValue());
		if (attrs.getNamedItem("pattern") != null) {
			data.setDataPattern(attrs.getNamedItem("pattern")
					.getNodeValue());
		}
		if (attrs.getNamedItem("array") != null) {
			data.setArray(Boolean.valueOf(attrs.getNamedItem("array")
					.getNodeValue()));
			data.setSearchNextPath(attrs.getNamedItem("next")
					.getNodeValue().toLowerCase());
		}
		if (attrs.getNamedItem("terminator") != null) {
			data.setTerminatorNode(attrs.getNamedItem("terminator")
					.getNodeValue());
			data.setTerminatorNodeType(attrs.getNamedItem(
					"terminatorType").getNodeValue());
		}
		if (attrs.getNamedItem("terminatorPath") != null) {
			data.setTerminatorPath(attrs.getNamedItem("terminatorPath")
					.getNodeValue().toLowerCase());
		}
	}
	
	private String getColumnType(Node node){
		NamedNodeMap attrs = node.getAttributes();
		if(attrs.getNamedItem("type") != null){
			return attrs.getNamedItem("type").getNodeValue();
		}else{
			String columnType = getSqlType(node.getLastChild().getNodeName());
			
			//If argument is an array type
			if((node.getFirstChild().getAttributes().getNamedItem("array") != null && node.getFirstChild().getAttributes().getNamedItem("array").getNodeValue().equals("true")) 
					|| (node.getFirstChild().getAttributes().getNamedItem("no") != null && (node.getFirstChild().getAttributes().getNamedItem("no").getNodeValue().matches("\\d+-\\d+") || node.getFirstChild().getAttributes().getNamedItem("no").getNodeValue().matches("\\d+(, \\d+)+")))
					|| (node.getLastChild().getAttributes().getNamedItem("array") != null && node.getLastChild().getAttributes().getNamedItem("array").getNodeValue().equals("true"))){
				columnType += "[]";
			}
			return columnType;
		}
	}
	
	private String getSqlType(String javaType){
		if (javaType.toLowerCase().equals("bool") || javaType.toLowerCase().equals("boolean")){
			return "boolean";
		} 
		if (javaType.toLowerCase().equals("byte") || javaType.toLowerCase().equals("int") || javaType.toLowerCase().equals("integer") ||  javaType.toLowerCase().equals("short")){
			return "integer";
		}
		if (javaType.toLowerCase().equals("long")){
			return "bigint";
		}
		if (javaType.toLowerCase().equals("float")){
			return "real";
		}
		if (javaType.toLowerCase().equals("double")){
			return "double precision";
		}
		if (javaType.toLowerCase().equals("char") || javaType.toLowerCase().equals("character")){
			return "character(1)";
		}
		if (javaType.toLowerCase().equals("string")){
			return "text";
		}
		return null;
	}

	

	public String parse(String configFilePath) throws ParserConfigurationException, SAXException, IOException {
		Document dom = loadDom(configFilePath);
		return parse(dom);
	}

	public String parse(Document dom) {
		StringBuilder message = new StringBuilder();
		Node configNode = dom.getFirstChild();
		parseConfigNode(configNode, message);
		
		Node tableNode = configNode.getFirstChild();
		
		while (tableNode != null) {
			parseTableNode(tableNode, message);
			tableNode = tableNode.getNextSibling();
		}
		return message.toString();
	}
	
	private void parseConfigNode(Node configNode, StringBuilder message){
		if (!configNode.getNodeName().equals("config")){
			message.append("First element in configuration has to be node \'config\'\n");
		}else{
			if (configNode.hasAttributes()) {
				NamedNodeMap attrs = configNode.getAttributes();
				if (attrs.getLength() != 5 ){
					message.append("Only database data can be defined in node \'config\'\n");
				}
				for (int i = 0; i < attrs.getLength(); i++){
					if(!(attrs.item(i).getNodeName().equals("host") || attrs.item(i).getNodeName().equals("port") || attrs.item(i).getNodeName().equals("dbname") || attrs.item(i).getNodeName().equals("user") || attrs.item(i).getNodeName().equals("password"))){
						message.append("Attribute \'" + attrs.item(i) + "\' is wrong for node \'config\'\n");
					}
				}
			}
		}
	}
	
	private void parseTableNode(Node tableNode, StringBuilder message){
		if (tableNode.getAttributes().getLength() > 4) {
			message.append("Wrong number of attributes in node \'" + tableNode.getNodeName() + "\'\n");
		}else{
			NamedNodeMap attrs = tableNode.getAttributes();
			for (int i = 0; i<attrs.getLength(); i++){
				String attrName = attrs.item(i).getNodeName();
				if (!attrName.equals("overwrite") && !attrName.equals("addUrl") && !attrName.equals("constraints") && !attrName.equals("urlAsPrimaryKey")){
					message.append("Wrong name of attribute \'" + attrName + "\' in node \'" + tableNode.getNodeName() + "\'\n");
				}
			}
			Node overwrite;
			if ((overwrite = tableNode.getAttributes().getNamedItem("overwrite")) != null){
				if (!overwrite.getNodeValue().equals("true") && !overwrite.getNodeValue().equals("false")){
					message.append("Wrong value of attribute \'overwrite\' in node \'" + tableNode.getNodeName() + "\'\n");
				}
			}
			Node addUrl;
			if ((addUrl = tableNode.getAttributes().getNamedItem("addUrl")) != null){
				if (!addUrl.getNodeValue().equals("true") && !addUrl.getNodeValue().equals("false")){
					message.append("Wrong value of attribute \'addUrl\' in node \'" + tableNode.getNodeName() + "\'\n");
				}
			}
			Node urlAsPrimaryKey;
			if ((urlAsPrimaryKey = tableNode.getAttributes().getNamedItem("urlAsPrimaryKey")) != null){
				if (!urlAsPrimaryKey.getNodeValue().equals("true") && !urlAsPrimaryKey.getNodeValue().equals("false")){
					message.append("Wrong value of attribute \'urlAsPrimaryKey\' in node \'" + tableNode.getNodeName() + "\'\n");
				}
			}
			
			Set<String> databaseFields = new HashSet<String>();
			NodeList nodeList = tableNode.getChildNodes();
			for (int k = 0; k < nodeList.getLength(); k++) {
				if (databaseFields.contains(nodeList.item(k).getNodeName())) {
					message.append("Wrong database fields names. Field name \'"
							+ nodeList.item(k)
							+ "\' is used more then once\n");
				}else{
					databaseFields.add(nodeList.item(k).getNodeName());
				}
			}
		}
		parseColumnDataNodes(tableNode.getFirstChild(), message);
	}
	

	private void parseColumnDataNodes(Node node, StringBuilder message) {
		while (node != null) {
			if (node.getNodeName().equals("link")) {	//link do przejscia na podstrone
				parseLinkNode(node, message);
				parseColumnDataNodes(node.getFirstChild(), message);
			} else if (node.getChildNodes() != null		
					&& node.getChildNodes().getLength() != 2) {		//dla zwyklego wezla z danymi
					message.append("Wrong children amount for node \'"
							+ node.getNodeName() + "\'\n");
					node = node.getNextSibling();
			} else if (node.hasChildNodes()){
				NamedNodeMap attrs = node.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++){
					if (!attrs.item(i).getNodeName().equals("type") && !attrs.item(i).getNodeName().equals("constraints")){
						message.append("Attribute \'" + attrs.item(i).getNodeName() + "\' is not allowed in node " + node.getNodeName() + "\n");
					}
				}
				if (attrs.getNamedItem("type") != null){
					boolean isSqlType = false;
					String type = attrs.getNamedItem("type").getNodeValue();
					for (String sqlType: SQL_TYPES){
						if(type.toLowerCase().matches(sqlType + "(\\(\\d+\\))?")){
							isSqlType = true;
						}
					}
					if (!isSqlType){
						message.append("Wrong value for attribute \'type\' in node " + node.getNodeName() + "\n");
					}
				}
				node = node.getFirstChild();
				parseFirstChild(node, message);
				
				node = node.getNextSibling();
				parseSecondChild(node, message);
			}
			node = node.getParentNode().getNextSibling();
		}
	}
	
	private void parseFirstChild(Node node, StringBuilder message){
		if (NODE_TYPES.contains(node.getNodeName())) {
			NamedNodeMap attrs = node.getAttributes();
			if (node.getNodeName().equals("xpath")
					&& node.getAttributes().getLength() > 0) {
				message.append("Wrong attributes for node \'xpath\' in "
						+ node.getParentNode().getNodeName() + "\n");
			}
			if ((node.getNodeName().equals("text") || node
					.getNodeName().equals("comment"))
					&& attrs.getNamedItem("attr") != null) {
				message.append("Node \'" + node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName()
						+ "\' cannot have attribute \'attr\'\n");
			}
			if (attrs.getNamedItem("no") != null
					&& !(attrs.getNamedItem("no").getNodeValue()
							.matches("\\d+") || (((attrs.getNamedItem(
							"no").getNodeValue().matches("\\d+-\\d+")) || attrs
							.getNamedItem("no").getNodeValue()
							.matches("\\d+(, \\d+)+"))
					&& attrs.getNamedItem("array") != null
					&& attrs.getNamedItem("array").getNodeValue()
							.equals("false")))) {
				message.append("Wrong value of attribute \'no\' in node \'"
						+ node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName() + "\'\n");
			}
			if (attrs.getNamedItem("array") != null
					&& attrs.getNamedItem("array").getNodeValue().toLowerCase()
							.equals("true")){
				if(node.getNextSibling().getAttributes()
								.getNamedItem("array") != null
						&& node.getNextSibling().getAttributes()
								.getNamedItem("array").getNodeValue().toLowerCase()
								.equals("true")) {
					message.append("Wrong definition of array type in node \'"
							+ node.getParentNode().getNodeName() + "\'\n");
					message.append("Attribute array can appear only once in column definition\n");
				}else if(node.getNextSibling().getAttributes().getNamedItem("next") != null){
					message.append("Wrong definition of array type in node \'"
							+ node.getParentNode().getNodeName() + "\'\n");
					message.append("Attribute array in first child is not working with \'next\' attribute in second child\n");
				}
			}
			for (int i = 0; i < attrs.getLength(); i++) {
				if (!(attrs.item(i).getNodeName().equals("no") || attrs
						.item(i).getNodeName().equals("attr") || attrs.item(i).getNodeName().equals("array"))) {
					message.append("Wrong attribute \'"
							+ attrs.item(i).getNodeName()
							+ "\' in node \'" + node.getNodeName()
							+ "\' of \'"
							+ node.getParentNode().getNodeName()
							+ "\'\n");
				}
			}
			if (node.getChildNodes().getLength() == 1) {
				if (node.getFirstChild().getNodeType() != Node.TEXT_NODE) {
					message.append("Wrong benchmark type in node \'"
							+ node.getParentNode().getNodeName()
							+ "\'\n");
				}
			} else if (node.getChildNodes().getLength() > 1) {
				message.append("Wrong benchmark in node \'"
						+ node.getParentNode().getNodeName()
						+ "\'. Only one benchmark element is allowed\n");
			}
		} else {
			message.append("Wrong node type: \'" + node.getNodeName()
					+ "\' of \'" + node.getParentNode().getNodeName()
					+ "\'\n");
		}
	}
	
	private void parseSecondChild(Node node, StringBuilder message){
		if (JAVA_TYPES.contains(node.getNodeName())) {
			NamedNodeMap attrs = node.getAttributes();
			if (attrs.getNamedItem("path") != null
					&& attrs.getNamedItem("type") != null) {
				if (!attrs.getNamedItem("path").getNodeValue()
						.toLowerCase().matches(PATH_REGEX)) {
					message.append("Wrongly constructed attribute \'path\' in node \'"
							+ node.getNodeName()
							+ "\' of \'"
							+ node.getParentNode().getNodeName()
							+ "\'\n");
				}
				if (!attrs.getNamedItem("path").getNodeValue()
						.toLowerCase().endsWith("attr")
						&& !attrs.getNamedItem("type").getNodeValue()
								.equals("text")
						&& !attrs.getNamedItem("type").getNodeValue()
								.equals("value")
						&& !attrs.getNamedItem("type").getNodeValue()
								.equals("name")) {
					message.append("Wrong value of attribute \'type\' in node \'"
							+ node.getNodeName()
							+ "\' of \'"
							+ node.getParentNode().getNodeName()
							+ "\'\n");
				}

				if (attrs.getNamedItem("terminator") != null) {
					if (attrs.getNamedItem("terminatorType") == null) {
						message.append("No attribute \'terminatorType\' in node \'"
								+ node.getNodeName()
								+ "\' of \'"
								+ node.getParentNode().getNodeName()
								+ "\'\n");
					}
					if (attrs.getNamedItem("terminatorPath") != null) {
						if (!attrs.getNamedItem("terminatorPath")
								.getNodeValue().toLowerCase()
								.matches(PATH_REGEX)) {
							message.append("Wrongly constructed attribute \'terminatorPath\' in node \'"
									+ node.getNodeName()
									+ "\' of \'"
									+ node.getParentNode()
											.getNodeName() + "\'\n");
						}
						if (((!attrs.getNamedItem("terminatorPath")
								.getNodeValue().toLowerCase()
								.endsWith("attr")) || !attrs
								.getNamedItem("path").getNodeValue()
								.endsWith("attr"))
								&& !attrs
										.getNamedItem("terminatorType")
										.getNodeValue().equals("text")
								&& !attrs
										.getNamedItem("terminatorType")
										.getNodeValue().equals("value")
								&& !attrs
										.getNamedItem("terminatorType")
										.getNodeValue().equals("name")
								&& !attrs
										.getNamedItem("terminatorType")
										.getNodeValue().equals("null")) {
							message.append("Wrong value of attribute \'terminatorType\' in node \'\n"
									+ node.getNodeName()
									+ "\' of \'"
									+ node.getParentNode()
											.getNodeName() + "\'\n");
						}
					}

				} else {
					if (node.getAttributes().getNamedItem(
							"terminatorType") != null
							|| node.getAttributes().getNamedItem(
									"terminatorPath") != null) {
						message.append("Wrong attributes in node "
								+ node.getNodeName() + "\' of \'"
								+ node.getParentNode().getNodeName()
								+ "\'\n");
						message.append("	Attributes \'terminatorType\' and \'terminatorPath\' cannot exist when attribute \'terminator\' does not exist\n");
					}
				}
				if (attrs.getNamedItem("array") != null) {
					if (attrs.getNamedItem("array").getNodeValue()
							.equals("true")
							|| attrs.getNamedItem("array")
									.getNodeValue().equals("false")) {
						if (attrs.getNamedItem("next") != null) {
							if (!attrs.getNamedItem("next")
									.getNodeValue().toLowerCase()
									.matches(PATH_REGEX)) {
								message.append("Wrongly constructed attribute \'next\' in node \'"
										+ node.getNodeName()
										+ "\' of \'"
										+ node.getParentNode()
												.getNodeName() + "\'\n");
							}
						} else {
							message.append("Missing attribute \'next\' in node "
									+ node.getNodeName()
									+ "\' of \'"
									+ node.getParentNode()
											.getNodeName() + "\'\n");

						}
					} else {
						message.append("Wrong value of attribute \'array\' in node "
								+ node.getNodeName()
								+ "\' of \'"
								+ node.getParentNode().getNodeName()
								+ "\'\n");
					}
				}
				if (attrs.getNamedItem("pattern") != null
						&& !attrs.getNamedItem("pattern")
								.getNodeValue()
								.contains(node.getNodeName())) {
					message.append("Wrongly constructed attribute terminatorPath in node \'"
							+ node.getNodeName()
							+ "\' of \'"
							+ node.getParentNode().getNodeName()
							+ "\'\n");
				}
				List<String> allowedAttrs = Arrays
						.asList("array,next,path,pattern,terminator,terminatorPath,terminatorType,type"
								.split(","));
				for (int i = 0; i < attrs.getLength(); i++) {
					if (!allowedAttrs.contains(attrs.item(i)
							.getNodeName())) {
						message.append("Wrong attribute \'"
								+ attrs.item(i).getNodeName()
								+ "\' in node \'" + node.getNodeName()
								+ "\' of \'"
								+ node.getParentNode().getNodeName()
								+ "\'\n");
					}
				}
			} else {
				message.append("Missing attributes path and type in node \'"
						+ node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName() + "\'\n");
			}
			if (node.getFirstChild() != null) {
				message.append("Node \'" + node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName()
						+ "\' cannot have any child\n");
			}
		} else {
			message.append("Wrong node type: \'" + node.getNodeName()
					+ "\' in node \'"
					+ node.getParentNode().getNodeName() + "\'\n");

		}
	}
}
