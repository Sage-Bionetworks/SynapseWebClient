package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
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
	SimplePanel author;
	@UiField
	Span createdOn;
	@UiField
	Span clickToViewReplies;
	@UiField
	Button loadMoreButton;
	@UiField
	Button replyButton;
	@UiField
	SimplePanel newReplyModalContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Icon threadDownIcon;
	@UiField
	Icon threadUpIcon;
	@UiField
	Icon replyDownIcon;
	@UiField
	Icon replyUpIcon;

	private Widget widget;
	private DiscussionThreadWidget presenter;

	@Inject
	public DiscussionThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		loadMoreButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.loadMore();
			}
		});
		showThread.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.toggleThread();
			}
		});
		showReplies.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.toggleReplies();
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
		replyButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickNewReply();
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
		threadTitle.clear();
		threadMessage.clear();
		activeUsers.clear();
		numberOfReplies.clear();
		clickToViewReplies.clear();
		lastActivity.clear();
		createdOn.clear();
		replyListContainer.clear();
		threadDownIcon.setVisible(true);
		threadUpIcon.setVisible(false);
		replyDownIcon.setVisible(true);
		replyUpIcon.setVisible(false);
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
	public void setAuthor(Widget author) {
		this.author.add(author);
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

	@Override
	public void setNewReplyModal(Widget w) {
		newReplyModalContainer.setWidget(w);
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setLoadMoreButtonVisibility(boolean visible) {
		loadMoreButton.setVisible(visible);
	}

	@Override
	public void setShowRepliesVisibility(boolean visible) {
		showReplies.setVisible(visible);
	}

	@Override
	public void showReplyDetails() {
		replyDetails.show();
	}

	@Override
	public void clearReplies() {
		replyListContainer.clear();
	}

	@Override
	public void addActiveAuthor(Widget user) {
		activeUsers.add(user);
	}

	@Override
	public boolean isThreadCollapsed() {
		return threadDetails.isHidden();
	}

	@Override
	public void setThreadUpIconVisible(boolean visible) {
		threadUpIcon.setVisible(visible);
	}

	@Override
	public void setThreadDownIconVisible(boolean visible) {
		threadDownIcon.setVisible(visible);
	}

	@Override
	public boolean isReplyCollapsed() {
		return replyDetails.isHidden();
	}

	@Override
	public void setReplyUpIconVisible(boolean visible) {
		replyUpIcon.setVisible(visible);
	}

	@Override
	public void setReplyDownIconVisible(boolean visible) {
		replyDownIcon.setVisible(visible);
	}
}
