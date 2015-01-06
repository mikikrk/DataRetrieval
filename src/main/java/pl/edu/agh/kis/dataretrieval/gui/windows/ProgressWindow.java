package pl.edu.agh.kis.dataretrieval.gui.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.ws.Holder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressWindow extends JFrame {

	private JPanel contentPane;
	private JTextPane textPane;
	private boolean enabled = false;
	private boolean paused = false;
	private Holder<Boolean> keepRetrieving;

	public ProgressWindow(boolean enabled, Holder<Boolean> keepRetrieving) throws HeadlessException {
		this.enabled = enabled;
		if (enabled){
			init();
			this.keepRetrieving = keepRetrieving;
		}
	}

	/**
	 * Create the frame.
	 */
	public void init() {
		setTitle("Retreiving progress");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 650);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		JScrollPane scrollPane = new JScrollPane();

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				keepRetrieving.value = false;
				closeWindow();
			}
		});
		btnStop.setPreferredSize(new Dimension(79, 25));
		btnStop.setMinimumSize(new Dimension(79, 25));
		btnStop.setMaximumSize(new Dimension(79, 25));

		JButton btnPause = new JButton("Pause");
		btnPause.setPreferredSize(new Dimension(79, 25));
		btnPause.setMinimumSize(new Dimension(79, 25));
		btnPause.setMaximumSize(new Dimension(79, 25));
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				paused = true;
			}
		});

		JButton btnResume = new JButton("Resume");
		btnResume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resume();
			}
		});

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.LEADING)
														.addComponent(
																scrollPane,
																GroupLayout.DEFAULT_SIZE,
																448,
																Short.MAX_VALUE)
														.addGroup(
																gl_contentPane
																		.createSequentialGroup()
																		.addComponent(
																				btnStop,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnPause,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				btnResume)))
										.addContainerGap()));
		gl_contentPane
				.setVerticalGroup(gl_contentPane
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								Alignment.TRAILING,
								gl_contentPane
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(scrollPane,
												GroupLayout.DEFAULT_SIZE, 529,
												Short.MAX_VALUE)
										.addPreferredGap(
												ComponentPlacement.UNRELATED)
										.addGroup(
												gl_contentPane
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																btnStop,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																btnPause,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(btnResume))
										.addContainerGap()));
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		contentPane.setLayout(gl_contentPane);
	}

	public void printInfo(String message) {
		appendToPane("[INFO] " + message, Color.BLACK);
		System.out.println("[INFO] " + message);
	}

	public void printError(String message) {
		appendToPane("[ERROR] " + message, Color.RED);
		System.err.println("[ERROR] " + message);
	}

	public void printWarning(String message){
		appendToPane("[WARN] " + message, Color.MAGENTA);
		System.err.println("[WARN] " + message);
	}

	private void appendToPane(String message, Color c) {
		if (enabled){
			textPane.setEditable(true);
			if (!message.endsWith("\n")) {
				message += "\n";
			}
			StyleContext sc = StyleContext.getDefaultStyleContext();
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
					StyleConstants.Foreground, c);
	
			int len = textPane.getDocument().getLength();
			textPane.setCaretPosition(len);
			textPane.setCharacterAttributes(aset, false);
			textPane.replaceSelection(message);
			textPane.setEditable(false);
		}
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public synchronized void pause() {
		while (paused){
			try {
				this.wait();
			} catch (InterruptedException e) {
				printError("Error while pausing");
			}
		}
	}
	
	private synchronized void resume(){
		paused = false;
		this.notifyAll();
	}
	
	private void closeWindow(){
		this.dispose();
	}
}