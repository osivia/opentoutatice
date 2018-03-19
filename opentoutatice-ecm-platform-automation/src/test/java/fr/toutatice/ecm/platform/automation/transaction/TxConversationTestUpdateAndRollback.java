package fr.toutatice.ecm.platform.automation.transaction;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;

/**
 * Create 2 documents with the same webid
 * @author jbarberet
 */
public class TxConversationTestUpdateAndRollback {

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

                    // Fist step: creation of a document with ttc:webid=oFyFyS
                    final String PATH = "/default-domain/workspaces/espace-tmc-0.1513599201682/documents";
                    OperationRequest operationCreateRequest = session.newRequest("Document.TTCCreate");

                    Map<String, String> properties = new HashMap<>();
                    properties.put("ttc:webid", "b8yFyS");
                    properties.put("dc:title", "Alors?");
                    
                    Document createdDoc = (Document) operationCreateRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(new PathRef(PATH))
                            .set("type", "Note")
                            .set("properties", properties)
                            .execute();
                    System.out.println("Creation DONE: " + createdDoc.getPath() + " | " + createdDoc.getInputRef() + "\n");
                    
                    // Second step: creation of a second document
                    operationCreateRequest = session.newRequest("Document.TTCCreate");
                    
                    Document createdDoc2 = (Document) operationCreateRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(new PathRef(PATH))
                            .set("type", "Note")
                            .set("properties",properties)
                            .execute();
                    //On attendrait qu'une exception soit levée car ce ttc:webid existe déjà, mais comme la recherche ne se fait pas dans le cache, il n'y a pas d'exception de levée
                    
                    System.out.println("Creation DONE: " + createdDoc2.getPath() + " | " + createdDoc2.getInputRef() + "\n");

                    
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
