package org.santfeliu.translation.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import java.util.ResourceBundle;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.matrix.translation.Translation;
import org.matrix.translation.TranslationState;
import org.matrix.translation.TranslationFilter;
import org.matrix.translation.TranslationManagerPort;
import org.matrix.translation.TranslationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.obj.BasicSearchBean;

@CMSManagedBean
public class TranslationBean extends BasicSearchBean
{
  private static final int VISIBLE_LENGTH = 50;
  private transient UIData dataTable;
  private int rowIndex; // index in current page
  private TranslationFilter filter;
  private Translation translation;

  public TranslationBean()
  {
    filter = new TranslationFilter();
    filter.setState(TranslationState.DRAFT);
  }
  
  public void setFilter(TranslationFilter filter)
  {
    this.filter = filter;
  }

  public TranslationFilter getFilter()
  {
    return filter;
  }

  public void setTranslation(Translation translation)
  {
    this.translation = translation;
  }

  public Translation getTranslation()
  {
    return translation;
  }

  public void setDataTable(UIData dataTable)
  {
    this.dataTable = dataTable;
  }

  public UIData getDataTable()
  {
    return dataTable;
  }

  public boolean isRowTranslationClosed()
  {
    Translation tr = (Translation)getValue("#{row}");
    return TranslationState.COMPLETED.equals(tr.getState());
  }

  public boolean isTranslationClosed()
  {
    return TranslationState.COMPLETED.equals(translation.getState());
  }

  public void setTranslationClosed(boolean closed)
  {
    translation.setState(closed ?
      TranslationState.COMPLETED : TranslationState.DRAFT);
  }

  public SelectItem[] getStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.translation.web.resources.TranslationBundle", getLocale());
    return FacesUtils.getEnumSelectItems(TranslationState.class, bundle);
  }

  public List<SelectItem> getLanguageSelectItems()
  {
    List<SelectItem> selectItems = new ArrayList();
    Locale userLocale = FacesUtils.getViewLocale();
    FacesContext context = FacesContext.getCurrentInstance();
    Iterator iter = context.getApplication().getSupportedLocales();
    while (iter.hasNext())
    {
      Locale locale = (Locale)iter.next();
      String language = locale.getLanguage();
      String displayLanguage = 
        locale.getDisplayLanguage(userLocale).toLowerCase();
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(language);
      selectItem.setLabel(displayLanguage);
      selectItems.add(selectItem);
    }
    return selectItems;
  }

  public String getDisplayLanguage()
  {
    String language = translation.getLanguage();
    Locale locale = new Locale(language);
    return locale.getDisplayLanguage(FacesUtils.getViewLocale());
  }
  
  public Date getReadDateTime()
  {
    return TextUtils.parseInternalDate(translation.getReadDateTime());
  }

  public Date getCreationDateTime()
  {
    return TextUtils.parseInternalDate(translation.getCreationDateTime());
  }

  public Date getModifyDateTime()
  {
    return TextUtils.parseInternalDate(translation.getModifyDateTime());
  }

  @CMSAction
  public String show()
  {
    return "translation_search";
  }

  @Override
  @CMSAction
  public String search()
  {
    translation = null;
    super.search();
    return "translation_search";      
  }

  public String getShortText()
  {
    Translation tRow = (Translation)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String shortText = "";
    if (tRow.getText().length() > VISIBLE_LENGTH)
    {
      shortText = tRow.getText().substring(0, VISIBLE_LENGTH) + "...";
    }
    else
    {
      shortText = tRow.getText();
    }
    return shortText;
  }

  public String getShortTranslation()
  {
    Translation tRow = (Translation)getFacesContext().getExternalContext().
      getRequestMap().get("row");
    String shortTranslation = "";
    if (tRow.getTranslation() != null)
    {
      if (tRow.getTranslation().length() > VISIBLE_LENGTH)
      {
        shortTranslation = tRow.getTranslation().substring(0, VISIBLE_LENGTH) + "...";
      }
      else
      {
        shortTranslation = tRow.getTranslation();
      }
    }
    return shortTranslation;
  }

  public int countResults()
  {
    try
    {
      return getPort().countTranslations(filter);
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return 0;
  }

  public List getResults(int firstResult, int maxResults)
  {
    try 
    {
      filter.setFirstResult(firstResult);
      filter.setMaxResults(maxResults);
      return getPort().findTranslations(filter);      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String showTranslation()
  {
    try 
    {
      rowIndex = dataTable.getRowIndex();
      translation = (Translation)dataTable.getRowData();
      return "translation_edit";
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String closeTranslation() throws Exception
  {
    try
    {
      translation.setState(TranslationState.COMPLETED);
      translation = getPort().storeTranslation(translation);
      if (nextTranslation())
        return null;
      else
        return search();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      error(ex);
    }
    return null;
  }
  
  public String storeTranslation() throws Exception
  {
    try
    {
      translation = getPort().storeTranslation(translation);
      return search();      
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String removeTranslation() throws Exception
  {
    try
    {
      getPort().removeTranslation(translation.getTransId());
      return search();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancel()
  {
    return search();
  }
  
  private boolean nextTranslation()
  {
    rowIndex++;
    if (rowIndex < getRows().size())
    {
      translation = (Translation)getRows().get(rowIndex);
      return true;
    }
    else
    {
      super.search(); // look for more
      rowIndex = 0;
      if (getRowCount() > 0)
      {
        translation = (Translation)getRows().get(rowIndex);
        return true;
      }
      else return false;
    }
  }
  
  private TranslationManagerPort getPort() throws Exception
  {
    UserSessionBean userSessionBean = UserSessionBean.getCurrentInstance();
    String userId = userSessionBean.getUsername();
    String password = userSessionBean.getPassword();
    
    WSDirectory dir = WSDirectory.getInstance();
    WSEndpoint endpoint = dir.getEndpoint(TranslationManagerService.class);
    return endpoint.getPort(TranslationManagerPort.class, userId, password);
  }
}
