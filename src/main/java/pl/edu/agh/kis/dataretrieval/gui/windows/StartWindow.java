package pl.edu.agh.kis.dataretrieval.gui.windows;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import pl.edu.agh.kis.dataretrieval.flow.FlowManager;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper;
import pl.edu.agh.kis.dataretrieval.gui.FilePathHolder;


public class StartWindow extends JFrame {
	
	private JPanel contentPane;
	private JList<FilePathHolder> retrievingList;
	private JList<FilePathHolder> searchingList;
	private JScrollPane searchingScrollPane;
	private JScrollPane retrievingScrollPane;
	
	private final StartWindow thisWindow = this;

	/**
	 * Create the frame.
	 */
	public StartWindow() {
		setTitle("Data retrieval system");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 458, 472);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		final JLabel searchingLabel = new JLabel("Searching configurations");
		final JLabel crawlingLabel = new JLabel("Retrieving configurations");
		
		searchingList = new JList<FilePathHolder>();
		searchingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchingScrollPane = new JScrollPane(searchingList);
		retrievingList = new JList<FilePathHolder>();
		retrievingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		retrievingScrollPane = new JScrollPane(retrievingList);
		
		refreshConfigLists();
		
		JButton newSearchingConfigBtn = new JButton("Add");
		newSearchingConfigBtn.setPreferredSize(new Dimension(75, 25));
		newSearchingConfigBtn.setMaximumSize(new Dimension(75, 23));
		newSearchingConfigBtn.setMinimumSize(new Dimension(75, 23));
		newSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton addCrawlingConfigButton = new JButton("Add");
		addCrawlingConfigButton.setSize(new Dimension(75, 25));
		addCrawlingConfigButton.setPreferredSize(new Dimension(75, 25));
		addCrawlingConfigButton.setMinimumSize(new Dimension(75, 23));
		addCrawlingConfigButton.setMaximumSize(new Dimension(75, 23));
		addCrawlingConfigButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.RETRIEVING);
				refreshConfigLists();
			}
		});
		
		JButton editSearchigConfigBtn = new JButton("Edit");
		editSearchigConfigBtn.setPreferredSize(new Dimension(75, 25));
		editSearchigConfigBtn.setMaximumSize(new Dimension(75, 23));
		editSearchigConfigBtn.setMinimumSize(new Dimension(75, 23));
		editSearchigConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.EDIT, ConfigHelper.ConfigType.SEARCHING, (FilePathHolder) searchingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton editCrawlingConfigBtn = new JButton("Edit");
		editCrawlingConfigBtn.setPreferredSize(new Dimension(75, 25));
		editCrawlingConfigBtn.setMaximumSize(new Dimension(75, 23));
		editCrawlingConfigBtn.setMinimumSize(new Dimension(75, 23));
		editCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.EDIT, ConfigHelper.ConfigType.RETRIEVING, (FilePathHolder) retrievingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton displaySerachingConfigBtn = new JButton("Diplay");
		displaySerachingConfigBtn.setPreferredSize(new Dimension(75, 25));
		displaySerachingConfigBtn.setMaximumSize(new Dimension(75, 23));
		displaySerachingConfigBtn.setMinimumSize(new Dimension(75, 23));
		displaySerachingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.DISPLAY, ConfigHelper.ConfigType.SEARCHING, (FilePathHolder) searchingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton displayCrawlingConfigBtn = new JButton("Display");
		displayCrawlingConfigBtn.setPreferredSize(new Dimension(75, 25));
		displayCrawlingConfigBtn.setMaximumSize(new Dimension(75, 23));
		displayCrawlingConfigBtn.setMinimumSize(new Dimension(75, 23));
		displayCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.DISPLAY, ConfigHelper.ConfigType.RETRIEVING, (FilePathHolder) retrievingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton createSearchigConfigBtn = new JButton("Create");
		createSearchigConfigBtn.setPreferredSize(new Dimension(75, 25));
		createSearchigConfigBtn.setMinimumSize(new Dimension(75, 25));
		createSearchigConfigBtn.setMaximumSize(new Dimension(75, 25));
		createSearchigConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.CREATE, ConfigHelper.ConfigType.SEARCHING);
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton createCrawlingConfigBtn = new JButton("Create");
		createCrawlingConfigBtn.setPreferredSize(new Dimension(75, 25));
		createCrawlingConfigBtn.setMaximumSize(new Dimension(75, 25));
		createCrawlingConfigBtn.setMinimumSize(new Dimension(75, 25));
		createCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.CREATE, ConfigHelper.ConfigType.RETRIEVING);
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton forwardBtn = new JButton("Start");
		forwardBtn.setPreferredSize(new Dimension(75, 25));
		forwardBtn.setSize(new Dimension(75, 25));
		forwardBtn.setMaximumSize(new Dimension(75, 23));
		forwardBtn.setMinimumSize(new Dimension(75, 23));
		forwardBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startRetrieving(false);
			}
		});
		
		JButton startBulkModeBtn = new JButton("Start bulk mode");
		startBulkModeBtn.setPreferredSize(new Dimension(155, 25));
		startBulkModeBtn.setMinimumSize(new Dimension(135, 23));
		startBulkModeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startRetrieving(true);
			}
		});
		
		JButton deleteSearchingConfigBtn = new JButton("Delete");
		deleteSearchingConfigBtn.setPreferredSize(new Dimension(75, 25));
		deleteSearchingConfigBtn.setMaximumSize(new Dimension(75, 23));
		deleteSearchingConfigBtn.setMinimumSize(new Dimension(75, 23));
		deleteSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteConfig(((FilePathHolder) searchingList.getSelectedValue()).getFilePath(), ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton deleteCrawlingConfigBtn = new JButton("Delete");
		deleteCrawlingConfigBtn.setPreferredSize(new Dimension(75, 25));
		deleteCrawlingConfigBtn.setMaximumSize(new Dimension(75, 23));
		deleteCrawlingConfigBtn.setMinimumSize(new Dimension(75, 23));
		deleteCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteConfig(((FilePathHolder) retrievingList.getSelectedValue()).getFilePath(), ConfigHelper.ConfigType.RETRIEVING);
				refreshConfigLists();
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(forwardBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(startBulkModeBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(crawlingLabel)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(addCrawlingConfigButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(createCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(editCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(displayCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(deleteCrawlingConfigBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(retrievingScrollPane)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(newSearchingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(createSearchigConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(editSearchigConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(displaySerachingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(deleteSearchingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(searchingLabel)
						.addComponent(searchingScrollPane))
					.addGap(135))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(searchingLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchingScrollPane, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(newSearchingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(createSearchigConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(editSearchigConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(displaySerachingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(deleteSearchingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(crawlingLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(retrievingScrollPane, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(addCrawlingConfigButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(createCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(editCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(displayCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(deleteCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(forwardBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(startBulkModeBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
	
	private void addNewConfigFile(ConfigHelper.ConfigType configType){
		File configDir = new File(ConfigHelper.CONFIG_FILES_DIRECTORY);

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files", "xml");
		fileChooser.setFileFilter(filter);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setCurrentDirectory(configDir);
		
		int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
        	File selectedFile = fileChooser.getSelectedFile();
        	ConfigHelper.addNewConfigToList(selectedFile.getAbsolutePath(), configType);
        }
	}
	
	void refreshConfigLists(){
		loadConfigFilesList(searchingList, ConfigHelper.SEARCHING_LIST_PATH);
		loadConfigFilesList(retrievingList, ConfigHelper.RETRIEVING_LIST_PATH);
	}
	
	private void loadConfigFilesList(JList<FilePathHolder> configList, String filePath){
		List<FilePathHolder> configFilesList = new ArrayList<FilePathHolder>();
		File configFilesListFile = new File(filePath);

		List<String> filesToDelete = new LinkedList<String>();
		
		Scanner scanner = null;
		try {
			if (!configFilesListFile.exists()){
				File configFilesDir = new File(ConfigHelper.CONFIG_FILES_DIRECTORY);
				if(!configFilesDir.exists()){
					configFilesDir.mkdirs();
				}
				configFilesListFile.createNewFile();
			}else{
				scanner = new Scanner(configFilesListFile);
				while(scanner.hasNextLine()){
					FilePathHolder file = new FilePathHolder(scanner.nextLine());
					if (Files.exists(new File(file.getFilePath()).toPath())){
						configFilesList.add(file);
					}else{
						filesToDelete.add(file.getFilePath());
					}
				}

			}
		} catch (FileNotFoundException e) {
			PopUpDialog dialog = new PopUpDialog("Error occured while creating file with configuration list");
			dialog.setVisible(true);
		} catch (IOException e) {
			PopUpDialog dialog = new PopUpDialog("Error occured while displaying configuration list");
			dialog.setVisible(true);
		}finally{
			if(scanner != null){
				scanner.close();
			}
		}

		for (String fileToDelete: filesToDelete){
			deleteConfigFromList(fileToDelete, filePath);
		}
		
		if(!configFilesList.isEmpty()){
			configList.setListData(configFilesList.toArray(new FilePathHolder[]{})); 
			configList.setSelectedIndex(0);
		}
	}
	
	private void deleteConfig(String filePath, ConfigHelper.ConfigType type){
		File file = new File(filePath);
		YesNoDialog yesNo = new YesNoDialog("Do you want to delete configuration file also?");
		if (yesNo.isYes()){
			file.delete();
		}
		
		if(type.equals(ConfigHelper.ConfigType.SEARCHING)){
			deleteConfigFromList(filePath, ConfigHelper.SEARCHING_LIST_PATH);
		}else{
			deleteConfigFromList(filePath, ConfigHelper.RETRIEVING_LIST_PATH);
		}
	}
	
	private void deleteConfigFromList(String filePath, String listPath){
		try {
			File listFile = new File(listPath);
			List<String> configsList = Files.readAllLines(listFile.toPath(), Charset.defaultCharset());
			if (configsList.contains(filePath)){
				configsList.remove(filePath);
			}
			Files.write(listFile.toPath(), configsList, Charset.defaultCharset());
		} catch (IOException e) {
			PopUpDialog errorDialog = new PopUpDialog("Error occured while deleting configuration from list");
			errorDialog.setVisible(true);
		}
	}
	
	private void startRetrieving(boolean bulkMode){
		if (searchingList.getSelectedValue() != null && retrievingList.getSelectedValue() != null){
			String searchingConfigPath = searchingList.getSelectedValue().getFilePath();
			String crawlingConfigPath = retrievingList.getSelectedValue().getFilePath();
			FlowManager siteSearcher = new FlowManager(searchingConfigPath, crawlingConfigPath, true, bulkMode);
			new Thread(siteSearcher).start();
			closeWindow();
		}else{
			PopUpDialog errorDialog = new PopUpDialog("Both cinfiguration files have to be selected");
			errorDialog.setVisible(true);
		}
	}
	
	private void closeWindow(){
		this.dispose();
	}
}
