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
 *   lbillon
 *   dchevrier
 *    
 */
package fr.toutatice.ecm.platform.automation.helper;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.util.Calendar;


/**
 * @author david chevrier
 *
 */
public class TimeDebugger {
    
    private static Map<String, TimeDebugger> instances = new HashMap<String, TimeDebugger>(0);
    
    private long startTime;
    
    private TimeDebugger () {};
    
    public static TimeDebugger getInstance(String methodName, String sessionId){
        String id = sessionId + methodName;
        TimeDebugger debugger = instances.get(id);
        if(debugger == null){
            debugger = new TimeDebugger();
            instances.put(id, debugger);
        }
        return debugger;
    }
    
    public void setStartTime (){
        this.startTime = Calendar.getInstance().getTimeInMillis();
    }
    
    public long getTotalTime(){
        return Calendar.getInstance().getTimeInMillis() - this.startTime;
    }
    
    public String getMessage(String methodName, String sessionId, String documentPath, long totalTime){
        String msg = "*** Method %s TIME = %s ms (sessionId: %s, document: %s)";
        return String.format(msg, methodName, String.valueOf(totalTime), sessionId, documentPath);
    }

}
