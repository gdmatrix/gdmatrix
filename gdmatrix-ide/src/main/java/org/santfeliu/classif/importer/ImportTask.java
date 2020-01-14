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
package org.santfeliu.classif.importer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.matrix.classif.Class;
import org.matrix.classif.ClassFilter;
import org.matrix.classif.ClassificationManagerPort;
import org.matrix.classif.ClassificationManagerService;
import org.matrix.util.WSDirectory;
import org.matrix.util.WSEndpoint;
import org.santfeliu.util.TextUtils;

/**
 *
 * @author realor
 */
public class ImportTask extends Thread
{
  private final String url;
  private final String userId;
  private final String password;
  private final String nonTerminalFile;
  private final String terminalFile;
  private final ClassImporter classImporter;
  private final HashMap<String, ClassNode> classes =
    new HashMap<String, ClassNode>();
  private final HashMap<String, Class> oldClasses = 
    new HashMap<String, Class>();
  private final boolean update;

  public ImportTask(ClassImporter classImporter, boolean update)
  {
    this.classImporter = classImporter;
    this.url = classImporter.getUrl();
    this.userId = classImporter.getUserId();
    this.password = classImporter.getPassword();
    this.nonTerminalFile = classImporter.getNonTerminalFile();
    this.terminalFile = classImporter.getTerminalFile();
    this.update = update;
  }

  @Override
  public void run()
  {
    try
    {
      classes.clear();
      oldClasses.clear();
      ClassNode root = new ClassNode();
      org.matrix.classif.Class cls = new org.matrix.classif.Class();
      cls.setClassId("0000");
      cls.setTitle("QdC");
      cls.setSuperClassId(null);
      root.setUserObject(cls);

      classes.put("0000", root);
      if (nonTerminalFile != null && nonTerminalFile.length() > 0)
      {
        readNonTerminalFile();
      }
      if (terminalFile != null && terminalFile.length() > 0)
      {
        readTerminalFile();
      }
      WSDirectory wsDirectory = WSDirectory.getInstance(new URL(url));
      WSEndpoint endpoint =
        wsDirectory.getEndpoint(ClassificationManagerService.class);
      ClassificationManagerPort port =
        endpoint.getPort(ClassificationManagerPort.class, userId, password);

      HashSet<String> visited = new HashSet<String>();
      syncService(port, root, visited);
      classImporter.log("Classes to remove: " + oldClasses.keySet(), true);

      classImporter.log("Total classes: " + classes.size(), true);
      
      classImporter.setClassTreeModel(new DefaultTreeModel(root));
      classImporter.completed();
    }
    catch (Exception ex)
    {
      classImporter.log(ex.toString(), true);
      classImporter.completed();
    }
  }

  private void readNonTerminalFile() throws Exception
  {
    classImporter.log("Reading file " + nonTerminalFile + "...", true);
    BufferedReader reader = new BufferedReader(new FileReader(nonTerminalFile));
    reader.readLine(); // skip header
    String line = reader.readLine();
    while (line != null)
    {
      String[] parts = readCSVLine(line);
      if (parts.length >= 3)
      {
        String classId = pad(parts[0], 4);
        String title = parts[2];
        String superClassId = pad(parts.length >= 4 ? parts[3] : "0", 4);
        classImporter.log(">> " + line, false);
        classImporter.log(classId + ", " + title + ", " + superClassId, false);
        addClass(classId, title, superClassId, null, null, null);
      }
      line = reader.readLine();
    }
  }

  private void readTerminalFile() throws Exception
  {
    classImporter.log("Reading file " + terminalFile + "...", true);
    BufferedReader reader = new BufferedReader(new FileReader(terminalFile));
    reader.readLine(); // skip header
    String line = reader.readLine();
    while (line != null)
    {
      String[] parts = readCSVLine(line);
      if (parts.length >= 7)
      {
        String superClassId = pad(parts[0], 4);
        String classId = pad(parts[1], 4);
        String title = parts[2];
        String state = parts[3];
        String taad = parts[5];
        String startDateTime = parseDate(parts[6]);
        classImporter.log(">> " + line, false);
        classImporter.log(classId + ", " + title + ", " + superClassId, false);
        addClass(classId, title, superClassId, taad, startDateTime, state);
      }
      line = reader.readLine();
    }
  }

  private void syncService(ClassificationManagerPort port, ClassNode classNode,
    Set<String> visited)
  {
    Class newClass = (Class)classNode.getUserObject();
    String classId = newClass.getClassId();
    if (visited.contains(classId))
    {
      classImporter.log("LOOP FOUND!!! => " + classId + " " +
        newClass.getTitle(), true);
      return;
    }
    visited.add(classId);
    oldClasses.remove(classId);
    
    try
    {
      Class oldClass = port.loadClass(classId, null);
      Thread.sleep(100);
      newClass.getProperty().addAll(oldClass.getProperty());
      if (newClass.getProperty().size() > 0)
      {
        classImporter.log("properties found for class: " + classId, true);
      }
      if (newClass.getStartDateTime() == null)
      {
        newClass.setStartDateTime(oldClass.getStartDateTime());
      }
      else if (oldClass.getStartDateTime() != null)
      {
        if (newClass.getStartDateTime().compareTo(oldClass.getStartDateTime()) < 0)
        {
          newClass.setStartDateTime(oldClass.getStartDateTime());
        }
      }
      classNode.setState(ClassNode.UPDATE);
      classImporter.log("Class found: " + classId, false);
    }
    catch (Exception ex)
    {
      classImporter.log("Class not found: " + classId, false);
      classNode.setState(ClassNode.NEW);
      // node is new;
    }

    if (newClass.getStartDateTime() == null || 
      newClass.getStartDateTime().length() == 0)
    {
      newClass.setStartDateTime("20000101000000");
    }
    if (update)
    {
      classImporter.log("storeClass(" + newClass.getClassId() + ", " + 
        newClass.getTitle() + ", " + newClass.getStartDateTime() + ", " +
        newClass.getEndDateTime() + ", " + newClass.getDescription() + ", " +
        newClass.getProperty().size() + ")", 
        false);
      newClass.setClassTypeId("Class");
      port.storeClass(newClass);
    }
    ClassFilter filter = new ClassFilter();
    filter.setSuperClassId(classId);
    filter.setStartDateTime(TextUtils.formatDate(new Date(), "yyyyMMddHHmmss"));
    List<Class> classList = port.findClasses(filter);
    for (Class cls : classList)
    {
      classImporter.log("oldClass " + cls.getClassId(), false);
      oldClasses.put(cls.getClassId(), cls);
    }
    
    for (int i = 0; i < classNode.getChildCount(); i++)
    {
      ClassNode childNode = (ClassNode)classNode.getChildAt(i);
      syncService(port, childNode, visited);
    }
  }
  
  private String[] readCSVLine(String line)
  {
    ArrayList<String> parts = new ArrayList<String>();
    boolean inString = false;
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < line.length(); i++)
    {
      char ch = line.charAt(i);
      if (inString)
      {
        if (ch == '"')
        {
          inString = false;
        }
        else
        {
          buffer.append(ch);
        }
      }
      else // out of string
      {
        if (ch == '"')
        {
          inString = true;
        }
        else if (ch == ';')
        {
          inString = false;
          parts.add(buffer.toString());
          buffer.setLength(0);
        }
        else
        {
          buffer.append(ch);
        }
      }
    }
    parts.add(buffer.toString());
    classImporter.log(parts.toString(), false);
    return parts.toArray(new String[parts.size()]);
  }

  private void addClass(String classId, String title, String superClassId,
    String taad, String startDateTime, String state)
  {
    ClassNode node = classes.get(classId);
    if (node == null)
    {
      node = new ClassNode();
      Class cls = new Class();
      cls.setClassId(classId);
      if (title.length() > 200) title = title.substring(0, 200);
      cls.setTitle(title);
      cls.setSuperClassId(superClassId);
      cls.setStartDateTime(startDateTime);
      cls.setDescription(taad);
      if (state != null && state.toLowerCase().startsWith("no"))
      {
        String nowDateTime = TextUtils.formatDate(new Date(), "yyyyMMddHHmmss");
        cls.setEndDateTime(nowDateTime);
      }
      node.setUserObject(cls);
      classes.put(classId, node);
    }
    else // class already exists
    {
      Class cls = (Class)node.getUserObject();
      String description = cls.getDescription();
      if (description == null) description = taad;
      else description += " " + taad;
      cls.setDescription(description);
      classImporter.log("DUPLICATED CLASS: " + classId + " " + title, true);
      return;
    }
    
    // bind parent
    ClassNode parentNode = classes.get(superClassId);
    if (parentNode != null)
    {
      parentNode.add(node);
    }
  }

  private String pad(String classId, int size)
  {
    classId = classId.trim();
    if (classId.endsWith("."))
      classId = classId.substring(0, classId.length() - 1);
    while (classId.length() < size)
    {
      classId = "0" + classId;
    }
    return classId;
  }

  private String parseDate(String dateString)
  {
    Date date = null;
    try
    {
      String[] parts = dateString.split("-");
      String day = pad(parts[0], 2);
      String month = "01";
      String year = parts[2];
      String monthName = parts[1].toLowerCase();
      if (monthName.startsWith("gen")) month = "01";
      else if (monthName.startsWith("feb")) month = "02";
      else if (monthName.startsWith("mar")) month = "03";
      else if (monthName.startsWith("abr")) month = "04";
      else if (monthName.startsWith("mai")) month = "05";
      else if (monthName.startsWith("jun")) month = "06";
      else if (monthName.startsWith("jul")) month = "07";
      else if (monthName.startsWith("ago")) month = "08";
      else if (monthName.startsWith("set")) month = "09";
      else if (monthName.startsWith("oct")) month = "10";
      else if (monthName.startsWith("nov")) month = "11";
      else if (monthName.startsWith("des")) month = "12";
      SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
      date = df.parse(day + "-" + month + "-" + year);
    }
    catch (Exception ex)
    {
      try
      {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy");
        date = df.parse(dateString);
      }
      catch (Exception ex2)
      {
        try
        {
          SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
          date = df.parse(dateString);
        }
        catch (Exception ex3)
        {
        }
      }
    }
    if (date == null) return null;
    SimpleDateFormat df2 = new SimpleDateFormat("yyyyMMdd");
    return df2.format(date) + "000000";
  }

  public class ClassNode extends DefaultMutableTreeNode
  {
    public static final String NEW = "NEW";
    public static final String UPDATE = "UPDATE";

    private String state = "NEW";

    public String getState()
    {
      return state;
    }

    public void setState(String state)
    {
      this.state = state;
    }
  }
}
