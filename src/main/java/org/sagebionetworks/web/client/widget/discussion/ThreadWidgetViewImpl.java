package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ThreadWidgetViewImpl implements ThreadWidgetView {

	public interface Binder extends UiBinder<Widget, ThreadWidgetViewImpl> {}

	@UiField
	Div replyListContainer;
	@UiField
	Span threadTitle;
	@UiField
	Paragraph threadMessage;
	@UiField
	Span activeUsers;
	@UiField
	Span numberOfReplies;
	@UiField
	Span numberOfViews;
	@UiField
	Span lastActivity;

	private Widget widget;
	private ThreadWidget presenter;

	@Inject
	public ThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(ThreadWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addReply(Widget w) {
		replyListContainer.add(w);
	}

	@Override
	public void clear() {
		replyListContainer.clear();
	}

	@Override
	public void setTitle(String title) {
		threadTitle.setHTML(title);
	}

	@Override
	public void setMessage(String message) {
		threadMessage.setHTML(message);
	}

	@Override
	public void setActiveUsers(String activeUsers){
		this.activeUsers.setHTML(activeUsers);
	}

	@Override
	public void setNumberOfReplies(String numberOfReplies) {
		this.numberOfReplies.setHTML(numberOfReplies);
	}

	@Override
	public void setNumberOfViews(String numberOfViews) {
		this.numberOfViews.setHTML(numberOfViews);
	}

	@Override
	public void setLastActivity(String lastActivity) {
		this.lastActivity.setHTML(lastActivity);
	}

}
