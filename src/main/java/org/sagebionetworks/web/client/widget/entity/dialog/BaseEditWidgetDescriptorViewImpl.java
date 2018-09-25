package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BaseEditWidgetDescriptorViewImpl implements BaseEditWidgetDescriptorView {
	public interface Binder extends UiBinder<Widget, BaseEditWidgetDescriptorViewImpl> {}
	
	//the modal
	Modal modal;

	@UiField
	SimplePanel paramsPanel;
	@UiField
	Button okButton;
	@UiField
	SimplePanel errorContainer;
	private Presenter presenter;
	private WidgetEditorPresenter widgetDescriptorPresenter;
	private WidgetRegistrar widgetRegistrar;
	private DialogCallback dialogCallback;
	private SynapseAlert synAlert;
	
	@Inject
	public BaseEditWidgetDescriptorViewImpl(Binder binder, WidgetRegistrar widgetRegistrar, SynapseAlert synAlert) {
		this.modal = (Modal)binder.createAndBindUi(this);
		this.widgetRegistrar = widgetRegistrar;
		this.synAlert = synAlert;
		errorContainer.setWidget(synAlert.asWidget());
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.apply();
			}
		});
		okButton.addDomHandler(DisplayUtils.getPreventTabHandler(okButton), KeyDownEvent.getType());
		
		dialogCallback = new DialogCallback() {
			@Override
			public void setPrimaryEnabled(boolean enable) {
				okButton.setEnabled(enable);
			}
		};
	}
	
	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void hide() {
		modal.hide();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}


	@Override
	public void showErrorMessage(String message) {
		synAlert.showError(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void setWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> widgetDescriptor) {
		//clear out params panel.  Get the right params editor based on the descriptor (it's concrete class, and configure based on the parameters inside of it).
		synAlert.clear();
		okButton.setEnabled(true);
		paramsPanel.clear();
		widgetRegistrar.getWidgetEditorForWidgetDescriptor(wikiKey, contentTypeKey, widgetDescriptor, dialogCallback, new AsyncCallback<WidgetEditorPresenter>() {
			@Override
			public void onSuccess(WidgetEditorPresenter result) {
				widgetDescriptorPresenter = result;
				if (widgetDescriptorPresenter != null) {
					Widget w = widgetDescriptorPresenter.asWidget();
					paramsPanel.add(w);
					//finish setting up the main dialog
					String friendlyName = widgetRegistrar.getFriendlyTypeName(contentTypeKey);
					modal.setTitle(friendlyName);
					DisplayUtils.focusOnChildInput(modal);
				} else {
					showErrorMessage("No editor found for the content type: " + contentTypeKey);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage("Editor not loaded: " + caught.getMessage());
			}
		});
	}
	
	
	
	@Override
	public String getTextToInsert() {
		return widgetDescriptorPresenter.getTextToInsert();
	}

	@Override
	public void updateDescriptorFromView() {
		widgetDescriptorPresenter.updateDescriptorFromView();
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return widgetDescriptorPresenter.getNewFileHandleIds();
	}
	
	@Override
	public List<String> getDeletedFileHandleIds() {
		return widgetDescriptorPresenter.getDeletedFileHandleIds();
	}
	
	@Override
	public void clear() {
	}
	@Override
	public void clearErrors() {
		synAlert.clear();
	}
}
