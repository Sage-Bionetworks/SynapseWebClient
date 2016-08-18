package org.sagebionetworks.web.client.widget.docker;

import java.util.Date;

import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerCommitRowWidgetViewImpl implements DockerCommitRowWidgetView {
	public static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

	@UiField
	Span tag;
	@UiField
	TextBox createdOn;
	@UiField
	Span digest;
	@UiField
	FocusPanel row;

	private Widget widget;
	private Presenter presenter;

	public interface Binder extends UiBinder<Widget, DockerCommitRowWidgetViewImpl> {}

	@Inject
	public DockerCommitRowWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
		row.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onClick();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setTag(String tag) {
		this.tag.setText(tag);
	}

	@Override
	public void setDigest(Widget widget) {
		this.digest.clear();
		this.digest.add(widget);
	}

	@Override
	public void setCreatedOn(Date createdOn) {
		this.createdOn.setText(DATE_FORMAT.format(createdOn));
	}

}
