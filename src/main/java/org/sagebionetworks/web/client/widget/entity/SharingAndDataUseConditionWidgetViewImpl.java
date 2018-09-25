package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Br;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.entity.restriction.v2.RestrictionWidget;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SharingAndDataUseConditionWidgetViewImpl extends FlowPanel implements SharingAndDataUseConditionWidgetView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	IconsImageBundle iconsImageBundle;
	FlowPanel container;
	PublicPrivateBadge publicPrivateBadge;
	RestrictionWidget restrictionWidgetV2;
	AccessControlListModalWidget accessControlListModalWidget;
	CookieProvider cookies;
	
	@Inject
	public SharingAndDataUseConditionWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			IconsImageBundle iconsImageBundle, 
			PublicPrivateBadge publicPrivateBadge, 
			AccessControlListModalWidget accessControlListModalWidget,
			RestrictionWidget restrictionWidgetV2,
			CookieProvider cookies) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.iconsImageBundle = iconsImageBundle;
		this.publicPrivateBadge = publicPrivateBadge;
		this.restrictionWidgetV2 = restrictionWidgetV2;
		restrictionWidgetV2.showFolderRestrictionUI();
		this.cookies = cookies;
		restrictionWidgetV2.setShowIfProject(true);
		restrictionWidgetV2.setShowFlagLink(false);
		this.accessControlListModalWidget = accessControlListModalWidget;
		this.addStyleName("sharingAndDataUseConditions");
		container = new FlowPanel();
		container.addStyleName("margin-top-left-10");
		this.add(container);
	}
	
	@Override
	public void configure(EntityBundle bundle, boolean showChangeLink) {
		container.clear();
		
		HelpWidget helpWidget = new HelpWidget();
		helpWidget.setHelpMarkdown("##### Sharing Settings: Controls who can view the content.\nBy default, folders and files inherit  the Sharing Settings of the parent folder or project.");
		helpWidget.setHref(WebConstants.DOCS_URL + "access_controls.html#sharing-setting");
		container.add(helpWidget.asWidget());
		
		//add share settings
		container.add(new InlineHTML("<h5 class=\"inline-block\">"+ DisplayConstants.SHARING_PUBLIC_TITLE +"</h5>"));
		final SimplePanel sharingDescriptionContainer = new SimplePanel();
		publicPrivateBadge.configure(bundle.getEntity(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isPublic) {
				//add the proper description into the container
				String description = isPublic ? DisplayConstants.SHARING_PUBLIC_DESCRIPTION : DisplayConstants.SHARING_PRIVATE_DESCRIPTION;
				sharingDescriptionContainer.add(new HTML("<p class=\"margin-left-20 nobottommargin\">"+description+"</p>"));
			}
			@Override
			public void onFailure(Throwable caught) {
				showErrorMessage(caught.getMessage());
			}
		});
		
		Widget publicPrivateBadgeWidget = publicPrivateBadge.asWidget();
		publicPrivateBadgeWidget.addStyleName("inline-block margin-left-10 moveup-2");
		container.add(publicPrivateBadgeWidget);
		
		if (showChangeLink && bundle.getPermissions().getCanChangePermissions()) {
			FlowPanel changeLinkContainer = new FlowPanel();
			changeLinkContainer.addStyleName("inline-block margin-left-5 moveup-2");
			changeLinkContainer.add(new InlineHTML("("));
			Anchor change = new Anchor();
			change.setText(DisplayConstants.CHANGE);
			change.addStyleName("link");
			accessControlListModalWidget.configure(bundle.getEntity(), true);
			change.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					accessControlListModalWidget.showSharing(new Callback() {
						@Override
						public void invoke() {
							presenter.entityUpdated();
						}
					});
				}
			});
			changeLinkContainer.add(change);
			changeLinkContainer.add(new InlineHTML(")"));
			container.add(changeLinkContainer);
		}
		container.add(sharingDescriptionContainer);
		container.add(new Br());
		
		helpWidget = new HelpWidget();
		helpWidget.setHelpMarkdown("##### Conditions For Use: Controls how the data can be used.\nBy default, folders and files inherit the Conditions For Use of the parent folder.");
		helpWidget.setHref(WebConstants.DOCS_URL + "access_controls.html#conditions-for-use");
		container.add(helpWidget.asWidget());

		container.add(new InlineHTML("<h5 class=\"inline-block\">"+ DisplayConstants.DATA_USE +"</h5>"));
		restrictionWidgetV2.setShowChangeLink(showChangeLink);
		restrictionWidgetV2.configure(bundle.getEntity(), bundle.getPermissions().getCanChangePermissions());
		container.add(restrictionWidgetV2);
	}
	
	@Override
	public void showLoading() {
		container.clear();
		container.add(DisplayUtils.getSmallLoadingWidget());
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}


	/*
	 * Private Methods
	 */

}
