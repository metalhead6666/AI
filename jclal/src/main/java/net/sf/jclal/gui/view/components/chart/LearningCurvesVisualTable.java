/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jclal.gui.view.components.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class LearningCurvesVisualTable extends JDialog {

    private static final long serialVersionUID = -26034215746849616L;

    private JScrollPane scroll;
    private JTable table;
    private ArrayList<String> queryNames;
    private Set<Integer> set;
    private JButton button;
    private Object[][] data;

    public LearningCurvesVisualTable(final ExternalBasicChart chart) {

        scroll = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        queryNames = chart.getQueryNames();
        set = chart.getSet();
        data = chart.getData();
        final LearningCurvesTableModel tableModel = new LearningCurvesTableModel(chart.getData(), chart.getQueryNames());

        table.setModel(tableModel);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.getSelectedRow();
                int column = table.getSelectedColumn();

                if (column == 0) {
                    int indice = index(row);
                    if (set.add(indice)) {

                    } else {

                        set.remove(indice);
                    }
                }
               
                chart.jComboBoxItemStateChanged();
            }

        });
   
        table.setDefaultRenderer(java.awt.Color.class, new ColorRenderer(true));
        table.setDefaultEditor(java.awt.Color.class, new ColorEditor(table, chart));

        scroll.setViewportView(table);

        button = new JButton("Ok");

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int rows = chart.getQueryNames().size();

                for (int r = 0; r < rows; r++) {

                    chart.getControlCurveColor().put((String) tableModel.getValueAt(r, 1), (Color) tableModel.getValueAt(r, 2));

                    chart.jComboBoxItemStateChanged();

                    LearningCurvesVisualTable.this.dispose();
                }

            }
        });

        setBounds(400, 200, 500, 200);
        setTitle("Learning curves");
        setAlwaysOnTop(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setVisible(true);
        setResizable(true);
        setModal(true);
        setLayout(new BorderLayout());

        add(scroll, BorderLayout.CENTER);
        add(button, BorderLayout.SOUTH);

        pack();

    }

    private int index(int row) {
        for (int i = 0; i < queryNames.size(); i++) {

            if (data[row][1].equals(queryNames.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
