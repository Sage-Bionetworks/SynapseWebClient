package org.sagebionetworks.web.client.widget.table.modal;

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

public class UploadTableModalViewImpl implements UploadTableModalView {
	
	public interface Binder extends UiBinder<Modal, UploadTableModalViewImpl> {}
	
	@UiField
	Button primaryButton;
	@UiField
	SimplePanel bodyPanel;
	
	Modal modal;
	
	@Inject
	public UploadTableModalViewImpl(Binder binder){
		modal = binder.createAndBindUi(this);
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
	public Widget asWidget() {
		return modal;
	}


	@Override
	public void showModal() {
		modal.show();
	}

	@Override
	public void setBody(IsWidget body) {
		bodyPanel.clear();
		bodyPanel.add(body);
	}

}
