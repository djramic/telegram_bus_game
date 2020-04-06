import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendGame;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import comands.JoinGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ServiceConfigurationError;

public class MainClass {
    private static TelegramBot bot;
    private static Long chatId;
    private static int adminId = 0;
    private static String adminName = "";
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<String> numbers = new ArrayList<>();
    private static ArrayList<String> tableNumbers = new ArrayList<>();
    private static boolean inGame = false;
    private static int currentNumbIndex;
    private static ArrayList<Integer> usersDone = new ArrayList<>();
    private static ArrayList<String> playersName = new ArrayList<>();

    public static void main(String args[]) {

        bot = new TelegramBot("931721120:AAHRhop-2STEq5cPh2dAWe4OERFRMbbT7hk");

        bot.setUpdatesListener(updates -> {
            for(Update update : updates){
                System.out.println("Usao sam u update");
                if(update.message()!= null) {
                    if(update.message().text()!= null) {
                        System.out.println("Primio sam poruku: " + update.message().toString());
                        executeMessage(update);
                    }
                }
                if(update.callbackQuery()!= null){
                    System.out.println("Usao sam u callback Query");
                    executeCallbackQuery(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });


    }

    private static void executeCallbackQuery(Update update) {
        if(!inGame){
            if(!playersName.contains(update.callbackQuery().from().firstName())) {
                User user = new User(update.callbackQuery().from().id());
                user.setName(update.callbackQuery().from().firstName());
                playersName.add(update.callbackQuery().from().firstName());
                users.add(user);
                SendResponse response = bot.execute(new SendMessage(chatId, "Korisnik " +
                        update.callbackQuery().from().firstName() + " se pridruzio igri\nProveri ko je sve u igri sa komandom /igraci"));
                System.out.println("Korisnik koji je pridruzio igri:" + update.callbackQuery().message().chat().id());
            } else{
                SendResponse response = bot.execute(new SendMessage(chatId,
                        update.callbackQuery().from().firstName() + " vec si se pridruzio.\nProveri ko je sve u igri sa komandom /igraci"));
            }
        }else {
            SendResponse response = bot.execute(new SendMessage(update.callbackQuery().message().chat().id(), "Igra je vec u toku...."));
        }
    }

    private static void executeMessage(Update update) {
        if (update.message().text().equals("/admin" ) && users.size() != 0) {
            System.out.println("-------PROSAO SAM USLOV ADMIN-------");
            SendResponse response = bot.execute(new SendMessage(update.message().chat().id(), "Admin je  " + adminName));

        }
        if(!inGame) {
            if (update.message().text().equals("/napravi_igru") || update.message().text().equals("/napravi_igru@mjesec_gejmer_bot")) {
                clearData();
                User user = new User(update.message().from().id());
                user.setName(update.message().from().firstName());
                playersName.add(update.message().from().firstName());
                users.add(user);
                adminId = user.getId();
                adminName = user.getName();
                SendResponse response = bot.execute(new SendGame(update.message().chat().id(), "autobusi"));
                chatId = update.message().chat().id();
            }
            if (update.message().text().equals("/igraci") || update.message().text().equals("/igraci@mjesec_gejmer_bot")) {
                System.out.println("-------PROSAO SAM USLOV IGRACI-------");
                System.out.println("Izlistavam igrace i saljem: " + users.toString());
                SendResponse response = bot.execute(new SendMessage(update.message().chat().id(), "Prijavljeni igraci " + playersName.toString()));
            }

            if (update.message().text().equals("/pokreni")  || update.message().text().equals("/pokreni@mjesec_gejmer_bot") ) {
                if( users.size() != 0 && update.message().from().id() == adminId) {
                    System.out.println("-------PROSAO SAM USLOV POKRENI-------");
                    SendResponse response = bot.execute(new SendMessage(update.message().chat().id(), "Igra se pokrece..."));
                }
                startGame();
            }
        }else {

            if (playersName.contains(update.message().text().substring(1))){
                System.out.println("-------PROSAO SAM USLOV IGRAC POSTOJI-------");
                play(update.message().from().firstName(),update.message().text().substring(1),update.message().chat().id());

            }

            if (update.message().text().equals("/kk")) {
                System.out.println("-------PROSAO SAM USLOV KK-------");
                if(!usersDone.contains(update.message().from().id())) {
                    usersDone.add(update.message().from().id());
                    String message = "Odigralo je  " + usersDone.size() +
                            "od" + String.valueOf(users.size());
                    sendToAll(message);
                }

            }

            if (update.message().text().equals("/prekini") || update.message().text().equals("/prekini@mjesec_gejmer_bot")) {
                System.out.println("-------PROSAO SAM USLOV PREKINI-------");
                SendResponse response = bot.execute(new SendMessage(chatId, "Prekinuli ste igru"));
                clearData();
                inGame = false;
            }

            if (update.message().text().equals("/dalje" ) && users.size() != 0 && update.message().from().id() == adminId) {
                System.out.println("-------PROSAO SAM USLOV DALJE-------");
                usersDone.clear();
                showNextNumber();
                sendTable();


            }

            if (update.message().text().equals("/status" ) && users.size() != 0) {
                System.out.println("-------PROSAO SAM USLOV STATUS-------");
                sendStatus(update.message().from().id());

            }
        }
    }

    private static void clearData() {
        users.clear();
        numbers.clear();
        playersName.clear();
        tableNumbers.clear();
    }

    private static void sendStatus(int userId) {
        String message = "Prostali brojevi: ";
        for(User user : users){
            message = message + "\n" + user.name + ": " + user.getNumbers().size();
        }
        SendResponse response = bot.execute(new SendMessage(userId, message));

    }


    private static void play(String drinkFrom,String drinkTo,long chatId) {
        if(playersName.contains(drinkFrom)) {
            int index = playersName.indexOf(drinkFrom);
            ArrayList<String> numbs = new ArrayList<>(users.get(index).getNumbers());
            System.out.println("trazim broj" + tableNumbers.get(currentNumbIndex) + " medju"+ numbs.toString()  );
            if(numbs.contains(tableNumbers.get(currentNumbIndex))){
                int numb_index = numbs.indexOf(tableNumbers.get(currentNumbIndex));
                numbs.remove(numb_index);
                users.get(index).setNumbers(numbs);
                for(User user : users) {
                    SendResponse response = bot.execute(new SendMessage(user.id,
                            drinkFrom + " ----> " + drinkTo +
                                    ", broj gutljaja :" + getSipDrink()));
                }
            }
        }

    }

    private static String getSipDrink() {
        if(currentNumbIndex<5){
            return "1";
        }
        else if(currentNumbIndex<9){
            return "2";
        }
        else if(currentNumbIndex<12){
            return "3";
        }
        else if(currentNumbIndex<14){
            return "4";
        }
        else return "5";
    }

    private static void sendToAll(String message) {
        for(User user : users) {
            SendResponse response = bot.execute(new SendMessage(user.id,message));
        }
    }

    private static void startGame() {
        inGame = true;
        initNumbers();
        currentNumbIndex = -1;
        usersDone.clear();
        Collections.shuffle(numbers);
        System.out.println("Imam listu brojeva:" + numbers.toString());
        SendResponse response = bot.execute(new SendMessage(users.get(0).id,"Pisem ti"));
        for(User user : users){
            shareNumers(user);
        }
        setTableNumbers();
        showNextNumber();
        sendTable();

    }

    private static void showNextNumber() {
        if(currentNumbIndex < 14 ) {
            currentNumbIndex++;
            tableNumbers.set(currentNumbIndex, numbers.get(currentNumbIndex));
        }else{
            for(User user : users){
                sendStatus(user.id);
            }
        }

    }

    private static void sendTable() {
        SendResponse response = bot.execute(new SendMessage(adminId,"/dalje"));
        for(User user : users) {
             response = bot.execute(new SendMessage(user.id,"-----------------AUTOBUSI-----------------"));
            ArrayList<String> playersCommand = new ArrayList<>();
            for(String plyCmd : playersName) {
                plyCmd = "/"+ plyCmd;
                playersCommand.add(plyCmd);
            }
            response = bot.execute(new SendMessage(user.id,"Komande: /kk , /status"));
            response = bot.execute(new SendMessage(user.id,"Igraci: " + playersCommand.toString()));
            response = bot.execute(new SendMessage(user.id,"Tvoji brojevi: " + user.getNumbers().toString()));
            response = bot.execute(new SendMessage(user.id,"Tabla: \n" +
                    tableNumbers.get(0) + " " + tableNumbers.get(1) + " " + tableNumbers.get(2) + " " +tableNumbers.get(3) + " "+tableNumbers.get(4) + "\n"+
                    tableNumbers.get(5) + " " + tableNumbers.get(6) + " " + tableNumbers.get(7) + " " + tableNumbers.get(8) + "\n"+
                    tableNumbers.get(9) + " " + tableNumbers.get(10) + " " + tableNumbers.get(11) + "\n" +
                    tableNumbers.get(12) + " " + tableNumbers.get(13) + "\n" +
                    tableNumbers.get(14)));
        }
    }

    private static void setTableNumbers() {
        for(int i = 0 ; i < 15; i++){
            tableNumbers.add("X");
        }
    }

    private static void shareNumers(User user) {
        ArrayList<String> numbs = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            String num = numbers.get(0);
            numbers.remove(0);
            numbs.add(num);
        }
        user.setNumbers(numbs);
    }

    private static void initNumbers() {
        /*numbers.clear();
        numbers.add("one");
        numbers.add("one");
        numbers.add("one");
        numbers.add("one");
        numbers.add("two");
        numbers.add("two");
        numbers.add("two");
        numbers.add("two");
        numbers.add("three");
        numbers.add("three");
        numbers.add("three");
        numbers.add("three");
        numbers.add("four");
        numbers.add("four");
        numbers.add("four");
        numbers.add("four");
        numbers.add("five");
        numbers.add("five");
        numbers.add("five");
        numbers.add("five");
        numbers.add("five");
        numbers.add("six");
        numbers.add("six");
        numbers.add("six");
        numbers.add("six");
        numbers.add("seven");
        numbers.add("seven");
        numbers.add("seven");
        numbers.add("seven");
        numbers.add("eight");
        numbers.add("eight");
        numbers.add("eight");
        numbers.add("eight");
        numbers.add("nine");
        numbers.add("nine");
        numbers.add("nine");
        numbers.add("nine");
        numbers.add("ten");
        numbers.add("ten");
        numbers.add("ten");
        numbers.add("ten");*/
        numbers.add("1");
        numbers.add("1");
        numbers.add("1");
        numbers.add("1");
        numbers.add("2");
        numbers.add("2");
        numbers.add("2");
        numbers.add("2");
        numbers.add("3");
        numbers.add("3");
        numbers.add("3");
        numbers.add("3");
        numbers.add("4");
        numbers.add("4");
        numbers.add("4");
        numbers.add("4");
        numbers.add("5");
        numbers.add("5");
        numbers.add("5");
        numbers.add("5");
        numbers.add("6");
        numbers.add("6");
        numbers.add("6");
        numbers.add("6");
        numbers.add("7");
        numbers.add("7");
        numbers.add("7");
        numbers.add("7");
        numbers.add("8");
        numbers.add("8");
        numbers.add("8");
        numbers.add("8");
        numbers.add("9");
        numbers.add("9");
        numbers.add("9");
        numbers.add("9");
        numbers.add("10");
        numbers.add("10");
        numbers.add("10");
        numbers.add("10");

    }


}
