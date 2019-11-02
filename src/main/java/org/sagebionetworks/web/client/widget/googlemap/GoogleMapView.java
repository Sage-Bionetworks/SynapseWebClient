package org.sagebionetworks.web.client.widget.googlemap;

import java.util.List;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface GoogleMapView extends IsWidget, SupportsLazyLoadInterface {
	void setSynAlert(Widget w);

	void showMap(String data);

	void showUsers(String location, List<Widget> badges);

	void setPresenter(Presenter presenter);

	void setVisible(boolean visible);

	void setHeight(String height);

	public interface Presenter {
		void markerClicked(String location, List<String> userIds);
	}
}
