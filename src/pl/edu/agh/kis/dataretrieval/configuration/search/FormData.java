package pl.edu.agh.kis.dataretrieval.configuration.search;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;

import com.meterware.httpunit.WebForm;

public class FormData implements ConfigData{
	private Integer no;
	private String formName;
	private String formId;
	
	private Integer buttonNo;
	private String buttonName;
	private String buttonId;
	
	private WebForm webForm;
	
	private List<FormFieldData> fields = new ArrayList<FormFieldData>();
	
	private int index = 0;
	
	public Integer getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no - 1;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}
	public List<FormFieldData> getFields() {
		return fields;
	}
	public void setFields(List<FormFieldData> fields) {
		this.fields = fields;
	}
	public void addFieldData(FormFieldData fieldData){
		fields.add(fieldData);
	}
	
	public boolean hasNextField(){
		return fields.get(index + 1) == null ? false : true;
	}
	
	public Object nextFlowData(){
		return fields.get(index++);
	}
	
	public void reset(){
		index = 0;
	}
	public Integer getButtonNo() {
		return buttonNo;
	}
	public void setButtonNo(int buttonNo) {
		this.buttonNo = buttonNo - 1;
	}
	public String getButtonName() {
		return buttonName;
	}
	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}
	public String getButtonId() {
		return buttonId;
	}
	public void setButtonId(String buttonId) {
		this.buttonId = buttonId;
	}
	public WebForm getWebForm() {
		return webForm;
	}
	public void setWebForm(WebForm webForm) {
		this.webForm = webForm;
	}
}
