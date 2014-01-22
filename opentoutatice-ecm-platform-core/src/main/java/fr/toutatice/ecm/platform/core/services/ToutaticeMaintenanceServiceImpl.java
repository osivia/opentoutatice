package fr.toutatice.ecm.platform.core.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeUserMngtHelper;

public class ToutaticeMaintenanceServiceImpl extends DefaultComponent implements ToutaticeMaintenanceService {

	private static final Log log = LogFactory.getLog(ToutaticeMaintenanceServiceImpl.class);
	private static final long DEFAULT_LOG_THRESHOLD = 150;
	private DocumentModel maintenanceDocument;

    @Override
    public void activate(ComponentContext context) throws Exception {
    	setModifiedNow();
        this.maintenanceDocument = null;
    }
    
	@Override
	public void reloadCfg() {
		this.maintenanceDocument = null;
	}

	@Override
	public long getAutomationLogsThreshold(CoreSession session) {
		long threshold = DEFAULT_LOG_THRESHOLD;

		try {
			DocumentModel mntDoc = getOrCreateMntDocument(session);
			if (null != mntDoc) {
				threshold =  (Long) mntDoc.getPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_MNT_AUTOMATION_LOGS_THRESHOLD);
			}
		} catch (Exception e) {
			log.error("Failed to check the automation logs threshold, error: " + e.getMessage());
		}

		return threshold;
	}

	@Override
	public boolean isAutomationLogsEnabled(CoreSession session) {
		boolean status = false;

		try {
			DocumentModel mntDoc = getOrCreateMntDocument(session);
			if (null != mntDoc) {
				Boolean prop = (Boolean) mntDoc.getPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_MNT_AUTOMATION_LOGS_STATUS);
				status = prop.booleanValue();
			}
		} catch (Exception e) {
			log.error("Failed to check the automation logs status, error: " + e.getMessage());
		}

		return status;
	}
	
	private DocumentModel getOrCreateMntDocument(CoreSession session) {
		if (null == this.maintenanceDocument) {
			try {
				MntDocumentGetter runner = new MntDocumentGetter(session);
				runner.runUnrestricted();
				this.maintenanceDocument = runner.getMntDocument(); 
			} catch (ClientException e) {
				log.error("Maintenance document cannot be obtained. cause: " + e.getMessage());
			}
		}

		return this.maintenanceDocument;
	}

	private class MntDocumentGetter extends UnrestrictedSessionRunner {
		private DocumentModel mntDocument;

		public MntDocumentGetter(CoreSession session) {
			super(session);
		}

		public DocumentModel getMntDocument() {
			return this.mntDocument;
		}

		@Override
		public void run() throws ClientException {

			List<String> administrators = ToutaticeUserMngtHelper.instance().getSuperAdministrators();
			if (0 < administrators.size()) {
				UserWorkspaceService userWorkspaceService = Framework.getLocalService(UserWorkspaceService.class);
				if (null != userWorkspaceService) {
					DocumentModel adminUserWorkspace = userWorkspaceService.getUserPersonalWorkspace(administrators.get(0), this.session.getRootDocument());
					String query = "SELECT * FROM "
							+ NuxeoStudioConst.CST_DOC_TYPE_MAINTENANCE
							+ " WHERE"
							+ " ecm:parentId = '" + adminUserWorkspace.getId() + "' "
							+ " AND ecm:isProxy = 0"
							+ " AND ecm:isCheckedInVersion = 0"
							+ " AND ecm:currentLifeCycleState != 'deleted'";
					DocumentModelList children = this.session.query(query);
					
					if (!children.isEmpty()) {
						DocumentRef mntDocRef = children.get(0).getRef();
						this.mntDocument = this.session.getDocument(mntDocRef); 
					} else {
						DocumentModel changeableDocument = this.session.createDocumentModel(adminUserWorkspace.getPathAsString(),
								NuxeoStudioConst.CST_DOC_TYPE_MAINTENANCE + "." + String.valueOf(System.currentTimeMillis()),
								NuxeoStudioConst.CST_DOC_TYPE_MAINTENANCE);
						this.mntDocument = this.session.createDocument(changeableDocument);
						this.session.save();
					}
					
					// detach from unrestricted session
					this.mntDocument.detach(true);

				} else {
					log.error("Maintenance document cannot be obtained. cause: failed to obtain the user worksapce service");
				}
			} else {
				log.error("Maintenance document cannot be obtained. cause: no administrator is defined in the user managment configuration.");
			}
		}

	}

//	@Override
//	public void enableAutomationServerLogs(CoreSession session) {
//		try {
//			DocumentModel mntDoc = getOrCreateMntDocument(session);
//			mntDoc.setPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_MNT_AUTOMATION_LOGS_STATUS, Boolean.TRUE);
//			updateMntDocument(session, mntDoc);
//		} catch (Exception e) {
//			log.error("Failed to enable the automation logs. error: " + e.getMessage());
//		}
//	}
//	
//	@Override
//	public void disableAutomationServerLogs(CoreSession session) {
//		try {
//			DocumentModel mntDoc = getOrCreateMntDocument(session);
//			mntDoc.setPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_MNT_AUTOMATION_LOGS_STATUS, Boolean.FALSE);
//			updateMntDocument(session, mntDoc);
//		} catch (Exception e) {
//			log.error("Failed to enable the automation logs. error: " + e.getMessage());
//		}
//	}
//	
//	private void updateMntDocument(CoreSession session, DocumentModel document) throws ClientException  {
//		UnrestrictedSessionRunner runner = new MntDocumentUpdater(session, document);
//		runner.runUnrestricted();
//		this.maintenanceDocument = null;
//	}
//	
//	private class MntDocumentUpdater extends UnrestrictedSessionRunner {
//		private DocumentModel mntDocument;
//
//		public MntDocumentUpdater(CoreSession session, DocumentModel document) {
//			super(session);
//			this.mntDocument = document;
//		}
//		
//		@Override
//		public void run() throws ClientException {
//			this.session.saveDocument(this.mntDocument);
//			this.session.save();
//		}
//
//	}

}
