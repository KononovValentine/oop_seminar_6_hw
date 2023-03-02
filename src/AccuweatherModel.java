import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Weather;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

public class AccuweatherModel implements WeatherModel {
    private static final String PROTOKOL = "https";
    private static final String BASE_HOST = "dataservice.accuweather.com";
    private static final String FORECASTS = "forecasts";
    private static final String VERSION = "v1";
    private static final String DAILY = "daily";
    private static final String ONE_DAY = "1day";
    private static final String FIVE_DAYS = "5day";
    private static final String API_KEY = "6Y9muQenMfK7N7nEibbo5NN2Ue3WlWmU";
    private static final String API_KEY_QUERY_PARAM = "apikey";
    private static final String LOCATIONS = "locations";
    private static final String CITIES = "cities";
    private static final String AUTOCOMPLETE = "autocomplete";

    private static final OkHttpClient okHttpClient = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private DataBaseRepository dataBaseRepository = new DataBaseRepository();

    public void getWeather(String selectedCity, Period period) throws IOException {
        switch (period) {
            case NOW:
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme(PROTOKOL)
                        .host(BASE_HOST)
                        .addPathSegment(FORECASTS)
                        .addPathSegment(VERSION)
                        .addPathSegment(DAILY)
                        .addPathSegment(ONE_DAY)
                        .addPathSegment(detectCityKey(selectedCity))
                        .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                        .build();

                Request request = new Request.Builder()
                        .url(httpUrl)
                        .build();

                Response oneDayForecastResponse = okHttpClient.newCall(request).execute();
                String weatherResponse = oneDayForecastResponse.body().string();
                String oneDay = weatherResponse.split("\"DailyForecasts\":\\[")[1];
                oneDay = oneDay.substring(0, oneDay.length() - 2);
                System.out.println("Погода для города " + selectedCity);
                System.out.println(getWeatherResult(oneDay));
                break;
            case FIVE_DAYS:
                HttpUrl httpUrlFiveDays = new HttpUrl.Builder()
                        .scheme(PROTOKOL)
                        .host(BASE_HOST)
                        .addPathSegment(FORECASTS)
                        .addPathSegment(VERSION)
                        .addPathSegment(DAILY)
                        .addPathSegment(FIVE_DAYS)
                        .addPathSegment(detectCityKey(selectedCity))
                        .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                        .build();

                Request requestFiveDays = new Request.Builder()
                        .url(httpUrlFiveDays)
                        .build();

                Response fiveDayForecastResponse = okHttpClient.newCall(requestFiveDays).execute();
                String weatherResponseFiveDays = fiveDayForecastResponse.body().string();
                String fiveDays = weatherResponseFiveDays.split("\"DailyForecasts\":\\[")[1];
                fiveDays = fiveDays.substring(0, fiveDays.length() - 2);
                String[] daysArray = fiveDays.split("},\\{");
                for (int i = 0; i < daysArray.length; i++) {
                    System.out.println(getWeatherResult(daysArray[i]));
                }
                break;
        }
    }

    private String getWeatherResult(String weather) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Дата: ");
        String date = weather.split("Date\":\"")[1].substring(0, 10);
        stringBuilder.append(date);
        stringBuilder.append(", минимальная температура (фаренгейт): ");
        String min = weather.split("Minimum\":\\{\"Value\":")[1].substring(0, 4);
        stringBuilder.append(min);
        stringBuilder.append(", максимальная температура (фаренгейт): ");
        String max = weather.split("Maximum\":\\{\"Value\":")[1].substring(0, 4);
        stringBuilder.append(max);

        return stringBuilder.toString();
    }

    public List<Weather> getSavedToDBWeather() {
        return dataBaseRepository.getSavedToDBWeather();
    }

    private String detectCityKey(String selectCity) throws IOException {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(PROTOKOL)
                .host(BASE_HOST)
                .addPathSegment(LOCATIONS)
                .addPathSegment(VERSION)
                .addPathSegment(CITIES)
                .addPathSegment(AUTOCOMPLETE)
                .addQueryParameter(API_KEY_QUERY_PARAM, API_KEY)
                .addQueryParameter("q", selectCity)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("accept", "application/json")
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseString = response.body().string();

        String cityKey = objectMapper.readTree(responseString).get(0).at("/Key").asText();
        return cityKey;
    }
}