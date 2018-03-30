package fr.toutatice.ecm.platform.service.inheritance;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

@XObject(value = "setter")
public class ToutaticeInheritanceSetterDescriptor  implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    
    private ToutaticeInheritanceSetter setter;

    @XNode("@name")
    protected String name;
    
	@XNode("@class")
    protected Class<?> clazz;

	public ToutaticeInheritanceSetterDescriptor() {
		this.setter = null;
	}
	
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public Class<?> getClazz() {
    	return clazz;
    }
    
    public void setClazz(Class<?> clazz) {
    	this.clazz = clazz;
    }
    
	public ToutaticeInheritanceSetter getSetter() {
		return this.setter;
	}
	
	public void setSetter(ToutaticeInheritanceSetter setter) {
		this.setter = setter;
	}
	
	@Override
    public String toString() {
    	return clazz.getName();
    }

    @Override
    public boolean equals(Object other) {
    	if (other == null) {
    		return false;
    	}

    	if (!(other instanceof ToutaticeInheritanceSetterDescriptor)) {
    		return false;
    	}

    	ToutaticeInheritanceSetterDescriptor otherAction = (ToutaticeInheritanceSetterDescriptor) other;
        return this.clazz == null ? otherAction.clazz == null : this.clazz.equals(otherAction.clazz);
    }
    
    @Override
    public int hashCode() {
        return this.clazz == null ? 0 : this.clazz.getName().hashCode();
    }

}