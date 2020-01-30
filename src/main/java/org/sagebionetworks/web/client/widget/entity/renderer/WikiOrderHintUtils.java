package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiOrderHint;

public class WikiOrderHintUtils {

	public static void sortHeadersByOrderHint(List<V2WikiHeader> headerList, V2WikiOrderHint orderHint) {
		List<String> idList = orderHint.getIdList();
		if (idList == null)
			return;

		/*
		 * The header associated with the first ID found in the order hint will be moved to index 0 of the
		 * header list, the header associated with the second ID found in the order hint will be moved to
		 * index 2, etc. Consequently, headers with no associated ID will migrate towards the back of the
		 * header list. If there is no header associated with an ID in the order hint, it is ignored.
		 */
		int insertIndex = 0;
		for (int i = 0; i < idList.size(); i++) {
			for (int j = 0; j < headerList.size(); j++) {
				if ((headerList.get(j)).getId().equals(idList.get(i))) {
					// The header was in the order hint. Move that header towards the front.
					V2WikiHeader toMove = headerList.remove(j);
					headerList.add(insertIndex, toMove);
					insertIndex++;
				}
			}
		}
	}

}
