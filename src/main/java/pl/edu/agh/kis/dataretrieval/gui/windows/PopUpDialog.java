package pl.edu.agh.kis.dataretrieval.gui.windows;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class PopUpDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private final int LETTER_WIDTH = 8;
	private final int LETTER_HEIGHT = 19;
	private final int STANDARD_HEIGHT = 90;
	
	private String message;
	private boolean error = false;
	
	/**
	 * Create the dialog.
	 */
	public PopUpDialog(String message, boolean error){
		this.message = message;
		this.error = error;
		init();
	}
	public PopUpDialog(String message) {
		this.message = message;
		init();
	}
	
	private void init(){
		if (error){
			System.err.println(message);
		}else{
			System.out.println(message);
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setDialogBounds(message);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblKonfiguracjaOPodanej = new JLabel(converToHTML(message));
			contentPanel.add(lblKonfiguracjaOPodanej);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						closeDialog();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		this.setVisible(true);
	}
	
	private void setDialogBounds(String message){
		String[] lines = message.split("\n");
		
		int linesAmount = lines.length;
		int longestLineSize = 0;

		if (linesAmount == 0){
			linesAmount = 1;
			longestLineSize = message.length();
		}else{
			for (String line: lines){
				if (longestLineSize < line.length()){
					longestLineSize = line.length();
				}
			}
		}
		setBounds(100, 100, longestLineSize * LETTER_WIDTH, linesAmount * LETTER_HEIGHT + STANDARD_HEIGHT);
	}
	
	/**
	 * Dla wyswietlenia wielu linii
	 */
	private String converToHTML(String message){
		return "<html>" + message.replaceAll("\n", "<br>") + "</html>";
	}
	
	private void closeDialog(){
		this.dispose();
	}

}
