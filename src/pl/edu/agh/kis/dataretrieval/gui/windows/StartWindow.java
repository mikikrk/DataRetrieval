package pl.edu.agh.kis.dataretrieval.gui.windows;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
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
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper;
import pl.edu.agh.kis.dataretrieval.gui.FilePathHolder;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper.ConfigType;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper.Type;
import pl.edu.agh.kis.dataretrieval.retriever.SiteRetriever;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;


public class StartWindow extends JFrame {
	
	private JPanel contentPane;
	private JList<FilePathHolder> crawlingList;
	private JList<FilePathHolder> searchingList;
	private JScrollPane searchingScrollPane;
	private JScrollPane crawlingScrollPane;
	
	private final StartWindow thisWindow = this;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartWindow frame = new StartWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StartWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 472);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		final JLabel searchingLabel = new JLabel("Searching configurations");
		final JLabel crawlingLabel = new JLabel("Crawling configurations");
		
		searchingList = new JList<FilePathHolder>();
		searchingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchingScrollPane = new JScrollPane(searchingList);
		crawlingList = new JList<FilePathHolder>();
		crawlingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		crawlingScrollPane = new JScrollPane(crawlingList);
		
		refreshConfigLists();
		
		JButton newSearchingConfigBtn = new JButton("Add");
		newSearchingConfigBtn.setMaximumSize(new Dimension(65, 23));
		newSearchingConfigBtn.setMinimumSize(new Dimension(65, 23));
		newSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton addCrawlingConfigButton = new JButton("Add");
		addCrawlingConfigButton.setMinimumSize(new Dimension(65, 23));
		addCrawlingConfigButton.setMaximumSize(new Dimension(65, 23));
		addCrawlingConfigButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.CRAWLING);
				refreshConfigLists();
			}
		});
		
		JButton editSearchigConfigBtn = new JButton("Edit");
		editSearchigConfigBtn.setMaximumSize(new Dimension(65, 23));
		editSearchigConfigBtn.setMinimumSize(new Dimension(65, 23));
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
		editCrawlingConfigBtn.setMaximumSize(new Dimension(65, 23));
		editCrawlingConfigBtn.setMinimumSize(new Dimension(65, 23));
		editCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.EDIT, ConfigHelper.ConfigType.CRAWLING, (FilePathHolder) crawlingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton displaySerachingConfigBtn = new JButton("Diplay");
		displaySerachingConfigBtn.setMaximumSize(new Dimension(65, 23));
		displaySerachingConfigBtn.setMinimumSize(new Dimension(65, 23));
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
		displayCrawlingConfigBtn.setMaximumSize(new Dimension(65, 23));
		displayCrawlingConfigBtn.setMinimumSize(new Dimension(65, 23));
		displayCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.DISPLAY, ConfigHelper.ConfigType.CRAWLING, (FilePathHolder) searchingList.getSelectedValue());
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton createSearchigConfigBtn = new JButton("Create");
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
		createCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						ConfigWindow configWindow = new ConfigWindow(thisWindow, ConfigHelper.Type.CREATE, ConfigHelper.ConfigType.CRAWLING);
						configWindow.setVisible(true);
					}
				}.start();
			}
		});
		
		JButton forwardBtn = new JButton("Start");
		forwardBtn.setMaximumSize(new Dimension(65, 23));
		forwardBtn.setMinimumSize(new Dimension(65, 23));
		forwardBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String searchingConfigPath = searchingList.getSelectedValue().getFilePath();
				String crawlingConfigPath = crawlingList.getSelectedValue().getFilePath();
				SiteRetriever siteSearcher = new SiteRetriever(searchingConfigPath, crawlingConfigPath);
				new Thread(siteSearcher).start();
				closeWindow();
			}
		});
		
		JButton continueCrawlingBtn = new JButton("Continue crawling");
		continueCrawlingBtn.setMinimumSize(new Dimension(135, 23));
		continueCrawlingBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JButton deleteSearchingConfigBtn = new JButton("Delete");
		deleteSearchingConfigBtn.setMaximumSize(new Dimension(65, 23));
		deleteSearchingConfigBtn.setMinimumSize(new Dimension(65, 23));
		deleteSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteConfig(((FilePathHolder) searchingList.getSelectedValue()).getFilePath(), ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton deleteCrawlingConfigBtn = new JButton("Delete");
		deleteCrawlingConfigBtn.setMaximumSize(new Dimension(65, 23));
		deleteCrawlingConfigBtn.setMinimumSize(new Dimension(65, 23));
		deleteCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteConfig(((FilePathHolder) crawlingList.getSelectedValue()).getFilePath(), ConfigHelper.ConfigType.CRAWLING);
				refreshConfigLists();
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
							.addComponent(forwardBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(continueCrawlingBtn))
						.addComponent(crawlingLabel, Alignment.LEADING)
						.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
							.addComponent(addCrawlingConfigButton, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(createCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(editCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(displayCrawlingConfigBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(deleteCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
						.addGroup(Alignment.LEADING, gl_contentPane.createParallelGroup(Alignment.LEADING, false)
							.addComponent(crawlingScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(newSearchingConfigBtn)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(createSearchigConfigBtn)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(editSearchigConfigBtn)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(displaySerachingConfigBtn)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(deleteSearchingConfigBtn, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
								.addGap(61))
							.addComponent(searchingLabel)
							.addComponent(searchingScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
						.addComponent(newSearchingConfigBtn)
						.addComponent(createSearchigConfigBtn)
						.addComponent(editSearchigConfigBtn)
						.addComponent(displaySerachingConfigBtn)
						.addComponent(deleteSearchingConfigBtn))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(crawlingLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(crawlingScrollPane, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(addCrawlingConfigButton)
						.addComponent(createCrawlingConfigBtn)
						.addComponent(editCrawlingConfigBtn)
						.addComponent(displayCrawlingConfigBtn)
						.addComponent(deleteCrawlingConfigBtn))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(forwardBtn)
						.addComponent(continueCrawlingBtn))
					.addContainerGap(65, Short.MAX_VALUE))
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
		loadConfigFilesList(crawlingList, ConfigHelper.CRAWLING_LIST_PATH);
	}
	
	private void loadConfigFilesList(JList<FilePathHolder> configList, String filePath){
		List<FilePathHolder> configFilesList = new ArrayList<FilePathHolder>();
		File configFilesListFile = new File(filePath);

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
					configFilesList.add(new FilePathHolder(scanner.nextLine()));
				}

			}
		} catch (FileNotFoundException e) {
			PopUpDialog dialog = new PopUpDialog("Nie uda³o siê prawid³owo utworzyæ pliku z list¹ konfiguracji");
			dialog.setVisible(true);
		} catch (IOException e) {
			PopUpDialog dialog = new PopUpDialog("Wyst¹pi³ b³¹d podczas wyœwietlania listy konfiguracji");
			dialog.setVisible(true);
		}finally{
			if(scanner != null){
				scanner.close();
			}
		}
		
		if(!configFilesList.isEmpty()){
			configList.setListData(configFilesList.toArray(new FilePathHolder[]{})); 
		}
	}
	
	private void deleteConfig(String filePath, ConfigHelper.ConfigType type){
		File file = new File(filePath);
		file.delete();
		
		if(type.equals(ConfigHelper.ConfigType.SEARCHING)){
			deleteConfigFromList(filePath, ConfigHelper.SEARCHING_LIST_PATH);
		}else{
			deleteConfigFromList(filePath, ConfigHelper.CRAWLING_LIST_PATH);
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
			PopUpDialog errorDialog = new PopUpDialog("Wyst¹pi³ b³¹d podczas usuwania konfiguracji z listy");
			errorDialog.setVisible(true);
		}
	}
	
	private void closeWindow(){
		this.dispose();
	}
}
