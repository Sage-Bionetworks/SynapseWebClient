package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidgetViewImpl implements ReplyWidgetView {

	public interface Binder extends UiBinder<Widget, ReplyWidgetViewImpl> {}

	@UiField
	Span author;
	@UiField
	Span createdOn;
	@UiField
	Paragraph replyMessage;

	private Widget widget;
	private ReplyWidget presenter;

	@Inject
	public ReplyWidgetViewImpl (Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(ReplyWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAuthor(Widget author){
		this.author.add(author);
	}

	@Override
	public void setCreatedOn(String createdOn) {
		this.createdOn.setText(createdOn);
	}

	@Override
	public void setMessage(String message) {
		this.replyMessage.setText(message);
	}

	@Override
	public void clear() {
		this.author.clear();
		this.createdOn.clear();
		this.replyMessage.clear();
	}
}
