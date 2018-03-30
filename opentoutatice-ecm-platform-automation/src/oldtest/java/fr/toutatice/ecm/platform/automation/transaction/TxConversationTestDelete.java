package fr.toutatice.ecm.platform.automation.transaction;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;

/**
 * Create a document and delete it
 * @author jbarberet
 */
public class TxConversationTestDelete {

    private static final String URL = "http://vm-jba-demo/nuxeo/site/automation";
    private static final String USER = "admin";
    private static final String PWD = "osivia";

    public static void main(String[] args) {
        Session session = null;
        String txId = null;
        Document createdDoc = null;
        try {
            HttpAutomationClient client = new HttpAutomationClient(URL);
            session = client.getSession(USER, PWD);

            // Start Tx
            Object object = session.newRequest("Repository.StartTransaction").execute();
            if (object instanceof FileBlob)
            {
                FileBlob txIdAsBlob = (FileBlob) object;
                try {
                    txId = IOUtils.toString(txIdAsBlob.getStream(), "UTF-8");
                    System.out.println("[TXID]: " + txId + "\n");

                    // Fist step: create a document
                    final String PATH = "/default-domain/workspaces/espace-tmc-0.1513599201682/documents";
                    OperationRequest operationCreateRequest = session.newRequest("Document.Create");

                    createdDoc = (Document) operationCreateRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(new PathRef(PATH))
                            .set("type", "Note")
                            .execute(); 
                    System.out.println("Creation DONE: " + createdDoc.getPath() + " | " + createdDoc.getInputRef() + "\n");

                    // 2nd step: delete the document
                    OperationRequest operationUpdateRequest = session.newRequest("Document.Delete");
                    operationUpdateRequest.setHeader("Tx-conversation-id", txId)
                    .setInput(createdDoc)
                    .execute();
                    System.out.println("Delete DONE");

                } catch (Exception e) {
                    //Mark transaction as rollback
                    System.out.println(e);
                    session.newRequest("Repository.MarkTransactionAsRollback").setHeader("Tx-conversation-id", txId).execute();
                } finally {
                    //Commit or rollback transaction
                    session.newRequest("Repository.CommitOrRollbackTransaction").setHeader("Tx-conversation-id", txId).execute();
                }
            } else
            {
                System.out.println("Pas réussi à faire l'appel à Start Transaction");
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
