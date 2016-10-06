package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityIdCellRendererViewImpl implements EntityIdCellRendererView {
	Callback onAttachCallback;
	Widget w;
	public interface Binder extends UiBinder<Widget, EntityIdCellRendererViewImpl> {}
	@UiField
	Image loadingUI;
	@UiField
	Tooltip errorField;
	@UiField
	Icon errorIcon;
	@UiField
	Icon entityIcon;
	@UiField
	Anchor entityLink;
	
	@Inject
	public EntityIdCellRendererViewImpl(Binder binder){
		w = binder.createAndBindUi(this);
		w.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if(event.isAttached()) {
					onAttach();
				}
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(w);
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}

	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}
	
	public void onAttach() {
		if (onAttachCallback != null) {
			onAttachCallback.invoke();
		}
	}
	
	@Override
	public void setIcon(IconType iconType) {
		errorIcon.setVisible(false);
		loadingUI.setVisible(false);
		entityIcon.setType(iconType);
		entityIcon.setVisible(true);
	}
	@Override
	public void setLinkHref(String href) {
		entityLink.setHref(href);
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
}
