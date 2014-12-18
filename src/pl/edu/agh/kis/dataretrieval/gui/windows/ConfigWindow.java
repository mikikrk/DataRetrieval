package pl.edu.agh.kis.dataretrieval.gui.windows;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper;
import pl.edu.agh.kis.dataretrieval.gui.FilePathHolder;

public class ConfigWindow extends JFrame {

	
	private JPanel contentPane;
	private ConfigHelper.Type type;
	private ConfigHelper.ConfigType configType;
	
	private JTextPane configNamePane;
	private JTextArea configXml;
	private JScrollPane configScroll;
	
	private FilePathHolder configPath;
	private StartWindow startWindow;

	/**
	 * Create the frame.
	 * @wbp.parser.constructor
	 */
	public ConfigWindow(StartWindow startWindow, ConfigHelper.Type type, ConfigHelper.ConfigType configType) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.startWindow = startWindow;
		this.type = type;
		this.configType = configType;
		prepareConfigWindow();
	}
	
	public ConfigWindow(StartWindow startWindow, ConfigHelper.Type type, ConfigHelper.ConfigType configType, FilePathHolder configPath) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.startWindow = startWindow;
		this.type = type;
		this.configType = configType;
		this.configPath = configPath;
		prepareConfigWindow();
		
		displayConfigName(configPath.getFilename());
		displayConfigFile(configPath.getFilePath());
		if (type.equals(ConfigHelper.Type.DISPLAY)){
			configXml.setEditable(false);
		}
	}
	
	private void prepareConfigWindow(){
		setBounds(100, 100, 600, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel configNameLbl = new JLabel("Configuration's name");
		
		configNamePane = new JTextPane();
		
		configXml = new JTextArea();
		
		configScroll = new JScrollPane(configXml);
		
		JLabel lblNewLabel = new JLabel("Configuration");
		
		JButton returnBtn = new JButton("Return");
		returnBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeWindow();
			}
		});
		
		JButton saveAsBtn = new JButton("Save as");
		setSaveAsButtonActionPerformer(saveAsBtn);
		
		JButton parseBtn = new JButton("Parse");
		setParseButtonActionPerformer(parseBtn);
		
		JButton saveBtn = new JButton("Save");
		setSaveButtonActionPerformer(saveBtn);
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(configNameLbl)
						.addComponent(configNamePane, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel)
						.addComponent(configScroll, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(parseBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(saveBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(saveAsBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(returnBtn)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(configNameLbl)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(configNamePane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(configScroll, GroupLayout.PREFERRED_SIZE, 620, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(returnBtn)
						.addComponent(saveAsBtn)
						.addComponent(parseBtn)
						.addComponent(saveBtn))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	private void setSaveAsButtonActionPerformer(JButton saveAsBtn){
		if(!type.equals(ConfigHelper.Type.DISPLAY)){
			saveAsBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					File configDir;
					File newFile;
					if (configPath != null){
						configDir = new File(configPath.getDirectory());
						newFile = new File(configPath.getFilePath());
					}else{
						configDir = new File(ConfigHelper.CONFIG_FILES_DIRECTORY);
						newFile = new File(ConfigHelper.CONFIG_FILES_DIRECTORY + configNamePane.getText() + ".xml");
					}
					JFileChooser fileChooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
					fileChooser.setFileFilter(filter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setCurrentDirectory(configDir);
					fileChooser.setSelectedFile(newFile);
					
					int returnValue = fileChooser.showSaveDialog(null);
			        if (returnValue == JFileChooser.APPROVE_OPTION) {
			        	File selectedFile = fileChooser.getSelectedFile();
			        	saveConfig(selectedFile);
			        }
				}
			});
		}else{
			saveAsBtn.setEnabled(false);
		}
	}
	
	private void setSaveButtonActionPerformer(JButton saveBtn){
		if(type.equals(ConfigHelper.Type.EDIT)){
			saveBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					File configFile = new File(configPath.getFilePath());
					saveConfig(configFile);
				}
			});
		}else{
			saveBtn.setEnabled(false);
		}
	}
	
	private void saveConfig(File selectedFile){
        try {
      	  if(!selectedFile.exists()){
      		  selectedFile.createNewFile();
      	  }
      	  saveConfigToFile(selectedFile.getAbsolutePath());
      	  ConfigHelper.addNewConfigToList(selectedFile.getAbsolutePath(), configType);
      	  startWindow.refreshConfigLists();
        } catch (IOException e) {
			PopUpDialog errorDialog = new PopUpDialog("Wyst�pi� b��d podczas zapisywania konfiguracji");
			errorDialog.setVisible(true);
        }
	}
	
	private void saveConfigToFile(String filePath){
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath);
			writer.print(configXml.getText());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	private void displayConfigName(String configName){
		configNamePane.setText(configName);
		if (type.equals(ConfigHelper.Type.DISPLAY)){
			configNamePane.setEditable(false);
		}else{
			configNamePane.setEditable(true);
		}
	}
	
	private void setParseButtonActionPerformer(JButton parseBtn){
		parseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ConfigurationReader configReader;
					
					if (configType.equals(ConfigHelper.ConfigType.CRAWLING)){
						configReader = new CrawlingConfigurationReader();
					}else{
						configReader = new SearchingConfigurationReader();
					}

					Document dom = configReader.getDOMFromString(configXml.getText());
					
					String message = configReader.parse(dom);
					
					if (!message.isEmpty()){
						PopUpDialog errorDialog = new PopUpDialog("Error in parsing configuration coused by: \n" + message);
						errorDialog.setVisible(true);
					}else {
						PopUpDialog errorDialog = new PopUpDialog("Configuration is written properly");
						errorDialog.setVisible(true);
					}
					
				} catch (ParserConfigurationException e){
					PopUpDialog errorDialog = new PopUpDialog("Configuration is not proper xml file \n" + e.getMessage());
					errorDialog.setVisible(true);
				}catch (Exception e) {
					PopUpDialog errorDialog = new PopUpDialog("There have occured error while reading configuration");
					errorDialog.setVisible(true);
				}
			}
		});
	}
	
	private void displayConfigFile(String configPath){
		File file = new File(configPath);
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			while(scanner.hasNextLine()){
				configXml.append(scanner.nextLine());
				configXml.append("\n");
			}
		} catch (FileNotFoundException e) {
			PopUpDialog errorDialog = new PopUpDialog("Plik z wybran� konfiguracj� nie istnieje");
			errorDialog.setVisible(true);
		}finally{
			scanner.close();
		}
	}
	
	
	private void closeWindow(){
		this.dispose();
	}
}
