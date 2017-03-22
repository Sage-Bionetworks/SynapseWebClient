package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionsPresenter extends AbstractActivity implements Presenter<ACTDataAccessSubmissionsPlace>, ACTDataAccessSubmissionsView.Presenter {
	//TODO: replace with correct state filter type when available
	private VerificationStateEnum stateFilter;
	private ACTDataAccessSubmissionsPlace place;
	private ACTDataAccessSubmissionsView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	LoadMoreWidgetContainer loadMoreContainer;
	public static Long LIMIT = 30L;
	Long currentOffset;
	ACTAccessRequirementWidget actAccessRequirementWidget;
	boolean isAccessRequirementVisible;
	public static final String HIDE_AR_TEXT = "Hide Access Requirement";
	public static final String SHOW_AR_TEXT = "Show Access Requirement";
	public static final String INVALID_AR_ID = "Invalid Access Requirement ID";
	Date fromDate, toDate;
	ACTAccessRequirement actAccessRequirement;
	FileHandleWidget ducTemplateFileHandleWidget;
	List<String> states;
	
	@Inject
	public ACTDataAccessSubmissionsPresenter(
			final ACTDataAccessSubmissionsView view,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			LoadMoreWidgetContainer loadMoreContainer,
			ACTAccessRequirementWidget actAccessRequirementWidget,
			final Button showHideAccessRequirementButton,
			FileHandleWidget ducTemplateFileHandleWidget
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.synapseClient = synapseClient;
		this.loadMoreContainer = loadMoreContainer;
		this.actAccessRequirementWidget = actAccessRequirementWidget;
		this.globalAppState = globalAppState;
		this.ducTemplateFileHandleWidget = ducTemplateFileHandleWidget;
		states = new ArrayList<String>();
		//TODO: replace with correct state filter type when available
		for (VerificationStateEnum state : VerificationStateEnum.values()) {
			states.add(state.toString());
		}
		view.setStates(states);
		isAccessRequirementVisible = false;
		showHideAccessRequirementButton.setText(SHOW_AR_TEXT);
		view.setAccessRequirementUIVisible(false);
		showHideAccessRequirementButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				isAccessRequirementVisible = !isAccessRequirementVisible;
				String buttonText = isAccessRequirementVisible ? HIDE_AR_TEXT : SHOW_AR_TEXT;
				showHideAccessRequirementButton.setText(buttonText);
				view.setAccessRequirementUIVisible(isAccessRequirementVisible);
			}
		});
		view.setSynAlert(synAlert);
		view.setAccessRequirementWidget(actAccessRequirementWidget);
		view.setLoadMoreContainer(loadMoreContainer);
		view.setShowHideButton(showHideAccessRequirementButton);
		view.setPresenter(this);
		
		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	@Override
	public void setPlace(ACTDataAccessSubmissionsPlace place) {
		this.place = place;
		actAccessRequirement = null;
		String accessRequirementId = place.getParam(ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM);
		String fromTime = place.getParam(ACTDataAccessSubmissionsPlace.MIN_DATE_PARAM);
		if (fromTime != null) {
			fromDate = new Date(Long.parseLong(fromTime));
			view.setSelectedMinDate(fromDate);
		}
		String toTime = place.getParam(ACTDataAccessSubmissionsPlace.MAX_DATE_PARAM);
		if (toTime != null) {
			toDate = new Date(Long.parseLong(toTime));
			view.setSelectedMaxDate(toDate);
		}
		synAlert.clear();
		if (accessRequirementId != null) {
			synapseClient.getAccessRequirement(Long.parseLong(accessRequirementId), new AsyncCallback<AccessRequirement>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.showError(INVALID_AR_ID);
				}
				@Override
				public void onSuccess(AccessRequirement requirement) {
					if (requirement instanceof ACTAccessRequirement) {
						actAccessRequirement = (ACTAccessRequirement) requirement;
						if (actAccessRequirement.getDucTemplateFileHandleId() != null) {
							FileHandleAssociation fha = new FileHandleAssociation();
							// TODO: change to Access Requirement type when available.
							fha.setAssociateObjectType(FileHandleAssociateType.TeamAttachment);
							fha.setAssociateObjectId(actAccessRequirement.getId().toString());
							fha.setFileHandleId(actAccessRequirement.getDucTemplateFileHandleId());
							ducTemplateFileHandleWidget.configure(fha);	
						}
						view.setAreOtherAttachmentsRequired(actAccessRequirement.getAreOtherAttachmentsRequired());
						view.setIsAnnualReviewRequired(actAccessRequirement.getIsAnnualReviewRequired());
						view.setIsCertifiedUserRequired(actAccessRequirement.getIsCertifiedUserRequired());
						view.setIsDUCRequired(actAccessRequirement.getIsDUCRequired());
						view.setIsIDUPublic(actAccessRequirement.getIsIDUPublic());
						view.setIsIRBApprovalRequired(actAccessRequirement.getIsIRBApprovalRequired());
						view.setIsValidatedProfileRequired(actAccessRequirement.getIsValidatedProfileRequired());
						
						actAccessRequirementWidget.setRequirement(actAccessRequirement);
						view.setDucColumnVisible(actAccessRequirement.getIsDUCRequired());
						view.setIrbColumnVisible(actAccessRequirement.getIsIRBApprovalRequired());
						view.setOtherAttachmentsColumnVisible(actAccessRequirement.getAreOtherAttachmentsRequired());
						loadData();
					} else {
						synAlert.showError(INVALID_AR_ID + ": wrong type - " + requirement.getClass().getName());
					}
				}
			});
		} else {
			synAlert.showError(INVALID_AR_ID);
		}
	}
	
	public void loadData() {
		loadMoreContainer.clear();
		currentOffset = 0L;
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		globalAppState.pushCurrentPlace(place);
		// TODO: ask for data access submissions once call is available, and create a widget to render.
//		synapseClient.getDataAccessSubmissions(accessRequirementId, stateFilter, fromDate, toDate, LIMIT, currentOffset, new AsyncCallback<List<DataAccessSubmission>>() {
//			@Override
//			public void onFailure(Throwable caught) {
//				synAlert.handleException(caught);
//				loadMoreContainer.setIsMore(false);
//			}
//			
//			public void onSuccess(List<DataAccessSubmission> dataSubmissions) {
//				currentOffset += LIMIT;
//				for (DataAccessSubmission submission : dataSubmissions) {
//					// create a new row for each data access submission.
//					DataAccessSubmissionWidget w = ginInjector.getDataAccessSubmissionWidget();
//					w.configure(submission); 
//					loadMoreContainer.add(w.asWidget());
//				}
//				loadMoreContainer.setIsMore(!dataSubmissions.isEmpty());
//			};
//		});
	}
	
	@Override
	public void onClearDateFilter() {
		fromDate = null;
		view.setSelectedMinDate(fromDate);
		place.removeParam(ACTDataAccessSubmissionsPlace.MIN_DATE_PARAM);
		toDate = null;
		view.setSelectedMaxDate(toDate);
		place.removeParam(ACTDataAccessSubmissionsPlace.MAX_DATE_PARAM);
		loadData();
	}
	
	@Override
	public void onClearStateFilter() {
		stateFilter = null;
		place.removeParam(ACTDataAccessSubmissionsPlace.STATE_FILTER_PARAM);
		view.setSelectedStateText("");
		loadData();	
	}
	
	@Override
	public void onMaxDateSelected(Date date) {
		toDate = date;
		place.putParam(ACTDataAccessSubmissionsPlace.MAX_DATE_PARAM, Long.toString(date.getTime()));
		loadData();
	}
	
	@Override
	public void onMinDateSelected(Date date) {
		fromDate = date;
		place.putParam(ACTDataAccessSubmissionsPlace.MIN_DATE_PARAM, Long.toString(date.getTime()));
		loadData();
	}
	
	public ACTDataAccessSubmissionsPlace getPlace() {
		return place;
	}
	
	@Override
    public String mayStop() {
        return null;
    }

	@Override
	public void onStateSelected(String selectedState) {
		//TODO: replace with correct state filter type when available
		stateFilter = VerificationStateEnum.valueOf(selectedState);
		place.putParam(ACTDataAccessSubmissionsPlace.STATE_FILTER_PARAM, selectedState);
		view.setSelectedStateText(selectedState);
		loadData();
	}
	
}
