package org.sagebionetworks.web.client.widget.entity.download;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.download.QuizInfoWidgetView.Presenter;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class QuizInfoWidget implements Presenter, SynapseWidgetPresenter {
	private GlobalApplicationState globalApplicationState;
	private QuizInfoWidgetView view;
	private Callback callback;
	
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
	 * @param callback invoked when cancel or continue is clicked.  If this widget is in a dialog, you should hide the dialog in the callback code.
	 */
	public void configure(Callback callback){
		this.callback = callback;
	}

	@Override
	public void buttonClicked() {
		callback.invoke();
	}
}
