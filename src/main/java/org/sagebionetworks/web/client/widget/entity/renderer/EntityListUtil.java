package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

/**
 * This utility class holds common presenter logic for the EntityListWidget and
 * EntityListConfigEditor
 * 
 * @author dburdick
 *
 */
public class EntityListUtil {

	private final static String NOTE_DELIMITER = ",";
	private final static String LIST_DELIMITER = ";";

	public static String recordsToString(List<EntityGroupRecord> records) {
		// add record to descriptor
		String recordStr = "";
		if (records == null)
			return recordStr;

		for (EntityGroupRecord record : records) {
			Reference ref = record.getEntityReference();
			if (ref == null)
				continue;
			if (!recordStr.equals(""))
				recordStr += LIST_DELIMITER;
			recordStr += DisplayUtils.createEntityVersionString(ref.getTargetId(), ref.getTargetVersionNumber());
			String note = record.getNote();
			if (note != null && !note.equals("")) {
				recordStr += NOTE_DELIMITER + WidgetEncodingUtil.encodeValue(note);
			}

		}
		return recordStr;
	}

	public static List<EntityGroupRecord> parseRecords(String recordStr) {
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		if (recordStr == null || "".equals(recordStr))
			return records;
		String[] entries = recordStr.split(LIST_DELIMITER);
		for (String entry : entries) {
			String[] parts = entry.split(NOTE_DELIMITER);
			if (parts.length <= 0)
				continue;
			EntityGroupRecord record = new EntityGroupRecord();
			if (parts[0] != null && !"".equals(parts[0])) {
				Reference ref = DisplayUtils.parseEntityVersionString(parts[0]);
				if (ref == null)
					continue;
				record.setEntityReference(ref);
			}
			if (parts.length > 1) {
				record.setNote(WidgetEncodingUtil.decodeValue(parts[1]));
			}
			if (record.getEntityReference() != null)
				records.add(record);
		}

		return records;
	}
}
