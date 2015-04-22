package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBrowserUtils {
	public static void loadFavorites(SynapseClientAsync synapseClient,
			final AdapterFactory adapterFactory,
			final GlobalApplicationState globalApplicationState,
			final AsyncCallback<List<EntityHeader>> callback) {
		synapseClient.getFavorites(new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> favorites) {
				//show whatever projects that we found (maybe zero)
				globalApplicationState.setFavorites(favorites);
				callback.onSuccess(favorites);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public static void sortEntityHeadersByName(List<EntityHeader> list) {
		Collections.sort(list, new Comparator<EntityHeader>() {
	        @Override
	        public int compare(EntityHeader o1, EntityHeader o2) {
	        	return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
	        }
		});
	}
	
}
