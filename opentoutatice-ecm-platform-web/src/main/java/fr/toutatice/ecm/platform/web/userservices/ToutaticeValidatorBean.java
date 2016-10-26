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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.BooleanUtils;
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
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

import fr.toutatice.ecm.platform.automation.SetWebID;
import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfigurationConstants;
import fr.toutatice.ecm.platform.service.url.ToutaticeWebIdHelper;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;
import fr.toutatice.ecm.platform.web.local.configuration.WebConfsConfigurationActions;


/**
 * @author David Chevrier
 */
@Name("toutaticeValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeValidatorBean implements Serializable {

    private static final long serialVersionUID = -6136329151688755416L;

    private static final Log log = LogFactory.getLog(ToutaticeValidatorBean.class);

    private static final String DOMAIN_ID_UNICITY_QUERY = "select * from Domain where ecm:uuid <> '%s' and ttc:domainID = '%s' and ecm:currentLifeCycleState <> 'deleted'";

    @In(create = true, required = true)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected transient DocumentActions documentActions;


    final Pattern patternId = Pattern.compile("([a-zA-Z_0-9\\-\\_]+)");
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
     * Validate webId value.
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateWebId(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        String webId = (String) value;
        if (StringUtils.isNotBlank(webId)) {
            String msg = null;

            // format control
            Matcher m = this.patternId.matcher(webId);
            if (!m.matches()) {
                msg = ComponentUtils.translate(context, "label.toutatice.validator.malformed.webid");
            } else {

                // unicity control
                DocumentModel doc = null;
                try {
                    doc = ((ToutaticeDocumentActionsBean) this.documentActions).getCurrentDocument();

                    if (doc != null) {

                        if (SetWebID.UnrestrictedSilentSetWebIdRunner.isNotUnique(documentManager, doc, webId)) {
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
     * Test if a configuration document can be created given its code.
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateWebConfCode(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        UnrestrictedWebConfsChecker checker = new UnrestrictedWebConfsChecker(documentManager, context, value);
        checker.runUnrestricted();

    }

    private class UnrestrictedWebConfsChecker extends UnrestrictedSessionRunner {

        private FacesContext context;
        private Object value;

        protected UnrestrictedWebConfsChecker(CoreSession session, FacesContext context, Object value) {
            super(session);
            this.context = context;
            this.value = value;
        }

        @Override
        public void run() throws ClientException {

            String msg = ComponentUtils.translate(this.context, "osivia.label.validator.webConf.code");
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);

            WebConfsConfigurationActions webConfsActions = (WebConfsConfigurationActions) SeamComponentCallHelper
                    .getSeamComponentByName("webConfsConfigurationActions");
            DocumentModel currentDomain = navigationContext.getCurrentDomain();

            // We try to override or create new conf
            if (currentDomain != null) {
                if (currentDomain.hasFacet(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_FACET)) {

                    Boolean allDeniedConfs = (Boolean) currentDomain
                            .getPropertyValue(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_DENIED_ALL_PROPERTY);
                    if (BooleanUtils.isFalse(allDeniedConfs)) {
                        // Case of denied confs
                        List<DocumentModel> deniedWebConfs = webConfsActions.getNotSelectedConfs(currentDomain);
                        if (deniedWebConfs != null) {
                            for (Iterator<DocumentModel> it = deniedWebConfs.iterator(); it.hasNext();) {
                                DocumentModel selectedWebConf = it.next();
                                String code = (String) selectedWebConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
                                if (code.equals((String) this.value)) {
                                    throw new ValidatorException(message);
                                }
                            }
                        }
                        // Case of allowed confs where one (or more) is desactived
                        List<String> selectedWebConfs = webConfsActions.getAllowedWebConfs(currentDomain);
                        if (selectedWebConfs != null && !selectedWebConfs.contains((String) value)) {
                            throw new ValidatorException(message);
                        }
                    }

                } else {
                    List<String> globalWebConfs = webConfsActions.getGlobalWebconfsCodes();
                    if (globalWebConfs != null) {
                        if (!globalWebConfs.isEmpty() && !globalWebConfs.contains((String) value)) {
                            String msg_ = ComponentUtils.translate(this.context, "osivia.label.validator.webConf.global.code");
                            FacesMessage message_ = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg_, null);
                            throw new ValidatorException(message_);
                        }
                    }
                }
            }

        }

    }

}
