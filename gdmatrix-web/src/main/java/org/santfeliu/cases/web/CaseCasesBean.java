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
package org.santfeliu.cases.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.model.SelectItem;
import org.matrix.cases.Case;
import org.matrix.cases.CaseCase;
import org.matrix.cases.CaseCaseFilter;
import org.matrix.cases.CaseCaseView;
import org.matrix.cases.CaseManagerPort;
import org.matrix.dic.DictionaryConstants;
import org.matrix.dic.PropertyDefinition;
import org.santfeliu.cases.CaseCaseCache;
import org.santfeliu.dic.Type;
import org.santfeliu.dic.TypeCache;
import org.santfeliu.dic.util.ObjectDumper;
import org.santfeliu.util.TextUtils;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.obj.DynamicTypifiedPageBean;
import org.santfeliu.web.obj.TypifiedPageBean;

/**
 *
 * @author blanquepa
 * @author lopezrj
 */
public class CaseCasesBean extends DynamicTypifiedPageBean
{
  //Dic Case properties
  public static final String ALL_TYPES_VISIBLE_PROPERTY = "_casesAllTypesVisible";  
  public static final String ROW_TYPE_ID_PROPERTY = "_casesRowTypeId";  
  public static final String ROOT_TYPE_ID_PROPERTY = "_caseRootTypeId";
  public static final String GROUPBY_PROPERTY = "_casesGroupBy";
  public static final String GROUP_SELECTION_MODE_PROPERTY = 
    "_casesGroupSelectionMode";
  public static final String ORDERBY_PROPERTY = "_casesOrderBy";
  public static final String CASECASE_PAGE_SIZE_PROPERTY = "_casePageSize";
  public static final String SHOW_DUPLICATES_PROPERTY = "_casesShowDuplicates";
  public static final String ENABLE_REVERSE_EDITION_PROPERTY = 
    "_enableCaseCaseReverseEdition";
  public static final String HELP_PROPERTY = "_casesHelp";

  //Dic CaseCase properties
  public static final String DIRECT_DESCRIPTION_PROPERTY = "_directDescription";
  public static final String REVERSE_DESCRIPTION_PROPERTY =
    "_reverseDescription";
  public static final String SOURCE_TYPE_ID_PROPERTY = "_sourceTypeId";
  public static final String TARGET_TYPE_ID_PROPERTY = "_targetTypeId";
  public static final String DIRECT_SHOW_PROPERTIES_PROPERTY = "_directCaseCasesProperties";
  public static final String REVERSE_SHOW_PROPERTIES_PROPERTY = "_reverseCaseCasesProperties";  
  public static final String DIRECTIONAL_SPLIT_PROPERTY = "_directionalSplit";

  //Dic Case & CaseCase properties
  public static final String SHOW_PROPERTIES_PROPERTY = "_caseCasesProperties";  
  
  private static final String REVERSE_CASECASETYPE_PREFIX = "rev_";
  private static final String REVERSE_CASECASELABEL_PREFIX = "(INV) ";
  
  private CaseCase editingCase;
  List<CaseCaseView> rows;
    
  private int pageSize;  
  
  private String selectedCaseId;
  private String selectedTypeId; //with rev_ prefix if needed
  
  private List<SelectItem> allTypeItems = null;  

  private String lastCaseCaseMid = null;
  private int objectPageScroll;
  
  public CaseCasesBean()
  {
    super(DictionaryConstants.CASE_CASE_TYPE, "CASE_ADMIN", false);    
  }
  
  public CaseCase getEditingCase()
  {
    return editingCase;
  }

  public void setEditingCase(CaseCase editingCase)
  {
    this.editingCase = editingCase;
  }

  public List<CaseCaseView> getAllRows()
  {
    return rows;
  }

  public String getSelectedCaseId()
  {
    return selectedCaseId;
  }

  public void setSelectedCaseId(String selectedCaseId)
  {
    this.selectedCaseId = selectedCaseId;
  }

  public String getSelectedTypeId()
  {
    return selectedTypeId;
  }

  public void setSelectedTypeId(String selectedTypeId)
  {
    this.selectedTypeId = selectedTypeId;
  }
  
  public int getObjectPageScroll()
  {
    return objectPageScroll;
  }

  public void setObjectPageScroll(int objectPageScroll)
  {
    this.objectPageScroll = objectPageScroll;
  }  

  @Override
  public String show()
  {
    objectPageScroll = 0;
    String mid = UserSessionBean.getCurrentInstance().getSelectedMid();
    if (!mid.equals(lastCaseCaseMid))
    {
      load();
    }
    lastCaseCaseMid = mid;
    refreshTypes();
    return "case_cases";
  }
  
  @Override
  public String store()
  {
    if (editingCase != null)
    {
      storeCase();
    }
    else
    {
      load();
    }
    return show();
  }

  public String changeCase()
  {
    refreshTypes();
    return null;
  }
  
  public String changeType()
  {
    setCurrentTypeId();
    return null;
  }
  
  public String showCase()
  {
    String caseId = null;
    CaseCaseView row = (CaseCaseView)getValue("#{row}");
    if (isReverseRow(row))
      caseId = row.getMainCase().getCaseId();
    else
      caseId = row.getRelCase().getCaseId();

    return getControllerBean().showObject("Case", caseId);
  }

  @Override
  public String showGroup()
  {    
    reset();
    return super.showGroup();
  }  
  
  public String searchCase()
  {
    return getControllerBean().searchObject("Case",
      "#{caseCasesBean.selectedCaseId}");
  }

  public String createCase()
  {
    reset();
    editingCase = new CaseCase();
    return null;
  }

  public String removeCase()
  {
    try
    {
      CaseCaseView row = (CaseCaseView)getRequestMap().get("row");
      preRemove();
      CaseManagerPort port = CaseConfigBean.getPort();
      port.removeCaseCase(row.getCaseCaseId());
      CaseCaseCache caseCaseCache = CaseCaseCache.getInstance();
      caseCaseCache.clear(row.getMainCase().getCaseId()); //main case
      caseCaseCache.clear(row.getRelCase().getCaseId()); //related case
      getViewPropertiesMap().remove(row.getCaseCaseId());
      postRemove();
      reset();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String storeCase()
  {
    try
    {  
      objectPageScroll = 0;
      //preStore();      
      if (isReverseEditingCase()) // reverse casecase -> invert caseId fields
      {
        editingCase.setCaseId(selectedCaseId);
        editingCase.setRelCaseId(getObjectId());
      }
      else // direct casecase
      {
        editingCase.setCaseId(getObjectId());
        editingCase.setRelCaseId(selectedCaseId);        
      }      
             
      Type type = getCurrentType();
      if (type != null)
        editingCase.setCaseCaseTypeId(type.getTypeId());      
      
      if (editingCase.getCaseCaseTypeId() == null)
      {
        editingCase.setCaseCaseTypeId(getRootTypeId());
      }
      
      preStore();
      
      editingCase.getProperty().clear();
      List properties = getFormDataAsProperties();
      if (properties != null)
        editingCase.getProperty().addAll(properties);      
      
      CaseManagerPort port = CaseConfigBean.getPort();      
      editingCase = port.storeCaseCase(editingCase);
      CaseCaseCache caseCaseCache = CaseCaseCache.getInstance();
      caseCaseCache.clear(editingCase.getCaseId()); //main case
      caseCaseCache.clear(editingCase.getRelCaseId()); //related case
      getViewPropertiesMap().remove(editingCase.getCaseCaseId());
      postStore();
      reset();
      load();
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return null;
  }
  
  public String editCase()
  {
    try
    {
      CaseCaseView row = (CaseCaseView)getExternalContext().
        getRequestMap().get("row");
      String caseCaseId = row.getCaseCaseId();
      if (caseCaseId != null)
      {
        editingCase = 
          CaseConfigBean.getPort().loadCaseCase(caseCaseId);
        setCurrentTypeId(editingCase.getCaseCaseTypeId());        
        setFormDataFromProperties(editingCase.getProperty());
        if (isReverseRow(row))
        {
          selectedCaseId = editingCase.getCaseId();
          selectedTypeId = REVERSE_CASECASETYPE_PREFIX + editingCase.getCaseCaseTypeId();          
        }
        else
        {
          selectedCaseId = editingCase.getRelCaseId();
          selectedTypeId = editingCase.getCaseCaseTypeId();
        }
      }
      else
      {
        reset();
        editingCase = new CaseCase();
      }
      allTypeItems = null;      
    }
    catch(Exception ex)
    {
      error(ex);
    }
    return null;
  }

  public String cancelCase()
  {
    objectPageScroll = 0;    
    reset();
    return null;
  }
  
  @Override
  public boolean isModified()
  {
    return editingCase != null;
  }  

  public List<SelectItem> getCaseSelectItems()
  {
    CaseBean caseBean = (CaseBean)getBean("caseBean");
    return caseBean.getSelectItems(selectedCaseId);
  }

  public int getRowCount()
  {
    return (isRowsEmpty() ? 0 : getRows().size());
  }

  protected void load()
  {
    try
    {
      editingCase = null;
      if (!isNew())
      {
        CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
        Case cas = caseMainBean.getCase();
        Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
        if (caseType != null)
        {
          loadPropertyDefinitions(caseType);
        }
        preLoad();
        rows = getResults(0, 0);
        loadViewPropertiesMap(rows);
        postLoad();
        setGroups(rows, getGroupExtractor());        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public String getViewStartDate()
  {
    String date = "";
    CaseCaseView row = (CaseCaseView)getValue("#{row}");
    if (row != null)
    {
      date = row.getStartDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public String getViewEndDate()
  {
    String date = "";
    CaseCaseView row = (CaseCaseView)getValue("#{row}");
    if (row != null)
    {
      date = row.getEndDate();
      date = TextUtils.formatDate(
        TextUtils.parseInternalDate(date), "dd/MM/yyyy");
    }
    return date;
  }

  public boolean isReverseRelation()
  {
    CaseCaseView row = (CaseCaseView)getValue("#{row}");
    return isReverseRow(row);
  }
  
  public boolean isRenderEditButton()
  {
    return !isReverseRelation() || 
      (isReverseRelation() && isReverseEditionEnabled());
  }
  
  @Override
  public List<SelectItem> getAllTypeItems()
  {
    if (allTypeItems == null) 
    {
      try
      {
        List<SelectItem> result = new ArrayList<SelectItem>();
        if (editingCase != null && selectedCaseId != null && !selectedCaseId.isEmpty())
        {
          CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
          Case mainCase = caseMainBean.getCase();
          String mainCaseTypeId = mainCase.getCaseTypeId();
          Case selectedCase = CaseConfigBean.getPort().loadCase(selectedCaseId);
          String selectedCaseTypeId = selectedCase.getCaseTypeId();

          TypeCache typeCache = TypeCache.getInstance();
          if (!DictionaryConstants.CASE_CASE_TYPE.equals(rootTypeId))
          {            
            List<SelectItem> auxSelectItems = super.getAllTypeItems();          
            result.addAll(filterCaseCaseSelectItems(auxSelectItems, 
              typeCache, mainCaseTypeId, selectedCaseTypeId, false));
          }
          else
          {
            addBasicCaseCaseTypeId(result, false);
          }

          if (isReverseEditionEnabled())
          {
            
            String oldRootTypeId = rootTypeId; //trick for the super.getAllTypeItems() method
            try
            {
              rootTypeId = getRootTypeId(selectedCaseTypeId);
              if (!DictionaryConstants.CASE_CASE_TYPE.equals(rootTypeId))
              {
                List<SelectItem> auxSelectItems = super.getAllTypeItems();              
                result.addAll(filterCaseCaseSelectItems(auxSelectItems, 
                  typeCache, mainCaseTypeId, selectedCaseTypeId, true));              
              }
              else
              {
                addBasicCaseCaseTypeId(result, true);
              }            
            }
            finally
            {
              rootTypeId = oldRootTypeId;
            }
            addReverseLabelPrefix(result);
          }
        }        
        allTypeItems = result;
      }
      catch (Exception ex)
      {
        error(ex);
      }      
    }
    return allTypeItems;
  }  
  
  private void addBasicCaseCaseTypeId(List<SelectItem> result, boolean reverse)
  {    
    TypeCache typeCache = TypeCache.getInstance();        
    org.santfeliu.dic.Type type = 
      typeCache.getType(DictionaryConstants.CASE_CASE_TYPE);
    
    SelectItem item = new SelectItem();    
    item.setLabel(type.getDescription());
    item.setValue((reverse ? REVERSE_CASECASETYPE_PREFIX : "") + type.getTypeId());
    item.setDescription(type.getDescription());
    result.add(item);
  }
  
  private List<SelectItem> filterCaseCaseSelectItems(List<SelectItem> selectItems, 
    TypeCache typeCache, String mainCaseTypeId, String selectedCaseTypeId, 
    boolean reverse)
  {
    List<SelectItem> result = new ArrayList();
    for (SelectItem selectItem : selectItems)
    {      
      String caseCaseTypeId = (String)selectItem.getValue();
      if (isMatchRelation(typeCache, caseCaseTypeId, mainCaseTypeId, selectedCaseTypeId, reverse))
      {
        String directionalLabel = getCaseCaseDirectionalLabel(caseCaseTypeId, reverse);
        if (directionalLabel != null)
        {
          selectItem.setLabel(directionalLabel);
        }
        if (reverse) 
        {
          selectItem.setValue(REVERSE_CASECASETYPE_PREFIX + selectItem.getValue());          
        }
        result.add(selectItem);
      }
    }
    return result;
  }
  
  private String getRootTypeId(String caseTypeId)
  {    
    Type caseType = TypeCache.getInstance().getType(caseTypeId);
    if (caseType != null)
    {
      PropertyDefinition pd1 =
        caseType.getPropertyDefinition(ROOT_TYPE_ID_PROPERTY);
      if (pd1 != null && pd1.getValue() != null && pd1.getValue().size() > 0)
      {
        return pd1.getValue().get(0);
      }
    }
    return DictionaryConstants.CASE_CASE_TYPE;    
  }

  public boolean isRenderTypeSelector()
  {
    return getCurrentType() != null;
  }
  
  public boolean isEnableStoreCaseButton()
  {
    return isRenderTypeSelector();    
  }
  
  public String getCaseCasesHelp()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();    
    String typeId = cas.getCaseTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    return getIndexedDicProperty(type, HELP_PROPERTY, null);
  }
  
  private int countResults()
  {
    try
    {
      CaseCaseFilter filter = new CaseCaseFilter();
      filter.setCaseId(getObjectId());
      filter.setCaseCaseTypeId(rowTypeId);
      int result = CaseConfigBean.getPort().countCaseCases(filter);
      filter = new CaseCaseFilter();
      filter.setRelCaseId(getObjectId());
      filter.setCaseCaseTypeId(rowTypeId);
      result = result + CaseConfigBean.getPort().countCaseCases(filter);
      return result;
    }
    catch (Exception ex)
    {
      return 0;
    }
  }

  private List getResults(int firstResult, int maxResults)
  {
    try
    {
      CaseCaseFilter filter = new CaseCaseFilter();
      if (firstResult != 0)
        filter.setFirstResult(firstResult);
      if (maxResults != 0)
        filter.setMaxResults(maxResults);
      filter.setCaseId(getObjectId());
      filter.setCaseCaseTypeId(rowTypeId);
      List<CaseCaseView> result = 
        CaseConfigBean.getPort().findCaseCaseViews(filter);
      List<CaseCaseView> revRows = new ArrayList();
      if (maxResults == 0 || result.size() < maxResults)
      {
        //Reverse cases
        int revFirstResult = 0;
        int revMaxResults = maxResults;
        if (result == null || result.isEmpty())
          revFirstResult =
            firstResult - CaseConfigBean.getPort().countCaseCases(filter);
        if (maxResults > 0)
          revMaxResults = maxResults - result.size();
        filter = new CaseCaseFilter();
        filter.setRelCaseId(getObjectId());
        filter.setCaseCaseTypeId(rowTypeId);        
        filter.setFirstResult(revFirstResult);
        filter.setMaxResults(revMaxResults);        
        revRows = CaseConfigBean.getPort().findCaseCaseViews(filter);
      }

      if (isShowDuplicates())
      {
        result.addAll(revRows);
      }
      else
      {
        for (CaseCaseView revRow : revRows)
        {
          if (!existsReverseCaseCase(result, revRow)) result.add(revRow);
        }
      }
      return result;
    }
    catch (Exception ex)
    {
      return null;
    }
  }

  private boolean isDescendantType(TypeCache typeCache, String typeId,
    String superTypeId)
  {
    Type type = typeCache.getType(typeId);
    List<String> typePathList = type.getTypePathList();
    return typePathList.contains(superTypeId);
  }

  private boolean isReverseRow(CaseCaseView row)
  {
    return !getObjectId().equals(row.getMainCase().getCaseId());
  }  

  private boolean isDirectionalSplit(CaseCaseView row)
  {
    String typeId = row.getCaseCaseTypeId();
    Type type = TypeCache.getInstance().getType(typeId);
    if (type != null)
    {
      PropertyDefinition pd = 
        type.getPropertyDefinition(DIRECTIONAL_SPLIT_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        return "true".equals(pd.getValue().get(0));
    }
    return false;
  }

  private boolean isShowDuplicates()
  {    
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    String value = getIndexedDicProperty(caseType, SHOW_DUPLICATES_PROPERTY, "true");
    return "true".equals(value);
  }
  
  private boolean isReverseEditionEnabled()
  {
    CaseMainBean caseMainBean = (CaseMainBean)getBean("caseMainBean");
    Case cas = caseMainBean.getCase();
    Type caseType = TypeCache.getInstance().getType(cas.getCaseTypeId());
    String value = getIndexedDicProperty(caseType, ENABLE_REVERSE_EDITION_PROPERTY, "false");
    return "true".equals(value);    
  }  
  
  private boolean existsReverseCaseCase(List<CaseCaseView> caseCaseViewList,
    CaseCaseView caseCase)
  {
    String caseId = caseCase.getMainCase().getCaseId();
    String relCaseId = caseCase.getRelCase().getCaseId();
    for (CaseCaseView caseCaseView : caseCaseViewList)
    {
      if (relCaseId.equals(caseCaseView.getMainCase().getCaseId()) &&
        caseId.equals(caseCaseView.getRelCase().getCaseId()))
      {
        return true;
      }
    }
    return false;
  }
  
  private boolean isMatchRelation(TypeCache typeCache, String caseCaseTypeId, String sourceCaseTypeId, 
    String targetCaseTypeId, boolean reverse)
  {
    boolean result = false;
    Type type = typeCache.getType(caseCaseTypeId);
    if ( 
      (reverse && isDescendantType(typeCache, type.getTypeId(), getRootTypeId(targetCaseTypeId))) 
      ||
      (!reverse && isDescendantType(typeCache, type.getTypeId(), getRootTypeId(sourceCaseTypeId)))
      )
    {
      String relSourceCaseTypeId = null;
      String relTargetCaseTypeId = null;
      PropertyDefinition pd =
        type.getPropertyDefinition(SOURCE_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
      {
        relSourceCaseTypeId = pd.getValue().get(0);
      }
      pd = type.getPropertyDefinition(TARGET_TYPE_ID_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
      {
        relTargetCaseTypeId = pd.getValue().get(0);
      }      
      if (relSourceCaseTypeId == null && relTargetCaseTypeId == null)
      {
        result = true;
      }
      else if (relSourceCaseTypeId != null && relTargetCaseTypeId == null)
      {
        result = isDescendantType(typeCache, 
          (reverse ? targetCaseTypeId : sourceCaseTypeId), 
          relSourceCaseTypeId);
      }
      else if (relSourceCaseTypeId == null && relTargetCaseTypeId != null)
      {
        result = isDescendantType(typeCache, 
          (reverse ? sourceCaseTypeId : targetCaseTypeId), 
          relTargetCaseTypeId);
      }
      else
      {
        result = 
          isDescendantType(typeCache, 
            (reverse ? targetCaseTypeId : sourceCaseTypeId), 
            relSourceCaseTypeId)
          && 
          isDescendantType(typeCache, 
            (reverse ? sourceCaseTypeId : targetCaseTypeId), 
            relTargetCaseTypeId);
      }
    }
    return result;
  }
  
  private boolean isReverseEditingCase() throws Exception
  {
    return (selectedTypeId != null && 
      selectedTypeId.startsWith(REVERSE_CASECASETYPE_PREFIX));
  }

  private void reset()
  {
    editingCase = null;
    selectedCaseId = null;
    allTypeItems = null;    
    setCurrentTypeId(null);
    selectedTypeId = null;
    getData().clear();
  }  
  
  private void refreshTypes()
  {
    allTypeItems = null;
    List<SelectItem> auxTypeItems = getAllTypeItems(); //force currentTypeId calc        
    if (auxTypeItems.isEmpty())
    {
      selectedTypeId = null;
    }
    else //first CaseCaseTypeId
    {
      selectedTypeId = (String)auxTypeItems.get(0).getValue();
    }
    setCurrentTypeId();
  }
  
  private void setCurrentTypeId()
  {
    if (selectedTypeId != null && selectedTypeId.startsWith(REVERSE_CASECASETYPE_PREFIX))
    {
      setCurrentTypeId(selectedTypeId.substring(REVERSE_CASECASETYPE_PREFIX.length()));
    }
    else
    {
      setCurrentTypeId(selectedTypeId);
    }
  }  
  
  private void addReverseLabelPrefix(List<SelectItem> items)
  {
    Set<String> caseCaseTypeIdSet = new HashSet();
    for (SelectItem item : items)
    {
      String caseCaseTypeId = (String)item.getValue();
      caseCaseTypeIdSet.add(caseCaseTypeId);
    }    
    for (SelectItem item : items)
    {
      String caseCaseTypeId = (String)item.getValue();
      if (caseCaseTypeId.startsWith(REVERSE_CASECASETYPE_PREFIX))
      {
        String noPrefixCaseCaseTypeId = 
          caseCaseTypeId.substring(REVERSE_CASECASETYPE_PREFIX.length());
        if (caseCaseTypeIdSet.contains(noPrefixCaseCaseTypeId))
        {          
          item.setLabel(REVERSE_CASECASELABEL_PREFIX + item.getLabel());
        }
      }      
    }    
  }  
  
  private String getCaseCaseDirectionalLabel(String caseCaseTypeId, 
    boolean reverse)
  {
    Type caseType = TypeCache.getInstance().getType(caseCaseTypeId);
    if (caseType != null)
    {
      String propertyName = (reverse ? REVERSE_DESCRIPTION_PROPERTY : 
        DIRECT_DESCRIPTION_PROPERTY);
      PropertyDefinition pd =
        caseType.getPropertyDefinition(propertyName);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
        return pd.getValue().get(0);
    }
    return null;
  }
  
  @Override
  public int getPageSize()
  {
    if (pageSize > 0)
      return pageSize;
    return super.getPageSize();
  }  
  
  private void loadPropertyDefinitions(Type caseType)
  {
    if (caseType != null)
    {
      rootTypeId = getIndexedDicProperty(caseType, ROOT_TYPE_ID_PROPERTY, 
        DictionaryConstants.CASE_CASE_TYPE);
      
      rowTypeId = getIndexedDicProperty(caseType, ROW_TYPE_ID_PROPERTY, null);      
      
      String allTypesVisibleString = getIndexedDicProperty(caseType, ALL_TYPES_VISIBLE_PROPERTY, null);
      if (allTypesVisibleString != null)
      {
        allTypesVisible = Boolean.parseBoolean(allTypesVisibleString);
      }
      
      String value = getIndexedDicProperty(caseType, CASECASE_PAGE_SIZE_PROPERTY, "0");
      pageSize = Integer.valueOf(value);
      
      groupBy = getIndexedDicProperty(caseType, GROUPBY_PROPERTY, null);
      
      groupSelectionMode = getIndexedDicProperty(caseType, GROUP_SELECTION_MODE_PROPERTY, 
        NONE_SELECTION_MODE);
      
      String orderByString = getIndexedDicProperty(caseType, ORDERBY_PROPERTY, null);
      if (orderByString != null)
      {
        String[] array = orderByString.split(",");
        if (array != null)
          orderBy = Arrays.asList(array);
      }      
            
      PropertyDefinition pd = 
        caseType.getPropertyDefinition(SHOW_PROPERTIES_PROPERTY);
      if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)      
        defaultViewDumper = new ObjectDumper(pd.getValue().get(0));
      else
        defaultViewDumper = new ObjectDumper(new ArrayList());
    }
  }  
  
  @Override
  public GroupExtractor getGroupExtractor()
  {
    if (groupBy == null)
      return null;
    else
    {
      if (groupBy.endsWith("TypeId"))
      {
        return new TypeGroupExtractor(groupBy);
      }
      else
        return super.getGroupExtractor();
    }
  }
  
  @Override
  public Object getSelectedRow()
  {
    return getRequestMap().get("row");    
  }
  
  @Override
  protected String getRowId(Object row)
  {
    CaseCaseView caseCaseRow = (CaseCaseView)row;
    return caseCaseRow.getCaseCaseId();
  }

  @Override
  protected String getRowTypeId(Object row)
  {
    CaseCaseView caseCaseRow = (CaseCaseView)row;
    return caseCaseRow.getCaseCaseTypeId();
  }

  @Override
  protected String getShowPropertiesPropertyName(Object row)
  {
    String propertyName;
    PropertyDefinition pd;
    CaseCaseView ccvRow = (CaseCaseView)row;
    String typeId = getRowTypeId(row);      
    Type type = TypeCache.getInstance().getType(typeId);
    if (isReverseRow(ccvRow))
    {
      propertyName = REVERSE_SHOW_PROPERTIES_PROPERTY;
      pd = type.getPropertyDefinition(propertyName);
    }
    else
    {
      propertyName = DIRECT_SHOW_PROPERTIES_PROPERTY;
      pd = type.getPropertyDefinition(propertyName);      
    }
    if (pd == null || pd.getValue() == null || pd.getValue().isEmpty())
    {
      propertyName = SHOW_PROPERTIES_PROPERTY;      
    }        
    return propertyName;
  }
  
  protected class TypeGroupExtractor extends TypifiedPageBean.TypeGroupExtractor
  {
    private static final String DIRECT_SUFFIX = "d";
    private static final String REVERSE_SUFFIX = "r";

    public TypeGroupExtractor(String typeIdPropertyName)
    {
      super(typeIdPropertyName);
    }

    @Override
    protected String getName(Object view)
    {
      String name = super.getName(view);
      CaseCaseView row = (CaseCaseView)view;
      if (isDirectionalSplit(row))
      {
        boolean reverse = isReverseRow(row);
        name = name + ";" + (reverse ? REVERSE_SUFFIX : DIRECT_SUFFIX);
      }
      return name;
    }

    @Override
    protected String getDescription(String keyName)
    {
      if (keyName != null && !keyName.equals(NULL_GROUP.getName()))
      {
        String[] keyNameSplit = keyName.split(";");
        keyName = keyNameSplit[0];
        Type type = TypeCache.getInstance().getType(keyName);
        if (type != null)
        {
          if (keyNameSplit.length > 1) //directional split
          {
            boolean reverse = REVERSE_SUFFIX.equals(keyNameSplit[1]);
            PropertyDefinition pd = null;
            if (reverse)
              pd = type.getPropertyDefinition(REVERSE_DESCRIPTION_PROPERTY);
            else
              pd = type.getPropertyDefinition(DIRECT_DESCRIPTION_PROPERTY);
            if (pd != null && pd.getValue() != null && pd.getValue().size() > 0)
              return pd.getValue().get(0);
            else
              return type.getDescription();
          }
          else
          {
            return type.getDescription();
          }
        }
        return keyName;
      }
      else
      {
        return NULL_GROUP.getDescription();
      }
    }
  }

}
