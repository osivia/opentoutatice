/**
 * 
 */
package org.opentoutatice.ecm.attached.images.bean;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.tag.fn.Functions;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;
import org.nuxeo.ecm.platform.util.RepositoryLocation;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author david
 *
 */
@Name("documentActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.INHERIT_TOUTATICE)
public class OttcDocumentActionsBean extends ToutaticeDocumentActionsBean {

    private static final long serialVersionUID = -7497960175016224842L;
    private static final Log log = LogFactory.getLog(OttcDocumentActionsBean.class);

    public OttcDocumentActionsBean() {
        super();
    }
    
    @Override
    public void download(DocumentView docView) throws ClientException {
        if (docView != null) {
            DocumentLocation docLoc = docView.getDocumentLocation();
            // fix for NXP-1799
            if (documentManager == null) {
                RepositoryLocation loc = new RepositoryLocation(docLoc.getServerName());
                navigationContext.setCurrentServerLocation(loc);
                documentManager = navigationContext.getOrCreateDocumentManager();
            }
            
            DocumentModel doc = null;
            // Creation mode
            if(docLoc.getDocRef() == null){
                doc = super.getCurrentDocument();
            } else {
                doc = documentManager.getDocument(docLoc.getDocRef());
            }
            
            if (doc != null) {
                // get properties from document view
                Blob blob = DocumentFileCodec.getBlob(doc, docView);
                if (blob == null) {
                    log.warn("No blob for docView: " + docView);
                    return;
                }
                String filename = DocumentFileCodec.getFilename(doc, docView);
                // download
                FacesContext context = FacesContext.getCurrentInstance();
                if (blob.getLength() > Functions.getBigFileSizeLimit()) {
                    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                    HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

                    String bigDownloadURL = BaseURL.getBaseURL(request);
                    bigDownloadURL += "nxbigfile" + "/";
                    bigDownloadURL += doc.getRepositoryName() + "/";
                    bigDownloadURL += doc.getRef().toString() + "/";
                    bigDownloadURL += docView.getParameter(DocumentFileCodec.FILE_PROPERTY_PATH_KEY) + "/";
                    bigDownloadURL += URIUtils.quoteURIPathComponent(filename, true);
                    try {
                        response.sendRedirect(bigDownloadURL);
                    } catch (IOException e) {
                        log.error("Error while redirecting for big file downloader", e);
                    }
                } else {
                    ComponentUtils.download(context, blob, filename);
                }
            }
        }
    }

}
