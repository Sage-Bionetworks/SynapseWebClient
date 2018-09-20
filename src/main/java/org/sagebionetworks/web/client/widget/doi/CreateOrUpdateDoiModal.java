package org.sagebionetworks.web.client.widget.doi;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceType;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateOrUpdateDoiModal implements CreateOrUpdateDoiModalView.Presenter {
	public static final String DOI_CREATED_MESSAGE = "DOI successfully updated for ";
	public static final String DOI_MODAL_TITLE = "Create or Update a DOI";
	public static final String DOI_SERVICES_UNAVAILABLE_AT_THIS_TIME = "DOI services unavailable at this time.";

	private CreateOrUpdateDoiModalView view;
	private JobTrackingWidget jobTrackingWidget;
	private SynapseJavascriptClient javascriptClient;
	private EntityUpdatedHandler entityUpdatedHandler;
	private Doi doi;
	private SynapseAlert synapseAlert;
	private PopupUtilsView popupUtilsView;

	@Inject
	public CreateOrUpdateDoiModal(CreateOrUpdateDoiModalView view,
								  JobTrackingWidget jobTrackingWidget,
								  SynapseJavascriptClient javascriptClient,
								  SynapseAlert synapseAlert,
								  PopupUtilsView popupUtilsView) {
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.javascriptClient = javascriptClient;
		this.synapseAlert = synapseAlert;
		this.popupUtilsView = popupUtilsView;
		view.setSynAlert(synapseAlert);
		view.setJobTrackingWidget(jobTrackingWidget);
		view.setPresenter(this);
		view.setModalTitle(DOI_MODAL_TITLE);
	}

	public void configureAndShow(String objectId, ObjectType objectType, Long versionNumber, EntityUpdatedHandler entityUpdatedHandler) {
		view.reset();
		synapseAlert.clear();
		this.entityUpdatedHandler = entityUpdatedHandler;
		doi = new Doi();
		getExistingDoi(objectId, objectType, versionNumber);
	}

	public void getExistingDoi(String objectId, ObjectType objectType, Long objectVersion) {
		javascriptClient.getDoi(objectId, objectType, objectVersion).addCallback(new FutureCallback<Doi>() {
			@Override
			public void onSuccess(@NullableDecl Doi doi) {
				populateForms(doi);
				view.show();
			}

			@Override
			public void onFailure(Throwable t) {
				if (t instanceof NotFoundException) {
					doi.setObjectId(objectId);
					doi.setObjectType(objectType);
					doi.setObjectVersion(objectVersion);
					view.show();
				} else {
					popupUtilsView.showErrorMessage(DOI_SERVICES_UNAVAILABLE_AT_THIS_TIME);
				}
			}
		}, directExecutor());
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}

	public void hide() {
		view.hide();
	}

	@Override
	public void onSaveDoi() {
		view.setIsLoading(true);
		Doi newDoi = new Doi();
		newDoi.setObjectId(doi.getObjectId());
		newDoi.setObjectType(doi.getObjectType());
		newDoi.setObjectVersion(doi.getObjectVersion());
		newDoi.setEtag(doi.getEtag());
		newDoi.setCreators(parseCreatorsString(view.getCreators()));
		newDoi.setTitles(parseTitlesString(view.getTitles()));
		DoiResourceType rt = new DoiResourceType();
		rt.setResourceTypeGeneral(DoiResourceTypeGeneral.valueOf(view.getResourceTypeGeneral()));
		newDoi.setResourceType(rt);
		newDoi.setPublicationYear(view.getPublicationYear());

		DoiRequest request = new DoiRequest();
		request.setDoi(newDoi);
		jobTrackingWidget.startAndTrackJob("", false, AsynchType.Doi, request, new AsynchronousProgressHandler() {
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				popupUtilsView.showInfo(DOI_CREATED_MESSAGE + newDoi.getObjectId());
				entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
				view.setIsLoading(false);
				view.hide();
			}

			@Override
			public void onFailure(Throwable caught) {
				synapseAlert.handleException(caught);
				view.setIsLoading(false);
			}

			@Override
			public void onCancel() {
				view.setIsLoading(false);
			}
		});
	}

	public void setDoi(Doi doi) {
		this.doi = doi;
	}

	public Doi getDoi() {
		return this.doi;
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Retrieves DOI fields from a class variable, translates them, and loads them into the view
	 * @param doi
	 */
	public void populateForms(Doi doi) {
		if (doi == null) {
			doi = new Doi();
		}

		if (doi.getCreators() == null) {
			doi.setCreators(new ArrayList<>());
		}

		if (doi.getTitles() == null) {
			doi.setTitles(new ArrayList<>());
		}

		if (doi.getResourceType() == null) {
			doi.setResourceType(new DoiResourceType());
			if (doi.getResourceType().getResourceTypeGeneral() == null) {
				doi.getResourceType().setResourceTypeGeneral(DoiResourceTypeGeneral.Dataset);
			}
		}

		if (doi.getPublicationYear() == null) {
			doi.setPublicationYear(2018L); // TODO: Current date?
		}

		view.setCreators(convertMultipleCreatorsToString(doi.getCreators()));
		view.setTitles(convertMultipleTitlesToString(doi.getTitles()));
		view.setResourceTypeGeneral(doi.getResourceType().getResourceTypeGeneral().name());
		view.setPublicationYear(doi.getPublicationYear());

		setDoi(doi);
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Converts a string of creator names to a List of DoiCreator, where creatorNames are separated with newlines
	 */
	public static List<DoiCreator> parseCreatorsString(String creators) {
		List<DoiCreator> doiCreators = new ArrayList<>();
		if (!creators.isEmpty()) {
			for (String creatorName : creators.split("\\n")) {
				DoiCreator creator = new DoiCreator();
				creator.setCreatorName(creatorName);
				doiCreators.add(creator);
			}
		}
		return doiCreators;
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Converts a List of DoiCreator to a string of creator names, concatenated with new lines
	 */
	public static String convertMultipleCreatorsToString(List<DoiCreator> creators) {
		return creators.stream()
				.map(DoiCreator::getCreatorName)
				.reduce((x,y) -> x + "\n" + y) //Separate creator names with new line
				.orElse("");
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Converts a string of titles to a List of DoiTitle, where titles are separated with newlines
	 */
	public static List<DoiTitle> parseTitlesString(String titles) {
		List<DoiTitle> doiTitles = new ArrayList<>();
		if (!titles.isEmpty()) {
			for (String titleText : titles.split("\\n")) {
				DoiTitle title = new DoiTitle();
				title.setTitle(titleText);
				doiTitles.add(title);
			}
		}
		return doiTitles;
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Converts a List of DoiTitle to a string of titles, concatenated with new lines
	 */
	public static String convertMultipleTitlesToString(List<DoiTitle> titles) {
		return titles.stream()
				.map(DoiTitle::getTitle)
				.reduce((x,y) -> x + "\n" + y) // Separate titles with new line
				.orElse("");
	}
}
