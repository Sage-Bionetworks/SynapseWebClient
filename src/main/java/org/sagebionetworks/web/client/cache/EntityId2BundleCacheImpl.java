package org.sagebionetworks.web.client.cache;

import com.google.inject.Inject;
import java.util.HashMap;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;

public class EntityId2BundleCacheImpl
  extends HashMap<String, EntityBundle>
  implements EntityId2BundleCache {

  @Inject
  public EntityId2BundleCacheImpl() {}
}
