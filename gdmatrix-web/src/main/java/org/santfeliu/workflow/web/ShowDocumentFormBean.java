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
package org.santfeliu.workflow.web;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;


/**
 *
 * @author realor
 */
public class ShowDocumentFormBean extends FormBean implements Serializable
{
  transient private HtmlBrowser browser;
  private String message;
  private boolean IFrame;
  private boolean showPrintButton;

  public ShowDocumentFormBean()
  {
  }

  public void setBrowser(HtmlBrowser browser)
  {
    this.browser = browser;
  }

  public HtmlBrowser getBrowser()
  {
    return browser;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public String getMessage()
  {
    return message;
  }

  public boolean isIFrame()
  {
    return IFrame;
  }

  public void setIFrame(boolean IFrame)
  {
    this.IFrame = IFrame;
  }

  public boolean isShowPrintButton()
  {
    return showPrintButton;
  }

  public void setShowPrintButton(boolean showPrintButton)
  {
    this.showPrintButton = showPrintButton;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();

    message = "";
    browser = new HtmlBrowser();
    browser.setUrl("about:blank");

    Object value;
    value = parameters.get("message");
    if (value != null) message = String.valueOf(value);
    value = parameters.get("url");
    if (value != null) browser.setUrl(String.valueOf(value));
    value = parameters.get("iframe");
    if (value != null) IFrame = Boolean.parseBoolean(String.valueOf(value));
    value = parameters.get("showPrintButton");
    if (value != null) showPrintButton = Boolean.parseBoolean(String.valueOf(value));
    else showPrintButton = true;

    return "show_document_form";
  }

  @Override
  public Map submit()
  {
    HashMap variables = new HashMap();
    return variables;
  }

}
