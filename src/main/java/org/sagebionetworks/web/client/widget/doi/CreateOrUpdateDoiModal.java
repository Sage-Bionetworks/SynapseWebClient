package org.sagebionetworks.web.client.widget.doi;

import java.util.Collections;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.doi.v2.Doi;
import org.sagebionetworks.repo.model.doi.v2.DoiCreator;
import org.sagebionetworks.repo.model.doi.v2.DoiRequest;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceType;
import org.sagebionetworks.repo.model.doi.v2.DoiResourceTypeGeneral;
import org.sagebionetworks.repo.model.doi.v2.DoiResponse;
import org.sagebionetworks.repo.model.doi.v2.DoiTitle;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateOrUpdateDoiModal implements CreateOrUpdateDoiModalView.Presenter {
	private CreateOrUpdateDoiModalView view;
	private JobTrackingWidget jobTrackingWidget;
	private CallbackP<Doi> doiCallback;
	TextBox authors;
	TextBox titles;
	TextBox publicationYear;
	ListBox resourceTypeGeneral;

	@Inject
	public CreateOrUpdateDoiModal(CreateOrUpdateDoiModalView view,
								  JobTrackingWidget jobTrackingWidget,
								  TextBox authors,
								  TextBox titles,
								  TextBox publicationYear,
								  ListBox resourceTypeGeneral) {
		this.view = view;
		this.jobTrackingWidget = jobTrackingWidget;
		this.authors = authors;
		this.titles = titles;
		this.publicationYear = publicationYear;
		this.resourceTypeGeneral = resourceTypeGeneral;
		for (DoiResourceTypeGeneral rtg : DoiResourceTypeGeneral.values()) {
			this.resourceTypeGeneral.addItem(rtg.name());
		}
		view.setWidgets(authors, titles, publicationYear, resourceTypeGeneral);
		view.setJobTrackingWidget(jobTrackingWidget);
		view.setPresenter(this);
	}
	
	public void configure(CallbackP<Doi> doi) {
		this.doiCallback = doi;
	}
	
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void show() {
		view.show();
	}
	
	public void hide() {
		view.hide();
	}

	@Override
	public void onCreateDoi() {
		GWT.debugger();
		// This is where the logic to pull data from forms and assemble it will go
		Doi doi = new Doi();

		DoiCreator creator = new DoiCreator();
		creator.setCreatorName(authors.getText());
		doi.setCreators(Collections.singletonList(creator));

		DoiTitle title = new DoiTitle();
		title.setTitle(titles.getText());
		doi.setTitles(Collections.singletonList(title));

		DoiResourceType rt = new DoiResourceType();
		rt.setResourceTypeGeneral(DoiResourceTypeGeneral.valueOf(resourceTypeGeneral.getSelectedValue()));
		doi.setResourceType(rt);

		doi.setPublicationYear(Long.valueOf(publicationYear.getText()));

		if (doi != null && doiCallback != null) {
			doiCallback.invoke(doi);
		}	
	}

	public void createOrUpdateDoi(Doi doi, AsyncCallback<Doi> callback){
		DoiRequest request = new DoiRequest();
		request.setDoi(doi);
		jobTrackingWidget.startAndTrackJob("Creating DOI Message Placeholder", false, AsynchType.Doi, request, new AsynchronousProgressHandler() {

			@Override
			public void onComplete(AsynchronousResponseBody response) {
				DoiResponse result = (DoiResponse) response;
				GWT.log("JOB RETRIEVED AS SUCCESS with DOI: " + result.getDoi());
				callback.onSuccess(result.getDoi());
			}

			@Override
			public void onFailure(Throwable failure) {
				GWT.log("JOB failed: " + failure);
				callback.onFailure(failure);
			}

			@Override
			public void onCancel() {
				GWT.log("JOB canceled");
			}
		});
	}
	
	public void setTitle(String title) {
		view.setTitle(title);
	}
}
