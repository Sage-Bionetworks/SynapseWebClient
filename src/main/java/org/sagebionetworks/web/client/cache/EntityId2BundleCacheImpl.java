package org.sagebionetworks.web.client.cache;

import java.util.HashMap;
import javax.inject.Inject;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;

public class EntityId2BundleCacheImpl
  extends HashMap<String, EntityBundle>
  implements EntityId2BundleCache {

  @Inject
  public EntityId2BundleCacheImpl() {}
}
