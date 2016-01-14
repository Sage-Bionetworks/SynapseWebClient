package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DiscussionThreadWidgetViewImpl implements DiscussionThreadWidgetView {

	public interface Binder extends UiBinder<Widget, DiscussionThreadWidgetViewImpl> {}

	public static final String REPLIES = "replies";

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
	@UiField
	FocusPanel showThread;
	@UiField
	Collapse threadDetails;
	@UiField
	FocusPanel showReplies;
	@UiField
	Collapse replyDetails;
	@UiField
	Span author;
	@UiField
	Span createdOn;
	@UiField
	Span clickToViewReplies;
	@UiField
	Button loadMore;
	@UiField
	Button replyButton;

	private Widget widget;
	private DiscussionThreadWidget presenter;

	@Inject
	public DiscussionThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		showThread.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.toggleThread();
			}
		});

		threadDetails.addAttachHandler(new AttachEvent.Handler(){

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					threadDetails.hide();
					threadDetails.setVisible(true);
				}
			}
		});
		replyDetails.addAttachHandler(new AttachEvent.Handler(){

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					replyDetails.hide();
					replyDetails.setVisible(true);
				}
			}
		});
	}

	@Override
	public void addClickHandlerToShowReplies(){
		showReplies.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.toggleReplies();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(DiscussionThreadWidget presenter) {
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
		threadTitle.setText(title);
	}

	@Override
	public void setMessage(String message) {
		threadMessage.setText(message);
	}

	@Override
	public void setActiveUsers(String activeUsers){
		this.activeUsers.setText(activeUsers);
	}

	@Override
	public void setNumberOfReplies(String numberOfReplies) {
		this.numberOfReplies.setText(numberOfReplies);
		this.clickToViewReplies.setText(numberOfReplies + " " + REPLIES);
	}

	@Override
	public void setNumberOfViews(String numberOfViews) {
		this.numberOfViews.setText(numberOfViews);
	}

	@Override
	public void setLastActivity(String lastActivity) {
		this.lastActivity.setText(lastActivity);
	}

	@Override
	public void setAuthor(String author) {
		this.author.setText(author);
	}

	@Override
	public void setCreatedOn(String createdOn) {
		this.createdOn.setText(createdOn);
	}

	@Override
	public void toggleThread() {
		threadDetails.toggle();
	}

	@Override
	public void toggleReplies() {
		replyDetails.toggle();
	}

}
