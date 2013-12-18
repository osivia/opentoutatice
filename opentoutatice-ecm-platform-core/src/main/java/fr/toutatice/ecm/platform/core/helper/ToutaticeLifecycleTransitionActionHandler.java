package fr.toutatice.ecm.platform.core.helper;

import org.jbpm.graph.exe.ExecutionContext;
import org.nuxeo.ecm.platform.jbpm.core.helper.LifecycleTransitionActionHandler;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;

public class ToutaticeLifecycleTransitionActionHandler extends LifecycleTransitionActionHandler {

	private static final long serialVersionUID = 1L;
	
    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        this.executionContext = executionContext;
        if (nuxeoHasStarted()) {
        	String endLifecycle = getEndLifecycleTransition();
        	if (!NuxeoStudioConst.CST_OPERATION_PARAM_NO_TRANSITION.equals(endLifecycle)) {
        		super.execute(executionContext);
        	} else {
        		executionContext.getToken().signal();
        	}
        }
    }
    
}
