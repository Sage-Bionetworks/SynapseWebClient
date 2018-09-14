package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.IconStack;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SingleDiscussionThreadWidgetViewImpl implements SingleDiscussionThreadWidgetView {

	public interface Binder extends UiBinder<Widget, SingleDiscussionThreadWidgetViewImpl> {}

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
	Div synAlertContainer;
	@UiField
	Div refreshAlertContainer;
	@UiField
	LoadingSpinner loadingMessage;
	@UiField
	Icon deleteIcon;
	@UiField
	Image restoreIcon;
	@UiField
	Icon editIcon;
	@UiField
	SimplePanel editThreadModalContainer;
	@UiField
	Label edited;
	@UiField
	Span subscribeButtonContainer;
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
	@UiField
	Button showAllRepliesButton;
	@UiField
	Div newReplyContainer;
	@UiField
	Div secondNewReplyContainer;
	@UiField
	Div deletedThread;
	@UiField
	Span subscribersContainer;
	
	private Widget widget;
	private SingleDiscussionThreadWidget presenter;

	@Inject
	public SingleDiscussionThreadWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
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
		showAllRepliesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				presenter.onClickShowAllReplies();
			}
		});
		restoreIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickRestore();
			}
		});
	}

	@Override
	public void setShowAllRepliesButtonVisible(boolean visible) {
		showAllRepliesButton.setVisible(visible);
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
	public void setRepliesContainer(IsWidget container) {
		replyListContainer.clear();
		replyListContainer.add(container);
	}

	@Override
	public void clear() {
		editThreadModalContainer.clear();
		threadTitle.clear();
		createdOn.clear();
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
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
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
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setEditIconVisible(boolean visible) {
		editIcon.setVisible(visible);
	}

	@Override
	public void setEditThreadModal(Widget widget) {
		editThreadModalContainer.clear();
		editThreadModalContainer.add(widget);
	}

	@Override
	public void setEditedLabelVisible(Boolean visible) {
		edited.setVisible(visible);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(message);
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
	public void setReplyContainersVisible(boolean visible) {
		newReplyContainer.setVisible(visible);
		secondNewReplyContainer.setVisible(visible);
	}

	@Override
	public void setSecondNewReplyContainerVisible(boolean visible) {
		secondNewReplyContainer.setVisible(visible);
	}

	@Override
	public void setDeletedThreadVisible(boolean visible) {
		deletedThread.setVisible(visible);
	}

	@Override
	public void setReplyListContainerVisible(boolean visible) {
		replyListContainer.setVisible(visible);
	}

	@Override
	public void setRestoreIconVisible(boolean visible) {
		restoreIcon.setVisible(visible);
	}

	@Override
	public void setNewReplyContainer(Widget widget) {
		newReplyContainer.clear();
		newReplyContainer.add(widget);
	}

	@Override
	public void setSecondNewReplyContainer(Widget widget) {
		secondNewReplyContainer.clear();
		secondNewReplyContainer.add(widget);
	}
	
	@Override
	public void setSubscribersWidget(Widget widget) {
		subscribersContainer.clear();
		subscribersContainer.add(widget);
	}
	@Override
	public void setSubscribersWidgetContainerVisible(boolean visible) {
		subscribersContainer.setVisible(visible);
	}
}
