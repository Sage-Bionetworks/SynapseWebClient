package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;

public class NChartCharactersTestImpl implements NChartCharacters {
	List<String> characters = new ArrayList<String>();

	@Override
	public void addCharacter(String characterId) {
		characters.add(characterId);
	}

}
