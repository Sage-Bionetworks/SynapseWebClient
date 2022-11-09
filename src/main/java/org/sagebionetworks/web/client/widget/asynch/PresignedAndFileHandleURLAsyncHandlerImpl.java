package org.sagebionetworks.web.client.widget.asynch;

import com.google.inject.Inject;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;

public class PresignedAndFileHandleURLAsyncHandlerImpl
  extends BaseFileHandleAsyncHandlerImpl
  implements PresignedAndFileHandleURLAsyncHandler {

  @Inject
  public PresignedAndFileHandleURLAsyncHandlerImpl(
    SynapseJavascriptClient jsClient,
    GWTWrapper gwt,
    ClientCache clientCache,
    AdapterFactory adapterFactory
  ) {
    super(jsClient, gwt, clientCache, adapterFactory);
  }

  @Override
  protected boolean isIncludeFileHandles() {
    return true;
  }

  @Override
  protected boolean isIncludePreSignedURLs() {
    return true;
  }
}
