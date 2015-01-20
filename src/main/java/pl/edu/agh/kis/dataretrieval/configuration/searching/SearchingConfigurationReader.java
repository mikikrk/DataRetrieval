package pl.edu.agh.kis.dataretrieval.configuration.searching;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;

public class SearchingConfigurationReader extends ConfigurationReader {

	private Map<String, List<String>> references = new HashMap<String, List<String>>();
	private Map<String, ConfigData> namedElements = new HashMap<String, ConfigData>();
	private SearchingData searchingData;

	public SearchingData load(String configFilePath) throws RetrievalException, ParserConfigurationException, SAXException, IOException {
		searchingData = new SearchingData();
		Document dom = loadDom(configFilePath);

		String message = parse(configFilePath);
		if (!message.isEmpty()) {
			throw new RetrievalException(
					"Error occured while parsing searching configuration: \n"
							+ message);
		}

		Node urlNode = dom.getFirstChild();
		loadUrlNode(urlNode, searchingData);

		Node flowNode = urlNode.getFirstChild();

		while (flowNode != null) {
			searchingData.addFlowData(loadFlowNode(flowNode));
			flowNode = flowNode.getNextSibling();
		}

		return searchingData;
	}
	
	private ConfigData loadFlowNode(Node flowNode){
		ConfigData flowData = null;
		if (flowNode.getNodeName().equals("link")) {
			flowData = loadLinkNode(flowNode);
		} else if (flowNode.getNodeName().equals("form")){
			flowData = loadFormNode(flowNode);
		} else if (flowNode.getNodeName().equals("switch")){
			flowData = loadSwitchNode(flowNode);
		} else if (flowNode.getNodeName().equals("content")){
			flowData = loadContentNode(flowNode);
		}
		return flowData;
	}
	
	private void loadUrlNode(Node urlNode, SearchingData searchingData) {
		NamedNodeMap urlAttrs = urlNode.getAttributes();
		searchingData.setUrl(urlAttrs.getNamedItem("address").getNodeValue());
		if (urlAttrs.getNamedItem("maxRecords") != null) {
			searchingData.setMaxRecords(Integer.valueOf(urlAttrs.getNamedItem(
					"maxRecords").getNodeValue()));
		} else {
			searchingData.setMaxRecords(-1);
		}
		if (urlAttrs.getNamedItem("startSite") != null) {
			searchingData.setStartSite(
					Integer.valueOf(urlAttrs.getNamedItem("startSite")
							.getNodeValue()));
		} else {
			searchingData.setStartSite(1);
		}
	}
	
	private SwitchData loadSwitchNode(Node switchNode){
		NamedNodeMap switchAttrs = switchNode.getAttributes();
		SwitchData switchData = new SwitchData();
		
		Map<Integer, String> fieldRefs = new HashMap<Integer, String>();
		
		for (int i = 0; i < switchAttrs.getLength(); i++){
			String refValue = switchAttrs.item(i).getNodeValue();
			String[] ref = refValue.split("\\.");
			
			String strFieldNb = switchAttrs.item(i).getNodeName().replace("field",  "");
			Integer fieldNb = strFieldNb.trim().isEmpty() ? 0 : Integer.valueOf(strFieldNb); 
			fieldRefs.put(fieldNb, refValue);
			
			FormData form = (FormData) namedElements.get("form" + ref[0]);
			for (FormFieldData fieldData: form.getFields()){
				if (fieldData.getFieldName().equals(ref[1])){
					switchData.addRefField(refValue, fieldData);
				}
			}
		}
		
		Node caseNode = switchNode.getFirstChild();
		while (caseNode != null) {
			switchData.addCases(loadCaseNode(caseNode, fieldRefs));
			caseNode = caseNode.getNextSibling();
		}
		return switchData;
	}
	
	private Map<CaseData, List<ConfigData>> loadCaseNode(Node caseNode, Map<Integer, String> fieldRefs){
		NamedNodeMap caseAttrs = caseNode.getAttributes();
		Map<CaseData, List<ConfigData>> caseFlowData = new HashMap<CaseData, List<ConfigData>>();
		CaseData caseData;
		
		if (caseNode.getNodeName().equals("case")){
			caseData = new CaseData();
			for (int i = 0; i < caseAttrs.getLength(); i++){
				String values = caseAttrs.item(i).getNodeValue();
				
				String strValuesNb = caseAttrs.item(i).getNodeName().replace("values",  "");
				Integer valuesNb = strValuesNb.trim().isEmpty() ? 0 : Integer.valueOf(strValuesNb);
				
				String refField = fieldRefs.get(valuesNb);
				caseData.addValue(refField, Arrays.asList(values.split(";")));
			}
		}else{
			caseData = CaseData.getDefaultCaseData();
		}
		
		List<ConfigData> flowDataList = new LinkedList<ConfigData>();
		Node flowNode = caseNode.getFirstChild();
		while (flowNode != null){
			flowDataList.add(loadFlowNode(flowNode));
			flowNode = flowNode.getNextSibling();
		}
		
		caseFlowData.put(caseData, flowDataList);
		return caseFlowData;
	}

	private LinkData loadLinkNode(Node node) {
		NamedNodeMap linkAttrs = node.getAttributes();
		if (linkAttrs.getNamedItem("ref") == null){
			LinkData linkData = new LinkData();
			linkData.setType(linkAttrs.getNamedItem("type").getNodeValue());
			linkData.setBenchmark(linkAttrs.getNamedItem("value").getNodeValue());
			if (linkAttrs.getNamedItem("as") != null){
				namedElements.put("link" + linkAttrs.getNamedItem("as").getNodeValue(), linkData);
			}
			return linkData;
		}else{
			return (LinkData) namedElements.get("link" + linkAttrs.getNamedItem("ref").getNodeValue());
		}
	}

	private FormData loadFormNode(Node node) {
		NamedNodeMap formAttrs = node.getAttributes();
		if (formAttrs.getNamedItem("ref") == null){
			FormData formData = new FormData();
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
				formData.addFieldData(loadFormFieldNode(fieldNode));
				fieldNode = fieldNode.getNextSibling();
			}
			if (formAttrs.getNamedItem("as") != null){
				namedElements.put("form" + formAttrs.getNamedItem("as").getNodeValue(), formData);
			}

			return formData;
		}else{
			return (FormData) namedElements.get("form" + formAttrs.getNamedItem("ref").getNodeValue());
		}
	}

	private FormFieldData loadFormFieldNode(Node fieldNode) {
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
		if (fieldAttrs.getNamedItem("values") != null) {
			fieldData.addAlternativeDefaultValues(Arrays.asList(fieldAttrs.getNamedItem("values")
					.getNodeValue().split("\\|")));
		}
		if (fieldAttrs.getNamedItem("lastUsedValues") != null) {
			fieldData.setLastUsedValues(fieldAttrs
					.getNamedItem("lastUsedValues").getNodeValue());
		}
		if (fieldNode.hasChildNodes()) {
			fieldData.setDescription(fieldNode.getFirstChild().getNodeValue());
		}
		return fieldData;
	}

	private ContentData loadContentNode(Node contentNode) {
		NamedNodeMap contentAttrs = contentNode.getAttributes();
		if (contentAttrs.getNamedItem("ref") == null){
			ContentData contentData = new ContentData();
	
			Node node = contentNode.getFirstChild();
			loadFirstContentNode(node, contentData);
	
			node = node.getNextSibling();
			loadSecondContentNode(node, contentData);
	
			node = node.getNextSibling();
			if (node != null) {
				contentData.setNextPageLink(loadLinkNode(node));
			}
			
			if (contentAttrs.getNamedItem("as") != null){
				namedElements.put("content" + contentAttrs.getNamedItem("as").getNodeValue(), contentData);
			}
			
			return contentData;
		}else{
			return (ContentData) namedElements.get("content" + contentAttrs.getNamedItem("ref").getNodeValue());
		}
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
		if (attrs.getNamedItem("next") != null){
			contentData.setSearchNextPath(attrs.getNamedItem("next")
					.getNodeValue().toLowerCase());
		}
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

	// ************************** Parsing searching configuration **************************************

	public String parse(String configFilePath) throws ParserConfigurationException, SAXException, IOException {
		Document dom = loadDom(configFilePath);
		return parse(dom);
	}

	public String parse(Document dom) {
		Node urlNode = dom.getFirstChild();
		StringBuilder message = new StringBuilder();

		parseUrlNode(urlNode, message);

		Node node = urlNode.getFirstChild();
		while (node != null) {
			parseFlowNode(node, message);
			node = node.getNextSibling();
		}

		return message.toString();
	}

	private void parseFlowNode(Node node, StringBuilder message) {

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
		} else {
			message.append("Wrong flow node name: \'" + node.getNodeName() + "\'\n");
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
						&& !attrs.item(i).getNodeName().equals("maxRecords")
						&& !attrs.item(i).getNodeName().equals("startSite")) {
					message.append("Wrong attribute \'"
							+ attrs.item(i).getNodeName()
							+ "\' in node \'url\'\n");
				}
			}
		}
		if (urlNode.getAttributes().getNamedItem("maxRecords") != null
				&& !urlNode.getAttributes().getNamedItem("maxRecords")
						.getNodeValue().matches("\\d+")) {
			message.append("Max records must be an integer\n");
		}
		if (urlNode.getAttributes().getNamedItem("startSite") != null
				&& !urlNode.getAttributes().getNamedItem("startSite")
						.getNodeValue().matches("\\d+")) {
			message.append("Start site value must be an integer\n");
		}
		if (urlNode.getNextSibling() != null){
			message.append("Node \'url\' cannot has any sibling");
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
				message.append("Attributes \'name\', \'id\' or \'no\' are neccessary in node \'form\'\n");
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
				if ((attrs.getNamedItem("name") != null
							&& attrs.getNamedItem("id") != null)
						|| (attrs.getNamedItem("name") != null
							&& attrs.getNamedItem("no") != null)
						|| attrs.getNamedItem("no") != null
							&& attrs.getNamedItem("id") != null
						){
					message.append("Only one of attributes: \'name\', \'id\', \'no\' can be used in \'fomr\' node");
				}
				if ((attrs.getNamedItem("buttonName") != null
						&& attrs.getNamedItem("buttonId") != null)
					|| (attrs.getNamedItem("buttonName") != null
						&& attrs.getNamedItem("buttonNo") != null)
					|| (attrs.getNamedItem("buttonNo") != null
						&& attrs.getNamedItem("buttonId") != null)
					){
				message.append("Only one of attributes: \'buttonName\', \'buttonId\', \'buttonNo\' can be used in \'fomr\' node");
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
				references.put(name, new LinkedList<String>());
			}

			Node fieldNode = node.getFirstChild();
			while (fieldNode != null){
				if (variable){
					references.get(name).add(fieldNode.getNodeName());
				}
				parseFieldNode(fieldNode, message);
				fieldNode = fieldNode.getNextSibling();
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
			if (fieldAttrs.item(i).getNodeName().equals("options")) {
				if (!fieldAttrs.item(i).getNodeValue().matches(".+(;.+)*")) {
					message.append("Wrong options value in \'"
							+ fieldNode.getNodeName() + "\'\n");
				}
			} else if (fieldAttrs.item(i).getNodeName().equals("values")) {
				if (fieldAttrs.item(i).getNodeValue().matches("((.+$every$) | ($every$.+) | (.+$all$) | ($all$.+))")) {
					message.append("Wrong \'values\' value in \'"
							+ fieldNode.getNodeName() + "\'\n");
				}
			} else if (!fieldAttrs.item(i).getNodeName().equals("lastUsedValues")
					&& !fieldAttrs.item(i).getNodeName().equals("name")) {
				message.append("Wrong attribute \'"
						+ fieldAttrs.item(i).getNodeName()
						+ "\' in node \'" + fieldNode + "\'\n");
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
				if (attrs.getLength() != 1){
					message.append("'Content' node cannot has another attributes then \'ref\' or \'as\'");
				}
				references.put("content"+ attrs.getNamedItem("as").getNodeValue(), null);
			}else if (attrs.getLength() != 0){
				message.append("'Content' node cannot has another attributes then \'ref\' or \'as\'");
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
			
			node = contentNode;
			do{
				if (node.getNextSibling() != null){
					message.append("Content node cannot be followed by another nodes\n");
				}
				node = node.getParentNode();
				if (!node.getNodeName().equals("url")){
					node = node.getParentNode();
				}
			}while(!node.getNodeName().equals("url"));
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
				&& node.getAttributes().getLength() > 1 || (node.getAttributes().getLength() == 1 && node.getAttributes().getNamedItem("no") != null)) {
			message.append("Wrong attributes for node \'xpath\' in "
					+ node.getParentNode().getNodeName() + "\n");
		} else if (node.getNodeName().equals("text")
				&& node.getAttributes().getLength() > 1 || (node.getAttributes().getLength() == 1 && node.getAttributes().getNamedItem("no") != null)) {
			message.append("Wrong attributes for node \'text\' in "
					+ node.getParentNode().getNodeName() + "\n");
		} else if (node.getAttributes().getLength() > 2 ||  (node.getAttributes().getLength() == 2 && node.getAttributes().getNamedItem("no") != null && node.getAttributes().getNamedItem("attr") != null) || (node.getAttributes().getLength() == 1 && (node.getAttributes().getNamedItem("no") != null && node.getAttributes().getNamedItem("attr") != null))){
			message.append("Wrong attributes for node \'" + node.getNodeName() + "\' in "
					+ node.getParentNode().getNodeName() + "\n");
		}
		if (!node.getNodeName().equals("xpath") && !node.getNodeName().equals("text") && node.getAttributes().getNamedItem("attr") == null && node.hasChildNodes()){
			message.append("Node " + node.getNodeName() + " without attribute \'attr\' cannot has child\n");
		}else{
			if (!node.hasChildNodes()){
				message.append("Node \'" + node.getNodeName() + "\' has to have text child\n");
			}
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
				}
				if ((attrs.getNamedItem("terminatorPath") != null && 
						!attrs.getNamedItem("terminatorPath").getNodeValue()
							.toLowerCase().endsWith("attr")) 
					|| (attrs.getNamedItem("terminatorPath") == null && 
						attrs.getNamedItem("next") != null &&
						!attrs.getNamedItem("next").getNodeValue()
							.toLowerCase().endsWith("attr")) 
					|| (attrs.getNamedItem("terminatorPath") == null && 
							attrs.getNamedItem("next") == null &&
							!attrs.getNamedItem("path").getNodeValue()
								.toLowerCase().endsWith("attr")) 
					){
						if (!attrs.getNamedItem("terminatorType")
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
		List<Integer> fieldNbs = new LinkedList<Integer>();
		for (int i = 0; i < attrs.getLength(); i++) {
			if (!attrs.item(i).getNodeName().matches("field\\d*")) {
				message.append("Wrong attribute \'"
						+ attrs.item(i).getNodeName()
						+ "\' in node \'switch\'\n");
			}
			String[] value = attrs.item(i).getNodeValue().split("\\.");
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
			String strFieldNb = attrs.item(i).getNodeName().replace("field",  "");
			Integer fieldNb = strFieldNb.trim().isEmpty() ? 0 : Integer.valueOf(strFieldNb);
			fieldNbs.add(fieldNb);
		}
		if (node.hasChildNodes()){
			Node caseNode = node.getFirstChild();
			while (caseNode != null){
				parseCaseNode(caseNode, message, fieldNbs);
				caseNode = caseNode.getNextSibling();
			}
		}else{
			message.append("Missing case statements in switch node\n");
		}
	}

	private void parseCaseNode(Node node, StringBuilder message, List<Integer> fieldNbs) {
		if (!node.getNodeName().equals("case") && !node.getNodeName().equals("default")){
			message.append("Unexpected node name: \'" + node.getNodeName() + "\' in switch\n");
		}else{
			if (node.getNodeName().equals("case")){
				NamedNodeMap attrs = node.getAttributes();
				if (attrs.getLength() == 0) {
					message.append("Missing values attributes in \'case\' node\n");
				}
				List<Integer> valuesNbs = new LinkedList<Integer>();
				for (int i = 0; i < attrs.getLength(); i++) {
					if (!attrs.item(i).getNodeName().matches("values\\d*")) {
						message.append("Wrong attribute \'"
								+ attrs.item(i).getNodeName() + "\' in node \'case\'\n");
					}
					String strValuesNb = attrs.item(i).getNodeName().replace("values",  "");
					Integer valuesNb = strValuesNb.trim().isEmpty() ? 0 : Integer.valueOf(strValuesNb);
					valuesNbs.add(valuesNb);
					if (!fieldNbs.contains(valuesNb)){
						message.append("There is no field matching to \'values" + valuesNb + "\'\n");
					}
				}
			}
			if (node.hasChildNodes()){
				Node flowNode = node.getFirstChild();
				while (flowNode != null){
					parseFlowNode(flowNode, message);
					flowNode = flowNode.getNextSibling();
				}
			}else{
				message.append("Node \'" + node.getNodeName() + "\' has no child nodes");
			}
		}
	}
}
