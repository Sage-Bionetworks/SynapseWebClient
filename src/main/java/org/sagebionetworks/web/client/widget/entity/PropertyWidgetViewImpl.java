package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class PropertyWidgetViewImpl extends FlowPanel implements PropertyWidgetView, IsWidget {
	public PropertyWidgetViewImpl() {
		this.addStyleName("inline-block");
	}
	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	public void setRows(List<EntityRow<?>> rows) {
		this.clear();
		//now add a button for every row
		SafeHtmlBuilder htmlSb = new SafeHtmlBuilder();
		for (EntityRow<?> row : rows) {
			
			htmlSb.appendHtmlConstant("<span class=\"label margin-right-5\" style=\"display:inline\"><span style=\"font-weight:normal\">");
			htmlSb.appendEscaped(row.getLabel());
			htmlSb.appendHtmlConstant(":</span> ");
			htmlSb.appendHtmlConstant(SafeHtmlUtils.htmlEscapeAllowEntities(row.getDislplayValue()));
			htmlSb.appendHtmlConstant("</span>");
		}
		this.add(new HTML(htmlSb.toSafeHtml()));
	}

}
