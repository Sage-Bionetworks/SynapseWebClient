package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.jsinterop.IconSvgProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactDOM;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class IconSvg extends Span {

	public IconSvg() {
	}
	
	public void configure(String icon, String color, String size, String padding, String label) {
		IconSvgProps props = IconSvgProps.create(icon, color, size, padding, label);
		ReactElement component = React.createElement(SRC.SynapseComponents.IconSvg, props);
		ReactDOM.render(component, getElement());
	}
}
