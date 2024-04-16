package view;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

//чтобы сделать таблицу
public class Table extends JFrame {
    public Table (String[] column, String[][] data){
        setSize(600, 800);
        JTable table = new JTable(data, column){
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent){
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };
        table.setOpaque(true);
        Color c = new Color(8, 39, 58);
        table.getTableHeader().setForeground(c);
        table.setForeground(c);
        Color fon = new Color(127, 190, 231, 255);
        table.setBackground(fon);
        table.getTableHeader().setBackground(fon);
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                int y = getSize().height;
                table.setRowHeight(y / data.length);
            }
        });
        setVisible(true);
    }
}
