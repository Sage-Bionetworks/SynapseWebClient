package org.sagebionetworks.web.client.widget.table;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.view.client.ProvidesKey;

public class TableModel implements Comparable<TableModel> {
	// The key provider that provides the unique ID of a contact.
	public static final ProvidesKey<TableModel> KEY_PROVIDER = new ProvidesKey<TableModel>() {
		@Override
		public Object getKey(TableModel item) {
			return item == null ? null : item.getId();
		}
	};

	private static int sequence = 0;
	private String id;
	private Map<String, String> map;

	/**
	 * Construct TableModel with default id
	 */
	public TableModel() {
		this("tm" + String.valueOf(++sequence));
	}

	/**
	 * Construct TableModel with provided id
	 * @param id - identified for this model. MUST be unique amongst other table models! If not, use the default constructor.
	 */
	public TableModel(String id) {
		this.id = id;
		map = new HashMap<String, String>();
	}

	public String getId() {
		return id;
	}

	@Override
	public int compareTo(TableModel o) {
		return (o == null || o.getId() == null) ? -1 : -o.getId().compareTo(getId());
	}

	public String get(String key) {
		String val = map.get(key) == null ? "" : map.get(key);
		return val;
	}

	public void put(String key, String value) {
		map.put(key, value);
	}

}
