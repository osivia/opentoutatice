/**
 * 
 */
package org.opentoutatice.ecm.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.ecm.reporter.Reporter;
import org.opentoutatice.ecm.reporter.config.ReporterConfigurationService;
import org.opentoutatice.ecm.scanner.AbstractScanUpdater;
import org.opentoutatice.ecm.scanner.Scanner;
import org.opentoutatice.ecm.scanner.ScannerImpl;
import org.opentoutatice.ecm.scanner.config.ScannerConfigurationService;
import org.opentoutatice.ecm.scanner.directive.Directive;


/**
 * @author david
 *
 */
public class ReportingRunner {

    /** Logger. */
    private final static Log log = LogFactory.getLog(ReportingRunner.class);

    /** Scan configuration service. */
    private final ScannerConfigurationService scanCfg;

    /** ReporterRegistry. */
    private ReporterConfigurationService reporterCfg;

    /**
     * Default constructor.
     */
    public ReportingRunner() {
        super();
        this.scanCfg = Framework.getService(ScannerConfigurationService.class);
        this.reporterCfg = Framework.getService(ReporterConfigurationService.class);
    }

    /**
     * Gets scanner.
     * 
     * @param event
     * @return
     * @throws Exception
     */
    private Scanner getScanner(Event event) throws Exception {
        // Updater of Scanner
        AbstractScanUpdater scanUpdater = this.scanCfg.getUpdater(event);

        // Scanner
        return new ScannerImpl((AbstractScanUpdater) scanUpdater);
    }

    /**
     * Gets reporter.
     * 
     * @param event
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Reporter getReporter(Event event) throws InstantiationException, IllegalAccessException {
        return this.reporterCfg.getReporter(this.getClass().getName());
    }

    /**
     * Runner.
     * 
     * @param event
     * @throws Exception
     */
    public void run(Event event) throws Exception {
        // Get Directive
        Directive directive = scanCfg.getDirective(event);
        // Scan
        Scanner scanner = getScanner(event);
        Iterable<?> scannedObjects = scanner.scan(directive);

        try {
            if (scannedObjects != null) {
                // Updater
                AbstractScanUpdater updater = scanner.getUpdater();

                // Iterates
                Iterator<?> iterator = scannedObjects.iterator();

                // Index
                int index = 0;
                // Counter of treated objects
                int counter = 0;

                while (iterator.hasNext()) {
                    // Scanned object
                    Object scannedObject = iterator.next();

                    try {
                        // Filters
                        if (updater.accept(index, scannedObject)) {
                            // Initialize if necessary
                            scannedObject = updater.initialize(index, scannedObject);

                            // Reporter
                            Reporter reporter = getReporter(event);
                            // Build report
                            Object report = reporter.build(scannedObject);

                            // Send it
                            try {
                                reporter.send(report);
                            } catch (Exception e) {
                                try {
                                    // Update to send later
                                    updater.updateOnError(index, scannedObject);
                                } catch (Exception ue) {
                                    // Do not block
                                    logStackTrace(log, e);
                                }
                            }

                            // Update scannedObject
                            updater.update(index, scannedObject);

                            // Counter
                            counter++;
                        }
                    } catch (Exception e) {
                        // Counter
                        counter++;
                        // Logs
                        logStackTrace(log, e);
                    }

                    index++;
                }

                // Debug
                if (log.isDebugEnabled()) {
                    log.debug("[Treated objects]: " + counter + " / " + index);
                }
            }
        } finally {
            // If scannedObjects closable
            if (scannedObjects != null) {
                Class<?>[] parameterStype = null;
                Method method = scannedObjects.getClass().getMethod("close", parameterStype);

                if (method != null) {
                    Object[] args = null;
                    method.invoke(scannedObjects, args);
                }
            }
        }
    }

    /**
     * Logs stack trace in reporting.log.
     * 
     * @param log
     * @param e
     */
    private void logStackTrace(Log log, Throwable t) {

        StringWriter stringWritter = new StringWriter();
        PrintWriter printWritter = new PrintWriter(stringWritter, true);
        t.printStackTrace(printWritter);

        log.error("[ERROR]: " + stringWritter.toString());
    }

}
