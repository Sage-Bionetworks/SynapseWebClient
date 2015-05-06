package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;

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
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	FlowPanel container;
	PublicPrivateBadge publicPrivateBadge;
	RestrictionWidget restrictionWidget;
	AccessControlListModalWidget accessControlListModalWidget;
	
	@Inject
	public SharingAndDataUseConditionWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, 
			IconsImageBundle iconsImageBundle, 
			PublicPrivateBadge publicPrivateBadge, 
			RestrictionWidget restrictionWidget,
			AccessControlListModalWidget accessControlListModalWidget) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.publicPrivateBadge = publicPrivateBadge;
		this.restrictionWidget = restrictionWidget;
		this.accessControlListModalWidget = accessControlListModalWidget;
		container = new FlowPanel();
		container.addStyleName("margin-top-left-10");
		this.add(container);
	}
	
	@Override
	public void configure(EntityBundle bundle, boolean showChangeLink) {
		container.clear();
		
		//add share settings
		container.add(new InlineHTML("<h5 class=\"inline-block\">"+ DisplayConstants.SHARING_PUBLIC_TITLE +"</h5>"));
		final SimplePanel sharingDescriptionContainer = new SimplePanel();
		publicPrivateBadge.configure(bundle.getEntity(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isPublic) {
				//add the proper description into the container
				String description = isPublic ? DisplayConstants.SHARING_PUBLIC_DESCRIPTION : DisplayConstants.SHARING_PRIVATE_DESCRIPTION;
				sharingDescriptionContainer.add(new HTML("<p class=\"margin-left-10 nobottommargin\">"+description+"</p>"));
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
		
		container.add(new InlineHTML("<br><h5 class=\"inline-block\">"+ DisplayConstants.DATA_USE +"</h5>"));
		restrictionWidget.configure(bundle, showChangeLink, true, false, new Callback() {
			@Override
			public void invoke() {
				presenter.entityUpdated();
			}
		});
		
		Widget widget = restrictionWidget.asWidget();
		if (widget != null) {
			widget.addStyleName("margin-top-left-10");
			container.add(widget);
			//and add description
			RESTRICTION_LEVEL level = restrictionWidget.getRestrictionLevel();
			String description = RESTRICTION_LEVEL.OPEN.equals(level) ? DisplayConstants.DATA_USE_UNRESTRICTED_DATA_DESCRIPTION : DisplayConstants.DATA_USE_RESTRICTED_DESCRIPTION;
			container.add(new HTML("<p class=\"margin-left-10 margin-bottom-20\">"+description+"</p>"));
		}
					
	}
	
	@Override
	public void showLoading() {
		container.clear();
		container.add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
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
