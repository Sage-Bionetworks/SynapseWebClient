package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.jsinterop.IconSvgProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

import com.google.gwt.user.client.ui.InlineHTML;

public class IconSvg extends InlineHTML {

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
		render();
	}

	private void render() {
		IconSvgProps props = IconSvgProps.create(icon, color, size, padding, label);
		ReactElement component = React.createElement(SRC.SynapseComponents.IconSvg, props);
		ReactDOM.render(component, getElement());
	}

	public void setIcon(String icon) {
		this.icon = icon;
		render();
	}
}
