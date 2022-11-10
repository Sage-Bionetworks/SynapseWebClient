package org.sagebionetworks.web.client;

import com.google.gwt.user.client.Event.NativePreviewHandler;

public interface GlobalApplicationStateView {
  void showVersionOutOfDateGlobalMessage();

  void initGlobalViewProperties();

  void showGetVersionError(String error);

  void back();

  void initSRCEndpoints(String repoEndpoint, String portalEndpoint);

  void addNativePreviewHandler(NativePreviewHandler handler);
}
