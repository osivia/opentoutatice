/**
 * 
 */
package org.opentoutatice.ecm.reporter;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.platform.ec.notification.email.EmailHelper;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestMode;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestModeException;


/**
 * @author david
 *
 */
public abstract class AbstractMailer implements Reporter {

    /** Logger. */
    public static final Log log = LogFactory.getLog(AbstractMailer.class);

    /** Date formater. */
    public static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("dd/MM/yyyy");

    /** Mail key. */
    public static final String MAIL_TO = "mail.to";

    /** Conditional key property to send mail. */
    public static final String SEND_MAIL = "ottc.news.scan.send.mail";

    /** Mail data. */
    private Map<String, Object> data;

    /** EmailHelper. */
    private EmailHelper emailHelper;

    /**
     * 
     */
    public AbstractMailer() {
        super();
        this.emailHelper = new EmailHelper();
    }

    /**
     * @return the data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void send(Object content) throws MessagingException, ErrorTestModeException {
        // Error test mode
        if (ErrorTestMode.generateErrorInTry(4)) {
            throw new ErrorTestModeException("Error in Mailer#send");
        }

        try {
            // Conditional sending
            if (Framework.isBooleanPropertyTrue(SEND_MAIL)) {

                this.emailHelper.sendmail((Map<String, Object>) content);

                // Log
                if (log.isInfoEnabled()) {
                    log.info("         [Mail sent]");
                }
            } else {
                // Log
                if (log.isInfoEnabled()) {
                    log.info("         [Mail should have been sent]");
                }
            }

        } catch (Exception e) {
            throw new MessagingException("Can not send mail", e);
        }
    }

}
