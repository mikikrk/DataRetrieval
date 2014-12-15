package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

import java.io.IOException;

import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.LinkData;

import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

public class LinkProcessor {

	public static WebResponse goTo(WebResponse resp, LinkData linkData) throws IOException, SAXException{
		String linkType = linkData.getType();
		String url = linkData.getBenchmark();

		return goTo(resp, linkType, url);
	}
	
	public static WebResponse goTo(WebResponse resp, CrawlingData configNode) throws SAXException, IOException{
		String linkType = configNode.getBenchmarkType();
		String url = configNode.getBenchmark();
		
		return goTo(resp, linkType, url);
	}
	
	public static WebResponse goTo(WebResponse resp, String linkType, String url) throws SAXException, IOException{
			
		WebLink link = getLink(resp, linkType, url);
		
		return link.click();
	}
	
	private static WebLink getLink(WebResponse resp, String linkType, String url) throws SAXException{
		WebLink link;
		if(linkType.equals("img")){
			link = resp.getLinkWithImageText(url);
		}else if (linkType.equals("text")){
			link = resp.getLinkWith(url);
		}else if (linkType.equals("id")){
			link = resp.getLinkWithID(url);
		} else {
			link = resp.getLinkWithName(url);
		}
		return link;
	}
}
