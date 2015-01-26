package pl.edu.agh.kis.dataretrieval.gui.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormFieldData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormFieldData.FieldType;

public class FormWindow extends JFrame {

	private List<FormFieldData> fields;
	private boolean submited;

	/**
	 * Create the frame.
	 * @throws RetrievalException 
	 */
	public FormWindow(List<FormFieldData> fields) throws RetrievalException {
		setTitle("Form");
		this.fields = fields;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, getRowsAmount() * 25);
		
		addForms(fields);
	}
	
	private void addForms(List<FormFieldData> fields) throws RetrievalException{
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		
		setLayout(gridbag);
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.WEST;
		int counter = 0;
		for(FormFieldData field: fields){
			field.clearComponents();
			field.clearValues();
			if(!field.getDescription().isEmpty()){
				JLabel label = new JLabel(field.getDescription());
				constraints.gridy = counter++;
				add(label, constraints);
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
				if (!field.getDefaultValues().isEmpty()){
					List<String> defaultValues = field.getDefaultValues();
					if (defaultValues.size() == 1){
						String value = defaultValues.get(0);
						if (field.getOptionsDescriptions().contains(value) || field.getOptions().contains(value)){
							combo.setSelectedItem(value);
						}else{
							throw new RetrievalException("Default value \'" + value + "\' is invalid");
						}
					}else{
						throw new RetrievalException("Multiple default values are invalid for combo box");
					}
				}
				
				constraints.gridy = counter++;
				add(combo, constraints);
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
				if (!field.getDefaultValues().isEmpty()){
					List<String> defaultValues = field.getDefaultValues();
					if (defaultValues.size() == 1){
						String value = defaultValues.get(0);
						if (field.getOptions().contains(value)){
							list.setSelectedIndex(field.getOptions().indexOf(value));
						}else if (field.getOptionsDescriptions().contains(value)){
							list.setSelectedIndex(field.getOptionsDescriptions().indexOf(value));
						}else{
							throw new RetrievalException("Default value \'" + value + "\' is invalid");
						}
					}else{
						throw new RetrievalException("Multiple default values are invalid for single selection list");
					}
				}else{
					list.setSelectedIndex(1);
				}
				JScrollPane jsp = new JScrollPane(list);
				counter += field.getSize() - (field.getDescription().isEmpty() ? 0 : 1);
				constraints.gridy = counter;
				gridbag.setConstraints(jsp, constraints);
				add(jsp, constraints);
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
				list.setListData(options);
				list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				if (!field.getDefaultValues().isEmpty()){
					for (String value: field.getDefaultValues()){
						if (field.getOptions().contains(value)){
							list.setSelectedIndex(field.getOptions().indexOf(value));
						}else if (field.getOptionsDescriptions().contains(value)){
							list.setSelectedIndex(field.getOptionsDescriptions().indexOf(value));
						}else{
							throw new RetrievalException("Default value \'" + value + "\' is invalid");
						}
					}
				}else{
					list.setSelectedIndex(0);
				}
				JScrollPane jsp = new JScrollPane(list);
				counter += field.getSize() - (field.getDescription().isEmpty() ? 0 : 1);
				constraints.gridy = counter;
				gridbag.setConstraints(jsp, constraints);
				add(jsp, constraints);
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
					if (!field.getDefaultValues().isEmpty() && (field.getDefaultValues().contains(optionsDescriptions.get(i)) || field.getDefaultValues().contains(options.get(i)))){
						checkbox.setSelected(true);
					}
					constraints.gridy = counter++;
					add(checkbox, constraints);
					field.addComponent(checkbox);
				}
			}else  if (field.getFieldType() != null && field.getFieldType().equals(FieldType.RADIO)){
				ButtonGroup bg = new ButtonGroup();
				List<String> options = field.getOptions();
				List<String> optionsDescriptions = field.getOptionsDescriptions();
				
				List<String> defaultValues = field.getDefaultValues();
				if (defaultValues.size() > 1){
					throw new RetrievalException("Multiple default values are invalid for radio button");
				}
				for (int i=0; i < options.size(); i++){
					JRadioButton radio = new JRadioButton();
					radio.setEnabled(true);
					if (i < optionsDescriptions.size()){
						radio.setText(optionsDescriptions.get(i));
					}else{
						radio.setText(options.get(i));
					}
						
					if (!field.getDefaultValues().isEmpty()){
						String value = defaultValues.get(0);
						if (value.equals(options.get(i)) || value.equals(optionsDescriptions.get(i))){
							radio.setSelected(true);
						}
					}else {
						if (i == 0){
							radio.setSelected(true);
						}
					}
					constraints.gridy = counter++;
					add(radio, constraints);
					bg.add(radio);
					field.addComponent(radio);
				}
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXT)){
				JTextField textField = new JTextField();
				if (!field.getDefaultValues().isEmpty()){
					List<String> defaultValues = field.getDefaultValues();
					if (defaultValues.size() == 1){
						String value = defaultValues.get(0);
						if (field.getOptionsDescriptions().contains(value) || field.getOptions().contains(value)){
							textField.setText(value);
						}else{
							throw new RetrievalException("Default value \'" + value + "\' is invalid");
						}
					}else{
						throw new RetrievalException("Multiple default values are invalid for single selection list");
					}
				}
				constraints.gridy = counter++;
				add(textField, constraints);
				field.setComponent(textField);
			}else if (field.getFieldType() != null && field.getFieldType().equals(FieldType.TEXTAREA)){
				JTextArea textArea = new JTextArea();
				if (!field.getDefaultValues().isEmpty()){
					List<String> defaultValues = field.getDefaultValues();
					if (defaultValues.size() == 1){
						String value = defaultValues.get(0);
						if (field.getOptionsDescriptions().contains(value) || field.getOptions().contains(value)){
							textArea.setText(value);
						}else{
							throw new RetrievalException("Default value \'" + value + "\' is invalid");
						}
					}else{
						throw new RetrievalException("Multiple default values are invalid for single selection list");
					}
				}
				JScrollPane jsp = new JScrollPane(textArea);
				constraints.gridy = counter++;
				gridbag.setConstraints(jsp, constraints);
				add(jsp, constraints);
				field.setComponent(textArea);
			}
		}
		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSubmited();
				closeWindow();
			}
		});
		constraints.gridy = counter++;
		add(btnNewButton, constraints);
	}
	
	private synchronized void setSubmited(){
		submited = true;
		notifyAll();
	}
	
	public synchronized void waitForSubmit() throws InterruptedException{
		while (!submited){
			this.wait();
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
