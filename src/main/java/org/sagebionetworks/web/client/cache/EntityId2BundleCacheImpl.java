package org.sagebionetworks.web.client.cache;

import java.util.HashMap;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.EntityPageTop;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EntityId2BundleCacheImpl extends HashMap<String, EntityBundle> implements EntityId2BundleCache {
	SynapseJavascriptClient jsClient;
	@Inject
	public EntityId2BundleCacheImpl(SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
	}
	
	@Override
	public void populate(String entityId) {
		jsClient.getEntityBundle(entityId, EntityPageTop.ALL_PARTS_MASK, new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				put(entityId, bundle);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				//no op
			}
		});
	}
}