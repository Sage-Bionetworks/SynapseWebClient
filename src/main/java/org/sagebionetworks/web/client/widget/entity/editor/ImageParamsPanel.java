package org.sagebionetworks.web.client.widget.entity.editor;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;

public class ImageParamsPanel extends FlowPanel{
	
	Button none = new Button("Do not float");
	Button left = new Button("Float left");
	Button center = new Button("Float center");
	Button right = new Button("Float right");
	String selectedAlignment;
	public ImageParamsPanel() {
		init();
	}
	
	public void init() {
		setAlignment(WidgetConstants.FLOAT_NONE);
		
		ButtonGroup group = new ButtonGroup();
		group.addStyleName("margin-10");
		group.add(none);
		group.add(left);
		group.add(center);
		group.add(right);
		add(group);
		
		none.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setAlignment(WidgetConstants.FLOAT_NONE);
			}
		});
		
		left.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setAlignment(WidgetConstants.FLOAT_LEFT);
			}
		});
		
		center.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setAlignment(WidgetConstants.FLOAT_CENTER);
			}
		});
		right.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setAlignment(WidgetConstants.FLOAT_RIGHT);
			}
		});
	}
	
	public String getAlignment() {
		return selectedAlignment;
	}

	public void setAlignment(String alignmentValue) {
		selectedAlignment = alignmentValue;
		setActive(false, left, center, right, none);
		if (WidgetConstants.FLOAT_LEFT.equals(alignmentValue)) {
			left.setActive(true);
		} else if (WidgetConstants.FLOAT_CENTER.equals(alignmentValue)) {
			center.setActive(true);
		} else if (WidgetConstants.FLOAT_RIGHT.equals(alignmentValue)) {
			right.setActive(true);
		 }else {
			none.setActive(true);
		}
	}
	
	private void setActive(boolean isActive, Button... buttons) {
		for (Button button : buttons) {
			button.setActive(isActive);
		}
	}
}

