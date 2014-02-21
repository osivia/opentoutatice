/**
 * 
 */
package org.nuxeo.ecm.platform.types;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("position")
public class PositionLayoutDescriptor implements Serializable {

    private static final long serialVersionUID = 2178639105074542595L;
    
    public static final int POS_UNDEFINED = -999;
    public static final String LAST_POSITION = "end";
    
    @XNode("before")
    String beforeLayout;
    
    public String getBeforeLayout(){
        return beforeLayout;
    }
    
    @XNode("after")
    String afterLayout;
    
    public String getAfterLayout(){
        return afterLayout;
    }
    
    @XNode("at")
    String positionLayout = String.valueOf(POS_UNDEFINED);
    
    public String getPositionLayout(){
        return positionLayout;
    }
    
}