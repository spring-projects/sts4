package org.springsource.ide.eclipse.commons.frameworks.core.internal.cache;

/**
 * A Cache is basically just some kind of map in which key/value pairs can be kept.
 * <p>
 * Different implementations of this interface may implement different methods of automatically
 * expiring cache entries.
 */
public interface Cache<K,V> {

	V get(K key);
	void put(K key, V value);
	void clear(); //clear all entries from the cache.

}
