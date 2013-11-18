package fr.toutatice.ecm.platform.web.publication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.ec.notification.NotificationConstants;
import org.nuxeo.ecm.platform.jbpm.JbpmEventNames;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.NuxeoJbpmException;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.api.PublishingEvent;
import org.nuxeo.ecm.platform.publisher.api.PublishingException;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.rules.PublishingValidatorException;
import org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeCoreProxyWithWorkflowFactory extends CoreProxyWithWorkflowFactory {

	private static final Log log = LogFactory.getLog(ToutaticeCoreProxyWithWorkflowFactory.class);
	
	public static final String ACAREN_TASK_NAME = "org.nuxeo.ecm.platform.publisher.jbpm.CoreProxyWithWorkflowFactory";

	@Override
	protected boolean isPublished(PublishedDocument publishedDocument, CoreSession session) throws PublishingException {
		boolean status = false;

		DocumentModel proxy = ((SimpleCorePublishedDocument) publishedDocument).getProxy();
		try {
			status = !isPublishedDocWaitingForPublication(proxy, session);
		} catch (Exception e) {
			log.error("Failed to get the published status from the document '" + proxy.getName() + "', error: " + e.getMessage());
		}

		return status;
	}

	@Override
	protected boolean hasValidationTask(DocumentModel proxy, NuxeoPrincipal currentUser) throws ClientException {
		boolean isValidator = isValidator(proxy, currentUser);
		boolean isDocWaitingForPublication = isPublishedDocWaitingForPublication(proxy, coreSession);
		return isDocWaitingForPublication && isValidator;
	}

	@Override
	protected void createTask(DocumentModel document, CoreSession session, NuxeoPrincipal principal) 
			throws PublishingValidatorException, ClientException, PublishingException {
		TaskInstance ti = new TaskInstance();
		String[] actorIds = getValidatorsFor(document);
		List<String> prefixedActorIds = new ArrayList<String>();
		for (String s : actorIds) {
			if (s.contains(":")) {
				prefixedActorIds.add(s);
			} else {
				UserManager userManager = Framework.getLocalService(UserManager.class);
				String prefix;
				try {
					prefix = userManager.getPrincipal(s) == null ? NuxeoGroup.PREFIX
							: NuxeoPrincipal.PREFIX;
				} catch (ClientException e) {
					throw new ClientRuntimeException(e);
				}
				prefixedActorIds.add(prefix + s);
			}
		}
		ti.setPooledActors(prefixedActorIds.toArray(new String[prefixedActorIds.size()]));
		Map<String, Serializable> variables = new HashMap<String, Serializable>();
		variables.put(JbpmService.VariableName.documentId.name(),
				document.getId());
		variables.put(JbpmService.VariableName.documentRepositoryName.name(),
				document.getRepositoryName());
		variables.put(JbpmService.VariableName.initiator.name(),
				principal.getName());
		ti.setVariables(variables);
		ti.setName(ACAREN_TASK_NAME);
		ti.setCreate(new Date());
		getJbpmService().saveTaskInstances(Collections.singletonList(ti));
		DocumentEventContext ctx = new DocumentEventContext(session, principal,
				document);
		ctx.setProperty(NotificationConstants.RECIPIENTS_KEY,
				prefixedActorIds.toArray(new String[prefixedActorIds.size()]));
		try {
			getEventProducer().fireEvent(ctx.newEvent(JbpmEventNames.WORKFLOW_TASK_ASSIGNED));
			getEventProducer().fireEvent(ctx.newEvent(JbpmEventNames.WORKFLOW_TASK_START));
			getEventProducer().fireEvent(ctx.newEvent(PublishingEvent.documentWaitingPublication.name()));
		} catch (ClientException e) {
			throw new PublishingException(e);
		}
	}

	protected void endTask(DocumentModel document, NuxeoPrincipal currentUser,
			CoreSession session, String comment, PublishingEvent event)
					throws PublishingException {
		try {
			final JbpmService jbpmService = getJbpmService();
			List<TaskInstance> tis = jbpmService.getTaskInstances(
					document, currentUser, null);
			String initiator = null;
			for (TaskInstance ti : tis) {
				if (ti.getName().equals(ACAREN_TASK_NAME)) {
					initiator = (String) ti.getVariable(JbpmService.VariableName.initiator.name());
					ti.end();
					jbpmService.saveTaskInstances(Collections.singletonList(ti));
					break;
				}
			}
			GetsProxySourceDocumentsUnrestricted runner = new GetsProxySourceDocumentsUnrestricted(session, document);
			runner.runUnrestricted();
			Map<String, Serializable> properties = new HashMap<String, Serializable>();
			if (initiator != null) {
				properties.put(NotificationConstants.RECIPIENTS_KEY,
						new String[] { initiator });
			}
			notifyEvent(event.name(), properties, comment, null,
					runner.liveDocument, session);
		} catch (NuxeoJbpmException e) {
			throw new PublishingException(e);
		} catch (ClientException ce) {
			throw new PublishingException(ce);
		}
	}

	protected JbpmService getJbpmService() {
		return Framework.getLocalService(JbpmService.class);
	}

	private class GetsProxySourceDocumentsUnrestricted extends
	UnrestrictedSessionRunner {

		public DocumentModel liveDocument;

		private DocumentModel sourceDocument;

		private final DocumentModel document;

		public GetsProxySourceDocumentsUnrestricted(CoreSession session,
				DocumentModel proxy) {
			super(session);
			this.document = proxy;
		}

		@Override
		public void run() throws ClientException {
			sourceDocument = session.getDocument(new IdRef(
					document.getSourceId()));
			liveDocument = session.getDocument(new IdRef(
					sourceDocument.getSourceId()));
		}
	}

}
