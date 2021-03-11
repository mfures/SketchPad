package hr.fer.zemris.diprad.util;

import java.util.List;

import hr.fer.zemris.diprad.recognition.objects.Line;

/**
 * Describes a simple pair of any 2 generic values
 * 
 * @author Matej
 *
 * @param <T> any value
 * @param <K> any value
 */
public class ListPair {
	/**
	 * First value in constructor
	 */
	public List<Line> l1;

	/**
	 * Second value in constructor
	 */
	public List<Line> l2;

	/**
	 * Empty constructor
	 */
	public ListPair() {
	}

	/**
	 * Generic constructor;
	 * 
	 * @param t
	 * @param k
	 */
	public ListPair(List<Line> l1, List<Line> l2) {
		this.l1 = l1;
		this.l2 = l2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l1 == null) ? 0 : l1.hashCode());
		result = prime * result + ((l2 == null) ? 0 : l2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListPair other = (ListPair) obj;
		if (l1 == null) {
			if (other.l1 != null)
				return false;
		} else if (!l1.equals(other.l1))
			return false;
		if (l2 == null) {
			if (other.l2 != null)
				return false;
		} else if (!l2.equals(other.l2))
			return false;
		return true;
	}
}
