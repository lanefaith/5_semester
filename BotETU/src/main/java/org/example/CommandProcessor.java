package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;

import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CommandProcessor {
    private int loop;
    private ScheduleParser scheduleParser;
    private final List<String> LIST_OF_COMMANDS = List.of("/near_lesson", "/schedule_for_tomorrow", "/schedule_for_week", "/schedule_for_day", "/help");
    private final Map<String, String> COMMAND_DESCRIPTIONS = Map.of(
            "/near_lesson", "Показать ближайшую пару.",
            "/schedule_for_tomorrow", "Показать расписание на завтра.",
            "/schedule_for_week", "Показать расписание на неделю.",
            "/schedule_for_day", "Показать расписание на определенный день.",
            "/help", "Показать список доступных команд."
    );
    public CommandProcessor() {
        scheduleParser = new ScheduleParser();
    }

    public String DayOfWeekTomorrow(LocalDate currentDate){
        LocalDate tomorrow = currentDate.plusDays(1);
        DayOfWeek dayOfWeek = tomorrow.getDayOfWeek();
        int dayOfWeekNumber = dayOfWeek.getValue();
        if (dayOfWeekNumber == 1) return "MON";
        if (dayOfWeekNumber == 2) return "TUE";
        if (dayOfWeekNumber == 3) return "WED";
        if (dayOfWeekNumber == 4) return "THU";
        if (dayOfWeekNumber == 5) return "FRI";
        if (dayOfWeekNumber == 6) return "SAT";
        if (dayOfWeekNumber == 7) return "SUN";
        else return "Ошибка";
    }
    public int WeekNumberTomorrow(LocalDate currentDate) {
        currentDate = currentDate.plusDays(1);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = currentDate.get(weekFields.weekOfWeekBasedYear());
        int week = 0;
        if(weekNumber % 2 == 0) { week = 2;}
        if(weekNumber % 2 == 1) { week = 1;}
        return week;
    }
    public String getHelp() {
        StringBuilder helpText = new StringBuilder("Доступные команды:\n");
        for (String command : LIST_OF_COMMANDS) {
            helpText.append(command).append(": ").append(COMMAND_DESCRIPTIONS.getOrDefault(command, "Описание отсутствует")).append("\n");
        }
        return helpText.toString();
    }
    public String getNextLesson() {
        return "Введите номер группы, следующая пара которой Вас интересует.";
    }
    public String getScheduleForTomorrow() {
        return "Введите номер группы, расписание которой Вы хотите узнать.";
    }
    public String getScheduleForWeek() {
        return "Введите номер группы, расписание которой Вы хотите узнать.";
    }
    public String getScheduleForDay() {
        return "Введите номер группы, расписание которой Вы хотите узнать.";
    }
    public void viewScheduleForDayOrWeek(long chatId, String apiUrl, TelegramBot bot) {
        String responseData = fetchDataFromApi(apiUrl);

        scheduleParser.resetData();
        scheduleParser.setResponseData(responseData);
        List<String> formattedSchedule = scheduleParser.parseSchedule();
        System.out.println("Number of items in formattedSchedule: " + formattedSchedule.size());

        StringBuilder fullSchedule = new StringBuilder();
        for (String daySchedule : formattedSchedule) {
            fullSchedule.append(daySchedule).append("\n\n");
        }
        SendResponse response = bot.execute(new SendMessage(chatId, fullSchedule.toString().trim()));
    }
    public void viewNearLesson(long chatId, String apiUrl, TelegramBot bot) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

       // LocalTime currentTime = LocalTime.of(12, 30);
       // LocalDate currentDate = LocalDate.of(2023, 12, 30);

        currentDate = currentDate.minusDays(1);
        String api = apiUrl;
        api += "&week=" + WeekNumberTomorrow(currentDate);
        api += "&weekDay=" + DayOfWeekTomorrow(currentDate);

        int currentHour = currentTime.getHour();
        int currentMinute = currentTime.getMinute();

        String responseData = fetchDataFromApi(api);

        scheduleParser.resetData();
        scheduleParser.setResponseData(responseData);
        List<String> formattedSchedule = scheduleParser.parseSchedule();
        System.out.println("Number of items in formattedSchedule: " + formattedSchedule.size());

        if (!formattedSchedule.isEmpty()) {
            String scheduleEntry = formattedSchedule.get(0);

            String[] lines = scheduleEntry.split("\\n");

            String dayName = "";
            String Name = "";
            String teacherName = "";
            int loop2=0;

            for (String line : lines) {
                if(Objects.equals(line, lines[0])) {dayName = line;}
                if (line.startsWith("Дисциплина: ")) { Name = extractText(line, "Дисциплина: ", "  ");}
                if (line.startsWith("\t\tПреподаватель: ")) { teacherName = extractText(line, "\t\tПреподаватель: ", "  ");}

                if (line.startsWith("\t\tВремя: ")) {
                    String startTime = extractTime(line, "Время: ", " - ");
                    String endTime = extractTime(line, " - ", " ").trim();

                    int startHour = Integer.parseInt(startTime.split(":")[0]);
                    int startMinute = Integer.parseInt(startTime.split(":")[1]);

                    if (!endTime.isEmpty()) {
                        int endHour = Integer.parseInt(endTime.split(":")[0]);
                        int endMinute = Integer.parseInt(endTime.split(":")[1]);

                        if (currentHour < startHour || (currentHour == startHour && currentMinute < startMinute)) {
                            loop2 = 1;
                            SendResponse response = bot.execute(new SendMessage(chatId, "Следующая пара: \n\n" + dayName
                                    + "\nДисциплина: " + Name + "\n\t\tПреподаватель: " + teacherName + "\n\t\tВремя: " + startTime + " - " + endTime));
                            break;
                        }
                    } else {
                        System.out.println("endTime: N/A");
                    }
                }
            }
            if(loop2 == 0){
                SendResponse response = bot.execute(new SendMessage(chatId, "Пары на сегодня закончились. Отдыхайте. Могу предоставить Вам расписание на завтра /schedule_for_tomorrow"));
            }
        }
    }
    private String extractTime(String input, String prefix, String suffix) {
        int startIndex = input.indexOf(prefix);
        int endIndex = input.indexOf(suffix, startIndex + prefix.length());

        if (startIndex != -1 && endIndex != -1) {
            return input.substring(startIndex + prefix.length(), endIndex).trim();
        } else {
            return "N/A";
        }
    }
    private String extractText(String input, String prefix, String suffix) {
        int startIndex = input.indexOf(prefix);
        if (startIndex != -1) {
            startIndex += prefix.length();

            int endIndex = input.indexOf(suffix, startIndex);
            if (endIndex != -1) {
                return input.substring(startIndex, endIndex).trim();
            }
        }
        return "N/A";
    }
    public void viewScheduleForTomorrow(long chatId, String apiUrl, TelegramBot bot) {
        LocalDate currentDate = LocalDate.now();
        apiUrl +="&week=" + WeekNumberTomorrow(currentDate);
        apiUrl +="&weekDay=" + DayOfWeekTomorrow(currentDate);

        String responseData = fetchDataFromApi(apiUrl);

        scheduleParser.resetData();
        scheduleParser.setResponseData(responseData);
        List<String> formattedSchedule = scheduleParser.parseSchedule();
        System.out.println("Number of items in formattedSchedule: " + formattedSchedule.size());

        StringBuilder fullSchedule = new StringBuilder();
        for (String daySchedule : formattedSchedule) {
            fullSchedule.append(daySchedule).append("\n\n");
        }
        SendResponse response = bot.execute(new SendMessage(chatId, fullSchedule.toString().trim()));
    }
    private String fetchDataFromApi(String api) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(api).newBuilder();
        String api_Url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(api_Url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseData = response.body().string();
                System.out.println("API Response: " + responseData);
                return responseData;
            } else {
                return "Ошибка при получении данных. Код ответа: " + response.code();
            }
        } catch (Exception e) {
            return "Ошибка при отправке запроса к API: " + e.getMessage();
        }
    }

    public boolean isGroupExists(String groupNumber) {
        try {
            String apiUrl = "https://digital.etu.ru/api/mobile/schedule?groupNumber=" + groupNumber;
            URL url = new URL(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();

                return !content.toString().equals("{}");
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}