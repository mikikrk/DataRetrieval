package pl.edu.agh.kis.dataretrieval.gui.forms;
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

import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper;
import pl.edu.agh.kis.dataretrieval.gui.PopUpDialog;
import pl.edu.agh.kis.dataretrieval.gui.FilePathHolder;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper.ConfigType;
import pl.edu.agh.kis.dataretrieval.gui.ConfigHelper.Type;


public class StartWindow extends JFrame {
	
	private JPanel contentPane;
	private JList<FilePathHolder> crawlingList;
	private JList<FilePathHolder> searchingList;
	
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
		setBounds(100, 100, 500, 505);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel urlLabel = new JLabel("Adres URL");
		urlLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		final JTextPane urlPane = new JTextPane();
		
		final JLabel searchingLabel = new JLabel("Konfiguracje wyszukiwania");
		searchingList = new JList<FilePathHolder>();
		final JLabel crawlingLabel = new JLabel("Konfiguracje wydobywania informacji");
		crawlingList = new JList<FilePathHolder>();
		
		refreshConfigLists();
		
		JButton newSearchingConfigBtn = new JButton("Dodaj");
		newSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton addCrawlingConfigButton = new JButton("Dodaj");
		addCrawlingConfigButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addNewConfigFile(ConfigHelper.ConfigType.CRAWLING);
				refreshConfigLists();
			}
		});
		
		JButton editSearchigConfigBtn = new JButton("Edytuj");
		editSearchigConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						new ConfigWindow(thisWindow, ConfigHelper.Type.EDIT, ConfigHelper.ConfigType.SEARCHING, (FilePathHolder) searchingList.getSelectedValue());
					}
				}.start();
			}
		});
		
		JButton editCrawlingConfigBtn = new JButton("Edytuj");
		editCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						new ConfigWindow(thisWindow, ConfigHelper.Type.EDIT, ConfigHelper.ConfigType.CRAWLING, (FilePathHolder) crawlingList.getSelectedValue());
					}
				}.start();
			}
		});
		
		JButton displaySerachingConfigBtn = new JButton("Wyswietl");
		displaySerachingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						new ConfigWindow(thisWindow, ConfigHelper.Type.DISPLAY, ConfigHelper.ConfigType.SEARCHING, (FilePathHolder) searchingList.getSelectedValue());
					}
				}.start();
			}
		});
		
		JButton displayCrawlingConfigBtn = new JButton("Wyswietl");
		displayCrawlingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						new ConfigWindow(thisWindow, ConfigHelper.Type.DISPLAY, ConfigHelper.ConfigType.CRAWLING, (FilePathHolder) searchingList.getSelectedValue());
					}
				}.start();
			}
		});
		
		JButton createSearchigConfigBtn = new JButton("Utworz");
		createSearchigConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(){
					public void run(){
						new ConfigWindow(thisWindow, ConfigHelper.Type.CREATE, ConfigHelper.ConfigType.SEARCHING);
					}
				}.start();
			}
		});
		
		JButton createCrawlingConfigBtn = new JButton("Utworz");
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
		
		JButton forwardBtn = new JButton("Dalej");
		forwardBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JButton continueCrawlingBtn = new JButton("Kontynuuj pobieranie");
		continueCrawlingBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JButton deleteSearchingConfigBtn = new JButton("Usun");
		deleteSearchingConfigBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteConfig(((FilePathHolder) searchingList.getSelectedValue()).getFilePath(), ConfigHelper.ConfigType.SEARCHING);
				refreshConfigLists();
			}
		});
		
		JButton deleteCrawlingConfigBtn = new JButton("Usun");
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
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(crawlingLabel)
							.addPreferredGap(ComponentPlacement.RELATED, 236, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(addCrawlingConfigButton, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(createCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(editCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(displayCrawlingConfigBtn)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(deleteCrawlingConfigBtn, GroupLayout.PREFERRED_SIZE, 63, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
							.addComponent(crawlingList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(urlLabel)
							.addComponent(urlPane, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
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
							.addComponent(searchingList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(forwardBtn)
							.addPreferredGap(ComponentPlacement.RELATED, 222, Short.MAX_VALUE)
							.addComponent(continueCrawlingBtn)))
					.addGap(135))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(urlLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(urlPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchingLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(searchingList, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
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
					.addComponent(crawlingList, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(addCrawlingConfigButton)
						.addComponent(createCrawlingConfigBtn)
						.addComponent(editCrawlingConfigBtn)
						.addComponent(displayCrawlingConfigBtn)
						.addComponent(deleteCrawlingConfigBtn))
					.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(forwardBtn)
						.addComponent(continueCrawlingBtn)))
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
			configList.setListData((FilePathHolder[]) configFilesList.toArray()); 
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
}
