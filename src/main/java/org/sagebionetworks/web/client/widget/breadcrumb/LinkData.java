package org.sagebionetworks.web.client.widget.breadcrumb;

import org.gwtbootstrap3.client.ui.constants.IconType;
import com.google.gwt.place.shared.Place;

public class LinkData {

	private String text;
	private IconType icon;
	private Place place;

	public LinkData(String text, Place place) {
		this(text, null, place);
	}

	public LinkData(String text, IconType icon, Place place) {
		this.text = text;
		this.icon = icon;
		this.place = place;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public IconType getIconType() {
		return icon;
	}

	public void setIconType(IconType icon) {
		this.icon = icon;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

}
