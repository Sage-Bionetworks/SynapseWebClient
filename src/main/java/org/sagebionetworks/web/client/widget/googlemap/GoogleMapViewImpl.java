package org.sagebionetworks.web.client.widget.googlemap;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GoogleMapViewImpl implements GoogleMapView {
	public interface GoogleMapViewImplUiBinder extends UiBinder<Widget, GoogleMapViewImpl> {
	}

	@UiField
	HTMLPanel loadingUI;
	@UiField
	Div synAlertContainer;
	@UiField
	Div googleMapContainer;
	@UiField
	Div userBadges;

	Widget widget;
	PortalGinInjector ginInjector;

	@Inject
	public GoogleMapViewImpl(GoogleMapViewImplUiBinder binder, PortalGinInjector ginInjector) {
		widget = binder.createAndBindUi(this);
		this.ginInjector = ginInjector;
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
		Element el = googleMapContainer.getElement();
		JavaScriptObject map = _createMap(el);
		userBadges.clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject markerJson = (JSONObject) jsonArray.get(i);
			JSONArray latLngArray = (JSONArray) markerJson.get("latLng");
			JSONObject location = new JSONObject();
			location.put("lat", latLngArray.get(0));
			location.put("lng", latLngArray.get(1));

			Div userBadgesContainer = new Div();
			JSONArray userIdsArray = (JSONArray) markerJson.get("userIds");
			for (int j = 0; j < userIdsArray.size(); j++) {
				UserBadge badge = ginInjector.getUserBadgeWidget();
				JSONNumber userId = userIdsArray.get(j).isNumber();
				double userIdDouble = userId.doubleValue();
				badge.configure(Double.toString(Math.floor(userIdDouble)));
				userBadgesContainer.add(badge.asWidget());
			}
			userBadges.add(userBadgesContainer);
			
			JSONString title = markerJson.get("location").isString();
			
			_addMarker(map, title.stringValue(), location, userBadgesContainer.getElement());
		}
	}

	private static native JavaScriptObject _createMap(Element el) /*-{
		return new $wnd.google.maps.Map(el, {
			center : {
				lat : -34.397,
				lng : 150.644
			},
			zoom : 8
		});
	}-*/;

	private static native void _addMarker(JavaScriptObject mapJsObject, String titleString, JSONValue position, Element infoWindowContentEl) /*-{
		var infowindow = new $wnd.google.maps.InfoWindow({
			content : infoWindowContentEl
		});

		var marker = new $wnd.google.maps.Marker({
			position : position,
			map : mapJsObject,
			title : titleString
		});
		marker.addListener('click', function() {
			infowindow.open(mapJsObject, marker);
		});
	}-*/;

}
