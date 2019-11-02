package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.header.Header;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownViewImpl implements DownView {
	private Header headerWidget;
	@UiField
	Heading messageHeading;
	@UiField
	Text secondsText;
	@UiField
	Div timerUI;

	public interface Binder extends UiBinder<Widget, DownViewImpl> {
	}

	Widget widget;

	@Inject
	public DownViewImpl(Binder uiBinder, Header headerWidget) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
	}

	@Override
	public void init() {
		headerWidget.configure();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void setMessage(String message) {
		messageHeading.setSubText(message);
	}

	@Override
	public void updateTimeToNextRefresh(int seconds) {
		secondsText.setText(" " + seconds);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setTimerVisible(boolean visible) {
		timerUI.setVisible(visible);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}
}
