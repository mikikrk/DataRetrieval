package pl.edu.agh.kis.dataretrieval.gui;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

import pl.edu.agh.kis.dataretrieval.gui.windows.PopUpDialog;


public class ConfigHelper {

	public final static String CONFIG_FILES_DIRECTORY = "ConfigFiles" + File.separator;
	public final static String SEARCHING_CONFIG_FILES_DIRECTORY = CONFIG_FILES_DIRECTORY + "SearchingConfigs" + File.separator;
	public final static String CRAWLING_CONFIG_FILES_DIRECTORY = CONFIG_FILES_DIRECTORY + "CrawlingConfigs" + File.separator;
	public final static String SEARCHING_LIST_PATH = CONFIG_FILES_DIRECTORY + "searchingList";
	public final static String CRAWLING_LIST_PATH = CONFIG_FILES_DIRECTORY + "crawlingList";

	public enum Type { CREATE, EDIT, DISPLAY };
	public enum ConfigType { SEARCHING, CRAWLING };

	
	public static void addNewConfigToList(String filePath, ConfigType configType){
		try {
			File configFile;
			if(configType.equals(ConfigHelper.ConfigType.CRAWLING)){
				configFile = new File(ConfigHelper.CRAWLING_LIST_PATH);
			}else{
				configFile = new File(ConfigHelper.SEARCHING_LIST_PATH);
			}
				
			List<String> configsList = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
			
			if(!configsList.contains(filePath)){
				configsList.add(filePath);
				Files.write(configFile.toPath(), configsList, Charset.defaultCharset());
			}
		} catch (FileNotFoundException e) {
			PopUpDialog errorDialog = new PopUpDialog("Nie znaleziono pliku z list¹ konfiguracji");
			errorDialog.setVisible(true);
		} catch (Exception e) { //UnsupportedEncodingException && IOException 
			PopUpDialog errorDialog = new PopUpDialog("Wyst¹pi³ b³¹d podczas dodawania konfiguracji do listy");
			errorDialog.setVisible(true);
		}
	}
}
