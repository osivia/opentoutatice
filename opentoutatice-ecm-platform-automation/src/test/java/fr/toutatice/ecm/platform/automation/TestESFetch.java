package fr.toutatice.ecm.platform.automation;

import junit.framework.Assert;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Documents;

public class TestESFetch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HttpAutomationClient client = new HttpAutomationClient("http://vm-dch-dev:8081/nuxeo/site/automation");

		try {
			Session session = client.getSession("Administrator", "osivia");
			Assert.assertNotNull(session);

			OperationRequest request = session.newRequest("Document.Query");
			request.set("query", "select * from Note");
			request.setHeader(Constants.HEADER_NX_SCHEMAS,"*");
			
			Documents documents = (Documents) request.execute();
			
			Assert.assertTrue(null != documents);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (null != client) {
				client.shutdown();
			}
		}
	}
}
