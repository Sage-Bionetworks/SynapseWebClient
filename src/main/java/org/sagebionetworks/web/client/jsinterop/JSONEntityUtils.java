package org.sagebionetworks.web.client.jsinterop;

import org.sagebionetworks.gwt.client.schema.adapter.JSONObjectGwt;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

public interface JSONEntityUtils {
  /**
   * Converts a JSONEntity to a JsInterop compatible object.
   * @param entity
   * @return a JsInterop compatible object that represents the passed entity.
   */
  public static Object toJsInteropCompatibleObject(JSONEntity entity) {
    JSONObjectAdapter adapter = new JSONObjectGwt();
    try {
      entity.writeToJSONObject(adapter);
    } catch (JSONObjectAdapterException e) {
      throw new RuntimeException(e);
    }
    // This is a quick-and-dirty implementation--serialize the object to a string and use JSON.parse to deserialize it
    // It would be more efficient to update the JSONObjectGwt instance to directly create and return a native object.
    return JSON.parse(adapter.toJSONString());
  }
}
