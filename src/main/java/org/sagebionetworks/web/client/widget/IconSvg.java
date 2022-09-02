package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.jsinterop.IconSvgProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class IconSvg extends ReactComponentSpan {

	private String icon;
	private String color;
	private String size;
	private String padding;
	private String label;

	public IconSvg() {
	}
	
	public void configure(String icon, String color, String size, String padding, String label) {
		this.icon = icon;
		this.color = color;
		this.size = size;
		this.padding = padding;
		this.label = label;
		renderComponent();
	}

	private void renderComponent() {
		IconSvgProps props = IconSvgProps.create(icon, color, size, padding, label);
		ReactNode component = React.createElement(SRC.SynapseComponents.IconSvg, props);
		this.render(component);
	}

	public void setIcon(String icon) {
		this.icon = icon;
		renderComponent();
	}
}
