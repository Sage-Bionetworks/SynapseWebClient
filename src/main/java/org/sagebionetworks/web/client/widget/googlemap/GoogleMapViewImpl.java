package org.sagebionetworks.web.client.widget.googlemap;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GoogleMapViewImpl implements GoogleMapView {
	public interface GoogleMapViewImplUiBinder extends UiBinder<Widget, GoogleMapViewImpl> {
	}
	Presenter presenter;
	@UiField
	HTMLPanel loadingUI;
	@UiField
	Div synAlertContainer;
	@UiField
	Div googleMapContainer;
	@UiField
	Div userBadges;
	@UiField
	Panel locationPanel;
	@UiField
	Heading locationTitle;
	Widget widget;
	
	@Inject
	public GoogleMapViewImpl(GoogleMapViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setLoading(boolean visible) {
		loadingUI.setVisible(visible);
	}

	@Override
	public void showMap(String data) {
		JSONArray jsonArray = (JSONArray) JSONParser.parseStrict(data);
		googleMapContainer.setHeight("600px");
		Element el = googleMapContainer.getElement();
		JavaScriptObject map = _createMap(el);
		userBadges.clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject markerJson = (JSONObject) jsonArray.get(i);
			JSONArray latLngArray = (JSONArray) markerJson.get("latLng");
			double lat = latLngArray.get(0).isNumber().doubleValue();
			double lng = latLngArray.get(1).isNumber().doubleValue();
			
			JSONArray userIdsArray = (JSONArray) markerJson.get("userIds");
			List<String> userIdsList = new ArrayList<String>();
			for (int j = 0; j < userIdsArray.size(); j++) {
				userIdsList.add(userIdsArray.get(j).isString().stringValue());
			}
			
			JSONString title = markerJson.get("location").isString();
			_addMarker(this, map, title.stringValue(), lat, lng, userIdsList);
		}
	}

	private static native JavaScriptObject _createMap(Element el) /*-{
		return new google.maps.Map(el, {
			center : new google.maps.LatLng(42, -34),
			zoom : 3
		});
	}-*/;
	
	public void markerClicked(String location, List<String> userIdsList) {
		presenter.markerClicked(location, userIdsList);
	}
	
	private static native void _addMarker(GoogleMapViewImpl x, JavaScriptObject mapJsObject, String locationString, double lat, double lng, List<String> userIdsList) /*-{
		var marker = new google.maps.Marker({
			position : new google.maps.LatLng(lat, lng),
			map : mapJsObject,
			title : locationString,
			icon: 'images/synapse-map-marker.png'
		});
		marker.addListener('click', function() {
			x.@org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl::markerClicked(Ljava/lang/String;Ljava/util/List;)(locationString, userIdsList);
		  });
		marker.setClickable(true);
	}-*/;

	@Override
	public void showUsers(String location, List<Widget> badges) {
		locationPanel.setVisible(true);
		locationTitle.setText(location);
		userBadges.clear();
		for (Widget widget : badges) {
			userBadges.add(widget);	
		}
	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
}
