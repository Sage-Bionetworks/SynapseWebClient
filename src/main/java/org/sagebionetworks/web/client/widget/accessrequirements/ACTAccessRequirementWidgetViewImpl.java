package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ACTAccessRequirementWidgetViewImpl implements ACTAccessRequirementWidgetView {

	@UiField
	Div approvedHeading;
	@UiField
	Div unapprovedHeading;
	@UiField
	BlockQuote wikiTermsUI;
	@UiField
	SimplePanel wikiContainer;
	@UiField
	BlockQuote termsUI;
	@UiField
	HTML terms;
	@UiField
	Button requestAccessButton;
	@UiField
	Div editAccessRequirementContainer;
	@UiField
	Div deleteAccessRequirementContainer;
	
	@UiField
	Div subjectsWidgetContainer;
	@UiField
	Div synAlertContainer;
	@UiField
	Div requestAccessButtonContainer;
	@UiField
	Alert requestApprovedMessage;
	@UiField
	Div manageAccessContainer;
	@UiField
	Div convertAccessRequirementContainer;
	@UiField
	Button loginButton;

	Callback onAttachCallback;
	public interface Binder extends UiBinder<Widget, ACTAccessRequirementWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public ACTAccessRequirementWidgetViewImpl(Binder binder, GlobalApplicationState globalAppState){
		this.w = binder.createAndBindUi(this);
		w.addAttachHandler(event -> {
			if (event.isAttached()) {
				onAttachCallback.invoke();
			}
		});
		loginButton.addClickHandler(event -> {
			globalAppState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		});
		requestAccessButton.addClickHandler(event -> {
			presenter.onRequestAccess();
		});
	}
	
	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void setWikiTermsWidget(Widget wikiWidget) {
		wikiContainer.setWidget(wikiWidget);
	}
	@Override
	public void setTerms(String arText) {
		terms.setHTML(arText);
	}
	@Override
	public void showTermsUI() {
		termsUI.setVisible(true);
	}
	@Override
	public void showWikiTermsUI() {
		wikiTermsUI.setVisible(true);
	}
	@Override
	public void showApprovedHeading() {
		approvedHeading.setVisible(true);
	}
	@Override
	public void showUnapprovedHeading() {
		unapprovedHeading.setVisible(true);
	}
	
	@Override
	public void showJiraIssueCollector(String principalId, String userDisplayName, String userEmailAddress,
			String dataObjectId, String accessRequirementId) {
		_showJiraIssueCollector(principalId, userDisplayName, userEmailAddress, dataObjectId, accessRequirementId);
	}
	
	@Override
	public void showRequestAccessButton() {
		requestAccessButton.setVisible(true);
	}
	
	private static native void _showJiraIssueCollector(
			String principalId, String userDisplayName, String userEmailAddress,
			String dataObjectId, String accessRequirementId) /*-{
		try {
			// Requires jQuery!
			$wnd.jQuery.ajax({
			    url: "https://sagebionetworks.jira.com/s/d41d8cd98f00b204e9800998ecf8427e-T/-2rg9hj/b/25/e73395c53c3b10fde2303f4bf74ffbf6/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?locale=en-US&collectorId=bd4dc1e5",
			    type: "get",
			    cache: true,
			    dataType: "script"
			});
			
			$wnd.ATL_JQ_PAGE_PROPS =  {
				"triggerFunction": function(showCollectorDialog) {
					showCollectorDialog();
				},
				"fieldValues": {
			 		summary : 'Request for ACT to grant access to data',
					description : 'By clicking \'Submit\' below, I request that the Synapse Access and Compliance Team contact me with further information on how to access this data.',
			 		priority : '4',
			 		customfield_10841: accessRequirementId,
			 		customfield_10742: dataObjectId,
			 		customfield_10840: userEmailAddress,
			 		email: userEmailAddress,
			 		customfield_10740: principalId,
			 		customfield_10741: userDisplayName,
			 		fullname: userDisplayName
				}};
		} catch (err) {
			console.error(err);
		}
	}-*/;
	
	@Override
	public void resetState() {
		approvedHeading.setVisible(false);
		unapprovedHeading.setVisible(false);
		requestAccessButton.setVisible(false);
		requestApprovedMessage.setVisible(false);
		loginButton.setVisible(false);
	}
	
	@Override
	public void setEditAccessRequirementWidget(IsWidget w) {
		editAccessRequirementContainer.clear();
		editAccessRequirementContainer.add(w);
	}
	@Override
	public void setDeleteAccessRequirementWidget(IsWidget w) {
		deleteAccessRequirementContainer.clear();
		deleteAccessRequirementContainer.add(w);
	}
	@Override
	public void setSubjectsWidget(IsWidget w) {
		subjectsWidgetContainer.clear();
		subjectsWidgetContainer.add(w);
	}
	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}
	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(w);
	}
	@Override
	public boolean isAttached() {
		return w.isAttached();
	}
	
	@Override
	public void hideButtonContainers() {
		editAccessRequirementContainer.setVisible(false);
		deleteAccessRequirementContainer.setVisible(false);
		requestAccessButtonContainer.setVisible(false);
	}
	@Override
	public void showRequestApprovedMessage() {
		requestApprovedMessage.setVisible(true);
	}
	@Override
	public void setManageAccessWidget(IsWidget w) {
		manageAccessContainer.clear();
		manageAccessContainer.add(w);
	}
	@Override
	public void setConvertAccessRequirementWidget(IsWidget w) {
		convertAccessRequirementContainer.clear();
		convertAccessRequirementContainer.add(w);
	}
	@Override
	public void showLoginButton() {
		loginButton.setVisible(true);
	}
}

