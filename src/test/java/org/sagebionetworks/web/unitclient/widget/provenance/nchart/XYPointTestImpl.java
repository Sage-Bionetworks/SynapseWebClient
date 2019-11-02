package org.sagebionetworks.web.unitclient.widget.provenance.nchart;

import org.sagebionetworks.web.client.widget.provenance.nchart.XYPoint;

public class XYPointTestImpl implements XYPoint {

	int x;
	int y;

	public XYPointTestImpl(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

}
