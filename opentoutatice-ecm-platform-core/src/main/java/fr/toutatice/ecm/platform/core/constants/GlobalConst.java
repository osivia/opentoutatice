package fr.toutatice.ecm.platform.core.constants;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;

import fr.toutatice.ecm.platform.core.publish.ToutaticeNullPublishedDocument;

/**
 * This class groups together all definitions required within the bundle (except those from the studio)
 * 
 * @author mberhaut1
 */
public class GlobalConst {

	public static final String CST_CONTENT_VIEW_ADVANCED_SEARCH = "cv_acaren_advanced_search";
	public static final String CST_CONTENT_VIEW_FACETED_SEARCH = "cv_acaren_faceted_search";
	public static final String CST_CONTENT_VIEW_RELATION_SEARCH = "relation_search";
	public static final String CST_ADVANCED_SEARCH_XPATH_PATH = "advanced_search:searchpath";
	public static final String CST_FACETED_SEARCH_XPATH_PATH = "faceted_search_default:ecm_path";
	public static final String CST_RELATION_SEARCH_XPATH_PATH = "relation_search:ecm_path";

	// Workflow tasks - validation
	public final static String CST_WORKFLOW_PROCESS_LEGACY_VALIDATION_APPROBATION = "review_approbation";
	public final static String CST_WORKFLOW_PROCESS_VALIDATION_APPROBATION = "acaren_validation_approbation";
	public final static String CST_WORKFLOW_PROCESS_VALIDATION_PARALLEL = "review_parallel";
	public final static String CST_WORKFLOW_TASK_LEGACY_VALIDATE = "validate";
	public final static String CST_WORKFLOW_TASK_LEGACY_REJECT = "reject";
	public final static String CST_WORKFLOW_TASK_VALIDATE = "validate_validation";
	public final static String CST_WORKFLOW_TASK_REJECT = "reject_validation";
	public final static String CST_WORKFLOW_TASK_CHOOSE_PARTICIPANT = "choose-participant";
	
	// Workflow tasks - mise en ligne
	public final static String CST_WORKFLOW_PROCESS_ONLINE = "acaren_online_approbation";
	public final static String CST_WORKFLOW_TASK_ONLINE_VALIDATE = "validate-online";
	public final static String CST_WORKFLOW_TASK_ONLINE_REJECT = "reject-online";
	public final static String CST_WORKFLOW_TASK_ONLINE_CHOOSE_PARTICIPANT = "choose-participant";
	
	// Workflow tasks - ensembles  
	public final static String[] CST_WORKFLOW_TASK_VALIDATION_ALL = new String[] {
		CST_WORKFLOW_TASK_LEGACY_VALIDATE,
		CST_WORKFLOW_TASK_VALIDATE,
		CST_WORKFLOW_TASK_LEGACY_REJECT,
		CST_WORKFLOW_TASK_REJECT,
		CST_WORKFLOW_TASK_CHOOSE_PARTICIPANT
	};

	public final static String[] CST_WORKFLOW_TASK_ONLINE_ALL = new String[] {
		CST_WORKFLOW_TASK_ONLINE_VALIDATE,
		CST_WORKFLOW_TASK_ONLINE_REJECT,
		CST_WORKFLOW_TASK_ONLINE_CHOOSE_PARTICIPANT
	};
	
	public final static String[] CST_WORKFLOW_TASK_ALL = new String[] {
		CST_WORKFLOW_TASK_VALIDATE,
		CST_WORKFLOW_TASK_REJECT,
		CST_WORKFLOW_TASK_CHOOSE_PARTICIPANT,
		CST_WORKFLOW_TASK_ONLINE_VALIDATE,
		CST_WORKFLOW_TASK_ONLINE_REJECT,
		CST_WORKFLOW_TASK_ONLINE_CHOOSE_PARTICIPANT
	};
	
	// Events
	public final static String CST_EVENT_SUB_TAB_SELECTION_CHANGED = "subTabSelectionChanged";
	public final static String CST_EVENT_PROPAGATE_SECTIONS = "propagateSections";
	public final static String CST_EVENT_PROPAGATE_ORGANISATION_SOURCE = "propagateOrganisationSource";
	public final static String CST_EVENT_SECTION_MODIFICATION = "sectionModified";

	// Events options
	public final static String CST_EVENT_OPTION_VALUE_ALL = "ALL";
	public final static String CST_EVENT_OPTION_KEY_RUN_UNRESTRICTED = "runUnrestricted";
	public final static String CST_EVENT_OPTION_KEY_APPEND = "append";
	public final static String CST_EVENT_OPTION_KEY_OVERWRITE = "overwrite";
	public final static String CST_EVENT_OPTION_KEY_SECTION_ID = CST_EVENT_OPTION_VALUE_ALL;
	
	// Audit events
	public final static String CST_AUDIT_EVENT_PROPAGATE_PROCESS_COMPLETED = "propagateProcessCompleted";
	public final static String CST_AUDIT_EVENT_LOCK_REMOVED_BY_SYSTEM = "lockRemovedBySystem";
	public final static String CST_AUDIT_EVENT_UPGRADE_BDD_V1_V2 = "upgradeBDDV1vsV2";
	
	// Others
	public final static String CST_PROXY_NAME_SUFFIX = ".proxy";
	public final static String CST_REMOTE_PROXY_NAME_SUFFIX = ".remote.proxy";
	public final static String CST_REMOTE_PROXY_PENDING_NAME_SUFFIX = ".remote.proxy.pending";
	
	/**
	 * Cette définition est utilisée pour la gestion du cache (cf: AcarenNavigationContext.java) pour distinguer le cas
	 * où le cache retourne aucun résultat parce qu'aucune recherche n'a pas encore été faite du cas où la recherche 
	 * a été faite mais aucun résultat existe.
	 */
	public final static DocumentModel NULL_DOCUMENT_MODEL = new DocumentModelImpl("NULL_DOCUMENT_MODEL");
	public final static PublishedDocument NULL_PUBLISHED_DOCUMENT_MODEL = new ToutaticeNullPublishedDocument();
	
	//Groupe
	public final static String CST_GROUP_ADMINISTRATOR = "Administrators";
}
