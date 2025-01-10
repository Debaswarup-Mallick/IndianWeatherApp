import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IndianWeatherApp extends Application {
    private static final String API_KEY = "46f80a02ecae410460d59960ded6e1c6";

    private TextField cityInputField;
    private Label temperatureLabel;
    private Label descriptionLabel;
    private VBox detailsBox;
    private ImageView weatherIcon;

    @Override
    public void start(Stage primaryStage) {
        // UI Elements
        Label titleLabel = new Label("Indian Weather App");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        cityInputField = new TextField();
        cityInputField.setPromptText("Enter Indian City");

        Button getWeatherButton = new Button("Get Weather");
        getWeatherButton.setOnAction(e -> fetchWeatherData());

        temperatureLabel = new Label();
        descriptionLabel = new Label();
        weatherIcon = new ImageView();
        detailsBox = new VBox(10);
        detailsBox.setStyle("-fx-padding: 10;");

        VBox root = new VBox(10, titleLabel, cityInputField, getWeatherButton, weatherIcon, temperatureLabel, descriptionLabel, detailsBox);
        root.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #f7f7f7;");

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Indian Weather App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void fetchWeatherData() {
        String cityName = cityInputField.getText().trim();
        if (cityName.isEmpty()) {
            showAlert("Please enter a city name.");
            return;
        }

        new Thread(() -> {
            try {
                String apiUrl = String.format(
                        "https://api.openweathermap.org/data/2.5/weather?q=%s,IN&appid=%s&units=metric",
                        cityName, API_KEY);
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseWeatherData(response.toString());
            } catch (Exception e) {
                showAlert("Failed to fetch weather data. Please try again.");
            }
        }).start();
    }

    private void parseWeatherData(String jsonData) {
        try {
            JSONObject data = new JSONObject(jsonData);

            String temperature = Math.round(data.getJSONObject("main").getDouble("temp")) + "°C";
            String description = data.getJSONArray("weather").getJSONObject(0).getString("description");
            String iconCode = data.getJSONArray("weather").getJSONObject(0).getString("icon");
            String feelsLike = "Feels like: " + Math.round(data.getJSONObject("main").getDouble("feels_like")) + "°C";
            String humidity = "Humidity: " + data.getJSONObject("main").getInt("humidity") + "%";
            String windSpeed = "Wind speed: " + data.getJSONObject("wind").getDouble("speed") + " m/s";

            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + ".png";

            // Update the UI on the JavaFX application thread
            javafx.application.Platform.runLater(() -> {
                temperatureLabel.setText(temperature);
                descriptionLabel.setText(description);
                weatherIcon.setImage(new Image(iconUrl));
                detailsBox.getChildren().setAll(new Label(feelsLike), new Label(humidity), new Label(windSpeed));
            });
        } catch (Exception e) {
            showAlert("Error parsing weather data.");
        }
    }

    private void showAlert(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            alert.show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
