/**
 * 
 */
package fr.toutatice.ecm.platform.core.query.helper;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author david
 *
 */
public class NxqlHelper {
	
	/**
	 * Helper class.
	 */
	private NxqlHelper(){}
	
	/**
	 * Utility Strings (to do not instantiate them everytime).
	 */
	public static final String IN_CLAUSE_KEYWORD = " IN ";
	public static final String QUOTE = "'";
	public static final String COMMA = ",";
	public static final String OPEN_PARENTHESIS = "(";
	public static final String CLOSE_PARENTHESIS = ")";
	public static final String EQUAL = " = ";
	
	/**
	 * Default clause.
	 */
	public static final String FIXED_QUERY_PART = " and ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'";
	
	
	/**
	 * @return IN ('a','b', ...) clause from input list.
	 */
	public static String buildInClause(List<String> values){
		if(CollectionUtils.isNotEmpty(values)){
			StringBuilder clause = new StringBuilder(IN_CLAUSE_KEYWORD.concat(OPEN_PARENTHESIS));
			Iterator<String> iterator = values.iterator();
			while (iterator.hasNext()){
				clause.append(QUOTE).append(iterator.next()).append(QUOTE);
				if(iterator.hasNext()){
					clause.append(COMMA);
				}
			}
			clause.append(CLOSE_PARENTHESIS);
			return clause.toString();
		}
		return StringUtils.EMPTY;
	}
	
	

}
