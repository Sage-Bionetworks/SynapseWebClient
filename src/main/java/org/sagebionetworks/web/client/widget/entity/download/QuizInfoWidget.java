package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidgetView.Presenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizInfoWidget implements Presenter, SynapseWidgetPresenter {
	private GlobalApplicationState globalApplicationState;
	private QuizInfoWidgetView view;
	private CallbackP<Boolean> callback;
	
	@Inject
	public QuizInfoWidget(QuizInfoWidgetView view, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		
		view.setPresenter(this);
	}

	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	/**
	 * 
	 * @param callback invoked when cancel or continue is clicked.
	 * Will return true clicked continue, false if cancel.  
	 */
	public void configure(boolean isCertificationRequired, CallbackP<Boolean> callback){
		this.callback = callback;
		view.configure(isCertificationRequired);
	}

	@Override
	public void continueClicked() {
		callback.invoke(true);
	}
	@Override
	public void cancelClicked() {
		callback.invoke(false);
	}
}
