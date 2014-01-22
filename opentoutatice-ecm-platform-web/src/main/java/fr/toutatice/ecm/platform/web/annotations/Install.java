package fr.toutatice.ecm.platform.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Install {
	   /**
	    * Precedence to use for components which override
	    * Open Toutatice components
	    */
	   public static final int TOUTATICE = org.jboss.seam.annotations.Install.DEPLOYMENT;

	   /**
	    * The precedence of the component. If multiple components with
	    * the same name exist, the one with the higher precedence will
	    * be installed.
	    */
	   public int precedence() default TOUTATICE;
}
