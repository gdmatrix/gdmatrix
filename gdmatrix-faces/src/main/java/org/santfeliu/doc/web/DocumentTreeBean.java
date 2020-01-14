package org.santfeliu.doc.web;

import java.util.List;


import org.apache.myfaces.custom.tree2.TreeModel;
import org.apache.myfaces.custom.tree2.TreeModelBase;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeStateBase;

import org.matrix.doc.Content;
import org.matrix.doc.Document;
import org.matrix.doc.DocumentFilter;
import org.matrix.doc.OrderByProperty;
import org.matrix.dic.Property;

import org.santfeliu.doc.util.DocumentUtils;
import org.santfeliu.doc.client.CachedDocumentManagerClient;
import org.santfeliu.faces.browser.HtmlBrowser;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.tree.ExtendedTreeNode;
import org.santfeliu.faces.tree.TreeModelUtils;
import org.santfeliu.util.MimeTypeMap;
import org.santfeliu.util.Table;
import org.santfeliu.web.UserSessionBean;
import org.santfeliu.web.WebBean;
import org.santfeliu.web.bean.CMSAction;
import org.santfeliu.web.bean.CMSManagedBean;
import org.santfeliu.web.bean.CMSProperty;

@CMSManagedBean
public class DocumentTreeBean extends WebBean
{
  @CMSProperty @Deprecated
  public static final String HEADER_DOCUMENT_PROPERTY = "header.document";
  @CMSProperty @Deprecated
  public static final String FOOTER_DOCUMENT_PROPERTY = "footer.document";
  @CMSProperty
  public static final String HEADER_DOCID_PROPERTY = "header.docId";
  @CMSProperty
  public static final String FOOTER_DOCID_PROPERTY = "footer.docId";
  @CMSProperty(mandatory=true)
  public static final String TREE_FILTERPROPERTY_NAME_PROPERTY =
    "tree.filterProperty.name";
  @CMSProperty(mandatory=true)
  public static final String TREE_FILTERPROPERTY_VALUE_PROPERTY =
    "tree.filterProperty.value";
  @Deprecated
  public static final String FILTERPROPERTY_NAME_PROPERTY =
    "filterProperty.name";
  @Deprecated  
  public static final String FILTERPROPERTY_VALUE_PROPERTY =
    "filterProperty.value";

  private static final String SERVLET_PATH = "/documents/";

  private HtmlBrowser headerBrowser = new HtmlBrowser();  
  private HtmlBrowser footerBrowser = new HtmlBrowser();
  private final String servletURL;

  public DocumentTreeBean()
  {
    servletURL = getContextURL() + SERVLET_PATH;
  }
  
  @CMSAction
  public String showDocuments()
  {
    return "documentTree";
  }

  public void setHeaderBrowser(HtmlBrowser headerBrowser)
  {
    this.headerBrowser = headerBrowser;
  }

  public HtmlBrowser getHeaderBrowser()
  {
    MenuItemCursor mic = UserSessionBean.getCurrentInstance().
      getMenuModel().getSelectedMenuItem();  
    String docId =
      (String) mic.getDirectProperties().get(HEADER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(HEADER_DOCUMENT_PROPERTY);
      
    if (docId != null)
    {
      headerBrowser.setUrl(servletURL + docId);    
      return headerBrowser;
    }
    else
      return null;
  }

  public void setFooterBrowser(HtmlBrowser footerBrowser)
  {
    this.footerBrowser = footerBrowser;
  }

  public HtmlBrowser getFooterBrowser()
  {
    MenuItemCursor mic = 
      UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
    String docId =
      (String) mic.getDirectProperties().get(FOOTER_DOCID_PROPERTY);
    if (docId == null)
      docId = (String) mic.getDirectProperties().get(FOOTER_DOCUMENT_PROPERTY);
    if (docId != null)
    {
      footerBrowser.setUrl(servletURL + docId);    
      return footerBrowser;
    }
    else 
      return null;
  }

  public TreeModel getTreeData()
  {
    try
    {
      MenuItemCursor mic = 
        UserSessionBean.getCurrentInstance().getMenuModel().getSelectedMenuItem();
      String propertyName = 
        (String)mic.getProperties().get(FILTERPROPERTY_NAME_PROPERTY);
      if (propertyName == null)
      {
        propertyName =
          (String)mic.getProperties().get(TREE_FILTERPROPERTY_NAME_PROPERTY);
      }
      String propertyValue = 
        (String)mic.getProperties().get(FILTERPROPERTY_VALUE_PROPERTY);
      if (propertyValue == null)
      {
        propertyValue =
          (String)mic.getProperties().get(TREE_FILTERPROPERTY_VALUE_PROPERTY);
      }
    
      if (propertyName == null) return getEmptyModel();

      CachedDocumentManagerClient client = getDocumentManagerClient();
      DocumentFilter filter = new DocumentFilter();
      Property p = new Property();
      p.setName(propertyName);
      p.getValue().add(propertyValue);
      filter.getProperty().add(p);
      filter.setVersion(0);
      OrderByProperty ob = new OrderByProperty();
      ob.setName("title");
      filter.getOrderByProperty().add(ob);
      filter.setIncludeContentMetadata(true);
      
      List<Document> documents = client.findDocuments(filter);
      
      // make up table
      Table table2 = new Table(new String[]{
        "path", "uuid", "size", "icon", "filename"});
      
      for (Document document : documents)
      {
        String path = document.getTitle();
        Content content = document.getContent();
        String size = "";
        Number nsize = content.getSize();
        if (nsize != null)
          size = DocumentUtils.getSizeString(nsize.longValue());

        String uuid = content.getContentId();
        String mimeType = content.getContentType();
        String icon = DocumentBean.getContentTypeIcon(mimeType);
        
        String title = "document";
        int index = path.lastIndexOf("\\");
        if (index == -1) title = path;
        else title = path.substring(index + 1);
        
        String extension = MimeTypeMap.getMimeTypeMap().getExtension(mimeType);
        String filename = DocumentUtils.getFilename(title) + "." + extension;

        table2.addRow(new Object[]{path, uuid, size, icon, filename});
      }

      // create tree model
      TreeNode root = TreeModelUtils.createTree(table2, "\\\\", "path");

      TreeModelBase treeModel = new TreeModelBase(root);
      TreeStateBase state = new TreeStateBase();
      state.toggleExpanded("0"); // expand root
      // expand one level
      int count = root.getChildren().size();
      for (int i = 0; i < count; i++)
      {
        state.toggleExpanded("0:" + i);
      }
      treeModel.setTreeState(state);
      return treeModel;
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return getEmptyModel();
    }
  }

  private TreeModel getEmptyModel()
  {
    return new TreeModelBase(new ExtendedTreeNode("folder", "root", false));
  }

  private CachedDocumentManagerClient getDocumentManagerClient()
    throws Exception
  {
    CachedDocumentManagerClient client = new CachedDocumentManagerClient(
      UserSessionBean.getCurrentInstance().getUsername(),
      UserSessionBean.getCurrentInstance().getPassword());

    return client;
  }
}
