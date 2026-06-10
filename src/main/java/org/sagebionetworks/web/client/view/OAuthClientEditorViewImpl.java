package org.sagebionetworks.web.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.oauthclient.OAuthClientEditor;

public class OAuthClientEditorViewImpl
  extends Composite
  implements OAuthClientEditorView {

  public interface OAuthClientEditorViewImplUiBinder
    extends UiBinder<Widget, OAuthClientEditorViewImpl> {}

  @UiField
  SimplePanel componentContainer;

  private Header headerWidget;

  private final OAuthClientEditorViewImplUiBinder binder = GWT.create(
    OAuthClientEditorViewImplUiBinder.class
  );

  @Inject
  public OAuthClientEditorViewImpl(Header headerWidget) {
    initWidget(binder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    headerWidget.configure();
  }

  @Override
  public void createReactComponentWidget() {
    OAuthClientEditor component = new OAuthClientEditor();
    componentContainer.clear();
    componentContainer.add(component);
  }
}
