package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ZerosPage extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";
    private JTextField nameField;
    private JPasswordField passField;
    private JCheckBox showPassword;

    public ZerosPage(){
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Авторизация");
        nameField = new JTextField("Логин", 15);
        passField = new JPasswordField("Пароль", 15);
        passField.setEchoChar('*');
        showPassword = new JCheckBox("Показать пароль");
        showPassword.addActionListener(new checkListener());
        JButton enterButton = new JButton("ok");
        enterButton.addActionListener(new enterListener());
        JButton nextButton = new JButton("Каталог");
        nextButton.addActionListener(new nextListener());
        JButton registrationButton = new JButton("Зарегистрироваться");
        registrationButton.addActionListener(new registrationListener());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(127, 190, 231, 255));
        panel.add(nameField);
        panel.add(passField);
        panel.add(showPassword);
        panel.add(enterButton);
        panel.add(nextButton);
        panel.add(registrationButton);
        setContentPane(panel);
        setSize(400, 400);
        setVisible(true);
    }

    //войти в кабинет: админ пользователь или сотрудник
    class enterListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            String name = nameField.getText();
            String password = passField.getText();
            //администратор
            if(name.equals("админ") && password.equals("админ")){
                new StartWindow();
            }
            else {
                //проверка авторизованный ли пользователь
                double count = enter(name, password);
                if (count >= 0) new ClientWindow(count);
                //если сотрудник
                else {
                    int numberWorker = enterWorker(name, password);
                    if (numberWorker >= 0) new WorcerWindow(name, numberWorker);
                    else {
                        JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\nНеверные данные", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        }
    }

    //если сотрудник, то возвращает номер сотрудника иначе -1
    private int enterWorker(String name, String password){
        int num = -1;
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT Пароль, Номер_сотрудника\n" +
                    "FROM Сотрудник\n" +
                    "WHERE ФИО = '" + name + "'");
            if (result.next() && password.equals(result.getString(1))){
                num = Integer.parseInt(result.getString(2));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return num;
    }

    //возвращает число бонусов если зарегистрированный пользователь, иначе -1
    private double enter(String name, String password){
        double bonus = -1;
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT Пароль\n" +
                    "FROM Клиент\n" +
                    "WHERE Номер_телефона = '" + name + "'");
            if (result.next() && password.equals(result.getString(1))) {
                Statement bonusStatement = conn.createStatement();
                ResultSet bonusResult = bonusStatement.executeQuery("SELECT Количество_бонусов\n" +
                        "FROM Клиент\n" +
                        "WHERE Номер_телефона = '" + name + "'");
                if (bonusResult.next()) {
                    bonus = Double.parseDouble(bonusResult.getString(1));
                }
                bonusResult.close();
                bonusStatement.close();
            }
            result.close();
            statement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return bonus;
    }

    //открыть страницу регистрации
    class registrationListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new Registration();
        }
    }

    //открыть страницу для обычного пользователя, -1 -- нет бонусов
    class nextListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new ClientWindow(-1);//тк у обычного пользователя нет бонусов
        }
    }

    //видно или не видно пароль
    class checkListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (showPassword.isSelected()) {
                passField.setEchoChar((char) 0);
            } else {
                passField.setEchoChar('*');
            }
        }
    }
}
