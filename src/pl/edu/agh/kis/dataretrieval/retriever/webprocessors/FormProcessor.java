package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

import java.awt.Component;
import java.io.IOException;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormFieldData;
import pl.edu.agh.kis.dataretrieval.gui.windows.FormWindow;

import com.meterware.httpunit.Button;
import com.meterware.httpunit.FormControl;
import com.meterware.httpunit.FormParameter;
import com.meterware.httpunit.RadioButtonFormControl;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

public class FormProcessor {

	public void loadForm(FormData formData, WebResponse resp) throws SAXException, RetrievalException{
		WebForm form = getWebForm(formData, resp);
		formData.setWebForm(form);
		
		for (FormFieldData fieldData: formData.getFields()){
			loadField(fieldData, form);
		}
	}
	
	private WebForm getWebForm(FormData formData, WebResponse resp) throws SAXException, RetrievalException{
		WebForm form;
		if (formData.getNo() != null){
			form = resp.getForms()[formData.getNo()];
			if (form == null){
				throw new RetrievalException("Form no " + formData.getNo() + " does not exist");
			}
		}else if (formData.getFormId() != null){
			form = resp.getFormWithID(formData.getFormId());
			if (form == null){
				throw new RetrievalException("Form with id " + formData.getFormId() + " does not exist");
			}
		}else if (formData.getFormName() != null){
			form = resp.getFormWithName(formData.getFormName());
			if (form == null){
				throw new RetrievalException("Form \'" + formData.getFormName() + "\' does not exist");
			}
		}else {
			throw new RetrievalException("No form identifier");
		}
		return form;
	}
	
	private void loadField(FormFieldData fieldData, WebForm form){
		String fieldName = fieldData.getFormFieldName() != null ? fieldData.getFormFieldName() : fieldData.getFieldName();
		FormParameter formParameter = form.getParameter(fieldName);
		fieldData.setFormParameter(formParameter);
		
		FormFieldData.FieldType fieldType = getFieldType(formParameter);
		fieldData.setFieldType(fieldType);
		setFieldOptions(formParameter, fieldType, fieldData.getOptions());
	}
	
	private FormFieldData.FieldType getFieldType(FormParameter formParameter){
		FormControl[] formControls = formParameter.getControls();
		String htmlFieldType = formControls[0].getType();
		FormFieldData.FieldType fieldType = null;
		if (htmlFieldType.equals("text")){
			fieldType = FormFieldData.FieldType.TEXT;
		}else if (htmlFieldType.equals("textarea")){
			fieldType = FormFieldData.FieldType.TEXTAREA;
		}else if (htmlFieldType.equals("checkbox")){
			fieldType = FormFieldData.FieldType.CHECKBOX;
		}else if (htmlFieldType.equals("select-one")){
			if (formControls[0].getAttribute("size").isEmpty()){
				fieldType = FormFieldData.FieldType.COMBOBOX;
			}else{
				fieldType = FormFieldData.FieldType.SELECT_ONE;
			}
		}else if (htmlFieldType.equals("select-multiple")){
			fieldType = FormFieldData.FieldType.SELECT_MULTIPLE;
		}else if (htmlFieldType.equals("radio") || htmlFieldType.equalsIgnoreCase("undefined")){
			fieldType = FormFieldData.FieldType.RADIO;
		}
		return fieldType;
	}
	
	private void setFieldOptions(FormParameter formParameter, FormFieldData.FieldType fieldType, List<String> options){
		if (fieldType.equals(FormFieldData.FieldType.COMBOBOX) || fieldType.equals(FormFieldData.FieldType.SELECT_ONE) || fieldType.equals(FormFieldData.FieldType.SELECT_MULTIPLE) || fieldType.equals(FormFieldData.FieldType.CHECKBOX) || fieldType.equals(FormFieldData.FieldType.RADIO)){
			for (FormControl formControl: formParameter.getControls()){
				for (String value: formControl.getOptionValues()){
					options.add(value);
				}
			}
		}
	}
	
	public WebResponse submitForm(FormData formData) throws RetrievalException{
		WebForm form = formData.getWebForm();
		for (FormFieldData fieldData: formData.getFields()){
			fillFormField(fieldData, form);
		}
		try{
			SubmitButton button;
			if (formData.getButtonId() != null){
				button = (SubmitButton) form.getButtonWithID(formData.getButtonId());
				if (button == null){
					throw new RetrievalException("Button with id \'" + formData.getButtonId() + "\' does not exist");
				}
				return form.submit(button);
			}else if (formData.getButtonNo() != null){
				button = (SubmitButton) form.getButtons()[formData.getButtonNo()];
				if (button == null){
					throw new RetrievalException("Button number " + formData.getButtonNo() + " does not exist");
				}
				return form.submit(button);
			}else if (formData.getButtonName() != null){
				if (formData.getButtonName().equals("Submit")){
					return form.submit();
				}else{
					for (Button btn: form.getButtons()){
						if (btn.getName().equals(formData.getButtonName())){
							button = (SubmitButton) btn;
							return form.submit(button);
						}
					}
					
					throw new RetrievalException("Button with name \'" + formData.getButtonName() + "\' does not exist");
				}
			}else{
				return form.submit();
			}
		}catch (Exception e) {//SAXException, IOException
			throw new RetrievalException("Error occured while submiting form");
		}
	}
	
	private void fillFormField(FormFieldData fieldData, WebForm form){
		List<Component> jForms = fieldData.getComponents();
		
		if (fieldData.getFieldType().equals(FormFieldData.FieldType.CHECKBOX)){
			FormParameter formParameter = fieldData.getFormParameter();
			for (int i = 0; i < jForms.size(); i++){
				JCheckBox jCheckbox = (JCheckBox) jForms.get(i);
				if (jCheckbox.isSelected()){
					formParameter.toggleCheckbox(fieldData.getOptions().get(i));
				}
			}
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.COMBOBOX)){
			JComboBox<String> jComboBox = (JComboBox<String>) jForms.get(0);
			form.setParameter(fieldData.getFieldName(), (String) jComboBox.getSelectedItem());
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.RADIO)){
			for (int i = 0; i < jForms.size(); i++){
				JRadioButton jRadioBtn = (JRadioButton) jForms.get(i);
				if (jRadioBtn.isSelected()){
					form.setParameter(fieldData.getFieldName(), fieldData.getOptions().get(i));;
				}
			}
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.SELECT_ONE)){
			JList<String> jList = (JList<String>) jForms.get(0);
			form.setParameter(fieldData.getFieldName(), jList.getSelectedValue());
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.SELECT_MULTIPLE)){
			JList<String> jList = (JList<String>) jForms.get(0);
			form.setParameter(fieldData.getFieldName(), jList.getSelectedValuesList().toArray(new String[]{}));
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.TEXT)){
			JTextField jTextField = (JTextField) jForms.get(0);
			form.setParameter(fieldData.getFieldName(), (String) jTextField.getText());
		}else if(fieldData.getFieldType().equals(FormFieldData.FieldType.TEXTAREA)){
			JTextArea jTextArea = (JTextArea) jForms.get(0);
			form.setParameter(fieldData.getFieldName(), (String) jTextArea.getText());
		}
	}
}
