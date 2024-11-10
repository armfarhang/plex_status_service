package org.cs4471.plexstatusservice.controller;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Testing {

    public Testing() throws IOException {
    }

    //    public static void main(String[] args) {
//        String response = WebClient.builder()
//                .baseUrl("https://tautulli.arminfarhang.com/api/v2?apikey=8437fff7c3c843908319d7b635711b47&cmd=get_home_stats")
//                .build()
//                .get()
//                .retrieve()
//                .bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(10))
//                .onErrorResume(Exception.class, ex -> Mono.just(""))
//                .block();
//
//        // Use HashMap<String, Object> to allow nested data structures
//        Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
//        HashMap<String, Object> mapping = new Gson().fromJson(response, type);
//
//        System.out.println("Mapping: " + mapping);
//    }
    private static String formatTitleForApi(String title) {
        // Convert the title to lowercase and replace spaces with plus signs
        return title.toLowerCase().replace(" ", "+");
    }

    private static JSONObject getAPI(String apiUrl) throws Exception {

        // Make the HTTP GET request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Check if the request was successful
        if (response.statusCode() == 200) {
            // Parse the JSON response
            String jsonResponse = response.body();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            return jsonObject;
        } else {
            System.out.println("Failed to retrieve data. HTTP Status Code: " + response.statusCode());
            return null;
        }

    }

    public static HashMap<String, String> movieHashMap(JSONObject obj) {


        // Create a new HashMap for movie details
        HashMap<String, String> movieDetails = new HashMap<>();

        // Populate the movieDetails map with actual values from the JSONObject
        movieDetails.put("title", obj.getString("title"));
        movieDetails.put("year", String.valueOf(obj.getInt("year")));
        movieDetails.put("total_plays", String.valueOf(obj.getInt("total_plays")));
        try {
            String apiURL = "https://api.themoviedb.org/3/search/movie?api_key=61e62ca75c37d6158c24dc885ec051b7&query=" + formatTitleForApi(obj.getString("title"));
            JSONObject tvdbJson = getAPI(apiURL);
            JSONObject results = tvdbJson.getJSONArray("results").getJSONObject(0);
            String id = String.valueOf(results.getInt("id"));
            movieDetails.put("tvdb_id", id);
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }



        return movieDetails;
    }


    public static void main(String[] args) {
        // Define the API URL
        String apiUrl = "https://tautulli.arminfarhang.com/api/v2?apikey=8437fff7c3c843908319d7b635711b47&cmd=get_home_stats";
        ArrayList<HashMap<String, String>> top10MoviesParams = new ArrayList<>();

        try {
            // Make the HTTP GET request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the JSON response
                String jsonResponse = response.body();
                JSONObject jsonObject = new JSONObject(jsonResponse);

                JSONObject responseObject = jsonObject.getJSONObject("response");
                JSONArray dataArray = responseObject.getJSONArray("data");


                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject statObject = dataArray.getJSONObject(i);
                    String statId = statObject.getString("stat_id");
                    String statTitle = statObject.getString("stat_title");
                    if (!(statId.equals("top_movies"))) {
                        break;
                    }
                    System.out.println("Statistic ID: " + statId);
                    System.out.println("Statistic Title: " + statTitle);

                    // Navigate to the "rows" array within each statObject
                    JSONArray rowsArray = statObject.getJSONArray("rows");
                    for (int j = 0; j < rowsArray.length(); j++) {
                        JSONObject movie = rowsArray.getJSONObject(j);
//                        System.out.println("Title: " + movie.getString("title"));
                        top10MoviesParams.add(movieHashMap(movie));
                    }

                }
//                System.out.println("Top10 Movies");
//                for (int i = 0; i < top10MoviesParams.size(); i++) {
//                    HashMap<String, String> movie = top10MoviesParams.get(i);
//                    System.out.println("Movie " + (i + 1) + ":");
//
//                    // Print each key-value pair in the HashMap
//                    for (String key : movie.keySet()) {
//                        System.out.println(key + ": " + movie.get(key));
//                    }
//
//                    System.out.println(); // Add a blank line for better readability
//
//                }
            } else {
                System.out.println("Failed to retrieve data. HTTP Status Code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
