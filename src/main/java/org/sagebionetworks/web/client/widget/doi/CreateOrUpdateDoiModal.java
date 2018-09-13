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
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateOrUpdateDoiModal implements CreateOrUpdateDoiModalView.Presenter {
	public static final String DOI_CREATION_PROGRESS_MESSAGE = "Registering DOI";
	private CreateOrUpdateDoiModalView view;
	private JobTrackingWidget jobTrackingWidget;
	private SynapseJavascriptClient javascriptClient;
	private EntityUpdatedHandler entityUpdatedHandler;
	private Doi doi;
	private SynapseAlert synapseAlert;

	@Inject
	public CreateOrUpdateDoiModal(CreateOrUpdateDoiModalView view,
								  JobTrackingWidget jobTrackingWidget,
								  SynapseJavascriptClient javascriptClient,
								  SynapseAlert synapseAlert) {
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.javascriptClient = javascriptClient;
		this.synapseAlert = synapseAlert;
		view.setSynAlert(synapseAlert);
		view.setJobTrackingWidget(jobTrackingWidget);
		view.setPresenter(this);
		view.setModalTitle("Create or Update a DOI");
	}

	public void configureAndShow(String objectId, ObjectType objectType, Long versionNumber, EntityUpdatedHandler entityUpdatedHandler) {
		view.reset();
		synapseAlert.clear();
		this.entityUpdatedHandler = entityUpdatedHandler;
		doi = new Doi();
		getExistingDoi(objectId, objectType, versionNumber);
		view.show();
	}

	public void getExistingDoi(String objectId, ObjectType objectType, Long objectVersion) {
		javascriptClient.getDoi(objectId, objectType, objectVersion).addCallback(new FutureCallback<Doi>() {
			@Override
			public void onSuccess(@NullableDecl Doi doi) {
				populateForms(doi);
			}

			@Override
			public void onFailure(Throwable t) {
				GWT.log(t.getMessage());
				doi.setObjectId(objectId);
				doi.setObjectType(objectType);
				doi.setObjectVersion(objectVersion);
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
		newDoi.setCreators(parseCreatorsString(view.getAuthors()));
		newDoi.setTitles(parseTitlesString(view.getTitles()));
		DoiResourceType rt = new DoiResourceType();
		rt.setResourceTypeGeneral(DoiResourceTypeGeneral.valueOf(view.getResourceTypeGeneral()));
		newDoi.setResourceType(rt);
		newDoi.setPublicationYear(view.getPublicationYear());

		DoiRequest request = new DoiRequest();
		request.setDoi(newDoi);
		GWT.log("Making request to create DOI with request: " + request.toString());
		jobTrackingWidget.startAndTrackJob(DOI_CREATION_PROGRESS_MESSAGE, false, AsynchType.Doi, request, new AsynchronousProgressHandler() {
			@Override
			public void onComplete(AsynchronousResponseBody response) {
				DisplayUtils.showInfo("Doi created: ", response.toString());
				GWT.log("Doi created: " + response.toString());
				entityUpdatedHandler.onPersistSuccess(new EntityUpdatedEvent());
				view.setIsLoading(false);
				view.hide();
			}

			@Override
			public void onFailure(Throwable caught) {
				synapseAlert.handleException(caught);
				GWT.log("Doi creation failed: " + caught.getMessage());
				view.setIsLoading(false);
				view.reset(); //?
			}

			@Override
			public void onCancel() {
				GWT.log("JOB canceled");
				view.setIsLoading(false);
				view.reset(); //?
			}
		});
	}

	void populateForms(Doi doi) {
		this.doi = doi;

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
			doi.setPublicationYear(2018L); // TODO: get current year
		}

		view.setAuthors(convertMultipleCreatorsToString(doi.getCreators()));
		view.setTitles(convertMultipleTitlesToString(doi.getTitles()));
		view.setResourceTypeGeneral(doi.getResourceType().getResourceTypeGeneral().name());
		view.setPublicationYear(doi.getPublicationYear());
	}

	static List<DoiCreator> parseCreatorsString(String creators) {
		List<DoiCreator> doiCreators = new ArrayList<>();
		for (String creatorName : creators.split("\\n")) {
			DoiCreator creator = new DoiCreator();
			creator.setCreatorName(creatorName);
			doiCreators.add(creator);
		}
		return doiCreators;
	}

	static String convertMultipleCreatorsToString(List<DoiCreator> creators) {
		return creators.stream()
				.map(DoiCreator::getCreatorName)
				.reduce((x,y) -> x + "\n" + y) //Separate creator names with new line
				.get();
	}

	static List<DoiTitle> parseTitlesString(String titles) {
		List<DoiTitle> doiTitles = new ArrayList<>();
		for (String titleText : titles.split("\\n")) {
			DoiTitle title = new DoiTitle();
			title.setTitle(titleText);
			doiTitles.add(title);
		}
		return doiTitles;
	}

	static String convertMultipleTitlesToString(List<DoiTitle> titles) {
		return titles.stream()
				.map(DoiTitle::getTitle)
				.reduce((x,y) -> x + "\n" + y) // Separate titles with new line
				.get();
	}
}
