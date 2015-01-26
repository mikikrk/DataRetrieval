package pl.edu.agh.kis.dataretrieval.gui.windows;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

public class YesNoDialog extends JDialog {

	private boolean result;
	private boolean resultSet = false;

	/**
	 * Create the dialog.
	 */
	public YesNoDialog(String message) {
		setBounds(100, 100, 450, 110);
		
		JButton yesBtn = new JButton("Yes");
		yesBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setYesNoResult(true);
				closeWindow();
			}
		});
		yesBtn.setActionCommand("Yes");
		
		JButton noBtn = new JButton("No");
		noBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setYesNoResult(false);
				closeWindow();
			}
		});
		noBtn.setActionCommand("No");
		
		JLabel messageLabel = new JLabel(message);
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(171, Short.MAX_VALUE)
					.addComponent(yesBtn, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(noBtn, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)
					.addGap(164))
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(messageLabel)
					.addContainerGap(378, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(messageLabel)
					.addPreferredGap(ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(yesBtn)
						.addComponent(noBtn))
					.addContainerGap())
		);
		getContentPane().setLayout(groupLayout);
	}
	
	private synchronized void setYesNoResult(boolean result){
		resultSet = true;
		this.result = result;
		notifyAll();
	}
	
	public synchronized boolean isYes(){
		while(!resultSet){
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		return result;
	}
	
	private void closeWindow(){
		this.dispose();
	}
}
