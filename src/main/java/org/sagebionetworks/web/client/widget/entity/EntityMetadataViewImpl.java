package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.AnimationProtector;
import org.sagebionetworks.web.client.utils.AnimationProtectorViewImpl;

import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadataViewImpl extends Composite implements EntityMetadataView {

	private FavoriteWidget favoriteWidget;
	private DoiWidget doiWidget;
	AnimationProtector annotationAnimation;
	
	interface EntityMetadataViewImplUiBinder extends UiBinder<Widget, EntityMetadataViewImpl> {
	}
	
	private static EntityMetadataViewImplUiBinder uiBinder = GWT
			.create(EntityMetadataViewImplUiBinder.class);

	@UiField
	HTMLPanel entityNamePanel;
	@UiField
	HTMLPanel detailedMetadata;
	@UiField
	HTMLPanel dataUseContainer;
	@UiField
	Image entityIcon;
	@UiField
	SpanElement entityName;
	@UiField
	SpanElement entityId;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	SimplePanel doiPanel;
	@UiField
	HTMLPanel annotationsPanel;
	@UiField
	LayoutContainer annotationsContent;
	@UiField
	InlineLabel showAnnotations;

	
	private Presenter presenter;
	private boolean annotationsFilled = false;

	@UiField(provided = true)
	final IconsImageBundle icons;

	AnnotationsWidget annotationsWidget;
	RestrictionWidget restrictionWidget;
	
	@Inject
	public EntityMetadataViewImpl(IconsImageBundle iconsImageBundle,
			FavoriteWidget favoriteWidget,
			DoiWidget doiWidget,
			AnnotationsWidget annotationsWidget,
			RestrictionWidget restrictionWidget
			) {
		this.icons = iconsImageBundle;
		this.favoriteWidget = favoriteWidget;
		this.doiWidget = doiWidget;
		this.annotationsWidget = annotationsWidget;
		this.restrictionWidget = restrictionWidget;
		initWidget(uiBinder.createAndBindUi(this));

				
		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		
		doiPanel.addStyleName("inline-block");
		doiPanel.setWidget(doiWidget.asWidget());
	}

	@Override
	public void setEntityBundle(EntityBundle bundle, boolean canAdmin, boolean canEdit, boolean isShowingOlderVersion) {
		clearmeta();
		
		Entity e = bundle.getEntity();
		restrictionWidget.configure(bundle, true, false, true, new com.google.gwt.core.client.Callback<Void, Throwable>() {
			@Override
			public void onSuccess(Void result) {
				presenter.fireEntityUpdatedEvent();
			}
			@Override
			public void onFailure(Throwable reason) {
				showErrorMessage(reason.getMessage());
			}
		});
		
		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(e, DisplayUtils.IconSize.PX24, icons));
		synapseIconForEntity.applyTo(entityIcon);
		
		setEntityName(e.getName());
		setEntityId(e.getId());
					
		dataUseContainer.clear();
		Widget dataUse = restrictionWidget.asWidget();
		if(dataUse != null) {
			dataUseContainer.setVisible(true);
			dataUseContainer.add(new InlineHTML("<span style=\"margin-right: 5px;\" class=\"boldText\">"+DisplayConstants.DATA_ACCESS_RESTRICTIONS_TEXT+"</span>"));
			dataUseContainer.add(dataUse);
		} else {
			dataUseContainer.setVisible(false);
		}		
		
		Long versionNumber = null;
		if (e instanceof Versionable) {
			Versionable vb = (Versionable) e;
			versionNumber = vb.getVersionNumber();
		}
		favoriteWidget.configure(bundle.getEntity().getId());
		
		//doi widget
		doiWidget.configure(bundle.getEntity().getId(), bundle.getPermissions().getCanEdit(), versionNumber);
		
		// annotations		
		configureAnnotations(bundle, canEdit);
	}

	private void configureAnnotations(EntityBundle bundle, boolean canEdit) {
		// configure widget
		annotationsWidget.configure(bundle, canEdit);
		// show widget?
		if(canEdit || !annotationsWidget.isEmpty()) {
			annotationsPanel.setVisible(true);
		} else {
			annotationsPanel.setVisible(false);
		}
		
		// reset view
		showAnnotations.setText(DisplayConstants.SHOW_LC);
		annotationsContent.setVisible(false);
				
		if(!annotationsFilled) {
			annotationAnimation = new AnimationProtector(new AnimationProtectorViewImpl(showAnnotations, annotationsContent));
			FxConfig hideConfig = new FxConfig(400);
			hideConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					annotationsContent.layout(true);
					showAnnotations.setText(DisplayConstants.SHOW_LC);
				}
			});
			annotationAnimation.setHideConfig(hideConfig);
			FxConfig showConfig = new FxConfig(400);
			showConfig.setEffectCompleteListener(new Listener<FxEvent>() {
				@Override
				public void handleEvent(FxEvent be) {
					// This call to layout is necessary to force the scroll bar to appear on page-load
					annotationsContent.layout(true);
					showAnnotations.setText(DisplayConstants.HIDE_LC);
				}
			});
			annotationAnimation.setShowConfig(showConfig);
			showAnnotations.setText(DisplayConstants.SHOW_LC);
			
			FlowPanel wrap = new FlowPanel();
			wrap.addStyleName("highlight-box margin-bottom-15");
			wrap.getElement().setAttribute("highlight-box-title", DisplayConstants.ANNOTATIONS);
			wrap.add(annotationsWidget.asWidget());
			annotationsContent.add(wrap);
			
			annotationsFilled = true;
		}
	}
	
	private void clearmeta() {
		dataUseContainer.clear();
		doiWidget.clear();
	}

	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}
	
	@Override
	public void setDetailedMetadataVisible(boolean visible) {
		detailedMetadata.setVisible(visible);
	}
	
	@Override
	public void setEntityNameVisible(boolean visible) {
		this.entityNamePanel.setVisible(visible);
	}
	
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	public void setEntityName(String text) {
		entityName.setInnerText(text);
	}

	public void setEntityId(String text) {
		entityId.setInnerText(text);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
}

	@Override
	public void clear() {
		clearmeta();
	}

}
