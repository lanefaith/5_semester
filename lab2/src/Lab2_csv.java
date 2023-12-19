import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//C:\вера\учеба\5_семестр\котлин\lab2\src\address.csv

public class Lab2_csv {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.println("Введите путь до файла-справочника или '0' для завершения работы:");
            try {
                String input = br.readLine();
                if (input.equals("0")) {
                    System.out.println("Работа приложения завершена.");
                    break;
                }

                long startTime = System.currentTimeMillis();
                processFile(input);
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Время обработки файла: " + totalTime + " мс.");
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла: " + e.getMessage());
            }
        }
    }

    private static void processFile(String filePath) throws IOException {
        Map<String, Integer> duplicates = new HashMap<>();
        Map<String, Map<Integer, Integer>> cityFloors = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] data = line.split(";");
                if (data.length == 4) {
                    String city = data[0].replaceAll("\"", "");
                    String floorString = data[3].replaceAll("\"", "");

                    try {
                        int floor = Integer.parseInt(floorString);

                        duplicates.put(line, duplicates.getOrDefault(line, 0) + 1);

                        if (!cityFloors.containsKey(city)) {
                            Map<Integer, Integer> floors = new HashMap<>();
                            floors.put(floor, 1);
                            cityFloors.put(city, floors);
                        } else {
                            Map<Integer, Integer> floors = cityFloors.get(city);
                            floors.put(floor, floors.getOrDefault(floor, 0) + 1);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Не удалось преобразовать в число: " + floorString);
                    }
                }
            }
        }

        System.out.println("Одинаковые записи:");
        for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
            if (entry.getValue() > 1) {
                System.out.println(entry.getKey() + " - количество: " + entry.getValue());
            }
        }

        System.out.println("\nСтатистика зданий в городах:");
        for (Map.Entry<String, Map<Integer, Integer>> entry : cityFloors.entrySet()) {
            System.out.print(entry.getKey() + ": " );
            Map<Integer, Integer> floors = entry.getValue();
            for (int i = 1; i <= 5; i++) {
                if (floors.containsKey(i)) {
                    System.out.print(i + "-этажных зданий: " + floors.get(i) + "; ");
                } else {
                    System.out.println("Количество " + i + "-этажных зданий: 0 ");
                }
            }
            System.out.println();
        }
    }
}