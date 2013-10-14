package org.sagebionetworks.web.client.widget.breadcrumb;

import com.google.gwt.place.shared.Place;
import com.google.gwt.resources.client.ImageResource;

public class LinkData {

	private String text;
	private ImageResource icon;
	private Place place;

	public LinkData(String text, Place place) {
		this(text, null, place);
	}

	public LinkData(String text, ImageResource icon, Place place) {
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

	public ImageResource getIcon() {
		return icon;
	}

	public void setIcon(ImageResource icon) {
		this.icon = icon;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

}
