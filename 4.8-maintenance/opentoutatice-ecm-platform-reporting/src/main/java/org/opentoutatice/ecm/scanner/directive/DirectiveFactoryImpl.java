/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive;

import org.opentoutatice.ecm.scanner.directive.nxql.NxqlQueryAndFetchDirective;
import org.opentoutatice.ecm.scanner.directive.nxql.NxqlQueryDirective;


/**
 * @author david
 *
 */
public class DirectiveFactoryImpl implements DirectiveFactory {

    /**
     * Default constructor.
     */
    public DirectiveFactoryImpl() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Directive create(DirectiveType type) throws DirectiveException {
        Directive directive = null;
        
        switch (type) {
            case nxql:
                directive = new NxqlQueryDirective();
                break;
                
            case nxqlQueryAndFetch:
                directive = new NxqlQueryAndFetchDirective();
                break;
                
            default:
                break;
        }
        
        return directive;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Directive create(DirectiveType type, String query) throws DirectiveException {
        Directive directive = null;
        
        switch (type) {
            case nxql:
                directive = new NxqlQueryDirective(query);
                break;
                
            case nxqlQueryAndFetch:
                directive = new NxqlQueryAndFetchDirective(query);
                break;
                
            default:
                break;
        }
        
        return directive;
    }

}
