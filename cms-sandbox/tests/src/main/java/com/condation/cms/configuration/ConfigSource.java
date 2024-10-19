package com.condation.cms.configuration;

import java.util.List;
import java.util.Map;

/**
 *
 * @author t.marx
 */
public interface ConfigSource {

	default boolean reload () {
		return false;
	}
	
	boolean exists ();
	
	String getString (final String field);	
	
	Object get (String field);
	
	Map<String, Object> getMap (String field);
	List<Object> getList (String field);
}
