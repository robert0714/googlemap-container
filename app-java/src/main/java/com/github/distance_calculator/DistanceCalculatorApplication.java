package com.github.distance_calculator;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
 
import java.io.File; 
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class DistanceCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistanceCalculatorApplication.class, args);
    }

    @RestController
    @RequestMapping("/api")
    public static class DistanceController {

        @PostConstruct
        public void setup() {
            File staticDir = new File("static");
            if (!staticDir.exists()) {
                staticDir.mkdirs();
            }
        }

        @PostMapping("/distance")
        public ResponseEntity<?> calculateDistance(@RequestBody JsonNode data) {
            String address1 = data.get("address1").asText();
            String address2 = data.get("address2").asText();

            String mapUrl = generateGoogleMapsUrl(address1, address2);
            System.out.println("Generated map URL: " + mapUrl);

            try {
                ScreenshotAndDistance result = captureMapScreenshotAndDistance(mapUrl);

                if (result.screenshotBase64 != null && result.distanceValue != null && result.distanceUnit != null) {
                    return ResponseEntity.ok(new DistanceResponse(result.distanceValue, result.distanceUnit, result.screenshotBase64));
                } else {
                    return ResponseEntity.status(500).body(new ErrorResponse("Unable to capture map screenshot or distance"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body(new ErrorResponse("Error: " + e.getMessage()));
            }
        }

        private String generateGoogleMapsUrl(String address1, String address2) {
            String baseUrl = "https://www.google.com/maps/dir/";
            return baseUrl + address1 + "/" + address2;
        }

        private ScreenshotAndDistance captureMapScreenshotAndDistance(String mapUrl) throws IOException {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=900,295");

            WebDriverManager.chromedriver().setup();
            WebDriver driver = new ChromeDriver(options);

            try {
                driver.get(mapUrl);
                System.out.println("Navigated to " + mapUrl);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                WebElement directionsLoaded = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"section-directions-trip-0\"]")));
                System.out.println("Directions loaded");

                WebElement distanceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("ivN21e")));
                System.out.println("Distance element found");

                String distanceText = distanceElement.getText();
                System.out.println("Distance text: " + distanceText);

                Pattern valuePattern = Pattern.compile("\\d+\\.\\d+|\\d+");
                Matcher valueMatcher = valuePattern.matcher(distanceText);
                String distanceValue = valueMatcher.find() ? valueMatcher.group() : null;

                Pattern unitPattern = Pattern.compile("[^\\d.]+");
                Matcher unitMatcher = unitPattern.matcher(distanceText);
                String distanceUnit = unitMatcher.find() ? unitMatcher.group().trim() : null;

                if (distanceValue == null || distanceUnit == null) {
                    throw new RuntimeException("Could not extract distance value or unit");
                }

                Thread.sleep(5000);

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                File screenshotFile = new File("static", "map_screenshot_" + timestamp + ".png");
                ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).renameTo(screenshotFile);
                System.out.println("Screenshot saved to " + screenshotFile.getPath());

                byte[] fileContent = Files.readAllBytes(screenshotFile.toPath());
                String screenshotBase64 = Base64.getEncoder().encodeToString(fileContent);

                Files.delete(screenshotFile.toPath());

                return new ScreenshotAndDistance(screenshotBase64, distanceValue, distanceUnit);
            } catch (Exception e) {
                e.printStackTrace();
                return new ScreenshotAndDistance(null, null, null);
            } finally {
                driver.quit();
            }
        }

        static class ScreenshotAndDistance {
            String screenshotBase64;
            String distanceValue;
            String distanceUnit;

            ScreenshotAndDistance(String screenshotBase64, String distanceValue, String distanceUnit) {
                this.screenshotBase64 = screenshotBase64;
                this.distanceValue = distanceValue;
                this.distanceUnit = distanceUnit;
            }
        }

        static class DistanceResponse {
            public String distance;
            public String unit;
            public String map_screenshot_base64;

            DistanceResponse(String distance, String unit, String map_screenshot_base64) {
                this.distance = distance;
                this.unit = unit;
                this.map_screenshot_base64 = map_screenshot_base64;
            }
        }

        static class ErrorResponse {
            public String error;

            ErrorResponse(String error) {
                this.error = error;
            }
        }
    }
}
