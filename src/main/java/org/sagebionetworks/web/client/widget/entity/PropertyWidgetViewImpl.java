package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class PropertyWidgetViewImpl extends FlowPanel implements PropertyWidgetView, IsWidget {
	public PropertyWidgetViewImpl() {
		this.addStyleName("span-24 notopmargin last");
	}
	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	public void setRows(List<EntityRow<?>> rows) {
		this.clear();
		//now add a button for every row
		for (EntityRow<?> row : rows) {
			FlowPanel container = new FlowPanel();
			container.addStyleName("inline-block light-border margin-right-5 margin-top-5");
			String value = SafeHtmlUtils.htmlEscapeAllowEntities(row.getDislplayValue());
			String label = row.getLabel();
			Label l1 = new Label(row.getLabel());
			l1.addStyleName("inline-block");
			String delimiter = label != null && label.trim().length() > 0 && value != null && value.trim().length() > 0 ? "&nbsp:&nbsp" : "";
			HTML l2 = new HTML(delimiter + value);
			l2.addStyleName("inline-block boldText");
			
			container.add(l1);
			container.add(l2);
			
			this.add(container);
		}
	}

}
