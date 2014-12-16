package pl.edu.agh.kis.dataretrieval.gui.windows;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
		setBounds(100, 100, 450, getRowsAmount() * 30);
		
		addForms(fields);
		
		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSubmited();
				closeWindow();
			}
		});
		add(new JLabel());
		add(btnNewButton);
	}
	
	private void addForms(List<FormFieldData> fields){
		for(FormFieldData field: fields){
			if(!field.getDescription().isEmpty()){
				JLabel label = new JLabel(field.getDescription());
				add(label);
			}
			if (field.getFieldType() != null && field.getFieldType().equals(FieldType.COMBOBOX)){
				JComboBox<String> combo = new JComboBox<String>();
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					if (i < optionsDescriptions.size()){
						combo.addItem(optionsDescriptions.get(i));
					}else{
						combo.addItem(options.get(i));
					}
				}
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					combo.setSelectedItem(field.getDefaultValue());
				}
				add(combo);
				field.setComponent(combo);
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.SELECT_ONE)){
				JList<String> list = new JList<String>(new DefaultListModel<String>());
				list.setSize(new Dimension(450, field.getSize() * 1000));
				String[] options;
				if (field.getOptionsDescriptions().isEmpty()){
					options = field.getOptions().toArray(new String[]{});
				}else{
					options = field.getOptionsDescriptions().toArray(new String[]{});
				}
				list.setListData(options);
				list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					list.setSelectedValue(field.getDefaultValue(), true);
				}
				JScrollPane jsp = new JScrollPane(list);
				add(jsp);
				field.setComponent(list);
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.SELECT_MULTIPLE)){
				JList<String> list = new JList<String>(new DefaultListModel<String>());
				list.setSize(new Dimension(450, field.getSize() * 1000));
				String[] options;
				if (field.getOptionsDescriptions().isEmpty()){
					options = field.getOptions().toArray(new String[]{});
				}else{
					options = field.getOptionsDescriptions().toArray(new String[]{});
				}
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					for (String value: field.getDefaultValue().split(";")){
						list.setSelectedValue(value, false);
					}
				}
				list.setListData(options);
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				JScrollPane jsp = new JScrollPane(list);
				add(jsp);
				field.setComponent(list);
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.CHECKBOX)){
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					JCheckBox checkbox = new JCheckBox();
					if (i < optionsDescriptions.size()){
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
			}else  if (field.getFieldType() != null && field.getFieldType().equals(FieldType.RADIO)){
				ButtonGroup bg = new ButtonGroup();
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				for (int i=0; i < options.size(); i++){
					JRadioButton radio = new JRadioButton();
					radio.setEnabled(true);
					if (i < optionsDescriptions.size()){
						radio.setText(optionsDescriptions.get(i));
					}else{
						radio.setText(options.get(i));
					}
					if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty() && field.getDefaultValue().equalsIgnoreCase(radio.getText())){
						radio.setSelected(true);
					}
					if (i == 0){
						add(radio, true);
					}else{
						add(radio);
					}
					bg.add(radio);
					field.addComponent(radio);
				}
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXT)){
				JTextField textField = new JTextField();
				if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()){
					textField.setText(field.getDefaultValue());
				}
				add(textField);
				field.setComponent(textField);
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXTAREA)){
				JTextArea textArea = new JTextArea();
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
	
	public synchronized void waitForSubmit(){
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
		return size + 2;	//+2 dla przerwy przed buttonem i dla buttona
	}
	
	private void closeWindow(){
		this.dispose();
	}

}
