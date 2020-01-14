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
package org.santfeliu.swing;

import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author unknown
 */
public class DateSelectorPanel
  extends JPanel
{
  //public static final String NOW = "NOW";
  //private String now = "NOW";
  private String now;
  private JLabel dateLabel = new JLabel();
  private JComboBox dateComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private String dateFormatPattern = "dd/MM/yyyy";
  private Map calendarMap = new HashMap();
  private Vector listeners = new Vector();
  private Calendar selectedTime = null;

  private ResourceBundle resourceBundle = 
    loadResourceBundle(Locale.getDefault());   

  public DateSelectorPanel()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
    throws Exception
  {
    now = getLocalizedText("nowUPPER");
  
    this.setLayout(gridBagLayout1);
    this.setSize(new Dimension(400, 105));
    dateLabel.setText(getLocalizedText("date") + ":");
    dateComboBox.setEditable(true);
    dateComboBox.setPreferredSize(new Dimension(140, 24));
    dateComboBox.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            dateComboBox_actionPerformed(e);
          }
        });
    this.add(dateComboBox, 
             new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(dateLabel, 
             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 
                                    new Insets(0, 0, 0, 4), 0, 0));
  }
  
  public void addTime(Calendar dateCalendar, String text)
    throws ParseException
  {
    if (text != null || dateCalendar != null)
    {
      if (text == null)
      {
        Date time = dateCalendar.getTime();
        text =  new SimpleDateFormat(dateFormatPattern).format(time);
      }
      if (dateCalendar == null)
      {
        if (!now.equals(text))
        {
          dateCalendar = Calendar.getInstance();      
          Date time = new SimpleDateFormat(dateFormatPattern).parse(text);
          dateCalendar.setTime(time);
        }
      }
    }
    if (!calendarMap.containsKey(text))
    {
      calendarMap.put(text, dateCalendar);
      dateComboBox.addItem(text);
    }
  }
  
  public void addNow(String text)
    throws ParseException
  {
    now = text;
    addTime(null, now);
  }
  
  public void addNull()
    throws ParseException
  {
    addTime(null, null);
  }
 
  public Calendar getSelectedTime()
  {
    if (selectedTime != null)
      return selectedTime;
    else
    {
      if (dateComboBox.getSelectedItem() == null || 
         ((String)dateComboBox.getSelectedItem()).length() == 0)
        return null;
      else
        return Calendar.getInstance();
    }
      
    //return selectedTime == null ? Calendar.getInstance() : selectedTime;
  }

  public boolean isNowSelected()
  {
    return selectedTime == null;
  }

  public void removeAllItems()
  {
    dateComboBox.removeAllItems();
  }

  public void setDateFormatPattern(String dateFormatPattern)
  {
    this.dateFormatPattern = dateFormatPattern;
  }

  public String getDateFormatPattern()
  {
    return dateFormatPattern;
  }

  private Calendar parseTime() throws Exception
  {
    String dateText = (String)dateComboBox.getSelectedItem();
    if (now.equals(dateText)) return null;
    
    Calendar result;
    if (calendarMap.containsKey(dateText))
      result = (Calendar)calendarMap.get(dateText);
    else
    {
      Date time = new SimpleDateFormat(dateFormatPattern).parse(dateText);        
      result = Calendar.getInstance();
      result.setTime(time);
    }
    return result; 
  }

  private void dateComboBox_actionPerformed(ActionEvent e)
  {
    String actionCommand = e.getActionCommand();
    if ("comboBoxEdited".equals(actionCommand))
    {
      try
      {
        addTime(null, dateComboBox.getSelectedItem().toString());
      }
      catch (ParseException ex)
      {
        ex.printStackTrace();
      }
    }
    else if ("comboBoxChanged".equals(actionCommand))
    {
      Calendar time;
      try
      {
        time = parseTime();
        if (time == null)
        {
          if (selectedTime != null)
          {
            selectedTime = time;
            fireStateChanged(new ChangeEvent(this));
          }
        }
        else
        {
          if (!time.equals(selectedTime))
          {
            selectedTime = time;
            fireStateChanged(new ChangeEvent(this));
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  public void addChangeListener(ChangeListener l)
  {
    listeners.add(l);
  }
  
  public void removeChangeListener(ChangeListener l)
  {
    listeners.remove(l);
  }
  
  protected void fireStateChanged(ChangeEvent event)
  {
    Iterator iter = listeners.iterator();
    while (iter.hasNext())
    {
      ChangeListener l = (ChangeListener)iter.next();
      l.stateChanged(event);
    }
  }
  
  private ResourceBundle loadResourceBundle(Locale locale) 
  {    
    return ResourceBundle.getBundle("org.santfeliu.swing.resources.DateSelectorBundle", locale);
  }
  
  public String getLocalizedText(String text) 
  {
    String result = null;
    try
    {
      result = resourceBundle.getString(text);
    }
    catch (MissingResourceException ex)
    {
      result = "{" + text + "}";
    }
    return result;
  }

  public static void main(String args[])
  {
    final DateSelectorPanel panel = new DateSelectorPanel();
    try
    {
      panel.addTime(Calendar.getInstance(), "NOW");
      panel.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent ev)
        {
          System.out.println(panel.isNowSelected());
          System.out.println(panel.getSelectedTime());
        }
      });
      JFrame frame = new JFrame();
      frame.setSize(150, 75);
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
