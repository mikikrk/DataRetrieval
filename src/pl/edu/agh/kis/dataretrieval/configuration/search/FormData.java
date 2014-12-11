package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.List;

public class FormData {

	public enum FieldType {TEXT, TEXTAREA, CHECKBOX, COMBOBOX, LIST, RADIO};
	
//	from configuration file
	private FieldType javaFieldType;
	private String fieldName;
	private String description;
	
//	from site
	private FieldType htmlFieldType;
	private List<String> options = new ArrayList<String>();

//	java data
	private String result;
	private List<Object> jForms = new ArrayList<Object>();
	
	
	
	public FormData(String fieldName, List<String> options) {
		super();
		this.fieldName = fieldName;
		this.options = options;
	}

	public FormData(String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	public FormData(FieldType javaFieldType, String fieldName) {
		super();
		this.javaFieldType = javaFieldType;
		this.fieldName = fieldName;
	}

	public FormData(String fieldName, String description) {
		super();
		this.fieldName = fieldName;
		this.description = description;
	}

	public FormData(String fieldName, String description, List<String> options) {
		super();
		this.fieldName = fieldName;
		this.description = description;
		this.options = options;
	}

	public FormData(FieldType javaFieldType, String fieldName,
			String description, List<String> options) {
		super();
		this.javaFieldType = javaFieldType;
		this.fieldName = fieldName;
		this.description = description;
		this.options = options;
	}

	public int getSize(){
		if(javaFieldType.equals(FieldType.CHECKBOX) || javaFieldType.equals(FieldType.LIST) || javaFieldType.equals(FieldType.RADIO)){
			return options.size() + (description.isEmpty() ? 0 : 1);
		}else{
			return 1 + (description.isEmpty() ? 0 : 1);
		}
	}
	
	public List<Object> getjForms(){
		return jForms;
	}
	
	public void addjForm(Object jForm){
		this.jForms.add(jForm);
	}
	
	public Object getjForm() {
		return jForms.get(0);
	}
	
	public void setjForm(Object jForm) {
		if (jForms.size() > 0){
			jForms.set(0, jForm);
		}else{
			jForms.add(jForm);
		}
	}

	public FieldType getJavaFieldType() {
		return javaFieldType;
	}

	public void setJavaFieldType(FieldType javaFieldType) {
		this.javaFieldType = javaFieldType;
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

	public FieldType getHtmlFieldType() {
		return htmlFieldType;
	}

	public void setHtmlFieldType(FieldType htmlFieldType) {
		this.htmlFieldType = htmlFieldType;
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

	public void setjForms(List<Object> jForms) {
		this.jForms = jForms;
	}
	
}
