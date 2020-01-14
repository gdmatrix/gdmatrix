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
package org.santfeliu.faces.fckeditor.util;

import java.io.Serializable;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;

/**
 *
 * @author unknown
 */
public class Tags {
   public static void setString(UIComponent component, String attributeName,
         String attributeValue) {
      if (attributeValue == null)
         return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else
         component.getAttributes().put(attributeName, attributeValue);
   }

   public static void setInteger(UIComponent component, 
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else 
         component.getAttributes().put(attributeName, 
               new Integer(attributeValue));
   }

   public static void setBoolean(UIComponent component, 
         String attributeName, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue))
         setValueBinding(component, attributeName, attributeValue);
      else 
         component.getAttributes().put(attributeName, 
               new Boolean(attributeValue));
   }
   
   public static void setValueBinding(UIComponent component, String attributeName,
         String attributeValue) {
      FacesContext context = FacesContext.getCurrentInstance();
      Application app = context.getApplication();
      ValueBinding vb = app.createValueBinding(attributeValue);
      component.setValueBinding(attributeName, vb);
   }

   public static void setActionListener(UIComponent component, String attributeValue) {
      setMethodBinding(component, "actionListener", attributeValue,
            new Class[] { ActionEvent.class });      
   }

   public static void setValueChangeListener(UIComponent component, 
         String attributeValue) {
      setMethodBinding(component, "valueChangeListener", attributeValue,
            new Class[] { ValueChangeEvent.class });      
   }

   public static void setValidator(UIComponent component, 
         String attributeValue) {
      setMethodBinding(component, "validator", attributeValue,
            new Class[] { FacesContext.class, UIComponent.class, Object.class });      
   }

   public static void setAction(UIComponent component, String attributeValue) {
      if (attributeValue == null) return;
      if (UIComponentTag.isValueReference(attributeValue))
      {
         setMethodBinding(component, "action", attributeValue,
               new Class[] {});
      }
      else
      {
         MethodBinding mb = new ActionMethodBinding(attributeValue);
         component.getAttributes().put("action", mb);         
      }
   }
      
   public static void setMethodBinding(UIComponent component, String attributeName,
         String attributeValue, Class[] paramTypes) {
      if (attributeValue == null)
         return;
      if (UIComponentTag.isValueReference(attributeValue)) {
         FacesContext context = FacesContext.getCurrentInstance();
         Application app = context.getApplication();
         MethodBinding mb = app.createMethodBinding(attributeValue, paramTypes);
         component.getAttributes().put(attributeName, mb);
      }
   }     
   
   private static class ActionMethodBinding 
         extends MethodBinding 
         implements Serializable 
   {      
	   /**
	    * 
	    */
	   private static final long serialVersionUID = 6027374557161316454L;
	   private String result;
   
      public ActionMethodBinding(String result) { this.result = result; }      
      public Object invoke(FacesContext context, Object params[]) {
         return result;
      }
      public String getExpressionString() { return result; }
      public Class getType(FacesContext context) { return String.class; }
   }
}
