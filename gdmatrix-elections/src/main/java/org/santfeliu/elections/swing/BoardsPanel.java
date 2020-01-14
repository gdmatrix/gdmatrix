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
package org.santfeliu.elections.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Collection;

import java.util.Iterator;

import java.util.Vector;

import javax.swing.JPanel;

import org.santfeliu.elections.Board;
import org.santfeliu.elections.Results;

/**
 *
 * @author unknown
 */
public class BoardsPanel extends JPanel
{
  private Vector districtPanes = new Vector();
  private Vector sectionPanes = new Vector();
  private int boxSize = 24;
  private int boardSize = 16;
  
  private Font normalFont = new Font("Arial", Font.PLAIN, 12);
  private Font smallFont = new Font("Arial", Font.PLAIN, 11);
  
  private Color selScruFillColor = Color.green;
  private Color selScruTextColor = Color.black;
  private Color selUnscruFillColor = new Color(100, 100, 100);
  private Color selUnscruTextColor = Color.white;
  private Color selBorderColor = Color.black;

  private Color unselScruFillColor = new Color(210, 254, 210);
  private Color unselScruTextColor = Color.lightGray;
  private Color unselUnscruFillColor = new Color(225, 225, 225);
  private Color unselUnscruTextColor = Color.lightGray;
  private Color unselBorderColor = Color.lightGray;
  
  private String currentDistrict;
  private String currentSection;
  private String currentBoardName;
  private Vector listeners = new Vector();

  public BoardsPanel()
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
  
  public String getCurrentDistrict()
  {
    return currentDistrict;
  }

  public String getCurrentSection()
  {
    return currentSection;
  }
  
  public String getCurrentBoardName()
  {
    return currentBoardName;
  }
  
  public void addActionListener(ActionListener l)
  {
    listeners.add(l);
  }
  
  public void removeActionListener(ActionListener l)
  {
    listeners.remove(l);
  }
  
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.black);
    ((Graphics2D)g).setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING, 
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    placeSections();
    Iterator iter = sectionPanes.iterator();
    while (iter.hasNext())
    {
      SectionPane sectionPane = (SectionPane)iter.next();
      sectionPane.paint(g);
    }
    iter = districtPanes.iterator();
    while (iter.hasNext())
    {
      DistrictPane districtPane = (DistrictPane)iter.next();
      districtPane.paint(g);
    }
    if (districtPanes.size() > 0)
    {
      DistrictPane dp = 
        (DistrictPane)districtPanes.get(districtPanes.size() - 1);
      int y = dp.getBottomY();
      g.setColor(Color.black);
      g.drawString("D:districte  S:secció", 4, y + getFont().getSize());
    }
  }
  
  public void updateData(Results results)
  {
    currentDistrict = results.getCurrentDistrict();
    currentSection = results.getCurrentSection();
    currentBoardName = results.getCurrentBoardName();
    exploreBoards(results);
    repaint();
  }
  
  private void exploreBoards(Results results)
  {
    Collection boards = results.getBoards();
    districtPanes.clear();
    sectionPanes.clear();
    Board prevBoard = null;
    SectionPane sectionPane = null;
    Iterator iter = boards.iterator();
    while (iter.hasNext())
    {
      Board board = (Board)iter.next();
      if (prevBoard == null ||
        !prevBoard.getDistrict().equals(board.getDistrict()) || 
        !prevBoard.getSection().equals(board.getSection())) // new section
      {
        sectionPane = new SectionPane();
        sectionPane.district = board.getDistrict();
        sectionPane.section = board.getSection();
        sectionPanes.add(sectionPane);
        if (prevBoard == null ||
          !board.getDistrict().equals(prevBoard.getDistrict()))
        {
          DistrictPane districtPane = new DistrictPane();
          districtPane.district = board.getDistrict();
          districtPane.description = results.getDistrict(districtPane.district);
          districtPanes.add(districtPane);
        }
      }
      sectionPane.addBoard(board);
      prevBoard = board;
    }
  }
  
  private void placeSections()
  {
    int columns = getWidth() / boxSize;
  
    // placement
    Iterator iter = sectionPanes.iterator();
    String prevDistrict = null;
    int column = 1;
    int row = 0;
    int districtIndex = 0;
    DistrictPane districtPane = null;
    while (iter.hasNext())
    {
      SectionPane sectionPane = (SectionPane)iter.next();
      if (prevDistrict == null || 
        !sectionPane.district.equals(prevDistrict))
      {
        districtPane = (DistrictPane)districtPanes.elementAt(districtIndex);
        districtPane.rowCount = 1;
        if (prevDistrict == null)
        {
          districtPane.row = row;
        }
        else
        {
          column = 1;
          row++;
          districtPane.row = row;
        }
        row++;
        districtPane.rowCount++;
        districtIndex++;
      }
      else if (column + 1 + sectionPane.getBoardsCount() > columns)
      {
        row++;
        column = 1;
        districtPane.rowCount++;
      }
      sectionPane.row = row;
      sectionPane.column = column;
      column += (1 + sectionPane.getBoardsCount());
      prevDistrict = sectionPane.district;
    }
  }

  private boolean match(String s1, String s2)
  {
    return s1 == null || s1.equals(s2);
  }

  protected void dispatchActionEvent(ActionEvent event)
  {
    for (int i = 0; i < listeners.size(); i++)
    {
      ActionListener l = (ActionListener)listeners.elementAt(i);
      l.actionPerformed(event);
    }
  }

  private void jbInit()
    throws Exception
  {
    this.setSize(new Dimension(388, 364));
    this.setToolTipText(
    "Botó esquerre del ratolí: selecciona àmbit, " +
    "botó dret del ratolí: activa totes les meses");
    addMouseListener(new MouseAdapter()
    {
      public void mouseReleased(MouseEvent event)
      {
        currentDistrict = null;
        currentSection = null;
        currentBoardName = null;
        
        if ((event.getModifiers() & MouseEvent.BUTTON1_MASK) != 0)
        {
          Point point = event.getPoint();
          boolean found = false;
          Iterator  iter = sectionPanes.iterator();
          while (iter.hasNext() && !found)
          {
            SectionPane sectionPane = (SectionPane)iter.next();
            found = sectionPane.select(point);
          }
          iter = districtPanes.iterator();
          if (!found)
          {
            while (iter.hasNext() && !found)
            {
              DistrictPane districtPane = (DistrictPane)iter.next();
              found = districtPane.select(point);
            }
          }
        }
        repaint();
        dispatchActionEvent(
          new ActionEvent(BoardsPanel.this, 0, "scopeChange"));
      }
    });
  }

  class DistrictPane
  {
    String district;
    String description;
    int row;
    int rowCount;
    
    public void paint(Graphics g)
    {
      g.setFont(smallFont);    
      int y = row * boxSize;        
      g.setColor(Color.black);
      String text = "D" + district + ": " + description;
      g.drawString(text, 3, y + g.getFont().getSize() + 2);

      y = (row + rowCount) * boxSize;
      g.setColor(Color.gray);
      g.drawLine(0, y, getWidth(), y);
    }
    
    public int getBottomY()
    {
      return (row + rowCount) * boxSize;
    }
    
    public String toString()
    {
      return district + " " + row;
    }
    
    public boolean select(Point point)
    {
      int y = row * boxSize;      
      Rectangle rect = new Rectangle(0, y, getWidth(), rowCount * boxSize);
      boolean selected = rect.contains(point);
      if (selected) currentDistrict = district;
      return selected;
    }
  }
  
  class SectionPane
  {
    String district;
    String section;
    Vector sectionBoards = new Vector();
    int row;
    int column;

    void addBoard(Board board)
    {
      sectionBoards.add(board);
    }
    
    int getBoardsCount()
    {
      return sectionBoards.size();
    }
    
    public void paint(Graphics g)
    {    
      g.setFont(normalFont);
      int x = column * boxSize;
      int y = row * boxSize;
      g.setColor(Color.lightGray);
      g.drawRect(x, y, boxSize * (1 + getBoardsCount()), boxSize);
      g.setColor(Color.black);
      g.drawString("S" + section, x + 4, y + boxSize - 6);
      for (int i = 0; i < sectionBoards.size(); i++)
      {
        Board sb = (Board)sectionBoards.elementAt(i);
        x = (column + i + 1) * boxSize;
        y = row * boxSize + (boxSize - boardSize) / 2;
        Color fillColor = null;
        Color textColor = null;
        Color borderColor = null;
        if (match(currentDistrict, sb.getDistrict()) && 
           match(currentSection, sb.getSection()) &&
           match(currentBoardName, sb.getBoardName()))
        {
          borderColor = selBorderColor;
          if (sb.isScrutinized())
          {
            fillColor = selScruFillColor;
            textColor = selScruTextColor;
          }
          else
          {
            fillColor = selUnscruFillColor;
            textColor = selUnscruTextColor;
          }
        }
        else
        {
          borderColor = unselBorderColor;
          if (sb.isScrutinized())
          {          
            fillColor = unselScruFillColor;
            textColor = unselScruTextColor;
          }
          else
          {
            fillColor = unselUnscruFillColor;
            textColor = unselUnscruTextColor;
          }
        }
        g.setColor(fillColor);
        g.fillRect(x, y, boardSize, boardSize);
        g.setColor(borderColor);
        g.drawRect(x, y, boardSize, boardSize);

        String boardName = sb.getBoardName();
        if (boardName.length() == 0) boardName = "U";
        g.setColor(textColor);
        g.drawString(boardName, x + 4, y + boardSize - 3);
      }
    }

    public boolean select(Point point)
    {
      int x = column * boxSize;
      int y = row * boxSize;
      Rectangle rect = 
        new Rectangle(x, y, boxSize * (1 + getBoardsCount()), boxSize);
      boolean selected = rect.contains(point);
      if (selected)
      {
        currentDistrict = district;
        currentSection = section;
        boolean found = false;
        int i = 0;
        while (!found && i < sectionBoards.size())
        {
          Board sb = (Board)sectionBoards.elementAt(i);
          x = (column + i + 1) * boxSize;
          y = row * boxSize + (boxSize - boardSize) / 2;
          rect = new Rectangle(x, y, boardSize, boardSize);
          if (rect.contains(point))
          {
            currentBoardName = sb.getBoardName();
          }
          i++;
        }
      }
      return selected;
    }    
  }
}
