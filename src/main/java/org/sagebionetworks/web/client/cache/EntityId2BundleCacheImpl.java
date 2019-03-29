package org.sagebionetworks.web.client.cache;

import java.util.HashMap;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.inject.Inject;

public class EntityId2BundleCacheImpl extends HashMap<String, EntityBundle> implements EntityId2BundleCache {
	@Inject
	public EntityId2BundleCacheImpl() {
	}
}