package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.util.Arrays;
import java.util.List;

public class BotETU {
    private final TelegramBot bot;
    private final String INVALID_COMMAND_MESSAGE = "Введена не команда. Попробуйте еще раз.";
    private static final List<String> DAYS_OF_WEEK = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    public int loop = 0;
    public int loop1 = 0;
    private String apiUrl = "https://digital.etu.ru/api/mobile/schedule?groupNumber=";
    private final CommandProcessor commandProcessor;
    private final DB db;
    public BotETU(String token) {
        bot = new TelegramBot(token);
        commandProcessor = new CommandProcessor();
        db = new DB();
    }
    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                processUpdate(update);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
    private boolean isCommand(String text) {
        return text.startsWith("/");
    }
    private void processTextMessage(Message message, User user) {
        if(loop1==10)
        {
            commandProcessor.setGroupNumber(user);
            loop1=0;
        }

        String text = message.text().trim();
        long chatId = message.chat().id();

        if (text.equals("Нет")){
            loop1=10;
            SendResponse response = bot.execute(new SendMessage(chatId, "Введите новый номер группы."));
            return;
        }

        if ((loop == 1 || loop == 2 || loop == 3 || loop == 4) && loop1 == 0) {
            if(db.getGroupForUser(user).equals("0")) {
                if (text.matches("\\d{4}")) {
                    if (commandProcessor.isGroupExists(text)) {
                        apiUrl += text;

                        loop1 = 1;

                        db.insertUserIfNotExist(user, text);

                        if (loop == 3 || loop == 4) {
                            apiUrl += "&week=";
                            SendResponse response = bot.execute(new SendMessage(chatId, "Введите номер недели (1 или 2)."));
                        }
                    } else {
                        SendResponse response = bot.execute(new SendMessage(chatId, "Я не нашел группу с таким номером. Повторите попытку."));
                    }
                } else {
                    SendResponse response = bot.execute(new SendMessage(chatId, "Неверный номер группы. Введите четырехзначный номер группы."));
                }
            }
            else {
                if (commandProcessor.isGroupExists(db.getGroupForUser(user))) {
                    apiUrl += db.getGroupForUser(user);

                    loop1 = 1;

                    db.insertUserIfNotExist(user, db.getGroupForUser(user));

                    if (loop == 3 || loop == 4) {
                        apiUrl += "&week=";
                        SendResponse response = bot.execute(new SendMessage(chatId, "Введите номер недели (1 или 2)."));
                    }
                }
            }
        } else if (loop1 == 0) {
            SendResponse response = bot.execute(new SendMessage(chatId, "Для начала выберите команду. Можете открыть справочник для удобства через /help"));
        } else if ((loop == 3 || loop == 4) && loop1 == 1) {
            if (text.equals("1") || text.equals("2")) {
                apiUrl += text;
                loop1 = 2;

                if (loop == 4) {
                    apiUrl += "&weekDay=";
                    sendWeekDayButtons(chatId);
                }
            } else {
                SendResponse response = bot.execute(new SendMessage(chatId, "Неверный номер недели. Попробуйте еще раз."));
            }
        } else if (loop1 == 2 && loop == 4) {
            if (DAYS_OF_WEEK.contains(text.toUpperCase())) {
                apiUrl += text.toUpperCase();
                loop1 = 3;
            } else {
                SendResponse response = bot.execute(new SendMessage(chatId, "Неверно введен день недели. Попробуйте еще раз."));
            }
        }

        if (loop == 1 && loop1 == 1) {
            commandProcessor.viewNearLesson(chatId, apiUrl, bot);
            loop1 = 5;
        } else if (loop == 2 && loop1 == 1) {
            commandProcessor.viewScheduleForTomorrow(chatId, apiUrl, bot);
            loop1 = 5;
        } else if (loop == 4 && loop1 == 3 || loop == 3 && loop1 == 2) {
            commandProcessor.viewScheduleForDayOrWeek(chatId, apiUrl, bot);
            loop1 = 5;
        }
        if (loop1 == 5) {
            apiUrl = "https://digital.etu.ru/api/mobile/schedule?groupNumber=";
            loop1 = 0;
            loop = 0;
        }
    }
    private void processUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        User user = update.message().from();

        Message message = update.message();
        String text = message.text().trim();

        if (isCommand(text)) {
            String responseText = processCommand(text, user);
            long chatId = message.chat().id();
            SendResponse response = bot.execute(new SendMessage(chatId, responseText));
            if(!db.getGroupForUser(user).equals("0")) {
                sendAnswersButtons(chatId);
            }
        } else {
            processTextMessage(message, user);
        }
    }
    private String processCommand(String text, User user) {
        switch (text) {
            case "/start":
                return "Привет! Я бот расписания ЛЭТИ. Для списка команд введите /help.";
            case "/near_lesson":
                loop = 1;
                return commandProcessor.getNextLesson(user);
            case "/schedule_for_tomorrow":
                loop = 2;
                return commandProcessor.getScheduleForTomorrow(user);
            case "/schedule_for_week":
                loop = 3;
                return commandProcessor.getScheduleForWeek(user);
            case "/schedule_for_day":
                loop = 4;
                return commandProcessor.getScheduleForDay(user);
            case "/help":
                loop = 0;
                return commandProcessor.getHelp();
            default:
                return INVALID_COMMAND_MESSAGE;
        }
    }
    private void sendWeekDayButtons(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("MON"),
                        new KeyboardButton("TUE"),
                        new KeyboardButton("WED"),
                        new KeyboardButton("THU"),
                        new KeyboardButton("FRI"),
                        new KeyboardButton("SAT"),
                        new KeyboardButton("SUN")
                }
        ).oneTimeKeyboard(true);
        SendMessage message = new SendMessage(chatId, "Выберите день недели:")
                .replyMarkup(keyboardMarkup);
        bot.execute(message);
    }

    private void sendAnswersButtons(long chatId) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(
                new KeyboardButton[]{
                        new KeyboardButton("Да"),
                        new KeyboardButton("Нет"),
                }
        ).oneTimeKeyboard(true);
        SendMessage message = new SendMessage(chatId, "Выберите:").replyMarkup(keyboardMarkup);
        bot.execute(message);
    }

    public static void main(String[] args) {
        String token = System.getenv("BotETUToken");
        BotETU botETU = new BotETU(token);
        botETU.start();
    }
}