package fr.toutatice.ecm.platform.core.executors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters.CmdParameter;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.commandline.executor.service.CommandLineDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.EnvironmentDescriptor;
import org.nuxeo.ecm.platform.commandline.executor.service.executors.ShellExecutor;
import org.nuxeo.log4j.ThreadedStreamGobbler;


/**
 * ShellExecutor with timeout support
 * 
 * @author Dorian Licois
 */
public class TimeoutShellExecutor extends ShellExecutor {

    private static final Log log = LogFactory.getLog(ShellExecutor.class);

    public static final String TIMEOUT_SHELL_EXECUTOR = "TimeoutShellExecutor";

    @Override
    public ExecResult exec(CommandLineDescriptor cmdDesc, CmdParameters params, EnvironmentDescriptor env) {
        long t0 = System.currentTimeMillis();
        List<String> output = Collections.synchronizedList(new ArrayList<String>());

        String[] cmd;
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                String[] paramsArray = getParametersArray(cmdDesc, params);
                cmd = new String[]{"cmd", "/C", cmdDesc.getCommand()};
                cmd = (String[]) ArrayUtils.addAll(cmd, paramsArray);
            } else {
                String paramsString = getParametersString(cmdDesc, params);
                cmd = new String[]{"/bin/sh", "-c", cmdDesc.getCommand() + " " + paramsString};
            }
        } catch (IllegalArgumentException e) {
            return new ExecResult(cmdDesc.getCommand(), e);
        }
        String commandLine = StringUtils.join(cmd, " ");
        
        HashMap<String, CmdParameter> paramsValues = params.getCmdParameters();
        CmdParameter timeoutDurationP = paramsValues.get("timeoutDuration");
        Long timeoutDuration = null;
        if (timeoutDurationP != null) {
            timeoutDuration = NumberUtils.toLong(timeoutDurationP.getValue());
        }

        Process p1;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Running system command: " + commandLine);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(new File(env.getWorkingDirectory()));
            processBuilder.environment().putAll(env.getParameters());
            p1 = processBuilder.start();
        } catch (IOException e) {
            return new ExecResult(commandLine, e);
        }

        ThreadedStreamGobbler out, err;
        if (cmdDesc.getReadOutput()) {
            out = new ThreadedStreamGobbler(p1.getInputStream(), output);
            err = new ThreadedStreamGobbler(p1.getErrorStream(), output);
        } else {
            out = new ThreadedStreamGobbler(p1.getInputStream(), SimpleLog.LOG_LEVEL_DEBUG);
            err = new ThreadedStreamGobbler(p1.getErrorStream(), SimpleLog.LOG_LEVEL_ERROR);
        }

        err.start();
        out.start();

        int exitCode = 0;
        try {
            if (timeoutDuration != null && timeoutDuration > 0) {
                boolean finished = waitFor(p1, timeoutDuration, TimeUnit.SECONDS);
                if (finished) {
                    exitCode = p1.exitValue();
                } else {
                    exitCode = 124;
                }
            } else {
                exitCode = p1.waitFor();
            }
            out.join();
            err.join();
        } catch (InterruptedException e) {
            return new ExecResult(commandLine, e);
        }

        long t1 = System.currentTimeMillis();
        return new ExecResult(commandLine, output, t1 - t0, exitCode);
    }

    /**
     * 
     * Causes the current thread to wait, if necessary, until the
     * subprocess represented by this {@code Process} object has
     * terminated, or the specified waiting time elapses.
     * 
     * 
     * <p>
     * If the subprocess has already terminated then this method returns
     * 
     * immediately with the value {@code true}. If the process has not
     * 
     * terminated and the timeout value is less than, or equal to, zero, then
     * 
     * this method returns immediately with the value {@code false}.
     * 
     * <p>
     * The default implementation of this methods polls the {@code exitValue}
     * 
     * to check if the process has terminated. Concrete implementations of this
     * 
     * class are strongly encouraged to override this method with a more
     * 
     * efficient implementation.
     * 
     * 
     * @param timeout the maximum time to wait
     * @param unit the time unit of the {@code timeout} argument
     * @return {@code true} if the subprocess has exited and {@code false} if
     *         the waiting time elapsed before the subprocess has exited.
     * @throws InterruptedException if the current thread is interrupted
     *             while waiting.
     * @throws NullPointerException if unit is null
     * 
     * @since 1.8
     */
    private boolean waitFor(Process p1, long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);

        do {
            try {
                p1.exitValue();
                return true;
            } catch (IllegalThreadStateException ex) {
                if (rem > 0)
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
            }
            rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        } while (rem > 0);
        return false;
    }
}
