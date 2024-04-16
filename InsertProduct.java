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

//чтобы администратор добавлял товар
public class InsertProduct extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";
    private JTextField nameField, costField;
    public InsertProduct(){
        setTitle("Добавить товар");
        nameField = new JTextField("Название товара", 15);
        costField = new JTextField("Стоимость товара", 15);
        JButton costButton = new JButton("ok");
        costButton.addActionListener(new insertListener());

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
    class insertListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                String name = nameField.getText();
                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                Statement statement = conn.createStatement();
                //проверка не существует ли такой товар
                ResultSet result = statement.executeQuery("SELECT Номер_товара\n" +
                        "FROM Товар\n" +
                        "WHERE Название = '" + name + "'");
                //если такого товара не существует
                if (!result.next()) {
                    Scanner s = new Scanner(costField.getText());
                    //окно с ошибкой что ввели неправильное значение
                    if (!s.hasNextDouble()) {
                        JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nВведеные данные не соответствуют типу", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        double cost = s.nextDouble();
                        Statement insertStatement = conn.createStatement();
                        insertStatement.execute("INSERT INTO Товар(Название, Цена)\n" +
                                "Values ('" + name + "', " + cost + ")");
                        insertStatement.close();
                        JOptionPane.showMessageDialog(new JFrame(), "Товар\n" + name + " стоимостью " + cost + "\nуспешно добавлен!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                //окно с ошибкой
                else{
                    JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nТакой товар уже есть в каталоге", "Error", JOptionPane.ERROR_MESSAGE);
                }
                result.close();
                statement.close();
                conn.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
