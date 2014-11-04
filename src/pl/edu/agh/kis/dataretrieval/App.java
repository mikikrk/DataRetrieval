package pl.edu.agh.kis.dataretrieval;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;

import pl.edu.agh.kis.dataretrieval.database.CrawlerDao;
import pl.edu.agh.kis.dataretrieval.database.FieldData;
import pl.edu.agh.kis.dataretrieval.journal.configuration.Configuration;
import pl.edu.agh.kis.dataretrieval.journal.configuration.NodeData;
import pl.edu.agh.kis.dataretrieval.journal.crawler.JournalCrawler;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

public class App {

	
	public static void main(String[] args) {
		
		WebConversation wc = new WebConversation();
		WebResponse resp = null;
		 try {
			   resp = wc.getResponse( "http://admin-apps.webofknowledge.com/JCR/JCR?SID=");
			   WebLink l=resp.getLinkWith("establish a new session");
			   l.click();
			   TimeUnit.SECONDS.sleep(2);
			   resp = wc.getCurrentPage();
			   WebForm form = resp.getFormWithName("limits");
			   resp = form.submit();
			   TimeUnit.SECONDS.sleep(1);
			   form = resp.getForms()[0];
			   resp = form.submit();
			   TimeUnit.SECONDS.sleep(1);
			   l=resp.getLinkWith("EURASIP J AUDIO SPEE");
			   l.click();
			   TimeUnit.SECONDS.sleep(1);
			   resp = wc.getCurrentPage();

			   Document dom = resp.getDOM();
			   Configuration config = new Configuration();
			   JournalCrawler jc = new JournalCrawler();
			   CrawlerDao dao = new CrawlerDao();
			   
			   List<NodeData> nodes = config.load("config.xml");
			   Map<String, List<FieldData>> siteData = jc.readData(dom, resp.getURL().toString(), wc, nodes);
			   for (Entry<String, List<FieldData>> entry: siteData.entrySet()){
				   dao.createTable(entry.getKey(), entry.getValue());
				   dao.addSiteData(entry.getKey(), entry.getValue());
			   }
			   dao.closeConnection();
		  } catch (java.net.MalformedURLException e) {
		   System.out.println("Bad url: " + e);
		  } catch (java.io.IOException e) {
		   System.out.println("IO Err: " + e);
		  } catch (org.xml.sax.SAXException e) {
		   System.out.println("XML Err: " + e);
		  } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
