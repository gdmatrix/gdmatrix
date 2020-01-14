package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
public class BetweenDatesColumnRenderer extends ColumnRenderer implements Serializable
{
  private String startDatePropName;
  private String endDatePropName;
  private String dateFormat = "yyyyMMdd";
  private String resultYes;
  private String resultNo;

  public BetweenDatesColumnRenderer(String startDatePropName, String endDatePropName,
    String dateFormat, String resultYes, String resultNo)
  {
    this.startDatePropName = startDatePropName;
    this.endDatePropName = endDatePropName;
    this.dateFormat = dateFormat;
    this.resultYes = resultYes;
    this.resultNo = resultNo;
  }

  public Object getValue(String columnName, Object row)
  {
    String startDate = null;
    String endDate = null;

    Object obj = PojoUtils.getDeepStaticProperty(row, startDatePropName);
    if (obj != null)
      startDate = ((List<String>)obj).get(0);

    obj = PojoUtils.getDeepStaticProperty(row, endDatePropName);
    if (obj != null)
      endDate = ((List<String>)obj).get(0);

    Date sDate = TextUtils.parseUserDate(startDate, dateFormat);
    Date eDate = TextUtils.parseUserDate(endDate, dateFormat);
    Date today = TextUtils.parseUserDate(
      TextUtils.formatDate(new Date(), dateFormat), dateFormat);

    if (sDate != null && sDate.compareTo(today) <= 0 &&
      eDate != null && eDate.compareTo(today) >= 0 || 
      (sDate == null && eDate == null))
      return resultYes;
    else
      return resultNo;
  }

  @Override
  public boolean isValueEscaped()
  {
    return false;
  }

}
