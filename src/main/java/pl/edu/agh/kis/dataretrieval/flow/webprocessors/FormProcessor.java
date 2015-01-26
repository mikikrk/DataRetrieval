package pl.edu.agh.kis.dataretrieval.flow.webprocessors;

import java.awt.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormFieldData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.SearchingData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.SwitchData;

import com.meterware.httpunit.Button;
import com.meterware.httpunit.FormControl;
import com.meterware.httpunit.FormParameter;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

public class FormProcessor {

	public void loadForm(FormData formData, WebResponse resp)
			throws SAXException, RetrievalException {
		WebForm form = getWebForm(formData, resp);
		formData.setWebForm(form);

		if (formData.getFields().isEmpty()){
			for (String parameterName: form.getParameterNames()){
				formData.addFieldData(new FormFieldData(parameterName));
			}
		}
		for (FormFieldData fieldData : formData.getFields()) {
			loadField(fieldData, form);
		}
	}

	private WebForm getWebForm(FormData formData, WebResponse resp)
			throws SAXException, RetrievalException {
		WebForm form;
		if (formData.getNo() != null) {
			form = resp.getForms()[formData.getNo()];
			if (form == null) {
				throw new RetrievalException("Form no " + formData.getNo()
						+ " does not exist");
			}
		} else if (formData.getFormId() != null) {
			form = resp.getFormWithID(formData.getFormId());
			if (form == null) {
				throw new RetrievalException("Form with id "
						+ formData.getFormId() + " does not exist");
			}
		} else if (formData.getFormName() != null) {
			form = resp.getFormWithName(formData.getFormName());
			if (form == null) {
				throw new RetrievalException("Form \'" + formData.getFormName()
						+ "\' does not exist");
			}
		} else {
			throw new RetrievalException("No form identifier");
		}
		return form;
	}

	private void loadField(FormFieldData fieldData, WebForm form) throws RetrievalException {
		String fieldName = fieldData.getFormFieldName() != null ? fieldData
				.getFormFieldName() : fieldData.getFieldName();
		FormParameter formParameter = form.getParameter(fieldName);
		if (formParameter == null){
			throw new RetrievalException("Field with name \'" + fieldData.getFieldName() + "\' does not exist");
		}
		fieldData.setFormParameter(formParameter);

		FormFieldData.FieldType fieldType = getFieldType(formParameter);
		fieldData.setFieldType(fieldType);
		Node fieldNode = getFieldNode(form.getNode().getFirstChild(), fieldName, fieldType);
		setFieldOptions(formParameter, fieldType, fieldData.getOptions());
		if (fieldNode != null){
			if (fieldData.getOptionsDescriptions().isEmpty()){
			setFieldTextOptions(fieldData.getOptionsDescriptions(), fieldNode, fieldType);
			}
			if (fieldData.getDescription().isEmpty()){
				fieldData.setDescription(getDescription(fieldNode));
			}
		}
	}

	private FormFieldData.FieldType getFieldType(FormParameter formParameter) {
		FormControl[] formControls = formParameter.getControls();
		String htmlFieldType = formControls[0].getType();
		FormFieldData.FieldType fieldType = null;
		if (htmlFieldType.equals("text")) {
			fieldType = FormFieldData.FieldType.TEXT;
		} else if (htmlFieldType.equals("textarea")) {
			fieldType = FormFieldData.FieldType.TEXTAREA;
		} else if (htmlFieldType.equals("checkbox")) {
			fieldType = FormFieldData.FieldType.CHECKBOX;
		} else if (htmlFieldType.equals("select-one")) {
			if (formControls[0].getAttribute("size").isEmpty()) {
				fieldType = FormFieldData.FieldType.COMBOBOX;
			} else {
				fieldType = FormFieldData.FieldType.SELECT_ONE;
			}
		} else if (htmlFieldType.equals("select-multiple")) {
			fieldType = FormFieldData.FieldType.SELECT_MULTIPLE;
		} else if (htmlFieldType.equals("radio")
				|| htmlFieldType.equalsIgnoreCase("undefined")) {
			fieldType = FormFieldData.FieldType.RADIO;
		}
		return fieldType;
	}

	private void setFieldOptions(FormParameter formParameter,
			FormFieldData.FieldType fieldType, List<String> options) {
		options.clear();
		if (fieldType.equals(FormFieldData.FieldType.COMBOBOX)
				|| fieldType.equals(FormFieldData.FieldType.SELECT_ONE)
				|| fieldType.equals(FormFieldData.FieldType.SELECT_MULTIPLE)
				|| fieldType.equals(FormFieldData.FieldType.CHECKBOX)
				|| fieldType.equals(FormFieldData.FieldType.RADIO)) {
			for (FormControl formControl : formParameter.getControls()) {
				for (String value : formControl.getOptionValues()) {
					options.add(value);
				}
			}
		}
	}

	private Node getFieldNode(Node fieldNode, String fieldName,
			FormFieldData.FieldType fieldType) {
		while (fieldNode != null) {
			if (isFieldNode(fieldNode, fieldType)
					&& fieldNode.getAttributes().getNamedItem("name") != null
					&& fieldNode.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase(fieldName)){
				return fieldNode;
			}else{
				Node returnNode;
				returnNode = getFieldNode(fieldNode.getFirstChild(), fieldName, fieldType);
				if (returnNode != null){
					return returnNode;
				}
			}
			fieldNode = fieldNode.getNextSibling();
		}
		return null;
	}

	private boolean isFieldNode(Node fieldNode,
			FormFieldData.FieldType fieldType) {
		if (fieldType.equals(FormFieldData.FieldType.TEXT)) {
			if (fieldNode.getNodeName().equalsIgnoreCase("input")
					&& fieldNode.getAttributes().getNamedItem("type") != null
					&& fieldNode.getAttributes().getNamedItem("type")
							.getNodeValue().equalsIgnoreCase("text")) {
				return true;
			} else {
				return false;
			}
		} else if (fieldType.equals(FormFieldData.FieldType.TEXTAREA)) {
			if (fieldNode.getNodeName().equalsIgnoreCase("textarea")) {
				return true;
			} else {
				return false;
			}
		} else if (fieldType.equals(FormFieldData.FieldType.CHECKBOX)) {
			if (fieldNode.getNodeName().equalsIgnoreCase("input")
					&& fieldNode.getAttributes().getNamedItem("type") != null
					&& fieldNode.getAttributes().getNamedItem("type")
							.getNodeValue().equalsIgnoreCase("checkbox")) {
				return true;
			} else {
				return false;
			}
		} else if ((fieldType.equals(FormFieldData.FieldType.SELECT_MULTIPLE))) {
			if (fieldNode.getNodeName().equalsIgnoreCase("select")
					&& fieldNode.getAttributes().getNamedItem("multiple") != null) {
				return true;
			} else {
				return false;
			}
		} else if ((fieldType.equals(FormFieldData.FieldType.SELECT_ONE))) {
			if (fieldNode.getNodeName().equalsIgnoreCase("select")
					&& fieldNode.getAttributes().getNamedItem("size") != null
					&& Integer.valueOf(fieldNode.getAttributes()
							.getNamedItem("size").getNodeValue()) > 1) {
				return true;
			} else {
				return false;
			}
		} else if (fieldType.equals(FormFieldData.FieldType.COMBOBOX)
				|| fieldType.equals(FormFieldData.FieldType.SELECT_MULTIPLE)) {
			if (fieldNode.getNodeName().equalsIgnoreCase("select")) {
				return true;
			} else {
				return false;
			}
		} else if (fieldType.equals(FormFieldData.FieldType.RADIO)) {
			if (fieldNode.getNodeName().equalsIgnoreCase("input")
					&& fieldNode.getAttributes().getNamedItem("type") != null
					&& fieldNode.getAttributes().getNamedItem("type")
							.getNodeValue().equalsIgnoreCase("radio")) {
				return true;
			} else {
				return false;
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private void setFieldTextOptions(List<String> options, Node fieldNode, FormFieldData.FieldType fieldType) {
		if (fieldType.equals(FormFieldData.FieldType.COMBOBOX) || fieldType.equals(FormFieldData.FieldType.SELECT_ONE) || fieldType.equals(FormFieldData.FieldType.SELECT_MULTIPLE)){
			getSelectTextOptions(options, fieldNode.getFirstChild());
		}else if (fieldType.equals(FormFieldData.FieldType.CHECKBOX) || fieldType.equals(FormFieldData.FieldType.RADIO)){
			
		}
	}

	private void getSelectTextOptions(List<String> options, Node fieldNode) {
		while (fieldNode != null) {
			if (fieldNode.getNodeName().equalsIgnoreCase("option")) {
				Node childNode = fieldNode.getFirstChild();
				if (childNode != null
						&& childNode.getNodeType() == Node.TEXT_NODE) {
					setOption(options, fieldNode, childNode.getNodeValue());
				}
			} else {
				getSelectTextOptions(options, fieldNode.getFirstChild());
			}
			fieldNode = fieldNode.getNextSibling();
		}
	}
	
	private void getBoxesTextValues(List<String> options, Node fieldNode){
		if (fieldNode.getNextSibling() != null && fieldNode.getNextSibling().getNodeType() == Node.TEXT_NODE){
			setOption(options, fieldNode, fieldNode.getNextSibling().getNodeValue());
		}else if (fieldNode.getNextSibling() != null && fieldNode.getNextSibling().getFirstChild() != null && fieldNode.getNextSibling().getFirstChild().getNodeType() == Node.TEXT_NODE){
			setOption(options, fieldNode, fieldNode.getNextSibling().getFirstChild().getNodeValue());
		}
	}
	
	private void setOption(List<String> options, Node fieldNode, String newOptionValue){
		if (!options.isEmpty()
				&& fieldNode.getAttributes().getNamedItem("value") != null) {
			String optionValue = fieldNode.getAttributes()
					.getNamedItem("value").getNodeValue();
			int optionIndex = options.indexOf(optionValue);
			if (optionIndex != -1) {
				options.remove(optionIndex);
				options.add(optionIndex, newOptionValue);
			} else {
				options.add(newOptionValue);
			}
		} else {
			options.add(newOptionValue);
		}
	}
	
	private String getDescription(Node fieldNode){
		if (fieldNode.getPreviousSibling() != null && fieldNode.getPreviousSibling().getNodeType() == Node.TEXT_NODE){
			return fieldNode.getPreviousSibling().getNodeValue();
		}
		return new String();
	}

	public static boolean iterateUsedValues(SearchingData searchingData, int startLink) {
		if (startLink == 1){
			List<FormFieldData> fieldDataList = getAllFormFieldData(searchingData
					.getFlowDataList());
			return iterateUsedValues(fieldDataList);
		}else{
			return false;
		}
	}

	private static List<FormFieldData> getAllFormFieldData(
			List<ConfigData> flowDataList) {
		List<FormFieldData> allFieldData = new LinkedList<FormFieldData>();

		for (ConfigData configData : flowDataList) {
			if (configData instanceof FormData) {
				allFieldData.addAll(((FormData) configData).getFields());
			} else if (configData instanceof SwitchData) {
				for (List<ConfigData> flDataList : ((SwitchData) configData)
						.getCases().values()) {
					allFieldData.addAll(getAllFormFieldData(flDataList));
				}
			}
		}
		return allFieldData;
	}

	private static boolean iterateUsedValues(List<FormFieldData> fieldDataList) {
		int i = 0;
		boolean newRetrieving = false;
		boolean iterated = false;
		while (iterated == false && i < fieldDataList.size()) {
			FormFieldData fieldData = fieldDataList.get(i++);
			if (!fieldData.getAlternativeDefaultValues().isEmpty()) {
				if (fieldData.getLastUsedValues() == null || fieldData.getLastUsedValues().isEmpty() || fieldData.getLastUsedValues().equals(
						fieldData.getAlternativeDefaultValues().get(fieldData.getAlternativeDefaultValues().size()-1))) {
					if (fieldData.getLastUsedValues() == null || fieldData.getLastUsedValues().isEmpty()){
						newRetrieving = true;
					}
					fieldData.setLastUsedValues(fieldData
							.getAlternativeDefaultValues().get(0));
				} else {
					fieldData
							.setLastUsedValues(fieldData
									.getAlternativeDefaultValues()
									.get(fieldData
											.getAlternativeDefaultValues()
											.indexOf(
													fieldData.getLastUsedValues()) + 1));
					iterated = true;
				}
			}
		}
		if (iterated == false && newRetrieving == false) { // jeœli przesz³o wszystkie wêz³y resetuj¹c je
									// i wracaj¹c do pocz¹tku
			return true;
		} else {
			return false;
		}
	}

	public static void setDefaultValues(List<FormFieldData> fieldDataList,
			boolean bulkMode) {
		for (FormFieldData fieldData : fieldDataList) {
			List<String> alternativeDefaultValues = fieldData
					.getAlternativeDefaultValues();
			if (!alternativeDefaultValues.isEmpty()) {
				if (alternativeDefaultValues.get(0).equals("$all$")){
					setAllValues(fieldData);
					if (fieldData.getLastUsedValues() == null || fieldData.getLastUsedValues().isEmpty()){
						fieldData.setLastUsedValues(fieldData.getAlternativeDefaultValues().get(0));
					}
				}else if(alternativeDefaultValues.get(0).equals("$every$")){
					setEveryValues(fieldData);
					if (fieldData.getLastUsedValues() == null || fieldData.getLastUsedValues().isEmpty()){
						fieldData.setLastUsedValues(fieldData.getAlternativeDefaultValues().get(0));
					}
				}
				List<String> defaultValues;
				if (bulkMode) {
					String lastUsedValues = fieldData.getLastUsedValues();
					if (lastUsedValues != null && !lastUsedValues.isEmpty()) {
						defaultValues = Arrays.asList(lastUsedValues.split(";"));
					} else {
						defaultValues = Arrays.asList(alternativeDefaultValues
								.get(0).split(";"));
					}
				} else {
					defaultValues = Arrays.asList(alternativeDefaultValues.get(
							0).split(";"));
				}
				defaultValues = renameDefaultValues(fieldData, defaultValues);
				fieldData.setDefaultValues(defaultValues);
			}
		}
	}
	
	private static void setEveryValues(FormFieldData fieldData){
		fieldData.setAlternativeDefaultValues(fieldData.getOptionValues());
	}
	
	private static void setAllValues(FormFieldData fieldData){
		List<String> options = fieldData.getOptionValues();
		if (!options.isEmpty()){
			List<String> allValues = new LinkedList<String> ();
			allValues.add(StringUtils.join(options, ";"));
			fieldData.setAlternativeDefaultValues(allValues);
		}
	}
	
	private static List<String> renameDefaultValues(FormFieldData fieldData, List<String> defaultValues){
		if (fieldData.getOptionsDescriptions().isEmpty()){
			return defaultValues;
		}else{
			List<String> renamedValues = new LinkedList<String>();
			for (String value: defaultValues){
				int optionIndex = fieldData.getOptionsDescriptions().indexOf(value);
				if (optionIndex != -1 && !fieldData.getOptions().isEmpty()){
					renamedValues.add(fieldData.getOptions().get(optionIndex));
				}else{
					renamedValues.add(value);
				}
			}
			return renamedValues;
		}
	}

	public WebResponse submitForm(FormData formData) throws RetrievalException {
		WebForm form = formData.getWebForm();
		for (FormFieldData fieldData : formData.getFields()) {
			fillFormField(fieldData, form);
		}
		try {
			SubmitButton button = null;
			if (formData.getButtonId() != null) {
				button = (SubmitButton) form.getButtonWithID(formData
						.getButtonId());
				if (button == null) {
					throw new RetrievalException("Button with id \'"
							+ formData.getButtonId() + "\' does not exist");
				}
			} else if (formData.getButtonNo() != null) {
				button = (SubmitButton) form.getButtons()[formData
						.getButtonNo()];
				if (button == null) {
					throw new RetrievalException("Button number "
							+ formData.getButtonNo() + " does not exist");
				}
			} else if (formData.getButtonName() != null) {
				if (formData.getButtonName().equals("Submit")) {
					button = null; // jeœli button ma nazwe "Submit" to jest on
									// domyœln¹ form¹ wysy³ania
				} else {
					for (Button btn : form.getButtons()) {
						if (btn.getName().equals(formData.getButtonName())) {
							button = (SubmitButton) btn;
						}
					}

					throw new RetrievalException("Button with name \'"
							+ formData.getButtonName() + "\' does not exist");
				}
			}
			return submit(form, button);
		} catch (Exception e) {// SAXException, IOException
			throw new RetrievalException("Error occured while submiting form");
		}
	}

	private void fillFormField(FormFieldData fieldData, WebForm form) {
		List<Component> jForms = fieldData.getComponents();

		if (fieldData.getFieldType().equals(FormFieldData.FieldType.CHECKBOX)) {
			FormParameter formParameter = fieldData.getFormParameter();
			for (int i = 0; i < jForms.size(); i++) {
				JCheckBox jCheckbox = (JCheckBox) jForms.get(i);
				if (jCheckbox.isSelected()) {
					formParameter.toggleCheckbox(fieldData.getOptions().get(i));
					if (fieldData.getOptionsDescriptions().isEmpty()) {
						fieldData.addValue(fieldData.getOptions().get(i));
					} else {
						fieldData.addValue(fieldData.getOptionsDescriptions()
								.get(i));
					}
				}
			}
		} else if (fieldData.getFieldType().equals(
				FormFieldData.FieldType.COMBOBOX)) {
			JComboBox<String> jComboBox = (JComboBox<String>) jForms.get(0);
			String selectedValue = (String) jComboBox.getSelectedItem();
			fieldData.addValue(selectedValue);
			int selectedIndex = fieldData.getOptionsDescriptions().indexOf(selectedValue);
			if (selectedIndex != -1){
				selectedValue = fieldData.getOptions().get(selectedIndex);
			}
			form.setParameter(getFieldName(fieldData), selectedValue);
		} else if (fieldData.getFieldType().equals(
				FormFieldData.FieldType.RADIO)) {
			for (int i = 0; i < jForms.size(); i++) {
				JRadioButton jRadioBtn = (JRadioButton) jForms.get(i);
				if (jRadioBtn.isSelected()) {
					form.setParameter(getFieldName(fieldData), fieldData
							.getOptions().get(i));
					if (fieldData.getOptionsDescriptions().isEmpty()) {
						fieldData.addValue(fieldData.getOptions().get(i));
					} else {
						fieldData.addValue(fieldData.getOptionsDescriptions()
								.get(i));
					}
				}
			}
		} else if (fieldData.getFieldType().equals(
				FormFieldData.FieldType.SELECT_ONE)) {
			JList<String> jList = (JList<String>) jForms.get(0);
			String selectedValue = jList.getSelectedValue();
			fieldData.addValue(selectedValue);
			int selectedIndex = fieldData.getOptionsDescriptions().indexOf(selectedValue);
			if (selectedIndex != -1){
				selectedValue = fieldData.getOptions().get(selectedIndex);
			}
			form.setParameter(getFieldName(fieldData), selectedValue);
		} else if (fieldData.getFieldType().equals(
				FormFieldData.FieldType.SELECT_MULTIPLE)) {
			JList<String> jList = (JList<String>) jForms.get(0);
			List<String> selectedValues = jList.getSelectedValuesList();
			fieldData.addValues(selectedValues);
			List<String> selectedValuesList = new LinkedList<String>();
			if (!fieldData.getOptionsDescriptions().isEmpty()){
				for (String selectedValue: selectedValues){
					int selectedIndex = fieldData.getOptionsDescriptions().indexOf(selectedValue);
					if (selectedIndex != -1){
						selectedValuesList.add(fieldData.getOptions().get(selectedIndex));
					}
				}
			}else { 
				selectedValuesList = selectedValues;
			}
			form.setParameter(fieldData.getFieldName(), selectedValuesList.toArray(new String[] {}));
		} else if (fieldData.getFieldType()
				.equals(FormFieldData.FieldType.TEXT)) {
			JTextField jTextField = (JTextField) jForms.get(0);
			String value = (String) jTextField.getText();
			form.setParameter(getFieldName(fieldData), value);
			fieldData.addValue(value);
		} else if (fieldData.getFieldType().equals(
				FormFieldData.FieldType.TEXTAREA)) {
			JTextArea jTextArea = (JTextArea) jForms.get(0);
			String value = (String) jTextArea.getText();
			form.setParameter(getFieldName(fieldData),
					(String) jTextArea.getText());
			fieldData.addValue(value);
		}
	}
	
	private String getFieldName(FormFieldData fieldData){
		if (fieldData.getFormFieldName() != null){
			return fieldData.getFormFieldName();
		}else{
			return fieldData.getFieldName();
		}
	}

	private WebResponse submit(WebForm form, SubmitButton btn)
			throws RetrievalException, IOException, SAXException {
		for (int submits = 0; submits < 5; submits++) {
			try {
				if (btn == null) {
					return form.submit();
				} else {
					return form.submit(btn);
				}
			} catch (RuntimeException e) {
				// Do nothing
			}
		}
		throw new RetrievalException("Could not submit form: " + form.getName());
	}
}
