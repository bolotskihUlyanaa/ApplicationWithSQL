package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class UpdatePrice extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";
    private JTextField nameField, costField;
    public UpdatePrice(){
        setTitle("Обновить цену товара");
        nameField = new JTextField("Название товара", 15);
        costField = new JTextField("Стоимость товара", 15);
        JButton costButton = new JButton("ok");
        costButton.addActionListener(new updateListener());

        JPanel contents = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contents.setBackground(new Color(127, 190, 231, 255));
        contents.add(nameField);
        contents.add(costField);
        contents.add(costButton);
        setContentPane(contents);
        setSize(400, 100);
        setVisible(true);
    }

    //Добавить товар
    class updateListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                String name = nameField.getText();
                Scanner s = new Scanner(costField.getText());
                //если ввели не double
                if (!s.hasNextDouble()){
                    JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nВведеные данные не соответствуют типу", "Error", JOptionPane.ERROR_MESSAGE);
                }
                else{
                    double cost = s.nextDouble();
                    Class.forName("org.postgresql.Driver");
                    Connection conn = DriverManager.getConnection(url, user, pass);
                    Statement statement = conn.createStatement();
                    //проверка существует ли такой товар
                    ResultSet result = statement.executeQuery("SELECT Номер_товара\n" +
                            "FROM Товар\n" +
                            "WHERE Название = '" + name + "'");
                    //если такого товара не существует
                    if (!result.next()) {
                        JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nТакого товара не существует", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        Statement updateStatement = conn.createStatement();
                        updateStatement.execute("UPDATE Товар\n" +
                                "SET Цена = " + cost + "\n" +
                                "WHERE Название = '" + name + "'");
                        JOptionPane.showMessageDialog(new JFrame(), "Стоимость товара \n" + name + "\nуспешно обновлена!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        updateStatement.close();
                    }
                    result.close();
                    statement.close();
                    conn.close();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
