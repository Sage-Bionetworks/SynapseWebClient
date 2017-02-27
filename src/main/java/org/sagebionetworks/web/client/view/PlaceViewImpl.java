package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlaceViewImpl implements PlaceView {

	public interface PlaceViewImplUiBinder extends UiBinder<Widget, PlaceViewImpl> {}

	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	Div above;
	@UiField
	Div below;
	@UiField
	Div body;
	
	private Header headerWidget;
	private Footer footerWidget;
	
	Widget widget;
	@Inject
	public PlaceViewImpl(PlaceViewImplUiBinder binder,
			Header headerWidget, 
			Footer footerWidget
			) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void initHeaderAndFooter() {
		header.clear();
		headerWidget.configure(false);
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();	
		Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public void add(Widget w) {
		body.add(w);
	}
	
	@Override
	public void clear() {
		body.clear();
	}
	
	@Override
	public void addAboveBody(Widget w) {
		above.add(w);
	}
	@Override
	public void addBelowBody(Widget w) {
		below.add(w);
	}
}
