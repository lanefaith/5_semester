import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class Main {

    //C:\вера\учеба\5_семестр\котлин\lab_xml\src\address.xml

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

    private static void processFile(String filePath) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(bufferedReader);

            Map<String, Integer> duplicateCount = new HashMap<>();
            Map<String, int[]> cityFloorsCount = new HashMap<>();
            String city = null;
            String street = null;
            String house = null;
            String floor = "0";

            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("item".equals(reader.getLocalName())) {
                            city = reader.getAttributeValue(null, "city");
                            street = reader.getAttributeValue(null, "street");
                            house = reader.getAttributeValue(null, "house");
                            floor = reader.getAttributeValue(null, "floor");
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        if ("item".equals(reader.getLocalName())) {
                            String key = city + ", " + street + ", " + house + ", " + floor;
                            int count = duplicateCount.getOrDefault(key, 0);
                            duplicateCount.put(key, count + 1);

                            if (!cityFloorsCount.containsKey(city)) {
                                cityFloorsCount.put(city, new int[5]);
                            }
                            int[] floors = cityFloorsCount.get(city);
                            int floorNumber = Integer.parseInt(floor);
                            if (floorNumber >= 1 && floorNumber <= 5) {
                                floors[floorNumber - 1]++;
                                cityFloorsCount.put(city, floors);
                            }
                        }
                        break;
                }
            }

            reader.close();

            System.out.println("Одинаковые строки:");
            for (Map.Entry<String, Integer> entry : duplicateCount.entrySet()) {
                if (entry.getValue() > 1) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }

            System.out.println("Статистика зданий в каждом городе:");
            for (Map.Entry<String, int[]> entry : cityFloorsCount.entrySet()) {
                System.out.print(entry.getKey() + ": ");
                int[] floors = entry.getValue();
                for (int j = 0; j < 5; j++) {
                    System.out.print((j + 1) + " этажных - " + floors[j] + "; ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }
}