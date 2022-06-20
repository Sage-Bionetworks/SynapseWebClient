package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenDataViewImpl implements IsWidget{
	public interface Binder  extends UiBinder<Div, OpenDataViewImpl> {}
	private Div widget;
	@UiField
	Div isPublicAndOpen;
	@UiField
	Div isPublicAndAdmin;
	@UiField
	Div isPrivateAndOpenAndAdmin;
	
	@Inject
	public OpenDataViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	public void configure(boolean isOpenData, boolean canChangePermission, boolean isPubliclyVisible) {
		isPublicAndOpen.setVisible(false);
		isPublicAndAdmin.setVisible(false);
		isPrivateAndOpenAndAdmin.setVisible(false);
		if (isPubliclyVisible) {
			if (isOpenData) {
				// This really is open data
				isPublicAndOpen.setVisible(true);
			} else if (canChangePermission) {
				// This is not really open data
				isPublicAndAdmin.setVisible(true);
			}
		} else {
			if (isOpenData && canChangePermission) {
				isPrivateAndOpenAndAdmin.setVisible(true);
			}
		}
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
