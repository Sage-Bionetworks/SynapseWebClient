package org.sagebionetworks.web.client.widget.googlemap;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface GoogleMapView extends IsWidget{
	void setSynAlert(Widget w);
	void setLoading(boolean visible);
	void showMap(String data);
}
