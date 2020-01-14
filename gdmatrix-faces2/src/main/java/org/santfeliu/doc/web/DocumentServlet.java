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
package org.santfeliu.doc.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author unknown
 */
public class DocumentServlet extends HttpServlet
{
  private static final long DEFAULT_READER_CACHE_TIME = 10000; //10 seconds
  
  private DocumentWriter documentWriter;
  private DocumentReader documentReader;

  /**
   * 
   * @param config
   * @throws ServletException
   */
  @Override
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
    initDocumentReader(config);
    initDocumentWriter(config);
  }

  /** Handles the HTTP <code>GET</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doGet(HttpServletRequest request, 
                       HttpServletResponse response)
    throws ServletException, java.io.IOException
  {
    documentReader.processRequest(request, response, getServletInfo());
  }

  /** Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doPost(HttpServletRequest request, 
                        HttpServletResponse response)
    throws ServletException, java.io.IOException
  {
    documentWriter.processRequest(request, response);
  }

  /** Handles the HTTP <code>POST</code> method.
   * @param request servlet request
   * @param response servlet response
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, java.io.IOException
  {
    documentWriter.processUpload(request, response);
  }
  
  /**
   * Returns the description of this servlet.
   * @return
   */
  @Override
  public String getServletInfo()
  {
    return "Document Servlet 1.0";
  }
  
  /**
   * 
   */
  public void destroy()
  {
  }

  private void initDocumentReader(ServletConfig config)
  {
    long defaultCacheTime = DEFAULT_READER_CACHE_TIME;
    String time = config.getInitParameter("defaultCacheTime");
    if (time != null)
      defaultCacheTime = Long.parseLong(time);
      
    documentReader = new DocumentReader(defaultCacheTime);
  }
  
  private void initDocumentWriter(ServletConfig config)
  {
    String tempFilePath = config.getInitParameter("path");
    documentWriter = new DocumentWriter(tempFilePath);
  }
}
