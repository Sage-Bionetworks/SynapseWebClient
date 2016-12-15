package org.sagebionetworks.web.client.widget.discussion;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NewReplyWidgetViewImpl implements NewReplyWidgetView{

	public interface Binder extends UiBinder<Widget, NewReplyWidgetViewImpl> {}

	@UiField
	TextBox replyTextBox;
	@UiField
	Div newReplyContainer;
	@UiField
	Div markdownEditorContainer;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;
	@UiField
	Div synAlert;

	private Presenter presenter;
	private Widget widget;

	@Inject
	public NewReplyWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		replyTextBox.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClickNewReply();
			}
		});
		cancelButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onCancel();
			}
		});
		saveButton.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
	}

	@Override
	public void setReplyTextBoxVisible(boolean visible) {
		replyTextBox.setVisible(visible);
	}

	@Override
	public void resetButton() {
		saveButton.state().reset();
	}

	@Override
	public void setNewReplyContainerVisible(boolean visible) {
		newReplyContainer.setVisible(visible);
	}

	@Override
	public void setMarkdownEditor(Widget widget) {
		markdownEditorContainer.add(widget);
	}

	@Override
	public void showSaving() {
		saveButton.state().loading();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setAlert(Widget widget) {
		this.synAlert.clear();
		synAlert.add(widget);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public Widget asWidget() {
		return this.widget;
	}

	@Override
	public void showErrorMessage(String error) {
		DisplayUtils.showErrorMessage(error);
	}

}
