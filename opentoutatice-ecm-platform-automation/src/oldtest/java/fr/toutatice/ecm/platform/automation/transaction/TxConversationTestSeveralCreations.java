package fr.toutatice.ecm.platform.automation.transaction;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Create several documents in a loop
 * @author jbarberet
 */
public class TxConversationTestSeveralCreations {

    private static final String URL = "http://vm-jba-demo/nuxeo/site/automation";
    private static final String USER = "admin";
    private static final String PWD = "osivia";
    private static final int SLEEP_TIME = 1000;

    public static void main(String[] args) {
        Session session = null;
        String txId = null;
        try {
            HttpAutomationClient client = new HttpAutomationClient(URL);
            session = client.getSession(USER, PWD);

            try {

                // Start Tx
                Object object = session.newRequest("Repository.StartTransaction").execute();
                if (object instanceof FileBlob)
                {
                    FileBlob txIdAsBlob = (FileBlob) object;

                    txId = IOUtils.toString(txIdAsBlob.getStream(), "UTF-8");
                    System.out.println("[TXID]: " + txId + "\n");
                    // Creation
                    final String PATH = "/default-domain/workspaces/espace-tmc-0.1513599201682/documents";

                    for (int i=0; i < 10; i++)
                    {
                        OperationRequest operationRequest = session.newRequest("Document.Create");
                        PropertyMap map = new PropertyMap();
                        map.set("dc:title", txId+"_document"+i);

                        Document createdDoc = (Document) operationRequest.setHeader("Tx-conversation-id", txId)
                                .setInput(new PathRef(PATH))
                                .set("type", "Note")
                                .set("properties", map)
                                .execute();
                        System.out.println("Creation DONE: " + createdDoc.getPath() + " | " + createdDoc.getInputRef() + "\n");
                        Thread.sleep(SLEEP_TIME);
                    }
                } else
                {
                    System.out.println("Pas réussi à faire l'appel à Start Transaction");
                }
            } catch (Exception e) {
                //Mark transaction as rollback
                System.out.println(e);
                session.newRequest("Repository.MarkTransactionAsRollback").setHeader("Tx-conversation-id", txId).execute();
            } finally {
                //Commit or rollback transaction
                session.newRequest("Repository.CommitOrRollbackTransaction").setHeader("Tx-conversation-id", txId).execute();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
