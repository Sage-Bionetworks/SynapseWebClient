package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IconSvgProps extends ReactComponentProps {
	IconSvgOptions options;
	
	@JsOverlay
	public static IconSvgProps create(String icon, String color, String size, String padding, String label) {
		IconSvgProps props = new IconSvgProps();
		IconSvgOptions options = IconSvgOptions.create(icon, color, size, padding, label);
		props.options = options;
		return props;
	}
}
