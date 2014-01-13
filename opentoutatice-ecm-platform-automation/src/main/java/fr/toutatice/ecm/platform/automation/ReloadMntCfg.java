package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;

import fr.toutatice.ecm.platform.core.services.ToutaticeMaintenanceService;

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
