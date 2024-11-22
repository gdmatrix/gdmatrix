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
package org.santfeliu.faces.render.myfaces.datascroller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import org.apache.myfaces.component.html.ext.HtmlCommandLink;
import org.apache.myfaces.custom.datascroller.HtmlDataScroller;
import org.apache.myfaces.custom.datascroller.ScrollerActionEvent;
import org.apache.myfaces.shared_tomahawk.renderkit.JSFAttr;
import org.apache.myfaces.shared_tomahawk.renderkit.RendererUtils;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HTML;
import org.apache.myfaces.shared_tomahawk.renderkit.html.HtmlRenderer;
import org.santfeliu.faces.component.HtmlAriaCommandLink;

/**
 * This class is a version of org.apache.myfaces.custom.datascroller.HtmlDataScrollerRenderer
 * to correct the problem with multiple scrollers rendered on the same page with
 * the same id (not same clientId).
 *
 * @author blanquepa
 */
@FacesRenderer(componentFamily="javax.faces.Panel",
  rendererType="org.apache.myfaces.DataScroller")
public class HtmlDataScrollerRenderer extends HtmlRenderer
{

  public static final String RENDERER_TYPE = "org.apache.myfaces.DataScroller";
  protected static final String PAGE_NAVIGATION = "idx".intern();

  @Override
  public boolean getRendersChildren()
  {
    return true;
  }

  /**
   * Determine which datascroller navigation option the user chose (if any),
   * and if they did then queue the appropriate ScrollerActionEvent for
   * later execution.
   */
  @Override
  public void decode(FacesContext context, UIComponent component)
  {
    RendererUtils.checkParamValidity(context, component, HtmlDataScroller.class);

    Map parameter = context.getExternalContext().getRequestParameterMap();
    String param = (String) parameter.get(component.getClientId(context));
    if (param != null && param.length() > 0)
    {
      if (param.startsWith(PAGE_NAVIGATION))
      {
        // the user chose a specific page# to jump to
        component.queueEvent(new ScrollerActionEvent(component, Integer.parseInt(param.substring(PAGE_NAVIGATION.length(), param.length()))));
      } else
      {
        // the user chose first/last/prev/next/fastrewind/fastforward
        component.queueEvent(new ScrollerActionEvent(component, param));
      }
    }
  }

  /**
   * Expose much of the internal state of this component so that UIComponents
   * nested within this component can very flexibly display the component state
   * to the user.
   */
  protected void setVariables(FacesContext facescontext, HtmlDataScroller scroller)
    throws IOException
  {
    Map requestMap = facescontext.getExternalContext().getRequestMap();

    String pageCountVar = scroller.getPageCountVar();
    if (pageCountVar != null)
    {
      int pageCount = scroller.getPageCount();
      requestMap.put(pageCountVar, pageCount);
    }
    String pageIndexVar = scroller.getPageIndexVar();
    if (pageIndexVar != null)
    {
      int pageIndex = (scroller.getRowCount() > 0) ? scroller.getPageIndex() : 0;
      requestMap.put(pageIndexVar, pageIndex);
    }
    String rowsCountVar = scroller.getRowsCountVar();
    if (rowsCountVar != null)
    {
      int rowsCount = scroller.getRowCount();
      requestMap.put(rowsCountVar, rowsCount);
    }
    String displayedRowsCountVar = scroller.getDisplayedRowsCountVar();
    if (displayedRowsCountVar != null)
    {
      int displayedRowsCount = scroller.getRows();
      int max = scroller.getRowCount() - scroller.getFirstRow();
      if (displayedRowsCount > max)
      {
        displayedRowsCount = max;
      }
      requestMap.put(displayedRowsCountVar, displayedRowsCount);
    }
    String firstRowIndexVar = scroller.getFirstRowIndexVar();
    if (firstRowIndexVar != null)
    {
      int firstRowIndex = (scroller.getRowCount() > 0) ? scroller.getFirstRow() + 1 : 0;
      requestMap.put(firstRowIndexVar, firstRowIndex);
    }
    String lastRowIndexVar = scroller.getLastRowIndexVar();
    if (lastRowIndexVar != null)
    {
      int lastRowIndex = scroller.getFirstRow() + scroller.getRows();
      int count = scroller.getRowCount();
      if (lastRowIndex > count)
      {
        lastRowIndex = count;
      }
      requestMap.put(lastRowIndexVar, lastRowIndex);
    }
  }

  public void removeVariables(FacesContext facescontext, HtmlDataScroller scroller)
    throws IOException
  {
    Map requestMap = facescontext.getExternalContext().getRequestMap();

    String pageCountVar = scroller.getPageCountVar();
    if (pageCountVar != null)
    {
      requestMap.remove(pageCountVar);
    }
    String pageIndexVar = scroller.getPageIndexVar();
    if (pageIndexVar != null)
    {
      requestMap.remove(pageIndexVar);
    }
    String rowsCountVar = scroller.getRowsCountVar();
    if (rowsCountVar != null)
    {
      requestMap.remove(rowsCountVar);
    }
    String displayedRowsCountVar = scroller.getDisplayedRowsCountVar();
    if (displayedRowsCountVar != null)
    {
      requestMap.remove(displayedRowsCountVar);
    }
    String firstRowIndexVar = scroller.getFirstRowIndexVar();
    if (firstRowIndexVar != null)
    {
      requestMap.remove(firstRowIndexVar);
    }
    String lastRowIndexVar = scroller.getLastRowIndexVar();
    if (lastRowIndexVar != null)
    {
      requestMap.remove(lastRowIndexVar);
    }
  }

  @Override
  public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
  {
    super.encodeBegin(facesContext, uiComponent);

    RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlDataScroller.class);

    HtmlDataScroller scroller = (HtmlDataScroller) uiComponent;

    setVariables(facesContext, scroller);
  }

  @Override
  public void encodeChildren(FacesContext facescontext, UIComponent uicomponent)
    throws IOException
  {
    RendererUtils.checkParamValidity(facescontext, uicomponent, HtmlDataScroller.class);

    // TOMAHAWK-1463 Don't render paginator links twice!
    if (uicomponent.getChildCount() > 0)
    {
      HtmlDataScroller scroller = (HtmlDataScroller) uicomponent;
      String scrollerIdPagePrefix = scroller.getId() + HtmlDataScrollerRenderer.PAGE_NAVIGATION;

      for (Iterator it = uicomponent.getChildren().iterator(); it.hasNext();)
      {
        UIComponent child = (UIComponent) it.next();
        String childId = child.getId();

        if (childId != null && !childId.startsWith(scrollerIdPagePrefix))
        {
          RendererUtils.renderChild(facescontext, child);
        }
      }
    }
  }

  @Override
  public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException
  {
    RendererUtils.checkParamValidity(facesContext, uiComponent, HtmlDataScroller.class);

    HtmlDataScroller scroller = (HtmlDataScroller) uiComponent;

    if (scroller.getUIData() == null)
    {
      return;
    }

    renderScroller(facesContext, scroller);
    removeVariables(facesContext, scroller);
  }

  protected void renderScroller(FacesContext facesContext, HtmlDataScroller scroller)
    throws IOException
  {
    ResponseWriter writer = facesContext.getResponseWriter();

    if (!scroller.isRenderFacetsIfSinglePage() && scroller.getPageCount() <= 1)
    {
      return;
    }

    if (scroller.getFirst() == null && scroller.getFastRewind() == null
      && scroller.getPrevious() == null && !scroller.isPaginator()
      && scroller.getNext() == null && scroller.getFastForward() == null
      && scroller.getLast() == null)
    {
      return;
    }

    writeScrollerStart(writer, scroller);
    String styleClass = scroller.getStyleClass();
    if (styleClass != null)
    {
      writer.writeAttribute(HTML.CLASS_ATTR, styleClass, JSFAttr.STYLE_CLASS_ATTR);
    }
    String style = scroller.getStyle();
    if (style != null)
    {
      writer.writeAttribute(HTML.STYLE_ATTR, style, JSFAttr.STYLE_ATTR);
    }
    writeScrollerRowStart(writer, scroller);

    boolean startActive = (scroller.getPageIndex() != 1);

    boolean endActive = (scroller.getPageIndex() != scroller.getPageCount());

    UIComponent facetComp = scroller.getFirst();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("firstStyleClass", scroller.getFirstStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_FIRST, startActive,
        scroller.isRenderFacetLinksIfFirstPage(), scroller.isDisableFacetLinksIfFirstPage());
      writeScrollerElementEnd(writer, scroller);
    }
    facetComp = scroller.getFastRewind();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("fastrStyleClass", scroller.getFastrStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_FAST_REWIND, startActive,
        scroller.isRenderFacetLinksIfFirstPage(), scroller.isDisableFacetLinksIfFirstPage());
      writeScrollerElementEnd(writer, scroller);
    }
    facetComp = scroller.getPrevious();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("previous", scroller.getPreviousStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_PREVIOUS, startActive,
        scroller.isRenderFacetLinksIfFirstPage(), scroller.isDisableFacetLinksIfFirstPage());
      writeScrollerElementEnd(writer, scroller);
    }
    if (scroller.isPaginator())
    {
      if (!scroller.isSingleElementLayout())
      {
        writeScrollerElementStart(writer, scroller);
      }
      renderPaginator(facesContext, scroller);
      if (!scroller.isSingleElementLayout())
      {
        writeScrollerElementEnd(writer, scroller);
      }
    }
    facetComp = scroller.getNext();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("next", scroller.getNextStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_NEXT, endActive,
        scroller.isRenderFacetLinksIfLastPage(), scroller.isDisableFacetLinksIfLastPage());
      writeScrollerElementEnd(writer, scroller);
    }
    facetComp = scroller.getFastForward();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("fastf", scroller.getFastfStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_FAST_FORWARD, endActive,
        scroller.isRenderFacetLinksIfLastPage(), scroller.isDisableFacetLinksIfLastPage());
      writeScrollerElementEnd(writer, scroller);
    }
    facetComp = scroller.getLast();
    if (facetComp != null)
    {
      writeScrollerElementStart(writer, scroller);
      writeStyleClass("last", scroller.getLastStyleClass(), writer);
      renderFacet(facesContext, scroller, facetComp, HtmlDataScroller.FACET_LAST, endActive,
        scroller.isRenderFacetLinksIfLastPage(), scroller.isDisableFacetLinksIfLastPage());
      writeScrollerElementEnd(writer, scroller);
    }

    writeScrollerRowEnd(writer, scroller);
    writeScrollerEnd(writer, scroller);
  }

  private void writeStyleClass(String jsfAttrName, String styleClass, ResponseWriter writer) throws IOException
  {
    if (styleClass != null)
    {
      writer.writeAttribute(HTML.CLASS_ATTR, styleClass, jsfAttrName);
    }
  }

  private boolean isListLayout(HtmlDataScroller scroller)
  {
    return scroller.isListLayout();
  }

  protected void writeScrollerEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.endElement(isListLayout(scroller) ? HTML.UL_ELEM : HTML.TABLE_ELEM);
  }

  protected void writeScrollerRowEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    if (!isListLayout(scroller))
    {
      writer.endElement(HTML.TR_ELEM);
    }
  }

  protected void writeScrollerElementEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.endElement(isListLayout(scroller) ? HTML.LI_ELEM : HTML.TD_ELEM);
  }

  protected void writeScrollerElementStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.startElement(isListLayout(scroller) ? HTML.LI_ELEM : HTML.TD_ELEM, scroller);
  }

  protected void writeScrollerRowStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    if (!isListLayout(scroller))
    {
      writer.startElement(HTML.TR_ELEM, scroller);
    }
  }

  protected void writeScrollerStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.startElement(isListLayout(scroller) ? HTML.UL_ELEM : HTML.TABLE_ELEM, scroller);
  }

  protected void renderFacet(FacesContext facesContext, HtmlDataScroller scroller,
    UIComponent facetComp, String facetName, boolean active, boolean renderLinks, boolean disableLinks) throws IOException
  {
    String onclick = scroller.getOnclick();
    String ondblclick = scroller.getOndblclick();

    HtmlAriaCommandLink link = getLink(facesContext, scroller, facetName);
    ValueExpression ve = facetComp.getValueExpression("title");
    String title = (String)(ve != null ? ve.getValue(facesContext.getELContext()) : "");
//    ValueBinding title = facetComp.getValueBinding("title");
    link.setAriaLabel(title != null? (String) title: null);

    if (onclick != null)
    {
      link.setOnclick(onclick);
    }

    if (ondblclick != null)
    {
      link.setOndblclick(ondblclick);
    }

    if (active)
    {
      if (disableLinks && link.isDisabled())
      {
        //Enable because the facet is active
        link.setDisabled(false);
      }
      link.encodeBegin(facesContext);
    } else if (renderLinks)
    {
      if (disableLinks && !link.isDisabled())
      {
        //Disable because the facet is not active
        link.setDisabled(true);
      }
      link.encodeBegin(facesContext);
    }

    facetComp.encodeBegin(facesContext);
    if (!facetComp.getRendersChildren())
    {
      facetComp.encodeChildren(facesContext);
    }
    facetComp.encodeEnd(facesContext);

    if (active || renderLinks)
    {
      link.encodeEnd(facesContext);
    }
  }

  /**
   * The "paginator" is a sequence of page numbers which the user can click
   * on to leap directly to a specific page of data.
   */
  protected void renderPaginator(FacesContext facesContext, HtmlDataScroller scroller)
    throws IOException
  {
    ResponseWriter writer = facesContext.getResponseWriter();

    int maxPages = scroller.getPaginatorMaxPages();
    if (maxPages <= 1)
    {
      maxPages = 2;
    }
    int pageCount = scroller.getPageCount();
    if (pageCount <= 1)
    {
      return;
    }
    int pageIndex = scroller.getPageIndex();
    int delta = maxPages / 2;

    int pages;
    int start;
    if (pageCount > maxPages && pageIndex > delta)
    {
      pages = maxPages;
      start = pageIndex - pages / 2 - 1;
      if (start + pages > pageCount)
      {
        start = pageCount - pages;
      }
    } else
    {
      pages = pageCount < maxPages ? pageCount : maxPages;
      start = 0;
    }

    if (!scroller.isSingleElementLayout())
    {
      writePaginatorStart(writer, scroller);

      String styleClass = scroller.getPaginatorTableClass();
      if (styleClass != null)
      {
        writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "paginatorTableClass");
      }
      String style = scroller.getPaginatorTableStyle();
      if (style != null)
      {
        writer.writeAttribute(HTML.STYLE_ATTR, style, "paginatorTableStyle");
      }

      writePaginatorRowStart(writer, scroller);
    }

    String onclick = scroller.getOnclick();
    String ondblclick = scroller.getOndblclick();

    // TOMAHAWK-596 TOMAHAWK-1249 Duplicate id exception for HtmlDataScrollerRenderer
    // Prevent render old paginator links removing it from tree.
    // Note that this only happens when transient components are not removed
    // from component tree on save and restore phase.
    if (scroller.getChildCount() != 0)
    {
      String scrollerIdPagePrefix = scroller.getId() + HtmlDataScrollerRenderer.PAGE_NAVIGATION;
      for (Iterator it = scroller.getChildren().iterator(); it.hasNext();)
      {
        UIComponent child = (UIComponent) it.next();
        String childId = child.getId();
        if (childId != null && childId.startsWith(scrollerIdPagePrefix))
        {
          try
          {
            int p = Integer.parseInt(childId.substring(scrollerIdPagePrefix.length()));
            if (p < start && p >= start + pages)
            {
              //Remove from child list
              it.remove();
            }
          } catch (NumberFormatException e)
          {
            //Do nothing because this component does not have the expected id format
          }
        }
      }
    }

    for (int i = start, size = start + pages; i < size; i++)
    {
      int idx = i + 1;
      writePaginatorElementStart(writer, scroller);
      String cStyleClass;
      String cStyle;
      if (idx == pageIndex)
      {
        cStyleClass = scroller.getPaginatorActiveColumnClass();
        cStyle = scroller.getPaginatorActiveColumnStyle();
      } else
      {
        cStyleClass = scroller.getPaginatorColumnClass();
        cStyle = scroller.getPaginatorColumnStyle();
      }
      if (cStyleClass != null)
      {
        writer.writeAttribute(HTML.CLASS_ATTR, cStyleClass, idx == pageIndex ? "paginatorActiveColumnClass" : "paginatorColumnClass");
      }
      if (cStyle != null)
      {
        writer.writeAttribute(HTML.STYLE_ATTR, cStyle, idx == pageIndex ? "paginatorActiveColumnStyle" : "paginatorColumnStyle");
      }

      if (idx == pageIndex && !scroller.isPaginatorRenderLinkForActive())
      {
        writer.write(Integer.toString(idx));
      } else
      {
        HtmlCommandLink link = getLink(facesContext, scroller, Integer.toString(idx), idx);
        if (onclick != null)
        {
          link.setOnclick(onclick);
        }
        if (ondblclick != null)
        {
          link.setOndblclick(ondblclick);
        }

        Locale locale = facesContext.getViewRoot().getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle(
          "org.santfeliu.faces.render.myfaces.datascroller.resources.ScrollerBundle", locale);
        link.setTitle(bundle.getString("page") + " " + Integer.toString(idx));
        if (link instanceof HtmlAriaCommandLink)
          ((HtmlAriaCommandLink)link).setAriaLabel(bundle.getString("page") + " " + Integer.toString(idx));
        link.encodeBegin(facesContext);
        link.encodeChildren(facesContext);
        link.encodeEnd(facesContext);
      }

      writePaginatorElementEnd(writer, scroller);
    }

    if (!scroller.isSingleElementLayout())
    {
      writePaginatorRowEnd(writer, scroller);
      writePaginatorEnd(writer, scroller);
    }
  }

  protected void writePaginatorEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.endElement(isListLayout(scroller) ? HTML.UL_ELEM : HTML.TABLE_ELEM);
  }

  protected void writePaginatorRowEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    if (!isListLayout(scroller))
    {
      writer.endElement(HTML.TR_ELEM);
    }
  }

  protected void writePaginatorElementEnd(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.endElement(isListLayout(scroller) ? HTML.LI_ELEM : HTML.TD_ELEM);
  }

  protected void writePaginatorElementStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.startElement(isListLayout(scroller) ? HTML.LI_ELEM : HTML.TD_ELEM, scroller);
  }

  protected void writePaginatorRowStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    if (!isListLayout(scroller))
    {
      writer.startElement(HTML.TR_ELEM, scroller);
    }
  }

  protected void writePaginatorStart(ResponseWriter writer, HtmlDataScroller scroller) throws IOException
  {
    writer.startElement(isListLayout(scroller) ? HTML.UL_ELEM : HTML.TABLE_ELEM, scroller);
  }

  protected HtmlCommandLink getLink(FacesContext facesContext, HtmlDataScroller scroller,
    String text, int pageIndex)
  {
    String id = HtmlDataScrollerRenderer.PAGE_NAVIGATION + Integer.toString(pageIndex);

    Application application = facesContext.getApplication();

    // TOMAHAWK-596 Duplicate id exception for HtmlDataScrollerRenderer
    // For prevent this condition, try to detect if there is a component.
    // Theorically this method should always return null, but it is known
    // than in some cases (portlet) when the state manager is different from
    // the default one, transient components are saved on the component tree.
    // Really the error is on the jsf portlet bridge implementation used, but
    // do this does not cause any side effect.
    HtmlCommandLink link = (HtmlCommandLink) scroller.findComponent(scroller.getId() + id);
    if (link == null)
    {
      // See Jira Issue TOMAHAWK-117 http://issues.apache.org/jira/browse/TOMAHAWK-117
      //     and http://issues.apache.org/jira/browse/MYFACES-1809
      link = new HtmlAriaCommandLink();

      link.setId(scroller.getId() + id);
      link.setTransient(true);
      List children = link.getChildren();
      if (text != null)
      {
        HtmlOutputText uiText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        uiText.setId(scroller.getId() + id + "_text");
        uiText.setTransient(true);
        uiText.setValue(text);
        children.add(uiText);
      }
      scroller.getChildren().add(link);
    }
    else
    {
      UIOutput uiText = (UIOutput) link.findComponent(scroller.getId() + id + "_text");
      if (uiText != null)
      {
        //Update text value
        uiText.setValue(text);
      }

      removeParameterFromList(link.getChildren(), scroller.getId() + id + "_param");
    }

    UIParameter parameter = (UIParameter) application.createComponent(UIParameter.COMPONENT_TYPE);
    parameter.setId(scroller.getId() + id + "_param");
    parameter.setTransient(true);
    parameter.setName(scroller.getClientId(facesContext));
    parameter.setValue(id);
    List children = link.getChildren();
    children.add(parameter);

    return link;
  }

  protected HtmlAriaCommandLink getLink(FacesContext facesContext, HtmlDataScroller scroller,
    String facetName)
  {
    Application application = facesContext.getApplication();

    // TOMAHAWK-596 TOMAHAWK-1249 Duplicate id exception for HtmlDataScrollerRenderer
    // For prevent this condition, try to detect if there is a component.
    // Theorically this method should always return null, but it is known
    // than in some cases (portlet) when the state manager is different from
    // the default one, transient components are saved on the component tree.
    // Really the error is on the jsf portlet bridge implementation used, but
    // do this does not cause any side effect.
    HtmlAriaCommandLink link = (HtmlAriaCommandLink) scroller.findComponent(scroller.getId() + facetName);
    if (link == null)
    {
      // See Jira Issue TOMAHAWK-117 http://issues.apache.org/jira/browse/TOMAHAWK-117
      //     and http://issues.apache.org/jira/browse/MYFACES-1809
      link = new HtmlAriaCommandLink();

      link.setId(scroller.getId() + facetName);
      link.setTransient(true);
    }
    else
      removeParameterFromList(link.getChildren(), scroller.getId() + facetName + "_param");

    UIParameter parameter = (UIParameter) application.createComponent(UIParameter.COMPONENT_TYPE);
    parameter.setId(scroller.getId() + facetName + "_param");
    parameter.setTransient(true);
    parameter.setName(scroller.getClientId(facesContext));
    parameter.setValue(facetName);
    List children = link.getChildren();
    children.add(parameter);
    scroller.getChildren().add(link);

    return link;
  }

  private void removeParameterFromList(List list, String parameter)
  {
    UIParameter remove = null;
    for (Object child : list)
    {
      if (child instanceof UIParameter)
      {
        UIParameter param = (UIParameter) child;
        if (param.getId().equals(parameter))
          remove = param;
      }
    }
    if (remove != null)
    {
      list.remove(remove);
    }
  }
}
