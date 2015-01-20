package pl.edu.agh.kis.dataretrieval.flow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.retrieving.RetrievingData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.CaseData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.ContentData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.FormFieldData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.LinkData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.SearchingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.searching.SearchingData;
import pl.edu.agh.kis.dataretrieval.configuration.searching.SwitchData;
import pl.edu.agh.kis.dataretrieval.database.DbFieldData;
import pl.edu.agh.kis.dataretrieval.database.RetrieverDao;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.ContentProcessor;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.DataRetriever;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.FormProcessor;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.LinkProcessor;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions.NoSiteRetrievedException;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions.NotAllFieldsRetrievedException;
import pl.edu.agh.kis.dataretrieval.gui.windows.FormWindow;
import pl.edu.agh.kis.dataretrieval.gui.windows.PopUpDialog;
import pl.edu.agh.kis.dataretrieval.gui.windows.ProgressWindow;
import pl.edu.agh.kis.dataretrieval.gui.windows.StartWindow;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class FlowManager implements Runnable{
	
	private WebConversation webConv = new WebConversation();
	private FormProcessor formProcessor = new FormProcessor();
	private WebResponse resp;
	
	private String searchingConfigPath;
	private String retrievingConfigPath;
	private boolean windowMode;
	private boolean bulkMode;
	private Holder<Boolean> keepRetrieving = new Holder<Boolean>();
	private ProgressWindow progressWindow;
	

	public FlowManager(String searchingConfigPath, String retrievingConfigPath, boolean windowMode, boolean bulkMode) {
		this.searchingConfigPath = searchingConfigPath;
		this.retrievingConfigPath = retrievingConfigPath;
		this.windowMode = windowMode;
		this.bulkMode = bulkMode;
		keepRetrieving.value = true;
		this.progressWindow = new ProgressWindow(windowMode, keepRetrieving);
		progressWindow.setVisible(true);
	}

	public void run() {
		boolean retrievingStarted = false;
		try {
			SearchingConfigurationReader searchConfigReader = new SearchingConfigurationReader();
			
			SearchingData searchingData = searchConfigReader.load(searchingConfigPath);
			RetrievingConfigurationReader retrievingConfigReader = new RetrievingConfigurationReader();
			List<RetrievingData> crawlingDataList = retrievingConfigReader.load(retrievingConfigPath);
			RetrieverDao dao = new RetrieverDao(retrievingConfigPath);
			try{
				dao.createTable(crawlingDataList);
				WebResponse startPageResp = goToSite(searchingData.getUrl());
				
				int sitesLeft = searchingData.getMaxRecords();
				int startLink = searchingData.getStartSite();
				int iteration = 1;
				int crawledSites;
				boolean allBulkCombinations = false;
				do {
					crawledSites = 0;
					if (bulkMode) {
						if (iteration++ != 1){
							startLink = 1; //w kolejnych iteracjach w trybie bulkowym strony maja byc juz pobierane od pocz¹tku
						}
						allBulkCombinations = FormProcessor.iterateUsedValues(searchingData, startLink);
					}
					try{
						resp = startPageResp;
						progressWindow.printInfo("Finding sites with data");
						ContentData contentData = goThroughtFlow(searchingData.getFlowDataList());
						ContentProcessor contentProcessor = new ContentProcessor(resp, contentData, progressWindow, sitesLeft, startLink);
						
						while(contentProcessor.hasNextSite() && keepRetrieving.value == true && (sitesLeft>0 || sitesLeft == -1)){
							progressWindow.pause();		//Sprawdza czy postêp zosta³ wstrzymany i jeœli tak to czeka na kontynuacje
							try{
								WebResponse siteResp = contentProcessor.findNextSite();
								if (siteResp != null){
									if (sitesLeft != -1){
										sitesLeft--;
									}
									crawledSites++;
									
									Map<String, List<DbFieldData>> siteData;
									try{
										siteData = DataRetriever.readData(siteResp, crawlingDataList);
									}catch(NotAllFieldsRetrievedException e){
										siteData = e.getSiteData();
										printError(e.getMessage());
									}
									for (Entry<String, List<DbFieldData>> entry: siteData.entrySet()){
									    dao.addSiteData(entry.getKey(), entry.getValue());
									}
									retrievingStarted = true;
								}
							}catch(final Exception e){
								handleException(e);
							}
						}
					
					}catch(NoSiteRetrievedException e){		//nie zosta³ pobrany ¿aden link
						if (!bulkMode){	//zakladamy ze tryb bulkowy moze wyszukac konfiguracje, w ktorej nie ma ¿adnych wynikow wyszukiwania
							throw e;
						}else {
							startLink = 1;
							System.out.println("WARN: " + e.getMessage());
						}
					}
				} while (bulkMode && !allBulkCombinations && sitesLeft > 0 && keepRetrieving.value == true);
			
				if (bulkMode){
					String newConfig;
					if (allBulkCombinations && sitesLeft > 0 && keepRetrieving.value == true){
						removeBulkAttributes(searchingData);
					}else{
						actualizeConfiguration(searchingData, startLink + crawledSites);
					}
				}
			}finally{
				dao.closeConnection();
			}
		} catch (Exception e){
			e.printStackTrace();
			handleException(e);
		} 
		String endMessage;
		if (retrievingStarted){
			endMessage = "Retrieving has been finished";
		}else{
			endMessage = "Could not rertieve any data";
		}
		if (windowMode){
			new Thread(){
				public void run(){
					StartWindow startWindow = new StartWindow();
					startWindow.setVisible(true);
				}
			}.start();
			
			progressWindow.printInfo(endMessage);
			PopUpDialog successDialog = new PopUpDialog(endMessage);
			successDialog.setVisible(true);
		}else{
			System.out.println(endMessage);
		}
	}
	
	private ContentData goThroughtFlow(List<ConfigData> flowDataList) throws IOException, SAXException, RetrievalException, XPathExpressionException, DOMException, NoSiteRetrievedException, InterruptedException{
		WebResponse nextResp;
		for (ConfigData flowDataObj: flowDataList){
			if (flowDataObj instanceof LinkData){
				LinkData linkData = (LinkData) flowDataObj;
				nextResp = LinkProcessor.goTo(resp, linkData);
				resp.close();
				resp = nextResp;
			} else if (flowDataObj instanceof FormData){
				FormData formData = (FormData) flowDataObj;
				formProcessor.loadForm(formData, resp);
				FormProcessor.setDefaultValues(formData.getFields(), bulkMode);
				FormWindow formWindow = new FormWindow(formData.getFields());
				if (windowMode && !bulkMode){
					formWindow.setVisible(true);
					formWindow.waitForSubmit();
				}else{
					formWindow.dispose();
				}
				nextResp = formProcessor.submitForm(formData);
				resp.close();
				resp = nextResp;
			} else if (flowDataObj instanceof SwitchData){
				SwitchData switchData = (SwitchData) flowDataObj;
				CaseData caseData = new CaseData();
				for (Entry<String, FormFieldData> entry: switchData.getRefFields().entrySet()){
					caseData.addValue(entry.getKey(), entry.getValue().getValues());
				}
				List<ConfigData> flowData = switchData.getFlowData(caseData);
				if (flowData == null){
					flowData = switchData.getFlowData(CaseData.getDefaultCaseData());
					if (flowData == null){
						throw new RetrievalException("There is no case for given values");
					}
				}
				return goThroughtFlow(flowData);
			} else if(flowDataObj instanceof ContentData){
				return (ContentData) flowDataObj;
			} 
		}
		return null;
	}
	
	private void removeBulkAttributes(SearchingData searchingData) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		Document configDOM = ConfigurationReader.loadDom(searchingConfigPath);
		removeStartSite(configDOM.getFirstChild());
		Node flowNode = configDOM.getFirstChild().getFirstChild();
		removeConfigUsedValues(searchingData.getFlowDataList(), flowNode);
		saveChangedConfiguration(configDOM);
	}
	
	private void removeStartSite(Node urlNode){
		urlNode.getAttributes().removeNamedItem("startSite");
	}
	
	private void removeConfigUsedValues(List<ConfigData> flowDataList, Node flowNode){
		for (ConfigData flowData: flowDataList){
			if (flowData instanceof FormData){
				Node fieldNode = flowNode.getFirstChild();
				for (FormFieldData fieldData: ((FormData) flowData).getFields()){
					if (fieldData.getLastUsedValues() != null && !fieldData.getLastUsedValues().isEmpty()){
						if(fieldNode.getAttributes().getNamedItem("lastUsedValues") != null){
							fieldNode.getAttributes().removeNamedItem("lastUsedValues");
						}
						fieldNode = fieldNode.getNextSibling();
					}
				}
			}
			if (flowData instanceof SwitchData){
				Node caseNode = flowNode.getFirstChild();
				for (List<ConfigData> caseFlowDataList: ((SwitchData) flowData).getCases().values()){
					removeConfigUsedValues(caseFlowDataList, caseNode.getFirstChild());
				}
			}
			flowNode = flowNode.getNextSibling();
		}
	}
	
	private void actualizeConfiguration(SearchingData searchingData, int startSite) throws ParserConfigurationException, SAXException, IOException, TransformerException{
		Document configDOM = ConfigurationReader.loadDom(searchingConfigPath);
		actualizeStartSite(configDOM, startSite);
		Node flowNode = configDOM.getFirstChild().getFirstChild();
		Attr newLastUsedValues = configDOM.createAttribute("lastUsedValues");
		actualizeConfigUsedValues(searchingData.getFlowDataList(), flowNode, newLastUsedValues);
		saveChangedConfiguration(configDOM);
	}
	
	private void actualizeStartSite(Document configDOM, int startSite){
		Node urlNode = configDOM.getFirstChild();
		Node startSiteAttr = urlNode.getAttributes().getNamedItem("startSite");
		if (startSiteAttr == null){
			startSiteAttr = configDOM.createAttribute("startSite");
		}
		startSiteAttr.setNodeValue(Integer.toString(startSite));
		urlNode.getAttributes().setNamedItem(startSiteAttr);
	}
	
	private void actualizeConfigUsedValues(List<ConfigData> flowDataList, Node flowNode, Attr newLastUsedValues){
		for (ConfigData flowData: flowDataList){
			if (flowData instanceof FormData){
				Node fieldNode = flowNode.getFirstChild();
				for (FormFieldData fieldData: ((FormData) flowData).getFields()){
					if (fieldData.getLastUsedValues() != null && !fieldData.getLastUsedValues().isEmpty()){
						Node usedValues;
						if((usedValues = fieldNode.getAttributes().getNamedItem("lastUsedValues")) == null){
							usedValues = newLastUsedValues.cloneNode(false);
						}
						usedValues.setNodeValue(fieldData.getLastUsedValues());
						fieldNode.getAttributes().setNamedItem(usedValues);
					}
					fieldNode = fieldNode.getNextSibling();
				}
			}
			if (flowData instanceof SwitchData){
				Node caseNode = flowNode.getFirstChild();
				for (List<ConfigData> caseFlowDataList: ((SwitchData) flowData).getCases().values()){
					actualizeConfigUsedValues(caseFlowDataList, caseNode.getFirstChild(), newLastUsedValues);
				}
			}
			flowNode = flowNode.getNextSibling();
		}
	}
	
	private void saveChangedConfiguration(Document DOM) throws TransformerException, FileNotFoundException{
		File file = new File(searchingConfigPath);
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		DOMSource source = new DOMSource(DOM);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}
	
	private WebResponse goToSite(String url) throws RetrievalException, IOException, SAXException{
		for (int retries = 0; retries < 5; retries++){
			try{
				return webConv.getResponse(url);
			}catch (RuntimeException e){
//				Do nothing
			}
		}
		throw new RetrievalException("Could not connect to: " + url);
	}
	
	private void handleException(Exception exception){
		String errorMessage;
		try{
			throw exception;
		}catch (IOException e) {
			errorMessage = "IO error occured: " + e.getMessage();
		} catch (SAXException e) {
			errorMessage = "Error occured while parsing x(ht)ml:\n " + e.getMessage();
		} catch (RetrievalException | NoSiteRetrievedException e) {
			errorMessage = e.getMessage();
		} catch (XPathExpressionException e) {
			errorMessage = "Error in XPath expression" + e.getMessage();
		} catch (DOMException e) {
			errorMessage = "Error while converting x(ht)ml to DOM\n" + e.getMessage();
		} catch (SQLException e) {
			errorMessage = "Database error: \n" + e.getMessage();
		} catch (ParserConfigurationException e) {
			errorMessage = "Configuration file is not proper xml file: \n" + e.getMessage();
		} catch (InterruptedException e){
			errorMessage = "Error occured while waiting for fufilling form\n" + e.getMessage();
		} catch (TransformerException e){
			errorMessage = "Error occured while overwriting searching configuration at " + searchingConfigPath + "\n" + e.getMessage();
		} catch (Exception e){
			errorMessage = "Error while running program\n" + e.getMessage() == null ? "" : e.getMessage();
		}
		printError(errorMessage);
	}
	
	private void printError(final String errorMessage){
		if (windowMode){
			progressWindow.printError(errorMessage);
		}else{
			System.err.println(errorMessage);
		}
	}
}
