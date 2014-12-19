package pl.edu.agh.kis.dataretrieval.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class ConfigurationReader {
	
	protected static final List<String> NODE_TYPES = Arrays
			.asList("!DOCTYPE> <a> <abbr> <acronym> <address> <applet> <area> <article> <aside> <audio> <b> <base> <basefont> <bdi> <bdo> <big> <blockquote> <body> <br> <button> <canvas> <caption> <center> <cite> <code> <col> <colgroup> <comment> <datalist> <dd> <del> <details> <dfn> <dialog> <dir> <div> <dl> <dt> <em> <embed> <fieldset> <figcaption> <figure> <font> <footer> <form> <frame> <frameset> <head> <header> <h1> <h2> <h3> <h4> <h5> <h6> <hr> <html> <i> <iframe> <img> <input> <ins> <kbd> <keygen> <label> <legend> <li> <link> <main> <map> <mark> <menu> <menuitem> <meta> <meter> <nav> <noframes> <noscript> <object> <ol> <optgroup> <option> <output> <p> <param> <pre> <progress> <q> <rp> <rt> <ruby> <s> <samp> <script> <section> <select> <small> <source> <span> <strike> <strong> <style> <sub> <summary> <sup> <table> <tbody> <td> <text> <textarea> <tfoot> <th> <thead> <time> <title> <tr> <track> <tt> <u> <ul> <var> <video> <wbr"
					.split("> <"));
	protected static final List<String> JAVA_TYPES = Arrays
			.asList("bool,boolean,byte,char,character,double,float,int,integer,long,short,string"
					.split(","));
	protected static final String PATH_REGEX = "(this|(\\d*\\.?(parent|sibling|nextsibling|prevsibling|child|firstchild|lastchild)/)*(\\d*\\.?(parent|sibling|nextsibling|prevsibling|child|firstchild|lastchild))|attr)";
	protected static final List<String> SQL_TYPES = Arrays.asList("array,bigint,binary,bit,blob,boolean,char,clob,datalink,date,decimal,distinct,double,float,integer,jaba_object,longvarchar,longvarbinary,nchar,nclob,null,numeric,nvarchar,other,real,ref,rowid,smallint,sqlxml,struct,time,timestamp,tinyint,varbinary,varchar".split(","));
	protected static final List<String> ALLOWED_TYPES = Arrays.asList("TEXT,TEXTAREA,CHECKBOX,COMBOBOX,LIST,RADIO".split(","));
	
	public abstract String parse(Document dom);
	public abstract String parse(String configFilePath) throws ParserConfigurationException, SAXException, IOException;
	
	public static Document loadDom(String configFilePath) throws ParserConfigurationException, SAXException, IOException {
			FileInputStream file = new FileInputStream(new File(configFilePath));
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setIgnoringComments(true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document dom = builder.parse(file);
			file.close();
			return dom;
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
	

	protected void parseLinkNode(Node node, StringBuilder message) {
		if (node.getAttributes().getLength() != 2 || node.getAttributes().getNamedItem("type") == null || node.getAttributes().getNamedItem("value") == null) {
			message.append("Wrong attributes for node \'link\'\n");
		} else {
			NamedNodeMap attrs = node.getAttributes();
			List<String> allowedLinkTypeList = Arrays.asList("img,name,id,text".split(","));
			if (!allowedLinkTypeList.contains(attrs.getNamedItem("type").getNodeValue())){
				message.append("Wrong value of attribute \'type\' in link node\n");
			}
		}
	}

	
	public Document getDOMFromString(String text) throws ParserConfigurationException, SAXException, IOException  {
		DocumentBuilder documentBuilder;
			
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(text));

		return documentBuilder.parse(is);
	}
}
