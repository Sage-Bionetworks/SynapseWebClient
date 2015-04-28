package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.security.AuthenticationController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DoiWidgetViewImpl extends Composite implements DoiWidgetView {
	
	private Presenter presenter;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	
	@UiField
	FlowPanel container;
	
	@UiField	
	Anchor createDoiLink;
	
	@UiField
	Alert errorCreatingDoi;
			
	@UiField
	SimplePanel doiProcessing;
	
	@UiField
	HTMLPanel doiHTML;
	
	Widget widget;
	
	public interface Binder extends UiBinder<Widget, DoiWidgetViewImpl> {}
	
	@Inject
	public DoiWidgetViewImpl(GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			Binder uiBinder) {
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		widget = uiBinder.createAndBindUi(this);
	}
	
	private void hideAllChildren() {
		GWT.debugger();
		errorCreatingDoi.setVisible(false);
		createDoiLink.setVisible(false);
		doiProcessing.setVisible(false);
		doiHTML.getElement().setInnerHTML("");
	}
	
	@Override
	public void showCreateDoi() {
		GWT.debugger();
		hideAllChildren();
	    createDoiLink.setVisible(true);
	} 

	@Override
	public void showDoi(final DoiStatus doi) {
		GWT.debugger();
		hideAllChildren();
		if (doi == DoiStatus.ERROR) {
			//show error UI
			createDoiLink.setVisible(true);
			errorCreatingDoi.setVisible(true);
		} else if (doi == DoiStatus.IN_PROCESS) {
			//show in process UI
			GWT.debugger();
			doiProcessing.setVisible(true);
		} else if (doi == DoiStatus.CREATED || doi == DoiStatus.READY) {
			//ask for the doi prefix from the presenter, and show a link to that!
			//first clear old handler, if there is one
			doiHTML.setVisible(true);
			final SynapseView view = this;
			presenter.getDoiPrefix(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String prefix) {
					doiHTML.getElement().setInnerHTML(presenter.getDoiHtml(prefix, doi == DoiStatus.READY));
				}
				
				@Override
				public void onFailure(Throwable caught) {
					if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
						showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
//	private void addWidgetToContainer(Widget widget) {
//		//we are showing something in the DOI area
//		Label title = new Label(DisplayConstants.DOI + ":");
//		title.addStyleName("inline-block boldText");
//		container.add(title);
//		
//		DisplayUtils.surroundWidgetWithParens(container, widget);
//		
//		// Why add an empty HTML?
//		container.add(new HTML());
//	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
		// Shouldn't go here, but where?
		createDoiLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.createDoi();
			}
		});
	}

	@Override
	public void showLoading() {
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void clear() {
		hideAllChildren();
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
