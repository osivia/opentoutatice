package fr.toutatice.ecm.platform.automation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

public class SetDocumentACLMain {

	private static final String ACE_DELIMITER = ",";
	private static final String ACE_FORMAT = "%s:%s:%s";
	
	public static void main(String[] args) throws Exception {
		HttpAutomationClient client = new HttpAutomationClient("http://localhost:8080/nuxeo/site/automation");

		try {
			Session session = client.getSession("Administrator", "Administrator");
			Assert.assertNotNull(session);

			OperationRequest request = session.newRequest(SetDocumentACL.ID);
			PathRef input = new PathRef("/default-domain.1392657624307/workspaces/espace-de-publication-1/p-1/article-1");
			request.setInput(input);
			request.set("acl", "local");
			request.set("overwrite", true);
			request.set("break", true);
			
	        final List<String> entries = new ArrayList<String>();
	        entries.add(String.format(ACE_FORMAT, "Contrib2", SecurityConstants.WRITE, false));
	        entries.add(String.format(ACE_FORMAT, "Gestionnaire1", SecurityConstants.READ, true));
	        entries.add(String.format(ACE_FORMAT, "GCONTRIBUTEURS", SecurityConstants.READ, true));
	        request.set("entries", StringUtils.join(entries, ACE_DELIMITER));
	        
			Document document = (Document) request.execute();
			Assert.assertTrue(null != document);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			if (null != client) {
				client.shutdown();
			}
		}
	}

}
