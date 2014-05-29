/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.core.helper;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ec.notification.NotificationConstants;
import org.nuxeo.ecm.platform.ec.notification.email.EmailHelper;
import org.nuxeo.ecm.platform.ec.notification.email.NotificationsRenderingEngine;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationService;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;
import org.nuxeo.ecm.platform.rendering.RenderingResult;
import org.nuxeo.ecm.platform.rendering.RenderingService;
import org.nuxeo.ecm.platform.rendering.impl.DocumentRenderingContext;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.freemarker.ToutaticeFunctions;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class ToutaticeEmailHelper extends EmailHelper {
	
	private static final Log log = LogFactory.getLog(ToutaticeEmailHelper.class);
	// used for loading templates from strings
    private final Configuration stringCfg = new Configuration();
	
	@Override
	 public void sendmail(Map<String, Object> mail) throws Exception {
		
        Session session = getSession();
        if (javaMailNotAvailable || session == null) {
            log.warn("Not sending email since JavaMail is not configured");
            return;
        }

        // Construct a MimeMessage
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(session.getProperty("mail.from")));
        Object to = mail.get("mail.to");
        if (!(to instanceof String)) {
            log.error("Invalid email recipient: " + to);
            return;
        }
        msg.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse((String) to, false));

        RenderingService rs = Framework.getService(RenderingService.class);

        DocumentRenderingContext context = new DocumentRenderingContext();
        context.remove("doc");
        context.putAll(mail);
        DocumentModel doc = (DocumentModel) mail.get("document");
		context.setDocument(doc);
        
		String link = (new ToutaticeFunctions()).getPermalink(doc);       
		context.put("docPermalink", link);   
        
        context.put("creator",doc.getPropertyValue("dc:creator"));
        
        boolean isOnLineWF = ToutaticeWorkflowHelper.isOnLineWorkflow(doc);
        context.put("isOnLineWF", isOnLineWF);
        String initiator = ToutaticeWorkflowHelper.getOnLineWorkflowInitiator(doc);
        context.put("initiator",initiator);
        
        String customSubjectTemplate = (String) mail.get(NotificationConstants.SUBJECT_TEMPLATE_KEY);
        if (customSubjectTemplate == null) {
            String subjTemplate = (String) mail.get(NotificationConstants.SUBJECT_KEY);
            Template templ = new Template("name",
                    new StringReader(subjTemplate), stringCfg);

            Writer out = new StringWriter();
            templ.process(mail, out);
            out.flush();

            msg.setSubject(out.toString(), "UTF-8");
        } else {
            rs.registerEngine(new NotificationsRenderingEngine(customSubjectTemplate));

            LoginContext lc = Framework.login();

            Collection<RenderingResult> results = rs.process(context);
            String subjectMail = "<HTML><P>No parsing Succeded !!!</P></HTML>";

            for (RenderingResult result : results) {
                subjectMail = (String) result.getOutcome();
            }
            subjectMail = NotificationServiceHelper.getNotificationService().getEMailSubjectPrefix()
                    + subjectMail;
            msg.setSubject(subjectMail, "UTF-8");

            lc.logout();
        }

        msg.setSentDate(new Date());

        rs.registerEngine(new NotificationsRenderingEngine((String) mail.get(NotificationConstants.TEMPLATE_KEY)));

        LoginContext lc = Framework.login();

        Collection<RenderingResult> results = rs.process(context);
        String bodyMail = "<HTML><P>No parsing Succedeed !!!</P></HTML>";

        for (RenderingResult result : results) {
            bodyMail = (String) result.getOutcome();
        }

        lc.logout();

        rs.unregisterEngine("ftl");

        msg.setContent(bodyMail, "text/html; charset=utf-8");

        // Send the message.
        Transport.send(msg);
	}
	
	   /**
     * Gets the session from the JNDI.
     */
    private static Session getSession() {
        Session session = null;
        if (javaMailNotAvailable) {
            return null;
        }
        // First, try to get the session from JNDI, as would be done under J2EE.
        try {
            NotificationService service = (NotificationService) Framework.getRuntime().getComponent(
                    NotificationService.NAME);
            InitialContext ic = new InitialContext();
            session = (Session) ic.lookup(service.getMailSessionJndiName());
        } catch (Exception ex) {
            log.warn("Unable to find Java mail API", ex);
            javaMailNotAvailable = true;
        }

        return session;
    }

}
