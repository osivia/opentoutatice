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
 *    
 */
package fr.toutatice.ecm.platform.service.fragments;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.service.fragments.types.Fragment;

/**
 * Generic fragment class
 * 
 */
public class FragmentServiceImpl extends DefaultComponent implements FragmentService {


    private static final Log log = LogFactory.getLog(FragmentServiceImpl.class);

    /** Map of all fragment types */
    private static final Map<FragmentDescriptor, Fragment> fragments = new HashMap<FragmentDescriptor, Fragment>();

    /** Main nuxeo schema shared by all fragments */
    public static String SCHEMA = "fragments";


    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {

        FragmentDescriptor contribDescriptor = (FragmentDescriptor) contribution;

        contribDescriptor.initFragment();

        addFragmentType(contribDescriptor);
    }


    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {

        FragmentDescriptor contribDescriptor = (FragmentDescriptor) contribution;

        removeFragmentType(contribDescriptor);
    }



    public void addFragmentType(FragmentDescriptor contribution) {
        if (fragments.get(contribution) == null) {
            fragments.put(contribution, contribution.getInstance());

        } else {
            log.warn("Contribution " + contribution.code + " has already been registered.");
        }
    }

    public void removeFragmentType(FragmentDescriptor contribution) {
        fragments.remove(contribution);

    }

    @Override
	public Map.Entry<FragmentDescriptor, Fragment> findByCode(String code) throws FragmentServiceException {
        for (Map.Entry<FragmentDescriptor, Fragment> entry : fragments.entrySet()) {
            if (entry.getKey().getCode().equals(code)) {
                return entry;
            }
        }
        throw new FragmentServiceException("osivia.error.fragment_not_found");

    }

    /**
     * @param doc nuxeo document
     * @param uri id of the fragment
     * @return the fragmentCategory
     */
    @Override
	public Entry<FragmentDescriptor, Fragment> getFragmentCategory(DocumentModel doc, String uri) throws FragmentServiceException {

        String fgtCategory = null;
        Map<String, Object> properties;

        try {
            properties = doc.getProperties(SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, Object>> listeFragments = (List<Map<String, Object>>) liste;

                for (Map<String, Object> fragment : listeFragments) {
                    if (uri.equals(fragment.get("uri"))) {

                        fgtCategory = fragment.get("fragmentCategory").toString();
                        break;
                    }
                }
            }
        } catch (ClientException e) {
            throw new FragmentServiceException(e);
        }

        if (fgtCategory == null)
            throw new FragmentServiceException("osivia.error.fragment_not_found");

        Entry<FragmentDescriptor, Fragment> findByCode = findByCode(fgtCategory);


        return findByCode;

    }


    /**
     * Initialize a default entry in the main fragments schema
     * 
     * @param doc the current simplepage
     * @param region the cms region
     * @param belowUri the position where the fragment goes
     * @param code2
     * @return the new id (timestamp)
     */
    @Override
	public String prepareCreation(DocumentModel doc, Fragment specific, String fragmentCategory, String region, String belowUri, String code2)
            throws FragmentServiceException {

        String uri = null;

        Map<String, Object> properties;

        try {
            properties = doc.getProperties(SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, Object>> listeFragments = (List<Map<String, Object>>) liste;

                // ================== Calcul des propriétés du schéma
                // Génération d'une URI
                uri = Long.toString(new Date().getTime());

                // Calcul du positionnement
                // Par défaut en haut de la région (0)
                String regionId = region;
                Integer order = new Integer(0);
                // Si l'uri du fragment au dessus est précisé, récupération de
                // la position et de la région du fragment
                if (belowUri != null) {
                    for (Map<String, Object> fragment : listeFragments) {
                        if (belowUri.equals(fragment.get("uri"))) {
                            regionId = fragment.get("regionId").toString();
                            String orderStr = fragment.get("order").toString();
                            order = Integer.parseInt(orderStr) + 1;
                            break;
                        }
                    }
                }


                // ================== Décalage des autres fragments de cette région
                for (Map<String, Object> fragment : listeFragments) {
                    String regionCompare = fragment.get("regionId").toString();
                    String orderCompare = fragment.get("order").toString();
                    if (regionId.equals(regionCompare) && Integer.parseInt(orderCompare) >= order) {
                        Integer newOrder = Integer.parseInt(orderCompare) + 1;
                        fragment.put("order", newOrder.toString());
                    }
                }

                // ================== Ajout d'une nouvelle entrée au schéma
                Map<String, Object> newEntry = new HashMap<String, Object>();

                newEntry.put("fragmentCategory", fragmentCategory);
                newEntry.put("order", order.toString());
                newEntry.put("regionId", regionId);
                newEntry.put("uri", uri);

                newEntry.put("title", "Nouveau fragment");
                newEntry.put("hideTitle", Boolean.FALSE.toString());
				newEntry.put("collapsed", Boolean.FALSE.toString());
                newEntry.put("style", "");

                listeFragments.add(newEntry);

                doc.setProperties(SCHEMA, properties);

            }
        } catch (ClientException e) {
            throw new FragmentServiceException(e);
        }

        specific.prepareCreation(doc, uri, region, belowUri, code2);

        return uri;
    }



}
