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

public class FetchPublicationInfosMain {

    private static final String URL = "http://qt.toutatice.fr/nuxeo/site/automation";
    private static final String USER = "jgrall2";
    private static final String PWD = "qt";

    public static void main(String[] args) {
        Session session = null;
        try {
            HttpAutomationClient client = new HttpAutomationClient(URL);
            session = client.getSession(USER, PWD);

			OperationRequest request = session.newRequest(FetchPublicationInfos.ID);
            request.set("path", "/pole-mer-et-enseignement/service-mer-ens/dos-mer-ens/service-historique-de-la.proxy");
			
			Object result = request.execute();
			Assert.assertTrue(null != result);
			
			FileReader reader = new FileReader(((FileBlob) result).getFile());
			JSONParser jsonParser = new JSONParser();
            JSONArray jsonObject = (JSONArray) jsonParser.parse(reader);
            Iterator<Object> itr = jsonObject.iterator();
            while (itr.hasNext()) {
            	JSONObject elt = (JSONObject) itr.next();
                System.out.println("> " + elt.get("editableByUser"));
            	
                // JSONArray children = (JSONArray ) elt.get("children");
                // Iterator<Object> childItr = children.iterator();
                // while (childItr.hasNext()) {
                // JSONObject child = (JSONObject) childItr.next();
                // System.out.println("   - " + child.get("value"));
                // }
            }
			
			System.out.println("Fin de l'opération");
		} catch (Exception e) {
            System.out.println(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
	}

}
