package pl.edu.agh.kis.dataretrieval.retriever.webprocessors;

import java.io.IOException;

import org.mozilla.javascript.EcmaError;
import org.xml.sax.SAXException;

import pl.edu.agh.kis.dataretrieval.RetrievalException;
import pl.edu.agh.kis.dataretrieval.configuration.crawl.CrawlingData;
import pl.edu.agh.kis.dataretrieval.configuration.search.LinkData;

import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

public class LinkProcessor {

	public static WebResponse goTo(WebResponse resp, LinkData linkData) throws IOException, SAXException, RetrievalException{
		String linkType = linkData.getType();
		String benchmark = linkData.getBenchmark();

		return goTo(resp, linkType, benchmark);
	}
	
	public static WebResponse goTo(WebResponse resp, CrawlingData configNode) throws SAXException, IOException, RetrievalException{
		String linkType = configNode.getBenchmarkType();
		String benchmark = configNode.getBenchmark();
		
		return goTo(resp, linkType, benchmark);
	}
	
	public static WebResponse goTo(WebResponse resp, String linkType, String benchmark) throws SAXException, IOException, RetrievalException{
			
		WebLink link = getLink(resp, linkType, benchmark);
		for (int clicks = 0; clicks < 5; clicks++){
			try{
				return link.click();
			}catch (EcmaError e){
//				Do nothing
			}catch (RuntimeException e){
//				Do nothing
			}
		}
		throw new RetrievalException("Could not click link: " + benchmark);
	}
	
	private static WebLink getLink(WebResponse resp, String linkType, String benchmark) throws SAXException{
		WebLink link;
		if(linkType.equals("img")){
			link = resp.getLinkWithImageText(benchmark);
		}else if (linkType.equals("text")){
			link = resp.getLinkWith(benchmark);
		}else if (linkType.equals("id")){
			link = resp.getLinkWithID(benchmark);
		} else {
			link = resp.getLinkWithName(benchmark);
		}
		return link;
	}
}
