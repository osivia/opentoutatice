/**
 * 
 */
package org.opentoutatice.ecm.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Iterator;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.opentoutatice.ecm.reporter.Reporter;
import org.opentoutatice.ecm.reporter.config.ReporterConfigurationService;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestMode;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestModeException;
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

    /** Transaction timeout parameter key. */
    public static final String TRANSAC_TIMEOUT = "ottc.scan.transaction.timeout";
    /** Default transaction timeout. */
    public static final String DEFAULT_TRANSAC_TIMEOUT = "900";

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
        // To be able to set transaction timeout cause we are yet in transaction (cf EventJob#execute)
        TransactionHelper.commitOrRollbackTransaction();
        // Transaction timeout
        Integer timeout = Integer.valueOf(Framework.getProperty(TRANSAC_TIMEOUT, DEFAULT_TRANSAC_TIMEOUT));
        TransactionHelper.startTransaction(timeout.intValue());

        // Get Directive
        Directive directive = scanCfg.getDirective(event);

        // Scan
        Scanner scanner = getScanner(event);
        Iterable<?> scannedObjects = scanner.scan(directive);

        // Use cases errors
        ErrorTestMode.incrementUCErrorsIndicator();

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

                if (log.isInfoEnabled()) {
                    log.info("== [Started] ==");
                }

                while (iterator.hasNext()) {
                    // Scanned object
                    Object scannedObject = iterator.next();

                    try {
                        // Filters
                        if (updater.acceptInNewTx(index, scannedObject)) {
                            // Initialize if necessary
                            scannedObject = updater.initializeInNewTx(index, scannedObject);

                            // Reporter
                            Reporter reporter = getReporter(event);
                            // Build report
                            Object report = reporter.build(index, scannedObject);

                            // Update scannedObject
                            updater.updateInNewTx(index, scannedObject);

                            // Send it
                            try {
                                reporter.send(report);
                            } catch (MessagingException | ErrorTestModeException e) {
                                try {
                                    // Update to send later
                                    updater.updateOnErrorInTx(index, scannedObject);
                                } catch (Exception ue) {
                                    // Do not block
                                    logStackTrace(log, ue);
                                }
                                // Do not block
                                logStackTrace(log, e);
                            }

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

                    // Use cases errors
                    ErrorTestMode.incrementUCErrorsIndicator();
                }

                // Error test mode
                ErrorTestMode.resetGeneratedUseCaseErrors();

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

            // To avoid timeout Exception on EventJob transaction commit
            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.startTransaction();

            if (log.isInfoEnabled()) {
                log.info("== [Ended] ==");
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
