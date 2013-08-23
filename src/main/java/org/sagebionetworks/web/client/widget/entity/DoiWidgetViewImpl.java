package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.dialog.ANNOTATION_TYPE;
import org.sagebionetworks.web.client.widget.entity.dialog.AddAnnotationDialog;

import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetViewImpl extends Composite implements DoiWidgetView {
	
	private Presenter presenter;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	
	FlowPanel container;
	Anchor createDoiLink;
	
	@Inject
	public DoiWidgetViewImpl(GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;

		container = new FlowPanel();
		initWidget(container);
		createDoiLink = new Anchor("create DOI");
		createDoiLink.addStyleName("link");
		createDoiLink.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				presenter.createDoi();
			}
		});
	}
	
	@Override
	public void showCreateDoi() {
	  container.clear();
	  addWidgetToContainer(createDoiLink);
	} 

	@Override
	public void showDoi(final DoiStatus doi) {
		container.clear();
		
		if (doi == DoiStatus.ERROR) {
			//show error UI
			addWidgetToContainer(createDoiLink);
			container.add(new HTMLPanel(DisplayUtils.getWarningHtml("Error creating DOI", "")));
		} else if (doi == DoiStatus.IN_PROCESS) {
			//show in process UI
			addWidgetToContainer(new Text("DOI processing"));
		} else if (doi == DoiStatus.CREATED || doi == DoiStatus.READY) {
			//ask for the doi prefix from the presenter, and show a link to that!
			//first clear old handler, if there is one
			final SynapseView view = this;
			presenter.getDoiPrefix(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String prefix) {
					addWidgetToContainer(new HTMLPanel(presenter.getDoiHtml(prefix, doi == DoiStatus.READY)));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.isLoggedIn(), view))
						showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	private void addWidgetToContainer(Widget widget) {
		//we are showing something in the DOI area
		Label title = new Label(DisplayConstants.DOI + ":");
		title.addStyleName("inline-block boldText");
		container.add(title);
		
		DisplayUtils.surroundWidgetWithParens(container, widget);
		
		container.add(new HTML());
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
