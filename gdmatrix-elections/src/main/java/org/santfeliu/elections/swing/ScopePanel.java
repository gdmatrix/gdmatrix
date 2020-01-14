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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.santfeliu.elections.Board;
import org.santfeliu.elections.Results;

/**
 *
 * @author unknown
 */
public class ScopePanel extends JPanel
{
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private JLabel headerLabel = new JLabel();
  private JLabel electorsLabel = new JLabel();
  private JLabel electorsValueLabel = new JLabel();
  private JLabel emittedVotesLabel = new JLabel();
  private JLabel emittedVotesValueLabel = new JLabel();
  private JLabel blankVotesLabel = new JLabel();
  private JLabel blankVotesValueLabel = new JLabel();
  private JLabel invalidVotesLabel = new JLabel();
  private JLabel invalidVotesValueLabel = new JLabel();
  private JLabel validVotesLabel = new JLabel();
  private JLabel validVotesValueLabel = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private JLabel participationLabel = new JLabel();
  private JLabel abstentionLabel = new JLabel();
  private JLabel participationValueLabel = new JLabel();
  private JLabel abstentionValueLabel = new JLabel();
  private ImageIcon headerIcon;
  private JLabel headerImageLabel = new JLabel();

  public ScopePanel()
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
    this.setLayout(gridBagLayout1);
    this.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    headerLabel.setText(" ");
    headerLabel.setOpaque(true);
    headerLabel.setBackground(new Color(132, 255, 255));
    electorsLabel.setText("Electors:");
    electorsValueLabel.setText("0");
    emittedVotesLabel.setText("Vots emesos:");
    emittedVotesValueLabel.setText("0");
    blankVotesLabel.setText("Vots en blanc:");
    blankVotesValueLabel.setText("0");
    invalidVotesLabel.setText("Vots nuls:");
    invalidVotesValueLabel.setText("0");
    validVotesLabel.setText("Vots vàlids:");
    validVotesValueLabel.setText("0");
    participationLabel.setText("Participació:");
    participationLabel.setToolTipText("null");
    abstentionLabel.setText("Abstenció:");
    participationValueLabel.setText("0 %");
    abstentionValueLabel.setText("0 %");
    this.add(headerLabel,
             new GridBagConstraints(0, 0, 6, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 6, 0), 0, 0));
    this.add(electorsLabel,
             new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 4), 0, 0));
    this.add(electorsValueLabel,
             new GridBagConstraints(1, 1, 1, 1, 0.1, 1.0,GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(emittedVotesLabel,
             new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 4), 0, 0));
    this.add(emittedVotesValueLabel,
             new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(blankVotesLabel,
             new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 4), 0, 0));
    this.add(blankVotesValueLabel,
             new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0,GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(invalidVotesLabel,
             new GridBagConstraints(0, 4, 1, 1, 0.0, 1.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 4), 0, 0));
    this.add(invalidVotesValueLabel,
             new GridBagConstraints(1, 4, 1, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(validVotesLabel,
             new GridBagConstraints(2, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 20, 0, 4), 0, 0));
    this.add(validVotesValueLabel,
             new GridBagConstraints(3, 2, 1, 1, 0.1, 1.0,GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(jLabel2,
             new GridBagConstraints(4, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 20, 0, 4), 0, 0));
    this.add(jLabel3,
             new GridBagConstraints(5, 1, 1, 1, 0.8, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(participationLabel,
             new GridBagConstraints(2, 3, 1, 1, 0.0, 1.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 20, 0, 0), 0, 0));
    this.add(abstentionLabel,
             new GridBagConstraints(2, 4, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                                    new Insets(0, 20, 0, 0), 0, 0));
    this.add(participationValueLabel,
             new GridBagConstraints(3, 3, 1, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(abstentionValueLabel,
             new GridBagConstraints(3, 4, 1, 1, 0.0, 1.0,GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
    this.add(headerImageLabel,
             new GridBagConstraints(4, 1, 2, 4, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                    new Insets(0, 0, 0, 0), 0, 0));
  }

  public void setHeader(String headerURL)
  {
    try
    {
      if (headerURL == null) return;
      headerIcon = new ImageIcon(new URL(headerURL));
      headerImageLabel.setIcon(headerIcon);
    }
    catch (Exception e)
    {
      System.out.println("Header not found !");
    }
  }

  public void updateData(Results results)
  {
    DecimalFormat df1 = new DecimalFormat("#,###,##0");
    DecimalFormat df2= new DecimalFormat("#,###,##0.00");

    String header = null;
    String district = results.getCurrentDistrict();
    if (district == null) header = "RESULTATS GLOBALS";
    else
    {
      header = "DISTRICTE " + district;
      String section = results.getCurrentSection();
      if (section != null)
      {
        header += " SECCIÓ " + section;
        String boardName = results.getCurrentBoardName();
        if (boardName != null)
        {
          header += " MESA " + boardName;
          Board board = results.getBoard(district, section, boardName);
          if (board.getDescription() != null)
          {
            header += ": " + board.getDescription();
          }
        }
      }
      else
      {
        header += ": " + results.getDistrict(district);
      }
    }
    headerLabel.setText(header);
    electorsValueLabel.setText(df1.format(results.getElectors()));
    emittedVotesValueLabel.setText(df1.format(results.getVotes()));
    blankVotesValueLabel.setText(df1.format(results.getBlankVotes()));
    invalidVotesValueLabel.setText(df1.format(results.getInvalidVotes()));
    validVotesValueLabel.setText(df1.format(results.getValidVotes()));
    double participation = results.getParticipation();
    if (participation != -1)
    {
      participationValueLabel.setText(df2.format(participation) + " %");
    }
    else
    {
      participationValueLabel.setText("");
    }
    double abstention = results.getAbstention();
    if (abstention != -1)
    {
      abstentionValueLabel.setText(df2.format(abstention) + " %");
    }
    else
    {
      abstentionValueLabel.setText("");
    }
  }
}
