package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * A React Ref, which may be either a {@link ReactRefObject} or a {@link ReactRefCallback}.
 * @param <T>
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public interface ReactRef<T> {}
