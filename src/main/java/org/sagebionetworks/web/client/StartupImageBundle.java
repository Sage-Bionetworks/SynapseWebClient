package org.sagebionetworks.web.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * A light weight bundle that can be used before any code is loaded.
 * 
 * @author John
 *
 */
public interface StartupImageBundle extends ClientBundle {

	@Source("images/loading-31.gif")
	ImageResource loading31();	
}
