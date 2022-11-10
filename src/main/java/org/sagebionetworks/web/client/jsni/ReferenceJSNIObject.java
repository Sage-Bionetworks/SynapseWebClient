package org.sagebionetworks.web.client.jsni;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JSNI-compatible object for {@link org.sagebionetworks.repo.model.Reference}
 */
public class ReferenceJSNIObject extends JavaScriptObject {

  protected ReferenceJSNIObject() {}

  public final native void setTargetId(String targetId) /*-{
        this.targetId = targetId;
    }-*/;

  public final native void setTargetVersionNumber(int targetVersionNumber) /*-{
        this.targetVersionNumber = targetVersionNumber;
    }-*/;

  public final native String getTargetId() /*-{ return this.targetId }-*/;

  public final native int getTargetVersionNumber() /*-{ return this.targetVersionNumber || -1 }-*/;

  public final org.sagebionetworks.repo.model.Reference getJavaObject() {
    org.sagebionetworks.repo.model.Reference r = new org.sagebionetworks.repo.model.Reference();
    r.setTargetId(this.getTargetId());
    if (this.getTargetVersionNumber() == -1) {
      r.setTargetVersionNumber(null);
    } else {
      r.setTargetVersionNumber(Long.valueOf(this.getTargetVersionNumber()));
    }
    return r;
  }
}
