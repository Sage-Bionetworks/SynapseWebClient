package org.sagebionetworks.web.client.widget.table;
import static org.sagebionetworks.web.client.DisplayUtils.TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeViewImpl;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;



/**
 * Simple list item for an entity.
 */
public class TableEntityListGroupItem implements IsWidget {
	Anchor entityAnchor;
	@UiField
	FocusPanel iconContainer;
	@UiField
	TextBox idField;
	@UiField
	Div modifiedByField;
	@UiField
	Label modifiedOnField;
	@UiField
	FlowPanel entityContainer;
	@UiField
	Icon icon;

	public interface Binder extends UiBinder<IsWidget, TableEntityListGroupItem> {}
	public IsWidget w;
	private UserBadge modifiedByBadge;
	private DateTimeUtils dateTimeUtils;
	
	@Inject
	TableEntityListGroupItem(Binder binder, UserBadge modifiedByBadge, DateTimeUtils dateTimeUtils, PortalGinInjector ginInjector){
		w = binder.createAndBindUi(this);
		this.modifiedByBadge = modifiedByBadge;
		this.dateTimeUtils = dateTimeUtils;
		if (EntityBadgeViewImpl.placeChanger == null) {
			EntityBadgeViewImpl.placeChanger = ginInjector.getGlobalApplicationState().getPlaceChanger(); 
		}
		idField.addClickHandler(TEXTBOX_SELECT_ALL_FIELD_CLICKHANDLER);
	}
	
	public void configure(EntityHeader header, final ClickHandler clickHandler) {
		entityAnchor = new Anchor();
		entityAnchor.addClickHandler(EntityBadgeViewImpl.STANDARD_CLICKHANDLER);
		entityAnchor.setText(header.getName());
		entityAnchor.addStyleName("link");
		entityAnchor.setHref("#!Synapse:" + header.getId());
		entityAnchor.getElement().setAttribute(EntityBadgeViewImpl.ENTITY_ID_ATTRIBUTE, header.getId());
		entityContainer.add(entityAnchor);
		idField.setText(header.getId());
		if (header.getModifiedBy() != null) {
			modifiedByBadge.configure(header.getModifiedBy());
			modifiedByField.add(modifiedByBadge);
		}
		if (header.getModifiedOn() != null) {
			modifiedOnField.setText(dateTimeUtils.getDateTimeString(header.getModifiedOn()));	
		}
		icon.setType(EntityTypeUtils.getIconTypeForEntityClassName(header.getType()));
	}
	
	@Override
	public Widget asWidget() {
		return w.asWidget();
	}
}
