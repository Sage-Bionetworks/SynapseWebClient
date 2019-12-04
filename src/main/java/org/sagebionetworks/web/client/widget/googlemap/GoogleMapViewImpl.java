package org.sagebionetworks.web.client.widget.googlemap;

import java.util.ArrayList;
import java.util.List;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GoogleMapViewImpl implements GoogleMapView {
	public interface GoogleMapViewImplUiBinder extends UiBinder<Widget, GoogleMapViewImpl> {
	}

	Presenter presenter;
	@UiField
	Div synAlertContainer;
	@UiField
	Div googleMapCanvas;
	@UiField
	Div userBadges;
	@UiField
	Div markerPopupContent;
	@UiField
	Div googleMapContainer;
	@UiField
	Heading locationTitle;
	Widget widget;
	Callback onAttachCallback;
	JavaScriptObject currentInfoWindow;

	@Inject
	public GoogleMapViewImpl(GoogleMapViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		widget.addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttach();
				}
			}
		});
	}

	@Override
	public void setHeight(String height) {
		googleMapContainer.setHeight(height);
	}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(widget);
	}

	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

	private void onAttach() {
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
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
	public void showMap(String data) {
		JSONArray jsonArray = (JSONArray) JSONParser.parseStrict(data);
		Element markerPopupContentEl = markerPopupContent.getElement();
		Element el = googleMapCanvas.getElement();
		JavaScriptObject map = _createMap(el);
		JavaScriptObject bounds = _getBounds();

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
			_addMarker(this, map, title.stringValue(), lat, lng, userIdsList, markerPopupContentEl, bounds);
		}
		setVisible(jsonArray.size() > 0);
		DisplayUtils.scrollToTop();
	}

	private static native JavaScriptObject _getBounds() /*-{
		return new google.maps.LatLngBounds();
	}-*/;

	private static native JavaScriptObject _createMap(Element el) /*-{
		return new google.maps.Map(el, {
			scrollwheel : true,
			mapTypeControl : false,
			streetViewControl : false,
			draggable : true,
			maxZoom : 10,
			controlSize : 22
		});
	}-*/;

	public void markerClicked(String location, List<String> userIdsList) {
		presenter.markerClicked(location, userIdsList);
	}

	private static native void _addMarker(GoogleMapViewImpl x, JavaScriptObject mapJsObject, String locationString, double lat, double lng, List<String> userIdsList, Element markerPopupContent, JavaScriptObject bounds) /*-{

		var image = {
			url : 'images/synapse-map-marker.png',
			size : new google.maps.Size(20, 32),
			origin : new google.maps.Point(0, 0),
			anchor : new google.maps.Point(0, 32)
		};
		var marker = new google.maps.Marker({
			position : new google.maps.LatLng(lat, lng),
			map : mapJsObject,
			icon : image
		});
		var infowindow = new google.maps.InfoWindow({
			content : markerPopupContent
		});
		bounds.extend(marker.getPosition());
		marker
				.addListener(
						'click',
						function() {
							x.@org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl::markerClicked(Ljava/lang/String;Ljava/util/List;)(locationString, userIdsList);
							var currentInfoWindow = x.@org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl::currentInfoWindow;
							if (currentInfoWindow) {
								currentInfoWindow.close();
							}
							x.@org.sagebionetworks.web.client.widget.googlemap.GoogleMapViewImpl::currentInfoWindow = infowindow;
							infowindow.open(mapJsObject, marker);
						});
		marker.setClickable(true);
		mapJsObject.fitBounds(bounds);
		mapJsObject.setCenter(bounds.getCenter());
	}-*/;

	@Override
	public void showUsers(String location, List<Widget> badges) {
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
