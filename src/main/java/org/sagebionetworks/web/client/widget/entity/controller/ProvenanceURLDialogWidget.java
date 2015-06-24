package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceURLDialogWidget implements ProvenanceURLDialogWidgetView.Presenter, IsWidget {
	
	ProvenanceURLDialogWidgetView view;
	Callback confirmCallback;
	SynapseAlert synAlert;
	
	@Inject
	public ProvenanceURLDialogWidget(ProvenanceURLDialogWidgetView view, SynapseAlert synAlert) {
		this.view = view;
		this.synAlert = synAlert;
		this.view.setPresenter(this);
		this.view.setSynAlertWidget(synAlert);
	}
	
	@Override
	public void configure(Callback confirmCallback) {
		this.confirmCallback = confirmCallback;
	}
	
	@Override
	public void show() {
		synAlert.clear();
		view.clear();
		view.show();
	}
	
	@Override
	public String getURLName() {
		return view.getURLName();
	}
	
	@Override
	public String getURLAddress() {
		return view.getURLAddress();
	}
	
	@Override
	public void hide() {
		view.hide();
	}
	
	@Override
	public void onSave() {
		if (view.getURLAddress().isEmpty()) {
			synAlert.showError("External URL must not be empty!");
		} else if (confirmCallback != null) {
			confirmCallback.invoke();
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
