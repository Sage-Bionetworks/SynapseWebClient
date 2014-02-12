package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.inject.Inject;

public class SharingAndDataUseConditionWidgetViewImpl extends LayoutContainer implements SharingAndDataUseConditionWidgetView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	IconsImageBundle iconsImageBundle;
	FlowPanel container;
	PublicPrivateBadge publicPrivateBadge;
	AccessControlListEditor aclEditor;
	RestrictionWidget restrictionWidget;
	
	@Inject
	public SharingAndDataUseConditionWidgetViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, 
			IconsImageBundle iconsImageBundle, 
			PublicPrivateBadge publicPrivateBadge, 
			AccessControlListEditor aclEditor,
			RestrictionWidget restrictionWidget) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.publicPrivateBadge = publicPrivateBadge;
		this.aclEditor = aclEditor;
		this.restrictionWidget = restrictionWidget;
		container = new FlowPanel();
		this.add(container);
	}
	
	@Override
	public void configure(EntityBundle bundle, boolean showChangeLink) {
		container.clear();
		
		//add share settings
		container.add(new InlineHTML("<h3>"+ DisplayConstants.SHARING_PUBLIC_DESCRIPTION +"</h3>"));
		publicPrivateBadge.configure(bundle.getEntity());
		container.add(publicPrivateBadge.asWidget());
		
		if (showChangeLink) {
			container.add(new InlineHTML("("));
			Anchor change = new Anchor();
			change.setText(DisplayConstants.CHANGE);
			change.addStyleName("link");
			aclEditor.setResource(bundle.getEntity());
			change.addClickHandler(new ClickHandler(){
				@Override
				public void onClick(ClickEvent event) {
					DisplayUtils.showSharingDialog(aclEditor, new Callback() {
						@Override
						public void invoke() {
							presenter.entityUpdated();
						}
					});
				}
			});
			container.add(change);
			container.add(new InlineHTML(")"));
		}
		
		container.add(new InlineHTML("<h3>"+ DisplayConstants.DATA_USE_TITLE +"</h3>"));
		restrictionWidget.configure(bundle, showChangeLink, new com.google.gwt.core.client.Callback<Void, Throwable>() {
			@Override
			public void onSuccess(Void result) {
				presenter.entityUpdated();
			}
			
			@Override
			public void onFailure(Throwable reason) {
				showErrorMessage(reason.getMessage());
			}
		});
		container.add(restrictionWidget.asWidget());		
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
	public void clear() {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}


	/*
	 * Private Methods
	 */

}
