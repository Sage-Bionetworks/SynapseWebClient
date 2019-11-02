package org.sagebionetworks.web.client.utils;

/**
 * A simple interface for passing a method which is to be called by the recipient, in which the
 * method takes a parameter as input
 * 
 * @author brucehoff
 *
 */
public interface CallbackP<T> {
	void invoke(T param);
}


