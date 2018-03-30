package fr.toutatice.ecm.platform.automation.transaction;

import java.io.File;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Blob;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.FileBlob;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.client.model.PropertyMap;

/**
 * Create a document, update it, and attach a blob
 * @author jbarberet
 */
public class TxConversationTestCreateBlob {

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
                txId = IOUtils.toString(txIdAsBlob.getStream(), "UTF-8");
                System.out.println("[TXID]: " + txId + "\n");
                
                final String PATH = "/default-domain/workspaces/espace-tmc-0.1513599201682/documents";
                try {
                    System.out.println("TX id = "+txId);
                    // Creation
                    OperationRequest operationRequest = session.newRequest("Document.Create");
                    
                    createdDoc = (Document) operationRequest.setHeader("Tx-conversation-id", txId)
                            .setInput(new PathRef(PATH))
                            .set("type", "Note")
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

                    
                    Blob blob = new FileBlob(new File(new URI("file:///home/jbarberet/generali.pdf")));
                    
                    OperationRequest req = session.newRequest("Blob.Attach").setInput(blob).set(
                            "document", createdDoc.getPath());
                    req.setHeader(Constants.HEADER_NX_VOIDOP, "true");
                    req.setHeader("Tx-conversation-id", txId);
                    req.set("xpath", "ttc:vignette");
                    req.execute();
                    
                    System.out.println("Blob attached");
                    
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
