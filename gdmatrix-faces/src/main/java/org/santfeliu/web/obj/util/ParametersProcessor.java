package org.santfeliu.web.obj.util;

import java.io.Serializable;
import java.util.Map;
import org.santfeliu.web.WebBean;

/**
 *
 * @author blanquepa
 */
public abstract class ParametersProcessor extends WebBean implements Serializable
{
  public abstract String processParameters(Map parameters);
}
