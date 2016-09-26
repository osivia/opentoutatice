/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *   dchevrier
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.core.constants;


/**
 * @author David Chevrier
 */
public final class PortalConstants {
    
    /** Notifications send to Portal. */
    public enum Notifications {
    	SUCCESS_MESSAGE_MODIFY,
    	SUCCESS_MESSAGE_CREATE,
    	SUCCESS_MESSAGE_CREATE_IN_WS,
    	SUCCESS_MESSAGE_PUBLISH,
    	SUCCESS_MESSAGE_ASK_PUBLISH
    }
    
    /** Final Portal View. */
    public static final String FINAL_PORTAL_VIEW = "done";

}
