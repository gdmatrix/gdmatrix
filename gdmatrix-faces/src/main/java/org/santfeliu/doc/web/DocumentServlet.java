package org.santfeliu.doc.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
