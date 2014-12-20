package org.sagebionetworks.web.client.widget.entity.renderer;


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
/**
 * Implementation with zero business logic.
 * @author John
 *
 */
public class WikiSubpagesOrderEditorModalWidgetViewImpl implements
									WikiSubpagesOrderEditorModalWidgetView {

	public interface Binder extends
			UiBinder<Modal, WikiSubpagesOrderEditorModalWidgetViewImpl> {
	}

	@UiField
	Modal uiModal;
	@UiField
	SimplePanel editorPanel;
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;

	Modal modal;

	@Inject
	public WikiSubpagesOrderEditorModalWidgetViewImpl(Binder binder) {
		modal = binder.createAndBindUi(this);
	}

	@Override
	public void showDialog() {
		modal.show();
	}

	@Override
	public void setDefaultButtonText(String text) {
		defaultButton.setText(text);
	}

	@Override
	public void setPrimaryButtonVisible(boolean visible) {
		primaryButton.setVisible(visible);
	}

	@Override
	public void setPrimaryButtonEnabled(boolean enabled) {
		primaryButton.setEnabled(enabled);
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void addEditor(IsWidget editor) {
		this.editorPanel.setWidget(editor);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPrimary();
			}
		});
	}

	@Override
	public void hideDialog() {
		modal.hide();
	}

	@Override
	public void setLoading(boolean loading) {
		if(loading){
			this.primaryButton.state().loading();
		}else{
			this.primaryButton.state().reset();
		}
	}
}
