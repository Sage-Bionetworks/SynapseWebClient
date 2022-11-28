package org.sagebionetworks.web.client.jsinterop.entity.actionmenu;

import java.util.List;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.sagebionetworks.web.client.jsinterop.ActionViewProps;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class EntityActionMenuLayout {

  private ActionViewProps[][] primaryMenuActions;
  private ActionViewProps[][] downloadMenuActions;
  private ActionViewProps[] buttonActions;
  private String primaryMenuText;
  /* Only strings that map to synapse-react-client Icons */
  private String primaryMenuEndIcon;

  private Object menuButtonSx;

  private EntityActionMenuLayout() {
    // Private constructor to force using the JsOverlay create
  }

  @JsOverlay
  public static final EntityActionMenuLayout create() {
    EntityActionMenuLayout layout = new EntityActionMenuLayout();
    layout.primaryMenuActions = new ActionViewProps[][] {};
    layout.downloadMenuActions = new ActionViewProps[][] {};
    layout.buttonActions = new ActionViewProps[] {};
    layout.primaryMenuText = "Tools";
    layout.primaryMenuEndIcon = "verticalEllipsis";
    return layout;
  }

  @JsOverlay
  public final void setButtonActions(List<ActionViewProps> buttonActions) {
    this.buttonActions = buttonActions.toArray(this.buttonActions);
  }

  @JsOverlay
  public final void setPrimaryMenuActions(
    List<List<ActionViewProps>> primaryMenuActions
  ) {
    this.primaryMenuActions = nestedActionViewPropsToArray(primaryMenuActions);
  }

  @JsOverlay
  public final void setDownloadMenuActions(
    List<List<ActionViewProps>> downloadMenuActions
  ) {
    this.downloadMenuActions =
      nestedActionViewPropsToArray(downloadMenuActions);
  }

  @JsOverlay
  private final ActionViewProps[][] nestedActionViewPropsToArray(
    List<List<ActionViewProps>> lists
  ) {
    ActionViewProps[][] array = new ActionViewProps[lists.size()][];
    ActionViewProps[] blankArray = new ActionViewProps[0];
    for (int i = 0; i < lists.size(); i++) {
      array[i] = lists.get(i).toArray(blankArray);
    }
    return array;
  }

  @JsOverlay
  public final void setPrimaryMenuText(String primaryMenuText) {
    this.primaryMenuText = primaryMenuText;
  }

  @JsOverlay
  public final void setPrimaryMenuEndIcon(String primaryMenuEndIcon) {
    this.primaryMenuEndIcon = primaryMenuEndIcon;
  }

  @JsOverlay
  public final void setMenuButtonSx(Object menuButtonSx) {
    this.menuButtonSx = menuButtonSx;
  }
}
