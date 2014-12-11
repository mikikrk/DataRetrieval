package pl.edu.agh.kis.dataretrieval.gui.forms;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import pl.edu.agh.kis.dataretrieval.configuration.search.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormData.FieldType;

public class FormWindow extends JFrame {

	private JTextField textField;
	private JTextArea textArea;
	private JLabel label;
	private JCheckBox checkbox;
	private JRadioButton radio;
	private JList<String> list;
	private JComboBox<String> combo;
	private List<FormData> forms;
	private boolean submited;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FormWindow(List<FormData> forms) {
		this.forms = forms;
		setLayout(new GridLayout(getRowsAmount(), 1));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		addForms(forms);
		
		JButton btnNewButton = new JButton("Submit");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSubmited();
			}
		});
		
		add(btnNewButton);
	}
	
	private void addForms(List<FormData> forms){
		for(FormData form: forms){
			if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.COMBOBOX)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.COMBOBOX))){
				label = new JLabel(form.getDescription());
				combo = new JComboBox<String>();
				for (String value: form.getOptions()){
					combo.addItem(value);
				}
				add(label);
				add(combo);
				form.setjForm(combo);
			}else if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.LIST)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.LIST))){
				label = new JLabel(form.getDescription());
				list = new JList<String>();
				list.setListData((String[]) form.getOptions().toArray());
				add(label);
				add(list);
				form.setjForm(list);
			}else if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.CHECKBOX)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.CHECKBOX))){
				label = new JLabel(form.getDescription());
				add(label);
				for (String value: form.getOptions()){
					checkbox = new JCheckBox();
					checkbox.setText(value);
					add(checkbox);
					form.addjForm(checkbox);
				}
			}else  if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.RADIO)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.RADIO))){
				label = new JLabel(form.getDescription());
				add(label);
				for (String value: form.getOptions()){
					radio = new JRadioButton();
					radio.setEnabled(true);
					radio.setText("1");
					add(radio);
					form.addjForm(radio);
				}
			}else if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.TEXT)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.TEXT))){
				label = new JLabel(form.getDescription());
				textField = new JTextField();
				add(label);
				add(textField);
				form.setjForm(textField);
			}else if ((form.getJavaFieldType() != null && form.getJavaFieldType().equals(FieldType.TEXTAREA)) || (form.getJavaFieldType() == null && form.getHtmlFieldType().equals(FieldType.TEXTAREA))){
				label = new JLabel(form.getDescription());
				textArea = new JTextArea();
				add(label);
				add(textArea);
				form.setjForm(textArea);
			}
		}
	}
	
	private synchronized void setSubmited(){
		submited = true;
		notifyAll();
	}
	
	public synchronized List<FormData> getResult(){
		try {
			while (!submited){
				this.wait();
				System.out.println(21);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return forms;
	}
	
	private int getRowsAmount(){
		int size = 0;
		for(FormData form: forms){
			size += form.getSize();
		}
		return size + 1;
	}

}
