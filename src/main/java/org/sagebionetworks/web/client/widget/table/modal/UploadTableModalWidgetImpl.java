package org.sagebionetworks.web.client.widget.table.modal;

import org.sagebionetworks.web.client.widget.table.TableCreatedHandler;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * All business logic is here.
 * 
 * @author John
 *
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget, UploadTableModalView.Presenter {
	
	
	UploadTableModalView view;

	@Inject
	public UploadTableModalWidgetImpl(UploadTableModalView view) {
		this.view = view;
		this.view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onPrimary() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configure(String parentId, TableCreatedHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showModal() {
		view.clear();
		view.showModal();
	}


}
