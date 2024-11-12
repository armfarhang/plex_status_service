package org.cs4471.service.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class StaticController {
    private static String TMDB_APIKEY = "61e62ca75c37d6158c24dc885ec051b7";
    private static String TAUTILLI_APIKEY = "8437fff7c3c843908319d7b635711b47";
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

    private static String getMoviePosterURL(double aspectRatio, String id){
        String apiURL = String.format("https://api.themoviedb.org/3/movie/%s/images?api_key=%s", id, TMDB_APIKEY);
        String file_path = null;
        try {
            JSONObject returnedJson = getAPI(apiURL);
            JSONArray posters = returnedJson.getJSONArray("posters");
            for (int i = 0; i < posters.length(); i++) {
                JSONObject poster = posters.getJSONObject(i);
                if (poster.has("iso_639_1") && !poster.isNull("iso_639_1")) {
                    if (aspectRatio == poster.getDouble("aspect_ratio") && poster.getString("iso_639_1").equals("en")){
                        file_path = poster.getString("file_path");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("https://image.tmdb.org/t/p/original%s", file_path);
    }

    public static HashMap<String, String> movieHashMap(JSONObject obj) {

        HashMap<String, String> movieDetails = new HashMap<>();

        // Populate the movieDetails map with actual values from the JSONObject
        movieDetails.put("title", obj.getString("title"));
        movieDetails.put("year", String.valueOf(obj.getInt("year")));
        movieDetails.put("total_plays", String.valueOf(obj.getInt("total_plays")));
        try {
            String apiURL = String.format("https://api.themoviedb.org/3/search/movie?api_key=%s&query=",TMDB_APIKEY) + formatTitleForApi(obj.getString("title"));
            JSONObject tvdbJson = getAPI(apiURL);
            JSONObject results = tvdbJson.getJSONArray("results").getJSONObject(0);
            String id = String.valueOf(results.getInt("id"));
            movieDetails.put("tmdb_id", id);
            movieDetails.put("tmdb_url", String.format("https://www.themoviedb.org/movie/%s", id));
            movieDetails.put("thumbnailURL", getMoviePosterURL(0.667, id));
        } catch (Exception e) {
            e.printStackTrace();
        }



        return movieDetails;
    }
    public static HashMap<String, String> currentMovieHashMap(JSONObject session) {

        HashMap<String, String> movieDetails = new HashMap<>();
        movieDetails.put("title", session.getString("title"));
        movieDetails.put("year", String.valueOf(session.getInt("year")));
        movieDetails.put("quality_profile", String.valueOf(session.getString("quality_profile")));
        movieDetails.put("summary", session.getString("summary"));
        movieDetails.put("rating", session.getString("audience_rating"));
        movieDetails.put("studio", session.getString("studio"));

        String tmdb_id = session.getJSONArray("guids").getString(1).replaceAll("\\D+", "");
        movieDetails.put("tmdb_id", tmdb_id);
        movieDetails.put("tmdb_url", String.format("https://www.themoviedb.org/movie/%s", tmdb_id));
        movieDetails.put("thumbnailURL", getMoviePosterURL(0.667, tmdb_id));


        return movieDetails;

    }



    private void updateTop10Attrib(Model model){
        String apiUrl = String.format("https://tautulli.arminfarhang.com/api/v2?apikey=%s&cmd=get_home_stats",TAUTILLI_APIKEY);
        ArrayList<HashMap<String, String>> top10MoviesParams = new ArrayList<>();
        try{
            JSONObject jsonObject = getAPI(apiUrl);
            JSONObject responseObject = jsonObject.getJSONObject("response");
            JSONArray dataArray = responseObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject statObject = dataArray.getJSONObject(i);
                String statId = statObject.getString("stat_id");
                if (!(statId.equals("top_movies"))) {
                    break;
                }

                JSONArray rowsArray = statObject.getJSONArray("rows");
                for (int j = 0; j < rowsArray.length(); j++) {

                    JSONObject movie = rowsArray.getJSONObject(j);

//                        System.out.println("Title: " + movie.getString("title"));
                    top10MoviesParams.add(movieHashMap(movie));
                }

            }

//            model.addAttribute("img_card_1", getMoviePosterURL(0.667, tmdbID));
            model.addAttribute("top10movies", top10MoviesParams);



        }
        catch (Exception e){e.printStackTrace();}

    }

    private void updateCurrentlyPlayingMovieAttrib(Model model){
        String apiUrl = String.format("https://tautulli.arminfarhang.com/api/v2?apikey=%s&cmd=get_activity",TAUTILLI_APIKEY);
        ArrayList<HashMap<String, String>> currentlyPlayingMoviesParams = new ArrayList<>();
        try{
            JSONObject jsonObject = getAPI(apiUrl);
//            boolean isPlaying = !jsonObject.getJSONObject("response").getJSONObject("data").getString("stream_count").equals("0");
            JSONArray sessions = jsonObject.getJSONObject("response").getJSONObject("data").getJSONArray("sessions");

            for (int i = 0; i < sessions.length(); i++) {
                JSONObject session = sessions.getJSONObject(i);
                //skip currently playing shows
                if (session.getString("media_type").equals("episode")){
                    continue;
                }
                currentlyPlayingMoviesParams.add(currentMovieHashMap(session));
            }
            boolean isPlaying = !currentlyPlayingMoviesParams.isEmpty();
            model.addAttribute("sessions", currentlyPlayingMoviesParams);
            model.addAttribute("isPlaying", isPlaying);

        }
        catch (Exception e){e.printStackTrace();}


    }

    //    isCurrently playing: return true if there is something currentl playing false otherwise
    //updatCurrentlyPlaying(Model model)
    @GetMapping("/")
    public String testpage(Model model) {
        updateTop10Attrib(model);
        updateCurrentlyPlayingMovieAttrib(model);
        return "hello";
    }



//    @GetMapping("/")
//    public String testpage(Model model) {
//        String response = WebClient.builder().baseUrl("https://tautulli.arminfarhang.com/api/v2?apikey=8437fff7c3c843908319d7b635711b47&cmd=get_home_stats").build().get().retrieve().bodyToMono(String.class)
//                .timeout(Duration.ofSeconds(10))
//                .onErrorResume(Exception.class, ex -> Mono.just(""))
//                .block();
//
//        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
//        HashMap<String, String> mapping = new Gson().fromJson(response, type);
//
//        model.addAttribute("imgurl", mapping.get("message"));
//
//        System.out.println("mapping: " + mapping);
//        return "hello";
//    }

}

