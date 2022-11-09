package org.sagebionetworks.web.client.resources;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface ResourceLoader {
  /**
   * Require certain web resources
   *
   * @param resources
   * @param loadedCallback
   */
  void requires(WebResource resource, AsyncCallback<Void> loadedCallback);

  /**
   * Require certain web resources
   *
   * @param resources
   * @param loadedCallback
   */
  void requires(
    List<WebResource> resources,
    AsyncCallback<Void> loadedCallback
  );

  boolean isLoaded(WebResource resource);
}
