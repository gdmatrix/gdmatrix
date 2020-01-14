package org.santfeliu.doc.web;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.matrix.dic.Property;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.doc.State;
import org.santfeliu.classif.ClassCache;
import org.santfeliu.dic.util.DictionaryUtils;
import org.santfeliu.util.FilterUtils;
import org.santfeliu.web.obj.util.DynamicFormFilter;
import org.santfeliu.web.obj.util.FormFilter;

/**
 *
 * @author blanquepa
 */
public class DocumentFormFilter extends DynamicFormFilter implements Serializable
{
  private String docIdInput;
  private String versionInput;
  private String titleInput = null;
  private String classIdInput = null;
  private String propertyNameInput1 = null;
  private String propertyValueInput1 = null;
  private String propertyNameInput2 = null;
  private String propertyValueInput2 = null;
  private boolean includeDraftCBValue;
  private boolean includeCompleteCBValue;
  private boolean includeRecordCBValue;
  private boolean includeDeletedCBValue;
  private DocumentFilter documentFilter;

  public DocumentFormFilter()
  {
    documentFilter = new DocumentFilter();
  }

  //Accessors
  public String getClassId()
  {
    return classIdInput;
  }

  public void setClassId(String classId)
  {
    this.classIdInput = classId;
  }

  public String getDocId()
  {
    return docIdInput;
  }

  public void setDocId(String docIdInput)
  {
    this.docIdInput = docIdInput;
  }

  public DocumentFilter getDocumentFilter()
  {
    if (documentFilter == null)
      documentFilter = new DocumentFilter();
    return documentFilter;
  }

  public void setDocumentFilter(DocumentFilter documentFilter)
  {
    this.documentFilter = new DocumentFilter();
    if (documentFilter != null)
    {
      this.documentFilter.setContentId(documentFilter.getContentId());
      this.documentFilter.setContentSearchExpression(documentFilter.getContentSearchExpression());
      this.documentFilter.setDateComparator(documentFilter.getDateComparator());
      this.documentFilter.setDocTypeId(documentFilter.getDocTypeId());
      this.documentFilter.setEndDate(documentFilter.getEndDate());
      this.documentFilter.setFirstResult(documentFilter.getFirstResult());
      this.documentFilter.setIncludeContentMetadata(documentFilter.isIncludeContentMetadata());
      this.documentFilter.setLanguage(documentFilter.getLanguage());
      this.documentFilter.setMaxResults(documentFilter.getMaxResults());
      this.documentFilter.setMetadataSearchExpression(documentFilter.getMetadataSearchExpression());
      this.documentFilter.setRolesDisabled(documentFilter.isRolesDisabled());
      this.documentFilter.setStartDate(documentFilter.getStartDate());
      this.documentFilter.setSummary(documentFilter.isSummary());
      this.documentFilter.setTitle(documentFilter.getTitle());
      this.documentFilter.setVersion(documentFilter.getVersion());
      this.documentFilter.getClassId().clear();
      this.documentFilter.getClassId().addAll(documentFilter.getClassId());
      this.documentFilter.getDocId().clear();
      this.documentFilter.getDocId().addAll(documentFilter.getDocId());
      this.documentFilter.getOrderByProperty().clear();
      this.documentFilter.getOrderByProperty().addAll(documentFilter.getOrderByProperty());
      this.documentFilter.getOutputProperty();
      this.documentFilter.getOutputProperty().addAll(documentFilter.getOutputProperty());
      this.documentFilter.getProperty().clear();
      this.documentFilter.getProperty().addAll(documentFilter.getProperty());
      this.documentFilter.getStates().clear();
      this.documentFilter.getStates().addAll(documentFilter.getStates());
    }
  }

  public boolean isIncludeCompleteCBValue()
  {
    return includeCompleteCBValue;
  }

  public void setIncludeCompleteCBValue(boolean includeCompleteCBValue)
  {
    this.includeCompleteCBValue = includeCompleteCBValue;
  }

  public boolean isIncludeDeletedCBValue()
  {
    return includeDeletedCBValue;
  }

  public void setIncludeDeletedCBValue(boolean includeDeletedCBValue)
  {
    this.includeDeletedCBValue = includeDeletedCBValue;
  }

  public boolean isIncludeDraftCBValue()
  {
    return includeDraftCBValue;
  }

  public void setIncludeDraftCBValue(boolean includeDraftCBValue)
  {
    this.includeDraftCBValue = includeDraftCBValue;
  }

  public boolean isIncludeRecordCBValue()
  {
    return includeRecordCBValue;
  }

  public void setIncludeRecordCBValue(boolean includeRecordCBValue)
  {
    this.includeRecordCBValue = includeRecordCBValue;
  }

  public String getPropertyName1()
  {
    return propertyNameInput1;
  }

  public void setPropertyName1(String propertyNameInput1)
  {
    this.propertyNameInput1 = propertyNameInput1;
  }

  public String getPropertyName2()
  {
    return propertyNameInput2;
  }

  public void setPropertyName2(String propertyNameInput2)
  {
    this.propertyNameInput2 = propertyNameInput2;
  }

  public String getPropertyValue1()
  {
    return propertyValueInput1;
  }

  public void setPropertyValue1(String propertyValueInput1)
  {
    this.propertyValueInput1 = propertyValueInput1;
  }

  public String getPropertyValue2()
  {
    return propertyValueInput2;
  }

  public void setPropertyValue2(String propertyValueInput2)
  {
    this.propertyValueInput2 = propertyValueInput2;
  }

  public String getTitle()
  {
    return titleInput;
  }

  public void setTitle(String titleInput)
  {
    this.titleInput = titleInput;
  }

  public String getVersion()
  {
    return versionInput;
  }

  public void setVersion(String versionInput)
  {
    this.versionInput = versionInput;
  }

  @Override
  public void setFirstResult(int value)
  {
    this.documentFilter.setFirstResult(value);
  }

  @Override
  public void setMaxResults(int value)
  {
    this.documentFilter.setMaxResults(value);
  }

  public void setDocTypeId(String docTypeId)
  {
    this.documentFilter.setDocTypeId(docTypeId);
  }

  public String getDocTypeId()
  {
    return documentFilter.getDocTypeId();
  }

  public void setMetadataSearchExpression(String searchExpression)
  {
    this.documentFilter.setMetadataSearchExpression(searchExpression);
  }

  public String getMetadataSearchExpression()
  {
    return this.documentFilter.getMetadataSearchExpression();
  }

  public void setContentSearchExpression(String contentSearchExpression)
  {
    this.documentFilter.setContentSearchExpression(contentSearchExpression);
  }

  public String getContentSearchExpression()
  {
    return this.documentFilter.getContentSearchExpression();
  }

  public List<State> getStates()
  {
    return documentFilter.getStates();
  }

  public List<OrderByProperty> getOrderByProperties()
  {
    return documentFilter.getOrderByProperty();
  }

  public void setIncludeContentMetadata(boolean value)
  {
    this.documentFilter.setIncludeContentMetadata(value);
  }

  public void setInputProperties(List<Property> formProperties)
  {
    //TITLE
    if (!StringUtils.isBlank(titleInput))
      documentFilter.setTitle(FilterUtils.addWildcards(titleInput));
    else if (DictionaryUtils.getPropertyByName(formProperties, "title") != null)
      documentFilter.setTitle(FilterUtils.addWildcards(documentFilter.getTitle()));
    else
      documentFilter.setTitle(null);

    //CONTENT
    if (StringUtils.isBlank(documentFilter.getContentSearchExpression()))
      documentFilter.setContentSearchExpression(null);

    //DOCID
    if (!StringUtils.isBlank(docIdInput))
      documentFilter.getDocId().add(docIdInput.trim());

    //CLASSID
    if (!StringUtils.isBlank(classIdInput))
      setClassIdFilter(documentFilter);

    //VERSION
    if (!StringUtils.isBlank(versionInput))
      documentFilter.setVersion(Integer.valueOf(versionInput));
    else
      documentFilter.setVersion(0);
    
    //STATES
    if (includeDraftCBValue)
      documentFilter.getStates().add(State.DRAFT);
    if (includeCompleteCBValue)
      documentFilter.getStates().add(State.COMPLETE);      
    if (includeRecordCBValue)
      documentFilter.getStates().add(State.RECORD);      
    if (includeDeletedCBValue)
      documentFilter.getStates().add(State.DELETED);      
  }

  public void clearLists()
  {
    documentFilter.getClassId().clear();
    documentFilter.getDocId().clear();
    documentFilter.getOrderByProperty().clear();
    documentFilter.getOutputProperty();
    documentFilter.getProperty().clear();
    documentFilter.getStates().clear();
  }

  public boolean isEmpty()
  {
    return (StringUtils.isBlank(getDocId()) && StringUtils.isBlank(getDocTypeId())
      && StringUtils.isBlank(getContentSearchExpression())
      && StringUtils.isBlank(getMetadataSearchExpression())
      && StringUtils.isBlank(getTitle()) && StringUtils.isBlank(getVersion())
      && StringUtils.isBlank(getClassId())
      && StringUtils.isBlank(getPropertyName1())
      && StringUtils.isBlank(getPropertyName2())
      && StringUtils.isBlank(getDocumentFilter().getStartDate())
      && StringUtils.isBlank(getDocumentFilter().getEndDate())
      && StringUtils.isBlank(getDocumentFilter().getLanguage())
      && StringUtils.isBlank(getDocumentFilter().getContentId())      
      && getDocumentFilter().getProperty().isEmpty()
      );
  }

  protected void copy(FormFilter src, FormFilter dst)
  {
    DocumentFormFilter dff = new DocumentFormFilter();
    if (src instanceof DocumentFormFilter)
    {
      DocumentFormFilter sff = (DocumentFormFilter)src;

      dff.setDocumentFilter(sff.getDocumentFilter());
      dff.setDocId(sff.getDocId());
      dff.setVersion(sff.getVersion());

      dff.setIncludeDraftCBValue(sff.isIncludeDraftCBValue());
      dff.setIncludeCompleteCBValue(sff.isIncludeCompleteCBValue());
      dff.setIncludeRecordCBValue(sff.isIncludeRecordCBValue());
      dff.setIncludeDeletedCBValue(sff.isIncludeDeletedCBValue());

      dff.setTitle(sff.getTitle());
      dff.setClassId(sff.getClassId());
      dff.setPropertyName1(sff.getPropertyName1());
      dff.setPropertyValue1(sff.getPropertyName2());
      dff.setPropertyName2(sff.getPropertyName2());
      dff.setPropertyValue2(sff.getPropertyValue2());
      dst = dff;
    }
  }

  protected void clearAll()
  {
    docIdInput = null;
    versionInput = null;
    titleInput = null;
    classIdInput = null;
    propertyNameInput1 = null;
    propertyValueInput1 = null;
    propertyNameInput2 = null;
    propertyValueInput2 = null;
    includeDraftCBValue = false;
    includeCompleteCBValue = false;
    includeDeletedCBValue = false;
    includeRecordCBValue = false;
    documentFilter = new DocumentFilter();
  }

  @Override
  protected Object getObjectFilter()
  {
    return documentFilter;
  }

  private void setClassIdFilter(DocumentFilter filter)
  {
    org.santfeliu.classif.Class classObject =
      ClassCache.getInstance().getClass(getClassId());
    if (classObject != null)
    {
      filter.getClassId().add(classObject.getClassId());
      for (org.santfeliu.classif.Class subClass : classObject.getSubClasses(true))
      {
        filter.getClassId().add(subClass.getClassId());
      }
    }
    else
      filter.getClassId().add(getClassId());
  }

  @Override
  protected List<Property> getProperty()
  {
    return documentFilter.getProperty();
  }

}
