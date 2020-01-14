package org.santfeliu.policy.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import org.matrix.dic.DictionaryConstants;
import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.policy.DocumentPolicy;
import org.matrix.policy.DocumentPolicyFilter;
import org.matrix.policy.DocumentPolicyView;
import org.matrix.policy.PolicyState;
import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.web.DocumentBean;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

/**
 *
 * @author realor
 */
@CMSManagedBean
public class DocumentPolicySearchBean extends BasicSearchBean
{
  private static final String DOC_SERVLET_URL = "/documents/";
  
  private Map<String, StateMapValue> statesMap;
  private DocumentPolicyFilter filter;
  private SelectItem[] stateSelectItems;
  private Locale locale;

  public DocumentPolicySearchBean()
  {
    locale = getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.policy.web.resources.PolicyBundle", locale);
    this.stateSelectItems =
      FacesUtils.getEnumSelectItems(PolicyState.class, bundle);

    statesMap = new HashMap();
    filter = new DocumentPolicyFilter();
  }

  public DocumentPolicyFilter getFilter()
  {
    return filter;
  }

  public void setFilter(DocumentPolicyFilter filter)
  {
    this.filter = filter;
  }

  public SelectItem[] getStateSelectItems()
  {
    if (!getLocale().equals(locale))
    {
      locale = getLocale();
      ResourceBundle bundle = ResourceBundle.getBundle(
        "org.santfeliu.policy.web.resources.PolicyBundle", locale);
      this.stateSelectItems =
        FacesUtils.getEnumSelectItems(PolicyState.class, bundle);
    }

    return this.stateSelectItems;
  }
  
  public void setStateSelectItems(SelectItem[] stateSelectItems)
  {
    this.stateSelectItems = stateSelectItems;
  }

  public Map<String, StateMapValue> getStatesMap()
  {
    return statesMap;
  }

  public void setStatesMap(Map<String, StateMapValue> statesMap)
  {
    this.statesMap = statesMap;
  }

  public PolicyState getRowState()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    String docPolicyId = docPolicyView.getDocPolicy().getDocPolicyId();
    
    StateMapValue stateMapValue = statesMap.get(docPolicyId);
    return (stateMapValue.newValue != null ? 
      stateMapValue.newValue : stateMapValue.oldDocPolicy.getState());
  }
  
  public void setRowState(PolicyState newValue)
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    String docPolicyId = docPolicyView.getDocPolicy().getDocPolicyId();

    StateMapValue stateMapValue = statesMap.get(docPolicyId);
    if (newValue.equals(stateMapValue.oldDocPolicy.getState()))
    {
      stateMapValue.newValue = null;
    }
    else
    {
      stateMapValue.newValue = newValue;
    }
  }

  public boolean isRowStateChanged()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    String docPolicyId = docPolicyView.getDocPolicy().getDocPolicyId();
    StateMapValue stateMapValue = statesMap.get(docPolicyId);

    return (stateMapValue != null && stateMapValue.newValue != null);
  }

  public String getFileTypeImage()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    
    Document document = docPolicyView.getDocument();
    if (document == null)
      return null;

    Content content = document.getContent();
    if (content != null)
    {
      return DocumentBean.getContentTypeIcon(content.getContentType());
    }
    else
    {
      return DocumentBean.getContentTypeIcon(null);
    }
  }

  public String getDocumentSize()
  {
    String result = " ";
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    Document document = docPolicyView.getDocument();
    if (document == null)
      return "";

    Content content = document.getContent();
    if (content != null && content.getSize() != null)
    {
      result = DocumentUtils.getSizeString(content.getSize());
    }

    return result;
  }

  public String getDocumentClassId()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    Document document = docPolicyView.getDocument();
    if (document == null)
      return null;

    List<String> classIds = document.getClassId();
    if (classIds != null && classIds.size() > 0)
      return TextUtils.collectionToString(classIds);
    else return null;
  }

  public String getDocumentUrl()
  {
    String url = null;
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    Document document = docPolicyView.getDocument();
    if (document == null)
      return null;

    String contextPath = getExternalContext().getRequestContextPath();
    String title = document.getTitle();
    if (title == null)
    {
      title = "";
    }
    Content content = document.getContent();
    if (content != null)
    {
      String mimeType = content.getContentType();
      String contentId = content.getContentId();
      String extension =
        MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
      String filename = DocumentUtils.getFilename(title) + "." + extension;

      url = contextPath + DOC_SERVLET_URL + contentId + "/" + filename;
    }
    return url;
  }

  public String searchPolicy()
  {
    return getControllerBean().searchObject("Policy",
      "#{documentPolicySearchBean.filter.policyId}");
  }
  
  @Override
  public int countResults()
  {
    try
    {
      return PolicyConfigBean.getPort().countDocumentPolicies(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  @Override
  public List getResults(int firstResult, int maxResults)
  {
    try
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);

      List<DocumentPolicyView> docPolicyViewList =
        PolicyConfigBean.getPort().findDocumentPolicyViews(filter);
      for (DocumentPolicyView docPolicyView : docPolicyViewList)
      {
        String key = docPolicyView.getDocPolicy().getDocPolicyId();
        DocumentPolicy docPolicy = docPolicyView.getDocPolicy();
        
        StateMapValue stateValue = statesMap.get(key);
        if (stateValue == null || stateValue.newValue == null)
        {
          stateValue = new StateMapValue();
          stateValue.oldDocPolicy = docPolicy;
          statesMap.put(key, stateValue);
        }
      }
      return docPolicyViewList;
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }


  @Override
  @CMSAction
  public String show()
  {
    return "document_policy_search";
  }

  public String showDocument()
  {
    DocumentPolicyView docPolicyView =
      (DocumentPolicyView) getValue("#{row}");
    String docId = docPolicyView.getDocument().getDocId();
    int version = docPolicyView.getDocument().getVersion();
    return getControllerBean().showObject(DictionaryConstants.DOCUMENT_TYPE,
      DocumentConfigBean.toObjectId(docId, version)); 
  }

  public String showPolicy()
  {
    return getControllerBean().showObject("Policy",
      (String)getValue("#{row.policy.policyId}"));
  }

  public String changeState() throws Exception
  {
    if (statesMap != null)
    {
      for (Map.Entry<String, StateMapValue> entry : statesMap.entrySet())
      {
        String docPolicyId = entry.getKey();
        StateMapValue value = entry.getValue();
        if (value != null && value.newValue != null)
        {
          String line = "Policy " + docPolicyId +
            ": State changed from " + value.oldDocPolicy.getState() +
            " to " + value.newValue;
          FacesMessage facesMessage =
            new FacesMessage(FacesMessage.SEVERITY_INFO, line, null);
          getFacesContext().addMessage(null, facesMessage);
          
          value.oldDocPolicy.setState(value.newValue);
          PolicyConfigBean.getPort().storeDocumentPolicy(value.oldDocPolicy);
          value.newValue = null;
        }
      }
    }
    return search();
  }

  public String cancelChanges()
  {
    statesMap.clear();
    return search();
  }

  public class StateMapValue implements Serializable
  {
    public DocumentPolicy oldDocPolicy;
    public PolicyState newValue;
  }
}
