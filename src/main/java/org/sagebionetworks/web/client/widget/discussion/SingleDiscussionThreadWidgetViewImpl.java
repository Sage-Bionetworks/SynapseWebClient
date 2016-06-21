package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_CANCEL;
import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_DELETE;
import static org.sagebionetworks.web.client.DisplayConstants.DANGER_BUTTON_STYLE;
import static org.sagebionetworks.web.client.DisplayConstants.DEFAULT_BUTTON_STYLE;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.IconStack;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.AlertCallback;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SingleDiscussionThreadWidgetViewImpl implements SingleDiscussionThreadWidgetView {

	public interface Binder extends UiBinder<Widget, SingleDiscussionThreadWidgetViewImpl> {}

	private static final String CONFIRM_DELETE_DIALOG_TITLE = "Confirm Deletion";

	@UiField
	Div replyListContainer;
	@UiField
	Span threadTitle;
	@UiField
	Div threadMessage;
	@UiField
	Span author;
	@UiField
	Span createdOn;
	@UiField
	HTMLPanel loadMore;
	@UiField
	Button replyButton;
	@UiField
	SimplePanel newReplyModalContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div refreshAlertContainer;
	@UiField
	HTMLPanel loadingReplies;
	@UiField
	HTMLPanel loadingMessage;
	@UiField
	Icon deleteIcon;
	@UiField
	Icon editIcon;
	@UiField
	SimplePanel editThreadModalContainer;
	@UiField
	Label edited;
	@UiField
	Span subscribeButtonContainer;
	@UiField
	Div threadButtonContainer;
	@UiField
	IconStack unpinIconStack;
	@UiField
	Icon unpinIcon;
	@UiField
	Icon pinIcon;
	@UiField
	Label moderatorBadge;
	@UiField
	Div commandsContainer;
	String threadLinkHref;
	private Widget widget;
	private SingleDiscussionThreadWidget presenter;

	@Inject
	public SingleDiscussionThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		replyButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickNewReply();
			}
		});
		deleteIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickDeleteThread();
			}
		});
		editIcon.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickEditThread();
			}
		});
		
		pinIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickPinThread();
			}
		});
		
		unpinIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickUnpinThread();
			}
		});
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(SingleDiscussionThreadWidget presenter) {
		this.presenter = presenter;
	}

	@Override
	public void addReply(Widget w) {
		replyListContainer.add(w);
	}

	@Override
	public void clear() {
		threadTitle.clear();
		createdOn.clear();
		replyListContainer.clear();
	}

	@Override
	public void setTitle(String title) {
		threadTitle.setText(title);
	}

	@Override
	public void setMarkdownWidget(Widget widget) {
		threadMessage.add(widget);
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
	public void setNewReplyModal(Widget w) {
		newReplyModalContainer.setWidget(w);
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setLoadMoreVisibility(boolean visible) {
		loadMore.setVisible(visible);
	}

	@Override
	public void clearReplies() {
		replyListContainer.clear();
	}

	@Override
	public void setLoadingRepliesVisible(boolean visible) {
		loadingReplies.setVisible(visible);
	}

	@Override
	public void setLoadingMessageVisible(boolean visible) {
		loadingMessage.setVisible(visible);
	}

	@Override
	public void setDeleteIconVisible(boolean visible) {
		deleteIcon.setVisible(visible);
	}

	@Override
	public void showDeleteConfirm(String deleteConfirmMessage, final AlertCallback deleteCallback) {
		Bootbox.Dialog.create()
				.setMessage(deleteConfirmMessage)
				.setCloseButton(false)
				.setTitle(CONFIRM_DELETE_DIALOG_TITLE)
				.addButton(BUTTON_CANCEL, DEFAULT_BUTTON_STYLE)
				.addButton(BUTTON_DELETE, DANGER_BUTTON_STYLE, deleteCallback)
				.show();
	}

	@Override
	public void setReplyButtonVisible(boolean visible) {
		replyButton.setVisible(visible);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setEditIconVisible(boolean visible) {
		editIcon.setVisible(visible);
	}

	@Override
	public void setEditThreadModal(Widget widget) {
		editThreadModalContainer.add(widget);
	}

	@Override
	public void setEditedVisible(Boolean visible) {
		edited.setVisible(visible);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void setThreadLink(String link){
		threadLinkHref = link;
	}
	@Override
	public void setSubscribeButtonWidget(Widget widget) {
		subscribeButtonContainer.clear();
		subscribeButtonContainer.add(widget);
	}
	
	@Override
	public void setRefreshAlert(Widget w) {
		refreshAlertContainer.clear();
		refreshAlertContainer.add(w);
	}
	@Override
	public void removeRefreshAlert() {
		refreshAlertContainer.clear();
	}

	@Override
	public void setButtonContainerWidth(String width){
		threadButtonContainer.setWidth(width);
	}
	
	@Override
	public void setPinIconVisible(boolean visible) {
		pinIcon.setVisible(visible);
	}
	
	@Override
	public void setUnpinIconVisible(boolean visible) {
		unpinIconStack.setVisible(visible);
	}
	
	@Override
	public void setIsAuthorModerator(boolean isModerator) {
		moderatorBadge.setVisible(isModerator);
	}
	
	@Override
	public void setCommandsVisible(boolean visible) {
		commandsContainer.setVisible(visible);
	}

	@Override
	public boolean isLoadMoreAttached() {
		return loadMore.isAttached();
	}

	@Override
	public boolean isLoadMoreInViewport() {
		return DisplayUtils.isInViewport(loadMore.asWidget());
	}

	@Override
	public boolean getLoadMoreVisibility() {
		return loadMore.isVisible();
	}
}
