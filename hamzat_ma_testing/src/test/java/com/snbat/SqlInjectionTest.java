package com.snbat;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

public class SqlInjectionTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // Remove deprecated system property - WebDriverManager handles this
        // automatically
        // or use ChromeOptions to specify driver path if needed
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void testSqlInjectionLogin() {
        List<String> payloads = Arrays.asList(
                "' OR 1=1--", "' OR 'x'='x", "\" OR \"x\"=\"x", "' OR '1'='1", "admin' --", "' OR ''='");
JavascriptExecutor js = (JavascriptExecutor) driver;

for (String payload : payloads) {
    try {
        driver.get("https://hmizate.ma/connexion?back=my-account");

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        
        // Modify the input field
        js.executeScript("arguments[0].setAttribute('type', 'text'); arguments[0].removeAttribute('required');", emailField);
        emailField.clear();
        emailField.sendKeys(payload);

        WebElement passwordField = driver.findElement(By.id("field-password"));
        passwordField.clear();
        passwordField.sendKeys("invalidPassword");

        WebElement loginButton = driver.findElement(By.id("submit-login"));
        loginButton.click();

        // Wait for response
        Thread.sleep(2000);

        if (driver.getCurrentUrl().contains("mon-compte")) {
            System.out.println("❗ Potential SQLi Vulnerability Detected with payload: " + payload);
        } else {
            System.out.println("✅ Login blocked as expected for payload: " + payload);
        }

    } catch (Exception e) {
        System.out.println("⚠️ Error during SQLi test with payload \"" + payload + "\": " + e.getMessage());
    }
}

        driver.quit();
    }
}
