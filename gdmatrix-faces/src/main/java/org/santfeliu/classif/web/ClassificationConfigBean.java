package org.santfeliu.classif.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;
import org.matrix.classif.Class;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class ClassificationConfigBean implements Serializable
{
  public static ClassificationManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint =
      wsDirectory.getEndpoint(ClassificationManagerService.class);
    return endpoint.getPort(ClassificationManagerPort.class,
      UserSessionBean.getCurrentInstance().getUserId(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

  public static String getDefaultDateTime()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    return df.format(date) + "000000";
  }

  public static String getClassStyle(Class classPeriod)
  {
    Date currentDate = new Date();
    Date date1 = TextUtils.parseInternalDate(classPeriod.getStartDateTime());
    Date date2 = TextUtils.parseInternalDate(classPeriod.getEndDateTime());

    if (date1 == null || date2 == null) return null;
    if (currentDate.after(date2)) return "past";
    if (currentDate.before(date1)) return "future";
    return "current";
  }
}
