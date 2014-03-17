package fr.toutatice.ecm.platform.web.urlservice;

import static org.jboss.seam.ScopeType.EVENT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.platform.ui.web.rest.RestHelper;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.util.RepositoryLocation;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

@Name("restHelper")
@Scope(EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeRestHelper extends RestHelper {

    private static final Log log = LogFactory.getLog(ToutaticeRestHelper.class);


    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    public String findWebId(DocumentView docView) {
        String outcome = null;

        if (docView != null) {
            DocumentLocation docLoc = docView.getDocumentLocation();
            String serverName = docLoc.getServerName();
            if (serverName != null) {
                String webid = docLoc.getPathRef().value;

                // déduction du document
                DocumentModel doc;


                try {
                    DocumentModelList docs;
                    docs = documentManager.query("SELECT * FROM Document where ttc:webid = '" + webid + "'");
                    doc = docs.get(0);

                    // redirection
                    docLoc = new DocumentLocationImpl(doc);

                    DocumentRef docRef = docLoc.getDocRef();

                    RepositoryLocation repoLoc = new RepositoryLocation(serverName);
                    if (docRef != null) {
                        if (docView.getParameter(WebActions.MAIN_TAB_ID_PARAMETER) == null && !webActions.hasCurrentTabId(WebActions.MAIN_TABS_CATEGORY)) {
                            webActions.setCurrentTabId(WebActions.MAIN_TABS_CATEGORY, WebActions.DOCUMENTS_MAIN_TAB_ID);
                        }
                        outcome = navigationContext.navigateTo(repoLoc, docRef);

                    } else {
                        navigationContext.setCurrentServerLocation(repoLoc);
                    }


                } catch (ClientException e) {
                    log.error("Impossible de traiter la weburl " + webid + ". " + e);
                }
            }
            if (outcome == null) {
                outcome = docView.getViewId();
            }

        }

        return outcome;
    }
}
