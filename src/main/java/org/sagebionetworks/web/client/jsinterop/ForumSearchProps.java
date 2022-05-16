package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ForumSearchProps extends ReactComponentProps {
	String forumId;
    @JsNullable
	String projectId;
	
    @FunctionalInterface
	@JsFunction
	public interface OnSearchUIVisibleHandler {
		void onUpdate(boolean visible);
	}
    @JsNullable
    OnSearchUIVisibleHandler onSearchUIVisible;

	@JsOverlay
	public static ForumSearchProps create(String forumId, String projectId, OnSearchUIVisibleHandler onSearchUIVisible) {
		ForumSearchProps props = new ForumSearchProps();
		props.forumId = forumId;
		props.projectId = projectId;
		props.onSearchUIVisible = onSearchUIVisible;
		return props;
	}
}
