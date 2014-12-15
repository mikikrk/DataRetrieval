package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.FindNodeData;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.ContentData;
import pl.edu.agh.kis.dataretrieval.database.DbFieldData;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class ContentProcessor {
	
	public static List<WebResponse> findSites(WebResponse mainPageResp, ContentData contentData, Integer maxAmount) throws SAXException, XPathExpressionException, IOException, DOMException, RetrievalException{
		List<WebResponse> sites = new ArrayList<WebResponse>();
		String expression = RetrievalHelper.getXpathExpression(contentData);
		
		Integer startLink = contentData.getCrawledSites();
		boolean allRetrieved = false;
		
		while(maxAmount != 0 || !allRetrieved){
			try {
				List<WebResponse> sitesFromOneSite = retrieveSites(mainPageResp, contentData, expression, startLink, maxAmount);
				sites.addAll(sitesFromOneSite);
				if (maxAmount != -1){
					maxAmount -= sitesFromOneSite.size();
				}
			}catch(NoSiteRetrievedException e){
				if (startLink > 0){
					startLink -= e.getLinkCount();
				}else{
					throw new RetrievalException("No link has been found");
				}
			}
			try{
				mainPageResp = LinkProcessor.goTo(mainPageResp, contentData.getNextPageLink());
			}catch (NullPointerException e){
				allRetrieved = true;
			}
		}
		
		return sites;
	}
	
	private static List<WebResponse> retrieveSites(WebResponse mainPageResp, ContentData contentData, String findBenchmarkExpression, Integer startLink, Integer maxAmount) throws XPathExpressionException, SAXException, IOException, DOMException, RetrievalException, NoSiteRetrievedException{
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Document mainPageDOM = mainPageResp.getDOM();
		List<WebResponse> sites = new ArrayList<WebResponse>();
		Integer linkCounter = 1;
		
		NodeList nodeList = (NodeList) xPath.compile(findBenchmarkExpression).evaluate(mainPageDOM, XPathConstants.NODESET);
		Node node;
		
		if (contentData.getBenchmarkNo() != null){
			node = nodeList.item(contentData.getBenchmarkNo());
		}else{
			node = nodeList.item(0);
		}
		
		RetrievalHelper.NodeAttrEntry entry = RetrievalHelper.realizePath(contentData.getSearchPathNodes(), node);
		if (entry.getNode() != null){
			boolean terminated = false;
			while (!terminated){
				if (linkCounter > startLink){
					if (maxAmount != -1){
						if (linkCounter < startLink + maxAmount){
							sites.add(getSite(mainPageResp, entry, contentData));
						}else{
							terminated = true;
						}
					}else{
						sites.add(getSite(mainPageResp, entry, contentData));
					}
				}
				if (!RetrievalHelper.checkTermination(entry.getNode(), contentData)){
					entry = RetrievalHelper.realizePath(contentData.getSearchNextNodes(), entry.getNode());
					linkCounter++;
				}else{
					terminated = true;
				}
			}
		}
		if (sites.isEmpty()){
			throw new NoSiteRetrievedException(linkCounter);
		}
		return sites;
	}
	
	private static WebResponse getSite(WebResponse mainPageResp, RetrievalHelper.NodeAttrEntry entry , ContentData contentData) throws SAXException, IOException{
		String url = getUrl(entry, contentData);
		return LinkProcessor.goTo(mainPageResp, contentData.getLinkType(), url);
	}
	
	private static String getUrl(RetrievalHelper.NodeAttrEntry entry , ContentData contentData){
		String url;
		if (entry.getAttrs() == null){		//if searched data is kept in attribute
			 url = RetrievalHelper.popDataFromNode(contentData.getDataType(), entry.getNode());
		}else{
			url = entry.getAttrs().getNamedItem(contentData.getSearchDataType()).getNodeValue();
		}
		return url;
	}
	
}

