package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.place.shared.Place;

/**
 * A presenter extends an Active and has a setPlace
 * 
 * @author John
 *
 * @param <T> - the presenter's place
 */
public interface Presenter<T extends Place> extends Activity {

	public void setPlace(T place);
}
