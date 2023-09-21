import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.awt.Desktop;
import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class WikipediaSearchApp {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("Enter a search query for Wikipedia (or 0 to exit): ");
                String Input = reader.readLine();

                if ("0".equals(Input)) {
                    break;
                }

                String encodedQuery = URLEncoder.encode(Input, "UTF-8");
                String APIUrl = "https://ru.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=" + encodedQuery + "&srlimit=10";

                String jsonResponse = HttpUtils.sendGet(APIUrl);

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                JsonObject query = jsonObject.getAsJsonObject("query");
                JsonArray searchResults = query.getAsJsonArray("search");

                if (!searchResults.isEmpty()) {
                    System.out.println("Search results:");

                    for (int i = 0; i < searchResults.size(); i++) {
                        JsonObject result = searchResults.get(i).getAsJsonObject();
                        String title = result.get("title").getAsString();
                        System.out.println((i + 1) + ". " + title);
                    }

                    while (true) {
                        System.out.print("Enter the article number to open: ");
                        int choice = Integer.parseInt(reader.readLine()) - 1;

                        if (choice >= 0 && choice < searchResults.size()) {
                            JsonObject chosenResult = searchResults.get(choice).getAsJsonObject();
                            int pageID = chosenResult.get("pageid").getAsInt();
                            System.out.print("Link to the article: https://ru.wikipedia.org/w/index.php?curid=" + pageID + "\n");
                            BrowserOpener.OpenBrowser(pageID);
                            break;
                        } else {
                            System.out.println("Incorrect article selection. Try again.");
                        }
                    }
                } else {
                    System.out.println("Nothing found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Error. It was not a number that was entered.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class BrowserOpener {
    public static void OpenBrowser(int pageID) {
        String url = "https://ru.wikipedia.org/w/index.php?curid=" + pageID;
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class HttpUtils {
    public static String sendGet(String url) throws IOException {
        StringBuilder response = new StringBuilder();
        java.net.URL obj = new java.net.URL(url);
        java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }
}