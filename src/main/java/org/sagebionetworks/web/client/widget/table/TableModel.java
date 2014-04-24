package org.sagebionetworks.web.client.widget.table;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.view.client.ProvidesKey;

public class TableModel implements Comparable<TableModel> {
	
	private static final String TEMP_ID_PREFIX = "__tm";
	
	// The key provider that provides the unique ID of a contact.
	public static final ProvidesKey<TableModel> KEY_PROVIDER = new ProvidesKey<TableModel>() {
		@Override
		public Object getKey(TableModel item) {
			return item == null ? null : item.id;
		}
	};

	private static int sequence = 0;
	private String id;
	private String versionNumber;
	private Map<String, String> map;

	/**
	 * Construct TableModel with default (temp) id
	 */
	public TableModel() {
		this(TEMP_ID_PREFIX + String.valueOf(++sequence), null);
	}

	/**
	 * Construct TableModel with provided id
	 * @param id - identified for this model. MUST be unique amongst other table models! If not, use the default constructor.
	 */
	public TableModel(String id, String versionNumber) {
		this.id = id;
		this.versionNumber = versionNumber;
		map = new HashMap<String, String>();
	}

	/**
	 * This should only be used to set the id for a new row that has finally acquired a rowId from the system
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		if(id.startsWith(TEMP_ID_PREFIX)) return null;
		return id;
	}	
	
	public String getVersionNumber() {
		return versionNumber;
	}

	@Override
	public int compareTo(TableModel o) {
		return (o == null || o.getId() == null) ? -1 : -o.getId().compareTo(getId());
	}

	/**
	 * For use by the GWT CellTable which doesn't handle null values well
	 * @param key
	 * @return
	 */
	public String getNeverNull(String key) {
		String val = map.get(key) == null ? "" : map.get(key);
		return val;
	}
	
	public String get(String key) {
		return map.get(key);
	}

	public void put(String key, String value) {
		map.put(key, value);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}

	@Override
	public String toString() {
		return "TableModel [id=" + id + ", versionNumber=" + versionNumber
				+ ", map=" + map + "]";
	}
	
	
}
