package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;

public class SearchingConfigurationReader extends ConfigurationReader {

	public void load(String cofigFilePath) {

	}

	public String parse(String configFilePath) {
		Document dom = loadDom(configFilePath);
		removeEmptyNodes(dom);
		return parse(dom);
	}

	public String parse(Document dom) {
		Node urlNode = dom.getFirstChild();
		StringBuilder message = new StringBuilder();
		
		removeEmptyNodes(urlNode);

		if (urlNode.getNodeName().equalsIgnoreCase("url")) {
			message.append("Wrong name for first node\n");
		}
		if ((urlNode.getAttributes().getLength() < 1 && urlNode.getAttributes().getLength() > 2)
				|| urlNode.getAttributes().getNamedItem("address") == null) {
			message.append("Wrong parameters for node url\n");
		}
		if (urlNode.getAttributes().getNamedItem("bulkRecords") != null && !urlNode.getAttributes().getNamedItem("bulkRecords").getNodeValue().matches("\\d+")){
			message.append("Bulk records must be an integer\n");
		}

		Node node = urlNode.getFirstChild();
		while (node != null) {
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
			} else {
				message.append("Wrong node name of\'" + node.getNodeName()
						+ "\'\n");
			}
			node = node.getNextSibling();
		}

		return message.toString();
	}

	private void parseFormNode(Node node, StringBuilder message) {
		NamedNodeMap attrs = node.getAttributes();

		if (!(attrs.getNamedItem("name") != null
				|| attrs.getNamedItem("id") != null || attrs.getNamedItem("no") != null)) {
			message.append("Attribute \'name\', \'id\' or \'no\' is neccessary in node \'form\'\n");
		} else {
			for (int i = 0; i < attrs.getLength(); i++) {
				if (!attrs.item(i).getNodeName().equals("name")
						&& !attrs.item(i).getNodeName().equals("id")
						&& !attrs.item(i).getNodeName().equals("no")) {
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

		Node fieldNode = node.getFirstChild();
		while (fieldNode != null) {
			NamedNodeMap fieldAttrs = fieldNode.getAttributes();

			for (int i = 0; i < fieldAttrs.getLength(); i++) {
				if (fieldAttrs.item(i).getNodeName().equals("type")) {
					if (ALLOWED_TYPES.contains(fieldAttrs.item(i)
							.getNodeValue().toUpperCase())) {
						message.append("Wrong type value in \'"
								+ fieldNode.getNodeName() + "\'\n");
					}
				} else if (fieldAttrs.item(i).getNodeName().equals("options")) {
					if (!fieldAttrs.item(i).getNodeValue().matches(".+(,.+)*")) {
						message.append("Wrong options value in \'"
								+ fieldNode.getNodeName() + "\'\n");
					} else {
						if (fieldAttrs.getNamedItem("value") != null) {
							List<String> options = Arrays.asList(fieldAttrs
									.item(i).getNodeValue().split(","));
							if (options.contains(fieldAttrs
									.getNamedItem("value"))) {
								message.append("Wrong \'value\' value in \'"
										+ fieldNode.getNodeName() + "\'\n");
							}
						}
					}
				} else if (!fieldAttrs.item(i).getNodeName().equals("value")
						&& !fieldAttrs.item(i).getNodeName().equals("name")) {
					message.append("Wrong attribute \'"
							+ fieldAttrs.item(i).getNodeName()
							+ "\' in node \'form\'\n");
				}
			}
		}
	}

	private void parseContentNode(Node contentNode, StringBuilder message) {
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

		if (node != null && node.getNodeName().equals("nextPageLink")){
			parseLinkNode(node, message);
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
				&& attrs.getNamedItem("type") != null) {
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
			
			if (!attrs.getNamedItem("next").getNodeValue()
					.toLowerCase().matches(PATH_REGEX)) {
				message.append("Wrongly constructed attribute \'next\' in node \'"
						+ node.getNodeName()
						+ "\' of \'"
						+ node.getParentNode().getNodeName()
						+ "\'\n");
			}
			
			List<String> allowedAttrs = Arrays
					.asList("next,path,terminator,terminatorPath,terminatorType,type"
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
}
