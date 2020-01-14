package org.santfeliu.test.web;

import java.io.Serializable;
import java.util.Date;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import org.santfeliu.faces.FacesBean;
import org.santfeliu.faces.component.HtmlCalendar;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author blanquepa
 */
public class WidgetTestBean extends FacesBean implements Serializable
{
  private transient HtmlPanelGrid panel;

  public WidgetTestBean()
  {
    loadPanel();
  }

  public HtmlPanelGrid getPanel()
  {
    return panel;
  }

  public void setPanel(HtmlPanelGrid panel)
  {
    this.panel = panel;
  }

  public String show()
  {
    return "widget_test";
  }

  public String outcome()
  {
    return show();
  }

  public String outcomeNull()
  {
    return null;
  }

  public String addInputText()
  {
    int size = panel.getChildCount();
    HtmlInputText input = new HtmlInputText();
    input.setId("input_id_" + size);
    input.setValue("child #" + size);
    panel.getChildren().add(input);

    return null;
  }

  public String getSection()
  {
    return "5627";
  }


  public String getAgendaMid()
  {
    return "16372";
  }

  public String getTheme()
  {
    MenuItemCursor cursor =
      UserSessionBean.getCurrentInstance().getMenuModel().getMenuItem(getAgendaMid());
    String value = cursor.getProperty("filterPanel.theme");
    return value;
  }

  private void loadPanel()
  {
    panel = new HtmlPanelGrid();
    panel.setId("panel_id");

    //Add output
    HtmlPanelGroup group = new HtmlPanelGroup();
    group.setId("group_id");
    HtmlOutputText output = new HtmlOutputText();
    output.setId("output_id");
    output.setValue("Hoy es: ");
    group.getChildren().add(output);

    HtmlCalendar cal = new HtmlCalendar();
    cal.setId("cal_id");
    cal.setValue(TextUtils.formatDate(new Date(), "dd/MM/yyyy"));
    group.getChildren().add(cal);

    panel.getChildren().add(group);
  }
}
