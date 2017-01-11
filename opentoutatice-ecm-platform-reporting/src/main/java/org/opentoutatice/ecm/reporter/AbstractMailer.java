/**
 * 
 */
package org.opentoutatice.ecm.reporter;

import java.util.Map;

import javax.mail.MessagingException;

import org.nuxeo.ecm.platform.ec.notification.email.EmailHelper;


/**
 * @author david
 *
 */
public abstract class AbstractMailer implements Reporter {
    
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
    public void send(Object content) throws Exception {
        try {
            this.emailHelper.sendmail((Map<String, Object>) content);
        } catch (MessagingException e) {
            throw new Exception(e);
        }
    }

}
