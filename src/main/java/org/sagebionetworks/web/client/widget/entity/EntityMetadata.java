package org.sagebionetworks.web.client.widget.entity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Text;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntityMetadata extends Composite {
	interface EntityMetadataUiBinder extends UiBinder<Widget, EntityMetadata> {
	}

	private static EntityMetadataUiBinder uiBinder = GWT
			.create(EntityMetadataUiBinder.class);

	@UiField
	HTMLPanel panel;

	@UiField
	SpanElement createName;
	@UiField
	SpanElement createDate;
	@UiField
	SpanElement modifyName;
	@UiField
	SpanElement modifyDate;
	@UiField
	SpanElement version;

	@UiField
	SpanElement previousVersions;

	@UiField
	Anchor allVersions;

	public EntityMetadata() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setCreateName(String text) {
		createName.setInnerText(text);
	}

	public void setCreateDate(String text) {
		createDate.setInnerText(text);
	}

	public void setModifyName(String text) {
		modifyName.setInnerText(text);
	}

	public void setModifyDate(String text) {
		modifyDate.setInnerText(text);
	}

	public void setVersionInfo(String text) {
		version.setInnerText(text);
	}

	public void setAllVersionHref(String href) {
		allVersions.setHref(href);
	}

	public void addToPreviousVersions(Widget widget, Text delim) {
		previousVersions.appendChild(delim);
		panel.add(widget, previousVersions);
	}

}
