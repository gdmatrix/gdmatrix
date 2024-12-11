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
package org.santfeliu.webapp.modules.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.DataSource;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.matrix.cases.Case;
import org.matrix.cases.CaseDocumentFilter;
import org.matrix.cases.CaseDocumentView;
import org.matrix.cases.CaseFilter;
import org.matrix.cases.CaseManagerPort;
import static org.matrix.dic.DictionaryConstants.EXECUTE_ACTION;
import static org.matrix.dic.DictionaryConstants.READ_ACTION;
import org.matrix.dic.EnumTypeItem;
import org.matrix.dic.Property;
import org.matrix.doc.Content;
import org.matrix.doc.ContentInfo;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentManagerPort;
import org.matrix.security.AccessControl;
import org.matrix.workflow.WorkflowConstants;
import org.primefaces.component.chip.Chip;
import org.santfeliu.dic.EnumTypeCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.doc.web.DocumentConfigBean;
import org.santfeliu.util.TextUtils;
import org.santfeliu.util.json.JSONUtils;
import org.santfeliu.util.keywords.KeywordsManager;
import org.santfeliu.util.template.JSTemplate;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.webapp.modules.cases.CasesModuleBean;
import org.santfeliu.webapp.modules.doc.DocModuleBean;

/**
 *
 * @author blanquepa
 */
@Named
@RequestScoped
public class ProcedureListBean extends WebBean implements Serializable
{
  public static final String PROCEDURE_TYPE_PROPERTY = "procedureTypeId";
  public static final String INFO_DOCUMENT_PROPERTY = "infoDocId";
  public static final String INTERNAL_INFO_DOCUMENT_PROPERTY = "internalInfoDocId";  
  public static final String REQUIREMENTS_DOCUMENT_PROPERTY = "requirementsDocId";

  private static final String WORKFLOW_DOCTYPEID = "WORKFLOW";

  private String description;
  private List<Procedure> rows;
  private int firstRow = 0;
  private String view = "procedure_list";

  private List<ProcedureFilter> filters;
  private Map<String, List<EnumTypeItem>> filterMap;
  private Map<String, String[]> selectedFilters = new HashMap<>();
  private Procedure procedure;
  private String templateString;
  private String internalTemplateString;

  @Inject
  WorkflowInstanceListBean instanceListBean;

  public enum Period
  {
    BEFORE,
    IN,
    AFTER
  }

  public ProcedureListBean()
  {    
  }

  public String getContent()
  {
    return "/pages/workflow/" + view + ".xhtml";
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public List getRows()
  {
    return rows;
  }

  public void setRows(List rows)
  {
    this.rows = rows;
  }

  public int getFirstRow()
  {
    return firstRow;
  }

  public void setFirstRow(int firstRow)
  {
    this.firstRow = firstRow;
  }

  public Procedure getProcedure()
  {
    return procedure;
  }

  public void setProcedure(Procedure procedure)
  {
    this.procedure = procedure;
  }

  public List<ProcedureFilter> getFilters()
  {
    return filters;
  }
  
  public List<EnumTypeItem> getFilterItems(String propName)
  {
    return filterMap.get(propName);
  }

  public Map<String, String[]> getSelectedFilters()
  {
    return selectedFilters;
  }
  
  public int getSelectedFiltersCount()
  {
    int count = 0;
    for (String[] items : selectedFilters.values())
    {
      if (items.length > 0)
        count++;
    }
    return count;
  }
  
  public List<String> getSelectedFiltersValues()
  {
    List<String> values = new ArrayList();
    if (selectedFilters != null)
    {
      Set<String> filterNames = selectedFilters.keySet();
      for (String filterName : filterNames)
      {
        String[] selected = selectedFilters.get(filterName);
        Arrays.stream(selected)
          .forEach(s -> values.add(getSelectedLabel(filterName, s)));      
      }
    }
    return values;
  }
  
  private String getSelectedLabel(String filterName, String value)
  {
    if (value == null)
      return "";
    
    for (EnumTypeItem item : filterMap.get(filterName))
    {
      if (value.equals(item.getValue()))
        return item.getLabel();
    }
    return value;
  }

  public String getInfo()
  {
    if (templateString == null)
    {    
      try 
      {
        String infoDocId = getProperty(INFO_DOCUMENT_PROPERTY);
        templateString = getTemplateCode(infoDocId);
      }
      catch (Exception ex) 
      {
        error(ex);
      }        
    }
      
    return getInfo(templateString);    
  }
  
  public String getInternalInfo()
  {
    if (internalTemplateString == null)
    {    
      try 
      {
        String infoDocId = getProperty(INTERNAL_INFO_DOCUMENT_PROPERTY);
        internalTemplateString = getTemplateCode(infoDocId);        
      }
      catch (Exception ex) 
      {
        error(ex);
      }        
    }
      
    return getInfo(internalTemplateString);      
  }
  
  private String getTemplateCode(String docId) throws Exception
  {
    String templateCode = null;
    DocumentManagerPort port = DocModuleBean.getPort(true);
    if (docId != null)
    {
      Document document = port.loadDocument(docId, 0, ContentInfo.ALL);
      Content content = document.getContent();
      DataSource ds = content.getData().getDataSource();
      templateCode = IOUtils.toString(ds.getInputStream(), "UTF-8");
    }    
    return templateCode;
  }
  
  private String getInfo(String code) 
  {
    String info = null;
    
    if (code == null)
      return info;
    
    try
    {
      if (procedure != null)
      {
        JSTemplate template = JSTemplate.create(code);
        info = template.merge(procedure.getProperties());        
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
    return info;
  }
    
  public String show()
  {
    filters = new ArrayList<>();  
    filterMap = new HashMap<>();    
    
    //Get filters configuration
    String nodeId = UserSessionBean.getCurrentInstance().getSelectedMid();
    JSONArray filtersConfig = 
      (JSONArray) JSONUtils.getJSON(nodeId).get("filters");    
    Iterator it = filtersConfig.iterator();
    while(it.hasNext())
    {
      JSONObject item = (JSONObject) it.next();
      ProcedureFilter pf = new ProcedureFilter(
        (String) item.get("name"), 
        (String) item.get("label"), 
        (String) item.get("enumTypeId"));
      filters.add(pf);
      List list = EnumTypeCache.getInstance().getItems(pf.enumTypeId);
      filterMap.put(pf.propName, list);
    }    
    
    
    String template = UserSessionBean.getCurrentInstance().getTemplate();
    search();
    HttpServletRequest request = (HttpServletRequest)FacesContext.
      getCurrentInstance().getExternalContext().getRequest();
    if ("GET".equals(request.getMethod()))
    {
      String procedureId = request.getParameter("oid");
      if (procedureId != null)
      {
        search(procedureId);
        if (rows != null && !rows.isEmpty())
          view(procedureId);
      }
      else
        search();
    }
    else
      search();
    return "/templates/" + template + "/template.xhtml";
  }

  public void view(String procedureId)
  {
    try
    {
      CaseManagerPort port = CasesModuleBean.getPort();
      Case cas = port.loadCase(procedureId);
      procedure = new Procedure(cas);
      procedure.setProperties(cas.getProperty());

      //Workflows
      CaseDocumentFilter filter = new CaseDocumentFilter();
      filter.setCaseId(cas.getCaseId());
      List<CaseDocumentView> docs = port.findCaseDocumentViews(filter);
      for (CaseDocumentView doc : docs)
      {
        String docTypeId = doc.getDocument().getDocTypeId();
        docTypeId = docTypeId.contains(":")
          ? docTypeId.substring(docTypeId.indexOf(":") + 1) : docTypeId;
        if (WORKFLOW_DOCTYPEID.equals(docTypeId))
        {
          String docId = doc.getDocument().getDocId();
          Document document = DocumentConfigBean.getPortAsAdmin()
            .loadDocument(docId, 0, ContentInfo.METADATA);
          if (canUserExecuteWorkflow(document))
          {
            procedure.getWorkflows().add(new Workflow(document));
          }
        }
      }
      
      //Other metadata
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public void search()
  {
    search(null);
  }

  public void search(String procedureId)
  {
    try
    {
      rows = new ArrayList();
      CaseFilter filter = new CaseFilter();
      if (procedureId != null)
        filter.getCaseId().add(procedureId);
      String typeId = getProperty(PROCEDURE_TYPE_PROPERTY);
      if (typeId == null)
        throw new Exception("INVALID_CONFIGURATION");
      filter.setCaseTypeId(typeId);

      filter.setSearchExpression(" ORDER BY title ");

      KeywordsManager keywordsManager = new KeywordsManager(description);
      filter.getProperty().add(keywordsManager.getDisjointKeywords());

      DictionaryUtils.setProperty(filter, "intern", "false");

      for (int i = 0; i < filters.size(); i++)
      {
        ProcedureFilter pf = filters.get(i);
        String[] filterValues = selectedFilters.get(pf.getPropName());

        if (filterValues != null && filterValues.length > 0)
          DictionaryUtils.setProperty(filter, pf.getPropName(), filterValues);
      }

      filter.getOutputProperty().add("dataIniciTramitacio");
      filter.getOutputProperty().add("dataFiTramitacio");
      filter.getOutputProperty().add("online");
      filter.getOutputProperty().add("workflow");
      List<Case> cases = CasesModuleBean.getPort().findCases(filter);
      for (int i = 0; i < cases.size(); i++)
      {
        Procedure row = new Procedure(cases.get(i));

        rows.add(row);
      }
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }

  public void onChangeFilter(AjaxBehaviorEvent event)
  {
    search();
  }
  
  public void onCloseSelectedFilter(AjaxBehaviorEvent event)
  {
    Chip chip = ((Chip) event.getSource());
    String label = chip.getLabel();
    //Clear all selected and fill again without closed one.
    Map<String,String[]> cloneMap = new HashMap();
    cloneMap.putAll(selectedFilters);
    selectedFilters.clear();
    for (String key : cloneMap.keySet())
    {
      List<String> newValues = new ArrayList<>();      
      String[] values = cloneMap.get(key);
      for (String value : values)
      {
        //Comparision by label
        if (!label.equals(getSelectedLabel(key, value)))
         newValues.add(value);
      }
      selectedFilters.put(key, newValues.toArray(String[]::new));
    }
    search();
  }

  public void transact(String workflowName)
  {
    try
    {
      instanceListBean.main();
      instanceListBean.setWorkflowName(workflowName);
      instanceListBean.transact();
    }
    catch (Exception ex)
    {
      error(ex);
    }
  }
  
  public boolean isInternalUser()
  {
    //TODO: Find user roles
    return true;
  }
  
  private boolean canUserExecuteWorkflow(Document workflow)
  {
    Set<String> roles = UserSessionBean.getCurrentInstance().getRoles();
    if (roles == null)
      return false;
    List<AccessControl> acl = workflow.getAccessControl();
    return roles.contains(WorkflowConstants.WORKFLOW_ADMIN_ROLE)
      || DictionaryUtils.canPerformAction(READ_ACTION, roles, acl)
      || DictionaryUtils.canPerformAction(EXECUTE_ACTION, roles, acl);
  }

  public class ProcedureFilter implements Serializable
  {
    private String propName;
    private String label;
    private String enumTypeId;
    private String styleClass;

    public ProcedureFilter(String propName, String label, String enumTypeId)
    {
      this.propName = propName;
      this.label = label;
      this.enumTypeId = enumTypeId;
    }
    
    public String getPropName()
    {
      return propName;
    }

    public void setPropName(String propName)
    {
      this.propName = propName;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public String getEnumTypeId()
    {
      return enumTypeId;
    }

    public void setEnumTypeId(String enumTypeId)
    {
      this.enumTypeId = enumTypeId;
    }

    public String getStyleClass()
    {
      return styleClass;
    }

    public void setStyleClass(String styleClass)
    {
      this.styleClass = styleClass;
    }

  }

  public class Procedure implements Serializable
  {
    private String id;
    private String title;
    private String startDate;
    private String endDate;
    private boolean online;
    private boolean certificate;
    private Map properties = new HashMap();
    private List<Workflow> workflows;

    public Procedure(Case cas)
    {
      if (cas != null)
      {
        this.id = cas.getCaseId();
        this.title = cas.getTitle();
        this.startDate = DictionaryUtils.getPropertyValue(cas.getProperty(),
          "dataIniciTramitacio");
        this.endDate = DictionaryUtils.getPropertyValue(cas.getProperty(),
          "dataFiTramitacio");
        String ol = DictionaryUtils.getPropertyValue(cas.getProperty(),
          "online");
        this.online = Boolean.parseBoolean(ol);
        String cert = DictionaryUtils.getPropertyValue(cas.getProperty(),
          "certificate");
        this.certificate = Boolean.parseBoolean(cert); 
        this.properties.put("caseId", cas.getCaseId());
        this.properties.put("workflowMid", getProperty("workflowMid"));
        this.workflows = new ArrayList();
      }
    }

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getTitle()
    {
      return title;
    }

    public void setTitle(String title)
    {
      this.title = title;
    }

    public String getStartDate()
    {
      return startDate;
    }

    public void setStartDate(String startDate)
    {
      this.startDate = startDate;
    }

    public String getUserStartDate()
    {
      return TextUtils.formatInternalDate(startDate, "dd/MM/yyyy");
    }

    public void setEndDate(String endDate)
    {
      this.endDate = endDate;
    }

    public String getEndDate()
    {
      return endDate;
    }

    public String getUserEndDate()
    {
      return TextUtils.formatInternalDate(endDate, "dd/MM/yyyy");
    }

    public boolean isOnline()
    {
      return online;
    }

    public void setOnline(boolean online)
    {
      this.online = online;
    }

    public boolean isCertificate()
    {
      return certificate;
    }

    public void setCertificate(boolean certificate)
    {
      this.certificate = certificate;
    }

    public List<Workflow> getWorkflows()
    {
      return workflows;
    }

    public Map getProperties()
    {
      return properties;
    }

    public void setProperties(Map properties)
    {
      this.properties = properties;
    }

    public void setProperties(List<Property> properties)
    {
      for (Property p : properties)
      {
        if (p.getValue() != null)
          this.properties.put(p.getName(), p.getValue().get(0));
      }
    }

    public Period getPeriod()
    {
      Date today = new Date();

      Date sd = TextUtils.parseInternalDate(startDate);
      Date ed = TextUtils.parseInternalDate(endDate);

      if (sd == null)
        return Period.IN;
      if (today.before(sd))
        return Period.BEFORE;
      else if (ed != null && today.after(ed))
        return Period.AFTER;
      else
        return Period.IN;
    }

    public boolean isInPeriod()
    {
      return Period.IN.equals(getPeriod());
    }

  }

  public class Workflow
  {
    private String name;
    private String description;

    public Workflow(Document document)
    {
      if (document != null && !document.getProperty().isEmpty())
      {
        List<Property> props = document.getProperty();
        name = DictionaryUtils.getPropertyValue(props, "workflow.xml");
        description = document.getTitle();
        if (description != null && description.contains(":"))
          description = description.substring(description.indexOf(":") + 1).trim();
      }
    }

    public Workflow(String name, String description)
    {
      this.name = name;
      this.description = description;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getDescription()
    {
      return description;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }

  }

}
