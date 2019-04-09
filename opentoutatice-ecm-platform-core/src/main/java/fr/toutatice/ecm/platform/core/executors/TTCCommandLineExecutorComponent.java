package fr.toutatice.ecm.platform.core.executors;

import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineExecutorComponent;
import org.nuxeo.runtime.model.ComponentContext;


public class TTCCommandLineExecutorComponent extends CommandLineExecutorComponent {

    public static final String ID = "fr.toutatice.ecm.platform.core.executors.TTCCommandLineExecutorComponent";

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        executors.put(DEFAULT_EXECUTOR, new TimeoutShellExecutor());
    }

}