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
package org.santfeliu.security.web;

import cat.aoc.valid.ValidClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import org.matrix.signature.SignatureManagerPort;
import org.matrix.signature.SignatureManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.cms.CMSListener;
import org.santfeliu.security.util.SecurityUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.workflow.VariableListConverter;

/**
 * ValidServlet
 *
 * Processes all requests coming from the VALid service.
 * There are 2 types of requests this servlet can handle:
 * -Authentication: this process is started from ValidBean and LoginFormBean
 * -Signature: this process is started from SignatureFormBean
 *
 * @author realor
 */
public class ValidServlet extends HttpServlet
{
  public final static String AUTH_ACTION = "authentication";
  public final static String SIGN_ACTION = "signature";
  public final static String ACTION_ATTRIBUTE = "validAction";
  public final static String RETURN_PARAMS_ATTRIBUTE = "returnParams";
  public final static String SIGID_ATTRIBUTE = "validSigId";
  public final static String WF_INSTANCE_ATTRIBUTE = "wfInstanceId";
  public final static String WF_FORM_VAR_ATTRIBUTE = "wfFormVariable";
  public final static String ACCESS_TOKEN_ATTRIBUTE = "validAccessToken";

  public final static String URLPARAM_CODE = "code";
  public final static String URLPARAM_ERROR = "error";
	public final static String URLPARAM_STATE = "state";
  public final static String URLPARAM_ERROR_CANCELLED = "SESSION_CANCEL";

  private final ValidClient client = new ValidClient();

  @Override
  public void init() throws ServletException
  {
    client.setBaseUrl(MatrixConfig.getProperty("valid.baseUrl"));
    client.setClientId(MatrixConfig.getProperty("valid.clientId"));
    client.setClientSecret(MatrixConfig.getProperty("valid.clientSecret"));
    client.setRedirectUrl(MatrixConfig.getProperty("valid.redirectUrl"));
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    HttpSession session = request.getSession(true);
    UserSessionBean userSessionBean = UserSessionBean.getInstance(session);
    //String queryString = request.getQueryString();

    if (userSessionBean == null)
    {
      showError(request, response, "NO_SESSION");
      return;
    }

    String error = request.getParameter(URLPARAM_ERROR);
    if (error != null)
    {
      showError(request, response, error);
      return;
    }
    String validAction = (String)userSessionBean.getAttribute("validAction");

    try
    {
      if (AUTH_ACTION.equals(validAction))
      {
        if ("VALID".equals(userSessionBean.getLoginMethod()))
        {
          // already identified
        }
        else
        {
          String authorizationCode = request.getParameter(URLPARAM_CODE);

          if (authorizationCode == null) throw new Exception("Missing CODE");
            // Authorization

          JSONObject accessTokenObject = client.getAccessToken(authorizationCode);
          error = (String)accessTokenObject.get("error");
          if (error != null) throw new Exception(error);

          String accessToken = (String)accessTokenObject.get("access_token");
          JSONObject userInfoObject = client.getUserInfo(accessToken);

          String givenName = (String)userInfoObject.get("name");
          String surnames = (String)userInfoObject.get("surnames");
          if (surnames == null)
          {
            surnames = "";
            String surname1 = (String)userInfoObject.get("surname1");
            if (surname1 != null) surnames += surname1;
            String surname2 = (String)userInfoObject.get("surname2");
            if (surname2 != null) surnames += " " + surname2;
          }
          String displayName = (givenName + " " + surnames).trim();
          String identifier = (String)userInfoObject.get("identifier");
          identifier = identifier.toUpperCase();
          String email = (String)userInfoObject.get("email");
          String identifierType = (String)userInfoObject.get("indentifierType");
          String companyName = (String)userInfoObject.get("companyName");
          String CIF = (String)userInfoObject.get("companyId");
          String certBase64 = (String)userInfoObject.get("userCertificate");

          userSessionBean.loginMobile(identifier, "VALID", "#{validBean.logout}");
          userSessionBean.setDisplayName(displayName);
          userSessionBean.setGivenName(givenName);
          userSessionBean.setSurname(surnames);
          userSessionBean.setEmail(email);
          userSessionBean.setOrganizationName(companyName);
          userSessionBean.setCIF(CIF);
          userSessionBean.setAttribute(ACCESS_TOKEN_ATTRIBUTE, accessToken);
          if ("1".equals(identifierType))
          {
            userSessionBean.setNIF(identifier);
          }
          if (certBase64 != null)
          {
            byte[] bytes = Base64.getDecoder().decode(certBase64);

            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate certificate = (X509Certificate)
              cf.generateCertificate(new ByteArrayInputStream(bytes));

            Map attribs = SecurityUtils.getCertificateAttributes(certificate);
            boolean representant = false;
            if (CIF != null)
            {
              String repCIF =
                SecurityUtils.getRepresentantCIF((String)attribs.get("CN"));
              representant = repCIF != null && CIF.equalsIgnoreCase(repCIF);
            }
            userSessionBean.setRepresentant(representant);
          }
        }
      }
      else if (SIGN_ACTION.equals(validAction))
      {
        String signatureCode = request.getParameter(URLPARAM_CODE);
        if (signatureCode == null) throw new Exception("Missing CODE");

        // Signature
        String accessToken =
          (String)userSessionBean.getAttribute(ACCESS_TOKEN_ATTRIBUTE);
        JSONObject signResult =
          client.getBasicSignature(accessToken, signatureCode);

        String result;
        String status = (String)signResult.get("status");
        if ("ko".equals(status))
        {
          error = (String)signResult.get("error");
          if (error != null && error.toLowerCase().contains("cancel"))
          {
            result = "CANCEL";
          }
          else throw new Exception(error);
        }
        else // ok
        {
          String evidenceBase64 = (String)signResult.get("evidence");
          byte[] bytes = Base64.getDecoder().decode(evidenceBase64);

          // complete signature
          String sigId =
            (String)userSessionBean.getAttribute(SIGID_ATTRIBUTE);
          SignatureManagerPort signPort =
            getSignatureManagerPort(userSessionBean);
          signPort.addExternalSignature(sigId, bytes);
          result = "OK";
        }

        // set workflow variables
        String wfInstanceId =
          (String)userSessionBean.getAttribute(WF_INSTANCE_ATTRIBUTE);
        String wfFormVariable =
          (String)userSessionBean.getAttribute(WF_FORM_VAR_ATTRIBUTE);

        HashMap variables = new HashMap();
        variables.put("result", result);
        variables.put(wfFormVariable, WorkflowConstants.FORWARD_STATE);
        WorkflowManagerPort wfPort = getWorkflowManagerPort(userSessionBean);
        wfPort.setVariables(wfInstanceId,
          VariableListConverter.toList(variables));

        // reset attributes
        userSessionBean.setAttribute(SIGID_ATTRIBUTE, null);
        userSessionBean.setAttribute(WF_INSTANCE_ATTRIBUTE, null);
        userSessionBean.setAttribute(WF_FORM_VAR_ATTRIBUTE, null);
      }
      else
      {
        throw new Exception("INVALID_ACTION");
      }

      // return to faces page
      String returnParams =
        (String)userSessionBean.getAttribute(RETURN_PARAMS_ATTRIBUTE);

      String returnUrl = CMSListener.GO_URI + "?" + returnParams;
      response.sendRedirect(returnUrl);
    }
    catch (Exception ex)
    {
      String message = ex.getMessage();
      if (message == null) message = ex.toString();
      showError(request, response, message);
    }
    finally
    {
      userSessionBean.setAttribute(ACTION_ATTRIBUTE, null);
      userSessionBean.setAttribute(RETURN_PARAMS_ATTRIBUTE, null);
    }
  }

  private void showError(HttpServletRequest request,
    HttpServletResponse response, String message)
    throws IOException, ServletException
  {
    request.setAttribute("error", message);
    RequestDispatcher dispatcher =
      request.getRequestDispatcher("/common/security/valid_error.faces");
    dispatcher.forward(request, response);
  }

  private SignatureManagerPort getSignatureManagerPort(
    UserSessionBean userSessionBean)
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(SignatureManagerService.class);
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    return endpoint.getPort(SignatureManagerPort.class, userId, password);
  }

  private WorkflowManagerPort getWorkflowManagerPort(
    UserSessionBean userSessionBean)
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(WorkflowManagerService.class);
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    return endpoint.getPort(WorkflowManagerPort.class, userId, password);
  }
}
