package org.sagebionetworks.web.client.widget.googlemap;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.PortalGinInjector;

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
		googleMapContainer.setHeight("600px");
		Element el = googleMapContainer.getElement();
		JavaScriptObject map = _createMap(el);
		userBadges.clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject markerJson = (JSONObject) jsonArray.get(i);
			JSONArray latLngArray = (JSONArray) markerJson.get("latLng");
			double lat = latLngArray.get(0).isNumber().doubleValue();
			double lng = latLngArray.get(1).isNumber().doubleValue();
			
			Div userBadgesContainer = new Div();
			JSONArray userIdsArray = (JSONArray) markerJson.get("userIds");
			userBadgesContainer.add(new Text(userIdsArray.toString()));
//			for (int j = 0; j < userIdsArray.size(); j++) {
//				UserBadge badge = ginInjector.getUserBadgeWidget();
//				JSONValue userId = userIdsArray.get(j);
//				String userIdString = userId.isString().stringValue();
//				badge.configure(userIdString);
//				userBadgesContainer.add(badge.asWidget());
//			}
			userBadges.add(userBadgesContainer);
			
			JSONString title = markerJson.get("location").isString();
			
			_addMarker(map, title.stringValue(), lat, lng, userBadgesContainer.getElement());
		}
	}

	private static native JavaScriptObject _createMap(Element el) /*-{
		return new google.maps.Map(el, {
			center : new google.maps.LatLng(42, -34),
			zoom : 3
		});
	}-*/;

	private static native void _addMarker(JavaScriptObject mapJsObject, String titleString, double lat, double lng, Element infoWindowContentEl) /*-{
		var infowindow = new google.maps.InfoWindow({
			content : infoWindowContentEl
		});

		var marker = new google.maps.Marker({
			position : new google.maps.LatLng(lat, lng),
			map : mapJsObject,
			title : titleString
		});
		google.maps.event.addListener(marker, 'click', function() {
			console.log('clicked');
			infowindow.open(mapJsObject, marker);
		});
	}-*/;

}
