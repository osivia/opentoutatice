package fr.toutatice.ecm.platform.automation;

import java.io.FileReader;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.FileBlob;

public class GetVocabulariesMain {
	
	public static void main(String[] args) throws Exception {
		HttpAutomationClient client = new HttpAutomationClient("http://localhost:8080/nuxeo/site/automation");
//		HttpAutomationClient client = new HttpAutomationClient("http://pp.toutatice.fr/nuxeo/site/automation");

		try {
			Session session = client.getSession("Administrator", "Administrator");
//			Session session = client.getSession("nxberhaut", "BERHAUT");
			Assert.assertNotNull(session);

			OperationRequest request = session.newRequest(GetVocabularies.ID);
			request.set("vocabularies", "niveauxEducatifs;niveauxEducatifs;niveauxEducatifs");
			
			Object result = request.execute();
			Assert.assertTrue(null != result);
			
			FileReader reader = new FileReader(((FileBlob) result).getFile());
			JSONParser jsonParser = new JSONParser();
            JSONArray jsonObject = (JSONArray) jsonParser.parse(reader);
            Iterator<Object> itr = jsonObject.iterator();
            while (itr.hasNext()) {
            	JSONObject elt = (JSONObject) itr.next();
            	System.out.println("> " + elt.get("value"));
            	
        		JSONArray children = (JSONArray ) elt.get("children");
                Iterator<Object> childItr = children.iterator();
                while (childItr.hasNext()) {
                	JSONObject child = (JSONObject) childItr.next();
                	System.out.println("   - " + child.get("value"));
                }
            }
			
			System.out.println("Fin de l'opération");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (null != client) {
				client.shutdown();
			}
		}
	}

}
