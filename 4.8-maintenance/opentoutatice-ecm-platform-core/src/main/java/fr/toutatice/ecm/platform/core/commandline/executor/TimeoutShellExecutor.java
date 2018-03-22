package fr.toutatice.ecm.platform.core.commandline.executor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.ExceptionUtils;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters.ParameterValue;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.EnvironmentDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.executors.ShellExecutor;


/**
 * ShellExecutor with timeout support
 *
 * @author Dorian Licois
 */
public class TimeoutShellExecutor extends ShellExecutor {

    private static final Log log = LogFactory.getLog(ShellExecutor.class);

    public static final String TIMEOUT_SHELL_EXECUTOR = "TimeoutShellExecutor";

    @Override
    protected ExecResult exec1(CommandLineDescriptor cmdDesc, CmdParameters params, EnvironmentDescriptor env) throws IOException {
        // split the configured parameters while keeping quoted parts intact
        final List<String> list = new ArrayList<>();
        list.add(cmdDesc.getCommand());
        final Matcher m = COMMAND_SPLIT.matcher(cmdDesc.getParametersString());
        while (m.find()) {
            String word;
            if (m.group(1) != null) {
                word = m.group(1); // double-quoted
            } else if (m.group(2) != null) {
                word = m.group(2); // single-quoted
            } else {
                word = m.group(); // word
            }
            final List<String> words = replaceParams(word, params);
            list.addAll(words);
        }

        final Map<String, ParameterValue> paramsValues = params.getParameters();
        final ParameterValue timeoutDurationP = paramsValues.get("timeoutDuration");
        Long timeoutDuration = null;
        if (timeoutDurationP != null) {
            timeoutDuration = NumberUtils.toLong(timeoutDurationP.getValue());
        }

        final List<Process> processes = new LinkedList<>();
        final List<Thread> pipes = new LinkedList<>();
        List<String> command = new LinkedList<>();
        Process process = null;
        for (final Iterator<String> it = list.iterator(); it.hasNext();) {
            final String word = it.next();
            boolean build;
            if (word.equals("|")) {
                build = true;
            } else {
                // on Windows, look up the command in the PATH first
                if (command.isEmpty() && SystemUtils.IS_OS_WINDOWS) {
                    command.add(getCommandAbsolutePath(word));
                } else {
                    command.add(word);
                }
                build = !it.hasNext();
            }
            if (!build) {
                continue;
            }
            final ProcessBuilder processBuilder = new ProcessBuilder(command);
            command = new LinkedList<>(); // reset for next loop
            processBuilder.directory(new File(env.getWorkingDirectory()));
            processBuilder.environment().putAll(env.getParameters());
            processBuilder.redirectErrorStream(true);
            final Process newProcess = processBuilder.start();
            processes.add(newProcess);
            if (process == null) {
                // first process, nothing to input
                IOUtils.closeQuietly(newProcess.getOutputStream());
            } else {
                // pipe previous process output into new process input
                // needs a thread doing the piping because Java has no way to connect two children processes directly
                // except through a filesystem named pipe but that can't be created in a portable manner
                final Thread pipe = pipe(process.getInputStream(), newProcess.getOutputStream());
                pipes.add(pipe);
            }
            process = newProcess;
        }

        // get result from last process
        @SuppressWarnings("null")
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        final List<String> output = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
        reader.close();

        // wait for all processes, get first non-0 exit status
        int returnCode = 0;
        for (final Process p : processes) {
            int exitCode = 0;
            try {

                if (timeoutDuration != null && timeoutDuration > 0) {
                    final boolean finished = p.waitFor(timeoutDuration, TimeUnit.SECONDS);
                    if (finished) {
                        exitCode = p.exitValue();
                    } else {
                        exitCode = 124;
                    }
                } else {
                    exitCode = p.waitFor();
                }

                if (returnCode == 0) {
                    returnCode = exitCode;
                }
            } catch (final InterruptedException e) {
                ExceptionUtils.checkInterrupt(e);
            }
        }

        // wait for all pipes
        for (final Thread t : pipes) {
            try {
                t.join();
            } catch (final InterruptedException e) {
                ExceptionUtils.checkInterrupt(e);
            }
        }

        return new ExecResult(null, output, 0, returnCode);
    }

}
