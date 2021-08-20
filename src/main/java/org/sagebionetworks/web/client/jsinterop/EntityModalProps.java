package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityModalProps extends ReactComponentProps {

	@JsFunction
	public interface Callback {
		void run();
	}

	String entityId;
	boolean show;
	@JsNullable
	Callback onClose;
	@JsNullable
	String initialTab; // "METADATA" | "ANNOTATIONS"
	@JsNullable
	boolean showTabs;

	@JsOverlay
	public static EntityModalProps create(String entityId, boolean show, Callback onClose, String initialTab, boolean showTabs) {
		EntityModalProps props = new EntityModalProps();
		props.entityId = entityId;
		props.show = show;
		props.onClose = onClose;
		props.initialTab = initialTab;
		props.showTabs = showTabs;
		return props;
	}
}
