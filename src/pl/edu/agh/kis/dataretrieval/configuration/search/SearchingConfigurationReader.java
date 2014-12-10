package pl.edu.agh.kis.dataretrieval.configuration.search;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;

public class SearchingConfigurationReader extends ConfigurationReader{
	public void load(String cofigFilePath){
		
	}
	
	public void parse(String configFilePath){
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);
		parse(dom);
	}
	
	public void parse(Document dom){
		Node urlNode = dom.getFirstChild();
		StringBuilder message = new StringBuilder();
		
		if (urlNode.getNodeName().equalsIgnoreCase("url")){
			message.append("Wrong name for first node");
		}
		if (urlNode.getAttributes().getLength() != 1 || urlNode.getAttributes().getNamedItem("address") == null){
			message.append("Wrong parameters for node url");
		}
		
		Node node = urlNode.getFirstChild();
		while (node != null){
			if (node.getNodeName().equals("link")){
				parseLinkNode(node, message);
			}else if(node.getNodeName().equals("form")){
				parseFormNode(node, message);
			}else {
				message.append("Wrong node name of\'" + node.getNodeName() + "\'");
			}
			node = node.getNextSibling();
		}
	}
	
	private void parseLinkNode(Node node, StringBuilder message){
		if (node.getAttributes().getLength()!=1 || node.getAttributes().getNamedItem("type") == null){
			message.append("Wrong attributes for node \'link\'");
		}else{
			Node type = node.getAttributes().getNamedItem("type");
			if (!(type.getNodeValue().equals("text") || type.getNodeValue().equals("img"))){
				message.append("Wrong value of attribute \'type\' in link node");
			}
		}
		if (node.getChildNodes().getLength() != 1){
			message.append("Wrong number of child nodes in node \'link\'");
		}else if (node.getFirstChild().getNodeName().equalsIgnoreCase("#text")){
			message.append("Wrong child type in node \'link\'");
		}
	}
	
	private void parseFormNode(Node node, StringBuilder message){
		NamedNodeMap attrs = node.getAttributes();
		
		if(!(attrs.getNamedItem("name") != null || attrs.getNamedItem("id") != null || attrs.getNamedItem("no") != null)){
			message.append("Attribute \'name\', \'id\' or \'no\' is neccessary in node \'form\'");
		}else{
			for (int i=0; i<attrs.getLength(); i++){
				if(!attrs.item(i).getNodeName().equals("name") && !attrs.item(i).getNodeName().equals("id") && !attrs.item(i).getNodeName().equals("no")){
					message.append("Wrong attribute \'"+ attrs.item(i).getNodeName() + "\' in node \'form\'");
				}
			}
		}
		if(attrs.getNamedItem("no") != null && !attrs.getNamedItem("no").getNodeValue().matches("\\d+")){
			message.append("Wrong value of attribute \'no\' in node \'form\'");
		}
		
		Node fieldNode = node.getFirstChild();
		while(fieldNode != null){
			for (int i=0; i<fieldNode..getLength(); i++){
				if(!attrs.item(i).getNodeName().equals("name") && !attrs.item(i).getNodeName().equals("id") && !attrs.item(i).getNodeName().equals("no")){
					message.append("Wrong attribute \'"+ attrs.item(i).getNodeName() + "\' in node \'form\'");
				}
			}
		}
	}
}
