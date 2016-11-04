/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.security.SecurityConstants;


/**
 * @author david
 *
 */
public class FunctionalSecurityCases {
    
    /*
     * Utility class
     */
    private FunctionalSecurityCases(){
        super();
    }
    
    /** Indicators */
    public enum PermissionFor {
        document, parent;
    }
    
    public enum FunctionalCases {
        CREATE, READ, UPDATE, DELETE,
        IMPORT, COPY, MOVE,
        ALL;
    }
    
    private static Map<PermissionFor, List<String>> buildDelete(){
        if(DELETE == null){
            DELETE = new HashMap<PermissionFor, List<String>>(0);
        }
        String[] docPerms = {SecurityConstants.WRITE_VERSION, SecurityConstants.REMOVE};
        DELETE.put(PermissionFor.document, Arrays.asList(docPerms));
        
        String[] parentPerms = {SecurityConstants.REMOVE_CHILDREN};
        DELETE.put(PermissionFor.parent, Arrays.asList(parentPerms));
        
        return DELETE;
    }
    
    private static Map<PermissionFor, List<String>> buildMove(){
        if(MOVE == null){
            MOVE = new HashMap<PermissionFor, List<String>>(0);
        }
        String[] docPerms = {SecurityConstants.WRITE_VERSION, SecurityConstants.REMOVE};
        MOVE.put(PermissionFor.document, Arrays.asList(docPerms));
        
        String[] parentPerms = {SecurityConstants.REMOVE_CHILDREN};
        MOVE.put(PermissionFor.parent, Arrays.asList(parentPerms));
        
        return MOVE;
    }
    
    public static final String document = "document";
    public static final String parent = "parent";
    
    /* Creation */
    public static final String CREATE = SecurityConstants.ADD_CHILDREN;
    /* Read */
    public static final String READ = SecurityConstants.READ; 
    /* Update */
    public static final String UPDATE = SecurityConstants.WRITE_PROPERTIES;
    /* Delete: 
     * - write version is for deleting a version
     * - to delete a document, we must can remove it and remove children in parent
     */
    public static Map<PermissionFor, List<String>> DELETE = buildDelete();
    
    /* Import :
     * - Read on parent
     * - Add children on parent
     */
    public static final String IMPORT = SecurityConstants.ADD_CHILDREN;
    /* Copy
     * - Add children on parent 
     */
    public static final String COPY = SecurityConstants.ADD_CHILDREN;
    /* Move:
     * - Read on source
     * - if destination is null: write properties on source's parent
     * - if destination is not null: 
     *   - add children on destination
     *   - remove children on source's parent
     *   - remove on source
     */
    public static Map<PermissionFor, List<String>> MOVE = buildMove();
    
    /** Owner cases. */
    //public static final String[] OWNER_CASES = {CREATE, READ, UPDATE, DELETE, IMPORT, COPY, MOVE};
    
    public final static Map<PermissionFor, List<String>> getPermissionsFor(String functionalCase){
        
        return null;
    }
}
