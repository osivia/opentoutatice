package fr.toutatice.ecm.platform.core.services.maintenance;

import org.nuxeo.ecm.core.api.CoreSession;

public interface ToutaticeMaintenanceService {
	
	public void reloadCfg();
	public boolean isAutomationLogsEnabled(CoreSession session);
	public long getAutomationLogsThreshold(CoreSession session);
	
}
