package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DivViewImpl implements DivView {
	
	Div div;
	
	@Override
	public Widget asWidget() {
		return div;
	}
	@Override
	public void add(Widget child) {
		div.add(child);
	}
	@Override
	public void add(IsWidget child) {
		div.add(child);
	}
	@Override
	public void clear() {
		div.clear();
	}
}
