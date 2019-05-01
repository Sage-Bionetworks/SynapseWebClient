package org.sagebionetworks.web.client.widget.footer;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ToggleACTActionsButton;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FooterViewImpl implements FooterView {
	
	public interface Binder extends UiBinder<Widget, FooterViewImpl> {
	}
	@UiField
	Alert debugModeAlert;
	@UiField
	Button debugLink;	
	@UiField
	Anchor debugOffLink;
	@UiField
	Anchor copyrightYear;
	@UiField
	Span portalVersionSpan;
	@UiField
	Span repoVersionSpan;
	@UiField
	Anchor reportAbuseLink;
	@UiField
	Anchor reportAbuseLink2;
	@UiField
	Span hideACTActionsContainer;
	String portalVersion, repoVersion;
	private Presenter presenter;
	private CookieProvider cookies;
	private GlobalApplicationState globalAppState;
	private ToggleACTActionsButton hideACTActionsButton;
	Div container = new Div();
	
	@Inject
	public FooterViewImpl(Binder binder, CookieProvider cookies, GlobalApplicationState globalAppState, ToggleACTActionsButton hideACTActionsButton, GWTWrapper gwt) {
		//defer constructing this view (to give a chance for other page components to load first)
		Callback constructViewCallback = () -> {
			IsWidget widget = binder.createAndBindUi(this);
			container.add(widget);
			
			initDebugModeLink();
			hideACTActionsContainer.add(hideACTActionsButton);
			copyrightYear.setText(DateTimeFormat.getFormat("yyyy").format(new Date()) + " SAGE BIONETWORKS");
			reportAbuseLink.addClickHandler(event->{
				presenter.onReportAbuseClicked();
			});
			reportAbuseLink2.addClickHandler(event->{
				presenter.onReportAbuseClicked();
			});
			if (portalVersion != null) {
				portalVersionSpan.setText(portalVersion);
				repoVersionSpan.setText(repoVersion);
			}
			
			refresh();
		};
		gwt.scheduleExecution(constructViewCallback, 2500);
		this.cookies = cookies;
		this.globalAppState = globalAppState;
		this.hideACTActionsButton = hideACTActionsButton;
	}
	
	@Override
	public Widget asWidget() {
		return container;
	}
	
	private void initDebugModeLink() {
		debugLink.addClickHandler(event -> {
			DisplayUtils.confirm(DisplayConstants.TEST_MODE_WARNING, () -> {
				//switch to pre-release test website mode
				DisplayUtils.setTestWebsite(true, cookies);
				Window.scrollTo(0, 0);
				refresh();
				globalAppState.refreshPage();
			});
		});
		debugOffLink.addClickHandler(event -> {
			DisplayUtils.setTestWebsite(false, cookies);
			Window.scrollTo(0, 0);
			refresh();
			globalAppState.refreshPage();
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setVersion(String portalVersion, String repoVersion) {
		if(portalVersion == null) portalVersion = "--";
		if(repoVersion == null) repoVersion = "--";
		this.portalVersion = portalVersion;
		this.repoVersion = repoVersion;
		if (portalVersionSpan != null) {
			portalVersionSpan.setText(portalVersion);
			repoVersionSpan.setText(repoVersion);	
		}
	}
	@Override
	public void open(String url) {
		DisplayUtils.newWindow(url, "_blank", "");
	}
	
	@Override
	public void refresh() {
		hideACTActionsButton.refresh();
		boolean isTestMode = DisplayUtils.isInTestWebsite(cookies);
		if (debugModeAlert != null) {
			debugModeAlert.setVisible(isTestMode);	
		}
		if (debugLink != null) {
			debugLink.setVisible(!isTestMode);	
		}
	}
	

	@Override
	public void showJiraIssueCollector(String principalId, String userDisplayName, String userEmailAddress) {
		_showJiraIssueCollector(principalId, userDisplayName, userEmailAddress, "", Window.Location.getHref());
	}
	
	public static native void _showJiraIssueCollector(
			String principalId, String userDisplayName, String userEmailAddress, String synapseDataObjectId, String url) /*-{
		try {
			// Requires jQuery!
			$wnd.jQuery.ajax({
				url: "https://sagebionetworks.jira.com/s/d41d8cd98f00b204e9800998ecf8427e-T/g39zuk/b/41/e73395c53c3b10fde2303f4bf74ffbf6/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?locale=en-US&collectorId=d0abcfa9",
				type: "get",
				cache: true,
				dataType: "script"
			});
			
			$wnd.ATL_JQ_PAGE_PROPS =  {
				"triggerFunction": function(showCollectorDialog) {
					showCollectorDialog();
				},
				
				"fieldValues": {
			 		summary : '',
					description : 'Reporting this page: ' + url + ' \n\nUser is reporting to the Synapse team that this page is in violation (for example: abusive or harmful content, spam, inappropriate ads).',
					priority : '3',
					customfield_10840: userEmailAddress,
					email: userEmailAddress,
					customfield_10740: principalId,
					customfield_10741: userDisplayName,
					customfield_10742: synapseDataObjectId,
					fullname: userDisplayName
				}};
		} catch (err) {
			console.error(err);
		}
	}-*/;
}
