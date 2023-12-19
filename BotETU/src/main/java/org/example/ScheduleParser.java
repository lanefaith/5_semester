package org.example;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScheduleParser {
    private String responseData;
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    public void resetData() {
        this.responseData = null;
    }
    public List<String> parseSchedule() {
        List<String> scheduleList = new ArrayList<>();

        try {
            JsonElement jsonElement = JsonParser.parseString(responseData);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String groupNumber = entry.getKey();
                JsonObject groupObject = entry.getValue().getAsJsonObject();

                JsonElement daysElement = groupObject.get("days");
                if (daysElement != null && daysElement.isJsonObject()) {
                    JsonObject daysObject = daysElement.getAsJsonObject();

                    for (Map.Entry<String, JsonElement> dayEntry : daysObject.entrySet()) {
                        String dayName = dayEntry.getKey();
                        JsonObject dayObject = dayEntry.getValue().getAsJsonObject();

                        String daySchedule = parseDaySchedule(groupNumber, dayName, dayObject);
                        scheduleList.add(daySchedule);
                    }
                } else {
                    scheduleList.add("No schedule for group " + groupNumber);
                }
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        return scheduleList;
    }
    private String parseDaySchedule(String groupNumber, String dayName, JsonObject dayObject) {
        if ("0".equals(dayName)) dayName = "Понедельник";
        if ("1".equals(dayName)) dayName = "Вторник";
        if ("2".equals(dayName)) dayName = "Среда";
        if ("3".equals(dayName)) dayName = "Четверг";
        if ("4".equals(dayName)) dayName = "Пятница";
        if ("5".equals(dayName)) dayName = "Суббота";
        if ("6".equals(dayName)) dayName = "Воскресенье";
        StringBuilder daySchedule = new StringBuilder(dayName + ":\n");

        JsonElement lessonsElement = dayObject.get("lessons");
        if (lessonsElement != null && lessonsElement.isJsonArray()) {
            JsonArray lessonsArray = lessonsElement.getAsJsonArray();

            if (lessonsArray.size() > 0) {
                for (JsonElement lessonElement : lessonsArray) {
                    JsonObject lessonObject = lessonElement.getAsJsonObject();
                    String lessonInfo = parseLessonInfo(lessonObject);
                    daySchedule.append(lessonInfo).append("\n");
                }
            } else {
                daySchedule.append("Занятий в этот день нет.\n");
            }
        } else {
            daySchedule.append("Занятий в этот день нет.\n");
        }

        return daySchedule.toString();
    }
    private String parseLessonInfo(JsonObject lessonObject) {
        String teacher = getStringOrNull(lessonObject, "teacher");
        String subjectType = getStringOrNull(lessonObject, "subjectType");
        String lessonName = getStringOrNull(lessonObject, "name");
        String startTime = getStringOrNull(lessonObject, "start_time");
        String endTime = getStringOrNull(lessonObject, "end_time");

        StringBuilder lessonInfo = new StringBuilder("Дисциплина: " + lessonName + " (" + subjectType + ")  \n");
        lessonInfo.append("\t\tПреподаватель: ").append(teacher).append("  \n");
        lessonInfo.append("\t\tВремя: ").append(startTime).append(" - ").append(endTime).append(" ");

        return lessonInfo.toString();
    }
    private String getStringOrNull(JsonObject jsonObject, String key) {
        JsonElement element = jsonObject.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : "N/A";
    }
}