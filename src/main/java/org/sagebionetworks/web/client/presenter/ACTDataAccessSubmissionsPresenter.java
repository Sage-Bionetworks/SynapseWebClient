package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.MAX_DATE_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.MIN_DATE_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.STATE_FILTER_PARAM;
import static org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep2.DAY_IN_MS;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTDataAccessSubmissionsPresenter extends AbstractActivity implements Presenter<ACTDataAccessSubmissionsPlace>, ACTDataAccessSubmissionsView.Presenter {
	private SubmissionState stateFilter;
	private ACTDataAccessSubmissionsPlace place;
	private ACTDataAccessSubmissionsView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	DataAccessClientAsync dataAccessClient;
	LoadMoreWidgetContainer loadMoreContainer;
	ManagedACTAccessRequirementWidget actAccessRequirementWidget;
	boolean isAccessRequirementVisible;
	public static final String HIDE_AR_TEXT = "Hide Access Requirement";
	public static final String SHOW_AR_TEXT = "Show Access Requirement";
	public static final String INVALID_AR_ID = "Invalid Access Requirement ID";
	Date fromDate, toDate;
	Long actAccessRequirementId;
	FileHandleWidget ducTemplateFileHandleWidget;
	List<String> states;
	boolean isSortedAsc;
	String nextPageToken;
	private ManagedACTAccessRequirement actAccessRequirement;
	private SubjectsWidget subjectsWidget;
	DateTimeFormat dateFormat;
	Callback refreshCallback;

	@Inject
	public ACTDataAccessSubmissionsPresenter(final ACTDataAccessSubmissionsView view, SynapseAlert synAlert, PortalGinInjector ginInjector, LoadMoreWidgetContainer loadMoreContainer, ManagedACTAccessRequirementWidget actAccessRequirementWidget, final Button showHideAccessRequirementButton, FileHandleWidget ducTemplateFileHandleWidget, DataAccessClientAsync dataAccessClient, SubjectsWidget subjectsWidget, GWTWrapper gwt) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.loadMoreContainer = loadMoreContainer;
		this.actAccessRequirementWidget = actAccessRequirementWidget;
		actAccessRequirementWidget.setReviewAccessRequestsVisible(false);
		this.subjectsWidget = subjectsWidget;
		this.ducTemplateFileHandleWidget = ducTemplateFileHandleWidget;
		dateFormat = gwt.getDateTimeFormat(PredefinedFormat.DATE_FULL);
		states = new ArrayList<String>();
		for (SubmissionState state : SubmissionState.values()) {
			states.add(state.toString());
		}
		view.setStates(states);
		isAccessRequirementVisible = false;
		showHideAccessRequirementButton.setText(SHOW_AR_TEXT);
		showHideAccessRequirementButton.setIcon(IconType.TOGGLE_RIGHT);
		view.setAccessRequirementUIVisible(false);
		showHideAccessRequirementButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				isAccessRequirementVisible = !isAccessRequirementVisible;
				String buttonText = isAccessRequirementVisible ? HIDE_AR_TEXT : SHOW_AR_TEXT;
				showHideAccessRequirementButton.setText(buttonText);
				showHideAccessRequirementButton.setIcon(isAccessRequirementVisible ? IconType.TOGGLE_DOWN : IconType.TOGGLE_RIGHT);
				view.setAccessRequirementUIVisible(isAccessRequirementVisible);
			}
		});
		view.setSynAlert(synAlert);
		view.setAccessRequirementWidget(actAccessRequirementWidget);
		view.setLoadMoreContainer(loadMoreContainer);
		view.setShowHideButton(showHideAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
		view.setPresenter(this);

		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		refreshCallback = new Callback() {
			@Override
			public void invoke() {
				loadData();
			}
		};
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(ACTDataAccessSubmissionsPlace place) {
		this.place = place;
		actAccessRequirementId = null;
		isSortedAsc = true;
		String actAccessRequirementIdString = place.getParam(ACCESS_REQUIREMENT_ID_PARAM);
		String fromTime = place.getParam(MIN_DATE_PARAM);
		if (fromTime != null) {
			fromDate = new Date(Long.parseLong(fromTime));
			view.setSelectedMinDate(fromDate);
		}
		String toTime = place.getParam(MAX_DATE_PARAM);
		if (toTime != null) {
			toDate = new Date(Long.parseLong(toTime));
			view.setSelectedMaxDate(toDate);
		}
		synAlert.clear();
		view.setProjectedExpirationDateVisible(false);
		if (actAccessRequirementIdString != null) {
			actAccessRequirementId = Long.parseLong(actAccessRequirementIdString);
			dataAccessClient.getAccessRequirement(actAccessRequirementId, new AsyncCallback<AccessRequirement>() {
				@Override
				public void onFailure(Throwable caught) {
					synAlert.showError(INVALID_AR_ID);
				}

				@Override
				public void onSuccess(AccessRequirement requirement) {
					if (requirement instanceof ManagedACTAccessRequirement) {
						actAccessRequirement = (ManagedACTAccessRequirement) requirement;
						refreshProjectedExpiration();
						if (actAccessRequirement.getDucTemplateFileHandleId() != null) {
							FileHandleAssociation fha = new FileHandleAssociation();
							fha.setAssociateObjectType(FileHandleAssociateType.AccessRequirementAttachment);
							fha.setAssociateObjectId(actAccessRequirement.getId().toString());
							fha.setFileHandleId(actAccessRequirement.getDucTemplateFileHandleId());
							ducTemplateFileHandleWidget.configure(fha);
						}
						view.setAreOtherAttachmentsRequired(actAccessRequirement.getAreOtherAttachmentsRequired());
						if (actAccessRequirement.getExpirationPeriod() != null) {
							view.setExpirationPeriod(actAccessRequirement.getExpirationPeriod() / DAY_IN_MS);
						}

						view.setIsCertifiedUserRequired(actAccessRequirement.getIsCertifiedUserRequired());
						view.setIsDUCRequired(actAccessRequirement.getIsDUCRequired());
						view.setIsIDUPublic(actAccessRequirement.getIsIDUPublic());
						view.setIsIRBApprovalRequired(actAccessRequirement.getIsIRBApprovalRequired());
						view.setIsValidatedProfileRequired(actAccessRequirement.getIsValidatedProfileRequired());

						actAccessRequirementWidget.setRequirement(actAccessRequirement, refreshCallback);
						subjectsWidget.configure(actAccessRequirement.getSubjectIds());

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

	public void refreshProjectedExpiration() {
		Long expirationPeriod = actAccessRequirement.getExpirationPeriod();
		if (expirationPeriod != null && expirationPeriod > 0) {
			Date expirationDate = new Date(new Date().getTime() + expirationPeriod);
			view.setProjectedExpirationDate(dateFormat.format(expirationDate));
			view.setProjectedExpirationDateVisible(true);
		}

	}

	public void loadData() {
		loadMoreContainer.clear();
		nextPageToken = null;
		loadMore();
	}

	public void loadMore() {
		loadMoreContainer.setIsProcessing(true);
		synAlert.clear();
		// ask for data access submissions once call is available, and create a widget to render.
		dataAccessClient.getDataAccessSubmissions(actAccessRequirementId, nextPageToken, stateFilter, SubmissionOrder.CREATED_ON, isSortedAsc, new AsyncCallback<SubmissionPage>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				loadMoreContainer.setIsMore(false);
				loadMoreContainer.setIsProcessing(false);
			}

			public void onSuccess(SubmissionPage submissionPage) {
				nextPageToken = submissionPage.getNextPageToken();
				for (Submission submission : submissionPage.getResults()) {
					// create a new row for each data access submission.
					ACTDataAccessSubmissionWidget w = ginInjector.getACTDataAccessSubmissionWidget();
					w.configure(submission);
					w.setDucColumnVisible(actAccessRequirement.getIsDUCRequired());
					w.setIrbColumnVisible(actAccessRequirement.getIsIRBApprovalRequired());
					w.setOtherAttachmentsColumnVisible(actAccessRequirement.getAreOtherAttachmentsRequired());
					loadMoreContainer.add(w.asWidget());
				}
				loadMoreContainer.setIsMore(nextPageToken != null);
				loadMoreContainer.setIsProcessing(false);
			};
		});
	}

	@Override
	public void onClearDateFilter() {
		fromDate = null;
		view.setSelectedMinDate(fromDate);
		place.removeParam(MIN_DATE_PARAM);
		toDate = null;
		view.setSelectedMaxDate(toDate);
		place.removeParam(MAX_DATE_PARAM);
		loadData();
	}

	@Override
	public void onClearStateFilter() {
		stateFilter = null;
		place.removeParam(STATE_FILTER_PARAM);
		view.setSelectedStateText("");
		loadData();
	}

	@Override
	public void onMaxDateSelected(Date date) {
		toDate = date;
		place.putParam(MAX_DATE_PARAM, Long.toString(date.getTime()));
		loadData();
	}

	@Override
	public void onMinDateSelected(Date date) {
		fromDate = date;
		place.putParam(MIN_DATE_PARAM, Long.toString(date.getTime()));
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
		stateFilter = SubmissionState.valueOf(selectedState.toUpperCase());
		place.putParam(STATE_FILTER_PARAM, selectedState);
		view.setSelectedStateText(selectedState);
		loadData();
	}

	@Override
	public void onCreatedOnClick() {
		isSortedAsc = !isSortedAsc;
		loadData();
	}
}
