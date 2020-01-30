package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtil {

	public static List<PaginationEntry> getPagination(int nResults, int start, int nPerPage, int nPagesToShow) {
		List<PaginationEntry> list = new ArrayList<PaginationEntry>();
		if (nResults < 0 || start < 0 || nPerPage < 1 || nPagesToShow < 1) {
			throw new IllegalArgumentException();
		}

		int nPages = new Double(Math.ceil((double) nResults / nPerPage)).intValue();
		int currentPage = new Double(Math.ceil((double) start / nPerPage)).intValue() + 1;

		// generally put the current one in the center or one right of center if nPagesToShow is even
		int leftPadding = nPerPage / 2;
		int firstPage = currentPage - leftPadding;
		int lastPage = currentPage + (nPagesToShow - leftPadding);

		// shift right if current page less than middle
		while (firstPage < 1) {
			firstPage++;
			lastPage++;
		}

		if (lastPage > nPages) {
			lastPage = nPages;
		}
		while (lastPage - firstPage >= nPagesToShow) {
			lastPage--;
		}

		// add prev if needed
		if (firstPage > 1) {
			list.add(new PaginationEntry("Prev", getNewStart(currentPage - 1, nPerPage), false));
		}

		// create entries
		for (int i = firstPage; i <= lastPage; i++) {
			boolean current = i == currentPage ? true : false;
			int pageStart = getNewStart(i, nPerPage);
			list.add(new PaginationEntry(i + "", pageStart, current));
		}

		// add next if needed
		if (lastPage < nPages) {
			list.add(new PaginationEntry("Next", getNewStart(currentPage + 1, nPerPage), false));
		}

		return list;
	}

	private static int getNewStart(int page, int nPerPage) {
		return (page - 1) * nPerPage;
	}
}
