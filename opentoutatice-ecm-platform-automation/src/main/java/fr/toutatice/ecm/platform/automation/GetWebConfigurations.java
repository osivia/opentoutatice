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
 *   mberhaut1
 *   dchevrier
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.jsf.OperationHelper;
import org.nuxeo.ecm.automation.seam.operations.SeamOperationFilter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;

import fr.toutatice.ecm.platform.service.fragments.configuration.ConfigurationBeanHelper;


/**
 * @author david chevrier
 *
 */
@Operation(id = GetWebConfigurations.ID, category = Constants.CAT_SERVICES, label = "Gets global and local web configurations", description = "Gets global and local web configuration.")
public class GetWebConfigurations {
    
    public static final String ID = "Context.GetWebConfigurations";
    
    /** Default conversation id. */
    public static final String CONVERSATION_ID = "0NXMAIN0";
    
    @Context
    protected OperationContext ctx;
    
    @Context
    protected CoreSession coreSession;
    
    @Param(name = "domainPath")
    protected String domainPath;
    
    @Param(name = "confType")
    protected String confType;
    
    @OperationMethod
    public DocumentModelList run() throws Exception {
        
        if (!OperationHelper.isSeamContextAvailable()) {
            SeamOperationFilter.handleBeforeRun(ctx, CONVERSATION_ID);
            try {
                return getConfigs();
            } finally {
                SeamOperationFilter.handleAfterRun(ctx, CONVERSATION_ID);
            }
        } else {
            return getConfigs();
        }
        
    }

    /**
     * @return configs.
     */
    private DocumentModelList getConfigs() {
        ConfigurationBeanHelper configHelper = ConfigurationBeanHelper.getBean();
        DocumentModel domain = coreSession.getDocument(new PathRef(domainPath));
        
        return configHelper.getConfigs(confType, coreSession, domain);
    }
    
}
