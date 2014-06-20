package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.team.WizardProgressWidget;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityAccessRequirementsWidgetViewImpl implements EntityAccessRequirementsWidgetView {
	
	private EntityAccessRequirementsWidgetView.Presenter presenter;
	
	private Window wizard;
	private Button okButton;
	private FlowPanel currentWizardContent;
	private Callback okButtonCallback;
	private WizardProgressWidget progressWidget;
	
	@Inject
	public EntityAccessRequirementsWidgetViewImpl(WizardProgressWidget progressWidget) {
		this.progressWidget = progressWidget;
	}
	
	@Override
	public void showLoading() {
		clear();
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);

	}

	@Override
	public void showErrorMessage(String message) {
		clear();
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void hideWizard() {
		if (wizard != null && wizard.isVisible())
			wizard.hide();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
			@Override
	public void showWizard() {
		wizard = new Window();
		wizard.addStyleName("whiteBackground");
		wizard.setHeaderVisible(false);
       	wizard.setClosable(false);
		wizard.setMaximizable(false);
        wizard.setSize(640, 480);
        wizard.setPlain(true); 
        wizard.setModal(true); 
        wizard.setAutoHeight(true);
        wizard.setResizable(false);
        FlowPanel buttonPanel = new FlowPanel();
		buttonPanel.addStyleName("bottomright");
		Button cancelButton = DisplayUtils.createButton(DisplayConstants.BUTTON_CANCEL);
		cancelButton.addStyleName("right margin-bottom-10 margin-right-10");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				wizard.hide();
				presenter.wizardCanceled();
			}
		});
			
		okButton = DisplayUtils.createButton("Continue", ButtonType.PRIMARY);
		okButton.addStyleName("right margin-bottom-10 margin-right-10");
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				okButtonCallback.invoke();
			}
		});
		currentWizardContent = new FlowPanel();
		currentWizardContent.addStyleName("whiteBackground padding-bottom-15");
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		Widget progress = progressWidget.asWidget();
		progress.addStyleName("margin-top-5");
		wizard.add(progress);
		wizard.add(currentWizardContent);
        wizard.add(buttonPanel);
		wizard.show();	
		DisplayUtils.center(wizard);
	}
	
	@Override
	public void clear() {
		hideWizard();
	}
	@Override
	public Widget asWidget() {
		return wizard;
	}

	@Override
	public void showAccessRequirement(
			String arText,
			final Callback touAcceptanceCallback) {
		DisplayUtils.relabelIconButton(okButton, DisplayConstants.ACCEPT, null);
		currentWizardContent.clear();
		HTML arTextHTML = new HTML(arText);
		arTextHTML.addStyleName("margin-10");
        ScrollPanel panel = new ScrollPanel(arTextHTML);
		panel.addStyleName("whiteBackground padding-5 margin-bottom-60");
        currentWizardContent.add(panel);
        wizard.layout(true);
        okButtonCallback = touAcceptanceCallback;
	}
	
	@Override
	public void updateWizardProgress(int currentPage, int totalPages) {
		progressWidget.configure(currentPage, totalPages);
	}
	
	
}
