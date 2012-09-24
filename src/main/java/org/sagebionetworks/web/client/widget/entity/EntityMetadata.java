package org.sagebionetworks.web.client.widget.entity;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
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
	HTMLPanel versions;
	@UiField
	SpanElement version;

	@UiField
	VerticalPanel previousVersions;

	@UiField
	InlineLabel allVersions;

	public EntityMetadata() {
		initWidget(uiBinder.createAndBindUi(this));
		allVersions.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (previousVersions.isVisible()) {
					previousVersions.el().slideOut(Direction.UP, FxConfig.NONE);
					allVersions.setText("show all versions");
				} else {
					previousVersions.setVisible(true);
					previousVersions.el().slideIn(Direction.DOWN, FxConfig.NONE);
					allVersions.setText("hide all versions");
				}
			}
		});
		previousVersions.setLayout(new VBoxLayout());
		previousVersions.setScrollMode(Scroll.AUTOY);
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

	public void addToPreviousVersions(Widget widget) {
		TableData data = new TableData();
		data.setStyle("padding-left:50px");
		previousVersions.add(widget, data);
		previousVersions.layout(true);
	}

	public void setVersionsVisible(boolean visible) {
		versions.setVisible(visible);
	}

}
