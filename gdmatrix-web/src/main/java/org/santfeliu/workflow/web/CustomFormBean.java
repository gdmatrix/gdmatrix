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
import org.matrix.doc.Document;
import org.matrix.translation.TranslationConstants;
import org.santfeliu.doc.client.DocumentManagerClient;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.MatrixConfig;
import org.santfeliu.util.Properties;
import org.santfeliu.workflow.form.Form;

/**
 *
 * @author realor
 */
public class CustomFormBean extends FormBean implements Serializable
{
  private transient String url = null;
  private String type;
  private String ref;
  private Map newValues;
  private String translationGroup;
  private boolean translationEnabled;

  public CustomFormBean()
  {
  }

  public String getUrl()
  {
    if (url == null)
    {
      if ("url".equals(type))
      {
        url = ref;
      }
      else
      {
        url = getFormUrl();
      }
    }
    return url;
  }

  public void setNewValues(Map newValues)
  {
    this.newValues = newValues;
  }

  public Map getNewValues()
  {
    return newValues;
  }

  public String getTranslationGroup()
  {
    return translationGroup;
  }

  public void setTranslationEnabled(boolean translationEnabled)
  {
    this.translationEnabled = translationEnabled;
  }

  public boolean isTranslationEnabled()
  {
    return translationEnabled;
  }

  @Override
  public String show(Form form)
  {
    Properties parameters = form.getParameters();
    Object otype = parameters.get("type");
    Object oref = parameters.get("ref");
    if (otype != null && oref != null)
    {
      this.type = String.valueOf(otype);
      this.ref = String.valueOf(oref);
      this.translationGroup = type + ":" + ref;
    }
    this.newValues = new HashMap();
    if (this.url != null)
      this.url = null;

    return "custom_form";
  }

  @Override
  public Map submit()
  {
    return newValues;
  }

  private String getFormUrl()
  {
    String url = null;
    try
    {
      String userLanguage = FacesUtils.getViewLanguage();
      String formName = ref;
      String docLanguage = null;

      DocumentManagerClient client = getDocumentManagerClient();
      String docTypeId = "FORM";
      Document document = client.loadDocumentByName(docTypeId,
        "workflow." + type, formName, userLanguage, 0);
      if (document != null)
      {
        url = "http://localhost:"
         + MatrixConfig.getProperty("org.santfeliu.web.defaultPort")
         + getContextPath() + "/documents/" +
          document.getContent().getContentId(); // TODO: fix url
        docLanguage = document.getLanguage();
      }
      translationEnabled =
        TranslationConstants.UNIVERSAL_LANGUAGE.equals(docLanguage);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return url;
  }

  private DocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    String userId =
      MatrixConfig.getProperty("adminCredentials.userId");
    String password =
      MatrixConfig.getProperty("adminCredentials.password");

    return new DocumentManagerClient(userId, password);
  }
}
