package hr.fer.zemris.diprad.util;

/**
 * Describes a simple pair of any 2 generic values
 * 
 * @author Matej
 *
 * @param <T> any value
 * @param <K> any value
 */
public class Pair<T, K> {
	public T t;
	public K k;

	/**
	 * Empty constructor
	 */
	public Pair() {
	}

	/**
	 * Generic constructor;
	 * 
	 * @param t
	 * @param k
	 */
	public Pair(T t, K k) {
		this.t = t;
		this.k = k;
	}
}
