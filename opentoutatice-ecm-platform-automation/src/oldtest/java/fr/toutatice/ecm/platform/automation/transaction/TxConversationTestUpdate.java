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
 * Create a document and update it
 * @author jbarberet
 */
public class TxConversationTestUpdate {

    private static final String URL = "http://vm-jba-demo/nuxeo/site/automation";
    private static final String USER = "admin";
    private static final String PWD = "osivia";

    public static void main(String[] args) {
        Session session = null;
        String txId = null;
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

                    // Fist step: creation
                    final String PATH = "/default-domain/workspaces/espace-tmc-0.1513599201682/documents";
                    OperationRequest operationCreateRequest = session.newRequest("Document.Create");

                    Document createdDoc = (Document) operationCreateRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(new PathRef(PATH))
                            .set("type", "Note")
                            //                            .set("properties",propertyMap)
                            .execute();
                    System.out.println("Creation DONE: " + createdDoc.getPath() + " | " + createdDoc.getInputRef() + "\n");

                    //Then, update document
                    PropertyMap propertyMap = new PropertyMap();
                    propertyMap.set("ttc:keywords", "test");
                    
                    OperationRequest operationUpdateRequest = session.newRequest("Document.Update");
                    Document createdDoc2 = (Document) operationUpdateRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(createdDoc)
                            .set("properties", propertyMap)
                            .execute();
                    System.out.println("Update DONE: " + createdDoc2.getPath() + " | " + createdDoc2.getInputRef() + "\n");
                    
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
                Document document = (Document) object;
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
