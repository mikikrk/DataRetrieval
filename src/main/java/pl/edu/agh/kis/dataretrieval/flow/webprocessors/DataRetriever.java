package pl.edu.agh.kis.dataretrieval.flow.webprocessors;

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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;
import pl.edu.agh.kis.dataretrieval.database.DbFieldData;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions.NotAllFieldsRetrievedException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

/**
 * Sciaganie danych ze strony internetowej na podstawie otrzymanej konfiguracji
 * @author Mikołaj Nowak
 *
 */
public class DataRetriever{
	
	/**
	 * Pobranie danych z podanego dokumentu na podstawie podanej konfiguracji
	 * @param dom dokument DOM zawierajacy strone, z ktorej maja byc pobrane dane
	 * @param url adres url strony, z ktorej maja byc pobrane dane
	 * @param wc WebConversation umozliwiajacy przechodzenie do podstron 
	 * @param configNodes wczytana konfiguracja
	 * @return
	 * @throws RetrievalException 
	 * @throws DOMException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws XPathExpressionException 
	 */
	public static Map<String, List<DbFieldData>> readData(WebResponse response, List<RetrievingData> configNodes) throws DOMException, RetrievalException, SAXException, XPathExpressionException, IOException{
		Map<String, List<DbFieldData>> siteData = new HashMap<String, List<DbFieldData>>();
		return readData(response, configNodes, siteData);
	}
	
	private static Map<String, List<DbFieldData>> readData(WebResponse response, List<RetrievingData> configNodes, Map<String, List<DbFieldData>> siteData) throws NotAllFieldsRetrievedException, SAXException{
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Document dom = response.getDOM();
		String url = response.getURL().toString();
		
		String errorsMessages = new String();
		for (RetrievingData configNode: configNodes){
			try{
				if (siteData.get(configNode.getDbTableName()) == null){
					siteData.put(configNode.getDbTableName(), new ArrayList<DbFieldData>());
					if (configNode.isDbAddUrl() || configNode.isDbUrlAsPrimaryKey()){
						siteData.get(configNode.getDbTableName()).add(new DbFieldData("url", "TEXT", false, configNode.isDbUrlAsPrimaryKey() ? "PRIMARY KEY" : "", "", url));
					}
				}
				if(configNode.getDataType().equals("link")){
					LinkProcessor linkProcessor = new LinkProcessor();
					WebResponse resp = linkProcessor.goTo(response, configNode);
					readData(resp, configNode.getUnderLink(), siteData);
				}else{
					String expression = RetrievalHelper.getXpathExpression(configNode);
					
					NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(dom, XPathConstants.NODESET);
					Node node;
					List<Object> dataList = new ArrayList<Object>(); 
					
					if (configNode.isArray() && configNode.getSearchNextPath() == null){	//gdy parametr 'array' jest podany w pierwszym wezle - kolejne elementy tablicy sa kolejnymi elementami znalezionymi przez sciezke xpath
						for (int i = 0; i<nodeList.getLength(); i++){
							node = nodeList.item(i);
							dataList.add(goThroughPath(configNode, node));
						}
						siteData.get(configNode.getDbTableName()).add(new DbFieldData(configNode, dataList));
					}else if(configNode.getBenchmarkNo().size() > 1){
						for (int i: configNode.getBenchmarkNo()){
							node = nodeList.item(i);
							dataList.add(goThroughPath(configNode, node));
						}
						siteData.get(configNode.getDbTableName()).add(new DbFieldData(configNode, dataList));
					}else {
						if (!configNode.getBenchmarkNo().isEmpty()){
							node = nodeList.item(configNode.getBenchmarkNo().get(0)-1); 
						}else{
							node = nodeList.item(0);
						}
						siteData.get(configNode.getDbTableName()).add(new DbFieldData(configNode, goThroughPath(configNode, node)));
					}
				}
			}catch(Exception e){
				errorsMessages += e.getMessage() + "\n";
			}
		}
		response.close();
		if (!errorsMessages.isEmpty()){
			throw new NotAllFieldsRetrievedException(errorsMessages, siteData);
		}
		return siteData;
	}
	
	private static Object goThroughPath(RetrievingData configNode, Node node) throws DOMException, RetrievalException{
		if (node != null){
			RetrievalHelper.NodeAttrEntry entry = RetrievalHelper.realizePath(configNode.getSearchPathNodes(), node);
			NamedNodeMap attrs = entry.getAttrs();	//jest zwracane gdy sciezka konczy sie 'attr'
			node = entry.getNode();	
			return getPathRecord(attrs, node, configNode);
		}else{
			throw new RetrievalException("Benchmark \'" + configNode.getBenchmark() + "\' was not found");
		}
	}
	
	private static Object getPathRecord(NamedNodeMap attrs, Node node, RetrievingData configNode) throws DOMException, RetrievalException{
		RetrievalHelper.NodeAttrEntry entry;
		if (!(configNode.isArray() && configNode.getSearchNextPath() != null)){
			return getData(attrs, node, configNode);
		}else{
			List<Object> dataList = new ArrayList<Object>(); 
			boolean stopCondition;
			do{
				dataList.add(getData(attrs, node, configNode));
				stopCondition = RetrievalHelper.checkTermination(node, configNode);
				if (!stopCondition){
					entry = RetrievalHelper.realizePath(configNode.getSearchNextNodes(), node);
					attrs = entry.getAttrs();
					node = entry.getNode();
				}
			}while(!stopCondition);
			return dataList;
		}
	}
	
	private static Object getData(NamedNodeMap attrs, Node node, RetrievingData configNode){
		if (attrs == null){		//if searched data is kept in attribute
			String data = RetrievalHelper.popDataFromNode(configNode.getDataType(), node);

			return getSearchedObject(data, configNode.getDataType(), configNode.getDataPattern());
		}else{
			return getSearchedObject(attrs.getNamedItem(configNode.getSearchDataType()).getNodeValue(), configNode.getDataType(), configNode.getDataPattern());
		}
	}
	
	private static Object getSearchedObject(String data, String dataType, String pattern){
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
			return data.equals("") ? null : Boolean.valueOf(data);
		}else if(dataType.equals("byte")){
			return data.equals("") ? 0 : Byte.valueOf(data);
		}else if (dataType.equals("int") || dataType.equals("integer")){ 
			return data.equals("") ? 0 : Integer.valueOf(data);
		}else if (dataType.equals("long")){
			return data.equals("") ? 0l : Long.valueOf(data);
		}else if (dataType.equals("short")){
			return data.equals("") ? 0 : Short.valueOf(data);
		}else if(dataType.equals("char") || dataType.equals("character")){
			return data.equals("") ? null : data.charAt(0);
		}else if(dataType.equals("float")){
			return data.equals("") ? 0f : Float.valueOf(data);
		}else if(dataType.equals("double")){
			return data.equals("") ? 0d : Double.valueOf(data);
		}else {
			return data;
		}
	}
	
	private static String getRegexForType(String type){
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
	
	private static String popDataString(String dataField, List<String> regexes){
		String data = "";
		if (regexes.size() == 3){
			String trimer = dataField;
			int i = trimer.length();
			if (!(regexes.get(1).equals("") || regexes.get(1) == null || regexes.get(1).equals(".*"))){
				while (trimer.matches(regexes.get(0)+regexes.get(2)+regexes.get(1)) && i>0){
					trimer = trimer.substring(0, --i);
				}
				while (!trimer.matches(regexes.get(0)+regexes.get(2)) && i>0){
					trimer = trimer.substring(0, --i);
				}
			}
			while (trimer.matches(regexes.get(0)+regexes.get(2)) && i>0){
				data = trimer.charAt(--i) + data;
				trimer = trimer.substring(0, i);
			}
		}
		return data;
	}
}
