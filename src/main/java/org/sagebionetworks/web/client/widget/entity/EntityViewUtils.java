package org.sagebionetworks.web.client.widget.entity;

import java.util.Map;
import java.util.TreeMap;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;

import com.extjs.gxt.ui.client.widget.Html;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class EntityViewUtils {

	public static Widget createTitleWidget(EntityBundle bundle, String entityTypeDisplay, IconsImageBundle iconsImageBundle, boolean canEdit, boolean readOnly, SynapseJSNIUtils synapseJSNIUtils) {
		LayoutContainer lc = new LayoutContainer();
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
	    lc.add(createMetadata(bundle.getEntity()));
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
		return lc;
	}

	/*
	 * Private Methods
	 */

	/**
	 * Basic meta data about this entity
	 * @param entity
	 * @return
	 */
	private static Widget createMetadata(Entity entity) {
		FlowPanel panel = new FlowPanel();
		panel.getElement().setAttribute("style", "font-size: 80%;");

		InlineLabel label = new InlineLabel();
		label.setText("Added by: " + entity.getCreatedBy() + " on: "
				+ String.valueOf(entity.getCreatedOn()));
		panel.add(label);

		panel.add(new InlineHTML("<br />"));

		label = new InlineLabel();
		label.setText("Last updated by: " + entity.getModifiedBy() + " on: "
				+ String.valueOf(entity.getModifiedOn()));
		panel.add(label);

		if (entity instanceof Versionable) {
			Versionable vb = (Versionable) entity;

			panel.add(new InlineHTML("<br />"));
			label = new InlineLabel();
			StringBuilder sb = new StringBuilder();
			sb.append("Version: ");
			sb.append(vb.getVersionLabel());
			sb.append(" (");
			sb.append(vb.getVersionNumber());
			sb.append(")");

			if (vb.getVersionComment() != null) {
				sb.append(" - ");
				sb.append(vb.getVersionComment());
			}
			label.setText(sb.toString());
			panel.add(label);
		}

		return panel;
	}
	
}
