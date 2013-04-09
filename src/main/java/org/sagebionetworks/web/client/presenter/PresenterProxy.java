package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.Activity;

/**
 * We use Presenter proxies to provide code splitting points see (https://developers.google.com/web-toolkit/doc/latest/DevGuideCodeSplitting).
 * We currently have 4.8 MB of javascript code and without code splitting the browser must download all of the code at once before anything is shown.
 * Depending on the browser and network speed, this download can take a perceptible amount of time. See: SWC-81.
 * 
 * GWT requires code splitting to be done with an asynchronous callbacks with no code dependencies across splits.  Since presenters correspond to pages 
 * and have very little external dependencies they provide natural split points. 
 * 
 * 
 * @author jmhill
 *
 * @param <T>
 */
public interface PresenterProxy<T> extends Activity {
	
	public void setPlace(T place);
	
}
