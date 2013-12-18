package fr.toutatice.ecm.platform.automation;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jbpm.graph.exe.ProcessInstance;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.jbpm.JbpmService;

@Operation(id = SendTaskNotification.ID, category = Constants.CAT_NOTIFICATION, label = "Send a notification event", since = "5.3.2", description = "Send a notification that aims to be catched and processed by the notification event service & listener. The event must be declared via the contribution to the target 'org.nuxeo.ecm.platform.ec.notification.service.NotificationService', point 'notifications'. The input is returned") 
public class SendTaskNotification {
	public static final String ID = "Notification.SendTaskNotification";

//	private static final Log log = LogFactory.getLog(SendTaskNotification.class);

    @Context
    protected OperationContext ctx;

	@Context
	protected CoreSession coreSession;

	@Context
	protected transient JbpmService jbpmService;

	@Param(name = "event", required = true, order = 0)
	protected String event;

	@Param(name = "actors", required = true, order = 1)
    protected String keyForActors;
	
	@Param(name = "comment", required = false, order = 2)
	protected String comment;
	
	protected ProcessInstance currentProcess;
	
	@OperationMethod()
	@SuppressWarnings("unchecked")
	public DocumentModel run(DocumentModel document) throws Exception {
		Principal principal = coreSession.getPrincipal();
		if (!(principal instanceof NuxeoPrincipal)) {
			throw new OperationException("Principal is not an instance of NuxeoPrincipal");
		}
		
		NuxeoPrincipal nuxpal = (NuxeoPrincipal) principal;
        List<String> recipients = new ArrayList<String>();
        Object actors = ctx.get(keyForActors);
        if (actors != null) {
        	boolean throwError = false;
        	try {
        		if (actors instanceof List) {
        			recipients.addAll((List<String>) actors);
        		} else if (actors instanceof String[]) {
        			for (String actor : (String[]) actors) {
        				recipients.add(actor);
        			}
        		} else if (actors instanceof String) {
        			recipients.add((String) actors);
        		} else {
        			throwError = true;
        		}
        	} catch (ClassCastException e) {
        		throwError = true;
        	}
        	
            if (throwError) {
                throw new OperationException(String.format("Invalid key to retrieve a list, array or single "
                        + "string of prefixed actor "
                        + "ids '%s', value is not correct: %s",
                        keyForActors, actors));
            }
        }

        if (!recipients.isEmpty()) {
        	jbpmService.notifyEventListeners(event,
        			StringUtils.isBlank(comment) ? " " : comment,
        					recipients.toArray(new String[] {}),
        					coreSession,
        					nuxpal, 
        					document);
        }
        
		return document;
	}
	
}
