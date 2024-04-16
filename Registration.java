package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;

public class Registration extends JFrame {
    private JTextField nameField, telephonField, passField, passCheckField;
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";

    public Registration(){
        setTitle("Регистрация");
        nameField = new JTextField("ФИО", 15);
        telephonField = new JTextField("Телефон", 15);
        passField = new JTextField("Пароль", 15);
        passCheckField = new JTextField("Повторите пароль", 15);
        JButton costButton = new JButton("ok");
        costButton.addActionListener(new insertListener());

        JPanel contents = new JPanel(new FlowLayout(FlowLayout.CENTER));
        contents.setBackground(new Color(127, 190, 231, 255));
        contents.add(nameField);
        contents.add(telephonField);
        contents.add(passField);
        contents.add(passCheckField);
        contents.add(costButton);
        setContentPane(contents);
        setSize(400, 400);
        setVisible(true);
    }

    //Зарегистирировать пользователя
    class insertListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                String name = nameField.getText();
                String telephon = telephonField.getText();
                String password = passField.getText();
                String passwordCheck = passCheckField.getText();

                Class.forName("org.postgresql.Driver");
                Connection conn = DriverManager.getConnection(url, user, pass);
                Statement statement = conn.createStatement();
                //проверка не существует ли такого клиента
                ResultSet result = statement.executeQuery("SELECT Пароль\n" +
                        "FROM Клиент\n" +
                        "WHERE Номер_телефона = '" + telephon + "'");
                //если результата нет, то значит такого клиента не существует
                if (!result.next()) {
                    String standart = "8\\d{10}|8(\\d{3})\\d{7}|8(\\d{3})\\d{3}-\\d{2}-\\d{2}|[+]7\\d{10}|[+]7\\d{6}-\\d{2}-\\d{2}|[+]7(\\d{3})\\d{3}-\\d{2}-\\d{2}";
                    // проверка совпадают ли введенные пароли или соответствует ли телефон шаблону
                    if (password.equals(passwordCheck) && Pattern.matches(standart, telephon)) {
                        Statement insertStatement = conn.createStatement();
                        insertStatement.execute("INSERT INTO Клиент(ФИО, Номер_телефона, Пароль, Количество_бонусов)\n" +
                                "VALUES ('" + name + "', '" + telephon + "', '" + password + "', 0)");
                        insertStatement.close();
                        JOptionPane.showMessageDialog(new JFrame(), "Поздравляю!\nВы зарегистрированы!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nВведеные данные не соответствуют типу", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nТакой клиент уже существует", "Error", JOptionPane.ERROR_MESSAGE);
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
