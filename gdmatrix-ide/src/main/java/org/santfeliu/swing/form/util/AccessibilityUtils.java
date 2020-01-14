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
import org.santfeliu.swing.form.ComponentView;
import org.santfeliu.swing.form.view.ButtonView;
import org.santfeliu.swing.form.view.CheckBoxView;
import org.santfeliu.swing.form.view.InputTextAreaView;
import org.santfeliu.swing.form.view.InputTextView;
import org.santfeliu.swing.form.view.LabelView;
import org.santfeliu.swing.form.view.OutputTextView;
import org.santfeliu.swing.form.view.RadioButtonView;
import org.santfeliu.swing.form.view.SelectBoxView;

/**
 *
 * @author lopezrj
 */
public class AccessibilityUtils
{
  private static final int COMPONENT_POSITION_ERROR_MARGIN = 8; //px
  
  public static boolean repairTabIndexes(Collection<ComponentView> componentViews)
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
    boolean changes = false;
    Map<ComponentView, List<ComponentView>> candidatesMap = new HashMap(); //Input -> Left/Above Candidates
    Map<ComponentView, ComponentView> assignationMap = new HashMap(); //Input -> Output    
    List<ComponentView> componentsToCreate = new ArrayList();
    List<ComponentView> componentsToRemove = new ArrayList();
    List<ComponentView> inputComponents = getInputComponents(componentViews);
    for (ComponentView inputComponent : inputComponents)
    {
      if (!hasLabelAssigned(inputComponent, componentViews))
      {  
        candidatesMap.put(inputComponent, new ArrayList());
        ComponentView horizCandidateComponent = getHorizontalOutputComponent(inputComponent, componentViews);
        if (horizCandidateComponent != null) 
          candidatesMap.get(inputComponent).add(horizCandidateComponent);
        ComponentView vertCandidateComponent = getVerticalOutputComponent(inputComponent, componentViews);
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
        if (candidates.size() > 1)
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
  
  public static boolean repairOutputOrder(Collection componentViews)
  {
    boolean changes = false;
    int outputOrder = 1;
    List<ComponentView> sortedComponents = 
      getSortedComponentListByPosition(componentViews);
    for (ComponentView view : sortedComponents)
    {
      Integer newOutputOrder = outputOrder++;      
      if (!newOutputOrder.equals(view.getOutputOrder())) changes = true;
      view.setOutputOrder(newOutputOrder);
    }
    return changes;
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
        int outputOrder1 = (c1.getOutputOrder() == null ? 0 : c1.getOutputOrder());
        ComponentView c2 = (ComponentView)o2;
        int outputOrder2 = (c2.getOutputOrder() == null ? 0 : c2.getOutputOrder());
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
  
  private static void assignComponentId(ComponentView componentView)
  {
    if (componentView.getId() == null)
    {
      componentView.setId(componentView.getX() + "-" + componentView.getY());
    }
  }

  private static List<ComponentView> getInputComponents(Collection<ComponentView> componentViews)
  {
    List<ComponentView> result = new ArrayList();
    for (ComponentView component : componentViews)
    {
      if (isInputComponent(component)) result.add(component);
    }
    return result;
  }

  private static ComponentView getHorizontalOutputComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews)  
  {
    ComponentView candidateComponent = null;
    if (inputComponent instanceof RadioButtonView ||
      inputComponent instanceof CheckBoxView)
    {
      candidateComponent = getRightCandidate(inputComponent, componentViews);
    }
    else
    {
      candidateComponent = getLeftCandidate(inputComponent, componentViews);
    }
    return (isValidCandidate(candidateComponent) ? candidateComponent : null);
  }
  
  private static ComponentView getLeftCandidate(ComponentView inputComponent, 
    Collection<ComponentView> componentViews)
  {    
    ComponentView candidateComponent = null;    
    int minX = 0;
    int maxX = inputComponent.getX();
    int minY = inputComponent.getY() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxY = inputComponent.getY() + inputComponent.getHeight();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() <= maxY && 
        (candidateComponent == null || component.getX() > candidateComponent.getX()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;    
  }

  private static ComponentView getRightCandidate(ComponentView inputComponent, 
    Collection<ComponentView> componentViews)
  {    
    ComponentView candidateComponent = null;    
    int minX = inputComponent.getX() + inputComponent.getWidth();
    int maxX = Integer.MAX_VALUE;
    int minY = inputComponent.getY() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxY = inputComponent.getY() + inputComponent.getHeight();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() < maxX && 
        component.getY() >= minY && component.getY() <= maxY && 
        (candidateComponent == null || component.getX() < candidateComponent.getX()))
      {
        candidateComponent = component;
      }
    }
    return candidateComponent;
  }
  
  private static ComponentView getVerticalOutputComponent(ComponentView inputComponent, 
    Collection<ComponentView> componentViews)
  {
    ComponentView candidateComponent = null;
    int minX = inputComponent.getX() - COMPONENT_POSITION_ERROR_MARGIN;
    int maxX = inputComponent.getX() + inputComponent.getWidth();
    int minY = 0;
    int maxY = inputComponent.getY();
    for (ComponentView component : componentViews)
    {
      if (component.getX() >= minX && component.getX() <= maxX && 
        component.getY() >= minY && component.getY() < maxY && 
        (candidateComponent == null || component.getY() > candidateComponent.getY()))
      {
        candidateComponent = component;
      }
    }
    return (isValidCandidate(candidateComponent) ? candidateComponent : null);
  }
  
  private static boolean isValidCandidate(ComponentView candidateComponent)
  {
    return (candidateComponent != null && isOutputComponent(candidateComponent) 
      && !hasForElementAssigned(candidateComponent));
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
          if (inputComponent.getId().equals(labelView.getForElement())) return true;        
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
        if (Math.abs(c1.getY() - c2.getY()) > COMPONENT_POSITION_ERROR_MARGIN)
        {
          return c1.getY() - c2.getY();
        }
        else if (Math.abs(c1.getX() - c2.getX()) > COMPONENT_POSITION_ERROR_MARGIN)
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
          ComponentView inputComponent = getComponentById(componentViews, inputId);
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
 
  private static ComponentView getComponentById(Collection<ComponentView> componentViews, String id)
  {
    for (ComponentView auxComponent : componentViews)
    {
      if (id.equals(auxComponent.getId())) return auxComponent;
    }
    return null;
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
