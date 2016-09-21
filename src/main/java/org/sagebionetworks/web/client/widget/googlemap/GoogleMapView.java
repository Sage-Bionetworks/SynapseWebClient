package org.sagebionetworks.web.client.widget.googlemap;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface GoogleMapView extends IsWidget{
	void setSynAlert(Widget w);
	void setLoading(boolean visible);
	void showMap(String data);
	void showUsers(String location, List<Widget> badges);
	void setPresenter(Presenter presenter);
	public interface Presenter {
		void markerClicked(String location, List<String> userIds);
	}
}
