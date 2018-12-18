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
package org.opentoutatice.core.type.enricher;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;



/**
 * @author David Chevrier
 */
@XObject("rules")
public class ExclusionRulesDescriptor implements Serializable {

    private static final long serialVersionUID = 1924936797682387528L;
    
    // /** Types rule key. */
    // public static final String TYPES_RULE = "types";
    // /** Facets rules key. */
    // public static final String FACETS_RULE = "facets";

    /** Facets rule. */
    @XNodeList(value = "facet@name", type = String[].class, componentType = String.class)
    String[] facets = new String[0];
    
    /**
     * @return facets of Facets rule.
     */
    public String[] getFacets(){
        return facets;
    }  
    
    /** Types rule. */
    @XNodeList(value = "types/type", type = String[].class, componentType = String.class)
    String[] types = new String[0];
    
    /**
     * @return types of Types rule.
     */
    public String[] getTypes(){
        return types;
    } 
    
    // /**
    // * Getter for exclusion rules structure.
    // *
    // * @param rules
    // * @return exclusion rules
    // */
    // public Map<String, List<String>> getExcludedRules(ExclusionRulesDescriptor rules) {
    // Map<String, List<String>> rulesStructure = new HashMap<String, List<String>>();
    //
    // // Types rule
    // String[] types = rules.getTypes();
    // if (ArrayUtils.isNotEmpty(types)) {
    // rulesStructure.put(TYPES_RULE, Arrays.asList(types));
    // }
    //
    // // Facets rule
    // String[] facets = rules.getFacets();
    // if (ArrayUtils.isNotEmpty(facets)) {
    // rulesStructure.put(FACETS_RULE, Arrays.asList(facets));
    // }
    //
    // return rulesStructure;
    // }

}