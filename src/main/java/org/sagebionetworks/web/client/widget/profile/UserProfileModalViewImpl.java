package org.sagebionetworks.web.client.widget.profile;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserProfileModalViewImpl implements UserProfileModalView {
	
	public interface Binder extends UiBinder<Widget, UserProfileModalViewImpl> {}
	
	@UiField
	Modal modal;
	@UiField
	Button primaryButton;
	@UiField
	SimplePanel bodyPanel;
	@UiField
	SimplePanel loadingPanel;
	@UiField
	Alert alert;
	
	Widget widget;
	
	@Inject
	public UserProfileModalViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
	}

	@Override
	public void setLoading(boolean loading) {
		bodyPanel.setVisible(!loading);
		loadingPanel.setVisible(loading);
	}

	@Override
	public void showError(String message) {
		alert.setVisible(true);
		alert.setText(message);
	}

	@Override
	public void showModal() {
		modal.show();
	}

	@Override
	public void hideError() {
		alert.setVisible(false);
	}

	@Override
	public void setProcessing(boolean processing) {
		if(processing){
			primaryButton.state().loading();
		}else{
			primaryButton.state().reset();
		}
	}

	@Override
	public void hideModal() {
		modal.hide();
	}

	@Override
	public void addEditorWidget(IsWidget editorWidget) {
		this.bodyPanel.add(editorWidget);
	}
	
}
