package pl.edu.agh.kis.dataretrieval.retriever;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.FormData;
import pl.edu.agh.kis.dataretrieval.configuration.search.LinkData;
import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingConfigurationReader;
import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingData;
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
	
	public SiteRetriever(String searchingConfigPath, String crawlingConfigPath) {
		this.searchingConfigPath = searchingConfigPath;
		this.crawlingConfigPath = crawlingConfigPath;
	}

	public void run() {
		try{
			SearchingConfigurationReader searchConfigReader = new SearchingConfigurationReader();
			
			SearchingData searchingData = searchConfigReader.load(searchingConfigPath);
			resp = webConv.getResponse(searchingData.getUrl());
			
			while (searchingData.hasNextFlowData()){
				Object flowDataObj = searchingData.nextFlowData();
				if (flowDataObj instanceof LinkData){
					LinkData linkData = (LinkData) flowDataObj;
					resp = LinkProcessor.goTo(resp, linkData);
				}else if (flowDataObj instanceof FormData){
					FormData formData = (FormData) flowDataObj;
					formProcessor.loadForm(formData, resp);
					FormWindow formWindow = new FormWindow(formData.getFields());
					formWindow.waitForSubmit();
					resp = formProcessor.submitForm(formData);
				}
			}
			List<WebResponse> sites = ContentProcessor.findSites(resp, searchingData.getContentFinder(), searchingData.getBulkRecords());
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
			PopUpDialog errorDialog = new PopUpDialog("All data has been succesfully downloaded to database");
			errorDialog.setVisible(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RetrievalException e) {
			PopUpDialog errorDialog = new PopUpDialog(e.getMessage());
			errorDialog.setVisible(true);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(){
			public void run(){
				StartWindow startWindow = new StartWindow();
				startWindow.setVisible(true);
			}
		}.start();
	}
	
	
}
