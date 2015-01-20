package pl.edu.agh.kis.dataretrieval.flow.webprocessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.searching.ContentData;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions.NoNextSiteException;
import pl.edu.agh.kis.dataretrieval.flow.webprocessors.exceptions.NoSiteRetrievedException;
import pl.edu.agh.kis.dataretrieval.gui.windows.ProgressWindow;

import com.meterware.httpunit.WebResponse;

public class ContentProcessor {
	
	private WebResponse firstMainPageResp;
	private ContentData contentData;
	private Integer maxAmount = -1;
	private Integer startLink = 1;
	
	private Integer iterator = 0;
	private List<String> sitesBenchmarksOnMainSite;
	private WebResponse actualMainPageResp;
	private WebResponse nextMainPageResp;
	private String findBenchmarkExpression;
	private Integer linkCounter = 1;
	private ProgressWindow progressWindow;
	
	public ContentProcessor(WebResponse mainPageResp, ContentData contentData, ProgressWindow progressWindow) throws IOException, SAXException, XPathExpressionException, DOMException, NoSiteRetrievedException, RetrievalException {
		super();
		this.firstMainPageResp = mainPageResp;
		this.contentData = contentData;
		this.progressWindow = progressWindow;
		init();
	}

	public ContentProcessor(WebResponse mainPageResp, ContentData contentData, ProgressWindow progressWindow,
			Integer maxAmount, Integer startLink) throws IOException, SAXException, XPathExpressionException, DOMException, NoSiteRetrievedException, RetrievalException {
		super();
		this.firstMainPageResp = mainPageResp;
		this.contentData = contentData;
		this.progressWindow = progressWindow;
		this.maxAmount = maxAmount;
		this.startLink = startLink;
		init();
	}
	
	private void init() throws IOException, SAXException, XPathExpressionException, DOMException, NoSiteRetrievedException, RetrievalException{
		actualMainPageResp = firstMainPageResp;
		nextMainPageResp = getNextPage(actualMainPageResp);
		findBenchmarkExpression = RetrievalHelper.getXpathExpression(contentData);
		actualizeSitesBenchmarks();
		if (startLink > 1){
			while (linkCounter < startLink){
				if (nextMainPageResp != null){
					goToNextPage();
					actualizeSitesBenchmarks();
				}else{
					throw new NoSiteRetrievedException(linkCounter, "CrawledSites number is bigger then avaliable sites");
				}
			}
		}
	}
	
	public boolean hasNextSite(){
		if (iterator < sitesBenchmarksOnMainSite.size()){
			return true;
		}else if(!sitesBenchmarksOnMainSite.isEmpty()){
			if (nextMainPageResp != null){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	public WebResponse findNextSite() throws SAXException, IOException, RetrievalException, XPathExpressionException, DOMException, NoSiteRetrievedException{
		int sitesAmount = sitesBenchmarksOnMainSite.size();
		if (iterator < sitesAmount){
			return getSite(sitesBenchmarksOnMainSite.get(iterator++));
		}else {
			if(sitesAmount != 0){
				if (nextMainPageResp != null){
					goToNextPage();
					actualizeSitesBenchmarks();
					iterator = 0;
					return findNextSite();
				}else{
					throw new NoNextSiteException();
				}
			}else{
				return null;
			}
		}
	}
	
	private void actualizeSitesBenchmarks() throws XPathExpressionException, DOMException, SAXException, NoSiteRetrievedException, RetrievalException{
		sitesBenchmarksOnMainSite = getSitesBenchmarks(actualMainPageResp);
	}
	
	private List<String> getSitesBenchmarks(WebResponse resp) throws SAXException, XPathExpressionException, NoSiteRetrievedException, DOMException, RetrievalException{
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Document mainPageDOM = resp.getDOM();
		List<String> sitesBenchmrks = new LinkedList<String>();
		
		NodeList nodeList = (NodeList) xPath.compile(findBenchmarkExpression).evaluate(mainPageDOM, XPathConstants.NODESET);
		Node node;
		
		if (contentData.getBenchmarkNo() != null){
			node = nodeList.item(contentData.getBenchmarkNo());
		}else{
			node = nodeList.item(0);
		}
		
		RetrievalHelper.NodeAttrEntry entry;
	
		try{
			entry = RetrievalHelper.realizePath(contentData.getSearchPathNodes(), node);
		}catch(RetrievalException e){
			throw new NoSiteRetrievedException(linkCounter, e.getMessage());
		}
		
		if (entry.getNode() != null){
			boolean terminated = false;
			while (!terminated){
				if (linkCounter >= startLink){
					if (maxAmount != -1){
						if (linkCounter < startLink + maxAmount){
							sitesBenchmrks.add(getLinkBenchmark(entry));
						}else{
							terminated = true;
						}
					} else{
						sitesBenchmrks.add(getLinkBenchmark(entry));
					}
				}
				if (!RetrievalHelper.checkTermination(entry.getNode(), contentData)){
					entry = RetrievalHelper.realizePath(contentData.getSearchNextNodes(), entry.getNode());
					linkCounter++;
				} else{
					terminated = true;
				}
			}
		}else{
			throw new NoSiteRetrievedException(linkCounter, "No data was found on path to link");
		}
		return sitesBenchmrks;
	}
 	
	private void goToNextPage() throws IOException, SAXException{
		WebResponse nextResp = getNextPage(nextMainPageResp);
		if (firstMainPageResp == actualMainPageResp && nextResp == null){
			progressWindow.printWarning("Could not go to the second page. NextPageLink node can be invalid");
		}
		actualMainPageResp.close();
		actualMainPageResp = nextMainPageResp;
		nextMainPageResp = nextResp;
	}
	
	private WebResponse getNextPage(WebResponse resp) throws IOException, SAXException{
		try{
			return LinkProcessor.goTo(resp, contentData.getNextPageLink());
		}catch (RetrievalException e){
			return null;
		}
	}
	
	private WebResponse getSite(String benchmark) throws SAXException, IOException, RetrievalException{
		return getSite(actualMainPageResp, benchmark);
	}
	
	private WebResponse getSite(WebResponse resp, String benchmark) throws SAXException, IOException, RetrievalException{
		try{
			progressWindow.printInfo("Processing \'" + benchmark + "\'");
			return LinkProcessor.goTo(resp, contentData.getLinkType(), benchmark);
		}catch(NullPointerException e){
			throw new RetrievalException("Data found at link path is not approriate link");
		}
	}
	
	private String getLinkBenchmark(RetrievalHelper.NodeAttrEntry entry){
		String benchmark;
		if (entry.getAttrs() == null){		//if searched data is kept in attribute
			 benchmark = RetrievalHelper.popDataFromNode(contentData.getDataType(), entry.getNode());
		}else{
			benchmark = entry.getAttrs().getNamedItem(contentData.getSearchDataType()).getNodeValue();
		}
		return benchmark;
	}
	
}

