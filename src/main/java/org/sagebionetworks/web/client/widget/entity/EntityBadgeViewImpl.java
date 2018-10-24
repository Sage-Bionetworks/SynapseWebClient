package org.sagebionetworks.web.client.widget.entity;
import static org.sagebionetworks.web.client.DisplayUtils.TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Emphasis;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBadgeViewImpl extends Composite implements EntityBadgeView {
	SynapseJSNIUtils synapseJSNIUtils;
	Widget modifiedByWidget;
	Presenter presenter;
	UserBadge modifiedByBadge;
	DateTimeUtils dateTimeUtils;
	public interface Binder extends UiBinder<Widget, EntityBadgeViewImpl> {	}
	
	@UiField
	FocusPanel iconContainer;
	@UiField
	Icon icon;
	@UiField
	FlowPanel entityContainer;
	@UiField
	TextBox idField;
	@UiField
	SimplePanel modifiedByField;
	@UiField
	Label modifiedOnField;
	
	@UiField
	Label sizeField;
	@UiField
	TextBox md5Field;
	@UiField
	org.gwtbootstrap3.client.ui.Anchor addToDownloadListLink;
	
	@UiField
	Div nameContainer;
	
	Callback onAttachCallback;
	Anchor entityAnchor;
	public static PlaceChanger placeChanger = null;
	HandlerRegistration clickHandlerRegistration;
	public static final String ENTITY_ID_ATTRIBUTE = "data-entity-id";
	
	public static final ClickHandler STANDARD_CLICKHANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			event.preventDefault();
			Widget panel = (Widget)event.getSource();
			String entityId = panel.getElement().getAttribute(ENTITY_ID_ATTRIBUTE);
			placeChanger.goTo(new Synapse(entityId));
		}
	};
	
	@Inject
	public EntityBadgeViewImpl(final Binder uiBinder,
			final SynapseJSNIUtils synapseJSNIUtils,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			UserBadge modifiedByBadge, 
			DateTimeUtils dateTimeUtils) {
		this.modifiedByBadge = modifiedByBadge;
		this.dateTimeUtils = dateTimeUtils;
		this.synapseJSNIUtils = synapseJSNIUtils;
		initWidget(uiBinder.createAndBindUi(this));
		if (EntityBadgeViewImpl.placeChanger == null) {
			EntityBadgeViewImpl.placeChanger = ginInjector.getGlobalApplicationState().getPlaceChanger(); 
		}
		
		idField.addClickHandler(TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER);
		md5Field.addClickHandler(TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER);
		addToDownloadListLink.addClickHandler(event -> {
			presenter.onAddToDownloadList();
		});
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
	
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
	}
	
	@Override
	public void setEntity(final EntityHeader entityHeader) {
		clear();
		if(entityHeader == null)  throw new IllegalArgumentException("Entity is required");
		
		if(entityHeader != null) {
			entityAnchor = new Anchor();
			clickHandlerRegistration = entityAnchor.addClickHandler(STANDARD_CLICKHANDLER);
			entityAnchor.setText(entityHeader.getName());
			entityAnchor.addStyleName("link");
			entityAnchor.setHref("#!Synapse:" + entityHeader.getId());
			entityAnchor.getElement().setAttribute(ENTITY_ID_ATTRIBUTE, entityHeader.getId());
			iconContainer.setWidget(icon);
			entityContainer.add(entityAnchor);
			idField.setText(entityHeader.getId());
			if (entityHeader.getModifiedBy() != null) {
				modifiedByBadge.configure(entityHeader.getModifiedBy());
				modifiedByField.add(modifiedByBadge);
			}
			if (entityHeader.getModifiedOn() != null) {
				modifiedOnField.setText(dateTimeUtils.getDateTimeString(entityHeader.getModifiedOn()));	
			}
		} 		
	}
	@Override
	public void setIcon(IconType iconType) {
		icon.setType(iconType);
	}
	
	@Override
	public void showLoadError(String principalId) {
		clear();
		entityContainer.add(new HTML(DisplayConstants.ERROR_LOADING));		
	}
	
	public void clear() {
		iconContainer.clear();
		entityContainer.clear();
	}
	
	@Override
	public void addClickHandler(final ClickHandler handler) {
		if (clickHandlerRegistration != null) {
			clickHandlerRegistration.removeHandler();	
		}
		clickHandlerRegistration = entityAnchor.addClickHandler(event -> {
			if (!DisplayUtils.isAnyModifierKeyDown(event)) {
				event.preventDefault();
				handler.onClick(event);
			}
		});
	}
	
	@Override
	public String getFriendlySize(Long contentSize, boolean abbreviatedUnits) {
		return DisplayUtils.getFriendlySize(contentSize, abbreviatedUnits);
	}
	
	@Override
	public void setAnnotations(String html) {
		Icon icon = new Icon(IconType.TAGS);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		Tooltip tooltip = new Tooltip(icon);
		tooltip.setHtml(SafeHtmlUtils.fromTrustedString(html));
		nameContainer.add(tooltip);
	}
	
	@Override
	public void setError(String error) {
		Icon icon = new Icon(IconType.EXCLAMATION_CIRCLE);
		icon.setFixedWidth(true);
		icon.setEmphasis(Emphasis.DANGER);
		icon.setPull(Pull.RIGHT);
		Tooltip tooltip = new Tooltip(icon, error);
		nameContainer.add(tooltip);
	}
	
	@Override
	public void setSize(String s) {
		sizeField.setText(s);
	}
	@Override
	public void setMd5(String s) {
		md5Field.setText(s);
	}

	@Override
	public void showHasWikiIcon() {
		Icon icon = new Icon(IconType.NEWSPAPER_O);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		nameContainer.add(new Tooltip(icon, "Has a wiki"));
	}
	@Override
	public void showPrivateIcon() {
		Icon icon = new Icon(IconType.LOCK);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		nameContainer.add(new Tooltip(icon, "Private"));
	}
	@Override
	public void showPublicIcon() {
		Icon icon = new Icon(IconType.GLOBE);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		nameContainer.add(new Tooltip(icon, "Public"));
	}
	@Override
	public void showSharingSetIcon() {
		Icon icon = new Icon(IconType.CHECK);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		nameContainer.add(new Tooltip(icon, "Sharing Settings have been set"));
	}
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(this);
	}
	
	@Override
	public void showDiscussionThreadIcon(){
		Icon icon = new Icon(IconType.COMMENT);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		nameContainer.add(new Tooltip(icon, "Has been mentioned in discussion"));
	}
	
	@Override
	public void showUnlinkIcon() {
		Icon icon = new Icon(IconType.CHAIN_BROKEN);
		icon.setFixedWidth(true);
		icon.setPull(Pull.RIGHT);
		icon.setEmphasis(Emphasis.DANGER);
		icon.addStyleName("imageButton");
		icon.addClickHandler(event -> {
			presenter.onUnlink();
		});
		
		nameContainer.add(new Tooltip(icon, "Remove this link"));
	}

	@Override
	public void showAddToDownloadList() {
		addToDownloadListLink.setVisible(true);	
	}

	@Override
	public void setModifiedByUserBadgeClickHandler(ClickHandler handler) {
		modifiedByBadge.setCustomClickHandler(handler);
	}
}
