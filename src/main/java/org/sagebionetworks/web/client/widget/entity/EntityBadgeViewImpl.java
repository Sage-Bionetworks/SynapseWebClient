package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

public class EntityBadgeViewImpl extends HorizontalPanel implements EntityBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	SageImageBundle sageImageBundle;
	PortalGinInjector ginInjector;
	PopupPanel popupPanel;
	
	@Inject
	public EntityBadgeViewImpl(SynapseJSNIUtils synapseJSNIUtils,
			GlobalApplicationState globalApplicationState,
			SageImageBundle sageImageBundle, 
			PortalGinInjector ginInjector) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.sageImageBundle = sageImageBundle;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void setEntity(final EntityHeader entityHeader) {
		clear();
		popupPanel = null;
		if(entityHeader == null)  throw new IllegalArgumentException("Entity is required");
		
		if(entityHeader != null) {
			final Anchor anchor = new Anchor();
			anchor.setText(entityHeader.getName());
			anchor.addStyleName("link");
			anchor.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					showEntityInformation(anchor, entityHeader.getId(), entityHeader.getName());
				}
			});
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (popupPanel != null) {
						popupPanel.hide();
					}
					globalApplicationState.getPlaceChanger().goTo(new Synapse(entityHeader.getId()));
				}
			});
			ClickHandler clickHandler = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					anchor.fireEvent(event);
				}
			};
			
			ImageResource icon = presenter.getIconForType(entityHeader.getType());
			Image iconPicture = new Image(icon);
			iconPicture.setWidth("16px");
			iconPicture.setHeight("16px");
			iconPicture.addStyleName("imageButton displayInline");
			iconPicture.addClickHandler(clickHandler);
			add(iconPicture);
			setCellWidth(iconPicture, "20px");
			add(anchor);
		} 		
		
	}
	
	public void showEntityInformation(Anchor anchor, final String entityId, final String entityName) {
		if (popupPanel != null) {
			popupPanel.show();
		} else {
			popupPanel = DisplayUtils.addToolTip(anchor, DisplayUtils.getLoadingHtml(sageImageBundle));
			popupPanel.setWidth("350px");
			presenter.getInfo(entityId, new AsyncCallback<Project>() {						
				@Override
				public void onSuccess(Project result) {
					renderPopover(result);
				}
				
				@Override
				public void onFailure(Throwable caught) {
					renderPopover(null);						
				}
				
				private void renderPopover(Project entity) {
					//build up content
					FlowPanel content = new FlowPanel();
					if (entity == null) {
						content.add(new HTML(DisplayConstants.DETAILS_UNAVAILABLE));
					} else {
						content.add(EntityPageTopViewImpl.createModifiedAndCreatedWidget(entity, ginInjector, false)); 
					}
					popupPanel.setWidget(content);
					popupPanel.show();
				}
			});
		}
		
	}

	@Override
	public void showLoadError(String principalId) {
		clear();
		add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	@Override
	public void showLoading() {
		clear();
		add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
	}

	@Override
	public void showInfo(String title, String message) {
	}

	@Override
	public void showErrorMessage(String message) {
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;		
	}
	
	/*
	 * Private Methods
	 */

}
