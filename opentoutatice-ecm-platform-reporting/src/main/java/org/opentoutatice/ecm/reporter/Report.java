/**
 * 
 */
package org.opentoutatice.ecm.reporter;


/**
 * @author david
 *
 */
public interface Report {
    
    <C,S> C write(S scannedObject) throws Exception ;
    
    <C> void send(C content) throws Exception ;

}
