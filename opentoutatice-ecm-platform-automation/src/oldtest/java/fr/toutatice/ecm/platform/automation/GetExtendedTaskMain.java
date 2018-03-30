package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;

import junit.framework.Assert;

public class GetExtendedTaskMain {

	public static void main(String[] args) throws Exception {
		HttpAutomationClient client = new HttpAutomationClient("http://ac-rennes-v3:8080/nuxeo/site/automation");

		try {
			Session session = client.getSession("nxjahier", "JAHIER");
			Assert.assertNotNull(session);

			OperationRequest request = session.newRequest(GetExtendedTasks.ID);			
			request.set("wkflsNames","");
			
			Object result = request.execute();
			Assert.assertTrue(null != result);
			
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
