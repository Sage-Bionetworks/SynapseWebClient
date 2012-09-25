package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class TitleWidget {


	private LayoutContainer lc;
	private EntityMetadata entityMetadata;

	public TitleWidget(EntityBundle bundle, String entityTypeDisplay,
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

	    // Metadata
	    lc.add(createMetadata(bundle.getEntity(), iconsImageBundle));
	    // the headers for description and property

		if(canEdit && readOnly) {
			HTML roContainer = new HTML("<h4 class=\"colored\"> " + DisplayConstants.READ_ONLY + " " +AbstractImagePrototype.create(iconsImageBundle.help16()).getHTML() + "</h4>");
			roContainer.setWidth("100px");

			Map<String, String> optionsMap = new TreeMap<String, String>();
			optionsMap.put("title", DisplayConstants.WHY_VERSION_READ_ONLY_MODE);
			optionsMap.put("data-placement", "right");
			DisplayUtils.addTooltip(synapseJSNIUtils, roContainer, optionsMap);
			lc.add(roContainer);
		}


	    lc.layout();
	}

	public Widget asWidget() {
		return lc;
	}

	public void setVersions(Versionable entity, TreeMap<Long, String> latestVersions) {
		if (latestVersions == null || latestVersions.size() == 0) {
			InlineLabel notFound = new InlineLabel(DisplayConstants.ERROR_VERSIONS_NOT_FOUND);
			entityMetadata.addToPreviousVersions(notFound);
			return;
		}

		for (Entry<Long, String> entry : latestVersions.entrySet()) {
			if (!entity.getVersionNumber().equals(entry.getKey())) {
				StringBuilder target = new StringBuilder("Synapse:");
				target.append(entity.getId());
				target.append("/version/");
				target.append(entry.getKey());

				StringBuilder label = new StringBuilder();
				label.append(entry.getValue().toString());
				label.append(" [");
				label.append(entry.getKey().toString());
				label.append("]");
				Hyperlink anchor = new Hyperlink(label.toString(),
						target.toString());
				anchor.setStyleName("link");

				entityMetadata.addToPreviousVersions(anchor);
			}
		}
	}

	public void clear() {
		lc.clearState();
		lc.removeAll();
	}

	private Widget createMetadata(Entity entity, IconsImageBundle iconsImageBundle) {
		entityMetadata = new EntityMetadata();
		entityMetadata.setCreateName(entity.getCreatedBy());
		entityMetadata.setCreateDate(String.valueOf(entity.getCreatedOn()));
		entityMetadata.setModifyName(entity.getModifiedBy());
		entityMetadata.setModifyDate(String.valueOf(entity.getModifiedOn()));
		entityMetadata.setVersionsVisible(false);

		if (entity instanceof Versionable) {
			entityMetadata.setVersionsVisible(true);
			Versionable vb = (Versionable) entity;
			StringBuilder sb = new StringBuilder();
			sb.append(vb.getVersionLabel());
			sb.append(" [");
			sb.append(vb.getVersionNumber());
			sb.append("]");

			if (vb.getVersionComment() != null) {
				sb.append(" - ");
				sb.append(vb.getVersionComment());
			}
			entityMetadata.setVersionInfo(sb.toString());
		}
		return entityMetadata;
	}

}
