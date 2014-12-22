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
	
	public static List<WebResponse> findSites(WebResponse mainPageResp, ContentData contentData, Integer maxAmount, Integer startLink) throws SAXException, XPathExpressionException, IOException, DOMException, RetrievalException, NoSiteRetrievedException{
		List<WebResponse> sites = new ArrayList<WebResponse>();
		String expression = RetrievalHelper.getXpathExpression(contentData);
		
		boolean allRetrieved = false;
		
		while(maxAmount != 0 && !allRetrieved){
			String pathErrorMessage = new String();
			try{
				List<WebResponse> sitesFromOneSite = retrieveSites(mainPageResp, contentData, expression, startLink, maxAmount);
				
				sites.addAll(sitesFromOneSite);
				if (maxAmount != -1){
					maxAmount -= sitesFromOneSite.size();
				}
			}catch(RetrievalException e){
				pathErrorMessage = e.getMessage();
			}catch(NoSiteRetrievedException e){
				if (!sites.isEmpty()){
					return sites;
				}else{
					throw e;
				}
			}
			
			try{
				mainPageResp = LinkProcessor.goTo(mainPageResp, contentData.getNextPageLink());
				
			}catch (RetrievalException e){
				allRetrieved = true;
			}
			if (!allRetrieved && !pathErrorMessage.isEmpty()){
				throw new RetrievalException("Error while realizing \'next\' path: \n" + pathErrorMessage);
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
							sites.add(getSite(mainPageResp, entry, contentData));
						}else{
							terminated = true;
						}
					} else{
						sites.add(getSite(mainPageResp, entry, contentData));
					}
				}
				if (!RetrievalHelper.checkTermination(entry.getNode(), contentData)){
					entry = RetrievalHelper.realizePath(contentData.getSearchNextNodes(), entry.getNode());
					
					linkCounter++;
				} else{
					terminated = true;
				}
			}
		}
		if (sites.isEmpty()){ //gdy nie by³o problemu w realizowaniu sciezek, ale i tak nie zostala pobrana rzadna strona
			throw new NoSiteRetrievedException(linkCounter, "No site has been retrieved");
		}
		return sites;
	}
	
	private static WebResponse getSite(WebResponse mainPageResp, RetrievalHelper.NodeAttrEntry entry , ContentData contentData) throws SAXException, IOException, RetrievalException{
		try{
			String benchmark = getLinkBenchmark(entry, contentData);
			System.out.println("Gettink link to " + benchmark);
			return LinkProcessor.goTo(mainPageResp, contentData.getLinkType(), benchmark);
		}catch(NullPointerException e){
			throw new RetrievalException("Data found at link path is not approriate link");
		}
	}
	
	private static String getLinkBenchmark(RetrievalHelper.NodeAttrEntry entry , ContentData contentData){
		String benchmark;
		if (entry.getAttrs() == null){		//if searched data is kept in attribute
			 benchmark = RetrievalHelper.popDataFromNode(contentData.getDataType(), entry.getNode());
		}else{
			benchmark = entry.getAttrs().getNamedItem(contentData.getSearchDataType()).getNodeValue();
		}
		return benchmark;
	}
	
}

