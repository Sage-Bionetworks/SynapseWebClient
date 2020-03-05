package org.sagebionetworks.web.client.presenter;

import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.place.AccessRequirementPlace;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class AccessRequirementPresenter extends AbstractActivity implements Presenter<AccessRequirementPlace> {
	private AccessRequirementPlace place;
	private PlaceView view;
	private AccessRequirementWidget arWidget;
	private String requirementId;
	
	@Inject
	public AccessRequirementPresenter(PlaceView view, AccessRequirementWidget arWidget, DivView arDiv) {
		this.view = view;
		this.arWidget = arWidget;
		arDiv.addStyleName("markdown");
		arDiv.add(arWidget.asWidget());
		view.add(arDiv.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(AccessRequirementPlace place) {
		this.place = place;
		view.initHeaderAndFooter();
		requirementId = place.getParam(AccessRequirementPlace.AR_ID_PARAM);
		// Note: configuring the Access Requirement widget without a target subject will result in notifications sent to the user will not have the context (Project/Folder/File associated with the restriction).
		RestrictableObjectDescriptor targetSubject = null;
		arWidget.configure(requirementId, targetSubject);
	}


	public AccessRequirementPlace getPlace() {
		return place;
	}

	@Override
	public String mayStop() {
		return null;
	}
}
