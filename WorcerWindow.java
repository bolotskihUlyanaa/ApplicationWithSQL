package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WorcerWindow extends JFrame {
    private String nameWorcer;
    private int numberWorcer;
    public WorcerWindow(String nameWorcer, int numberWorcer){
        this.nameWorcer = nameWorcer;
        this.numberWorcer = numberWorcer;
        setTitle("Сотрудник");
        setSize(600, 800);

        JButton catalogButton = new JButton("Каталог");
        catalogButton.addActionListener(new catalogListener());
        JButton createButton = new JButton("Создать заказ");
        createButton.addActionListener(new createListener());

        JPanel panel = new JPanel();
        panel.setBackground(new Color(127, 190, 231, 255));
        setSize(600, 800);
        panel.add(createButton);
        panel.add(catalogButton);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.repaint();
        panel.validate();
        Container c = getContentPane();
        c.add(panel);
        setVisible(true);
    }

    //открыть каталог
    class catalogListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new ClientWindow(-1);
        }
    }

    //открыть окно чтобы создать заказ
    class createListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new TakeOrder(nameWorcer, numberWorcer);
        }
    }
}
