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
package org.santfeliu.cases.web.detail;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.matrix.cases.Case;
import org.matrix.dic.Property;
import org.matrix.report.ParameterDefinition;
import org.matrix.report.Report;
import org.matrix.report.ReportManagerPort;
import org.santfeliu.dic.util.DictionaryUtils;
import static org.santfeliu.report.web.ReportBean.CONNECTION_NAME_PROPERTY;
import org.santfeliu.report.web.ReportConfigBean;
import org.santfeliu.report.web.ReportServlet;
import org.santfeliu.security.util.Credentials;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.security.util.URLCredentialsCipher;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;

/**
 *
 * @author blanquepa
 */
public class ReportDetailPanel extends DetailPanel
{
  public static final String SHOW_IN_IFRAME_PROPERTY = "showInIFrame";  
  public static final String REPORT_NAME_PROPERTY = "reportName";
//  public static final String OUTPUT_FORMAT_PROPERTY = "outputFormat";
  public static final String SPREAD_REQUEST_PARAMETERS_PROPERTY = "spreadRequestParameters"; 
  public static final String ALLOWED_TAGS_PROPERTY = "allowedHtmlTags";
  
  private String url;
  private Case cas;
  

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  @Override
  public void loadData(DetailBean detailBean)
  {
    this.cas = ((CaseDetailBean) detailBean).getCase(); 
    
    String reportName = getReportName();
    if (reportName != null)
    {
      url = getContextURL() + "/reports/" + reportName + ".html" +
        getParametersString(detailBean);
      Credentials credentials = ReportConfigBean.getExecutionCredentials();
      URLCredentialsCipher cipher = SecurityUtils.getURLCredentialsCipher();
      url = cipher.putCredentials(url, credentials);
    }
  }

  @Override
  public boolean isRenderContent()
  {
    return getUrl() != null;
  }

  @Override
  public String getType()
  {
    return "report";
  }
  
  public boolean isShowInIFrame()
  {
    return "true".equalsIgnoreCase(getProperty(SHOW_IN_IFRAME_PROPERTY));
  }  
  
  public String getAllowedHtmlTags()
  {
    return getProperty(ALLOWED_TAGS_PROPERTY);
  }

  private String getParametersString(DetailBean detailBean)
  {
    try
    {
      StringBuilder buffer = new StringBuilder();
      
      String caseId = ((CaseDetailBean) detailBean).getCaseId(); 
      buffer.append(buffer.length() == 0 ? "?" : "&");
      buffer.append("caseId=").append(caseId);
      
      UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
      String NIF = userSessionBean.getNIF();
      if (NIF != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append("NIF=").append(NIF);
      }
      String CIF = userSessionBean.getCIF();
      if (CIF != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append("CIF=").append(CIF);
      }
      String username = userSessionBean.getUsername();
      buffer.append(buffer.length() == 0 ? "?" : "&");
      buffer.append("username=").append(URLEncoder.encode(username, "UTF-8"));
      
      for (Object e : getParameters().entrySet())
      {
        Map.Entry<String, String> entry = (Map.Entry<String, String>)e;
        String parameter = entry.getKey();
        String value = entry.getValue();
        if (value != null)
        {
          buffer.append(buffer.length() == 0 ? "?" : "&");
          buffer.append(parameter).append("=");
          buffer.append(URLEncoder.encode(value, "UTF-8"));
        }
        else
        {
          Property property = DictionaryUtils.getProperty(this.cas, parameter);
          if (property != null && property.getValue() != null && 
            !property.getValue().isEmpty())
          {
            buffer.append(buffer.length() == 0 ? "?" : "&");
            buffer.append(parameter).append("=");            
            String propValue = property.getValue().get(0);
            String urlEncodedValue = 
              propValue != null ? URLEncoder.encode(propValue, "UTF-8") : null;
            buffer.append(urlEncodedValue);            
          }
        }
      }
      
      String connectionName = getProperty(CONNECTION_NAME_PROPERTY);
      if (connectionName != null)
      {
        buffer.append(buffer.length() == 0 ? "?" : "&");
        buffer.append(ReportServlet.CONNECTION_NAME_PARAMETER + "=");
        buffer.append(connectionName);
      }
      
      //spread url parameters
      List<String> spreadParameters = getMultivaluedProperty(SPREAD_REQUEST_PARAMETERS_PROPERTY);
      if (spreadParameters != null)
      {
        Map requestParams = getExternalContext().getRequestParameterMap();
        for (String spParam : spreadParameters)
        {
          if (!spParam.equalsIgnoreCase("username")
           && !spParam.equalsIgnoreCase("CIF")
           && !spParam.equalsIgnoreCase("NIF"))  //avoid override of internal params
          {
            String value = (String) requestParams.get(spParam);
            buffer.append(buffer.length() == 0 ? "?" : "&");
            buffer.append(spParam).append("=");
            buffer.append(value);          
          }
        }
      }
      
      return buffer.toString();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private Map getParameters()
  {
    try
    {
      return getReportDefaultParameters(getReportName());
    }
    catch (Exception ex)
    {
      error(ex);
      return new HashMap();
    }
  }
  
  private Map getReportDefaultParameters(String reportName) throws Exception
  {
    Map defaultParams = new HashMap();
    ReportManagerPort port = ReportConfigBean.getReportManagerPort(
      ReportConfigBean.getReportAdminCredentials());
    Report report = port.loadReport(reportName, false);
    List<ParameterDefinition> paramDefs = report.getParameterDefinition();
    for (ParameterDefinition paramDef : paramDefs)
    {
      defaultParams.put(paramDef.getName(), paramDef.getDefaultValue());
    }
    return defaultParams;
  }  

  private String getReportName()
  {
    return getProperty(REPORT_NAME_PROPERTY);
  }
  
}
