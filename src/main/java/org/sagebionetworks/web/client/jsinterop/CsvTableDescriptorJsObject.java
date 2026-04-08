package org.sagebionetworks.web.client.jsinterop;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.table.CsvTableDescriptor;

/**
 * JsInterop bridge for the CsvTableDescriptor plain JavaScript object expected by the React CsvPreview component
 * Only includes fields needed by GWT, add more as needed.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class CsvTableDescriptorJsObject {

  public String separator;
  public String escapeCharacter;
  public Boolean isFirstLineHeader;

  @JsOverlay
  public static CsvTableDescriptorJsObject create(
    CsvTableDescriptor descriptor
  ) {
    CsvTableDescriptorJsObject js = new CsvTableDescriptorJsObject();
    js.separator = descriptor.getSeparator();
    js.escapeCharacter = descriptor.getEscapeCharacter();
    js.isFirstLineHeader = descriptor.getIsFirstLineHeader();
    return js;
  }
}
