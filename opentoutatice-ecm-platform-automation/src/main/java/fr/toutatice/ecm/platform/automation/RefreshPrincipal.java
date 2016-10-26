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
 *    
 */
package fr.toutatice.ecm.platform.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

@Operation(id = RefreshPrincipal.ID, 
category = Constants.CAT_USERS_GROUPS, 
label = "Refresh princial", 
description = "Refresh the principal (to take into consideration LDAP updates)")
public class RefreshPrincipal {
	private static final Log log = LogFactory.getLog(RefreshPrincipal.class);
	public static final String ID = "Document.RefreshPrincipal";

	@Context
	protected OperationContext ctx;

	@OperationMethod
	public void run() throws Exception {
		try {
			NuxeoPrincipal principal = (NuxeoPrincipal) ctx.getPrincipal();
			UserManager userManager = Framework.getService(UserManager.class);
			DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);

			/**
			 * Refer to Jira for implementation: https://jira.nuxeo.com/browse/SUPNXP-10432
			 */
			
			// flush the user (LDAP) directories cache
			String userDirectoryName = userManager.getUserDirectoryName();
			Directory userDirectory = directoryService.getDirectory(userDirectoryName);
			userDirectory.getCache().invalidate(principal.getName());
			
			// refresh the principal (rebuild its data model)
			DocumentModel um = userManager.getUserModel(principal.getName());
			principal.setModel(um);
		} catch (Exception e) {
			log.warn("Failed to refresh the principal, error: " + e.getMessage());
			throw new ClientException(e);
		}
	}

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentModel doc) throws Exception {
		run();
		return ctx.getCoreSession().getDocument(doc.getRef());
	}

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentRef docRef) throws Exception {
		run();
		return ctx.getCoreSession().getDocument(docRef);
	}

}
