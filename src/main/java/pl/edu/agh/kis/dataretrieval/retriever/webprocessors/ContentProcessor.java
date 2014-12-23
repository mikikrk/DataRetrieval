package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

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
import pl.edu.agh.kis.dataretrieval.configuration.search.ContentData;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.exceptions.NoNextSiteException;
import pl.edu.agh.kis.dataretrieval.retriever.webprocessors.exceptions.NoSiteRetrievedException;

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
	
	public ContentProcessor(WebResponse mainPageResp, ContentData contentData) throws IOException, SAXException, XPathExpressionException, DOMException, NoSiteRetrievedException, RetrievalException {
		super();
		this.firstMainPageResp = mainPageResp;
		this.contentData = contentData;
		init();
	}

	public ContentProcessor(WebResponse mainPageResp, ContentData contentData,
			Integer maxAmount, Integer startLink) throws IOException, SAXException, XPathExpressionException, DOMException, NoSiteRetrievedException, RetrievalException {
		super();
		this.firstMainPageResp = mainPageResp;
		this.contentData = contentData;
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
		}else{
			if (nextMainPageResp != null){
				return true;
			}else{
				return false;
			}
		}
	}
	
	public WebResponse findNextSite() throws SAXException, IOException, RetrievalException, XPathExpressionException, DOMException, NoSiteRetrievedException{
		int sitesAmount = sitesBenchmarksOnMainSite.size();
		if (iterator < sitesAmount){
			return getSite(sitesBenchmarksOnMainSite.get(iterator++));
		}else{
			if (nextMainPageResp != null){
				goToNextPage();
				actualizeSitesBenchmarks();
				iterator = 0;
				return findNextSite();
			}else{
				throw new NoNextSiteException();
			}
		}
	}

//	@Deprecated
//	public List<WebResponse> findSites() throws SAXException, XPathExpressionException, IOException, DOMException, RetrievalException, NoSiteRetrievedException{
//		List<WebResponse> sites = new ArrayList<WebResponse>();
//		String expression = RetrievalHelper.getXpathExpression(contentData);
//		WebResponse processingMainPageResp = firstMainPageResp;
//		
//		boolean allRetrieved = false;
//		
//		while(maxAmount != 0 && !allRetrieved){
//			String pathErrorMessage = new String();
//			try{
//				List<WebResponse> sitesFromOneSite = retrieveSites(processingMainPageResp, contentData, expression, startLink, maxAmount);
//				
//				sites.addAll(sitesFromOneSite);
//				if (maxAmount != -1){
//					maxAmount -= sitesFromOneSite.size();
//				}
//			}catch(RetrievalException e){
//				pathErrorMessage = e.getMessage();
//			}catch(NoSiteRetrievedException e){
//				if (!sites.isEmpty()){
//					return sites;
//				}else{
//					throw e;
//				}
//			}
//			
//			goToNextPage(processingMainPageResp);
//			if (!allRetrieved && !pathErrorMessage.isEmpty()){
//				throw new RetrievalException("Error while realizing \'next\' path: \n" + pathErrorMessage);
//			}
//		}
//		
//		return sites;
//	}
//	
//	@Deprecated
//	private List<WebResponse> retrieveSites(WebResponse mainPageResp, ContentData contentData, String findBenchmarkExpression, Integer startLink, Integer maxAmount) throws XPathExpressionException, SAXException, IOException, DOMException, RetrievalException, NoSiteRetrievedException{
//		XPath xPath =  XPathFactory.newInstance().newXPath();
//		Document mainPageDOM = mainPageResp.getDOM();
//		List<WebResponse> sites = new ArrayList<WebResponse>();
//		Integer linkCounter = 1;
//		
//		NodeList nodeList = (NodeList) xPath.compile(findBenchmarkExpression).evaluate(mainPageDOM, XPathConstants.NODESET);
//		Node node;
//		
//		if (contentData.getBenchmarkNo() != null){
//			node = nodeList.item(contentData.getBenchmarkNo());
//		}else{
//			node = nodeList.item(0);
//		}
//		
//		RetrievalHelper.NodeAttrEntry entry;
//	
//		try{
//			entry = RetrievalHelper.realizePath(contentData.getSearchPathNodes(), node);
//		}catch(RetrievalException e){
//			throw new NoSiteRetrievedException(linkCounter, e.getMessage());
//		}
//		
//		if (entry.getNode() != null){
//			boolean terminated = false;
//			while (!terminated){
//				if (linkCounter >= startLink){
//					if (maxAmount != -1){
//						if (linkCounter < startLink + maxAmount){
//							sites.add(getSite(mainPageResp, getLinkBenchmark(entry)));
//						}else{
//							terminated = true;
//						}
//					} else{
//						sites.add(getSite(mainPageResp, getLinkBenchmark(entry)));
//					}
//				}
//				if (!RetrievalHelper.checkTermination(entry.getNode(), contentData)){
//					entry = RetrievalHelper.realizePath(contentData.getSearchNextNodes(), entry.getNode());
//					
//					linkCounter++;
//				} else{
//					terminated = true;
//				}
//			}
//		}
//		if (sites.isEmpty()){ //gdy nie by³o problemu w realizowaniu sciezek, ale i tak nie zostala pobrana rzadna strona
//			throw new NoSiteRetrievedException(linkCounter, "No site has been retrieved");
//		}
//		return sites;
//	}
	
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
			System.out.println("WARN: Could not go to the second page. NextPageLink node can be invalid");
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
			System.out.println("Processing \'" + benchmark + "\'");
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

