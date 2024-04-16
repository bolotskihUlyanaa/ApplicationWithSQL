package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class TakeOrder extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";
    private Vector<String[]> data;
    private ArrayList<int[]> order;
    private final String date;
    private final int numberWorker;
    private int numberClient, numberOrder;
    private JTextField clientField, commentField;
    private JTextField[] productField;
    private JCheckBox bonCheck;
    //сотрудник дата вносятся автоматически
    //номер заказа назначается в бд
    public TakeOrder(String nameWorker, int numberWorker){
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        this.numberWorker = numberWorker;
        date = getDateOrder();
        setTitle("Создать заказ");
        setSize(600, 800);

        //создаем панель и добавляем туда все элементы
        catalog();//чтобы узнать какие товары
        int size = data.size() + 5;//все товары + дата, сотрудник, клиент, бонусы, комментарий
        GridLayout layout = new GridLayout(size, 2);
        JPanel panel = new JPanel();
        panel.setBackground(new Color(127, 190, 231, 255));
        panel.setLayout(layout);
        panel.add(new JLabel("Дата"));
        panel.add(new JLabel(date));
        panel.add(new JLabel("Сотрудник"));
        panel.add(new JLabel(nameWorker));
        panel.add(new JLabel("Клиент"));
        clientField = new JTextField("Номер телефона");
        panel.add(clientField);
        panel.add(new JLabel("Списать бонусы"));
        bonCheck = new JCheckBox();
        panel.add(bonCheck);
        productField = new JTextField[data.size()];
        for(int i = 0; i < data.size(); i++){
            panel.add(new JLabel(data.get(i)[0]));
            productField[i] = new JTextField("0");
            panel.add(productField[i]);
        }
        JButton createButton = new JButton("Создать заказ");
        createButton.addActionListener(new createListener());
        panel.add(createButton);
        commentField = new JTextField("Комментарий к заказу");
        panel.add(commentField);

        panel.repaint();
        panel.validate();
        Container c = getContentPane();
        c.add(panel);
        setVisible(true);
    }

    //при нажатии на кнопку создаем заказ
    class createListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                //проверяем существует ли такой клиент и правильно ли ввели номер checkClient()
                //Проверить введенные данные (количество товара) checkData()
                //Создать заказ createOrder()
                if(checkClient() && checkData() && createOrder() > 0) {
                    createSales();//Создать продажи
                    //payment() проводит оплату
                    //если комментарий есть, то добавляем его в окно
                    if (!commentField.getText().equals("Комментарий к заказу")) successWindow("Заказ № " + numberOrder + " успешно создан!\n К оплате " + payment() + " рублей.\nКомментарий к заказу: \n" + commentField.getText());
                    else successWindow("Заказ № " + numberOrder + " успешно создан!\n К оплате " + payment() + " рублей.\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //чтобы делать запрос без ResultSet
    private void requestWithoutResult(String request){
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            statement.execute(request);
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //создать продажу
    private void createSales() {
        for (int[] i : order) {
            if (i[1] > 0) requestWithoutResult("INSERT INTO Продажа(Номер_заказа, Номер_товара, Количество_проданного_товара)\n" +
                        "VALUES(" + numberOrder + ", " + i[0] + ", " + i[1] + ")");
        }
    }

    //обновить количество бонусов
    private void refreshBonus(double countBonus){
        requestWithoutResult("UPDATE Клиент\n" +
                "SET Количество_бонусов = " + countBonus + "\n" +
                "WHERE Номер_клиента = " + numberClient);
    }

    //оплата и обновление бонусов
    private double payment(){
        double countPay = getCostOrder();
        //если клиент указан
        if(numberClient > 0) {
            double countAddBonus = countPay * 0.1;//сколько бонусов начислить
            double countBonus = getBonus();
            //если списать бонусы
            if (bonCheck.isSelected()) {
                //если стоимость заказа больше чем количество бонусов
                if (countPay > countBonus) {
                    countPay -= countBonus;
                    countBonus = 0;
                }
                //если стоимость заказа меньше количества бонусов
                else {
                    countPay = 0;
                    countBonus -= countPay;
                }
            }
                countAddBonus += countBonus;
            //начислить бонусы countAddBonus
            refreshBonus(countAddBonus);
        }
        return countPay;
    }

    //узнать количестово бонусов у клиента
    private double getBonus(){
        double countBonus = 0;
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT Количество_бонусов\n" +
                    "FROM Клиент\n" +
                    "WHERE Номер_клиента = " + numberClient);
            if (result.next()) {
                countBonus = Double.parseDouble(result.getString(1));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countBonus;
    }

    //узнать стоимость заказа
    private double getCostOrder(){
        double costOrder = 0;
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT SUM(Продажа.Количество_проданного_товара * Товар.Цена)\n" +
                    "From Заказ\n" +
                    "INNER JOIN Продажа ON Заказ.Номер_заказа = Продажа.Номер_заказа\n" +
                    "INNER JOIN Товар ON Товар.Номер_товара = Продажа.Номер_товара\n" +
                    "WHERE Заказ.Номер_заказа = " + numberOrder + "\n" +
                    "GROUP BY Заказ.Номер_заказа");
            if (result.next()) {
                costOrder = Double.parseDouble(result.getString(1));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return costOrder;
    }

    //создать заказ
    private int createOrder() {
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result;
            if (numberClient == -1) {
                result = statement.executeQuery("INSERT INTO Заказ(Номер_сотрудника, Дата_заказа)\n" +
                        "VALUES(" + numberWorker + ", '" + date + " ')\n" +
                "RETURNING Номер_заказа");
            } else {
                result = statement.executeQuery("INSERT INTO Заказ(Номер_сотрудника, Номер_клиента, Дата_заказа)\n" +
                        "VALUES(" + numberWorker + ", " + numberClient + ", '" + date + "')\n" +
                "RETURNING Номер_заказа");
            }
            if (result.next()) {
                numberOrder = Integer.parseInt(result.getString(1));
            } else {
                numberOrder = -1;
                errorWindow("При создании запроса");
            }
            result.close();
            statement.close();
            conn.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return numberOrder;
    }

    //проверить правильность введенных данных
    private boolean checkData(){
        int countProduck = 0;//считаем проданные товары
        order = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            Scanner scanner = new Scanner(productField[i].getText());
            if(!scanner.hasNext() || scanner.hasNextInt()) {
                int[] sale = new int[2];
                sale[0] = Integer.parseInt(data.get(i)[1]);//код товара
                if(scanner.hasNext()) sale[1] = scanner.nextInt();//количество
                countProduck += sale[1];
                order.add(sale);
            }
            else{
                errorWindow("Неверный тип");
                return false;
            }
        }
        if (countProduck == 0) return false;//чтобы не создавать пустой заказ
        return true;
    }

    //проверяем существует ли такой клиент и правильно ли ввели номер
    private boolean checkClient(){
        boolean flag = false;//правильно ли ввели данные
        try {
            numberClient = -1;
            String telephonStandart = "8\\d{10}|8(\\d{3})\\d{7}|8(\\d{3})\\d{3}-\\d{2}-\\d{2}|[+]7\\d{10}|[+]7\\d{6}-\\d{2}-\\d{2}|[+]7(\\d{3})\\d{3}-\\d{2}-\\d{2}";
            String telephonValue = clientField.getText();
            Scanner scanner = new Scanner(telephonValue);
            if(!scanner.hasNext()) return true;//если телефон не введен
            if (Pattern.matches(telephonStandart, telephonValue)) {
                Connection conn = DriverManager.getConnection(url, user, pass);
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery("SELECT Номер_клиента\n" +
                        "FROM Клиент\n" +
                        "WHERE Номер_телефона = '" + telephonValue + "'");
                if (result.next()) {
                    numberClient = Integer.parseInt(result.getString(1));
                    flag = true;
                } else {
                    errorWindow("Клиента с таким номером телефона не существует");
                }
                result.close();
                statement.close();
                conn.close();
            } else {
                if (!telephonValue.equals("Номер телефона")) errorWindow("Ошибка в номере телефона");
                else flag = true;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return flag;
    }

    //окно ошибки
    public void errorWindow(String message){
        JOptionPane.showMessageDialog(new JFrame(), "Ошибка!\n" + message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //окно успеха
    public void successWindow(String message){
        JOptionPane.showMessageDialog(new JFrame(), message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    //узнать какие товары есть
    private void catalog(){
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT Название, Номер_товара\n" +
                    "FROM Товар");
            data = new Vector<>();
            while (result.next()) {
                String[] pair = new String[2];//пара: название товара и код
                for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {
                    pair[j - 1] = result.getString(j);
                }
                data.add(pair);
            }
            result.close();
            statement.close();
            conn.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //получить дату в виде "2024/04/04"
    private String getDateOrder(){
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(currentDate);
    }
}
