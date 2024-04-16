package view;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

public class ClientWindow extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";
    private Vector<String[]> data;
    public ClientWindow(double bonusCount){
        setTitle("Каталог");
        setSize(600, 800);
        catalog();

        int size = data.size();
        if(bonusCount >= 0){
            size++;
        }
        GridLayout layout = new GridLayout(size, 2);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(127, 190, 231, 255));
        panel.setLayout(layout);
        //если клиент вошел в личный кабинет
        if(bonusCount >= 0){
            panel.add(new JLabel(new ImageIcon("res/бонус.png")));
            panel.add(new JLabel("Приветствуем!\n У вас " + bonusCount + " бонусов!"));
        }
        for(String[] i:data){
            panel.add(new JLabel(new ImageIcon("res/этлон/" + i[0] + ".png")));
            panel.add(new JLabel(i[0] + " " + i[1] + " p."));
        }
        panel.repaint();
        panel.validate();
        Container c = getContentPane();
        c.add(panel);
        setVisible(true);
    }

    //узнать товары и их цену
    private void catalog(){
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT Название, Цена\n" +
                    "FROM Товар");
            data = new Vector<>();
            while (result.next()) {
                String[] a = new String[2];
                for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {
                    a[j - 1] = result.getString(j);
                }
                data.add(a);
            }
            result.close();
            statement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
