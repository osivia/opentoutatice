package fr.toutatice.ecm.platform.service.inheritance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.actions.Action;
import org.nuxeo.ecm.platform.actions.ActionContext;
import org.nuxeo.ecm.platform.actions.ejb.ActionManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;

public class ToutaticeInheritanceServiceImpl extends DefaultComponent implements ToutaticeInheritanceService {

	private static final Log log = LogFactory.getLog(ToutaticeInheritanceServiceImpl.class);
	public static final ComponentName ID = new ComponentName("fr.toutatice.ecm.platform.service.inheritance");

	public static final String EXTENSION_POINTS_SETTERS = "setters";

	protected final Map<String, ToutaticeInheritanceSetterDescriptor> settersDescriptors;
	protected ActionManager actionManager;

	public ToutaticeInheritanceServiceImpl() {
		this.settersDescriptors = new HashMap<String, ToutaticeInheritanceSetterDescriptor>();
	}

	@Override
	public void deactivate(ComponentContext context) throws Exception {
		this.settersDescriptors.clear();
	}

	@Override
	public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
		if (EXTENSION_POINTS_SETTERS.equals(extensionPoint)) {
			ToutaticeInheritanceSetterDescriptor setterContribution = (ToutaticeInheritanceSetterDescriptor) contribution;
			Class<?> clazz = setterContribution.getClazz();
            if (null != clazz && ToutaticeInheritanceSetter.class.isAssignableFrom(clazz)) {
            	ToutaticeInheritanceSetter setter = (ToutaticeInheritanceSetter) clazz.newInstance();
            	setterContribution.setSetter(setter);
				this.settersDescriptors.put(setterContribution.getName(), setterContribution);
			} else {
				log.warn("Failed to register the contribution '" + contributor.getName() + "'. Either a null clazz is defined or it doesn't implement the interface 'ToutaticeInheritanceSetter'");
			}
		}
	}
	
	@Override
	public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
		if (EXTENSION_POINTS_SETTERS.equals(extensionPoint)) {
			ToutaticeInheritanceSetterDescriptor setterContribution = (ToutaticeInheritanceSetterDescriptor) contribution;
			this.settersDescriptors.remove(setterContribution.getName());
		}
	}

	@Override
	public void runSync(Event event) {
		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
		CoreSession session = eventContext.getCoreSession();
		
		try {
			ToutaticeSilentProcessRunnerHelper runner = new InheritanceSilentModeRunner(session, event, false);
			runner.silentRun(true);
		} catch (ClientException e) {
			log.error("Failed to run the inheritance process, error: " + e.getMessage());
		}
	}
	
	@Override
	public void runAsync(Event event) {
		DocumentEventContext eventContext = (DocumentEventContext) event.getContext();
		CoreSession session = eventContext.getCoreSession();
		
		try {
			ToutaticeSilentProcessRunnerHelper runner = new InheritanceSilentModeRunner(session, event, true);
			runner.silentRun(true);
		} catch (ClientException e) {
			log.error("Failed to run the inheritance process, error: " + e.getMessage());
		}
	}
	
	private ActionManager getActionService() throws Exception {
		if (null == this.actionManager) {
			this.actionManager = Framework.getService(ActionManager.class);
		}
		return this.actionManager;
	}
	
	private class InheritanceParentFilter implements Filter {
		private static final long serialVersionUID = 1L;
		
		private ActionManager actionManager;
		private String[] filters;
		private ActionContext actionContext;

		public InheritanceParentFilter(ActionManager manager, ActionContext context, String[] filters) {
			this.actionManager = manager;
			this.filters = filters;
			this.actionContext = context;
		}
		
		@Override
		public boolean accept(DocumentModel docModel) {
			boolean status = true;
			
			this.actionContext.setCurrentDocument(docModel);
			for (String filterId : this.filters) {
				status = this.actionManager.checkFilter(filterId, this.actionContext);
				if (false == status) {
					break;
				}
			}
			
			return status;
		}
		
	}
	
	private class InheritanceSilentModeRunner extends ToutaticeSilentProcessRunnerHelper {
		
		private Event event;
		private boolean asynchronously;

		public InheritanceSilentModeRunner(CoreSession session, Event event, boolean asynchronously) {
			super(session);
			this.event = event;
			this.asynchronously = asynchronously;
		}

		@Override
		public void run() throws ClientException {
			DocumentEventContext eventContext = (DocumentEventContext) this.event.getContext();
			String eventName = this.event.getName();
			DocumentModel document = eventContext.getSourceDocument();
			
			run(eventContext, document, eventName);
		}
		
		@SuppressWarnings("unchecked")
		private void run(DocumentEventContext eventContext, DocumentModel document, String eventName) {
			boolean thisIncluded;
			boolean immediateOnly;
			boolean recursive;
			DocumentModelList parents = null;
			
			if (document.isImmutable()) {
				return;
			}
			
			try {
				ActionContext actionContext = new ActionContext();
				actionContext.setCurrentDocument(document);
				actionContext.setDocumentManager(this.session);
				actionContext.setCurrentPrincipal((NuxeoPrincipal) this.session.getPrincipal()); 

				String ACTION_ID_PREFIX = String.format("OPENTOUTATICE_INHERITANCE_%s@", (this.asynchronously) ? "ASYNC" : "SYNC");
				List<Action> actions = getActionService().getActions(ACTION_ID_PREFIX + eventName, actionContext);
				for (Action action : actions) {
					/**
					 * Get action source properties
					 */
					Map<String, Serializable> properties = action.getProperties();

					Map<String, Serializable> srcProperties = (Map<String, Serializable>) properties.get("source");
					if (null != srcProperties) {
						String thisIncludedPrty = (String) srcProperties.get("thisIncluded");
						thisIncluded = StringUtils.isNotBlank(thisIncludedPrty) ? Boolean.parseBoolean(thisIncludedPrty) : false;
						
						String immediateOnlyPrty = (String) srcProperties.get("immediateOnly");
						immediateOnly = StringUtils.isNotBlank(immediateOnlyPrty) ? Boolean.parseBoolean(immediateOnlyPrty) : true;
						
						String[] srcFilters = (String[]) srcProperties.get("filters");
						
						/** 
						 * Find parent
						 * (handle newly created document - then path is not initialized into the document model but is set inside a context property)
						 */
						DocumentModel root = document;
						if (DocumentEventTypes.EMPTY_DOCUMENTMODEL_CREATED.equals(eventName)) {
							String parentPath = (String) eventContext.getProperty("parentPath");
							root = this.session.getDocument(new PathRef(parentPath));
							thisIncluded = true;
						}
						
						parents = ToutaticeDocumentHelper.getParentList(this.session, 
								root,
								new InheritanceParentFilter(getActionService(), actionContext, srcFilters), 
								true, immediateOnly, thisIncluded);
					}
					
					/**
					 * Get action destination properties
					 */
					Map<String, Serializable> dstProperties = (Map<String, Serializable>) properties.get("destination");
					String setterName = (String) dstProperties.get("setter");
					String recursivePrty = (String) dstProperties.get("recursive");
					recursive = StringUtils.isNotBlank(recursivePrty) ? Boolean.parseBoolean(recursivePrty) : false;
					
					/**
					 *  Apply setter
					 */
					if (settersDescriptors.containsKey(setterName)) {
						ToutaticeInheritanceSetterDescriptor sDesc = settersDescriptors.get(setterName);
						ToutaticeInheritanceSetter setter = sDesc.getSetter();
						boolean isUpdated  = setter.execute(session, (null != parents && 0 < parents.size()) ? parents.get(0) : null, document);
						
						if (isUpdated && !DocumentEventTypes.EMPTY_DOCUMENTMODEL_CREATED.equals(eventName)) {
							this.session.saveDocument(document);
						}
						
						// Apply action recursively if it is specifies so 
						if (document.isFolder() && recursive) {
							DocumentModelList children = session.getChildren(document.getRef(), null, new Filter() {

								private static final long serialVersionUID = 1L;

								@Override
								public boolean accept(DocumentModel docModel) {
									boolean status = false;
									
									try {
										status = !docModel.isImmutable() && !LifeCycleConstants.DELETED_STATE.equals(docModel.getCurrentLifeCycleState());
									} catch (ClientException e) {
										log.error("Failed to evaluate children, error: " + e.getMessage());
									}
									
									return status;
								}
								
							}, null);
							
							for (DocumentModel child : children) {
								run(eventContext, child, eventName);
							}
						}
					} else {
						log.warn("Referenced unknown setter '" + setterName + "'");
					}
				}
			} catch (Exception e) {
				log.error("Failed while processing inheritance, error: " + e.getMessage());
			}
		}
		
	}
	
}