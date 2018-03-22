package fr.toutatice.ecm.platform.core.commandline.executor;

import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineExecutorComponent;
import org.nuxeo.runtime.model.ComponentContext;


public class TTCCommandLineExecutorComponent extends CommandLineExecutorComponent {

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
        executors.put(DEFAULT_EXECUTOR, new TimeoutShellExecutor());
    }

}
