package pl.edu.agh.kis.dataretrieval.journal.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.database.FieldData;
import pl.edu.agh.kis.dataretrieval.journal.configuration.NodeData;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

/**
 * Sciaganie danych ze strony internetowej na podstawie otrzymanej konfiguracji
 * @author Mikołaj Nowak
 *
 */
public class JournalCrawler{

	private class NodeAttrEntry{	//Klasa zwracajaca wynik przejscia zdefiniowanej w konfiguracji (koncowy wezel moze byc atrybutem, albo wezlem)
		private Node node;
		private NamedNodeMap attrs;
		public NodeAttrEntry(Node node, NamedNodeMap attrs) {
			super();
			this.node = node;
			this.attrs = attrs;
		}
		public Node getNode() {
			return node;
		}
		public NamedNodeMap getAttrs() {
			return attrs;
		}
	}
	
	/**
	 * Pobranie danych z podanego dokumentu na podstawie podanej konfiguracji
	 * @param dom dokument DOM zawierajacy strone, z ktorej maja byc pobrane dane
	 * @param url adres url strony, z ktorej maja byc pobrane dane
	 * @param wc WebConversation umozliwiajacy przechodzenie do podstron 
	 * @param configNodes wczytana konfiguracja
	 * @return
	 */
	public Map<String, List<FieldData>> readData(Document dom, String url, WebConversation wc, List<NodeData> configNodes){
		Map<String, List<FieldData>> siteData = new HashMap<String, List<FieldData>>();
		return readData(dom, url, wc, configNodes, siteData);
	}
	
	private Map<String, List<FieldData>> readData(Document dom, String url, WebConversation wc, List<NodeData> configNodes, Map<String, List<FieldData>> siteData){
		XPath xPath =  XPathFactory.newInstance().newXPath();

		try {
			for (NodeData configNode: configNodes){
				if (siteData.get(configNode.getDbTableName()) == null){
					siteData.put(configNode.getDbTableName(), new ArrayList<FieldData>());
					siteData.get(configNode.getDbTableName()).add(new FieldData("url", "TEXT", "", "String", false, url));
				}
				if(configNode.getBenchmark().equals("link")){
					WebResponse resp = null;
					if(configNode.getBenchmarkType().equals("img")){
						resp = wc.getCurrentPage();
						WebLink link = resp.getLinkWithImageText(configNode.getBenchmarkAttr());
						link.click();
						resp = wc.getCurrentPage();
					}else{
//						TODO: Other link types
					}
					readData(resp.getDOM(), url, wc, configNode.getUnderLink(), siteData);
				}else{
					String expression = getXpathExpression(configNode);
					
					NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(dom, XPathConstants.NODESET);
					Node node;
					List<Object> dataList = new ArrayList<Object>(); 
					
					if (configNode.isArray() && configNode.getSearchNextPath() == null){	//gdy parametr 'array' jest podany w pierwszym wezle - kolejne elementy tablicy sa kolejnymi elementami znalezionymi przez sciezke xpath
						for (int i = 0; i<nodeList.getLength(); i++){
							node = nodeList.item(i);
							dataList.add(goThroughPath(configNode, node));
						}
						siteData.get(configNode.getDbTableName()).add(new FieldData(configNode, dataList));
					}else if(configNode.getBenchmarkNo().size() > 1){
						for (int i: configNode.getBenchmarkNo()){
							node = nodeList.item(i);
							dataList.add(goThroughPath(configNode, node));
						}
						siteData.get(configNode.getDbTableName()).add(new FieldData(configNode, dataList));
					}else {
						if (!configNode.getBenchmarkNo().isEmpty()){
							node = nodeList.item(configNode.getBenchmarkNo().get(0)-1); 
						}else{
							node = nodeList.item(0);
						}
						siteData.get(configNode.getDbTableName()).add(new FieldData(configNode, goThroughPath(configNode, node)));
					}
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return siteData;
	}
	
	private String getXpathExpression(NodeData configNode){
		String expression;
		if (configNode.getBenchmarkType().equals("text")){
			expression = "//*[contains(text(),\'" + configNode.getBenchmark() + "\')]";
		}else if(configNode.getBenchmarkType().equals("xpath")){
			expression = configNode.getBenchmark();					
		}else{
			expression = "//" + configNode.getBenchmarkType().toUpperCase() + (configNode.getBenchmarkAttr() != null ? "[@" + configNode.getBenchmarkAttr() + "=\'" + configNode.getBenchmark() + "\']" : ""); 
		}
		return expression;
	}
	
	private Object goThroughPath(NodeData configNode, Node node){
		
		NodeAttrEntry entry = realizePath(configNode.getSearchPathNodes(), node);
		NamedNodeMap attrs = entry.getAttrs();	//jest zwracane gdy sciezka konczy sie 'attr'
		node = entry.getNode();	
		return getPathRecord(attrs, node, configNode);
	}
	
	private NodeAttrEntry realizePath(List<String> path, Node node){
		NamedNodeMap attrs = null;
		for (String nextNode: path){
			do{
				if (nextNode.equals("parent")){
					node = node.getParentNode();
				} else if(nextNode.equals("sibling") || nextNode.equals("nextsibling")){
//					odkomentowanie komentarzy pozwala na ignorowanie komentarzy na stronie
	//				do{
						node = node.getNextSibling();
	//				}while(node.getNodeType() == Node.COMMENT_NODE);
				} else if(nextNode.equals("prevsibling")){
	//				do{
						node = node.getPreviousSibling();
	//				}while(node.getNodeType() == Node.COMMENT_NODE);
				} else if(nextNode.equals("child") || nextNode.equals("firstchild")){
					//w przypadku pustego wezla, brane jest pierwsze nie puste dziecko
					if (!node.getNodeName().equals("#text") || (node.getNodeName().equals("#text") && !node.getNodeValue().trim().isEmpty())){ 
						node = node.getFirstChild();
					}else{
						node = node.getNextSibling();
					}
	//				while(node.getNodeType() == Node.COMMENT_NODE){
	//					node = node.getNextSibling();
	//				}
				} else if(nextNode.equals("lastchild")){
					if (!node.getNodeName().equals("#text") || (node.getNodeName().equals("#text") && !node.getNodeValue().trim().isEmpty())){ 
						node = node.getLastChild();
					}else{
						node = node.getPreviousSibling();
					}
	//				while(node.getNodeType() == Node.COMMENT_NODE){
	//					node = node.getPreviousSibling();
	//				}
				} else if(nextNode.equals("this")){
	//				do nothing
				}else{
					attrs = node.getAttributes();
				}
			}while((node != null && node.getNodeName().equals("#text") && node.getNodeValue().trim().isEmpty()));
		}
		return new NodeAttrEntry(node, attrs);
	}
	
	private boolean checkTermination(Node node, NodeData configNode){
		NodeAttrEntry entry = null;
		if(configNode.getTerminatorPath() != null){
			entry = realizePath(configNode.getSearchTerminatorNodes(), node);
		}else {
			entry = realizePath(configNode.getSearchNextNodes(), node);
		}
		NamedNodeMap attrs = entry.getAttrs();
		node = entry.getNode();
		if(configNode.getTerminatorNodeType() == null || configNode.getTerminatorNodeType().equals("null")){
			return node == null;
		}else if(attrs == null){
			String terminator = popDataFromNode(configNode.getTerminatorNodeType(), node);
			return terminator.toLowerCase().equals(configNode.getTerminatorNode().toLowerCase());
		}else {
			return configNode.getTerminatorNode().toLowerCase().equals(attrs.getNamedItem(configNode.getTerminatorNodeType()).getNodeValue().toLowerCase());
		}
	}
	
	private String popDataFromNode(String dataType, Node node){
		if (dataType.equals("name")){
			return node.getNodeName();
		}else{
			return node.getNodeValue();
		}
	}
	
	private Object getPathRecord(NamedNodeMap attrs, Node node, NodeData configNode){
		NodeAttrEntry entry;
		if (!(configNode.isArray() && configNode.getSearchNextPath() != null)){
			return getData(attrs, node, configNode);
		}else{
			List<Object> dataList = new ArrayList<Object>(); 
			boolean stopCondition;
			do{
				dataList.add(getData(attrs, node, configNode));
				stopCondition = checkTermination(node, configNode);
				if (!stopCondition){
					entry = realizePath(configNode.getSearchNextNodes(), node);
					attrs = entry.getAttrs();
					node = entry.getNode();
				}
			}while(!stopCondition);
			return dataList;
		}
	}
	
	private Object getData(NamedNodeMap attrs, Node node, NodeData configNode){
		if (attrs == null){		//if searched data is kept in attribute
			String data = popDataFromNode(configNode.getDataType(), node);

			return getSearchedObject(data, configNode.getDataType(), configNode.getDataPattern());
		}else{
			return getSearchedObject(attrs.getNamedItem(configNode.getSearchDataType()).getNodeValue(), configNode.getDataType(), configNode.getDataPattern());
		}
	}
	
	private Object getSearchedObject(String data, String dataType, String pattern){
		data = data.replaceAll("\u00a0", "");
		data = data.trim();
		if (pattern != null){
			ArrayList<String> regexes = new ArrayList<String>(Arrays.asList(pattern.split(dataType)));
			if (regexes.size() == 1){
				if (pattern.indexOf(regexes.get(0)) < pattern.indexOf(dataType)){
					regexes.add(1, "");
				}else{
					regexes.add(0, "");
				}
			}
			regexes.add(getRegexForType(dataType));
			data = popDataString(data, regexes);
		}
		if (dataType.equals("bool") || dataType.equals("boolean")){
			return Boolean.valueOf(data);
		}else if(dataType.equals("byte")){
			return Byte.valueOf(data);
		}else if (dataType.equals("int") || dataType.equals("integer")){ 
			return Integer.valueOf(data);
		}else if (dataType.equals("long")){
			return Long.valueOf(data);
		}else if (dataType.equals("short")){
			return Short.valueOf(data);
		}else if(dataType.equals("char") || dataType.equals("character")){
			return data.charAt(0);
		}else if(dataType.equals("float")){
			return Float.valueOf(data);
		}else if(dataType.equals("double")){
			return Double.valueOf(data);
		}else {
			return data;
		}
	}
	
	private String getRegexForType(String type){
		if (type.equals("bool") || type.equals("boolean")){
			return "(true|false)";
		}else if(type.equals("byte") || type.equals("int") || type.equals("integer") || type.equals("long") || type.equals("short")){
			return "\\d+";
		}else if(type.equals("char") || type.equals("character")){
			return ".";
		}else if(type.equals("float") || type.equals("double")){
			return "\\d+((\\.)\\d+)?";
		}else {
			return ".+";
		}
	}
	
	private String popDataString(String dataField, List<String> regexes){
		String data = "";
		if (regexes.size() == 3){
			String trimer = dataField;
			int i = trimer.length();
			if (!(regexes.get(1).equals("") || regexes.get(1) == null || regexes.get(1).equals(".*"))){
				while (trimer.matches(regexes.get(0)+regexes.get(2)+regexes.get(1)) && i>=0){
					trimer = trimer.substring(0, --i);
				}
				while (!trimer.matches(regexes.get(0)+regexes.get(2)) && i>=0){
					trimer = trimer.substring(0, --i);
				}
			}
			while (trimer.matches(regexes.get(0)+regexes.get(2)) && i>=0){
				data = trimer.charAt(--i) + data;
				trimer = trimer.substring(0, i);
			}
		}
		return data;
	}
}
