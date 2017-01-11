/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive;


/**
 * @author david
 *
 */
public interface DirectiveFactory {

    /**
     * Creates a directive.
     * 
     * @param type
     * @return Directive
     * @throws DirectiveException
     */
    Directive create(DirectiveType type) throws DirectiveException;

    /**
     * Creates a directive.
     * 
     * @param type
     * @param query
     * @return Directive
     * @throws DirectiveException
     */
    Directive create(DirectiveType type, String query) throws DirectiveException;

}
