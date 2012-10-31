package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.model.EntityBundle;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class EntityMetadataViewImpl extends Composite {
	interface EntityMetadataViewImplUiBinder extends UiBinder<Widget, EntityMetadataViewImpl> {
	}

	protected static final String VERSION_KEY_ID = "id";

	protected static final String VERSION_KEY_NUMBER = "number";

	protected static final String VERSION_KEY_LABEL = "label";

	protected static final String VERSION_KEY_COMMENT = "comment";

	protected static final String VERSION_KEY_MOD_ON = "modifiedOn";

	protected static final String VERSION_KEY_MOD_BY = "modifiedBy";

	private static EntityMetadataViewImplUiBinder uiBinder = GWT
			.create(EntityMetadataViewImplUiBinder.class);

	interface Style extends CssResource {
		String limitedHeight();
		String currentVersion();
	}

	@UiField
	Style style;

	@UiField
	HTMLPanel panel;

	@UiField
	Image entityIcon;
	@UiField
	SpanElement entityName;
	@UiField
	SpanElement entityId;
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
	SpanElement label;
	@UiField
	SpanElement comment;

	@UiField
	LayoutContainer previousVersions;

	@UiField
	InlineLabel allVersions;

	public EntityMetadataViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));

		final FxConfig config = new FxConfig(400);
		config.setEffectCompleteListener(new Listener<FxEvent>() {
			@Override
			public void handleEvent(FxEvent be) {
				// This call to layout is necessary to force the scroll bar to appear on page-load
				previousVersions.layout(true);
				allVersions.getElement().setPropertyBoolean("animating", false);
			}
		});

		allVersions.setText(DisplayConstants.SHOW_VERSIONS);
		allVersions.getElement().setPropertyBoolean("animating", false);
		allVersions.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!allVersions.getElement().getPropertyBoolean("animating")) {
					allVersions.getElement().setPropertyBoolean("animating", true);
					if (previousVersions.el().isVisible()) {
						allVersions.setText(DisplayConstants.SHOW_VERSIONS);
						previousVersions.el().slideOut(Direction.UP, config);
					} else {
						previousVersions.setVisible(true);
						allVersions.setText(DisplayConstants.HIDE_VERSIONS);
						previousVersions.el().slideIn(Direction.DOWN, config);
					}
				}
			}
		});
		previousVersions.setLayout(new FlowLayout(10));
	}

	public void setEntityBundle(EntityBundle bundle) {
		Entity e = bundle.getEntity();
		setEntityName(e.getName());
		setEntityId(e.getId());

		setCreateName(e.getCreatedBy());
		setCreateDate(String.valueOf(e.getCreatedOn()));

		setModifyName(e.getModifiedBy());
		setModifyDate(String.valueOf(e.getModifiedOn()));

		setVersionsVisible(false);
		if (e instanceof Versionable) {
			setVersionsVisible(true);
			Versionable vb = (Versionable) e;
			setVersionInfo(vb);
		}

	}

	public void setEntityName(String text) {
		entityName.setInnerText(text);
	}

	public void setEntityId(String text) {
		entityId.setInnerText(text);
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

	public void setVersionInfo(Versionable vb) {
		StringBuilder sb = new StringBuilder();
		sb.append(vb.getVersionLabel());

		if (vb.getVersionComment() != null) {
			sb.append(" - ");

			comment.setTitle(vb.getVersionComment());
			comment.setInnerText(DisplayUtils.stubStr(vb.getVersionComment(), 60));
		}
		label.setInnerText(sb.toString());
	}

	public void setPreviousVersions(ContentPanel versions) {
		previousVersions.add(versions);
		previousVersions.layout(true);
	}

	public void clearPreviousVersions() {
		previousVersions.removeAll();
	}

	public void setVersionsVisible(boolean visible) {
		versions.setVisible(visible);
	}

	public Style getStyle() {
		return style;
	}

}
