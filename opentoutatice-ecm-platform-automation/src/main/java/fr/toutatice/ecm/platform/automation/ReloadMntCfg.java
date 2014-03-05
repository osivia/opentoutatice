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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;

import fr.toutatice.ecm.platform.core.services.maintenance.ToutaticeMaintenanceService;

@Operation(id = ReloadMntCfg.ID, 
	category = Constants.CAT_SERVICES, 
	label = "Reload the maintenance service configuration", description = "Force reload the whole configuration of the maintenance service. Accepts no parameter. Returns void.")
public class ReloadMntCfg {
	public static final String ID = "Services.ReloadMntCfg";
    
	@Context
	protected ToutaticeMaintenanceService mntService;
    
    @OperationMethod
    public void run() throws Exception {
    	mntService.reloadCfg();
    }

}
