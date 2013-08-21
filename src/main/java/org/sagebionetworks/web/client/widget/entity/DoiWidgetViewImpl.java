package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class DoiWidgetViewImpl extends Composite implements DoiWidgetView {
	
	private Presenter presenter;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	
	FlowPanel container;
	
	@Inject
	public DoiWidgetViewImpl(GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;

		container = new FlowPanel();
		initWidget(container);
	}

	@Override
	public void showDoi(final DoiStatus doi) {
		container.clear();
		
		if (doi == DoiStatus.ERROR) {
			//show error UI
			container.add(new HTMLPanel(DisplayUtils.getWarningHtml("Error creating DOI", "")));
		} else if (doi == DoiStatus.IN_PROCESS) {
			//show in process UI
			container.add(new HTMLPanel("<span class=\"margin-left-5\">DOI processing...</span>"));
		} else if (doi == DoiStatus.CREATED || doi == DoiStatus.READY) {
			//ask for the doi prefix from the presenter, and show a link to that!
			//first clear old handler, if there is one
			final SynapseView view = this;
			presenter.getDoiPrefix(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String prefix) {
					container.add(new HTMLPanel(presenter.getDoiHtml(prefix, doi == DoiStatus.READY)));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
						showErrorMessage(caught.getMessage());
				}
			});
			
		}
			
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void clear() {
		container.clear();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	

}
