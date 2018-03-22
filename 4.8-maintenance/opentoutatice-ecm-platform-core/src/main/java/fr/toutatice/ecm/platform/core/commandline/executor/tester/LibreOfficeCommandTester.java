/**
 * 
 */
package fr.toutatice.ecm.platform.core.commandline.executor.tester;

import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.cmdtesters.CommandTestResult;
import org.nuxeo.ecm.platform.commandline.executor.service.cmdtesters.CommandTester;

/**
 * @author david
 */
public class LibreOfficeCommandTester implements CommandTester {

    @Override
    public CommandTestResult test(CommandLineDescriptor cmdDescriptor) {
        String cmd = cmdDescriptor.getCommand();
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            return new CommandTestResult("command " + cmd + " not found in system path");
        }

        return new CommandTestResult();
    }

}
