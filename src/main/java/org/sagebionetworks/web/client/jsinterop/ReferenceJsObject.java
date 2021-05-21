package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.repo.model.Reference;

import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name="Object")
public class ReferenceJsObject {

    public String targetId;
    @JsNullable
    public Integer targetVersionNumber;

    @JsOverlay
    public static ReferenceJsObject fromReference(Reference reference){
        if (reference == null){
            throw new IllegalArgumentException("Reference can not be null");
        }
        ReferenceJsObject referenceJsObject = new ReferenceJsObject();
        referenceJsObject.targetId = reference.getTargetId();
        referenceJsObject.targetVersionNumber = reference.getTargetVersionNumber().intValue();
        return referenceJsObject;
    }

    @JsOverlay
    public static final Reference toJavaObject(ReferenceJsObject jsObject) {
        Reference reference = new Reference();
        reference.setTargetId(jsObject.targetId);
        if (jsObject.targetVersionNumber != null) {
            reference.setTargetVersionNumber(Long.valueOf(jsObject.targetVersionNumber));
        }
        return reference;
    }
}
