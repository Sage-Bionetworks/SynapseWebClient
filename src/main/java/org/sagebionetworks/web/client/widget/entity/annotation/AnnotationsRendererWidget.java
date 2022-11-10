package org.sagebionetworks.web.client.widget.entity.annotation;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.sagebionetworks.repo.model.annotation.v2.AnnotationsValue;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;

/**
 * Render entity annotations
 */
public class AnnotationsRendererWidget
  implements AnnotationsRendererWidgetView.Presenter, IsWidget {

  private EntityBundle bundle;
  private AnnotationsRendererWidgetView view;
  private EditAnnotationsDialog editorDialog;
  private Map<String, AnnotationsValue> annotationsMap;
  private PreflightController preflightController;
  private PortalGinInjector ginInjector;
  private CookieProvider cookies;

  /**
   *
   * @param factory
   * @param cache
   * @param propertyView
   */
  @Inject
  public AnnotationsRendererWidget(
    AnnotationsRendererWidgetView propertyView,
    PreflightController preflightController,
    PortalGinInjector ginInjector,
    CookieProvider cookies
  ) {
    super();
    this.view = propertyView;
    this.ginInjector = ginInjector;
    this.preflightController = preflightController;
    this.view.setPresenter(this);
    this.cookies = cookies;
  }

  public EditAnnotationsDialog getEditAnnotationsDialog() {
    if (editorDialog == null) {
      editorDialog = ginInjector.getEditAnnotationsDialog();
      view.addEditorToPage(editorDialog.asWidget());
    }
    return editorDialog;
  }

  public void configure(
    EntityBundle bundle,
    boolean canEdit,
    boolean isCurrentVersion
  ) {
    this.bundle = bundle;
    annotationsMap = bundle.getAnnotations().getAnnotations();
    if (!annotationsMap.isEmpty()) view.configure(annotationsMap); else {
      view.showNoAnnotations();
    }
    view.setEditUIVisible(isCurrentVersion && canEdit);
  }

  public boolean isEmpty() {
    return annotationsMap.isEmpty();
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void onEdit() {
    preflightController.checkUploadToEntity(
      bundle,
      () -> getEditAnnotationsDialog().configure(bundle)
    );
  }
}
