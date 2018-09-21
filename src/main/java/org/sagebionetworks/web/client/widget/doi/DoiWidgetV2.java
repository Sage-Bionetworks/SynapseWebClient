package org.sagebionetworks.web.client.widget.doi;

import org.sagebionetworks.repo.model.doi.v2.DoiAssociation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetV2 implements IsWidget {
	private DoiWidgetV2View view;

	@Inject
	public DoiWidgetV2(DoiWidgetV2View view) {
		this.view = view;
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void configure(DoiAssociation newDoi) {
		clear();
		if (newDoi != null && newDoi.getDoiUri() != null) {
			view.showDoi(newDoi.getDoiUri());
		}
	}

	public void clear() {
		view.hide();
		view.clear();
	}
}
