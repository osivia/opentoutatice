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
 *   lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.impl.InvokableMethod;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.utils.exception.ToutaticeException;

public class ToutaticeOperationHelper {

	private static final Log log = LogFactory.getLog(ToutaticeOperationHelper.class);

    private static AutomationService automationService;

	private ToutaticeOperationHelper() {
		// static class, cannot be instantiated
	}

	public static void runOperationChain(CoreSession session, String chainId, Object document) throws ToutaticeException {
		OperationContext context = new OperationContext(session);
		context.setInput(document);
		runOperationChain(session, chainId, context);
    }
	
	public static void runOperationChain(CoreSession session, String chainId, OperationContext context) throws ToutaticeException {
        try {
        	AutomationService automationService = getAutomationService();
            automationService.run(context, chainId);
        } catch (Exception e) {
        	List<String> documentsList = new ArrayList<String>();
        	Object input = context.getInput();
        	if (input instanceof DocumentModel) {
        		documentsList.add(((DocumentModel) input).getName());
        	} else if (input instanceof DocumentModelList) {
        		for (DocumentModel document : (DocumentModelList) input) {
            		documentsList.add(document.getName());
        		}
        	}
    		log.error("Failed to run the operation chain '" + chainId + "' on document '" + StringUtils.join(documentsList, ",") + "', error: " + e.getMessage());
        	throw new ToutaticeException(e);
        }
    }
	
	private static AutomationService getAutomationService() throws NuxeoException {
		if (automationService == null) {
			try {
				automationService = Framework.getService(AutomationService.class);
			} catch (Exception e) {
				final String errMsg = "Error connecting to AutomationService. " + e.getMessage();
				log.error(errMsg);
				throw new NuxeoException(errMsg, e);
			}
			
			if (automationService == null) {
				String errMsg = "AutomationService service not bound";
				log.error(errMsg);
				throw new NuxeoException(errMsg);
			}
		}
		return automationService;
	}
	
	/**
	 * Méthode permettant d'appeler une opération Nuxeo..
	 * 
	 * @param automation
	 *            Service automation
	 * @param ctx
	 *            Contexte d'exécution
	 * @param operationId
	 *            identifiant de l'opération
	 * @param parameters
	 *            paramètres de l'opération
	 * @return le résultat de l'opération dont le type n'est pas connu à
	 *         priori
	 * @throws ServeurException
	 */
	public static Object callOperation(OperationContext ctx, String operationId, Map<String, Object> parameters) throws ToutaticeException {
		
		Object operationRes = null;
		try {
			AutomationService automationService = getAutomationService();
			operationRes = automationService.run(ctx, operationId, parameters);
		
		} catch (Exception e) {
			DocumentModel document = (DocumentModel) ctx.getInput();
        	log.error("Failed to run the operation '" + operationId + "' on document '" + document.getName() + "', error: " + e.getMessage());
        	throw new ToutaticeException(e);
		}
		
		return operationRes;
	}


}
