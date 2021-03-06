package pl.edu.agh.kis.dataretrieval.configuration.searching;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.meterware.httpunit.FormParameter;

public class FormFieldData {

	public enum FieldType {TEXT, TEXTAREA, CHECKBOX, COMBOBOX, SELECT_ONE, SELECT_MULTIPLE, RADIO};
	
//	from configuration file
	private String fieldName;		//nazwa pola w formularzu brana z nazwy w�z�a w pliku xml
	private String formFieldName; //je�li nazwy pola nie da si� ustawi� jako w�ze� xml np. 'field[]'
	private String description = new String();
	private List<String> defaultValues = new ArrayList<String>();
	private List<String> alternativeDefaultValues = new ArrayList<String>();
	private List<String> optionsDescriptions = new ArrayList<String>();
	
//	for bulk run
	private String lastUsedValues = new String();
	
//	from site
	private FieldType fieldType;
	private FormParameter formParameter;
	private List<String> options = new ArrayList<String>();

//	java data
	private String result;
	private List<Component> components = new ArrayList<Component>();
	
//	after retrieval
	private List<String> values = new LinkedList<String>();
	
	public FormFieldData(){
		super();
	}
	
	public FormFieldData(String fieldName, List<String> options) {
		super();
		this.fieldName = fieldName;
		this.options = options;
	}

	public FormFieldData(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	public FormFieldData(String fieldName, String description) {
		super();
		this.fieldName = fieldName;
		this.description = description;
	}

	public FormFieldData(String fieldName, String description, List<String> options) {
		super();
		this.fieldName = fieldName;
		this.description = description;
		this.options = options;
	}

	public int getSize(){
		if(fieldType.equals(FieldType.CHECKBOX) || fieldType.equals(FieldType.SELECT_ONE) || fieldType.equals(FieldType.SELECT_MULTIPLE) || fieldType.equals(FieldType.RADIO)){
			return (options.size() > 8 ? 8 : options.size()) + (description.isEmpty() ? 0 : 1);
		}if (fieldType.equals(FieldType.TEXTAREA)) {
			return 5 + (description.isEmpty() ? 0 : 1);
		}else{
			return 1 + (description.isEmpty() ? 0 : 1);
		}
	}
	
	public List<Component> getComponents(){
		return components;
	}
	
	public void addComponent(Component component){
		this.components.add(component);
	}
	
	public Component getComponent() {
		return components.get(0);
	}
	
	public void setComponent(Component component) {
		if (components.size() > 0){
			components.set(0, component);
		}else{
			components.add(component);
		}
	}
	
	public void clearComponents(){
		components.clear();
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public String getFormFieldName() {
		return formFieldName;
	}

	public void setFormFieldName(String formFieldName) {
		this.formFieldName = formFieldName;
	}

	public String getLastUsedValues() {
		return lastUsedValues;
	}

	public void setLastUsedValues(String lastUsedValue) {
		this.lastUsedValues = lastUsedValue;
	}
	public List<String> getDefaultValues() {
		return defaultValues;
	}

	public void addDefaultValue(String defaultValue) {
		this.defaultValues.add(defaultValue);
	}

	public void addDefaultValues(List<String> defaultValues) {
		this.defaultValues.addAll(defaultValues);
	}
	
	public void setDefaultValues(List<String> defaultValues) {
		this.defaultValues = defaultValues;
	}
	
	public List<String> getAlternativeDefaultValues() {
		return alternativeDefaultValues;
	}

	public void addAlternativeDefaultValue(String defaultValue) {
		this.alternativeDefaultValues.add(defaultValue);
	}

	public void addAlternativeDefaultValues(List<String> defaultValues) {
		this.alternativeDefaultValues.addAll(defaultValues);
	}
	
	public void setAlternativeDefaultValues(List<String> defaultValues) {
		alternativeDefaultValues.clear();
		addAlternativeDefaultValues(defaultValues);
	}

	public FormParameter getFormParameter() {
		return formParameter;
	}

	public void setFormParameter(FormParameter formParameter) {
		this.formParameter = formParameter;
	}

	public List<String> getOptionsDescriptions() {
		return optionsDescriptions;
	}

	public void setOptionsDescriptions(List<String> optionsDescriptions) {
		this.optionsDescriptions = optionsDescriptions;
	}
	
	public void addOptionsDescriptions(Collection<String> optionsDescriptions) {
		this.optionsDescriptions.addAll(optionsDescriptions);
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
	public void addValue(String value) {
		this.values.add(value);
	}
	public void addValues(List<String> values) {
		this.values.addAll(values);
	}
	public void clearValues(){
		values.clear();
	}
	
	public List<String> getOptionValues(){
		if (!optionsDescriptions.isEmpty()){
			return optionsDescriptions;
		}else{
			return options;
		}
	}
}
