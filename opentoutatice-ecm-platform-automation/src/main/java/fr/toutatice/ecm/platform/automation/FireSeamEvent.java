package fr.toutatice.ecm.platform.automation;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;

@Operation(id = FireSeamEvent.ID, category = Constants.CAT_NOTIFICATION, label = "Send Event in seam context", description = "Send a seam event.")
public class FireSeamEvent {
	public static final String ID = "Notification.SendSeamEvent";
	
    @Param(name = "name", required = true)
    protected String name;

    @OperationMethod
    public void run() throws Exception {
        if (StringUtils.isNotBlank(name)) {
        	Events.instance().raiseEvent(name);
        }
    }

}
