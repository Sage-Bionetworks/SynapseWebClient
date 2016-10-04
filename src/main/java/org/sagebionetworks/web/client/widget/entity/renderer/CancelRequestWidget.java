package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.sagebionetworks.web.client.ChallengeClientAsync;
import org.sagebionetworks.web.client.DisplayConstants;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CancelRequestWidget implements SingleButtonView.Presenter, IsWidget {
	
	private SingleButtonView view;
	private ChallengeClientAsync challengeClient;
	@Inject
	public CancelRequestWidget(SingleButtonView view, 
			ChallengeClientAsync challengeClient) {
		this.view = view;
		this.challengeClient = challengeClient;
//		view.setButtonVisible(false);
		view.setButtonText(DisplayConstants.BUTTON_CANCEL);
		view.setButtonType(ButtonType.DANGER);
		view.setPresenter(this);
	}
	
	public void configure(String json) {
		
	}
	
	@Override
	public void onClick() {
		
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
