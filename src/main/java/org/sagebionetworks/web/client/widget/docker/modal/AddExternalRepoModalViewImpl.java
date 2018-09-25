package org.sagebionetworks.web.client.widget.docker.modal;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddExternalRepoModalViewImpl implements AddExternalRepoModalView{
	public interface Binder extends UiBinder<Widget, AddExternalRepoModalViewImpl> {}

	@UiField
	Button saveButton;
	@UiField
	Button cancelButton;
	@UiField
	Modal addExternalRepoModal;
	@UiField
	TextBox repoName;
	@UiField
	Div synAlertContainer;

	private Widget widget;
	private Presenter presenter;

	@Inject
	public AddExternalRepoModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addExternalRepoModal.hide();
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
	public void showDialog() {
		addExternalRepoModal.show();
	}

	@Override
	public void hideDialog() {
		addExternalRepoModal.hide();
	}

	@Override
	public String getRepoName() {
		return repoName.getText();
	}

	@Override
	public void clear() {
		repoName.setText("");
		saveButton.state().reset();
	}

	@Override
	public void setAlert(Widget widget) {
		synAlertContainer.add(widget);
	}

	@Override
	public void showSuccess(String title, String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showSaving() {
		saveButton.state().loading();
	}

	@Override
	public void resetButton() {
		saveButton.state().reset();
	}

	@Override
	public void setModalTitle(String title) {
		addExternalRepoModal.setTitle(title);
	}
}
