package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.meterware.httpunit.FormParameter;

public class FormFieldData {

	public enum FieldType {TEXT, TEXTAREA, CHECKBOX, COMBOBOX, SELECT_ONE, SELECT_MULTIPLE, RADIO};
	
//	from configuration file
	private String fieldName;		//nazwa pola w formularzu brana z nazwy wêz³a w pliku xml
	private String formFieldName; //jeœli nazwy pola nie da siê ustawiæ jako wêze³ xml np. 'field[]'
	private String description;
	private String defaultValue;
	private List<String> optionsDescriptions;
	
//	for bulk run
	private List<String> usedValues = new ArrayList<String>();
	
//	from site
	private FieldType fieldType;
	private FormParameter formParameter;
	private List<String> options = new ArrayList<String>();

//	java data
	private String result;
	private List<Component> components = new ArrayList<Component>();
	
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
		if(fieldType.equals(FieldType.CHECKBOX) || fieldType.equals(FieldType.SELECT_ONE) || fieldType.equals(FieldType.RADIO)){
			return options.size() + (description.isEmpty() ? 0 : 1);
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

	public List<String> getUsedValues() {
		return usedValues;
	}

	public void setUsedValues(List<String> usedValues) {
		this.usedValues = usedValues;
	}
	
	public void addUsedValues(Collection<String> usedValues){
		this.usedValues.addAll(usedValues);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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
	
}
