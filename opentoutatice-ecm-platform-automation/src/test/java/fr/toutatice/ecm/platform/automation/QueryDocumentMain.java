package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;
import org.nuxeo.ecm.automation.core.operations.document.Query;

public class QueryDocumentMain {

	public static void main(String[] args) throws Exception {
		HttpAutomationClient client = new HttpAutomationClient("http://localhost:8080/nuxeo/site/automation");

		try {
			Session session = client.getSession("Administrator", "kamelia");

			OperationRequest request = session.newRequest(Query.ID);
			request.set("query", "SELECT * FROM Document WHERE dc:title LIKE 'L'arc et la flèche'");
			request.setHeader(Constants.HEADER_NX_SCHEMAS, "dublincore");
			Documents documents = (Documents) request.execute();
			
			for (Document doc : documents.list()) {
				System.out.println(doc.getTitle());
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
