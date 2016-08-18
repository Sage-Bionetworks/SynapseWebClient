package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DigestWidgetViewImpl implements DigestWidgetView{

	public interface Binder extends UiBinder<Widget, DigestWidgetViewImpl> {}

	@UiField
	TextBox digest;

	Widget widget;
	Presenter presenter;

	@Inject
	public DigestWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		digest.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				digest.selectAll();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setDigest(String digest) {
		this.digest.setText(digest);
	}
}
