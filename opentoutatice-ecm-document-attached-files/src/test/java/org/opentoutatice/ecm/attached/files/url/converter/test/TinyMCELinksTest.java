/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.converter.test;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;
import org.opentoutatice.ecm.attached.files.url.converter.DocumentContentConverter;


/**
 * @author david
 *
 */
@RunWith(FeaturesRunner.class)
@Features(RuntimeFeature.class)
public class TinyMCELinksTest {

    /** Set of absolute external URLs. */
    public static final String[] ABS_EXT_LINK = {"http://www.osivia.org", "https://www.youtube.com", "mailto:dchevrier@osivia.com",
            "nxdrive://edit/http/vm-dch-dev/nuxeo/repo/default/nxdocid/610104e0-fa7c-451a-9a88-14870a049fff/filename/90-portal.ldif"};
    /** Set of absolute internal URLs. */
    // TODO: /portalRef?
    public static final String[] ABS_INT_LINK = {
            "http://ac-rennes-v3/nuxeo/nxpath/default/ckr/eptest/pagetest/filewf@view_documents?tabIds=%3AATAB_EDIT&conversationId=0NXMAIN1",
            "http://vm-dch-dev/portal/auth/pagemarker/5/cms/default-domain/workspaces/espace-pedagogie/test-mail"};

    /** Set of absolute like URLs. */
    public static final String[] ABS_LIKE_LINK = {"//www.osivia.org", "www.osivia.org", "osivia.org"};

    /** Set of relative Nx resources URLs. */
    public static final String[] NX_SRC_LINKS = {"web/bernie-mini3?content=Medium", "nxfile/default/attachedImages/ttc:images/0/file/chat.jpeg",
            "nxfile/default/df253d7f-8811-42d7-b853-512df435c1b0/ttc:images/0/file/rat.jpg?conversationId=0NXMAIN1"};
    
    /** Set of relative Nx resources URLs. */
    public static final String[] NX_DOWNLOAD_SRC_LINKS = {"nxfile/default/1182d4e1-81bc-4e96-bf8e-b4e2bc6bdcd6/blobholder:0/Erreurs%20%C3%A0%20l'import%20de%20fichiers.txt"};

    /**
     * Set of relative bad Nx resources URLs.
     * Tey are bad caus tiniyMCE doesn't allow beginning with /.
     */
    //public static final String[] BAD_NX_SRC_LINKS = {"/web/bernie-mini3?content=Medium", "/nxfile/default/attachedImages/ttc:images/0/file/chat.jpeg"};

    /** Set of relative Nx document URLs. */
    public static final String[] NX_DOC_LINKS = {
            "nxdoc/default/dcb06690-4e47-4af3-8c29-404e48a1f3e1/view_documents",
            "nxdoc/default/dcb06690-4e47-4af3-8c29-404e48a1f3e1/view_documents@view_documents?tabIds=MAIN_TABS%3Adocuments%2C%3A&old_conversationId=0NXMAIN2",
            "nxpath/default/default-domain/workspaces/espace-pedagogie/test-mail/too-move/90-portal-ldif",
            "nxpath/default/default-domain/workspaces/espace-pedagogie/test-mail/too-move/90-portal-ldif@view_documents?tabIds=MAIN_TABS%3Adocuments%2C%3A&old_conversationId=0NXMAIN2",
            "web/bernie-mini", "web/bernie-mini?tabIds=MAIN_TABS%3Adocuments%2C%3A&old_conversationId=0NXMAIN2"};

    /** List of possibilities. */
    public static final String[][] ALL_POSSIBLE_LINKS = {ABS_EXT_LINK, ABS_INT_LINK, ABS_LIKE_LINK, NX_SRC_LINKS, /*BAD_NX_SRC_LINKS,*/ NX_DOC_LINKS};

    /** Content links skeleton. */
    public static final String CONTENT_LINKS_SKELETON = "<p><a href=\"%s\">%s</a></p><br/>";

    /** Content resources skeleton. */
    public static final String CONTENT_SRC_SKELETON = "<p><img src=\"%s\">%s</a></p><br/>";

    @Test
    public void test_links() throws Exception {
        // Content to test
        String content = StringUtils.EMPTY;
        
        String[][] POSSIBLE_LINKS = {NX_DOWNLOAD_SRC_LINKS};

        for (String[] links : POSSIBLE_LINKS) {
            for (String link : links) {
                // Label of html image or link
                String label = StringUtils.substringAfterLast(link, "/");
                if (StringUtils.isEmpty(label)) {
                    label = StringUtils.substringBefore(link, "/");
                }
                // TODO: remove this later:
                label = "OOO";
                
                // a tags
                content = content.concat(String.format(CONTENT_LINKS_SKELETON, link, label));
                // img tags
                //content = content.concat(String.format(CONTENT_SRC_SKELETON, link, label));
            }
        }
        // Do it as unit test .... (no ... sysou) !!
        
        // Display content
        String[] input = StringUtils.splitByWholeSeparator(content, "<br/>");
        for(String tag : input){
            System.out.println(tag);
        }
        
        // Conversion
        String convertedContent = (String) new DocumentContentConverter().getAsObject(null, null, content);

        // Display converted content
        System.out.println("============================ \r\n");
        String[] output = StringUtils.splitByWholeSeparator(convertedContent, "<br/>");
        for(String tag : output){
            System.out.println(tag);
        }
    }
}
