package org.santfeliu.misc.client.web;

import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSProperty;

/**
 *
 * @author blanquepa
 */
public class MatrixClientBean extends WebBean
{
  @CMSProperty
  public static final String HELP_URL = "matrixClientHelpUrl";
  
  public String getHelpUrl()
  {
    String helpProp = getProperty(HELP_URL);
    if (helpProp == null) return null;
    
    String address = UserSessionBean.getCurrentInstance().getRemoteAddress();
    if (!helpProp.contains("?"))
    {
      return helpProp + "?ip=" + address;
    }
    else
    {
      return helpProp + "&ip=" + address;      
    }
  }
}
