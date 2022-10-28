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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.net.URL;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.santfeliu.elections.Councillor;
import org.santfeliu.elections.Party;
import org.santfeliu.elections.Results;
import org.santfeliu.elections.Results.PartyStats;


/**
 *
 * @author realor
 */
public class CouncillorsPanel extends JPanel
{
  private Animator animator;
  private int paintedCouncillors = 0;
  private Collection parties;
  private Collection electedCouncillors;
  private int councillorsCount;
  private int totalVotes;
  private Map partyStats;
  private boolean doPaint;
  private String toolTip = null;
  private ArrayList councillorsAreas = new ArrayList();
  private HashMap logoCache = new HashMap();
  private HashMap photoCache = new HashMap();
  private ImageIcon samplePhoto;

  private Font normalFont = new Font("Arial", Font.PLAIN, 12);
  private Font mediumFont = new Font("Arial", Font.BOLD, 14);
  private Font bigFont = new Font("Arial", Font.BOLD, 28);
  private int partyMargin = 10;
  private int partyWidth = 50;
  private int boxMargin = 4; // box that contains councillors photo
  private int boxWidth = 40;
  private int boxHeight = 60;
  private int logoWidth = 50;
  private int logoHeight = 50;
  private int photoWidth = 37;
  private int photoHeight = 44;
  private int rowHeight = boxHeight + boxMargin;
  private boolean newData = true;

  public CouncillorsPanel()
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

  private void jbInit() throws Exception
  {
    this.setSize(new Dimension(510, 481));
    URL url = getClass().getClassLoader().getResource(
      "org/santfeliu/elections/swing/resources/icon/photo.gif");
    samplePhoto = new ImageIcon(url);

    addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseMoved(MouseEvent e)
      {
        Point point = e.getPoint();
        Iterator iter = councillorsAreas.iterator();
        String selectedName = null;
        Rectangle rect = null;
        while (selectedName == null && iter.hasNext())
        {
          Object[] elem = (Object[])iter.next();
          rect = (Rectangle)elem[0];
          String name = (String)elem[1];
          if (rect.contains(point)) selectedName = name;
        }
        if (selectedName != null)
        {
          if (!selectedName.equals(toolTip))
          {
            CouncillorsPanel.this.setToolTipText(selectedName);
          }
        }
        else
        {
          if (toolTip != null)
          {
            CouncillorsPanel.this.setToolTipText(null);
          }
        }
        toolTip = selectedName;
      }
    });
  }

  @Override
  public Dimension getPreferredSize()
  {
    if (parties == null || partyStats == null) return new Dimension(10, 10);
    int width = 0;
    int height = (parties.size() + 1) * rowHeight + 10;

    Iterator iter = parties.iterator();
    while (iter.hasNext())
    {
      Party party = (Party)iter.next();
      PartyStats stats = (PartyStats)partyStats.get(party.getId());
      if (stats != null)
      {
        int nc = stats.getNumElectedCouncillors();
        int nw = partyMargin + partyWidth + (nc + 3) * (boxWidth + boxMargin);
        if (nw > width) width = nw;
      }
    }
    return new Dimension(width, height);
  }

  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(Color.black);
    if (!doPaint) return;

    councillorsAreas.clear();
    DecimalFormat df1 = new DecimalFormat("#,###,##0.00");
    DecimalFormat df2 = new DecimalFormat("#,###,##0");
    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Iterator iter = parties.iterator();
    while (iter.hasNext())
    {
      Party party = (Party)iter.next();
      int row = party.getOrder() - 1;
      Image logoImage = getLogo(party);
      if (logoImage != null)
      {
        g.drawImage(logoImage, partyMargin,
          row * rowHeight + (rowHeight - logoHeight) / 2,
          logoWidth, logoHeight, this);
      }
      else
      {
        g.setFont(normalFont);
        g.setColor(Color.black);
        String abbr = party.getAbbreviation();
        g.drawString(abbr, partyMargin,
          row * rowHeight + (normalFont.getSize() + rowHeight) / 2);
      }
      g.setColor(Color.lightGray);
      g.drawLine(0, row * rowHeight - 2, getWidth(), row * rowHeight - 2);
      g.setColor(Color.black);
      if (paintedCouncillors >= electedCouncillors.size())
      {
        PartyStats stats = (PartyStats)partyStats.get(party.getId());
        if (stats != null)
        {
          String nc = String.valueOf(stats.getNumElectedCouncillors());
          g.setFont(bigFont);
          int ncWidth =
            (int)g.getFontMetrics().getStringBounds(nc, g).getWidth();
          g.drawString(nc,
            partyMargin + partyWidth + 2 * (boxWidth - ncWidth) / 3,
            row * rowHeight + (bigFont.getSize() + rowHeight) / 2);

          int xStats = partyWidth + boxWidth + 15 +
            stats.getNumElectedCouncillors() * (boxMargin + boxWidth);
          int yStats = row * rowHeight + normalFont.getSize() +
            (rowHeight  - (4 * mediumFont.getSize())) / 2;

          g.setFont(mediumFont);
          g.setColor(Color.black);
          int totalVotes = stats.getTotalVotes();
          String votesLabel = totalVotes == 1 ? "vot" : "vots";
          g.drawString(df2.format(totalVotes) + " " + votesLabel,
            xStats, yStats);

          g.setColor(Color.blue);
          double votesPercentage = stats.getVotesPercentage();
          String perc = df1.format(votesPercentage) + "%";
          if (votesPercentage < 5.0) perc += " (inferior al 5%)";
          g.drawString(perc, xStats,
            yStats + mediumFont.getSize());

          g.setColor(Color.red);
          if (stats.isTakesLastCouncillor())
          {
            g.drawString("darrer regidor (" +
              df1.format(stats.getLastCouncillorScore()) + ")",
              xStats, yStats + 2 * mediumFont.getSize());
          }
          else if (stats.getNumElectedCouncillors() < councillorsCount)
          {
            String draw = (stats.getVotesToNextCouncillor() == 0 &&
              stats.getTotalVotes() > 0) ? " REGIDOR PER SORTEIG" : "";
            g.drawString("+" + stats.getVotesToNextCouncillor() + " var (" +
              df1.format(stats.getNextCouncillorScore()) + ")" + draw,
              xStats, yStats + 2 * mediumFont.getSize());
          }
          g.setColor(Color.gray);
          double linearCouncillors = stats.getLinearCouncillors();
          g.drawString(df1.format(linearCouncillors) + " RL", xStats,
            yStats + 3 * mediumFont.getSize());
        }
      }
    }
    int dividerPos = parties.size() * rowHeight - 2;
    g.setColor(new Color(250, 250, 200));
    g.fillRect(0, dividerPos, getWidth(), getHeight() - dividerPos);
    g.setColor(Color.gray);
    g.drawLine(0, dividerPos, getWidth(), dividerPos);
    g.setColor(Color.black);

    if (electedCouncillors == null) return;
    int c = 1;
    iter = electedCouncillors.iterator();
    while (iter.hasNext() && c <= paintedCouncillors)
    {
      Councillor councillor = (Councillor)iter.next();
      Party party = councillor.getParty();
      Rectangle rect = getBox(councillor);
      double score = councillor.getScore();
      String name = councillor.getName();

      if (party.getColor() != null)
      {
        g.setColor(party.getColor());
      }
      ((Graphics2D)g).fill(rect);
      councillorsAreas.add(new Object[]{rect,
        name + " (puntuació segons llei d'Hondt: " + score + ")"});
      Image photoImage = getPhoto(councillor);
      if (photoImage != null)
      {
        g.drawImage(photoImage, rect.x + 2, rect.y + 2,
          photoWidth, photoHeight, this);
      }
      g.setFont(normalFont);
      g.setColor(Color.black);
      String value = String.valueOf(c);
      int fontWidth =
        (int)(g.getFontMetrics().getStringBounds(value, g).getWidth());
      g.drawString(value,
        rect.x + rect.width - fontWidth - 2,
        rect.y + rect.height - 2);
      ((Graphics2D)g).draw(rect);
      c++;
    }

    // paint unassigned councillors
    if (totalVotes > 0)
    {
      int unassignedCouncillors = councillorsCount - electedCouncillors.size();
      if (unassignedCouncillors > 0 &&
        paintedCouncillors >= electedCouncillors.size())
      {
        String text = String.valueOf(unassignedCouncillors);
        if (unassignedCouncillors == 1) text += " REGIDOR PER SORTEIG";
        else text += " REGIDORS PER SORTEIG";
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        double textWidth = g.getFontMetrics().getStringBounds(text, g).getWidth();
        int x = (int)(getWidth() - (int)textWidth - 6);
        int y = 18;
        g.setColor(Color.WHITE);
        g.fillRect(x - 4, 0, getWidth(), y + 4);
        g.setColor(Color.RED);
        g.drawString(text, x, y);
      }
    }

    // paint legend
    g.setFont(normalFont);
    int xLegend =  partyWidth + partyMargin + boxWidth + boxMargin;
    int yLegend = parties.size() * rowHeight + 10;

    Rectangle refRect =
      new Rectangle(xLegend, yLegend, boxWidth, boxHeight);
    g.setColor(Color.lightGray);
    ((Graphics2D)g).fill(refRect);
    g.drawImage(samplePhoto.getImage(), refRect.x + 2, refRect.y + 2,
      photoWidth, photoHeight, this);
    g.setColor(Color.black);
    String letter = "N";
    g.drawString(letter,
      refRect.x + refRect.width -
        (int)g.getFontMetrics().getStringBounds(letter, g).getWidth() - 2,
      refRect.y + refRect.height - 2);
    ((Graphics2D)g).draw(refRect);
    g.setColor(Color.black);
    g.drawString("9999 vots",
      xLegend + boxMargin + boxWidth, yLegend + normalFont.getSize());
    g.drawString(": nombre total de vots de la candidatura",
      xLegend + boxMargin + boxWidth + 60, yLegend + normalFont.getSize());

    g.setColor(Color.blue);
    g.drawString("99,99%",
      xLegend + boxMargin + boxWidth, yLegend + 2 * normalFont.getSize());
    g.drawString(": percentatge de vots de la candidatura",
      xLegend + boxMargin + boxWidth + 60, yLegend +  2 * normalFont.getSize());

    g.setColor(Color.red);
    g.drawString("+999 var",
      xLegend + boxMargin + boxWidth, yLegend + 3 * normalFont.getSize());
    g.drawString(": nombre de vots que li ha faltat per aconseguir un altre regidor (puntuació segons llei d'Hondt del següent/darrer regidor)",
      xLegend + boxMargin + boxWidth + 60, yLegend +  3 * normalFont.getSize());

    g.setColor(Color.gray);
    g.drawString("99,99 RL",
      xLegend + boxMargin + boxWidth, yLegend + 4 * normalFont.getSize());
    g.drawString(": nombre de regidors amb repartiment lineal (veure ajuda)",
      xLegend + boxMargin +boxWidth + 60, yLegend +  4 * normalFont.getSize());

    g.setColor(Color.black);
    g.drawString("N : ordre del regidor segons l'aplicació de la llei d'Hondt",
      xLegend + boxMargin + boxWidth, yLegend +  5 * normalFont.getSize());
  }

  public void updateData(Results results)
  {
    if ("1".equals(results.getCallId()))
    {
      doPaint = true;
      newData = true;
      parties = results.getParties();
      electedCouncillors = results.getElectedCouncillors();
      councillorsCount = results.getCouncillorsCount();
      totalVotes = results.getTotalVotes();
      partyStats = results.getPartyStats();
      invalidate();
      if (isPanelVisible()) animate();
    }
    else
    {
      doPaint = false;
      newData = false;
      parties = null;
      electedCouncillors = null;
      partyStats = null;
      invalidate();
    }
  }

  private boolean isPanelVisible()
  {
    Container c = getParent();
    while (c != null)
    {
      if (!c.isVisible())
        return false;
      else
         c = c.getParent();
    }
    return true;
  }

  public void animate()
  {
    if (animator == null && newData)
    {
      animator = new Animator();
      animator.start();
    }
  }

  private Rectangle getBox(Councillor councillor)
  {
    Party party = councillor.getParty();
    int po = party.getOrder() - 1;
    int co = councillor.getOrder();

    int x = partyMargin + partyWidth + co * (boxWidth + boxMargin);
    int y = po * (boxHeight + boxMargin);
    return new Rectangle(x, y, boxWidth, boxHeight);
  }

  private Image getLogo(Party party)
  {
    Image logo = null;
    try
    {
      String logoURLString = party.getLogo();
      if (logoURLString != null)
      {
        logo = (Image)logoCache.get(logoURLString);
        if (logo == null)
        {
          URL logoURL = new URL(logoURLString);
          ImageIcon image = new ImageIcon(logoURL);
          logo = image.getImage();
          logoCache.put(logoURLString, logo);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return logo;
  }

  private Image getPhoto(Councillor councillor)
  {
    Image photo = null;
    try
    {
      String photoURLString = councillor.getImageURL();
      if (photoURLString != null)
      {
        photo = (Image)photoCache.get(photoURLString);
        if (photo == null)
        {
          URL photoURL = new URL(photoURLString);
          ImageIcon image = new ImageIcon(photoURL);
          photo = image.getImage();
          photoCache.put(photoURLString, photo);
        }
      }
    }
    catch (Exception ex)
    {
    }
    return photo;
  }

  class Animator extends Thread
  {
    @Override
    public void run()
    {
      try
      {
        paintedCouncillors = 0;
        while (paintedCouncillors <= electedCouncillors.size())
        {
          repaint();
          sleep(200);
          paintedCouncillors++;
        }
      }
      catch (Exception ex)
      {
      }
      finally
      {
        animator = null;
        newData = false;
      }
    }
  }
}
