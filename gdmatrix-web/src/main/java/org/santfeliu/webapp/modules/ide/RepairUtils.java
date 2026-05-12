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
package org.santfeliu.webapp.modules.ide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.santfeliu.form.View;
import org.santfeliu.form.type.html.HtmlForm;
import org.santfeliu.form.type.html.HtmlView;

/**
 *
 * @author granadogj
 */
public class RepairUtils
{

  private static final Logger LOGGER = Logger.getLogger(RepairUtils.class.getName());

  private static final int INFO_TEXTS_MAX_DISTANCE = 20;
  private static final int ROW_THRESHOLD = 20;
  private static final int MAX_HORIZONTAL_DIF = 300;
  private static final int MAX_VERTICAL_DIF = 60;
  private static final int COMPONENT_POSITION_ERROR_MARGIN = 8;

  private static final Pattern POSITION_PATTERN = Pattern.compile("(top|left)\\s*:\\s*([0-9]+)", Pattern.CASE_INSENSITIVE);

  private final Map<HtmlView, Map<String, Integer>> positionCache = new HashMap<>();
  private final Map<HtmlView, Boolean> scriptCache = new HashMap<>();

  public RepairUtils()
  {
  }

  public List<HtmlView> extractViews(HtmlForm htmlForm, String containerId)
  {
    positionCache.clear();
    scriptCache.clear();

    View root = htmlForm.getRootView();
    HtmlView container = (HtmlView) findView(root, containerId);

    if (container == null)
    {
      LOGGER.log(Level.WARNING, "Contenedor no encontrado: {0}", containerId);
      return new ArrayList<>();
    }

    List<HtmlView> absoluteViews = new ArrayList<>();
    collectAbsoluteViews(container, absoluteViews);
    return absoluteViews;
  }

  // Repair VisualOrder
  public void performSort(List<HtmlView> views)
  {
    if (views == null || views.isEmpty())
    {
      return;
    }

    performUnifiedCleanup(views);
    sortViews(views);
  }

  // Button repairLabels
  public void performLabelsAndGroupsRepair(List<HtmlView> views)
  {
    if (views == null || views.isEmpty())
    {
      return;
    }
    
    performUnifiedCleanup(views);
    processRadioGroups(views);
    processStandardLabels(views);
  }

  public void rebuildHtml(HtmlForm htmlForm, String containerId, List<HtmlView> processedViews, Boolean assignOrder)
  {
    View root = htmlForm.getRootView();
    HtmlView container = (HtmlView) findView(root, containerId);

    if (container != null && processedViews != null && !processedViews.isEmpty())
    {
      container.getChildren().clear();
      int outputOrder = 1;

      for (HtmlView view : processedViews)
      {
        if (!containsScript(view))
        {
          ensureSelectHasOption(view);
        }

        if (Boolean.TRUE.equals(assignOrder))
        {
          view.setProperty("data-outputorder", String.valueOf(outputOrder++));
        }

        container.getChildren().add(view);
      }
    }
    positionCache.clear();
    scriptCache.clear();
  }
  
  private void performUnifiedCleanup(List<HtmlView> views)
  {
    for (HtmlView view : views)
    {
      deepClean(view);
    }
  }

    /**
   * Walk the tree once and apply all cleaning rules: 1. If
   * it's a <script>, fixes the text content (removes u00A0). 2. If
   * container (<fieldset> or has scripts), deletes junk children. 3. Continue
   * down recursively.
   */
  private void deepClean(HtmlView view)
  {
    boolean isScript = "script".equalsIgnoreCase(view.getNativeViewType());
    boolean isFieldset = "fieldset".equalsIgnoreCase(view.getNativeViewType());
    boolean hasScriptChildren = containsScript(view);

    // CASE 1: Inside an script - 2nd validation
    if (isScript)
    {
      sanitizeScriptContent(view);
      return; 
    }

    // CASE 2: In a sensitive container (Fieldset or Div with scripts) -> We delete garbage
    if (isFieldset || hasScriptChildren)
    {
      removeGarbageTextNodes(view);
    }

    // CASE 3: Recursivity to clear in depth
    if (view.getChildren() != null && !view.getChildren().isEmpty())
    {
      for (View child : view.getChildren())
      {
        if (child instanceof HtmlView)
        {
          deepClean((HtmlView) child);
        }
      }
    }
  }

  private void sanitizeScriptContent (HtmlView scriptView)
  {
    List<View> children = scriptView.getChildren();
    if (children == null) return;
    
    for (View child : children)
    {
      if (isTextNode(child))
      {
        HtmlView textNode = (HtmlView) child;
        String content = (String) textNode.getProperty("text");
        if (content != null)
        {
          String fixedContent = content.replace("\u00A0", " ");
          fixedContent = fixedContent.replace("&#160;", " ");
          fixedContent = fixedContent.replace("&nbsp;", " ");
          fixedContent = fixedContent.replace("&quot;", "\"");
          fixedContent = fixedContent.replace("&amp;", "&");
          fixedContent = fixedContent.replace("&lt;", "<");
          fixedContent = fixedContent.replace("&gt;", ">");
          
          textNode.setProperty("text", fixedContent);
        }
      }
    }
  }

   // Deletes nodes that generate "&#160;"
  private void removeGarbageTextNodes(HtmlView container)
  {
    List<View> children = container.getChildren();
    if (children == null || children.isEmpty())
    {
      return;
    }

    children.removeIf(child ->
    {
      if (isTextNode(child))
      {
        HtmlView v = (HtmlView) child;
        String text = (String) v.getProperty("text");
        if (text == null)
        {
          return true;
        }
        // If only spaces, delete the node
        String clean = text.replace("\u00A0", "").trim();
        return clean.isEmpty();
      }
      return false;
    });
  }

  private boolean containsScript(HtmlView view)
  {
    if (scriptCache.containsKey(view))
    {
      return scriptCache.get(view);
    }

    boolean result = false;
    if ("script".equalsIgnoreCase(view.getNativeViewType()))
    {
      result = true;
    }
    else if (view.getChildren() != null)
    {
      for (View child : view.getChildren())
      {
        if (child instanceof HtmlView && containsScript((HtmlView) child))
        {
          result = true;
          break;
        }
      }
    }
    scriptCache.put(view, result);
    return result;
  }

  private boolean isTextNode(View view)
  {
    if (view instanceof HtmlView)
    {
      HtmlView v = (HtmlView) view;
      return View.TEXT == v.getViewType() || "#text".equals(v.getNativeViewType());
    }
    return false;
  }

  private String extractTextFromView(HtmlView view)
  {
    if (isTextNode(view))
    {
      return (String) view.getProperty("text");
    }

    if (view.getChildren() != null)
    {
      for (View child : view.getChildren())
      {
        if (child instanceof HtmlView)
        {
          String text = extractTextFromView((HtmlView) child);
          if (text != null && !text.trim().isEmpty())
          {
            return text;
          }
        }
      }
    }
    return null;
  }

  private void processRadioGroups(List<HtmlView> views)
  {
    Map<String, List<HtmlView>> radioGroups = new HashMap<>();

    for (HtmlView view : views)
    {
      if (containsScript(view)) continue;
      
      if (isInputType(view, "radio"))
      {
        String name = (String) view.getProperty("name");
        if (name != null)
        {
          radioGroups.computeIfAbsent(name, k -> new ArrayList<>()).add(view);
        }
      }
    }

    for (Map.Entry<String, List<HtmlView>> entry : radioGroups.entrySet())
    {
      List<HtmlView> radios = entry.getValue();
      if (radios.isEmpty())
      {
        continue;
      }

      radios.sort(Comparator.comparingInt(r -> getPosition(r).getOrDefault("left", 0)));

      HtmlView fieldset = new HtmlView();
      fieldset.setNativeViewType("fieldset");
      String tempId = "f" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
      fieldset.setProperty("id", "fieldset_"+tempId);// Asing temporary ID
      fieldset.setProperty("class", "generated-fieldset");

      HtmlView legendLabel = findGroupLabel(radios, views);

      int insertIndex = -1;
      if (legendLabel != null && views.contains(legendLabel))
      {
        insertIndex = views.indexOf(legendLabel);
      }
      else if (!radios.isEmpty() && views.contains(radios.get(0)))
      {
        insertIndex = views.indexOf(radios.get(0));
      }

      if (legendLabel != null)
      {
        legendLabel.setNativeViewType("legend");
        legendLabel.setProperty("class", "radio-group-legend");
        String originalStyle = (String) legendLabel.getProperty("style");
        fieldset.setProperty("style", cleanFieldsetStyle(originalStyle) + "border:none");
        legendLabel.removeProperty("style");
        fieldset.getChildren().add(legendLabel);
        views.remove(legendLabel);
      }

      for (HtmlView radio : radios)
      {
        ensureId(radio);
        HtmlView optionLabel = findExplicitLabel(radio, views);
        if (optionLabel == null)
        {
          optionLabel = findOptionLabel(radio, views);
        }

        radio.removeProperty("style");
        fieldset.getChildren().add(radio);
        views.remove(radio);

        if (optionLabel != null)
        {
          optionLabel.setNativeViewType("label");
          optionLabel.setProperty("for", radio.getId());
          optionLabel.removeProperty("style");
          fieldset.getChildren().add(optionLabel);
          views.remove(optionLabel);
        }
      }

      if (insertIndex >= 0 && insertIndex <= views.size())
      {
        views.add(insertIndex, fieldset);
      }
      else
      {
        views.add(fieldset);
      }
    }
  }

  private void processStandardLabels(List<HtmlView> views)
  {
    List<HtmlView> inputs = new ArrayList<>();
    List<HtmlView> potentialLabels = new ArrayList<>();

    for (HtmlView view : views)
    {
      if (containsScript(view)) continue;

      if (isPotentialLabel(view))
      {
        String text = extractTextFromView(view);
        if (text != null && !text.trim().isEmpty() && !text.contains("${"))
        {
          potentialLabels.add(view);
        }
      }
      else if (isInput(view) && !isInputType(view, "radio"))
      {
        inputs.add(view);
      }
    }

    for (HtmlView input : inputs)
    {
      ensureId(input);
      HtmlView explicitLabel = findExplicitLabel(input, potentialLabels);
      if (explicitLabel != null)
      {
        potentialLabels.remove(explicitLabel);
        continue;
      }

      double minDistance = Double.MAX_VALUE;
      Map<String, Integer> inputPos = getPosition(input);
      int iTop = inputPos.getOrDefault("top", 0);
      int iLeft = inputPos.getOrDefault("left", 0);
      boolean isCheckbox = isInputType(input, "checkbox");
      HtmlView bestLabel = null;

      Iterator<HtmlView> it = potentialLabels.iterator();
      while (it.hasNext())
      {
        HtmlView candidate = it.next();
        if (candidate.getProperty("for") != null)
        {
          continue;
        }

        Map<String, Integer> lPos = getPosition(candidate);
        int lTop = lPos.getOrDefault("top", 0);
        int lLeft = lPos.getOrDefault("left", 0);

        if (lTop > iTop + INFO_TEXTS_MAX_DISTANCE)
        {
          continue;
        }
        if (Math.abs(lLeft - iLeft) > MAX_HORIZONTAL_DIF)
        {
          continue;
        }
        if ((iTop - lTop) > MAX_VERTICAL_DIF)
        {
          continue;
        }

        if (isCheckbox)
        {
          if (lLeft < iLeft)
          {
            continue;
          }
        }
        else
        {
          if (lLeft > iLeft + 20)
          {
            continue;
          }
        }

        double dist = Math.hypot(lLeft - iLeft, lTop - iTop);
        if (dist < minDistance)
        {
          minDistance = dist;
          bestLabel = candidate;
        }
      }

      if (bestLabel != null)
      {
        bestLabel.setNativeViewType("label");
        bestLabel.setProperty("for", input.getId());
        potentialLabels.remove(bestLabel);
      }
    }
  }

  private HtmlView findExplicitLabel(HtmlView input, List<HtmlView> candidates)
  {
    String inputId = input.getId();
    if (inputId == null) return null;
    
    for (HtmlView candidate : candidates)
    {
      if ("label".equalsIgnoreCase(candidate.getNativeViewType()))
      {
        String forAttr = (String) candidate.getProperty("for");
        if (inputId.equals(forAttr))
        {
          return candidate;
        }
      }
    }
    return null;
  }

  private void sortViews(List<HtmlView> views)
  {
    if (views == null || views.isEmpty()) return;
    
    views.sort(Comparator.comparingInt(v -> getPosition(v).getOrDefault("top", 0)));

    List<HtmlView> finalSortedList = new ArrayList<>();
    List<HtmlView> currentRow = new ArrayList<>();
    HtmlView rowReference = views.get(0);
    currentRow.add(rowReference);
    int referenceTop = getPosition(rowReference).getOrDefault("top", 0);

    for (int i = 1; i < views.size(); i++)
    {
      HtmlView current = views.get(i);
      int currentTop = getPosition(current).getOrDefault("top", 0);
      if (Math.abs(currentTop - referenceTop) <= ROW_THRESHOLD)
      {
        currentRow.add(current);
      }
      else
      {
        currentRow.sort(Comparator.comparingInt(v -> getPosition(v).getOrDefault("left", 0)));
        finalSortedList.addAll(currentRow);
        currentRow.clear();
        currentRow.add(current);
        rowReference = current;
        referenceTop = currentTop;
      }
    }
    if (!currentRow.isEmpty())
    {
      currentRow.sort(Comparator.comparingInt(v -> getPosition(v).getOrDefault("left", 0)));
      finalSortedList.addAll(currentRow);
    }
    views.clear();
    views.addAll(finalSortedList);
  }

  private HtmlView findOptionLabel(HtmlView input, List<HtmlView> allViews)
  {
    Map<String, Integer> iPos = getPosition(input);
    int iTop = iPos.getOrDefault("top", 0);
    int iLeft = iPos.getOrDefault("left", 0);
    for (HtmlView view : allViews)
    {
      if (containsScript(view)) continue;
      
      if (!isPotentialLabel(view)) continue;
      
      if (view.getProperty("for") != null) continue;
      
      Map<String, Integer> lPos = getPosition(view);
      if (lPos.getOrDefault("left", 0) <= iLeft)
      {
        continue;
      }
      if (Math.abs(lPos.getOrDefault("top", 0) - iTop) > COMPONENT_POSITION_ERROR_MARGIN)
      {
        continue;
      }
      if ((lPos.getOrDefault("left", 0) - iLeft) > 200)
      {
        continue;
      }
      return view;
    }
    return null;
  }

  private HtmlView findGroupLabel(List<HtmlView> radios, List<HtmlView> allViews)
  {
    HtmlView firstRadio = radios.stream().min(Comparator.comparingInt(r -> getPosition(r).getOrDefault("top", Integer.MAX_VALUE))).orElse(null);
    if (firstRadio == null)
    {
      return null;
    }
    
    Map<String, Integer> rPos = getPosition(firstRadio);
    int rTop = rPos.getOrDefault("top", 0);
    int rLeft = rPos.getOrDefault("left", 0);
    HtmlView best = null;
    int minVerticalDist = Integer.MAX_VALUE;
    
    for (HtmlView view : allViews)
    {
      if (containsScript(view)) continue;
      if (!isPotentialLabel(view)) continue;
      if (view.getProperty("for") != null) continue;
      
      Map<String, Integer> lPos = getPosition(view);
      if (lPos.getOrDefault("top", 0) >= rTop)
      {
        continue;
      }
      if (Math.abs(lPos.getOrDefault("left", 0) - rLeft) > 40)
      {
        continue;
      }
      int dist = rTop - lPos.getOrDefault("top", 0);
      if (dist < minVerticalDist && dist < 50)
      {
        minVerticalDist = dist;
        best = view;
      }
    }
    return best;
  }

  private String cleanFieldsetStyle(String style)
  {
    if (style == null || style.trim().isEmpty())
    {
      return "";
    }
    StringBuilder resultStyle = new StringBuilder();
    String[] rules = style.split(";");
    for (String rule : rules)
    {
      String[] kv = rule.split(":");
      if (kv.length == 2)
      {
        String key = kv[0].trim().toLowerCase();
        String val = kv[1].trim();
        if (!key.equals("width") && !key.equals("height"))
        {
          resultStyle.append(key).append(":").append(val).append(";");
        }
      }
    }
    return resultStyle.toString();
  }

  private void ensureSelectHasOption(HtmlView view)
  {
    if ("select".equalsIgnoreCase(view.getNativeViewType()))
    {
      if (view.getChildren().isEmpty())
      {
        HtmlView option = new HtmlView();
        option.setNativeViewType("option");
        option.setViewType(View.ITEM);
        option.setProperty("value", "");
        HtmlView text = new HtmlView();
        text.setNativeViewType("#text");
        text.setViewType(View.TEXT);
        text.setProperty("text", "(Seleccionar)");
        option.getChildren().add(text);
        view.getChildren().add(option);
      }
    }
  }

  private void ensureId(HtmlView view)
  {
    if (view.getId() == null || view.getId().trim().isEmpty())
    {
      String name = (String) view.getProperty("name");
      if (name != null && !name.isEmpty())
      {
        if (isInputType(view, "radio"))
        {
          String val = (String) view.getProperty("value");
          view.setId(name + "_" + (val != null ? val : "idx"));
        }
        else
        {
          view.setId(name);
        }
        view.setProperty("id", view.getId());
      }
    }
  }

  private void collectAbsoluteViews(View parent, List<HtmlView> result)
  {
    if (parent == null) return;
    
    List<View> children = new ArrayList<>(parent.getChildren());
    for (View child : children)
    {
      if (child instanceof HtmlView)
      {
        HtmlView htmlChild = (HtmlView) child;
        if (isAbsolute(htmlChild))
        {
          result.add(htmlChild);
        }
        else
        {
          collectAbsoluteViews(htmlChild, result);
        }
      }
    }
  }

  private boolean isAbsolute(HtmlView view)
  {
    String style = (String) view.getProperty("style");
    return style != null && style.toLowerCase().contains("position:absolute");
  }

  private Map<String, Integer> getPosition(HtmlView view)
  {
    if (positionCache.containsKey(view))
    {
      return positionCache.get(view);
    }
    Map<String, Integer> pos = new HashMap<>();
    String style = (String) view.getProperty("style");
    if (style != null && !style.isEmpty())
    {
      Matcher m = POSITION_PATTERN.matcher(style);
      while (m.find())
      {
        try
        {
          pos.put(m.group(1).toLowerCase(), Integer.parseInt(m.group(2)));
        }
        catch (Exception ex){}
      }
    }
    positionCache.put(view, pos);
    return pos;
  }

  private boolean isInput(HtmlView view)
  {
    if (containsScript(view)) return false;
   
    String tag = view.getNativeViewType();
    if (tag == null) return false; 
    
    return tag.equalsIgnoreCase("input") 
      || tag.equalsIgnoreCase("select") 
      || tag.equalsIgnoreCase("textarea");
  }

  private boolean isInputType(HtmlView view, String type)
  {
    if (!isInput(view))
    {
      return false;
    }
    String t = (String) view.getProperty("type");
    return t != null && t.equalsIgnoreCase(type);
  }

  private boolean isPotentialLabel(HtmlView view)
  {
    if (containsScript(view)) return false;
    
    String tag = view.getNativeViewType();
    if (tag == null) return false;
    
    if ("sectionHeader".equals(view.getProperty("class")))
    {
      view.setProperty("class", view.getProperty("class") + " col-12");
      return false;
    }
    return tag.equalsIgnoreCase("div") 
      || tag.equalsIgnoreCase("span") 
      || tag.equalsIgnoreCase("label");
  }

  private View findView(View parent, String id)
  {
    if (id != null && id.equals(parent.getId()))
    {
      return parent;
    }
    for (View child : parent.getChildren())
    {
      View found = findView(child, id);
      if (found != null)
      {
        return found;
      }
    }
    return null;
  }
}
