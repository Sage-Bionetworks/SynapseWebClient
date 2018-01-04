package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererViewImpl implements EntityIdCellRendererView {
	Widget w;
	public interface Binder extends UiBinder<Widget, EntityIdCellRendererViewImpl> {}
	@UiField
	Span loadingUI;
	@UiField
	Tooltip errorField;
	@UiField
	Icon errorIcon;
	@UiField
	Icon entityIcon;
	@UiField
	Anchor entityLink;
	String entityId;
	ClickHandler customClickHandler;
	@Inject
	public EntityIdCellRendererViewImpl(Binder binder, GlobalApplicationState globalAppState){
		w = binder.createAndBindUi(this);
		entityLink.addClickHandler(event -> {
			event.preventDefault();
			if (customClickHandler != null) {
				customClickHandler.onClick(event);
			} else {
				globalAppState.getPlaceChanger().goTo(new Synapse(entityId));
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setIcon(IconType iconType) {
		errorIcon.setVisible(false);
		loadingUI.setVisible(false);
		entityIcon.setType(iconType);
		entityIcon.setVisible(true);
	}
	
	@Override
	public void setEntityId(String entityId) {
		entityLink.setHref(Synapse.getHrefForDotVersion(entityId));
		this.entityId = entityId;
	}
	
	@Override
	public void setClickHandler(ClickHandler clickHandler) {
		customClickHandler = clickHandler;
	}
	
	@Override
	public void setLinkText(String text) {
		entityLink.setText(text);
	}
	
	@Override
	public void showErrorIcon(String error) {
		loadingUI.setVisible(false);
		entityIcon.setVisible(false);
		errorIcon.setVisible(true);
		errorField.setTitle(error);
		errorField.reconfigure();
	}
	
	@Override
	public void showLoadingIcon() {
		entityIcon.setVisible(false);
		errorIcon.setVisible(false);
		loadingUI.setVisible(true);
	}
	@Override
	public void hideAllIcons() {
		entityIcon.setVisible(false);
		errorIcon.setVisible(false);
		loadingUI.setVisible(false);
	}
	
	@Override
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}
}
