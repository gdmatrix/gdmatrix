package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.util.PojoUtils;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author blanquepa
 */
  public class DateTimeRowStyleClassGenerator extends RowStyleClassGenerator
    implements Serializable
  {
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String DATETIME_FORMAT = "yyyyMMddHHmmss";
    private static final String DEFAULT_START_PROPNAME = "startDate";
    private static final String DEFAULT_END_PROPNAME = "endDate";    
    
    private String startDatePropName;
    private String endDatePropName;
    private String dateFormat;
    private String allowedClasses;

    public DateTimeRowStyleClassGenerator(String startDatePropName,
      String endDatePropName, String allowedClasses)
    {
      if (startDatePropName == null)
        this.startDatePropName = DEFAULT_START_PROPNAME;
      else
        this.startDatePropName = startDatePropName;
      
      if (endDatePropName == null)
        this.endDatePropName = DEFAULT_END_PROPNAME;
      else
        this.endDatePropName = endDatePropName;
      
      this.allowedClasses = allowedClasses != null ? allowedClasses :
        "before,after,between";
    }    

    public DateTimeRowStyleClassGenerator(String startDatePropName,
      String endDatePropName, String dateFormat, String allowedClasses)
    {
      this(startDatePropName, endDatePropName, allowedClasses);
      this.dateFormat = dateFormat;
    }    

    @Override
    public String getStyleClass(Object row)
    {
      String startDate = "";
      String endDate = "";

      String[] startDateProps = startDatePropName.split(",");
      for (String propName : startDateProps)
      {
        Object obj = PojoUtils.getDeepStaticProperty(row, propName);
        if (obj != null)
        {
          if (obj instanceof List)
            startDate = startDate + ((List<String>)obj).get(0);
          else
            startDate = startDate + String.valueOf(obj);
        }
      }

      String[] endDateProps = endDatePropName.split(",");
      for (String propName : endDateProps)
      {
        Object obj = PojoUtils.getDeepStaticProperty(row, propName);
        if (obj != null)
        {
          if (obj instanceof List)
            endDate = endDate + ((List<String>)obj).get(0);
          else
            endDate = endDate + String.valueOf(obj);
        }
      }

      if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate) && allowedClasses.contains("no_data"))
        return "no_data";
      
      if (dateFormat == null && !StringUtils.isBlank(startDate))
      {
        if (startDate.length() == 14)
          dateFormat = DATETIME_FORMAT;
        else if (startDate.length() == 12)
          dateFormat = DATETIME_FORMAT;
        else
          dateFormat = DATE_FORMAT;
      }
      else
        dateFormat = DATE_FORMAT;
      
      Date sDate = TextUtils.parseUserDate(startDate, dateFormat);
      Date eDate = TextUtils.parseUserDate(endDate, dateFormat);
      Date today = TextUtils.parseUserDate(
        TextUtils.formatDate(new Date(), dateFormat), dateFormat);

      if (sDate != null && sDate.compareTo(today) <= 0 &&
        eDate != null && eDate.compareTo(today) >= 0 ||
        (sDate == null && eDate == null) ||
        (eDate == null && sDate.compareTo(today) <= 0))
        return allowedClasses.contains("between") ? "between" : null;
      else if (eDate != null && eDate.compareTo(today) < 0)
        return allowedClasses.contains("before") ? "before" : null;
      else if (sDate != null && sDate.compareTo(today) > 0 ||
        (eDate == null && sDate.compareTo(today) > 0))
        return allowedClasses.contains("after") ? "after" : null;
      else
        return null;
    }
  }

