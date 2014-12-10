package pl.edu.agh.kis.dataretrieval;

import java.util.ArrayList;
import java.util.List;

import pl.edu.agh.kis.dataretrieval.configuration.search.SearchingData;
import pl.edu.agh.kis.dataretrieval.gui.FormWindow;

import com.meterware.httpunit.FormParameter;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

public class App {

	
	public static void main(String[] args) {
		
		WebConversation wc = new WebConversation();
		WebResponse resp = null;
		 try {
			   resp = wc.getResponse( "http://www.tizag.com/phpT/examples/formex.php/");
//			   WebLink l=resp.getLinkWith("establish a new session");
//			   l.click();
//			   
//			   TimeUnit.SECONDS.sleep(2);
//			   resp = wc.getCurrentPage();
//
			   WebForm form = resp.getForms()[0];
			   System.out.println("education Options");
			   for (String s: form.getParameterNames()){
				   System.out.println(s);
			   }
			   FormParameter p = form.getParameter("food[]");
			   System.out.println(p.getControls()[0].getType());
			  
//			   System.out.println(Arrays.asList(form.getOptions("science_year")));
//			   form.setParameter("science_year", "2012");
			   
			   List<SearchingData> list = new ArrayList<SearchingData>();
			   list.add(new SearchingData(SearchingData.FieldType.TEXT, "text"));
			   list.add(new SearchingData(SearchingData.FieldType.RADIO, "check"));
			   list.add(new SearchingData(SearchingData.FieldType.COMBOBOX, "select"));
			   FormWindow ss = new FormWindow(list);
			   ss.setVisible(true);
			   ss.getResult();
			 
//			   resp = form.submit();
//			   TimeUnit.SECONDS.sleep(1);
//			   form = resp.getForms()[0];
//			   resp = form.submit();
//			   TimeUnit.SECONDS.sleep(1);
//			   l=resp.getLinkWith("EURASIP J AUDIO SPEE");
//			   l.click();
//			   TimeUnit.SECONDS.sleep(1);
//			   resp = wc.getCurrentPage();
//
//			   Document dom = resp.getDOM();
//			   Configuration config = new Configuration();
//			   JournalCrawler jc = new JournalCrawler();
//			   CrawlerDao dao = new CrawlerDao();
//			   
//			   List<NodeData> nodes = config.load("config.xml");
//			   Map<String, List<FieldData>> siteData = jc.readData(dom, resp.getURL().toString(), wc, nodes);
//			   for (Entry<String, List<FieldData>> entry: siteData.entrySet()){
//				   dao.createTable(entry.getKey(), entry.getValue());
//				   dao.addSiteData(entry.getKey(), entry.getValue());
//			   }
//			   dao.closeConnection();
		  } catch (java.net.MalformedURLException e) {
		   System.out.println("Bad url: " + e);
		  } catch (java.io.IOException e) {
		   System.out.println("IO Err: " + e);
		  } catch (org.xml.sax.SAXException e) {
		   System.out.println("XML Err: " + e);
//		  } catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}

}
