package org.santfeliu.cases.web.detail;

import java.util.Date;
import org.matrix.cases.Case;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class MainDetailPanel extends DetailPanel
{
  private CaseDetailBean detailBean;

  @Override
  public void loadData(DetailBean detailBean)
  {
    this.detailBean = (CaseDetailBean) detailBean;
  }

  public Case getCase()
  {
    if (detailBean != null)
      return detailBean.getCase();
    else
      return null;
  }

  public String getStartDate()
  {
    Case cas = getCase();
    if (cas != null)
      return formatDate(cas.getStartDate());
    else
      return null;
  }

  public String getStartTime()
  {
    Case cas = getCase();
    if (cas != null)
      return formatTime(cas.getStartTime());
    else
      return null;
  }

  public String getEndDate()
  {
    Case cas = getCase();
    if (cas != null)
      return formatDate(cas.getEndDate());
    else
      return null;
  }

  public String getEndTime()
  {
    Case cas = getCase();
    if (cas != null)
      return formatTime(cas.getEndTime());
    else
      return null;
  }

  public String getCaseType()
  {
    Case cas = getCase();
    if (cas != null)
    {
      Type type = TypeCache.getInstance().getType(cas.getCaseTypeId());
      if (type != null)
        return type.getDescription();
    }

    return null;
  }

  @Override
  public boolean isRenderContent()
  {
    return true;
  }

  public String getType()
  {
    return "main";
  }

  private String formatDate(String date)
  {
    if (date != null)
    {
      Date d = TextUtils.parseInternalDate(date);
      return TextUtils.formatDate(d, "dd/MM/yyyy");
    }
    return null;    
  }

  private String formatTime(String time)
  {
    if (time != null && !time.equals("000000"))
    {
      Date t = TextUtils.parseUserDate(time, "HHmmss");
      return TextUtils.formatDate(t, "HH:mm");
    }
    return null;
  }
  
}
