package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.APPROVAL_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.RESTRICTION_LEVEL;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListEditor;
import org.sagebionetworks.web.client.widget.sharing.PublicPrivateBadge;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityMetadataViewImpl extends Composite implements EntityMetadataView {

	private FavoriteWidget favoriteWidget;
	private DoiWidget doiWidget;
	
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
	
	private Presenter presenter;

	@UiField(provided = true)
	final IconsImageBundle icons;

	private SynapseJSNIUtils synapseJSNIUtils;
	private AccessControlListEditor accessControlListEditor;
	private PublicPrivateBadge publicPrivateBadge;
	
	@Inject
	public EntityMetadataViewImpl(IconsImageBundle iconsImageBundle,
			SynapseJSNIUtils synapseJSNIUtils, FavoriteWidget favoriteWidget, DoiWidget doiWidget, AccessControlListEditor accessControlListEditor, PublicPrivateBadge publicPrivateBadge) {
		this.icons = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.favoriteWidget = favoriteWidget;
		this.doiWidget = doiWidget;
		this.accessControlListEditor = accessControlListEditor;
		this.publicPrivateBadge = publicPrivateBadge;
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
		
		AbstractImagePrototype synapseIconForEntity = AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(e, DisplayUtils.IconSize.PX24, icons));
		synapseIconForEntity.applyTo(entityIcon);
		
		setEntityName(e.getName());
		setEntityId(e.getId());
					
		dataUseContainer.clear();
		if(bundle.getPermissions().getCanPublicRead()) {
			Widget dataUse = createRestrictionWidget();
			if(dataUse != null) {
				dataUseContainer.setVisible(true);
				dataUseContainer.add(dataUse);
			} else {
				dataUseContainer.setVisible(false);
			}		
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
	}
	
	private void configureShareSettings(Anchor link, Entity entity){
		accessControlListEditor.setResource(entity);
		link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				DisplayUtils.showSharingDialog(accessControlListEditor, new Callback() {
					@Override
					public void invoke() {
						presenter.fireEntityUpdatedEvent();
					}
				});
			}
		});
				
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
	
	private Widget createRestrictionWidget() {
		if (!presenter.includeRestrictionWidget()) return null;
		boolean isAnonymous = presenter.isAnonymous();
		boolean hasAdministrativeAccess = false;
		boolean hasFulfilledAccessRequirements = false;
		String jiraFlagLink = null;
		if (!isAnonymous) {
			hasAdministrativeAccess = presenter.hasAdministrativeAccess();
			jiraFlagLink = presenter.getJiraFlagUrl();
		}
		RESTRICTION_LEVEL restrictionLevel = presenter.getRestrictionLevel();
		APPROVAL_TYPE approvalType = presenter.getApprovalType();
		String accessRequirementText = null;
		Callback touAcceptanceCallback = null;
		Callback requestACTCallback = null;
		Callback imposeRestrictionsCallback = presenter.getImposeRestrictionsCallback();
		Callback loginCallback = presenter.getLoginCallback();
		if (approvalType!=APPROVAL_TYPE.NONE) {
			accessRequirementText = presenter.accessRequirementText();
			if (approvalType==APPROVAL_TYPE.USER_AGREEMENT) {
				touAcceptanceCallback = presenter.accessRequirementCallback();
			} else { // APPROVAL_TYPE.ACT_APPROVAL
				// get the Jira link for ACT approval
				if (!isAnonymous) {
					requestACTCallback = new Callback() {
						@Override
						public void invoke() {
							Window.open(presenter.getJiraRequestAccessUrl(), "_blank", "");

						}
					};
				}
			}
			if (!isAnonymous) hasFulfilledAccessRequirements = presenter.hasFulfilledAccessRequirements();
		}
		return EntityViewUtils.createRestrictionsWidget(
				jiraFlagLink,
				isAnonymous,
				hasAdministrativeAccess,
				accessRequirementText,
				touAcceptanceCallback,
				requestACTCallback,
				imposeRestrictionsCallback,
				loginCallback,
				restrictionLevel,
				approvalType,
				hasFulfilledAccessRequirements,
				icons,
				synapseJSNIUtils);
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
