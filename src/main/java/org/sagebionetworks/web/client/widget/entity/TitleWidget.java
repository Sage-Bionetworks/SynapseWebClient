package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class TitleWidget {


	private LayoutContainer lc;
	private EntityMetadataViewImpl entityMetadata;

	public TitleWidget(EntityBundle bundle, Widget shareSettingsWidget, Widget restrictionsWidget, String entityTypeDisplay,
			IconsImageBundle iconsImageBundle, boolean canEdit,
			boolean readOnly, SynapseJSNIUtils synapseJSNIUtils) {
		lc = new LayoutContainer();
		lc.setAutoWidth(true);
		lc.setAutoHeight(true);

		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h2>")
		.appendHtmlConstant(AbstractImagePrototype.create(DisplayUtils.getSynapseIconForEntity(bundle.getEntity(), DisplayUtils.IconSize.PX24, iconsImageBundle)).getHTML())
		.appendHtmlConstant("&nbsp;")
		.appendEscaped(bundle.getEntity().getName())
		.appendHtmlConstant("&nbsp;(")
		.appendEscaped(bundle.getEntity().getId())
		.appendHtmlConstant(")")
		.appendHtmlConstant("</h2>");
		lc.add(new HTML(shb.toSafeHtml()));

	    if (shareSettingsWidget!=null) lc.add(shareSettingsWidget);

	    // the headers for description and property
	    if (restrictionsWidget!=null) lc.add(restrictionsWidget);  

	    if(canEdit && readOnly) {
			HTML roContainer = new HTML("<h4 class=\"colored\"> " + DisplayConstants.READ_ONLY + " " +AbstractImagePrototype.create(iconsImageBundle.help16()).getHTML() + "</h4>");
			roContainer.setWidth("100px");

			DisplayUtils.addTooltip(synapseJSNIUtils, roContainer, DisplayConstants.WHY_VERSION_READ_ONLY_MODE, TOOLTIP_POSITION.RIGHT);
			lc.add(roContainer);
		}

	    // Metadata
	    lc.add(createMetadata(bundle.getEntity(), iconsImageBundle));
	    
	    lc.layout();
	}

	public Widget asWidget() {
		return lc;
	}

	public void clear() {
		lc.clearState();
		lc.removeAll();
	}

	private Widget createMetadata(Entity entity, IconsImageBundle iconsImageBundle) {
		//entityMetadata = new EntityMetadataViewImpl();
		entityMetadata.setCreateName(entity.getCreatedBy());
		entityMetadata.setCreateDate(String.valueOf(entity.getCreatedOn()));
		entityMetadata.setModifyName(entity.getModifiedBy());
		entityMetadata.setModifyDate(String.valueOf(entity.getModifiedOn()));
		entityMetadata.setVersionsVisible(false);

		if (entity instanceof Versionable) {
			entityMetadata.setVersionsVisible(true);
			Versionable vb = (Versionable) entity;
			entityMetadata.setVersionInfo(vb);
		}
		return entityMetadata;
	}

	public void setVersions(ContentPanel panel) {
		entityMetadata.clearPreviousVersions();
		entityMetadata.setPreviousVersions(panel);
	}

}
