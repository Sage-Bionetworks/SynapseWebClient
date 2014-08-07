package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Tooltip;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.provenance.ProvViewUtil;
import org.sagebionetworks.web.shared.KeyValueDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadgeViewImpl extends Composite implements EntityBadgeView {
	
	private Presenter presenter;
	SynapseJSNIUtils synapseJSNIUtils;
	SageImageBundle sageImageBundle;
	
	public interface Binder extends UiBinder<Widget, EntityBadgeViewImpl> {	}
	
	@UiField
	Tooltip popover;
	@UiField
	SimplePanel iconContainer;
	@UiField
	FlowPanel entityContainer;
	boolean isPopoverInitialized;
	boolean isMouseOverAnchor;
	
	@Inject
	public EntityBadgeViewImpl(final Binder uiBinder,
			SynapseJSNIUtils synapseJSNIUtils,
			SageImageBundle sageImageBundle, 
			PortalGinInjector ginInjector) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.sageImageBundle = sageImageBundle;
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void setEntity(final EntityHeader entityHeader) {
		clear();
		if(entityHeader == null)  throw new IllegalArgumentException("Entity is required");
		
		if(entityHeader != null) {
			isPopoverInitialized = false;
			popover.setIsHtml(true);
			popover.setTitle(entityHeader.getName());
			popover.setText(DisplayUtils.getLoadingHtml(sageImageBundle));
			
			final Anchor anchor = new Anchor();
			anchor.setText(entityHeader.getName());
			anchor.addStyleName("link");
			anchor.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					isMouseOverAnchor = true;
					showPopover(anchor, entityHeader.getId(), entityHeader.getName());
				}
			});
			anchor.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					popover.hide();
					isMouseOverAnchor = false;
				}
			});
			anchor.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.entityClicked(entityHeader);
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
			iconContainer.setWidget(iconPicture);
			entityContainer.add(anchor);
		} 		
	}
	
	public void showPopover(Anchor anchor, final String entityId, final String entityName) {
		if (!isPopoverInitialized) {
			presenter.getInfo(entityId, new AsyncCallback<KeyValueDisplay<String>>() {						
				@Override
				public void onSuccess(KeyValueDisplay<String> result) {
					renderPopover(ProvViewUtil.createEntityPopoverHtml(result).asString());
				}
				
				@Override
				public void onFailure(Throwable caught) {
					renderPopover(DisplayConstants.DETAILS_UNAVAILABLE);						
				}
				
				private void renderPopover(String content) {
					isPopoverInitialized = true;
					popover.setText(content);
					popover.reconfigure();
					if (isMouseOverAnchor)
						popover.show();
				}
			});
		}
		popover.show();
	}

	@Override
	public void showLoadError(String principalId) {
		clear();
		entityContainer.add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	@Override
	public void showLoading() {
		clear();
		entityContainer.add(new HTML(DisplayUtils.getLoadingHtml(sageImageBundle)));
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
	
	@Override
	public void clear() {
		iconContainer.clear();
		entityContainer.clear();
	}
	
	/*
	 * Private Methods
	 */

}
