package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

public class StartWindow extends JFrame {
    private final String url = "jdbc:postgresql://localhost:5432/postgres";
    private final String user = "postgres";
    private final String pass = "1234567";

    public StartWindow() {
        try {
            Class.forName("org.postgresql.Driver");// загрузить драйвер
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        setTitle("Администратор");
        setSize(600, 800);

        JButton costButton = new JButton("Информация о заказах");
        costButton.addActionListener(new costTableListener());
        JButton popularButton = new JButton("Рейтинг товаров");
        popularButton.addActionListener(new popularTableListener());
        JButton insertButton = new JButton("Добавить товар");
        insertButton.addActionListener(new insertProductListener());
        JButton updateButton = new JButton("Обновить цену товара");
        updateButton.addActionListener(new updatePriceListener());
        JButton dayTableButton = new JButton("Анализ по датам");
        dayTableButton.addActionListener(new dayTableListener());
        JButton catalogButton = new JButton("Каталог");
        catalogButton.addActionListener(new catalogListener());

        JPanel panel = new JPanel();
        panel.setBackground(new Color(127, 190, 231, 255));
        panel.add(costButton);
        panel.add(popularButton);
        panel.add(insertButton);
        panel.add(updateButton);
        panel.add(dayTableButton);
        panel.add(catalogButton);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.repaint();
        panel.validate();
        Container c = getContentPane();
        c.add(panel);
        setVisible(true);
    }

    //таблица заказ стоимость сотрудник клиент дата
    class costTableListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                Connection conn = DriverManager.getConnection(url, user, pass);//создать соединение
                Statement statement = conn.createStatement();//Создание запроса
                //получение результата запроса
                ResultSet result = statement.executeQuery("SELECT p.Номер_заказа, Выручка,  Сотрудник.ФИО, Клиент.ФИО, p.Дата_заказа\n" +
                        "FROM (SELECT Заказ.Номер_заказа,  SUM(Продажа.Количество_проданного_товара * Товар.Цена) AS Выручка, Заказ.Номер_сотрудника, Заказ.Номер_клиента, Заказ.Дата_заказа\n" +
                        "From Заказ\n" +
                        "INNER JOIN Продажа ON Заказ.Номер_заказа = Продажа.Номер_заказа\n" +
                        "INNER JOIN Товар ON Товар.Номер_товара = Продажа.Номер_товара\n" +
                        "GROUP BY Заказ.Номер_заказа\n" +
                        ") AS p\n" +
                        "INNER JOIN Сотрудник ON p.Номер_сотрудника = Сотрудник.Номер_сотрудника\n" +
                        "LEFT JOIN Клиент ON p.Номер_клиента = Клиент.Номер_клиента\n" + "ORDER BY p.Номер_заказа");
                String[] columnNames = new String[]{"Номер заказа", "Стоимость заказа", "Сотрудник", "Клиент", "Дата заказа"};
                Vector<String[]> data = new Vector<>();
                while (result.next()) {
                    String[] a = new String[5];
                    for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {
                        a[j - 1] = result.getString(j);
                    }
                    data.add(a);
                }
                String[][] dataForTable = new String[data.size()][5];
                for (int i = 0; i < data.size(); i++) {
                    dataForTable[i][0] = data.get(i)[0];
                    dataForTable[i][1] = data.get(i)[1];
                    dataForTable[i][2] = data.get(i)[2];
                    dataForTable[i][3] = data.get(i)[3];
                    dataForTable[i][4] = data.get(i)[4];
                }
                result.close();// Закрытие набора данных
                statement.close();// Закрытие базы данных
                conn.close();// Отключение от базы данных
                new Table(columnNames, dataForTable);//создать результирующую таблицу
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //таблица рейтинг товаров
    class popularTableListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                Connection conn = DriverManager.getConnection(url, user, pass);
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery("SELECT Товар.Название, SUM(Продажа.Количество_проданного_товара) * 100 / (SELECT SUM(Продажа.Количество_проданного_товара) FROM Продажа) AS Процент_проданного_товара\n" +
                        "From Продажа\n" +
                        "INNER JOIN Товар ON Товар.Номер_товара = Продажа.Номер_товара\n" +
                        "GROUP BY Продажа.Номер_товара, Товар.Название\n" +
                        "ORDER BY Процент_проданного_товара DESC");
                String[] columnNames = new String[]{"Название товара", "Процент проданного товара"};
                Vector<String[]> data = new Vector<>();
                while (result.next()) {
                    String[] a = new String[2];
                    for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {
                        a[j - 1] = result.getString(j);
                    }
                    data.add(a);
                }
                String[][] dataForTable = new String[data.size()][2];
                for (int i = 0; i < data.size(); i++) {
                    dataForTable[i][0] = data.get(i)[0];
                    dataForTable[i][1] = data.get(i)[1];
                }
                result.close();
                statement.close();
                conn.close();
                new Table(columnNames, dataForTable);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //Добавить товар
    class insertProductListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new InsertProduct();
        }
    }

    //Обновить цену товара
    class updatePriceListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new UpdatePrice();
        }
    }

    //Информация по дням
    class dayTableListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                Connection conn = DriverManager.getConnection(url, user, pass);
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery("SELECT Заказ.Дата_заказа AS Дата,  SUM(Продажа.Количество_проданного_товара * Товар.Цена) AS Выручка, SUM(Продажа.Количество_проданного_товара) AS Количество_проданного_товара\n" +
                        "From Заказ\n" +
                        "INNER JOIN Продажа ON Заказ.Номер_заказа = Продажа.Номер_заказа\n" +
                        "INNER JOIN Товар ON Товар.Номер_товара = Продажа.Номер_товара\n" +
                        "GROUP BY Заказ.Дата_заказа\n" + "ORDER BY Заказ.Дата_заказа");
                String[] columnNames = new String[]{"Дата", "Выручка", "Количество проданного товара"};
                Vector<String[]> data = new Vector<>();
                while (result.next()) {
                    String[] a = new String[3];
                    for (int j = 1; j <= result.getMetaData().getColumnCount(); j++) {
                        a[j - 1] = result.getString(j);
                    }
                    data.add(a);
                }
                String[][] dataForTable = new String[data.size()][3];
                for (int i = 0; i < data.size(); i++) {
                    dataForTable[i][0] = data.get(i)[0];
                    dataForTable[i][1] = data.get(i)[1];
                    dataForTable[i][2] = data.get(i)[2];
                }
                result.close();
                statement.close();
                conn.close();
                new Table(columnNames, dataForTable);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    //Каталог -1 чтобы не высвечивались бонусы
    class catalogListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            new ClientWindow(-1);
        }
    }
}