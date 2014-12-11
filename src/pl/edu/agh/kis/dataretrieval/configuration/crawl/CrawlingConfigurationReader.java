package pl.edu.agh.kis.dataretrieval.configuration.crawl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;
import pl.edu.agh.kis.dataretrieval.database.DatabaseData;

/**
 * Klasa zajmująca się parsowaniem i wczytywaniem pliku konfiguracyjnego 
 * @author Mikołaj Nowak
 *
 */
public class CrawlingConfigurationReader extends ConfigurationReader{
	private ArrayList<CrawlingData> configNodes;
	
	/**
	 * Wczytanie pliku konfiguracyjnego
	 * @param configFilePath - sciezka do pliku 
	 * @return lista przetworzonych wezlow odpowiadajacych poszczegolnym danym do wyciagniecia ze strony www
	 */
	public List<CrawlingData> load(String configFilePath) {
		configNodes = new ArrayList<CrawlingData>();
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);

		parse(dom);
//			throw new RuntimeException(e);	//TODO zostanie zmienione na odpowiednie wypisanie do gui
		
		Node tableNode = dom.getFirstChild();
		
		while (tableNode != null) {	
			String dbTableName = tableNode.getNodeName();
			Node node = tableNode.getFirstChild();
			while (node != null) {
				configNodes.add(loadConfigNode(node, dbTableName));
				node = node.getNextSibling();
			}
			tableNode = tableNode.getNextSibling();
		}
		return configNodes;
	}
	
	/**
	 * Wczytywanie danych do laczenia z baza danych
	 * @param configFilePath
	 * @return
	 */
	public DatabaseData loadDbData(String configFilePath) {
		DatabaseData dbData = new DatabaseData();
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);
		
		Node tableNode = dom.getFirstChild();
		NamedNodeMap attrs = tableNode.getAttributes();
		if (attrs.getNamedItem("host") != null){
			dbData.setHost(attrs.getNamedItem("host").getNodeValue());
		}else{
			dbData.setHost("localhost");
		}
		if (attrs.getNamedItem("port") != null){
			dbData.setPort(Integer.valueOf(attrs.getNamedItem("port").getNodeValue()));
		}else{
			dbData.setPort(5432);
		}
		if (attrs.getNamedItem("dbname") != null){
			dbData.setDbname(attrs.getNamedItem("dbname").getNodeValue());
		}else{
			dbData.setDbname("postgres");
		}
		if (attrs.getNamedItem("user") != null){
			dbData.setUser(attrs.getNamedItem("user").getNodeValue());
		}else{
			dbData.setUser("postgres");
		}
		if (attrs.getNamedItem("password") != null){
			dbData.setPassword(attrs.getNamedItem("password").getNodeValue());
		}else{
			dbData.setPassword("");
		}
		return dbData;
	}
	
	private CrawlingData loadConfigNode(Node node, String dbTableName){
		CrawlingData data = new CrawlingData();
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
				data.addUnderLink(loadConfigNode(linkNode, dbTableName));
				linkNode = linkNode.getNextSibling();
			}
		}else{
			data.setDbColName(node.getNodeName());
			data.setDbColType(getColumnType(node));
			data.setDbConstraints(node.getAttributes().getNamedItem("constraints")!=null ? node.getAttributes().getNamedItem("constraints").getNodeValue() : "");
			
			node = node.getFirstChild();
			loadFirstChildData(node, data);
	
			node = node.getNextSibling();
			loadLastChildData(node, data);
			
			node = node.getParentNode().getNextSibling();
		}
		return data;
	}
	
	private void loadFirstChildData(Node node, CrawlingData data){
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
	
	private void loadLastChildData(Node node, CrawlingData data){
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

	

	public String parse(String configFilePath) {
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);
		return parse(dom);
	}

	public String parse(Document dom) {
		Node tableNode = dom.getFirstChild();
		StringBuilder message = new StringBuilder();
		if (tableNode.hasAttributes()) {
			NamedNodeMap attrs = tableNode.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++){
				if(!(attrs.item(i).getNodeName().equals("host") || attrs.item(i).getNodeName().equals("port") || attrs.item(i).getNodeName().equals("dbname") || attrs.item(i).getNodeName().equals("user") || attrs.item(i).getNodeName().equals("password"))){
					message.append("Attribute \'" + attrs.item(i) + "\' is wrong for node \'" + tableNode.getNodeName() + "\'\n");
				}
			}
		}
		Node firstTableNode = tableNode;
		while (tableNode != null) {
			if (tableNode != firstTableNode && tableNode.hasAttributes()) {
				NamedNodeMap attrs = tableNode.getAttributes();
				if(attrs.getLength() > 0){
					message.append("Only the first table node can contain database attributes\n");
				}
			}
			
			Set<String> databaseFields = new HashSet<String>();
			NodeList nodeList = tableNode.getChildNodes();
			for (int k = 0; k < nodeList.getLength(); k++) {
				if (databaseFields.contains(nodeList.item(k).getNodeName())) {
					message.append("Wrong database fields names. Field name \'"
							+ nodeList.item(k)
							+ "\' is used more then one time\n");
				}else{
					databaseFields.add(nodeList.item(k).getNodeName());
				}
			}

			parseColumnDataNodes(tableNode.getFirstChild(), message);
			tableNode = tableNode.getNextSibling();
		}
		return message.toString();
	}

	private void parseColumnDataNodes(Node node, StringBuilder message) {
		while (node != null) {
			if (node.getNodeName().equals("link")) {	//link do przejscia na podstrone
				parseLinkNode(node, message);
				parseColumnDataNodes(node.getFirstChild(), message);
			} else if (node.getChildNodes() == null		
					|| node.getChildNodes().getLength() != 2) {		//dla zwyklego wezla z danymi

//				if(node.getChildNodes() == null) { // TODO: obsluga dla wezlow definiujacych tylko pola bazodanowe
				message.append("Wrong children amount for node \'"
						+ node.getNodeName() + "\'\n");
				node = node.getNextSibling();
				// message.append("Node " + node.getNodeName() +
				// " has to have exactly 2 children: \n");
				// message.append("	1. Searching benchmark (<benchmark_node_type [no=\"same_benchmark_pattern_no\"]>benchmark_text</benchmark_note_type>");
				// message.append("	2. Searching info <java_data_type path=\"path_of_dom_nodes_to_searched_element\" type=\"search_node_type\" [pattern=\"pattern_to_pull_out_element_from_complex_text(\"array=\"true_for_array_data\" ");
			} else {
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
						if(type.toLowerCase().matches(sqlType + "\\(\\d+\\)")){
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