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
	@Override
	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setDepth(int depth) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getEntityId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setEntityId(String entityId) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isExpanded() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setIsExpanded(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Private Methods
	 */

}
