package org.sagebionetworks.web.client.jsinterop.mui;

import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.react.HasStyle;

public class Grid extends HasStyle<GridProps> {

  public Grid() {
    super();
    this.props = GridProps.create(false);
  }

  @Override
  protected void onLoad() {
    renderComponent();
  }

  private void renderComponent() {
    ReactNode component = React.createElement(MaterialUI.Unstable_Grid2, props);
    this.render(component);
  }

  public void setId(String id) {
    props.id = id;
    renderComponent();
  }

  public void setContainer(boolean container) {
    props.container = container;
    renderComponent();
  }

  public void setXs(int xs) {
    props.xs = xs;
    renderComponent();
  }

  public void setSm(int sm) {
    props.sm = sm;
    renderComponent();
  }

  public void setMd(int md) {
    props.md = md;
    renderComponent();
  }

  public void setLg(int lg) {
    props.lg = lg;
    renderComponent();
  }

  public void setXl(int xl) {
    props.xl = xl;
    renderComponent();
  }

  public void setXsOffset(int xsOffset) {
    props.xsOffset = xsOffset;
    renderComponent();
  }

  public void setSmOffset(int smOffset) {
    props.smOffset = smOffset;
    renderComponent();
  }

  public void setMdOffset(int mdOffset) {
    props.mdOffset = mdOffset;
    renderComponent();
  }

  public void setLgOffset(int lgOffset) {
    props.lgOffset = lgOffset;
    renderComponent();
  }

  public void setXlOffset(int xlOffset) {
    props.xlOffset = xlOffset;
    renderComponent();
  }

  public void setMt(String mt) {
    props.mt = mt;
    renderComponent();
  }

  public void setPl(String pl) {
    props.pl = pl;
    renderComponent();
  }

  public void setRowSpacing(String rowSpacing) {
    props.rowSpacing = rowSpacing;
    renderComponent();
  }

  public void setColumnSpacing(String columnSpacing) {
    props.columnSpacing = columnSpacing;
    renderComponent();
  }
}
