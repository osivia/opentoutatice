/**
 * 
 */
package org.opentoutatice.ecm.attached.images.bean;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.opentoutatice.core.io.download.TransientDocumentCache;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author david
 *
 */
@Name("documentActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE + 100)
public class OttcDocumentActionsBean extends ToutaticeDocumentActionsBean {

    private static final long serialVersionUID = -7497960175016224842L;

    private static final Log log = LogFactory.getLog(OttcDocumentActionsBean.class);

    public OttcDocumentActionsBean() {
        super();
    }

    @Override
    public String saveDocument(String viewId) throws NuxeoException {
        String toView = super.saveDocument(viewId);

        // Invalidate transient cache if images have been attached creating document
        OttcEditorImageActionsBean editorImgBean = (OttcEditorImageActionsBean) SeamComponentCallHelper.getSeamComponentByName("editorImageActions");

        String virtualChangeableDocId = editorImgBean.getVirtualChangeableDocId();
        if (StringUtils.isNotEmpty(virtualChangeableDocId)) {
            TransientDocumentCache.invalidate(virtualChangeableDocId);
            // Reset bean attribute
            editorImgBean.setVirtualChangeableDocId(null);
        }

        return toView;
    }

}