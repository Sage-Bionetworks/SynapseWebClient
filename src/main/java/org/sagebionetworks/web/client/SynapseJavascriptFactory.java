package org.sagebionetworks.web.client;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.RestrictionInformationResponse;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dao.WikiPageKey;
import org.sagebionetworks.repo.model.wiki.WikiPage;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

public class SynapseJavascriptFactory {
	public enum OBJECT_TYPE {
		EntityBundle,
		Team,
		RestrictionInformationResponse,
		EntityChildrenResponse,
		WikiPageKey,
		UserGroupHeaderResponsePage,
		WikiPage,
		ListWrapperUserProfile,
		PaginatedResultsEntityHeader,
		UserBundle
	}

	/**
	 * Create a new instance of a concrete class using the object type
	 * @throws JSONObjectAdapterException 
	 */
	public Object newInstance(OBJECT_TYPE type, JSONObjectAdapter json) throws JSONObjectAdapterException {
		switch (type) {
		case EntityBundle :
			return new EntityBundle(json);
		case Team :
			return new Team(json);
		case RestrictionInformationResponse :
			return new RestrictionInformationResponse(json);
		case EntityChildrenResponse :
			return new EntityChildrenResponse(json);
		case WikiPageKey :
			return new WikiPageKey(json);
		case UserGroupHeaderResponsePage :
			return new UserGroupHeaderResponsePage(json);
		case WikiPage :
			return new WikiPage(json);
		case UserBundle :
			return new UserBundle(json);
		case PaginatedResultsEntityHeader :
			// json really represents a PaginatedResults (cannot reference here in js)
			List<EntityHeader> entityHeaderList = new ArrayList<>();
			JSONArrayAdapter resultsJsonArray = json.getJSONArray("results");
			for (int i = 0; i < resultsJsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = resultsJsonArray.getJSONObject(i);
				entityHeaderList.add(new EntityHeader(jsonObject));
			}
			return entityHeaderList;
		case ListWrapperUserProfile :
			// json really represents a ListWrapper, but we can't reference ListWrapper here because it uses Class.forName() (breaks gwt compile)
			List<UserProfile> list = new ArrayList<>();
			JSONArrayAdapter jsonArray = json.getJSONArray("list");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObjectAdapter jsonObject = jsonArray.getJSONObject(i);
				list.add(new UserProfile(jsonObject));
			}

			return list;
		default:
			throw new IllegalStateException("No match found for : "+ type);
		}
	}
}

