package org.sagebionetworks.web.client.jsinterop;

import java.util.ArrayList;
import java.util.List;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsNullable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.web.client.utils.CallbackP;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityHeaderTableProps extends ReactComponentProps {

  ReferenceJsObject[] references;
  boolean isEditable;

  @FunctionalInterface
  @JsFunction
  public interface OnUpdateJavaScriptObject {
    void onUpdate(ReferenceJsObject[] referenceListJsObject);
  }

  @JsNullable
  OnUpdateJavaScriptObject onUpdate;

  @JsNullable
  String removeSelectedRowsButtonText;

  @JsOverlay
  public static EntityHeaderTableProps create(
    List<Reference> refs,
    boolean isEditable,
    CallbackP<ReferenceList> onUpdate,
    String removeSelectedRowsButtonText
  ) {
    EntityHeaderTableProps props = new EntityHeaderTableProps();
    props.references = new ReferenceJsObject[refs.size()];
    for (int i = 0; i < refs.size(); i++) {
      ReferenceJsObject newRef = ReferenceJsObject.create(
        refs.get(i).getTargetId(),
        refs.get(i).getTargetVersionNumber()
      );
      props.references[i] = newRef;
    }

    props.isEditable = isEditable;
    props.removeSelectedRowsButtonText = removeSelectedRowsButtonText;
    props.onUpdate =
      referenceListJsObject -> {
        ReferenceList newList = new ReferenceList();
        List<Reference> referenceList = new ArrayList<Reference>();
        for (ReferenceJsObject refJsObject : referenceListJsObject) {
          Reference ref = new Reference();
          ref.setTargetId(refJsObject.targetId);
          referenceList.add(ref);
        }
        newList.setReferences(referenceList);
        onUpdate.invoke(newList);
      };

    return props;
  }
}
