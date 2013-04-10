package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.Activity;

/**
 * A presenter extends an Active and has a setPlace
 * @author John
 *
 * @param <T>
 */
public interface Presenter<T> extends Activity {

	public void setPlace(T place);
}
