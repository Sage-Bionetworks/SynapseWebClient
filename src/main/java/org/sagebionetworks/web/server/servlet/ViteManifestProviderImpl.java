package org.sagebionetworks.web.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ViteManifestProviderImpl implements ViteManifestProvider {

  public ViteManifestProviderImpl() {}

  private static final String PATH_TO_MANIFEST = "/vite/manifest.json";
  private JSONObject manifest = null;

  @Override
  public JSONObject getManifest() {
    if (manifest != null) {
      return manifest;
    }
    try (
      InputStream stream =
        this.getClass().getClassLoader().getResourceAsStream(PATH_TO_MANIFEST)
    ) {
      JSONTokener tokener = new JSONTokener(stream);
      this.manifest = new JSONObject(tokener);
      return manifest;
    } catch (IOException e) {
      // Without a manifest file, we cannot provide necessary JavaScript assets, so this is not recoverable.
      throw new RuntimeException(e);
    }
  }
}
