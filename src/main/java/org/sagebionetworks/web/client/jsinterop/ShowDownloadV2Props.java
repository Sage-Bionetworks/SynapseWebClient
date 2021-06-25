package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class ShowDownloadV2Props extends ReactComponentProps {

	public String to;

	@JsOverlay
	public static ShowDownloadV2Props create(String to) {
		ShowDownloadV2Props props = new ShowDownloadV2Props();
		props.to = to;
		return props;
	}
}
