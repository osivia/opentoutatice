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
package fr.toutatice.ecm.platform.core.helper;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;


public abstract class ToutaticeSorterHelper<T> implements Comparator<T> {

	Collator collator;
	
	/**
	 * French sort helper.
	 */
	public ToutaticeSorterHelper() {
		this.collator = Collator.getInstance(Locale.FRENCH);
		this.collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		this.collator.setStrength(Collator.TERTIARY);   
	}
	
	/**
	 * Sorter with parameters.
	 * 
	 * @param locale
	 * @param strength
	 * @param decomposition
	 */
	public ToutaticeSorterHelper(Locale locale, int strength, int decomposition) {
        this.collator = Collator.getInstance(locale);
        this.collator.setDecomposition(decomposition);
        this.collator.setStrength(strength);   
    }
	
	@Override
	public int compare(T o1, T o2) {
		String string1 = getComparisionString(o1);
		String string2 = getComparisionString(o2);
		
		return this.collator.compare(string1, string2);
	}
	
	/**
	 * La méthode à implémenter par les classes qui étendent celle-ci.
	 * Doit retourner la chaine de caractère qui servira d'objet de comparaison.
	 * 
	 * @param t l'objet comparé (type généric)
	 * @return la chaîne de caractère à comparer
	 */
	public abstract String getComparisionString(T t);

}
