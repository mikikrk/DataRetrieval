package pl.edu.agh.kis.dataretrieval;

import javax.xml.ws.Holder;

import pl.edu.agh.kis.dataretrieval.flow.FlowManager;
import pl.edu.agh.kis.dataretrieval.gui.windows.ProgressWindow;
import pl.edu.agh.kis.dataretrieval.gui.windows.StartWindow;

public class RetrievalMain {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length == 0){
			new Thread(){
				public void run(){
					StartWindow startWindow = new StartWindow();
					startWindow.setVisible(true);
				}
			}.start();
		}else{
			if (args.length != 2){
				System.out.println("Wrong usage of application\n"
						+ "To run window mode run application without arguments\n"
						+ "To run console mode run application with two arguments:\n"
						+ "	-searching configuration path\n"
						+ "	-crawling configuration path");
			}else{
				FlowManager siteRetriever = new FlowManager(args[0], args[1], false, true);
				new Thread(siteRetriever).start();
			}
		}
//		
//		WebConversation wc = new WebConversation();
//		WebResponse resp = null;
//		 try {
//			   resp = wc.getResponse( "http://www.tizag.com/phpT/examples/formex.php/");
////			   WebLink l=resp.getLinkWith("establish a new session");
////			   l.click();
////			   
////			   TimeUnit.SECONDS.sleep(2);
////			   resp = wc.getCurrentPage();
////
//			   WebForm form = resp.getForms()[0];
////			   System.out.println("education Options");
//			   for (String s: form.getParameterNames()){
//				   System.out.println(s);
//				   FormParameter p = form.getParameter(s);
//				   for (FormControl fc: p.getControls()){
//					   System.out.println(fc.getType());
//					   System.out.println(fc.getText());
//					   System.out.println(fc.getAttribute("size").isEmpty());
//					   for (String v: fc.getOptionValues())
//						   System.out.println(v);
//					   System.out.println();
//
//				   }
//				   System.out.println();
//			   }
////			   FormParameter p = form.getParameter("food[]");
////			   System.out.println(p.getControls()[0].getType());
//			  
////			   System.out.println(Arrays.asList(form.getOptions("science_year")));
////			   form.setParameter("science_year", "2012");
//			   
//			   List<FormFieldData> list = new ArrayList<FormFieldData>();
//			   FormWindow ss = new FormWindow(list);
//			   ss.setVisible(true);
//			   ss.waitForSubmit();
//			 
////			   resp = form.submit();
////			   TimeUnit.SECONDS.sleep(1);
////			   form = resp.getForms()[0];
////			   resp = form.submit();
////			   TimeUnit.SECONDS.sleep(1);
////			   l=resp.getLinkWith("EURASIP J AUDIO SPEE");
////			   l.click();
////			   TimeUnit.SECONDS.sleep(1);
////			   resp = wc.getCurrentPage();
////
////			   Document dom = resp.getDOM();
////			   Configuration config = new Configuration();
////			   JournalCrawler jc = new JournalCrawler();
////			   CrawlerDao dao = new CrawlerDao();
////			   
////			   List<NodeData> nodes = config.load("config.xml");
////			   Map<String, List<FieldData>> siteData = jc.readData(dom, resp.getURL().toString(), wc, nodes);
////			   for (Entry<String, List<FieldData>> entry: siteData.entrySet()){
////				   dao.createTable(entry.getKey(), entry.getValue());
////				   dao.addSiteData(entry.getKey(), entry.getValue());
////			   }
////			   dao.closeConnection();
//		  } catch (java.net.MalformedURLException e) {
//		   System.out.println("Bad url: " + e);
//		  } catch (java.io.IOException e) {
//		   System.out.println("IO Err: " + e);
//		  } catch (org.xml.sax.SAXException e) {
//		   System.out.println("XML Err: " + e);
////		  } catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		}
	}

}
