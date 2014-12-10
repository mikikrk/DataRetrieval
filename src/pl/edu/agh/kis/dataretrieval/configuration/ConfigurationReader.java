package pl.edu.agh.kis.dataretrieval.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigurationReader {
	
	protected Document loadDom(String configFilePath) {
		try {
			FileInputStream file = new FileInputStream(new File(configFilePath));
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setIgnoringComments(true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document dom = builder.parse(file);
			file.close();
			return dom;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	protected void removeEmptyNodes(Document dom) {
		NodeList nodeList = dom.getChildNodes();
		int i = 0;
		while (i < nodeList.getLength()) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE
					&& nodeList.item(i).getNodeValue().trim().isEmpty()) {
				dom.removeChild(nodeList.item(i));
			} else {
				removeEmptyNodes(nodeList.item(i));
				i++;
			}
		}
	}

	protected void removeEmptyNodes(Node node) {
		NodeList nodeList = node.getChildNodes();
		int i = 0;
		while (i < nodeList.getLength()) {
			if (nodeList.item(i).getNodeType() == Node.TEXT_NODE
					&& nodeList.item(i).getNodeValue().trim().isEmpty()) {
				node.removeChild(nodeList.item(i));
			} else {
				removeEmptyNodes(nodeList.item(i));
				i++;
			}
		}
	}
}
