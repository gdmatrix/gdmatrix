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
package org.santfeliu.webapp.modules.translation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.DictionaryConstants;
import org.matrix.translation.Translation;
import org.matrix.translation.TranslationState;
import org.santfeliu.faces.FacesUtils;
import static org.santfeliu.webapp.NavigatorBean.NEW_OBJECT_ID;
import org.santfeliu.webapp.ObjectBean;
import org.santfeliu.webapp.setup.ActionObject;

/**
 *
 * @author blanquepa
 */
@Named
@ViewScoped
public class TranslationObjectBean extends ObjectBean
{
  private Translation translation = new Translation();
  private String formSelector;

  @Inject
  TranslationTypeBean translationTypeBean;

  @Inject
  TranslationFinderBean translationFinderBean;

  @PostConstruct
  public void init()
  {
    System.out.println("Creating " + this);
  }

  @Override
  public String getRootTypeId()
  {
    return DictionaryConstants.TRANSLATION_TYPE;
  }

  @Override
  public TranslationTypeBean getTypeBean()
  {
    return translationTypeBean;
  }

  @Override
  public TranslationFinderBean getFinderBean()
  {
    return translationFinderBean;
  }

  public String getFormSelector()
  {
    return formSelector;
  }

  public void setFormSelector(String formSelector)
  {
    this.formSelector = formSelector;
  }

  @Override
  public String getDescription()
  {
    return isNew() ? "" : translationTypeBean.describe(translation);
  }

  @Override
  public Translation getObject()
  {
    return isNew() ? null : translation;
  }

  public Translation getTranslation()
  {
    return translation;
  }

  public void setTranslation(Translation cas)
  {
    this.translation = cas;
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

  public String getPropertyLabel(String propName, String altName)
  {
    return translationTypeBean.getPropertyLabel(translation, propName, altName);
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
        StringUtils.capitalize(locale.getDisplayLanguage(userLocale));
      SelectItem selectItem = new SelectItem();
      selectItem.setValue(language);
      selectItem.setLabel(displayLanguage);
      selectItems.add(selectItem);
    }
    return selectItems;
  }
  
  public SelectItem[] getStateSelectItems()
  {
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.translation.web.resources.TranslationBundle", getLocale());
    return FacesUtils.getEnumSelectItems(TranslationState.class, bundle);
  }  

  public String getDisplayLanguage()
  {
    String language = translation.getLanguage() != null ? 
      translation.getLanguage() : FacesUtils.getViewLocale().getLanguage();
    return getDisplayLanguage(language);
  }

  public String getDisplayLanguage(String language)
  {
    Locale locale = new Locale(language);
    return StringUtils
      .capitalize(locale.getDisplayLanguage(FacesUtils.getViewLocale()));
  }  

  @Override
  public void loadObject() throws Exception
  {
    formSelector = null;

    if (!NEW_OBJECT_ID.equals(objectId))
      translation = TranslationModuleBean.getPort(false)
        .loadTranslation(objectId);
    else
      translation = new Translation();
  }

  @Override
  public void storeObject() throws Exception
  {
    translation = TranslationModuleBean.getPort(false)
      .storeTranslation(translation);
    setObjectId(translation.getTransId());
    translationFinderBean.outdate();
    if (isTranslationClosed() && translationFinderBean.hasNext())
      translationFinderBean.viewNext();
  }
         
  @Override
  public void removeObject() throws Exception
  {
    TranslationModuleBean.getPort(false)
      .removeTranslation(translation.getTransId());
    translationFinderBean.outdate();
  }
  
  @Override
  protected void setActionResult(ActionObject result)
  {
    if (result != null)
    {
      if (result.getObject() != null)
        translation = (Translation) result.getObject();      
    }
  }  

  @Override
  public Serializable saveState()
  {
    return new Object[] { translation, formSelector };
  }

  @Override
  public void restoreState(Serializable state)
  {
    Object[] array = (Object[])state;
    this.translation = (Translation) array[0];
    this.formSelector = (String)array[1];
  }
  
  @Override
  public boolean isEditable()
  {    
    return true;
  }

}
