package pl.edu.agh.kis.dataretrieval.retriever;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.ConfigData;
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
	
	public SiteRetriever(String searchingConfigPath, String crawlingConfigPath, boolean windowMode, boolean bulkMode) {
		this.searchingConfigPath = searchingConfigPath;
		this.crawlingConfigPath = crawlingConfigPath;
		this.windowMode = windowMode;
	}

	public void run() {
		String dialogMessage;
		try{
			SearchingConfigurationReader searchConfigReader = new SearchingConfigurationReader();
			
			SearchingData searchingData = searchConfigReader.load(searchingConfigPath);
			resp = webConv.getResponse(searchingData.getUrl());
			List<WebResponse> sites = goThroughtFlow(searchingData.getFlowDataList(), searchingData.getMaxRecords(), searchingData.getCrawledSites());
			
			CrawlingConfigurationReader crawlingConfigReader = new CrawlingConfigurationReader();
			List<CrawlingData> crawlingDataList = crawlingConfigReader.load(crawlingConfigPath);
			CrawlerDao dao = new CrawlerDao();
			
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
	
	private List<WebResponse> goThroughtFlow(List<ConfigData> flowDataList, Integer maxRecords, Integer crawledSites) throws IOException, SAXException, RetrievalException, XPathExpressionException, DOMException{
		for (ConfigData flowDataObj: flowDataList){
			if (flowDataObj instanceof LinkData){
				LinkData linkData = (LinkData) flowDataObj;
				resp = LinkProcessor.goTo(resp, linkData);
			} else if (flowDataObj instanceof FormData){
				FormData formData = (FormData) flowDataObj;
				formProcessor.loadForm(formData, resp);
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
				List<WebResponse> sites = goThroughtFlow(switchData.getFlowData(caseData), maxRecords, crawledSites);
				if (!sites.isEmpty()){
					return sites;
				}
			} else if(flowDataObj instanceof ContentData){
				ContentData contentData = (ContentData) flowDataObj;
				return ContentProcessor.findSites(resp, contentData, maxRecords, crawledSites + 1);
			} 
		}
		return new LinkedList<WebResponse>();
	}
	
	
}
