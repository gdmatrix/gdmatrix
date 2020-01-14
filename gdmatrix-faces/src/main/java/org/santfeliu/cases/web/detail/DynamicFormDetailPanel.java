package org.santfeliu.cases.web.detail;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.matrix.cases.Case;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.form.Form;
import org.santfeliu.form.builder.TypeFormBuilder;
import org.santfeliu.web.obj.DetailBean;
import org.santfeliu.web.obj.DetailPanel;
import org.santfeliu.web.obj.util.DynamicFormsManager;

/**
 *
 * @author blanquepa
 */
public class DynamicFormDetailPanel extends DetailPanel
{
  private static final String RENDER_IF_PROPERTY_EXISTS = "renderIfPropertyExists";
  private static final String RENDERER_TYPES = "rendererTypes";

  private CaseDetailBean detailBean;
  private DynamicFormsManager dynamicFormsManager;
  private String renderIfPropertyExists;

  @Override
  public void loadData(DetailBean detailBean)
  {
    this.detailBean = (CaseDetailBean) detailBean;

    renderIfPropertyExists = getProperty(RENDER_IF_PROPERTY_EXISTS);
    
    dynamicFormsManager = new DynamicFormsManager(TypeFormBuilder.VIEW_PREFIX,
      getMultivaluedProperty(DynamicFormsManager.ALLOWED_FORM_SELECTORS));
    Case cas = this.detailBean.getCase();
    dynamicFormsManager.setCurrentTypeId(cas.getCaseTypeId());
    dynamicFormsManager.setFormDataFromProperties(
      DictionaryUtils.getProperties(cas), cas.getCaseId());
  }

  public Map getData()
  {
    return dynamicFormsManager.getData();
  }

  public void setData(Map data)
  {
    dynamicFormsManager.setData(data);
  }

  public Form getForm() 
  {
    try
    {
      return dynamicFormsManager.getForm();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  public boolean isTypeUndefined()
  {
    return dynamicFormsManager.isTypeUndefined();
  }

  public String getSelector()
  {
    return dynamicFormsManager.getSelector();
  }

  @Override
  public boolean isRenderContent()
  {
    return
      getSelector() != null && !isEmptyData();
  }

  @Override
  public String getType()
  {
    return "dynamicform";
  }
  
  public String getRendererTypes()
  {
    String rendererTypes = getProperty(RENDERER_TYPES);
    if (StringUtils.isBlank(rendererTypes))
      rendererTypes = "DisabledHtmlFormRenderer";
    return rendererTypes;
  }
  
  private boolean isEmptyData()
  {
    Map data = dynamicFormsManager.getData();
    return (data == null
      || data.isEmpty()
      || (renderIfPropertyExists != null &&
        (!data.containsKey(renderIfPropertyExists) || data.get(renderIfPropertyExists) == null)));
  }
}
