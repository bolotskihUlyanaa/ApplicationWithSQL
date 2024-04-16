package view;

//Для входа в личный кабинет
//клиент логин:89111234567 пароль:123
//сотрудник логин:тест пароль:123


public class Main {
    public static void main(String[] args) {
        try {
            new ZerosPage();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}
