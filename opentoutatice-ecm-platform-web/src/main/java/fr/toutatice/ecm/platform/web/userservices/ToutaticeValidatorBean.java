/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * 
 * Contributors:
 * mberhaut1
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.web.userservices;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author David Chevrier
 */
@Name("toutaticeValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeValidatorBean {

    private static final Log log = LogFactory.getLog(ToutaticeValidatorBean.class);

    private static final String DOMAIN_ID_UNICITY_QUERY = "select * from Domain where ecm:uuid <> '%s' and ttc:domainID = '%s' and ecm:currentLifeCycleState <> 'deleted'";

    private static final String WEB_ID_UNICITY_QUERY = "select * from Document Where ttc:domainID = '%s'"
            + " AND ttc:webid = '%s' AND ecm:uuid <> '%s' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted' AND ecm:isCheckedInVersion = 0";

    @In(create = true, required = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;


    final Pattern patternId = Pattern.compile("([a-zA-Z_0-9\\-]+)");
    final Pattern patternExplicit = Pattern.compile("([a-zA-Z_0-9\\-\\/]+)");

    public void validateDomainIdUnicity(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        String domainID = (String) value;
        if (StringUtils.isNotBlank(domainID)) {
            String msg = null;


            // format control
            Matcher m = patternId.matcher(domainID);
            if (!m.matches()) {
                msg = ComponentUtils.translate(context, "label.toutatice.validator.malformed.domainid");
            } else {

                DocumentModel currentDomain = null;
                try {
                    currentDomain = ((ToutaticeDocumentActionsBean) documentActions).getCurrentDocument();
                } catch (ClientException ce) {
                    msg = ce.getMessage();
                }
                if (currentDomain != null) {
                    String domainUUID = currentDomain.getId();

                    DocumentModelList domains = null;
                    try {
                        domains = documentManager.query(String.format(DOMAIN_ID_UNICITY_QUERY, domainUUID, domainID));
                    } catch (ClientException e) {
                        msg = e.getMessage();
                    }
                    if (domains.size() > 0) {
                        msg = ComponentUtils.translate(context, "label.toutatice.validator.not.single.domainId");
                    }
                } else {
                    msg = ComponentUtils.translate(context, "label.toutatice.validator.no.domain");
                }
            }
            if (msg != null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
        }
    }


    /**
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateWebId(FacesContext context, UIComponent component, Object value) throws ValidatorException {


        String webID = (String) value;
        if (StringUtils.isNotBlank(webID)) {
            String msg = null;


            // format control
            Matcher m = patternId.matcher(webID);
            if (!m.matches()) {
                msg = ComponentUtils.translate(context, "label.toutatice.validator.malformed.webid");
            } else {


                // unicity control
                DocumentModel doc = null;
                try {
                    doc = ((ToutaticeDocumentActionsBean) documentActions).getCurrentDocument();


                    if (doc != null) {
                        Serializable domainID = doc.getPropertyValue("ttc:domainID");

                        DocumentModelList doubles = documentManager.query(String.format(WEB_ID_UNICITY_QUERY, domainID, webID, doc.getId()));

                        if (doubles.size() > 0) {
                            msg = ComponentUtils.translate(context, "label.toutatice.validator.webid.no.unicity");
                        }


                    } else {
                        msg = ComponentUtils.translate(context, "label.toutatice.validator.no.doc");
                    }
                } catch (ClientException ce) {
                    msg = ce.getMessage();
                }

            }

            if (msg != null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateExplicitUrl(FacesContext context, UIComponent component, Object value) throws ValidatorException {


        String explicitUrl = (String) value;
        if (StringUtils.isNotBlank(explicitUrl)) {
            String msg = null;

            // format control
            Matcher m = patternExplicit.matcher(explicitUrl);
            if (!m.matches()) {
                msg = ComponentUtils.translate(context, "label.toutatice.validator.malformed.explicit");
            }

            if (msg != null) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Get the parent space and look at the property "ttcs:hasWebIdEnabled"
     * 
     * @param doc
     * @return true if webid are enabled
     * @throws PropertyException
     * @throws ClientException
     */
    public Boolean getSpaceSupportsWebId() throws PropertyException, ClientException {


        DocumentModel doc = navigationContext.getCurrentDocument();

        // check if document belong to a space whose supports webid
        boolean spaceSupportsWebId = true;
		DocumentModelList spaces = ToutaticeDocumentHelper.getParentSpaceList(documentManager, doc, true, true, true);
		if (spaces != null && spaces.size() > 0) {

            DocumentModel space = spaces.get(0);
            Property hasWebIdEnabled = space.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICESPACE_WEBID_ENABLED);

            if (hasWebIdEnabled != null) {
                if (hasWebIdEnabled.getValue(Boolean.class) == false) {
                    spaceSupportsWebId = false;
                }
            } else {
                spaceSupportsWebId = false; // param in space is set to false
            }
        } else {
            spaceSupportsWebId = false; // space is not found
        }
        return spaceSupportsWebId;
    }

}
