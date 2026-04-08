package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction between the UploadCSVPreviewWidget and its view.
 *
 * @author John
 *
 */
public interface UploadCSVPreviewPageView extends IsWidget {
  /**
   * Business logic goes here.
   *
   */
  interface Presenter {}

  /**
   * Bind the view to its presenter.
   *
   * @param presenter
   */
  public void setPresenter(Presenter presenter);

  /**
   * Add the preview widget to the view.
   *
   * @param uploadPreviewWidget
   */
  public void setPreviewWidget(IsWidget uploadPreviewWidget);

  /**
   * Add the options widget to the view.
   *
   * @param asWidget
   */
  public void setCSVOptionsWidget(IsWidget asWidget);
}
