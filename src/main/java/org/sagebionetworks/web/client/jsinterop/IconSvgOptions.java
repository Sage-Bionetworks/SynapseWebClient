package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IconSvgOptions {
	String icon;
	@JsNullable
	String color;
	@JsNullable
	String size;
	@JsNullable
	String padding;
	@JsNullable
	String label;
	
	@JsOverlay
	public static IconSvgOptions create(String icon, String color, String size, String padding, String label) {
		IconSvgOptions options = new IconSvgOptions();
		options.icon = icon;
		options.color = color;
		options.size = size;
		options.padding = padding;
		options.label = label;
		
		return options;
	}

}
