package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PlaceViewImpl implements PlaceView {

	public interface PlaceViewImplUiBinder extends UiBinder<Widget, PlaceViewImpl> {
	}

	@UiField
	Div above;
	@UiField
	Div below;
	@UiField
	Div body;
	@UiField
	Heading title;
	private Header headerWidget;

	Widget widget;

	@Inject
	public PlaceViewImpl(PlaceViewImplUiBinder binder, Header headerWidget) {
		widget = binder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void initHeaderAndFooter() {
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void add(Widget w) {
		body.add(w);
	}

	@Override
	public void addTitle(String text) {
		title.add(new Text(text));
	}

	@Override
	public void addTitle(Widget w) {
		title.add(w);
	}

	@Override
	public void clearAboveBody() {
		above.clear();
	}

	@Override
	public void clearBelowBody() {
		below.clear();
	}

	@Override
	public void clearBody() {
		body.clear();
	}

	@Override
	public void addAboveBody(IsWidget w) {
		above.add(w);
	}

	@Override
	public void addBelowBody(IsWidget w) {
		below.add(w);
	}
}
