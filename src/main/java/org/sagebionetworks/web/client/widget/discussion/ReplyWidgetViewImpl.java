package org.sagebionetworks.web.client.widget.discussion;

import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_CANCEL;
import static org.sagebionetworks.web.client.DisplayConstants.BUTTON_DELETE;
import static org.sagebionetworks.web.client.DisplayConstants.DANGER_BUTTON_STYLE;
import static org.sagebionetworks.web.client.DisplayConstants.DEFAULT_BUTTON_STYLE;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.SimpleCallback;
import org.gwtbootstrap3.extras.bootbox.client.options.DialogOptions;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ReplyWidgetViewImpl implements ReplyWidgetView {

	private static final String CONFIRM_DELETE_DIALOG_TITLE = "Confirm Deletion";

	public interface Binder extends UiBinder<Widget, ReplyWidgetViewImpl> {}

	@UiField
	Span author;
	@UiField
	Span createdOn;
	@UiField
	Div replyMessage;
	@UiField
	Div synAlertContainer;
	@UiField
	Div copyTextModalContainer;
	
	@UiField
	Icon deleteIcon;
	@UiField
	Icon editIcon;
	@UiField
	Icon linkIcon;
	
	@UiField
	SimplePanel editReplyModalContainer;
	@UiField
	Label edited;
	@UiField
	HTMLPanel loadingMessage;
	@UiField
	Label moderatorBadge;
	@UiField
	Div commandsContainer;

	private Widget widget;
	private ReplyWidget presenter;
	
	@Inject
	public ReplyWidgetViewImpl (Binder binder) {
		widget = binder.createAndBindUi(this);
		deleteIcon.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickDeleteReply();
			}
		});
		editIcon.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickEditReply();
			}
		});
		linkIcon.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickReplyLink();
			}
		});
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
	public void setMarkdownWidget(Widget widget) {
		this.replyMessage.add(widget);
	}

	@Override
	public void clear() {
		this.createdOn.clear();
	}

	@Override
	public void setAlert(Widget w) {
		synAlertContainer.add(w);
	}

	@Override
	public void setDeleteIconVisibility(Boolean visible) {
		deleteIcon.setVisible(visible);
	}

	@Override
	public void showDeleteConfirm(String deleteConfirmMessage, SimpleCallback deleteCallback) {
		DialogOptions options = DialogOptions.newOptions(deleteConfirmMessage);
		options.setCloseButton(false);
		options.setTitle(CONFIRM_DELETE_DIALOG_TITLE);
		options.addButton(BUTTON_CANCEL, DEFAULT_BUTTON_STYLE);
		options.addButton(BUTTON_DELETE, DANGER_BUTTON_STYLE, deleteCallback);
		
		Bootbox.dialog(options);
		
	}

	@Override
	public void setEditIconVisible(boolean visible) {
		editIcon.setVisible(visible);
	}

	@Override
	public void setEditReplyModal(Widget widget) {
		editReplyModalContainer.add(widget);
	}

	@Override
	public void setEditedVisible(Boolean visible) {
		edited.setVisible(visible);
	}

	@Override
	public void setLoadingMessageVisible(Boolean visible) {
		loadingMessage.setVisible(visible);
	}

	@Override
	public void setMessageVisible(boolean visible) {
		replyMessage.setVisible(visible);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	
	@Override
	public void setIsAuthorModerator(boolean isModerator) {
		moderatorBadge.setVisible(isModerator);
	}

	@Override
	public void setCommandsContainerVisible(boolean visible) {
		commandsContainer.setVisible(visible);
	}

	@Override
	public void setCopyTextModal(Widget widget) {
		copyTextModalContainer.clear();
		copyTextModalContainer.add(widget);
	}
}
