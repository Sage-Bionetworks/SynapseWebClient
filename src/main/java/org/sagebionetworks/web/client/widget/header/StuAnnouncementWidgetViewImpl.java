package org.sagebionetworks.web.client.widget.header;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class StuAnnouncementWidgetViewImpl implements StuAnnouncementWidgetView {

	private StuAnnouncementWidget presenter;
	public interface Binder extends UiBinder<Widget, StuAnnouncementWidgetViewImpl> {}
	Widget w;
	@UiField
	Text bubbleText;
	
	@UiField
	FocusPanel panel;
	
	@Inject
	public StuAnnouncementWidgetViewImpl(Binder binder) {
		w = binder.createAndBindUi(this);
		panel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickAnnouncement();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setPresenter(StuAnnouncementWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void hide() {
		w.setVisible(false);
	}
	
	@Override
	public void show(String text) {
		bubbleText.setText(text);
		w.setVisible(true);
	}
}
