package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ViteManifestProviderImpl implements ViteManifestProvider {

  /*
   We use the ServletContext to access the Vite manifest because manifest & asset files must be emitted to the webapp
   folder so that the browser can access the assets. This would not be possible if the manifest was stored in the classpath (resources folder).

   NOTE: ServletContext cannot be injected, so an instance of this class must be created manually.
   */
  ServletContext context;

  public ViteManifestProviderImpl(ServletContext context) {
    this.context = context;
  }

  private static final String PATH_TO_MANIFEST =
    "generated/vite/.vite/manifest.json";
  private JSONObject manifest = null;

  @Override
  public JSONObject getManifest() {
    if (manifest != null) {
      return manifest;
    }
    try (InputStream stream = context.getResourceAsStream(PATH_TO_MANIFEST)) {
      JSONTokener tokener = new JSONTokener(stream);
      this.manifest = new JSONObject(tokener);
      return manifest;
    } catch (IOException e) {
      // Without a manifest file, we cannot provide necessary JavaScript assets, so this is not recoverable.
      throw new RuntimeException(e);
    }
  }
}
