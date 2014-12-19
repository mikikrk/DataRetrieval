package pl.edu.agh.kis.dataretrieval.retriever;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
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
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.CaseData;
import pl.edu.agh.kis.dataretrieval.configuration.search.ContentData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormFieldData;
import pl.edu.agh.kis.dataretrieval.configuration.search.LinkData;
import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.SwitchData;
import pl.edu.agh.kis.dataretrieval.database.CrawlerDao;
import pl.edu.agh.kis.dataretrieval.database.DbFieldData;
import pl.edu.agh.kis.dataretrieval.gui.windows.FormWindow;
import pl.edu.agh.kis.dataretrieval.gui.windows.PopUpDialog;
import pl.edu.agh.kis.dataretrieval.gui.windows.StartWindow;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.ContentProcessor;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.FormProcessor;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.LinkProcessor;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.NoSiteRetrievedException;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.SiteCrawler;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class SiteRetriever implements Runnable{
	
	private WebConversation webConv = new WebConversation();
	private WebResponse resp;
	private FormProcessor formProcessor = new FormProcessor();
	
	private String searchingConfigPath;
	private String crawlingConfigPath;
	private boolean windowMode;
	private boolean bulkMode;
	
	public SiteRetriever(String searchingConfigPath, String crawlingConfigPath, boolean windowMode, boolean bulkMode) {
		this.searchingConfigPath = searchingConfigPath;
		this.crawlingConfigPath = crawlingConfigPath;
		this.windowMode = windowMode;
		this.bulkMode = bulkMode;
	}

	public void run() {
		String dialogMessage;
		try{
			SearchingConfigurationReader searchConfigReader = new SearchingConfigurationReader();
			
			SearchingData searchingData = searchConfigReader.load(searchingConfigPath);
			CrawlingConfigurationReader crawlingConfigReader = new CrawlingConfigurationReader();
			List<CrawlingData> crawlingDataList = crawlingConfigReader.load(crawlingConfigPath);
			CrawlerDao dao = new CrawlerDao();
			resp = webConv.getResponse(searchingData.getUrl());
			
			int sitesLeft = searchingData.getMaxRecords();
			int startLink = searchingData.getCrawledSites() + 1;
			int crawledSites = 0;
			do {
				if (bulkMode) {
					FormProcessor.iterateUsedValues(searchingData);
				}
				try{
					List<WebResponse> sites = goThroughtFlow(searchingData.getFlowDataList(), sitesLeft, startLink);
					
					crawledSites = sites.size();
					if (sitesLeft != -1){
						sitesLeft -= crawledSites;
					}
					startLink = 1;		//jeœli nie jest rzucony wyj¹tek to link startowy zosta³ pobrany i wsyzsstkie kolejne maj¹ byæ pobierane
				
					boolean wasCreated = false;
					for (WebResponse siteResp: sites){
						Map<String, List<DbFieldData>> siteData = SiteCrawler.readData(siteResp, crawlingDataList);
						if (!wasCreated){
							for (Entry<String, List<DbFieldData>> entry: siteData.entrySet()){
								dao.createTable(entry.getKey(), entry.getValue());
							}
						}
						for (Entry<String, List<DbFieldData>> entry: siteData.entrySet()){
						    dao.addSiteData(entry.getKey(), entry.getValue());
						}
					}
				
					dao.closeConnection();
				}catch(NoSiteRetrievedException e){		//nie zosta³ pobrany ¿aden link
					if (startLink >= 1 && e.getLinkCount() != 0){
						startLink -= e.getLinkCount();		//jeœli numer linku pocz¹tkowego by³ wiêkszy od 1 oraz zostal zwrocony licznik stron, to znaczy, ¿e link poczatkowy nie zostal znaleziony (prawdopodobnie zmalala ilosc linkow od ostatniego wyszukiwania)
					}else{
						if (!bulkMode){	//zakladamy ze tryb bulkowy moze wyszukac konfiguracje, w ktorej nie ma rzadnych wynikow wyszukiwania
							throw new RetrievalException("No link has been found on content site");
						}
					}
				}
				
			} while (bulkMode && sitesLeft > 0);
			if (bulkMode){
				actualizeConfiguration(searchingData, crawledSites);
			}
			dialogMessage = "All data has been succesfully downloaded to database";
		} catch (IOException e) {
			dialogMessage = "IO error occured: " + e.getMessage();
		} catch (SAXException e) {
			dialogMessage = "Error occured while parsing x(ht)ml:\n " + e.getMessage();
		} catch (RetrievalException e) {
			dialogMessage = e.getMessage();
		} catch (XPathExpressionException e) {
			dialogMessage = "Error in XPath expression" + e.getMessage();
		} catch (DOMException e) {
			dialogMessage = "Error while converting x(ht)ml to DOM\n" + e.getMessage();
		} catch (SQLException e) {
			dialogMessage = "Database error: \n" + e.getMessage();
		} catch (ParserConfigurationException e) {
			dialogMessage = "Configuration file is not proper xml file: \n" + e.getMessage();

		}
		if (windowMode){
			PopUpDialog successDialog = new PopUpDialog(dialogMessage);
			successDialog.setVisible(true);

			new Thread(){
				public void run(){
					StartWindow startWindow = new StartWindow();
					startWindow.setVisible(true);
				}
			}.start();
		}else{
			System.out.println(dialogMessage);
		}
	}
	
	private List<WebResponse> goThroughtFlow(List<ConfigData> flowDataList, Integer maxRecords, Integer startLink) throws IOException, SAXException, RetrievalException, XPathExpressionException, DOMException, NoSiteRetrievedException{
		for (ConfigData flowDataObj: flowDataList){
			if (flowDataObj instanceof LinkData){
				LinkData linkData = (LinkData) flowDataObj;
				resp = LinkProcessor.goTo(resp, linkData);
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
				resp = formProcessor.submitForm(formData);
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
				List<WebResponse> sites = goThroughtFlow(flowData, maxRecords, startLink);
				if (!sites.isEmpty()){
					return sites;
				}
			} else if(flowDataObj instanceof ContentData){
				ContentData contentData = (ContentData) flowDataObj;
				return ContentProcessor.findSites(resp, contentData, maxRecords, startLink);
			} 
		}
		return new LinkedList<WebResponse>();
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
					usedValues.setNodeValue(StringUtils.join(fieldData.getUsedValues(), "\\|"));
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
}
