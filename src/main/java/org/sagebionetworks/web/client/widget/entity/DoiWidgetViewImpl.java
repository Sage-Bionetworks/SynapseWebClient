package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.DisplayUtils;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class DoiWidgetViewImpl extends Composite implements DoiWidgetView {
	
	private Presenter presenter;
	
	FlowPanel container;
	Button createDoiButton;
	@Inject
	public DoiWidgetViewImpl() {
		container = new FlowPanel();
		initWidget(container);
		createDoiButton = new Button("Create DOI");
		createDoiButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				presenter.createDoi();
			}
		});
	}

	@Override
	public void showCreateDoi() {
		container.clear();
		container.add(createDoiButton);
	}
	
	@Override
	public void showDoi(DoiStatus doi) {
		container.clear();
		
		if (doi == DoiStatus.ERROR) {
			//show error UI
			container.add(new HTMLPanel(DisplayUtils.getWarningHtml("Error creating DOI", "")));
			container.add(createDoiButton);
		} else if (doi == DoiStatus.IN_PROCESS) {
			//show in process UI
			container.add(new HTMLPanel("<span class=\"margin-left-5\">DOI processing...</span>"));
		} else if (doi == DoiStatus.READY) {
			//ask for the doi prefix from the presenter, and show a link to that!
			//first clear old handler, if there is one
			presenter.getDoiPrefix(new AsyncCallback<String>() {
				@Override
				public void onSuccess(String prefix) {
					container.add(new HTMLPanel(presenter.getDoiLink(prefix)));
				}
				
				@Override
				public void onFailure(Throwable caught) {
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
