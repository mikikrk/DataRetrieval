package pl.edu.agh.kis.dataretrieval.flow.webprocessors;

import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;

public class RetrievalHelper {
	/**
	 * Klasa zwracajaca wynik przejscia zdefiniowanej w konfiguracji (koncowy wezel moze byc atrybutem, albo wezlem)
	 * @author Miko³aj Nowak
	 *
	 */
	public static class NodeAttrEntry{	
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
	
	public static String getXpathExpression(FindNodeData configNode){
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
	
	public static NodeAttrEntry realizePath(List<String> path, Node startNode) throws DOMException, RetrievalException{
		NamedNodeMap attrs = null;
		Node node = startNode;
		try{
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
		}catch (RuntimeException e){
			throw new RetrievalException("Wrong path: " + path + " to realize for " + startNode.getNodeName() + ":" + startNode.getNodeValue());
		}
	}
	
	public static boolean checkTermination(Node node, FindNodeData configNode) throws DOMException, RetrievalException{
		NodeAttrEntry entry = null;
		try{
			if(configNode.getTerminatorPath() != null){
				entry = realizePath(configNode.getSearchTerminatorNodes(), node);
			}else {
				entry = realizePath(configNode.getSearchNextNodes(), node);
			}
		}catch (NullPointerException e){
			return true;
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
	
	public static String popDataFromNode(String dataType, Node node){
		if (dataType.equals("name")){
			return node.getNodeName();
		}else{
			return node.getNodeValue();
		}
	}
}
