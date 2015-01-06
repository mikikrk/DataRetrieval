package pl.edu.agh.kis.dataretrieval.flow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Holder;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
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
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.FormProcessor;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.LinkProcessor;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.DataRetriever;
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
			dao.createTable(crawlingDataList);
			WebResponse startPageResp = goToSite(searchingData.getUrl());
			
			int sitesLeft = searchingData.getMaxRecords();
			int startLink = searchingData.getCrawledSites() + 1;
			int crawledSites;
			do {
				crawledSites = 0;
				if (bulkMode) {
					FormProcessor.iterateUsedValues(searchingData, startLink);
				}
				try{
					resp = startPageResp;
					ContentData contentData = goThroughtFlow(searchingData.getFlowDataList());
					ContentProcessor contentProcessor = new ContentProcessor(resp, contentData, progressWindow, sitesLeft, startLink);
					startLink = 1; //w kolejnych iteracjach w trybie bulkowym strony maja byc juz pobierane od pocz¹tku
					
					while(contentProcessor.hasNextSite() && keepRetrieving.value == true){
						progressWindow.pause();		//Sprawdza czy postêp zosta³ wstrzymany i jeœli tak to czeka na kontynuacje
						try{
							WebResponse siteResp = contentProcessor.findNextSite();
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
				if (sitesLeft != -1){
					sitesLeft -= crawledSites;
				}
			} while (bulkMode && sitesLeft > 0);
			dao.closeConnection();
			if (bulkMode){
				actualizeConfiguration(searchingData, crawledSites);
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
				formProcessor.setDefaultValues(formData.getFields(), bulkMode);
				FormWindow formWindow = new FormWindow(formData.getFields());
				if (windowMode){
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
	
	private void actualizeConfiguration(SearchingData searchingData, int crawledSites) throws ParserConfigurationException, SAXException, IOException{
		Document configDOM = ConfigurationReader.loadDom(searchingConfigPath);
		Node flowNode = configDOM.getFirstChild().getFirstChild();
		Attr newUsedValues = configDOM.createAttribute("usedValues");
		actualizeConfigUsedValues(searchingData.getFlowDataList(), flowNode, newUsedValues);
		configDOM.getTextContent();
	}
	
	private void actualizeConfigUsedValues(List<ConfigData> flowDataList, Node flowNode, Attr newUsedValues){
		for (ConfigData flowData: flowDataList){
			if (flowData instanceof FormData){
				Node fieldNode = flowNode.getFirstChild();
				for (FormFieldData fieldData: ((FormData) flowData).getFields()){
					Node usedValues;
					if((usedValues = fieldNode.getAttributes().getNamedItem("usedValues")) == null){
						usedValues = newUsedValues.cloneNode(false);
					}
					if (fieldData.getUsedValues().containsAll(fieldData.getOptionValues())){
						fieldData.clearUsedValues();
						fieldData.addUsedValue("$all$");
					}
					usedValues.setNodeValue(StringUtils.join(fieldData.getUsedValues(), "|"));
					fieldNode.getAttributes().setNamedItem(usedValues);
					fieldNode = fieldNode.getNextSibling();
				}
			}
			if (flowData instanceof SwitchData){
				Node caseNode = flowNode.getFirstChild();
				for (List<ConfigData> caseFlowDataList: ((SwitchData) flowData).getCases().values()){
					actualizeConfigUsedValues(caseFlowDataList, caseNode.getFirstChild(), newUsedValues);
				}
			}
			flowNode = flowNode.getNextSibling();
		}
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
			errorMessage = "Error while waiting for fufilling form\n" + e.getMessage();
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
