package org.santfeliu.faces.menu.view;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;

import org.santfeliu.faces.FacesUtils;
import org.santfeliu.faces.Translator;
import org.santfeliu.faces.menu.model.MenuItemCursor;
import org.santfeliu.faces.menu.model.MenuModel;
import org.santfeliu.faces.menu.util.MenuUtils;
import org.santfeliu.web.UserSessionBean;


public class HtmlTreeMenu extends UIComponentBase
  implements NamingContainer
{
  public static String DATA_FACET = "data";

  public static String EXPAND = "EXP:";
  public static String COLLAPSE = "COL:";

  private MenuModel _menuModel = null;
  private MenuItemCursor _currentMenuItem = null;
  
  private String _var;
  private String _baseMid;
  private String _style;
  private String _styleClass;
  private String _menuStyleClass;
  private String _selectedStyleClass;
  private String _unselectedStyleClass;
  private String _expandImageUrl;
  private String _collapseImageUrl;
  private Integer _expandDepth;
  private Boolean _expandSelected;
  private Boolean _headingsRender;
  private Integer _headingsBaseLevel;
  private String _headingsStyleClass;
  private Set _expandedMenuItems = new HashSet();
  private Boolean _enableDropdownButton;
  
  private final String MENU_DECORATED_PROPERTY = "menuDecorated";

  private final int MAX_CHILDREN = 1000;
  private static final Logger logger = Logger.getLogger("HtmlTreeMenu");
  
  public HtmlTreeMenu() throws Exception
  {
  }

  public String getFamily()
  {
    return "TreeMenu";
  }

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  /* remove it */
  public void setValue(Object value)
  {
  }
  
  /* remove it */
  public Object getValue()
  {
    return null;
  }

  public void setExpandDepth(Integer expandDepth)
  {
    this._expandDepth = expandDepth;
  }

  public Integer getExpandDepth()
  {
    if (_expandDepth != null) return _expandDepth;
    ValueBinding vb = getValueBinding("expandDepth");
    return vb != null ? (Integer)vb.getValue(getFacesContext()) : null;
  }

  public void setHeadingsBaseLevel(Integer headingsBaseLevel)
  {
    this._headingsBaseLevel = headingsBaseLevel;
  }

  public Integer getHeadingsBaseLevel()
  {
    if (_headingsBaseLevel != null) return _headingsBaseLevel;
    ValueBinding vb = getValueBinding("headingsBaseLevel");
    return vb != null ? (Integer)vb.getValue(getFacesContext()) : 3;
  }

  public void setExpandSelected(Boolean expandSelected)
  {
    this._expandSelected = expandSelected;
  }

  public Boolean getExpandSelected()
  {
    if (_expandSelected != null) return _expandSelected;
    ValueBinding vb = getValueBinding("expandSelected");
    return vb != null ? (Boolean)vb.getValue(getFacesContext()) : null;
  }

  public void setHeadingsRender(Boolean headingsRender)
  {
    this._headingsRender = headingsRender;
  }

  public Boolean getHeadingsRender()
  {
    if (_headingsRender != null) return _headingsRender;
    ValueBinding vb = getValueBinding("headingsRender");
    return vb != null ? (Boolean)vb.getValue(getFacesContext()) : Boolean.FALSE;
  }

  public void setExpandedMenuItems(Set set)
  {
    _expandedMenuItems = set;
  }
  
  // value binding has preference
  public Set getExpandedMenuItems()
  {
    ValueBinding vb = getValueBinding("expandedMenuItems");
    if (vb == null) return _expandedMenuItems;
    Set set = (Set)vb.getValue(getFacesContext());
    if (set != null) return set;
    vb.setValue(getFacesContext(), _expandedMenuItems);
    return _expandedMenuItems;
  }

  public void setVar(String var)
  {
    _var = var;
  }

  public String getVar()
  {
    return _var;
  }

  public void setBaseMid(String baseMid)
  {
    _baseMid = baseMid;
  }

  public String getBaseMid()
  {
    if (_baseMid != null) return _baseMid;
    ValueBinding vb = getValueBinding("baseMid");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  /* remove it */
  public void setMode(String mode)
  {
  }

  /* remove it */
  public String getMode()
  {
    return null;
  }

  public void setStyle(String style)
  {
    _style = style;
  }

  public String getStyle()
  {
    if (_style != null) return _style;
    ValueBinding vb = getValueBinding("style");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setStyleClass(String styleClass)
  {
    _styleClass = styleClass;
  }

  public String getStyleClass()
  {
    if (_styleClass != null) return _styleClass;
    ValueBinding vb = getValueBinding("styleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setMenuStyleClass(String menuStyleClass)
  {
    _menuStyleClass = menuStyleClass;
  }

  public String getMenuStyleClass()
  {
    if (_menuStyleClass != null) return _menuStyleClass;
    ValueBinding vb = getValueBinding("menuStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setHeadingsStyleClass(String headingsStyleClass)
  {
    _headingsStyleClass = headingsStyleClass;
  }

  public String getHeadingsStyleClass()
  {
    if (_headingsStyleClass != null) return _headingsStyleClass;
    ValueBinding vb = getValueBinding("headingsStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setSelectedStyleClass(String selectedStyleClass)
  {
    _selectedStyleClass = selectedStyleClass;
  }

  public String getSelectedStyleClass()
  {
    if (_selectedStyleClass != null) return _selectedStyleClass;
    ValueBinding vb = getValueBinding("selectedStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setUnselectedStyleClass(String unselectedStyleClass)
  {
    _unselectedStyleClass = unselectedStyleClass;
  }

  public String getUnselectedStyleClass()
  {
    if (_unselectedStyleClass != null) return _unselectedStyleClass;
    ValueBinding vb = getValueBinding("unselectedStyleClass");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public void setExpandImageUrl(String _expandImageUrl)
  {
    this._expandImageUrl = _expandImageUrl;
  }

  public String getExpandImageUrl()
  {
    if (_expandImageUrl != null) return _expandImageUrl;
    ValueBinding vb = getValueBinding("expandImageUrl");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }
  
  public void setCollapseImageUrl(String _collapseImageUrl)
  {
    this._collapseImageUrl = _collapseImageUrl;
  }

  public String getCollapseImageUrl()
  {
    if (_collapseImageUrl != null) return _collapseImageUrl;
    ValueBinding vb = getValueBinding("collapseImageUrl");
    return vb != null ? (String)vb.getValue(getFacesContext()) : null;
  }

  public Boolean getEnableDropdownButton()
  {
    if (_enableDropdownButton != null) return _enableDropdownButton;
    ValueBinding vb = getValueBinding("enableDropdownButton");
    return vb != null ? Boolean.valueOf((String)vb.getValue(getFacesContext())) : Boolean.FALSE;
  }

  public void setEnableDropdownButton(Boolean enableDropdownButton)
  {
    this._enableDropdownButton = enableDropdownButton;
  }

  public MenuItemCursor getCurrentMenuItem()
  {
    return _currentMenuItem;
  }

  public void setMenuItemVar(MenuItemCursor menuItem)
  {
    _currentMenuItem = menuItem;
    // update var cursor
    FacesContext context = FacesContext.getCurrentInstance();
    String var = getVar();
    if (_currentMenuItem == null)
    {
      if (var != null)
      {
        context.getExternalContext().getRequestMap().remove(var);
      }
    }
    else
    {
      if (var != null)
      {
        context.getExternalContext().getRequestMap().put(var, _currentMenuItem);
      }
    }
  }

  @Override
  public void processDecodes(FacesContext context)
  {
    if (context == null)
      throw new NullPointerException("context");

    if (!isRendered()) return;

    _menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
    MenuItemCursor baseMenuItem = getBaseMenuItem();

    setMenuItemVar(null);
    Integer depth = getExpandDepth();

    processMenuItemDecodes(context, baseMenuItem, depth);
    setMenuItemVar(null);

    try
    {
      decode(context);
    }
    catch (RuntimeException e)
    {
      context.renderResponse();
      throw e;
    }
  }

  @Override
  public void decode(FacesContext context)
  {
    Map parametersMap = FacesContext.getCurrentInstance().
      getExternalContext().getRequestParameterMap();

    String menuId = getClientId(context);
    Iterator iter = parametersMap.keySet().iterator();
    String action = null;
    while (iter.hasNext() && action == null)
    {
      // parameter: menuId:action:mid.x
      String parameter = (String)iter.next();
      if (parameter.startsWith(menuId))
      {
        action = parameter.substring(menuId.length() + 1);
      }
    }
    if (action != null)
    {
      if (action.startsWith(EXPAND))
      {
        String mid = action.substring(EXPAND.length(), action.length() - 2);
        getExpandedMenuItems().add(mid);
      }
      else if (action.startsWith(COLLAPSE))
      {
        String mid = action.substring(COLLAPSE.length(), action.length() - 2);
        getExpandedMenuItems().remove(mid);
      }
    }
  }

  @Override
  public void encodeBegin(FacesContext context) throws IOException
  {
    _menuModel = UserSessionBean.getCurrentInstance().getMenuModel();
  }

  @Override
  public void encodeChildren(FacesContext context) throws IOException
  {
    String formId = FacesUtils.getParentFormId(this, context);
    String menuId = getClientId(context);

    MenuItemCursor baseMenuItem = getBaseMenuItem();
    ResponseWriter writer = context.getResponseWriter();
    MenuUtils.encodeJavascript(context, writer, this);

    setMenuItemVar(null);

    writer.startElement("div", this);
    String style = getStyle();
    if (style != null)
    {
      writer.writeAttribute("style", style, null);
    }
    String styleClass = getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute("class", styleClass, null);
    }
    Integer depth = getExpandDepth();

    encodeChildMenuItems(baseMenuItem, writer, context, formId, menuId, depth, 
      getHeadingsBaseLevel());
    writer.endElement("div");

    setMenuItemVar(null);
  }

  @Override
  public void encodeEnd(FacesContext context) throws IOException
  {
  }

  @Override
  public String getClientId(FacesContext context)
  {
    String clientId = super.getClientId(context);
    MenuItemCursor currentMenuItem = getCurrentMenuItem();
    if (currentMenuItem == null)
    {
      return clientId;
    }
    return clientId + "_" + currentMenuItem.getMid();
  }

  @Override
  public void queueEvent(FacesEvent event)
  {
    super.queueEvent(new FacesEventWrapper(event, getCurrentMenuItem(), this));
  }

  @Override
  public void broadcast(FacesEvent event) throws AbortProcessingException
  {
    if (event instanceof FacesEventWrapper)
    {
      MenuItemCursor cursor = getCurrentMenuItem();
      setMenuItemVar(((FacesEventWrapper)event).getMenuItem());
      try
      {
        FacesEvent originalEvent = 
          ((FacesEventWrapper)event).getWrappedFacesEvent();
        originalEvent.getComponent().broadcast(originalEvent);
      }
      finally
      {
        setMenuItemVar(cursor);
      }
    }
    else
    {
      super.broadcast(event);
    }
  }

  @Override
  public Object saveState(FacesContext context)
  {
    Object values[] = new Object[17];
    values[0] = super.saveState(context);
    values[1] = _var;
    values[2] = _baseMid;
    values[3] = _style;
    values[4] = _styleClass;
    values[5] = _menuStyleClass;
    values[6] = _selectedStyleClass;
    values[7] = _unselectedStyleClass;
    values[8] = _expandImageUrl;
    values[9] = _collapseImageUrl;
    values[10] = _expandDepth;
    values[11] = _expandSelected;
    values[12] = _headingsRender;
    values[13] = _headingsBaseLevel;
    values[14] = _expandedMenuItems.toArray();
    values[15] = _headingsStyleClass; 
    values[16] = _enableDropdownButton;
    return values;
  }

  @Override
  public void restoreState(FacesContext context, Object state)
  {
    Object[] values = (Object[])state;
    super.restoreState(context, values[0]);
    _var = (String)values[1];
    _baseMid = (String)values[2];
    _style = (String)values[3];
    _styleClass = (String)values[4];
    _menuStyleClass = (String)values[5];
    _selectedStyleClass = (String)values[6];
    _unselectedStyleClass = (String)values[7];
    _expandImageUrl = (String)values[8];
    _collapseImageUrl = (String)values[9];
    _expandDepth = (Integer)values[10];
    _expandSelected = (Boolean)values[11];
    _headingsRender = (Boolean)values[12];
    _headingsBaseLevel = (Integer)values[13];
    Object[] items = (Object[])values[14];
    if (items != null)
    {
      _expandedMenuItems = new HashSet();
      for (int i = 0; i < items.length; i++)
      {
        _expandedMenuItems.add(items[i]);
      }
    }
    _headingsStyleClass = (String)values[15];
    _enableDropdownButton = (Boolean)values[16];
  }

  private static class FacesEventWrapper extends FacesEvent
  {
    private static final long serialVersionUID = 6648047334065628973L;
    private FacesEvent _wrappedFacesEvent;
    private MenuItemCursor _menuItem;

    public FacesEventWrapper(FacesEvent facesEvent, MenuItemCursor menuItem,
             HtmlTreeMenu redirectComponent)
    {
      super(redirectComponent);
      _wrappedFacesEvent = facesEvent;
      _menuItem = menuItem.getClone();
    }

    @Override
    public PhaseId getPhaseId()
    {
      return _wrappedFacesEvent.getPhaseId();
    }

    @Override
    public void setPhaseId(PhaseId phaseId)
    {
      _wrappedFacesEvent.setPhaseId(phaseId);
    }

    @Override
    public void queue()
    {
      _wrappedFacesEvent.queue();
    }

    @Override
    public String toString()
    {
      return _wrappedFacesEvent.toString();
    }

    public boolean isAppropriateListener(FacesListener faceslistener)
    {
      return _wrappedFacesEvent.isAppropriateListener(faceslistener);
    }

    public void processListener(FacesListener faceslistener)
    {
      _wrappedFacesEvent.processListener(faceslistener);
    }

    public FacesEvent getWrappedFacesEvent()
    {
      return _wrappedFacesEvent;
    }

    public MenuItemCursor getMenuItem()
    {
      return _menuItem;
    }
  }

  // ************* private methods *****************

  private MenuItemCursor getBaseMenuItem()
  {
    MenuItemCursor baseMenuItem;
    String baseMid = getBaseMid();
    
    if (baseMid == null)
    {
      baseMid = null; //TODO: getRoot()
    }
    baseMenuItem = _menuModel.getMenuItem(baseMid);
    if (baseMenuItem.isNull())
    {
      baseMid = null; //TODO: getRoot()
      baseMenuItem = _menuModel.getMenuItem(baseMid);
    }
    return baseMenuItem;
  }

  private void processMenuItemDecodes(FacesContext context,
    MenuItemCursor menuItem, Integer depth)
  {
    setMenuItemVar(menuItem);

    UIComponent dataComponent = getFacet(DATA_FACET);
    if (dataComponent != null)
    {
      // regenerate clientId (see spec 3.1.6)
      resetId(dataComponent);
      if (dataComponent.isRendered())
      {
        dataComponent.processDecodes(context);

        boolean decodeChildren = menuItem.hasChildren();
        if (Boolean.TRUE.equals(getExpandSelected()))
        {
          if (!menuItem.containsSelection()) decodeChildren = false;
        }
        if (decodeChildren)
        {
          Set expandedMenuItems = getExpandedMenuItems();
          boolean processChildrenDecodes;
          if (depth == null)
          {
            processChildrenDecodes = 
              expandedMenuItems.contains(menuItem.getMid());
          }
          else
          {
            if (depth.intValue() > 0)
            {
              processChildrenDecodes = true;
              depth = new Integer(depth.intValue() - 1);
            }
            else processChildrenDecodes = false;
          }

          if (processChildrenDecodes)
          {
            MenuItemCursor childMenuItem = menuItem.getFirstChild();
            while (!childMenuItem.isNull())
            {
              processMenuItemDecodes(context, childMenuItem, depth);
              childMenuItem = childMenuItem.getNext();
            }
          }
        }
      }
    }
  }

  private void encodeChildMenuItems(MenuItemCursor menuItem,
    ResponseWriter writer, FacesContext context, String formId, String menuId, 
    Integer depth, Integer headingsLevel) throws IOException
  {
    if (menuItem.hasChildren())
    {
      boolean encodeChildMenuItems;
      if (depth == null)
      {
        encodeChildMenuItems = true;
      }
      else if (depth.intValue() > 0)
      {
        encodeChildMenuItems = true;
        depth = new Integer(depth.intValue() - 1);
      }
      else // depth == 0 => stop
      {
        encodeChildMenuItems = false;
      }

      // encode children if necessary
      if (encodeChildMenuItems)
      {
        if (isAnyChildMenuItemVisible(menuItem))
        {
          setMenuItemVar(menuItem);          
          writer.startElement("ul", this);
          String menuStyleClass = getMenuStyleClass();
          if (menuStyleClass != null)
          {
            writer.writeAttribute("class", menuStyleClass, null);
            if (menuStyleClass.contains("popup") && 
              isMenuItemDecorated(menuItem))
            {
              // add extra div tag for border decoration
              writer.startElement("div", this);
              writer.endElement("div");
            }
          }
          MenuItemCursor child = menuItem.getFirstChild();
          int childCount = 0;
          while (!child.isNull() && childCount < MAX_CHILDREN)
          {
            encodeMenuItem(child, writer, context, formId, menuId, depth, 
              headingsLevel + 1);
            child.moveNext();
            childCount++;
          }
          if (childCount == MAX_CHILDREN)
          {
            logger.log(Level.SEVERE, "Max children exceeded in node {0}", 
              new Object[]{menuItem.getMid()});
          }
          writer.endElement("ul");
        }
      }
    }
  }

  private void encodeMenuItem(MenuItemCursor menuItem, ResponseWriter writer, 
    FacesContext context, String formId, String menuId, Integer depth, 
    Integer headingsLevel) throws IOException
  {
    setMenuItemVar(menuItem);
    if (isChildComponentRendered())
    {
      // start tag li
      writer.startElement("li", this);
      // apply styles
      String styleClass = null;
      if (menuItem.containsSelection()) // selected menuItem
      {
        styleClass = getSelectedStyleClass();
      }
      else // unselected menuItem
      {
        styleClass = getUnselectedStyleClass();
      }
      if (styleClass != null || menuItem.hasChildren())
      {
        String sclass = styleClass != null ? styleClass : "";
        if (depth == null || depth > 0)
          sclass = sclass + " " + (menuItem.hasChildren() ? "has-children" : "");
        writer.writeAttribute("class", sclass, null);
      }

      boolean encodeChildren;
      if (depth == null) // depth don't care
      {
        if (menuItem.hasChildren())
        {
          boolean isExpanded = 
            getExpandedMenuItems().contains(menuItem.getMid());
          String togglerAction = isExpanded ? COLLAPSE : EXPAND;
          encodeToggler(menuItem, writer, menuId, togglerAction, context);
          encodeChildren = isExpanded;
        }
        else encodeChildren = false;
      }
      else encodeChildren = true;

      // encode heading
      boolean encodeHeading = false;
      if (Boolean.TRUE.equals(getHeadingsRender()))
      {
        if (depth == null || depth > 0)
        {
          boolean expandMode = false;
          if (getExpandSelected() != null) expandMode = getExpandSelected();            
          if (expandMode)
          {
            try
            {
              String parentSelectedMid = UserSessionBean.getCurrentInstance().
                getSelectedMenuItem().getParent().getMid();
              encodeHeading = !menuItem.isDescendantOf(parentSelectedMid);
            }
            catch (Exception ex) { }            
          }
          else
          {
            //encodeHeading = menuItem.hasChildren();
            encodeHeading = true;
          }
        }
      }
      
      if (encodeHeading)
      {
        writer.startElement("h" + (headingsLevel <= 6 ? headingsLevel : 6), this);
        String headingsStyleClass = getHeadingsStyleClass();
        if (headingsStyleClass == null)
        {
          headingsStyleClass = "element-invisible";
        }
        writer.writeAttribute("class", headingsStyleClass, null);
        writer.write(getTranslation(menuItem.getLabel()));
        writer.endElement("h" + (headingsLevel <= 6 ? headingsLevel : 6));
      }
      
      // encode data
      encodeData(menuItem, writer, context);

      // encode children
      if (encodeChildren)
      {
        boolean enableDropdownButton = getEnableDropdownButton();        
        if (enableDropdownButton && (depth == null || depth > 0) && menuItem.hasChildren())
          encodeDropdownButton(menuItem, writer, context);
        
        encodeChildMenuItems(menuItem, writer, context, formId, menuId, depth, 
          headingsLevel);
      }

      // end tag li
      writer.endElement("li");
    }
  }

  private void encodeToggler(MenuItemCursor menuItem,
    ResponseWriter writer, String menuId, String action, FacesContext context) throws IOException
  {
    String imageUrl;
    String title;
    
    Locale locale = context.getViewRoot().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.menu.resources.TreeMenuBundle", locale);      
    
    if (EXPAND.equals(action))
    {
      imageUrl = getExpandImageUrl();
      title = bundle.getString("expandNode") + " " 
        + getTranslation(menuItem.getLabel());
    }
    else // COLLAPSE
    {
      imageUrl = getCollapseImageUrl();
      title = bundle.getString("collapseNode") + " " 
        + getTranslation(menuItem.getLabel());
    }

    if (imageUrl != null)
    {
      String mid = menuItem.getMid();
      writer.startElement("input", this);
      writer.writeAttribute("type", "image", null);
      writer.writeAttribute("name", menuId + ":" + action + mid, null);
      writer.writeAttribute("src", imageUrl, null);
      writer.writeAttribute("class", "toggler", null);
      writer.writeAttribute("alt", title, null);
      writer.writeAttribute("title", title, null);
      writer.endElement("input");
    }
  }
  
  private void encodeData(MenuItemCursor menuItem, 
                          ResponseWriter writer,
                          FacesContext context) throws IOException
  {
    UIComponent component = getFacet(DATA_FACET);
    if (component != null)
    {
      // regenerate clientId (see spec 3.1.6)
      resetId(component);
      RendererUtils.renderChild(context, component);
    }
    else
    {
      char[] ch = menuItem.getLabel().toCharArray();
      writer.writeText(ch, 0, ch.length);
    }
  }
  
  private void encodeDropdownButton(MenuItemCursor menuItem, 
                          ResponseWriter writer, FacesContext context) throws IOException
  {
    Locale locale = context.getViewRoot().getLocale();
    ResourceBundle bundle = ResourceBundle.getBundle(
      "org.santfeliu.faces.menu.resources.TreeMenuBundle", locale);    
    
    writer.startElement("button", this);    
    writer.startElement("span", this);    
    writer.startElement("span", this);  
    writer.writeAttribute("class", "visuallyhidden", null);
    writer.write(bundle.getString("showSubmenuFor") + " '" + getTranslation(menuItem.getLabel()) + "'");
    writer.endElement("span");          
    writer.endElement("span");    
    writer.endElement("button");    
  }
  
  private void resetId(UIComponent component)
  {
    if (!component.isRendered()) return;
    
    component.setId(component.getId());

    List list = component.getChildren();
    Iterator iter = list.iterator();
    while (iter.hasNext())
    {
      UIComponent child = (UIComponent)iter.next();
      resetId(child);
    }
  }

  private boolean isAnyChildMenuItemVisible(MenuItemCursor menuItem)
  {
    // if expandSelected enabled, do not encode unselected items
    if (Boolean.TRUE.equals(getExpandSelected()))
    {
      if (!menuItem.containsSelection()) return false;
    }
    // find at least one child menu item that must be rendered
    boolean found = false;
    int childCount = 0;
    MenuItemCursor child = menuItem.getFirstChild();
    while (!child.isNull() && !found && childCount < MAX_CHILDREN)
    {
      setMenuItemVar(child);
      found = isChildComponentRendered();
      child.moveNext();
      childCount++;
    }
    if (childCount == MAX_CHILDREN)
    {
      logger.log(Level.SEVERE, "Max children exceeded in node {0}", 
        new Object[]{menuItem.getMid()});
    }
    return found;
  }
  
  private boolean isMenuItemDecorated(MenuItemCursor menuItem)
  {
    String decorated = menuItem.getProperty(MENU_DECORATED_PROPERTY);
    if (decorated != null) return Boolean.parseBoolean(decorated);
    else return true;
  }

  private boolean isChildComponentRendered()
  {
    UIComponent component = (UIComponent)getFacet(DATA_FACET);
    return (component == null) ? false : component.isRendered();
  }
  
  private String getTranslation(String text) throws IOException
  {
    try
    {
      if (text != null)
      {  
        Translator translator = 
          UserSessionBean.getCurrentInstance().getTranslator();
        if (translator != null)
        {
          String userLanguage = FacesUtils.getViewLanguage();
          String translationGroup = 
            UserSessionBean.getCurrentInstance().getTranslationGroup();
          StringWriter sw = new StringWriter();
          translator.translate(new StringReader(text), sw, "text/plain",
            userLanguage, translationGroup);
          return sw.toString();
        }      
      }
    }
    catch (Exception ex) 
    {
      
    }
    return text;
  }
  
}
