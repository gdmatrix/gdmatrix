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
    return "custom_form";
  }

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
