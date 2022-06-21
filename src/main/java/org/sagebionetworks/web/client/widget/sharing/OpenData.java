package org.sagebionetworks.web.client.widget.sharing;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenData implements IsWidget {

	OpenDataView view;
	
	@Inject
	public OpenData(OpenDataView view) {
		this.view = view;
	}
	
	public void configure(boolean isOpenData, boolean canChangePermission, boolean isPubliclyVisible) {
		view.reset();
		if (canChangePermission) {
			if (isPubliclyVisible) {
				if (isOpenData) {
					// This really is open data
					view.setIsPublicAndOpenAndAdmin();
				} else {
					// This is not really open data
					view.setIsPublicAndAdmin();
				}
			} else {
				if (isOpenData) {
					view.setIsPrivateAndOpenAndAdmin();
				}
			}	
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
