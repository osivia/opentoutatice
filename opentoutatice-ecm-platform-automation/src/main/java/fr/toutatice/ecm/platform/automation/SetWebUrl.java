package fr.toutatice.ecm.platform.automation;

import org.jboss.seam.annotations.In;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

@Operation(id = SetWebUrl.ID, category = Constants.CAT_DOCUMENT, label = "Set weburl.",
        description = "Check unicity of weburl and apply to the document in current domain..")
public class SetWebUrl {

    public static final String ID = "Document.SetWebUrl";

    private static final String SEARCH_QUERY = "SELECT * FROM Document WHERE %s";

    private static final String CREATE_OR_COPY = "createOrCopyOp";
    private static final String MOVE_OR_RESTORE = "movedOrRestoredOp";
    private static final String MODIFY = "beforeModificationOp";

    @Context
    protected CoreSession coreSession;

    @In(create = true)
    protected NavigationContext navigationContext;
    
    private final Filter filter = new Filter() {

        @Override
        public boolean accept(DocumentModel docModel) {
            return docModel.getType().equals("Domain");
        }
    };

    @Param(name = "chainSource", required = true)
    protected String chainSource;


    @OperationMethod()
    public DocumentModel run(DocumentModel doc) throws Exception {

        boolean hasToBeUpdated = false;

        if (!doc.hasSchema(NuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
            return doc;
        }

        // en création ou copie la weburl est déduite du path initialisé par nuxeo
        String weburl = null;
        if (CREATE_OR_COPY.equals(chainSource)) {

            String[] arrayPath = doc.getPathAsString().split("/");
            weburl = arrayPath[arrayPath.length - 1];
            hasToBeUpdated = true;
        }
        // dans les autres modes, on prend la valeur déjà paramétrée
        else {
            if (doc.getPropertyValue(NuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBURL) != null) {
                weburl = doc.getPropertyValue(NuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBURL).toString();

                // Nettoyage espaces, accents, chaine trop longue.
                weburl = IdUtils.generateId(weburl, "-", true, 24);
            }
        }


        String domain = null;
        DocumentModelList parents = ToutaticeDocumentHelper.getParentList(coreSession, doc, filter, false);
        if (!parents.isEmpty()) {
            domain = parents.get(0).getPathAsString();
        }

        // si weburl évalué et domain renseigné
        if ((weburl != null && weburl.length() > 0) && domain != null) {
            

            // En moficiation, on notifie l'utilisateur que la weburl est déjà prise
            if (MODIFY.equals(chainSource)) {

                String searchDuplicatedWebUrl = "ecm:path startswith '" + domain + "' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'"
                        + " AND ttc:weburl = '" + weburl + "' AND ecm:path <>'" + doc.getPathAsString() + "'";

                String queryStr = String.format(SEARCH_QUERY, searchDuplicatedWebUrl);

                DocumentModelList query = coreSession.query(queryStr);
                
                if (query.size() > 0) {
                    throw new OperationException("L'identifiant weburl est déjà attribué à un autre contenu dans ce domaine.");
                }
            } else {

                // Pour les autres opérations, on vérifie l'unicité sans déclencher d'exception, la weburl est suffixée par un nombre
                boolean unicity = true;
                Integer suffix = null;
                String weburlconcat = weburl;
                do {

                    String searchDuplicatedWebUrl = "ecm:path startswith '" + domain + "' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'"
                            + " AND ttc:weburl = '" + weburlconcat + "' AND ecm:path <> '" + doc.getPathAsString() + "'";

                    String queryStr = String.format(SEARCH_QUERY, searchDuplicatedWebUrl);

                    DocumentModelList query = coreSession.query(queryStr);

                    if (query.size() > 0) {
                        unicity = false;
                        if (suffix == null)
                            suffix = 1;
                        else
                            suffix = suffix + 1;
                        weburlconcat = weburl.concat(".").concat(suffix.toString());
                    } else {
                        unicity = true;

                        if (!weburl.equals(weburlconcat)) {
                            weburl = weburlconcat;
                            hasToBeUpdated = true;
                        }
                    }
                } while (!unicity);

                // save weburl
                if (hasToBeUpdated) {
                    doc.setPropertyValue(NuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBURL, weburl);
                    this.coreSession.saveDocument(doc);
                }

            }
        }

        return doc;
    }
}
