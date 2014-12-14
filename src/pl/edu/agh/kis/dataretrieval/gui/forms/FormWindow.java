package pl.edu.agh.kis.dataretrieval.gui.forms;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import pl.edu.agh.kis.dataretrieval.configuration.search.FormFieldData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormFieldData.FieldType;

public class FormWindow extends JFrame {

	private List<FormFieldData> fields;
	private boolean submited;

	/**
	 * Create the frame.
	 */
	public FormWindow(List<FormFieldData> fields) {
		this.fields = fields;
		setLayout(new GridLayout(getRowsAmount(), 1));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		addForms(fields);
		
		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSubmited();
				closeWindow();
			}
		});
		
		add(btnNewButton);
	}
	
	private void addForms(List<FormFieldData> fields){
		for(FormFieldData field: fields){
			if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.COMBOBOX)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.COMBOBOX))){
				JLabel label = new JLabel(field.getDescription());
				JComboBox<String> combo = new JComboBox<String>();
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					if (optionsDescriptions.size() < i){
						combo.addItem(optionsDescriptions.get(i));
					}else{
						combo.addItem(options.get(i));
					}
				}
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					combo.setSelectedItem(field.getDefaultValue());
				}
				add(label);
				add(combo);
				field.setComponent(combo);
			}else if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.SELECT_ONE)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.SELECT_ONE))){
				JLabel label = new JLabel(field.getDescription());
				JList<String> list = new JList<String>();
				String[] options;
				if (field.getOptionsDescriptions().isEmpty()){
					options = (String[]) field.getOptions().toArray();
				}else{
					options = (String[]) field.getOptionsDescriptions().toArray();
				}
				list.setListData(options);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					list.setSelectedValue(field.getDefaultValue(), true);
				}
				add(label);
				add(list);
				field.setComponent(list);
			}else if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.SELECT_MULTIPLE)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.SELECT_MULTIPLE))){
				JLabel label = new JLabel(field.getDescription());
				JList<String> list = new JList<String>();
				String[] options;
				if (field.getOptionsDescriptions().isEmpty()){
					options = (String[]) field.getOptions().toArray();
				}else{
					options = (String[]) field.getOptionsDescriptions().toArray();
				}
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					for (String value: field.getDefaultValue().split(";")){
						list.setSelectedValue(value, false);
					}
				}
				list.setListData(options);
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				add(label);
				add(list);
				field.setComponent(list);
			}else if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.CHECKBOX)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.CHECKBOX))){
				JLabel label = new JLabel(field.getDescription());
				add(label);
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					JCheckBox checkbox = new JCheckBox();
					if (optionsDescriptions.size() < i){
						checkbox.setText(optionsDescriptions.get(i));
					}else{
						checkbox.setText(options.get(i));
					}
					if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty() && Arrays.asList(field.getDefaultValue().split(";")).contains(checkbox.getText())){
						checkbox.setSelected(true);
					}
					add(checkbox);
					field.addComponent(checkbox);
				}
			}else  if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.RADIO)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.RADIO))){
				JLabel label = new JLabel(field.getDescription());
				add(label);
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					JRadioButton radio = new JRadioButton();
					radio.setEnabled(true);
					if (optionsDescriptions.size() < i){
						radio.setText(optionsDescriptions.get(i));
					}else{
						radio.setText(options.get(i));
					}
					if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty() && field.getDefaultValue().equalsIgnoreCase(radio.getText())){
						radio.setSelected(true);
					}
					add(radio);
					field.addComponent(radio);
				}
			}else if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXT)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.TEXT))){
				JLabel label = new JLabel(field.getDescription());
				JTextField textField = new JTextField();
				add(label);
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					textField.setText(field.getDefaultValue());
				}
				add(textField);
				field.setComponent(textField);
			}else if ((field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXTAREA)) || (field.getFieldType() == null && field.getFieldType().equals(FieldType.TEXTAREA))){
				JLabel label = new JLabel(field.getDescription());
				JTextArea textArea = new JTextArea();
				add(label);
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					textArea.setText(field.getDefaultValue());
				}
				add(textArea);
				field.setComponent(textArea);
			}
		}
	}
	
	private synchronized void setSubmited(){
		submited = true;
		notifyAll();
	}
	
	public synchronized void waitForResult(){
		try {
			while (!submited){
				this.wait();
				System.out.println(21);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private int getRowsAmount(){
		int size = 0;
		for(FormFieldData form: fields){
			size += form.getSize();
		}
		return size + 1;
	}
	
	private void closeWindow(){
		this.dispose();
	}

}
