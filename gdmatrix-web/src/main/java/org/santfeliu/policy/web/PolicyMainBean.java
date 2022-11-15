/*
 * GDMatrix
 *
 * Copyright (C) 2020, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.santfeliu.policy.web;

import java.util.Date;
import java.util.List;
import org.matrix.dic.DictionaryConstants;
import org.matrix.policy.Policy;
import org.matrix.policy.PolicyManagerPort;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSProperty;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;

/**
 *
 * @author realor
 */
public class PolicyMainBean extends DynamicTypifiedPageBean
{
  @CMSProperty
  public static final String HELP_URL_PROPERTY = "help_url";

  private Policy policy;

  public PolicyMainBean()
  {
    super(DictionaryConstants.POLICY_TYPE, "POLICY_ADMIN");
  }

  public Policy getPolicy()
  {
    return policy;
  }

  public void setPolicy(Policy policy)
  {
    this.policy = policy;
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(policy.getCreationDateTime());
  }

  public Date getChangeDateTime()
  {
    return TextUtils.parseInternalDate(policy.getChangeDateTime());
  }

  @Override
  public String show()
  {
    return "policy_main";
  }

  @Override
  public String store()
  {
    try
    {
      // apply setters
      policy.setPolicyTypeId(getCurrentTypeId());
      policy.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        policy.getProperty().addAll(properties);

      PolicyManagerPort port = PolicyConfigBean.getPort();
      policy = port.storePolicy(policy);
      setObjectId(policy.getPolicyId());

      setFormDataFromProperties(policy.getProperty());
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return "policy_main";
  }

  @Override
  protected void load()
  {
    if (isNew())
    {
      policy = new Policy();
    }
    else
    {
      try
      {
        PolicyManagerPort port = PolicyConfigBean.getPort();
        policy = port.loadPolicy(getObjectId());
      }
      catch (Exception ex)
      {
        getObjectBean().clearObject();
        error(ex);
        policy = new Policy();
      }
      setCurrentTypeId(policy.getPolicyTypeId());
      setFormDataFromProperties(policy.getProperty());
    }
  }

  public String getHelpUrl()
  {
    String helpUrl = getProperty(HELP_URL_PROPERTY);
    if (helpUrl == null) helpUrl = "/common/policy/help.jsp";

    return helpUrl;
  }

  public String searchType()
  {
    return searchType("#{policyMainBean.currentTypeId}");
  }

  public String showType()
  {
    return getControllerBean().showObject("Type", getCurrentTypeId());
  }

  public boolean isRenderShowTypeButton()
  {
    return getCurrentTypeId() != null && getCurrentTypeId().trim().length() > 0;
  }

  public String getScripts()
  {
    StringBuilder buffer = new StringBuilder();
    addScriptFile("/plugins/codemirror/codemirror.js", buffer);
    addScriptFile("/plugins/codemirror/javascript.js", buffer);
    addScriptFile("/plugins/codemirror/runmode.js", buffer);
    addScriptFile("/plugins/codemirror/matchbrackets.js", buffer);
    buffer.append("<script type=\"text/javascript\">var editor1 = " +
      "document.getElementById(\"activationDateExpression\");var cm1 = " +
      "CodeMirror.fromTextArea(editor1, " +
      "{lineNumbers:false, matchBrackets: true, lineWrapping: true});</script>");
    buffer.append("<script type=\"text/javascript\">var editor2 = " +
      "document.getElementById(\"activationCondition\");var cm2 = " +
      "CodeMirror.fromTextArea(editor2, " +
      "{lineNumbers:false, matchBrackets: true, lineWrapping: true});</script>");
    return buffer.toString();
  }

  private void addScriptFile(String path, StringBuilder buffer)
  {
    String contextPath = getContextPath();
    buffer.append("<script src=\"").append(contextPath);
    buffer.append(path);
    buffer.append("\" type=\"text/javascript\">\n</script>\n");
  }
}
