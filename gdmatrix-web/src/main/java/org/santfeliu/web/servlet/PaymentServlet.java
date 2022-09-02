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
package org.santfeliu.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.matrix.workflow.Variable;
import org.matrix.workflow.WorkflowConstants;
import org.matrix.workflow.WorkflowManagerPort;
import org.matrix.workflow.WorkflowManagerService;
import org.santfeliu.util.MatrixConfig;


/**
 *
 * @author blanquepa
 */
public class PaymentServlet extends HttpServlet
{
  private static final String REF_ENT_PAGO_STC = "REF_ENT_PAGO_STC";
  private static final String REF_SAL_PAGO_STC = "REF_SAL_PAGO_STC";
  private static final String ORIGEN = "ORIGEN";
  private static final String INSTANCEID = "instanceid";

  protected static final Logger log = Logger.getLogger("PaymentServlet");

/**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    try
    {
      String refPayment = request.getParameter(REF_SAL_PAGO_STC);
      String origin = request.getParameter(ORIGEN);
      String instanceId = request.getParameter(INSTANCEID);
      if (refPayment != null && instanceId != null)
      {
        log.log(Level.INFO, "Registering payment reference {0} on instance {1} from {2}", new Object[]{refPayment, instanceId, origin});
        WorkflowManagerPort port = getWorkflowManagerPort();
        List<Variable> variables = port.getVariables(instanceId);
        int i = variables.size() - 1;
        boolean isWaitingResponse = false;
        while (i >= 0 && !isWaitingResponse)
        {
          Variable variable = variables.get(i);
          isWaitingResponse = REF_ENT_PAGO_STC.equals(variable.getName()) && variable.getValue() != null;
          i--;
        }
        if (isWaitingResponse)
        {
          List<Variable> list = new ArrayList();
          Variable var = new Variable();
          var.setName(REF_SAL_PAGO_STC);
          var.setType(WorkflowConstants.TEXT_TYPE);
          var.setValue(refPayment);
          list.add(var);
          port.processInstance(instanceId, list, false);
          log.log(Level.INFO,
            "Payment reference {0} registered successfully with result {1} on instance {2}",
            new Object[]{refPayment, list.size(), instanceId});
        }
        else
          log.log(Level.INFO, "Instance was not waiting a response");
      }
      else
        log.log(Level.WARNING,
          "Payment reference {0} with origin {1} not processed on instance {2}",
          new Object[]{refPayment, origin, instanceId});
    }
    catch (Exception ex)
    {
      log.log(Level.SEVERE, ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Payment Servlet v1.0";
  }

  private WorkflowManagerPort getWorkflowManagerPort()
    throws Exception
  {
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(WorkflowManagerService.class);
    String userId = MatrixConfig.getProperty("adminCredentials.userId");
    String password = MatrixConfig.getProperty("adminCredentials.password");
    return endpoint.getPort(WorkflowManagerPort.class, userId, password);
  }

}
