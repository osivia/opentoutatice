package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.InvalidChainException;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.OperationParameters;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.runtime.api.Framework;

@Operation(id = SetOnLine.ID, category = Constants.CAT_DOCUMENT, label = "Set on line a document", description = "Set on line a document (publish locally, acl copy).")
public class SetOnLine {
	
	public static final String ID = "Document.SetOnLineOperation";
	public static final String CHAIN_ID = "Document.SetOnLineChain";
	public static final String CHAIN_LOG_ID = "Log";
	
	@Context
	protected CoreSession session;

	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws Exception {
		UnrestrictedSetOnLineRunner setOnLineRunner = new UnrestrictedSetOnLineRunner(session, doc);
		setOnLineRunner.runUnrestricted();	
		
		logAudit(doc);
		
		return setOnLineRunner.getDocument();
	}

	private static class UnrestrictedSetOnLineRunner extends UnrestrictedSessionRunner {
		
		private DocumentModel document;
		
		public DocumentModel getDocument(){
			return this.document;
		}
		
		public UnrestrictedSetOnLineRunner(CoreSession session, DocumentModel document){
			super(session);
			this.document = document;
		}
		
		@Override
		public void run() throws ClientException {
			
			OperationChain onLineChain = new OperationChain(CHAIN_ID);
			
			OperationParameters publishOpParam = new OperationParameters("Document.TTCPPublish");
			DocumentModel parentDoc = this.session.getParentDocument(this.document.getRef());
			publishOpParam.set("target", parentDoc);
			publishOpParam.set("override", true);
			onLineChain.add(publishOpParam);
			
			OperationParameters copyAclsOpParam = new OperationParameters("Document.CopyACLs");
			copyAclsOpParam.set("the source document", this.document);
			copyAclsOpParam.set("Copy all ACLs", true);
			copyAclsOpParam.set("Overwrite", true);	
			onLineChain.add(copyAclsOpParam);
			
			try {
				runChain(this.session, this.document, onLineChain);
			} catch (Exception e) {
				throw new ClientException(e);
			}

		}
		
	}
	
	/*
	 * AcarenOperationHelper n'est pas utilisé car la chaîne doit être enregistrée
	 * pour référencement par id.
	 */
	private static void runChain(CoreSession session, DocumentModel doc, OperationChain logChain) throws Exception,
			OperationException, InvalidChainException {
		AutomationService automationService = Framework.getService(AutomationService.class);
		OperationContext context = new OperationContext(session);
		context.setInput(doc);
		automationService.run(context, logChain);
	}
	
	/*
	 * Déporté pour être exécuté dans la session utilisateur
	 */
	private void logAudit(DocumentModel doc) throws InvalidChainException, OperationException, Exception {
		OperationChain logChain = new OperationChain(CHAIN_LOG_ID);
		OperationParameters logOpParam = new OperationParameters("Audit.Log");
		logOpParam.set("event", "Mise en ligne");
		logOpParam.set("category", "Automation");
		logOpParam.set("comment", "Version " + doc.getVersionLabel());
		logChain.add(logOpParam);
		runChain(session, doc, logChain);

	}

}
