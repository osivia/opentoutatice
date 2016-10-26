/**
 * 
 */
package fr.toutatice.ecm.platform.automation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.jaxrs.impl.HttpAutomationClient;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.PathRef;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 *
 */

public class SetWebIdTest {

    public static void main(String[] args) throws Exception {

        try {
            HttpAutomationClient client = new HttpAutomationClient("http://vm-lbi-foad:8081/nuxeo/site/automation");
            Session session = client.getSession("admin", "osivia44");
            
            final long beginCrea1 = System.currentTimeMillis();
            
            Document createdDoc = (Document) session.newRequest(CreateDocument.ID)
                    .setInput(new PathRef("/default-domain/workspaces/espace-pedagogie/documents"))
                    .set("type", "Note").set("properties", "dc:title=Note avec webid")
                    .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                    .execute();
            
            final long endCrea1 = System.currentTimeMillis(); 
            System.out.println("=====" + (endCrea1 - beginCrea1) + "=====");

            String createdWebId = createdDoc.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
            System.out.println("CREATED: " + createdWebId);
            
            Document otherCreatedDoc = null;
            Document otherCreatedDocForInc = null;
            Document orphanDraft = null;
            Document createdAfterOrphanDraft = null;

            try {
                // Unicity test
                final long beginCrea2 = System.currentTimeMillis();
                otherCreatedDoc = (Document) session.newRequest(CreateDocument.ID)
                        .setInput(new PathRef("/default-domain/workspaces/espace-pedagogie/documents"))
                        .set("type", "Note").set("properties", "dc:title=Note avec webid")
                        .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                        .execute();
                final long endCrea2 = System.currentTimeMillis(); 
                System.out.println("=====" + (endCrea2 - beginCrea2) + "=====");
                
                String otherCreatedWebId = otherCreatedDoc.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                System.out.println("OTHER CREATED: " + otherCreatedWebId);
                Assert.assertNotEquals(createdWebId, otherCreatedWebId);
                
                // WebId increment test
                final long beginCrea3 = System.currentTimeMillis();
                otherCreatedDocForInc = (Document) session.newRequest(CreateDocument.ID)
                        .setInput(new PathRef("/default-domain/workspaces/espace-pedagogie/documents"))
                        .set("type", "Note").set("properties", "dc:title=Note avec webid")
                        .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                        .execute();
                final long endCrea3 = System.currentTimeMillis(); 
                System.out.println("=====" + (endCrea3 - beginCrea3) + "=====");
                
                String otherCreatedDocIncWebId = otherCreatedDocForInc.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                System.out.println("OTHER CREATED INC: " + otherCreatedDocIncWebId);
                Assert.assertNotEquals(createdWebId, otherCreatedDocIncWebId);
                Assert.assertNotEquals(otherCreatedWebId, otherCreatedDocIncWebId);
                
                // Drafts tests
                final long beginCrea4 = System.currentTimeMillis();
                orphanDraft = (Document) session.newRequest(CreateDocument.ID)
                        .setInput(new PathRef("/default-domain/workspaces/espace-pedagogie/documents"))
                        .set("type", "Note").set("properties", "dc:title=Note avec webid")
                        .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                        .execute();
                final long endCrea4 = System.currentTimeMillis(); 
                System.out.println("=====" + (endCrea4 - beginCrea4) + "=====");
                
                String beforeDraftWebid = orphanDraft.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                System.out.println("BEFORE DRAFT ORPHAN WEBID: " + beforeDraftWebid);
                
                final long beginUpdate5 = System.currentTimeMillis();
                Document updatedDraftDoc = (Document) session.newRequest("Document.Update").setInput(orphanDraft).set(
                        "properties", "ttc:webid=draft_" + beforeDraftWebid)
                        .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                        .execute();
                final long endUpdate5 = System.currentTimeMillis(); 
                System.out.println("===== UPDATE: " + (endUpdate5 - beginUpdate5) + "=====");
                String draftOrphanWebid = updatedDraftDoc.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                System.out.println("DRAFT ORPHAN WEBID: " + draftOrphanWebid);
                Assert.assertEquals("draft_" + beforeDraftWebid, draftOrphanWebid);
                
                final long beginCrea6 = System.currentTimeMillis();
                createdAfterOrphanDraft = (Document) session.newRequest(CreateDocument.ID)
                        .setInput(new PathRef("/default-domain/workspaces/espace-pedagogie/documents"))
                        .set("type", "Note").set("properties", "dc:title=Note avec webid")
                        .setHeader(Constants.HEADER_NX_SCHEMAS, "toutatice")
                        .execute();
                final long endCrea6 = System.currentTimeMillis(); 
                System.out.println("=====" + (endCrea6 - beginCrea6) + "=====");
                String createdAfterOrphanWebid = createdAfterOrphanDraft.getProperties().getString(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                System.out.println("CREATED WEBID AFTER ORPHAN DRAFT: " + createdAfterOrphanWebid);
                Assert.assertNotEquals(draftOrphanWebid, createdAfterOrphanWebid);
                
            } catch (Exception e) {
                Assert.fail(e.getMessage());
            } finally {
                if (createdDoc != null) {
                    try {
                        session.newRequest("Document.Delete").setInput(createdDoc).execute();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                }
                
                if (otherCreatedDoc != null) {
                    try {
                        session.newRequest("Document.Delete").setInput(otherCreatedDoc).execute();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                }
                
                if (otherCreatedDocForInc != null) {
                    try {
                        session.newRequest("Document.Delete").setInput(otherCreatedDocForInc).execute();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                }
                
                if (orphanDraft != null) {
                    try {
                        session.newRequest("Document.Delete").setInput(orphanDraft).execute();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                }
                
                if (createdAfterOrphanDraft != null) {
                      try {
                          session.newRequest("Document.Delete").setInput(createdAfterOrphanDraft).execute();
                      } catch (Exception e) {
                          Assert.fail(e.getMessage());
                      }
                  }
                
            }

        } catch (Exception ce) {
            Assert.fail(ce.getMessage());
        }


    }

}
