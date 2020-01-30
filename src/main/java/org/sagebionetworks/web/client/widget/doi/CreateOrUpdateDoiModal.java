package org.sagebionetworks.web.client.widget.doi;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceType;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateOrUpdateDoiModal implements CreateOrUpdateDoiModalView.Presenter {
	public static final String DOI_CREATED_MESSAGE = "DOI successfully updated for ";
	public static final String DOI_MODAL_TITLE = "Create or Update a DOI";

	private CreateOrUpdateDoiModalView view;
	private JobTrackingWidget jobTrackingWidget;
	private SynapseJavascriptClient javascriptClient;
	private EventBus eventBus;
	private Doi doi;
	private SynapseAlert synapseAlert;
	private PopupUtilsView popupUtilsView;
	private DateTimeUtils dateTimeUtils;

	@Inject
	public CreateOrUpdateDoiModal(CreateOrUpdateDoiModalView view, JobTrackingWidget jobTrackingWidget, SynapseJavascriptClient javascriptClient, SynapseAlert synapseAlert, PopupUtilsView popupUtilsView, EventBus eventBus, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.javascriptClient = javascriptClient;
		this.synapseAlert = synapseAlert;
		this.popupUtilsView = popupUtilsView;
		this.eventBus = eventBus;
		this.dateTimeUtils = dateTimeUtils;
		view.setSynAlert(synapseAlert);
		view.setJobTrackingWidget(jobTrackingWidget);
		view.setPresenter(this);
		view.setModalTitle(DOI_MODAL_TITLE);
	}

	public void configureAndShow(Entity entity, Long entityVersion, UserProfile userProfile) {
		view.reset();
		synapseAlert.clear();
		doi = new Doi();
		javascriptClient.getDoi(entity.getId(), ObjectType.ENTITY, entityVersion).addCallback(new FutureCallback<Doi>() {
			@Override
			public void onSuccess(@NullableDecl Doi doi) {
				boolean doiExists = true;
				setDoi(doi);
				populateAndShowView(doiExists);
			}

			@Override
			public void onFailure(Throwable t) {
				boolean doiExists = false;
				if (t instanceof NotFoundException) {
					setDoi(createNewDoi(entity, entityVersion, userProfile));
					populateAndShowView(doiExists);
				} else {
					popupUtilsView.showErrorMessage(t.getMessage());
				}
			}
		}, directExecutor());
	}

	private void populateAndShowView(boolean doiExists) {
		populateForms();
		view.showOverwriteWarning(doiExists);
		view.show();
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Creates a 'skeleton' DOI object that a user is given as a template to mint a DOI, where the
	 * populated values are 'best guesses' for what the user may want to enter.
	 */
	public Doi createNewDoi(Entity entity, Long entityVersion, UserProfile userProfile) {
		Doi newDoi = new Doi();
		newDoi.setObjectId(entity.getId());
		newDoi.setObjectType(ObjectType.ENTITY);
		newDoi.setObjectVersion(entityVersion);

		List<DoiCreator> creators = new ArrayList<>();
		DoiCreator creator = new DoiCreator();
		creator.setCreatorName(getFormattedCreatorName(userProfile));
		creators.add(creator);
		newDoi.setCreators(creators);

		List<DoiTitle> titles = new ArrayList<>();
		DoiTitle title = new DoiTitle();
		title.setTitle(entity.getName());
		titles.add(title);
		newDoi.setTitles(titles);

		newDoi.setResourceType(new DoiResourceType());
		newDoi.getResourceType().setResourceTypeGeneral(getSuggestedResourceTypeGeneral(EntityTypeUtils.getEntityTypeForEntityClassName(entity.getClass().getName())));

		newDoi.setPublicationYear(Long.valueOf(dateTimeUtils.getYear(new Date())));
		return newDoi;
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Retrieves a user's name in "Last, First" format. If the user has not set a first and last name,
	 * returns an empty string.
	 */
	public static String getFormattedCreatorName(UserProfile userProfile) {
		if (userProfile != null && userProfile.getLastName() != null && userProfile.getFirstName() != null && !userProfile.getLastName().isEmpty() && !userProfile.getFirstName().isEmpty()) {
			return userProfile.getLastName() + ", " + userProfile.getFirstName();
		} else {
			return "";
		}
	}

	public Widget asWidget() {
		return view.asWidget();
	}

	public void hide() {
		view.hide();
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Gets the most likely DoiResourceTypeGeneral based on the entity type.
	 */
	public static DoiResourceTypeGeneral getSuggestedResourceTypeGeneral(EntityType type) {
		if (type.equals(EntityType.project) || type.equals(EntityType.folder)) {
			return DoiResourceTypeGeneral.Collection;
		} else {
			return DoiResourceTypeGeneral.Dataset;
		}
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
				eventBus.fireEvent(new EntityUpdatedEvent());
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
	 * Retrieves DOI fields from an instance variable (if it exists) and loads them into the view.
	 */
	public void populateForms() {
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
			doi.setPublicationYear(Long.valueOf(dateTimeUtils.getYear(new Date())));
		}

		view.setCreators(convertMultipleCreatorsToString(doi.getCreators()));
		view.setTitles(convertMultipleTitlesToString(doi.getTitles()));
		view.setResourceTypeGeneral(doi.getResourceType().getResourceTypeGeneral().name());
		view.setPublicationYear(doi.getPublicationYear());
	}

	/**
	 * Do not use!!! Public only for testing purposes
	 *
	 * Converts a string of creator names to a List of DoiCreator, where creatorNames are separated with
	 * newlines
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
		return creators.stream().map(DoiCreator::getCreatorName).reduce((x, y) -> x + "\n" + y) // Separate creator names with new line
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
		return titles.stream().map(DoiTitle::getTitle).reduce((x, y) -> x + "\n" + y) // Separate titles with new line
				.orElse("");
	}
}
