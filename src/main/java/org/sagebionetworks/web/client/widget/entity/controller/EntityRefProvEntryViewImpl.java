package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityRefProvEntryViewImpl implements EntityRefProvEntryView {

	public interface EntityRefProvEntryUIBinder extends UiBinder<Widget, EntityRefProvEntryViewImpl> {
	}

	@UiField
	Anchor synIdField;

	@UiField
	Text synVersionField;

	@UiField
	Button removeButton;

	Widget widget;
	Callback removalCallback;

	@Inject
	public EntityRefProvEntryViewImpl(EntityRefProvEntryUIBinder binder) {
		this.widget = binder.createAndBindUi(this);
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (removalCallback != null)
					removalCallback.invoke();
			}
		});
	}

	@Override
	public void configure(String synId, String versionNumber) {
		synIdField.setText(synId);
		synVersionField.setText(versionNumber);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public String getEntryId() {
		return synIdField.getText();
	}

	@Override
	public void setRemoveCallback(Callback removalCallback) {
		this.removalCallback = removalCallback;
	}

	@Override
	public String getEntryVersion() {
		return synVersionField.getText();
	}

	@Override
	public void setAnchorTarget(String targetURL) {
		synIdField.setHref(targetURL);
	}

}
