package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceConfigViewImpl extends LayoutContainer implements ProvenanceConfigView {

	private Presenter presenter;
		
	@Inject
	public ProvenanceConfigViewImpl() {
	}
	
	@Override
	public void initView() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void checkParams() throws IllegalArgumentException {
//		if (!field.isValid())
//			throw new IllegalArgumentException(field.getErrorMessage());
	}

	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public int getDisplayHeight() {
		return 50;
	}
	@Override
	public void clear() {
	}

	/*
	 * Private Methods
	 */

}
