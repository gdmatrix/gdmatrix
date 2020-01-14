package org.santfeliu.cases.web.detail;

import java.io.StringReader;
import java.io.StringWriter;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 * Implemented by panels that shows results on a dataTable.
 * 
 * @author blanquepa
 */
public abstract class TabulatedDetailPanel extends DetailPanel
{
  public static final String TABLE_SUMMARY_PROPERTY = "tableSummary";
  
  private String tableSummary;

  public String getTableSummary()
  {
    if (this.tableSummary != null)
      return tableSummary;
    else
    {
      String tableSummary = getProperty(TABLE_SUMMARY_PROPERTY);
      Translator translator = UserSessionBean.getCurrentInstance().getTranslator();
      String translationGroup = UserSessionBean.getCurrentInstance().getTranslationGroup();
      if (translator != null)
      {
        String userLanguage = FacesUtils.getViewLanguage();
        StringWriter sw = new StringWriter();
        try
        {
          translator.translate(new StringReader(tableSummary), sw, "text/plain",
            userLanguage, translationGroup);
          this.tableSummary = sw.toString();
        }
        catch (Exception ex)
        {
          //Summary not translated
          this.tableSummary = tableSummary;
        }
      }    
    }
    return this.tableSummary;
  }
 
}
