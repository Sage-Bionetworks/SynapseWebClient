package org.sagebionetworks.web.client.widget.sharing;

import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AddPeopleToAclPanel implements AddPeopleToAclPanelView.Presenter, SynapseWidgetPresenter {

	private AddPeopleToAclPanelView view;
	
	@Inject
	public AddPeopleToAclPanel(AddPeopleToAclPanelView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
