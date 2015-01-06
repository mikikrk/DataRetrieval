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
	public final static String RETRIEVING_CONFIG_FILES_DIRECTORY = CONFIG_FILES_DIRECTORY + "RetrievingConfigs" + File.separator;
	public final static String SEARCHING_LIST_PATH = CONFIG_FILES_DIRECTORY + "searchingList";
	public final static String RETRIEVING_LIST_PATH = CONFIG_FILES_DIRECTORY + "retrievingList";

	public enum Type { CREATE, EDIT, DISPLAY };
	public enum ConfigType { SEARCHING, RETRIEVING };

	
	public static void addNewConfigToList(String filePath, ConfigType configType){
		try {
			File configFile;
			if(configType.equals(ConfigHelper.ConfigType.RETRIEVING)){
				configFile = new File(ConfigHelper.RETRIEVING_LIST_PATH);
			}else{
				configFile = new File(ConfigHelper.SEARCHING_LIST_PATH);
			}
				
			List<String> configsList = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
			
			if(!configsList.contains(filePath)){
				configsList.add(filePath);
				Files.write(configFile.toPath(), configsList, Charset.defaultCharset());
			}
		} catch (FileNotFoundException e) {
			PopUpDialog errorDialog = new PopUpDialog("File with list of configurations was not found");
			errorDialog.setVisible(true);
		} catch (Exception e) { //UnsupportedEncodingException && IOException 
			PopUpDialog errorDialog = new PopUpDialog("Error occured while adding configuration to list");
			errorDialog.setVisible(true);
		}
	}
}
