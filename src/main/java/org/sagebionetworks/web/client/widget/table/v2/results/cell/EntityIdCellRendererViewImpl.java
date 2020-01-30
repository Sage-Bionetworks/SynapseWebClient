package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.Synapse;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererViewImpl implements EntityIdCellRendererView {
	Widget w;

	public interface Binder extends UiBinder<Widget, EntityIdCellRendererViewImpl> {
	}

	@UiField
	Span loadingUI;
	Icon errorIcon;
	@UiField
	Icon entityIcon;
	@UiField
	Anchor entityLink;
	String entityId;

	public static final String ENTITY_ID_ATTRIBUTE = "data-entity-id";
	public static PlaceChanger placeChanger = null;
	public static final ClickHandler STANDARD_CLICKHANDLER = event -> {
		if (!DisplayUtils.isAnyModifierKeyDown(event)) {
			event.preventDefault();
			Widget panel = (Widget) event.getSource();
			String entityId = panel.getElement().getAttribute(ENTITY_ID_ATTRIBUTE);
			placeChanger.goTo(new Synapse(entityId));
		}
	};
	HandlerRegistration handlerRegistration;

	@Inject
	public EntityIdCellRendererViewImpl(Binder binder, GlobalApplicationState globalAppState) {
		w = binder.createAndBindUi(this);
		placeChanger = globalAppState.getPlaceChanger();
		handlerRegistration = entityLink.addClickHandler(STANDARD_CLICKHANDLER);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setIcon(IconType iconType) {
		loadingUI.setVisible(false);
		entityIcon.setType(iconType);
		entityIcon.setVisible(true);
	}

	@Override
	public void setEntityId(String entityId) {
		entityLink.setHref(Synapse.getHrefForDotVersion(entityId));
		this.entityId = entityId;
		entityLink.getElement().setAttribute(ENTITY_ID_ATTRIBUTE, entityId);
	}

	@Override
	public void setClickHandler(ClickHandler clickHandler) {
		handlerRegistration.removeHandler();
		handlerRegistration = entityLink.addClickHandler(event -> {
			if (!DisplayUtils.isAnyModifierKeyDown(event)) {
				event.preventDefault();
				clickHandler.onClick(event);
			}
		});
	}

	@Override
	public void setLinkText(String text) {
		entityLink.setText(text);
	}

	@Override
	public void showErrorIcon(String error) {
		// lazily construct error UI
		entityIcon.setType(IconType.EXCLAMATION_CIRCLE);
		loadingUI.setVisible(false);
		entityIcon.setVisible(true);
	}

	@Override
	public void showLoadingIcon() {
		entityIcon.setVisible(false);
		loadingUI.setVisible(true);
	}

	@Override
	public void hideAllIcons() {
		entityIcon.setVisible(false);
		loadingUI.setVisible(false);
	}

	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}
}
