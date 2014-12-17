package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;

public class SearchingConfigurationReader extends ConfigurationReader {

	private Map<String, List<String>> references = new HashMap<String, List<String>>();

	public SearchingData load(String configFilePath) throws RetrievalException {
		SearchingData searchingData = new SearchingData();
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);

		String message = parse(dom);
		if (!message.isEmpty()) {
			throw new RetrievalException(
					"Error occured while parsing searching configuration: \n"
							+ message);
		}

		Node urlNode = dom.getFirstChild();
		loadUrlNode(urlNode, searchingData);

		Node flowNode = urlNode.getFirstChild();

		while (!flowNode.getNodeName().equals("content")) {
			if (flowNode.getNodeName().equals("link")) {
				loadLinkNode(flowNode, searchingData);
			} else {
				loadFormNode(flowNode, searchingData);
			}
			flowNode = flowNode.getNextSibling();
		}

		loadContentNode(flowNode, searchingData);

		return searchingData;
	}

	private void loadUrlNode(Node urlNode, SearchingData searchingData) {
		NamedNodeMap urlAttrs = urlNode.getAttributes();
		searchingData.setUrl(urlAttrs.getNamedItem("address").getNodeValue());
		if (urlAttrs.getNamedItem("bulkRecords") != null) {
			searchingData.setBulkRecords(Integer.valueOf(urlAttrs.getNamedItem(
					"bulkRecords").getNodeValue()));
		} else {
			searchingData.setBulkRecords(-1);
		}
		if (urlAttrs.getNamedItem("crawledSites") != null) {
			searchingData.getContentFinder().setCrawledSites(
					Integer.valueOf(urlAttrs.getNamedItem("crawledSites")
							.getNodeValue()));
		} else {
			searchingData.getContentFinder().setCrawledSites(0);
		}
	}

	private void loadLinkNode(Node node, SearchingData searchingData) {
		searchingData.addFlowData(loadLinkNode(node));
	}

	private void loadLinkNode(Node node, ContentData contentData) {
		contentData.setNextPageLink(loadLinkNode(node));
	}

	private LinkData loadLinkNode(Node node) {
		LinkData linkData = new LinkData();
		NamedNodeMap linkAttrs = node.getAttributes();
		linkData.setType(linkAttrs.getNamedItem("type").getNodeValue());
		linkData.setBenchmark(linkAttrs.getNamedItem("value").getNodeValue());
		return linkData;
	}

	private void loadFormNode(Node node, SearchingData searchingData) {
		FormData formData = new FormData();
		NamedNodeMap formAttrs = node.getAttributes();
		if (formAttrs.getNamedItem("no") != null) {
			formData.setNo(Integer.valueOf(formAttrs.getNamedItem("no")
					.getNodeValue()));
		} else if (formAttrs.getNamedItem("name") != null) {
			formData.setFormName(formAttrs.getNamedItem("name").getNodeValue());
		} else if (formAttrs.getNamedItem("id") != null) {
			formData.setFormId(formAttrs.getNamedItem("id").getNodeValue());
		}

		if (formAttrs.getNamedItem("buttonNo") != null) {
			formData.setButtonNo(Integer.valueOf(formAttrs.getNamedItem(
					"buttonNo").getNodeValue()));
		} else if (formAttrs.getNamedItem("buttonName") != null) {
			formData.setButtonName(formAttrs.getNamedItem("buttonName")
					.getNodeValue());
		} else if (formAttrs.getNamedItem("buttonId") != null) {
			formData.setButtonId(formAttrs.getNamedItem("buttonId")
					.getNodeValue());
		}

		Node fieldNode = node.getFirstChild();
		while (fieldNode != null) {
			loadFormFieldNode(fieldNode, formData);
			fieldNode = fieldNode.getNextSibling();
		}

		searchingData.addFlowData(formData);
	}

	private void loadFormFieldNode(Node fieldNode, FormData formData) {
		FormFieldData fieldData = new FormFieldData();
		NamedNodeMap fieldAttrs = fieldNode.getAttributes();

		fieldData.setFieldName(fieldNode.getNodeName());
		if (fieldAttrs.getNamedItem("options") != null) {
			fieldData.addOptionsDescriptions(Arrays.asList(fieldAttrs
					.getNamedItem("options").getNodeValue().split(";")));
		}
		if (fieldAttrs.getNamedItem("name") != null) {
			fieldData.setFormFieldName(fieldAttrs.getNamedItem("name")
					.getNodeValue());
		}
		if (fieldAttrs.getNamedItem("value") != null) {
			fieldData.setDefaultValue(fieldAttrs.getNamedItem("value")
					.getNodeValue());
		}
		if (fieldAttrs.getNamedItem("usedValues") != null) {
			fieldData.addUsedValues(Arrays.asList(fieldAttrs
					.getNamedItem("usedValues").getNodeValue().split(";")));
		}
		if (fieldNode.hasChildNodes()) {
			fieldData.setDescription(fieldNode.getFirstChild().getNodeValue());
		}
		formData.addFieldData(fieldData);
	}

	private void loadContentNode(Node contentNode, SearchingData searchingData) {
		ContentData contentData = searchingData.getContentFinder();

		Node node = contentNode.getFirstChild();
		loadFirstContentNode(node, contentData);

		node = node.getNextSibling();
		loadSecondContentNode(node, contentData);

		node = node.getNextSibling();
		if (node != null) {
			loadLinkNode(node, contentData);
		}
		searchingData.setContentFinder(contentData);
	}

	private void loadFirstContentNode(Node node, ContentData contentData) {
		contentData.setBenchmarkType(node.getNodeName());
		if (node.hasAttributes()) {
			NamedNodeMap attrs = node.getAttributes();
			if (attrs.getNamedItem("no") != null) {
				contentData.setBenchmarkNo(Integer.valueOf(attrs.getNamedItem(
						"no").getNodeValue()));
			}
			if (attrs.getNamedItem("attr") != null) {
				contentData.setBenchmarkAttr(attrs.getNamedItem("attr")
						.getNodeValue());
			}
		}
		contentData.setBenchmark(node.getFirstChild().getNodeValue());
	}

	private void loadSecondContentNode(Node node, ContentData contentData) {
		contentData.setDataType(node.getNodeName());
		NamedNodeMap attrs = node.getAttributes();
		contentData.setSearchPath(attrs.getNamedItem("path").getNodeValue()
				.toLowerCase());
		contentData.setBenchmarkType(attrs.getNamedItem("type").getNodeValue());
		contentData.setLinkType(attrs.getNamedItem("linkType").getNodeValue());
		if (attrs.getNamedItem("terminator") != null) {
			contentData.setTerminatorNode(attrs.getNamedItem("terminator")
					.getNodeValue());
			contentData.setTerminatorNodeType(attrs.getNamedItem(
					"terminatorType").getNodeValue());
		}
		if (attrs.getNamedItem("terminatorPath") != null) {
			contentData.setTerminatorPath(attrs.getNamedItem("terminatorPath")
					.getNodeValue().toLowerCase());
		}
	}

	// ************************** Parsing searching configuration
	// **************************************

	public String parse(String configFilePath) {
		Document dom = loadDom(configFilePath);
		return parse(dom);
	}

	public String parse(Document dom) {
		removeEmptyNodes(dom);

		Node urlNode = dom.getFirstChild();
		StringBuilder message = new StringBuilder();

		removeEmptyNodes(urlNode);

		parseUrlNode(urlNode, message);

		Node node = urlNode.getFirstChild();
		while (node != null) {
			parseFlowNode(node, message, false);
			node = node.getNextSibling();
		}

		return message.toString();
	}

	private void parseFlowNode(Node node, StringBuilder message,
			boolean inSwitch) {

		if (node.getNodeName().equals("link")) {
			parseLinkNode(node, message);
		} else if (node.getNodeName().equals("form")) {
			parseFormNode(node, message);
		} else if (node.getNodeName().equals("content")) {
			if (node.getNextSibling() != null) {
				message.append("\'Content\' has to be the last node\n");
			} else {
				parseContentNode(node, message);
			}
		} else if (node.getNodeName().equals("switch")) {
			parseSwitchNode(node, message);
			parseFlowNode(node.getFirstChild(), message, true);
		} else if (inSwitch && node.getNodeName().equals("case")) {
			parseCaseNode(node, message);
		} else {
			message.append("Wrong node name of\'" + node.getNodeName() + "\'\n");
		}
	}

	private void parseUrlNode(Node urlNode, StringBuilder message) {
		if (!urlNode.getNodeName().equalsIgnoreCase("url")) {
			message.append("Wrong name for first node\n");
		}
		if ((urlNode.getAttributes().getLength() < 1 && urlNode.getAttributes()
				.getLength() > 3)
				|| urlNode.getAttributes().getNamedItem("address") == null) {
			message.append("Wrong parameters for node url\n");
		} else {
			NamedNodeMap attrs = urlNode.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				if (!attrs.item(i).getNodeName().equals("address")
						&& !attrs.item(i).getNodeName().equals("bulkRecords")
						&& !attrs.item(i).getNodeName().equals("crawledSites")) {
					message.append("Wrong attribute \'"
							+ attrs.item(i).getNodeName()
							+ "\' in node \'url\'\n");
				}
			}
		}
		if (urlNode.getAttributes().getNamedItem("bulkRecords") != null
				&& !urlNode.getAttributes().getNamedItem("bulkRecords")
						.getNodeValue().matches("\\d+")) {
			message.append("Bulk records must be an integer\n");
		}
		if (urlNode.getAttributes().getNamedItem("crawledSites") != null
				&& !urlNode.getAttributes().getNamedItem("bulkRecords")
						.getNodeValue().matches("\\d+")) {
			message.append("Crawled sites must be an integer\n");
		}
	}
	
	protected void parseLinkNode(Node node, StringBuilder message){
		NamedNodeMap attrs = node.getAttributes();
		if (attrs.getNamedItem("ref") == null){
			super.parseLinkNode(node, message);
			if (attrs.getNamedItem("as") != null){
				references.put("link" + attrs.getNamedItem("as").getNodeValue(), null);
			}
		} else {
			String refValue = attrs.getNamedItem("ref").getNodeValue();
			if (attrs.getLength() != 1) {
				message.append("Wrong attributes in node \'link\' referencing to \'"
						+ refValue + "\'\n");
			}
			if (node.hasChildNodes()) {
				message.append("Referencing node cannot has children\n");
			}
			if (!references.containsKey("link" + refValue)) {
				message.append("Reference to link \'" + refValue
						+ "\' is not valid\n");
			}
		}
	}

	private void parseFormNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();

		if (attrs.getNamedItem("ref") == null) {
			if (!(attrs.getNamedItem("name") != null
					|| attrs.getNamedItem("id") != null || attrs
						.getNamedItem("no") != null)) {
				message.append("Attribute \'name\', \'id\' or \'no\' is neccessary in node \'form\'\n");
			} else {
				for (int i = 0; i < attrs.getLength(); i++) {
					if (!attrs.item(i).getNodeName().equals("name")
							&& !attrs.item(i).getNodeName().equals("id")
							&& !attrs.item(i).getNodeName().equals("no")
							&& !attrs.item(i).getNodeName()
									.equals("buttonName")
							&& !attrs.item(i).getNodeName().equals("buttonId")
							&& !attrs.item(i).getNodeName().equals("buttonNo")
							&& !attrs.item(i).getNodeName().equals("as")) {
						message.append("Wrong attribute \'"
								+ attrs.item(i).getNodeName()
								+ "\' in node \'form\'\n");
					}
				}
			}
			if (attrs.getNamedItem("no") != null
					&& !attrs.getNamedItem("no").getNodeValue().matches("\\d+")) {
				message.append("Wrong value of attribute \'no\' in node \'form\'\n");
			}
			if (attrs.getNamedItem("buttonNo") != null
					&& !attrs.getNamedItem("buttonNo").getNodeValue()
							.matches("\\d+")) {
				message.append("Wrong value of attribute \'buttonNo\' in node \'form\'\n");
			}
			boolean variable = false;
			String name = null;
			if (attrs.getNamedItem("as") != null) {
				variable = true;
				name = "form" + attrs.getNamedItem("as").getNodeValue();
				references.put(
						name,
						new LinkedList<String>());
			}

			Node fieldNode = node.getFirstChild();
			while (fieldNode != null){
				if (variable){
					references.get("form" + name).add(fieldNode.getNodeName());
				}
				parseFieldNode(fieldNode, message);
			}
		} else {
			String refValue = attrs.getNamedItem("ref").getNodeValue();
			if (attrs.getLength() != 1) {
				message.append("Wrong attributes in node \'form\' referencing to \'"
						+ refValue + "\'\n");
			}
			if (node.hasChildNodes()) {
				message.append("Referencing node cannot has children\n");
			}
			if (!references.containsKey("form" + refValue)) {
				message.append("Reference to form \'" + refValue
						+ "\' is not valid in node \'form\'\n");
			}
		}
	}

	private void parseFieldNode(Node fieldNode, StringBuilder message) {
		NamedNodeMap fieldAttrs = fieldNode.getAttributes();

		for (int i = 0; i < fieldAttrs.getLength(); i++) {
			if (fieldAttrs.item(i).getNodeName().equals("type")) {
				if (ALLOWED_TYPES.contains(fieldAttrs.item(i).getNodeValue()
						.toUpperCase())) {
					message.append("Wrong type value in \'"
							+ fieldNode.getNodeName() + "\'\n");
				}
			} else if (fieldAttrs.item(i).getNodeName().equals("options")) {
				if (!fieldAttrs.item(i).getNodeValue().matches(".+(;.+)*")) {
					message.append("Wrong options value in \'"
							+ fieldNode.getNodeName() + "\'\n");
				} else {
					if (fieldAttrs.getNamedItem("value") != null) {
						List<String> options = Arrays.asList(fieldAttrs.item(i)
								.getNodeValue().split(";"));
						if (!options.contains(fieldAttrs.getNamedItem("value"))) {
							message.append("Wrong \'value\' value in \'"
									+ fieldNode.getNodeName() + "\'\n");
						}
					}
					if (fieldAttrs.getNamedItem("usedValues") != null) {
						List<String> options = Arrays.asList(fieldAttrs.item(i)
								.getNodeValue().split(","));
						List<String> usedValues = Arrays.asList(fieldAttrs
								.getNamedItem("usedValues").getNodeValue()
								.split(";"));
						for (String usedValue : usedValues) {
							if (!options.contains(usedValue)) {
								message.append("Wrong \'usedValues\' value in \'"
										+ fieldNode.getNodeName() + "\'\n");
							}
						}
					}
				}
			} else if (fieldAttrs.item(i).getNodeName().equals("usedValues")) {
				if (!fieldAttrs.item(i).getNodeValue().matches(".+(;.+)*")) {
					message.append("Wrong \'usedValues\' value in \'"
							+ fieldNode.getNodeName() + "\'\n");
				}
			} else if (!fieldAttrs.item(i).getNodeName().equals("value")
					&& !fieldAttrs.item(i).getNodeName().equals("name")) {
				message.append("Wrong attribute \'"
						+ fieldAttrs.item(i).getNodeName()
						+ "\' in node \'form\'\n");
			}
			if (fieldNode.hasChildNodes()) {
				if (fieldNode.getChildNodes().getLength() != 1
						&& fieldNode.getFirstChild().getNodeType() != Node.TEXT_NODE) {
					message.append("Wrong children type of node \'"
							+ fieldNode.getNodeName() + "\'\n");
				}
			}
		}
		fieldNode = fieldNode.getNextSibling();
	}

	private void parseContentNode(Node contentNode, StringBuilder message) {
		NamedNodeMap attrs = contentNode.getAttributes();
		if (attrs.getNamedItem("ref") == null){
			if (attrs.getNamedItem("as") != null){
				references.put("content"+ attrs.getNamedItem("as").getNodeValue(), null);
			}
			Node node = contentNode.getFirstChild();
			parseBenchmarkNode(node, message);
	
			node = node.getNextSibling();
			if (node == null || !node.getNodeName().equals("link")) {
				message.append("Wrong name of second node in \'content\' node");
			}
			while (node != null && node.getNodeName().equals("link")) {
				parseSiteLinkNode(node, message);
				node = node.getNextSibling();
			}
	
			if (node != null && node.getNodeName().equals("nextPageLink")) {
				parseLinkNode(node, message);
			}
		}else {
			String refValue = attrs.getNamedItem("ref").getNodeValue();
			if (attrs.getLength() != 1) {
				message.append("Wrong attributes in node \'content\' referencing to \'"
						+ refValue + "\'\n");
			}
			if (contentNode.hasChildNodes()) {
				message.append("Referencing node cannot has children\n");
			}
			if (!references.containsKey("content" + refValue)) {
				message.append("Reference to content \'" + refValue
						+ "\' is not valid'\n");
			}
		}
	}

	private void parseBenchmarkNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();
		if (node.getNodeName().equals("xpath")
				&& node.getAttributes().getLength() > 0) {
			message.append("Wrong attributes for node \'xpath\' in "
					+ node.getParentNode().getNodeName() + "\n");
		}
		if ((node.getNodeName().equals("text") || node.getNodeName().equals(
				"comment"))
				&& attrs.getNamedItem("attr") != null) {
			message.append("Node \'" + node.getNodeName() + "\' of \'"
					+ node.getParentNode().getNodeName()
					+ "\' cannot have attribute \'attr\'\n");
		}
		if (attrs.getNamedItem("no") != null
				&& !(attrs.getNamedItem("no").getNodeValue().matches("\\d+"))) {
			message.append("Wrong value of attribute \'no\' in node \'"
					+ node.getNodeName() + "\' of \'"
					+ node.getParentNode().getNodeName() + "\'\n");
		}
	}

	private void parseSiteLinkNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();

		if (attrs.getNamedItem("path") != null
				&& attrs.getNamedItem("type") != null
				&& attrs.getNamedItem("linkType") != null) {
			if (!attrs.getNamedItem("path").getNodeValue().toLowerCase()
					.matches(PATH_REGEX)) {
				message.append("Wrongly constructed attribute \'path\' in node \'"
						+ node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName() + "\'\n");
			}
			if (!attrs.getNamedItem("path").getNodeValue().toLowerCase()
					.endsWith("attr")
					&& !attrs.getNamedItem("type").getNodeValue()
							.equals("text")
					&& !attrs.getNamedItem("type").getNodeValue()
							.equals("value")
					&& !attrs.getNamedItem("type").getNodeValue()
							.equals("name")) {
				message.append("Wrong value of attribute \'type\' in node \'"
						+ node.getNodeName() + "\' of \'"
						+ node.getParentNode().getNodeName() + "\'\n");
			}

			List<String> allowedLinkTypeList = Arrays.asList("img,name,id,text"
					.split(","));
			if (!allowedLinkTypeList.contains(attrs.getNamedItem("linkType")
					.getNodeValue())) {
				message.append("Wrong value of attribute \'linkType\' in a content link node\n");
			}

			if (attrs.getNamedItem("terminator") != null) {
				if (attrs.getNamedItem("terminatorType") == null) {
					message.append("No attribute \'terminatorType\' in node \'"
							+ node.getNodeName() + "\' of \'"
							+ node.getParentNode().getNodeName() + "\'\n");
				}
				if (attrs.getNamedItem("terminatorPath") != null) {
					if (!attrs.getNamedItem("terminatorPath").getNodeValue()
							.toLowerCase().matches(PATH_REGEX)) {
						message.append("Wrongly constructed attribute \'terminatorPath\' in node \'"
								+ node.getNodeName()
								+ "\' of \'"
								+ node.getParentNode().getNodeName() + "\'\n");
					}
					if (((!attrs.getNamedItem("terminatorPath").getNodeValue()
							.toLowerCase().endsWith("attr")) || !attrs
							.getNamedItem("path").getNodeValue()
							.endsWith("attr"))
							&& !attrs.getNamedItem("terminatorType")
									.getNodeValue().equals("text")
							&& !attrs.getNamedItem("terminatorType")
									.getNodeValue().equals("value")
							&& !attrs.getNamedItem("terminatorType")
									.getNodeValue().equals("name")
							&& !attrs.getNamedItem("terminatorType")
									.getNodeValue().equals("null")) {
						message.append("Wrong value of attribute \'terminatorType\' in node \'\n"
								+ node.getNodeName()
								+ "\' of \'"
								+ node.getParentNode().getNodeName() + "\'\n");
					}
				}

			} else {
				if (node.getAttributes().getNamedItem("terminatorType") != null
						|| node.getAttributes().getNamedItem("terminatorPath") != null) {
					message.append("Wrong attributes in node "
							+ node.getNodeName() + "\' of \'"
							+ node.getParentNode().getNodeName() + "\'\n");
					message.append("	Attributes \'terminatorType\' and \'terminatorPath\' cannot exist when attribute \'terminator\' does not exist\n");
				}
			}

			if (!attrs.getNamedItem("next").getNodeValue().toLowerCase()
					.matches(PATH_REGEX)) {
				message.append("Wrongly constructed attribute \'next\' in node \'"
						+ node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName() + "\'\n");
			}

			List<String> allowedAttrs = Arrays
					.asList("next,path,terminator,terminatorPath,terminatorType,type,linkType"
							.split(","));
			for (int i = 0; i < attrs.getLength(); i++) {
				if (!allowedAttrs.contains(attrs.item(i).getNodeName())) {
					message.append("Wrong attribute \'"
							+ attrs.item(i).getNodeName() + "\' in node \'"
							+ node.getNodeName() + "\' of \'"
							+ node.getParentNode().getNodeName() + "\'\n");
				}
			}
		} else {
			message.append("Missing attributes path and type in node \'"
					+ node.getNodeName() + "\' of \'"
					+ node.getParentNode().getNodeName() + "\'\n");
		}
		if (node.getFirstChild() != null) {
			message.append("Node \'" + node.getNodeName() + "\' of \'"
					+ node.getParentNode().getNodeName()
					+ "\' cannot have any child\n");
		}
	}

	private void parseSwitchNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();
		if (attrs.getLength() == 0) {
			message.append("Missing fields attributes in \'switch\' node\n");
		}
		for (int i = 0; i < attrs.getLength(); i++) {
			if (!attrs.item(i).getNodeName().matches("field\\d*")) {
				message.append("Wrong attribute \'"
						+ attrs.item(i).getNodeName()
						+ "\' in node \'switch\'\n");
			}
			String[] value = attrs.item(i).getNodeValue().split(".");
			if (value.length != 2) {
				message.append("Wrong value of attribute \'"
						+ attrs.item(i).getNodeName()
						+ "\' in node \'switch\'\n");
			} else {
				if (!references.containsKey("form" + value[0])) {
					message.append("Reference to form \'" + value[0]
							+ "\' is not valid in node \'switch\'\n");
				} else if (!references.get("form" + value[0])
						.contains(value[1])) {
					message.append("Form \'" + value[0]
							+ "\' does not contain field \'" + value[1]
							+ "\'\n");
				}
			}
		}
	}

	private void parseCaseNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();
		if (attrs.getLength() == 0) {
			message.append("Missing values attributes in \'case\' node\n");
		}
		for (int i = 0; i < attrs.getLength(); i++) {
			if (!attrs.item(i).getNodeName().matches("values\\d*")) {
				message.append("Wrong attribute \'"
						+ attrs.item(i).getNodeName() + "\' in node \'case\'\n");
			}
		}
	}
}
