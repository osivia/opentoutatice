package fr.toutatice.ecm.platform.automation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.PortalSSOAuthInterceptor;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.PathRef;
import net.sf.json.JSONArray;

public class SetDocumentTagsMain {

	public static void main(String[] args) throws Exception {
		final HttpAutomationClient client = new HttpAutomationClient("http://ac-rennes-v3:8081/nuxeo/site/automation");
		client.setRequestInterceptor(new PortalSSOAuthInterceptor("nuxeo5secretkey", "Administrator"));

		try {
			final Session session = client.getSession();
			assertNotNull(session);

			final PathRef input = new PathRef("/Domaine TESTS/workspaces/testsetdocumenttags");
			final List<String> existingTags = getTags(session, input);
			assertNotNull(existingTags);
			assertFalse(existingTags.isEmpty());

			final List<String> newTags = Arrays.asList(generateTagLabel("newtag1"), generateTagLabel("newtag2"));

			final OperationRequest request = session.newRequest(SetDocumentTags.ID);
			request.setInput(input);
			request.set("labels", StringUtils.join(newTags, ","));
			request.set("username", "Administrator");

			request.execute();

			final List<String> currentTags = getTags(session, input);

			assertEquals(newTags, currentTags);

			System.out.println("SUCCESS : suppression des tags existants : [" + StringUtils.join(existingTags, ", ") + "] puis ajout des tags [" + StringUtils.join(newTags, ", ") + "] au document : " + input.getInputRef());
		} catch (final Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if (null != client) {
				client.shutdown();
			}
		}
	}

	private static String generateTagLabel(String prefix) {
		return prefix + "_" + new Date().getTime();
	}

	@SuppressWarnings("unchecked")
	private static List<String> getTags(Session session, PathRef docPath) throws Exception {
		final OperationRequest request = session.newRequest(GetDocumentTags.ID);
		request.setInput(docPath);

		final Blob rawResult = (Blob) request.execute();

		return JSONArray.toList(JSONArray.fromObject(IOUtils.toString(rawResult.getStream())));
	}

}
