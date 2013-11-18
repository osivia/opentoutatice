package fr.toutatice.ecm.platform.core.helper;

import java.text.Collator;
import java.util.Comparator;


public abstract class ToutaticeSorterHelper<T> implements Comparator<T> {

	private static final long serialVersionUID = -8025278561121445915L;
	
	Collator collator;
	
	public ToutaticeSorterHelper() {
		this.collator = Collator.getInstance();
		this.collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		this.collator.setStrength(Collator.TERTIARY);   
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
