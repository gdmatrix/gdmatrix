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
package org.santfeliu.swing.form.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.FormDesigner;
import org.santfeliu.swing.form.view.ButtonView;
import org.santfeliu.swing.form.view.CheckBoxView;
import org.santfeliu.swing.form.view.InputTextAreaView;
import org.santfeliu.swing.form.view.InputTextView;
import org.santfeliu.swing.form.view.LabelView;
import org.santfeliu.swing.form.view.OutputTextAreaView;
import org.santfeliu.swing.form.view.OutputTextView;
import org.santfeliu.swing.form.view.RadioButtonView;
import org.santfeliu.swing.form.view.ScriptView;
import org.santfeliu.swing.form.view.SelectBoxView;

/**
 *
 * @author lopezrj
 */
public class AccessibilityUtils
{
  private static final int COMPONENT_POSITION_ERROR_MARGIN = 8; //px
  private static final int INFO_TEXTS_MAX_DISTANCE = 16; //px  
  
  public static boolean repairTabIndexes(Collection<ComponentView> 
    componentViews)
  {    
    boolean changes = false;
    List<ComponentView> auxList = 
      getSortedComponentListByPosition(getInputComponents(componentViews));
    int tabIndex = 1;
    for (ComponentView component : auxList)
    {      
      Integer newTabIndex = tabIndex++;      
      if (component instanceof InputTextView)
      {
        InputTextView auxComponent = (InputTextView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;
        auxComponent.setTabindex(newTabIndex);
      }
      else if (component instanceof InputTextAreaView)
      {
        InputTextAreaView auxComponent = (InputTextAreaView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;
        auxComponent.setTabindex(newTabIndex);
      }
      else if (component instanceof SelectBoxView)
      {
        SelectBoxView auxComponent = (SelectBoxView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;
        auxComponent.setTabindex(newTabIndex);
      }
      else if (component instanceof RadioButtonView)
      {
        RadioButtonView auxComponent = (RadioButtonView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;        
        auxComponent.setTabindex(newTabIndex);
      }
      else if (component instanceof CheckBoxView)
      {
        CheckBoxView auxComponent = (CheckBoxView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;
        auxComponent.setTabindex(newTabIndex);
      }
      else if (component instanceof ButtonView)
      {
        ButtonView auxComponent = (ButtonView)component;
        if (!newTabIndex.equals(auxComponent.getTabindex())) changes = true;
        auxComponent.setTabindex(newTabIndex);
      }
    }
    return changes;
  }
  
  public static boolean repairLabels(Collection<ComponentView> componentViews)
  {   
    boolean changes;
    //Clear invalid forElement
    changes = clearAllInvalidForElementValues(componentViews);
    //Input -> Left/Above Candidates
    Map<ComponentView, List<ComponentView>> candidatesMap = new HashMap(); 
    //Input -> Output
    Map<ComponentView, ComponentView> assignationMap = new HashMap();     
    List<ComponentView> componentsToCreate = new ArrayList();
    List<ComponentView> componentsToRemove = new ArrayList();
    List<ComponentView> inputComponents = getInputComponents(componentViews);
    for (ComponentView inputComponent : inputComponents)
    {
      if (!hasLabelAssigned(inputComponent, componentViews))
      {  
        candidatesMap.put(inputComponent, new ArrayList());
        ComponentView horizCandidateComponent = 
          getHorizontalLabelCandidate(inputComponent, componentViews);
        if (horizCandidateComponent != null) 
          candidatesMap.get(inputComponent).add(horizCandidateComponent);
        ComponentView vertCandidateComponent = 
          getVerticalLabelCandidate(inputComponent, componentViews);
        if (vertCandidateComponent != null) 
          candidatesMap.get(inputComponent).add(vertCandidateComponent);
      }
    }
    
    boolean altered;
    do
    {
      altered = false;
      for (ComponentView inputComponent : candidatesMap.keySet())
      {
        if (!assignationMap.containsKey(inputComponent))
        {
          List<ComponentView> candidates = candidatesMap.get(inputComponent);
          if (candidates.size() == 1)
          {
            ComponentView assignedCandidate = candidates.get(0);
            assignationMap.put(inputComponent, assignedCandidate);
            for (List<ComponentView> candidatesAux : candidatesMap.values())
            {
              candidatesAux.remove(assignedCandidate);
            }
            altered = true;
          }
        }
      }      
    }
    while (altered);
      
    //Second pass -> Assign unassigned
    for (ComponentView inputComponent : candidatesMap.keySet())
    {
      if (!assignationMap.containsKey(inputComponent))
      {        
        List<ComponentView> candidates = candidatesMap.get(inputComponent);
        if (candidates.size() >= 1)
        {
          ComponentView assignedCandidate = candidates.get(0);            
          assignationMap.put(inputComponent, assignedCandidate);
          for (List<ComponentView> candidatesAux : candidatesMap.values())
          {
            candidatesAux.remove(assignedCandidate);
          }            
        }        
      }
    }    
    
    for (ComponentView inputComponent : assignationMap.keySet())
    {
      if (assignationMap.get(inputComponent) != null)
      {
        assignComponentId(inputComponent);
        ComponentView outputComponent = assignationMap.get(inputComponent);
        LabelView labelComponent = null;
        if (outputComponent instanceof OutputTextView)
        {
          labelComponent = convertToLabelView((OutputTextView)outputComponent);
          componentsToRemove.add(outputComponent);
          componentsToCreate.add(labelComponent);
        }
        else if (outputComponent instanceof LabelView)
        {
          labelComponent = (LabelView)outputComponent;
        }
        labelComponent.setForElement(inputComponent.getId());
        changes = true;
      }
    }      
    
    for (ComponentView componentView : componentsToRemove)
    {
      componentViews.remove(componentView);
    }
    for (ComponentView componentView : componentsToCreate)
    {
      componentViews.add(componentView);
    }
    
    return changes;
  }
  
  public static boolean repairInfoTexts(Collection componentViews)
  {
    boolean changes = false;
    //Input -> Right output
    Map<ComponentView, ComponentView> infoTextMap = new HashMap(); 
    //Input -> Below output
    Map<ComponentView, ComponentView> helpTextMap = new HashMap();     
    
    List<ComponentView> inputComponents = getInputComponents(componentViews);
    for (ComponentView inputComponent : inputComponents)
    {
      if (inputComponent instanceof InputTextView || 
        inputComponent instanceof InputTextAreaView || 
        inputComponent instanceof SelectBoxView)
      {
        if (!hasInfoTextAssigned(inputComponent))
        {
          ComponentView candidate = getRightComponent(inputComponent, 
            componentViews, INFO_TEXTS_MAX_DISTANCE);
          if (candidate != null && isValidInfoTextCandidate(candidate)) 
            infoTextMap.put(inputComponent, candidate);
        }
        if (!hasHelpTextAssigned(inputComponent))
        {
          ComponentView candidate = getBelowComponent(inputComponent, 
            componentViews, INFO_TEXTS_MAX_DISTANCE);
          if (candidate != null && isValidHelpTextCandidate(candidate))
          {
            helpTextMap.put(inputComponent, candidate);
          }
          else
          {
            candidate = getRightComponent(inputComponent, 
              componentViews, INFO_TEXTS_MAX_DISTANCE);
            if (candidate != null && isValidHelpTextCandidate(candidate))
              helpTextMap.put(inputComponent, candidate);
            
          }
        }      
      }
    }
    
    for (ComponentView inputComponent : infoTextMap.keySet())
    {
      ComponentView outputComponent = infoTextMap.get(inputComponent);
      setInfoText(inputComponent, getText(outputComponent));
      componentViews.remove(outputComponent);
      changes = true;
    }

    for (ComponentView inputComponent : helpTextMap.keySet())
    {
      ComponentView outputComponent = helpTextMap.get(inputComponent);
      if (componentViews.contains(outputComponent)) //not used as infoText
      {
        setHelpText(inputComponent, getText(outputComponent));
        componentViews.remove(outputComponent);
        changes = true;
      }
    }
    
    return changes;
  }  

  public static boolean adaptStyleClasses(FormDesigner panel)
  {
    Collection componentViews = panel.getComponentViews();
    boolean changes = false;
    if (panel.getMinimumSize() != null)
    {
      int panelWidth = (int)Math.round(panel.getMinimumSize().getWidth());
      if (panelWidth > 0)
      {
        List<ComponentView> inputComponents = 
          getInputComponents(componentViews);
        for (ComponentView inputComponent : inputComponents)
        {
          String originalStyleClass = inputComponent.getStyleClass();
          if (originalStyleClass == null || 
            !originalStyleClass.contains("col-"))
          {
            String adaptedStyleClass = getAdaptedStyleClass(inputComponent, 
              panelWidth);
            if (adaptedStyleClass != null)
            {
              StringBuilder sbStyleClass = new StringBuilder();            
              sbStyleClass.
                append(StringUtils.defaultString(originalStyleClass)).
                append(" ").
                append(adaptedStyleClass);
              inputComponent.setStyleClass(sbStyleClass.toString().trim());
              changes = true;
            }
          }        
        }
      }
    }
    return changes;
  }

  public static boolean convertOutputTexts(Collection componentViews)
  {
    boolean changes = false;
    List<OutputTextView> outputTexts = getOutputTexts(componentViews);
    for (OutputTextView outputText : outputTexts)
    {
      OutputTextAreaView outputTextArea = 
        convertToOutputTextAreaView(outputText);
      componentViews.remove(outputText);
      componentViews.add(outputTextArea);
      changes = true;
    }
    return changes;
  }

  public static boolean repairOutputOrder(Collection componentViews)
  {
    Map<ComponentView, Integer> beforeMap = buildOutputOrderMap(componentViews);
    int outputOrder = 1;
    List<ComponentView> sortedComponents = 
      getSortedComponentListByPosition(componentViews);
    for (ComponentView view : sortedComponents)
    {
      if (!(view instanceof ScriptView)) //ignore scripts
      {
        view.setOutputOrder(outputOrder++);
      }
    }
    Map<ComponentView, Integer> afterMap = buildOutputOrderMap(componentViews);
    return diffOutputOrderMaps(beforeMap, afterMap);
  }
  
  public static List<ComponentView> getSortedComponentListByOutputOrder(
    Collection<ComponentView> componentViews)
  {
    List<ComponentView> auxList = new ArrayList(componentViews);
    Collections.sort(auxList, new Comparator() {
      @Override
      public int compare(Object o1, Object o2)
      {
        ComponentView c1 = (ComponentView)o1;
        int outputOrder1 = 
          (c1.getOutputOrder() == null ? 0 : c1.getOutputOrder());
        ComponentView c2 = (ComponentView)o2;
        int outputOrder2 = 
          (c2.getOutputOrder() == null ? 0 : c2.getOutputOrder());
        return outputOrder1 - outputOrder2;
      }
    });
    return auxList;
  } 
  
  private static LabelView convertToLabelView(OutputTextView outputTextView)
  {
    LabelView labelView = new LabelView();
    labelView.setBackground(outputTextView.getBackground());
    labelView.setBorderBottomColor(outputTextView.getBorderBottomColor());
    labelView.setBorderBottomStyle(outputTextView.getBorderBottomStyle());
    labelView.setBorderBottomWidth(outputTextView.getBorderBottomWidth());
    labelView.setBorderLeftColor(outputTextView.getBorderLeftColor());
    labelView.setBorderLeftStyle(outputTextView.getBorderLeftStyle());
    labelView.setBorderLeftWidth(outputTextView.getBorderLeftWidth());
    labelView.setBorderRightColor(outputTextView.getBorderRightColor());
    labelView.setBorderRightStyle(outputTextView.getBorderRightStyle());
    labelView.setBorderRightWidth(outputTextView.getBorderRightWidth());
    labelView.setBorderTopColor(outputTextView.getBorderTopColor());
    labelView.setBorderTopStyle(outputTextView.getBorderTopStyle());
    labelView.setBorderTopWidth(outputTextView.getBorderTopWidth());
    labelView.setBounds(outputTextView.getBounds());
    labelView.setContentHeight(outputTextView.getContentHeight());
    labelView.setContentWidth(outputTextView.getContentWidth());
    labelView.setFontFamily(outputTextView.getFontFamily());
    labelView.setFontSize(outputTextView.getFontSize());    
    labelView.setForeground(outputTextView.getForeground());
    labelView.setHeight(outputTextView.getHeight());
    labelView.setId(outputTextView.getId());
    labelView.setOutputOrder(outputTextView.getOutputOrder());
    labelView.setRenderer(outputTextView.getRenderer());
    labelView.setStyleClass(outputTextView.getStyleClass());
    labelView.setText(outputTextView.getText());
    labelView.setTextAlign(outputTextView.getTextAlign());
    labelView.setWidth(outputTextView.getWidth());
    labelView.setX(outputTextView.getX());
    labelView.setY(outputTextView.getY());
    return labelView;
  }
  
  private static OutputTextAreaView convertToOutputTextAreaView(
    OutputTextView outputTextView)
  {
    OutputTextAreaView otaView = new OutputTextAreaView();
    otaView.setBackground(outputTextView.getBackground());
    otaView.setBorderBottomColor(outputTextView.getBorderBottomColor());
    otaView.setBorderBottomStyle(outputTextView.getBorderBottomStyle());
    otaView.setBorderBottomWidth(outputTextView.getBorderBottomWidth());
    otaView.setBorderLeftColor(outputTextView.getBorderLeftColor());
    otaView.setBorderLeftStyle(outputTextView.getBorderLeftStyle());
    otaView.setBorderLeftWidth(outputTextView.getBorderLeftWidth());
    otaView.setBorderRightColor(outputTextView.getBorderRightColor());
    otaView.setBorderRightStyle(outputTextView.getBorderRightStyle());
    otaView.setBorderRightWidth(outputTextView.getBorderRightWidth());
    otaView.setBorderTopColor(outputTextView.getBorderTopColor());
    otaView.setBorderTopStyle(outputTextView.getBorderTopStyle());
    otaView.setBorderTopWidth(outputTextView.getBorderTopWidth());
    otaView.setBounds(outputTextView.getBounds());
    otaView.setContentHeight(outputTextView.getContentHeight());
    otaView.setContentWidth(outputTextView.getContentWidth());
    otaView.setFontFamily(outputTextView.getFontFamily());
    otaView.setFontSize(outputTextView.getFontSize());    
    otaView.setForeground(outputTextView.getForeground());
    otaView.setHeight(outputTextView.getHeight());
    otaView.setId(outputTextView.getId());
    otaView.setOutputOrder(outputTextView.getOutputOrder());
    otaView.setRenderer(outputTextView.getRenderer());
    otaView.setStyleClass(outputTextView.getStyleClass());
    otaView.setText(outputTextView.getText());
    otaView.setTextAlign(outputTextView.getTextAlign());
    otaView.setWidth(outputTextView.getWidth());
    otaView.setX(outputTextView.getX());
    otaView.setY(outputTextView.getY());
    return otaView;
  }  
  
  private static void assignComponentId(ComponentView componentView)
  {
    if (componentView.getId() == null)
    {
      if (componentView instanceof RadioButtonView)
      {
        componentView.setId(componentView.getX() + "-" + componentView.getY());
      }
      else
      {
        String variable = getVariable(componentView);
        if (variable != null)
          componentView.setId(variable);
        else
          componentView.setId(componentView.getX() + "-" + 
            componentView.getY());
      }
    }      
  }

  private static List<ComponentView> getInputComponents(
    Collection<ComponentView> componentViews)
  {
    List<ComponentView> result = new ArrayList();
    for (ComponentView component : componentViews)
    {
      if (isInputComponent(component)) result.add(component);
    }
    return result;
  }
  
  private static List<OutputTextView> getOutputTexts(
    Collection<ComponentView> componentViews)
  {
    List<OutputTextView> result = new ArrayList();
    for (ComponentView component : componentViews)
    {
      if (component instanceof OutputTextView)
      {
        result.add((OutputTextView)component);
      }
    }
    return result;
  }

  private static ComponentView getHorizontalLabelCandidate(
    ComponentView inputComponent, Collection<ComponentView> componentViews)  
  {
    ComponentView candidateComponent = null;
    if (inputComponent instanceof RadioButtonView ||
      inputComponent instanceof CheckBoxView)
    {
      ComponentView rightComponent = getRightComponent(inputComponent, 
        componentViews, null);
      if (rightComponent != null)
      {
        String text = getText(rightComponent);
        if (text != null && !text.endsWith(":"))
        {
          candidateComponent = rightComponent;
        }        
      }      
      if (candidateComponent == null) //Search in the left
      {
        ComponentView leftComponent = getLeftComponent(inputComponent, 
          componentViews, null);
        if (leftComponent != null)
        {
          String text = getText(leftComponent);
          if (text != null && text.endsWith(":"))
          {
            candidateComponent = leftComponent;
          }        
        }
      }
    }
    else
    {
      candidateComponent = getLeftComponent(inputComponent, componentViews, 
        null);
    }
    return (isValidLabelCandidate(candidateComponent) ? candidateComponent : 
      null);
  }
  
  private static ComponentView getLeftComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews, Integer maxDistance)
  {    
    ComponentView candidateComponent = null;    
    int minX = (maxDistance == null ? 0 : inputComponent.getX() - maxDistance);
    int maxX = inputComponent.getX();
    int minY = inputComponent.getY() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxY = inputComponent.getY() + inputComponent.getHeight();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() < maxY && 
        (candidateComponent == null || 
        component.getX() > candidateComponent.getX()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;    
  }

  private static ComponentView getRightComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews, Integer maxDistance)
  {    
    ComponentView candidateComponent = null;    
    int minX = inputComponent.getX() + inputComponent.getWidth();
    int maxX = (maxDistance == null ? Integer.MAX_VALUE : 
      minX + maxDistance + 1);
    int minY = inputComponent.getY() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxY = inputComponent.getY() + inputComponent.getHeight();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() < maxY && 
        (candidateComponent == null || 
        component.getX() < candidateComponent.getX()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;
  }
  
  private static ComponentView getVerticalLabelCandidate(
    ComponentView inputComponent, Collection<ComponentView> componentViews)
  {
    ComponentView candidateComponent = 
      getAboveComponent(inputComponent, componentViews, null);
    return (isValidLabelCandidate(candidateComponent) ? candidateComponent : 
      null);
  }
  
  private static ComponentView getAboveComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews, Integer maxDistance)
  {
    ComponentView candidateComponent = null;
    int minX = inputComponent.getX() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxX = inputComponent.getX() + inputComponent.getWidth();
    int minY = (maxDistance == null ? 0 : inputComponent.getY() - maxDistance);    
    int maxY = inputComponent.getY();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() < maxY && 
        (candidateComponent == null || 
        component.getY() > candidateComponent.getY()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;
  }
  
  private static ComponentView getBelowComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews, Integer maxDistance)
  {
    ComponentView candidateComponent = null;
    int minX = inputComponent.getX() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxX = inputComponent.getX() + inputComponent.getWidth();
    int minY = inputComponent.getY() + inputComponent.getHeight();
    int maxY = (maxDistance == null ? Integer.MAX_VALUE : 
      minY + maxDistance + 1);
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() < maxY && 
        (candidateComponent == null || 
        component.getY() < candidateComponent.getY()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;
  }  
  
  private static boolean isValidLabelCandidate(ComponentView candidateComponent)
  {
    return (candidateComponent != null && isOutputComponent(candidateComponent) 
      && !hasForElementAssigned(candidateComponent));
  }
  
  private static boolean isValidInfoTextCandidate(
    ComponentView candidateComponent)
  {
    return (candidateComponent instanceof OutputTextView && 
      ((OutputTextView)candidateComponent).getText().length() <= 4);      
  }

  private static boolean isValidHelpTextCandidate(
    ComponentView candidateComponent)
  {
    return (candidateComponent instanceof OutputTextView);
  }  

  private static boolean isInputComponent(ComponentView component)
  {
    return (component instanceof InputTextView || 
      component instanceof InputTextAreaView || 
      component instanceof SelectBoxView ||
      component instanceof RadioButtonView ||
      component instanceof CheckBoxView ||
      component instanceof ButtonView);      
  }  
  
  private static boolean isOutputComponent(ComponentView component)
  {
    return (component instanceof OutputTextView ||
      component instanceof LabelView);
  }
  
  private static boolean hasForElementAssigned(ComponentView component)
  {
    if (component instanceof LabelView)
    {
      return (((LabelView)component).getForElement() != null);
    }
    return false;
  }

  private static boolean hasLabelAssigned(ComponentView inputComponent, 
    Collection<ComponentView> componentViews)
  {
    if (inputComponent.getId() != null)
    {    
      for (ComponentView component : componentViews)
      {
        if (component instanceof LabelView)
        {
          LabelView labelView = (LabelView)component;
          if (inputComponent.getId().equals(labelView.getForElement())) 
            return true;        
        }
      }
    }
    return false;
  }
  
  private static List<ComponentView> getSortedComponentListByPosition(
    Collection<ComponentView> componentViews)
  {
    List<ComponentView> result = new ArrayList();
    List<ComponentGroup> groupList = getComponentGroupList(componentViews);
    Collections.sort(groupList, new Comparator() {
      @Override
      public int compare(Object o1, Object o2)
      {
        ComponentGroup c1 = (ComponentGroup)o1;
        ComponentGroup c2 = (ComponentGroup)o2;
        if (Math.abs(c1.getY() - c2.getY()) > 0)
        {
          return c1.getY() - c2.getY();
        }
        else if (Math.abs(c1.getX() - c2.getX()) > 0)
        {
          return c1.getX() - c2.getX();
        }
        else
        {
          return 0;
        }
      }
    });
    for (ComponentGroup group : groupList)
    {
      for (ComponentView componentView : group.getComponentList())
      {
        result.add(componentView);
      }
    }
    return result;
  }

  private static List<ComponentGroup> getComponentGroupList(
    Collection<ComponentView> componentViews)
  {
    List<ComponentGroup> auxGroupList = new ArrayList();
    //Labels correction
    Map<ComponentView, ComponentView> labelMap = new HashMap();
    for (ComponentView auxComponent : componentViews)    
    {
      if (auxComponent instanceof LabelView)
      {
        String inputId = ((LabelView)auxComponent).getForElement();
        if (inputId != null)
        {
          ComponentView inputComponent = 
            getComponentById(componentViews, inputId);
          if (inputComponent != null)
          {
            labelMap.put(auxComponent, inputComponent);
          }          
        }
      }
    }
    for (ComponentView auxComponent : componentViews)
    {
      if (labelMap.keySet().contains(auxComponent)) //label element
      {
        ComponentGroup componentGroup = new ComponentGroup();
        componentGroup.getComponentList().add(auxComponent);
        ComponentView inputElement = labelMap.get(auxComponent);
        componentGroup.getComponentList().add(inputElement);
        componentGroup.setX(inputElement.getX());
        componentGroup.setY(inputElement.getY());
        auxGroupList.add(componentGroup);
      }
      else if (labelMap.values().contains(auxComponent)) //input element
      {
        //pass
      }
      else
      {
        ComponentGroup componentGroup = new ComponentGroup();
        componentGroup.getComponentList().add(auxComponent);
        componentGroup.setX(auxComponent.getX());
        componentGroup.setY(auxComponent.getY());
        auxGroupList.add(componentGroup);
      }
    }
    return auxGroupList;
  }
 
  private static ComponentView getComponentById(Collection<ComponentView> 
    componentViews, String id)
  {
    for (ComponentView auxComponent : componentViews)
    {
      if (id.equals(auxComponent.getId())) return auxComponent;
    }
    return null;
  }
  
  private static void setInfoText(ComponentView inputComponent, String text)
  {
    if (inputComponent instanceof InputTextAreaView)
    {
      ((InputTextAreaView)inputComponent).setInfoText(text);
    }
    else if (inputComponent instanceof InputTextView)
    {
      ((InputTextView)inputComponent).setInfoText(text);
    }
    else if (inputComponent instanceof SelectBoxView)
    {
      ((SelectBoxView)inputComponent).setInfoText(text);
    }    
  }
  
  private static boolean hasInfoTextAssigned(ComponentView inputComponent)
  {
    String text = null;
    if (inputComponent instanceof InputTextAreaView)
    {
      text = ((InputTextAreaView)inputComponent).getInfoText();
    }
    else if (inputComponent instanceof InputTextView)
    {
      text = ((InputTextView)inputComponent).getInfoText();
    }
    else if (inputComponent instanceof SelectBoxView)
    {
      text = ((SelectBoxView)inputComponent).getInfoText();
    }
    return (!StringUtils.defaultString(text).isEmpty());
  }

  private static void setHelpText(ComponentView inputComponent, String text)
  {
    if (inputComponent instanceof InputTextAreaView)
    {
      ((InputTextAreaView)inputComponent).setHelpText(text);
    }
    else if (inputComponent instanceof InputTextView)
    {
      ((InputTextView)inputComponent).setHelpText(text);
    }
    else if (inputComponent instanceof SelectBoxView)
    {
      ((SelectBoxView)inputComponent).setHelpText(text);
    }
  }
  
  private static boolean hasHelpTextAssigned(ComponentView inputComponent)
  {
    String text = null;
    if (inputComponent instanceof InputTextAreaView)
    {
      text = ((InputTextAreaView)inputComponent).getHelpText();
    }
    else if (inputComponent instanceof InputTextView)
    {
      text = ((InputTextView)inputComponent).getHelpText();
    }
    else if (inputComponent instanceof SelectBoxView)
    {
      text = ((SelectBoxView)inputComponent).getHelpText();
    }    
    return (!StringUtils.defaultString(text).isEmpty());
  }  
  
  private static String getText(ComponentView outputComponent)
  {
    if (outputComponent instanceof OutputTextView)
    {
      return ((OutputTextView)outputComponent).getText();
    }
    else if (outputComponent instanceof LabelView)
    {
      return ((LabelView)outputComponent).getText();
    }
    return null;
  }

  private static String getVariable(ComponentView inputComponent)
  {
    String variable = null;
    if (inputComponent instanceof InputTextView)
      variable = ((InputTextView)inputComponent).getVariable();
    else if (inputComponent instanceof InputTextAreaView)
      variable = ((InputTextAreaView)inputComponent).getVariable();
    else if (inputComponent instanceof SelectBoxView)
      variable = ((SelectBoxView)inputComponent).getVariable();
    else if (inputComponent instanceof RadioButtonView)
      variable = ((RadioButtonView)inputComponent).getVariable();
    else if (inputComponent instanceof CheckBoxView)
      variable = ((CheckBoxView)inputComponent).getVariable();
    else if (inputComponent instanceof ButtonView)
      variable = ((ButtonView)inputComponent).getVariable();
    return variable;
  }
  
  private static boolean clearAllInvalidForElementValues(
    Collection<ComponentView> componentViews)
  {
    boolean changes = false;
    List<LabelView> labelViews = getAllLabelViews(componentViews);
    for (LabelView labelView : labelViews)
    {
      String forElement = labelView.getForElement();
      if (forElement != null)
      {
        if (getComponentById(componentViews, forElement) == null)
        {
          labelView.setForElement(null);
          changes = true;
        }
      }      
    }
    return changes;
  }
  
  private static List<LabelView> getAllLabelViews(Collection<ComponentView> 
    componentViews)
  {
    List<LabelView> labelViews = new ArrayList();  
    for (ComponentView component : componentViews)
    {
      if (component instanceof LabelView)
      {
        labelViews.add((LabelView)component);
      }
    }
    return labelViews;
  }
  
  private static String getAdaptedStyleClass(ComponentView component, 
    Integer panelWidth)
  {
    float ratio = (float)component.getWidth() / (float)panelWidth;
    if (ratio < 0.33f)
    {
      if (component instanceof CheckBoxView)
      {
        return "col-12 md:col-2";
      }
      else
      {
        return "col-12 md:col-4";
      }
    }
    else if (ratio < 0.66f)
    {
      return "col-12 md:col-6";              
    }
    else
    {
      return "col-12";
    }
  }
  
  private static Map<ComponentView, Integer> buildOutputOrderMap(
    Collection componentViews)
  {
    Map<ComponentView, Integer> map = new HashMap();
    for (Object obj : componentViews)
    {
      ComponentView view = (ComponentView)obj;
      map.put(view, view.getOutputOrder());
    }
    return map;
  }
  
  private static boolean diffOutputOrderMaps(
    Map<ComponentView, Integer> map1, Map<ComponentView, Integer> map2)
  {
    if (map1.size() != map2.size()) return true;
    
    for (ComponentView view : map1.keySet())
    {
      Integer o1 = map1.get(view);
      Integer o2 = map2.get(view);
      if (o1 == null && o2 == null) 
      {
        //nothing here
      }
      else if (o1 != null && o2 != null) 
      {
        if (!o1.equals(o2)) return true;
      }
      else
      {
        return true;
      }
    }
    return false;
  }  
  
  private static class ComponentGroup
  {
    private List<ComponentView> componentList = new ArrayList();
    private int x;
    private int y;

    public ComponentGroup()
    {
    }

    public List<ComponentView> getComponentList()
    {
      return componentList;
    }

    public int getX()
    {
      return x;
    }

    public void setX(int x)
    {
      this.x = x;
    }

    public int getY()
    {
      return y;
    }

    public void setY(int y)
    {
      this.y = y;
    }
  }

}
