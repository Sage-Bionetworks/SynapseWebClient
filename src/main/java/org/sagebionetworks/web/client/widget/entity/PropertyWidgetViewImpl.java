package org.sagebionetworks.web.client.widget.entity;

import java.util.List;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.TOOLTIP_POSITION;
import org.sagebionetworks.web.client.widget.entity.row.EntityRow;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

/**
 * A widget that renders entity properties.
 * 
 * @author jmhill
 *
 */
public class PropertyWidgetViewImpl extends FlowPanel implements PropertyWidgetView, IsWidget {
	
	private IconsImageBundle iconsImageBundle;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public PropertyWidgetViewImpl(IconsImageBundle iconsImageBundle, SynapseJSNIUtils synapseJSNIUtils) {
		this.addStyleName("span-24 notopmargin last");
		this.iconsImageBundle = iconsImageBundle;
		this.synapseJSNIUtils = synapseJSNIUtils;
	}
	
	private Image getNewButton(ImageResource resource, ClickHandler handler, String tooltipText) {
		Image addAnnotationButton = new Image(resource);
		addAnnotationButton.addStyleName("imageButton vertical-align-middle margin-left-5");
		addAnnotationButton.addClickHandler(handler);
		DisplayUtils.addTooltip(synapseJSNIUtils, addAnnotationButton, tooltipText, TOOLTIP_POSITION.BOTTOM);
		return addAnnotationButton;
	}
	
	/**
	 * The rows of data to render.
	 * 
	 * @param rows
	 */
	@Override
	public void configure(List<EntityRow<?>> rows, boolean canEdit) {
		this.clear();
		if (!rows.isEmpty() || canEdit) {
			Label title = new Label(DisplayConstants.ANNOTATIONS + ":");
			title.addStyleName("inline-block boldText");
			add(title);
			//include Add Annotation button if user can edit
			if (canEdit) {
				ClickHandler handler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						
					}
				};
				add(getNewButton(iconsImageBundle.addSquareGrey16(), handler, DisplayConstants.TEXT_ADD_ANNOTATION));
			}
			add(new HTML());
		}
			
		//now add a button for every row
		for (EntityRow<?> row : rows) {
			FlowPanel container = new FlowPanel();
			container.addStyleName("inline-block light-border margin-right-5 margin-top-5");
			String value = SafeHtmlUtils.htmlEscapeAllowEntities(row.getDislplayValue());
			String label = row.getLabel();
			String delimiter = label != null && label.trim().length() > 0 && value != null && value.trim().length() > 0 ? " : " : "";
			Label l1 = new Label(label + delimiter);
			l1.addStyleName("inline-block");
			HTML l2 = new HTML(value);
			l2.addStyleName("inline-block");
			DisplayUtils.addTooltip(synapseJSNIUtils, l2, row.getToolTipsBody(), TOOLTIP_POSITION.BOTTOM);
			
			container.add(l1);
			container.add(l2);
			
			//if user can edit values, then make it clickable
			if (canEdit) {
				//edit annotation handler
				ClickHandler editHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						
					}
				};
				l2.addStyleName("link");
				l2.addClickHandler(editHandler);
				//container.add(getNewButton(iconsImageBundle.editGrey16(), editHandler, DisplayConstants.BUTTON_EDIT));
				
				//delete annotation handler
				ClickHandler deleteHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						
					}
				};
				container.add(getNewButton(iconsImageBundle.deleteButtonGrey16(), deleteHandler, DisplayConstants.LABEL_DELETE));
			}
			else {
				l2.addStyleName("blackText");
			}
			
			
			this.add(container);
			
		}
	}

}
