package org.santfeliu.survey.web;

import org.matrix.survey.SurveyManagerPort;
import org.matrix.survey.SurveyManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.web.UserSessionBean;

/**
 *
 * @author lopezrj
 */
public class SurveyConfigBean
{
  public static SurveyManagerPort getPort() throws Exception
  {
    WSDirectory wsDirectory = WSDirectory.getInstance();
    WSEndpoint endpoint = wsDirectory.getEndpoint(SurveyManagerService.class);
    return endpoint.getPort(SurveyManagerPort.class,
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());
  }

}
