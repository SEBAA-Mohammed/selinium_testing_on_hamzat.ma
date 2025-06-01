package com.snbat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver",
                "C:/Users/hp/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void testLogin() {
// Test Login to Hmizate with valid credentials
        try {
            // Navigate to the login page
            driver.get("https://hmizate.ma/connexion?back=my-account");

            // Enter email
            WebElement emailField = driver.findElement(By.cssSelector("input[name='email']"));
            emailField.sendKeys("ayman.ab@mailna.co");

            // Enter password
            WebElement passwordField = driver.findElement(By.id("field-password"));
            passwordField.sendKeys("0688115325");

            // Click login button
            WebElement loginButton = driver.findElement(By.id("submit-login"));
            loginButton.click();

            // Wait for login to complete
            wait.until(ExpectedConditions.urlToBe("https://hmizate.ma/mon-compte"));
            System.out.println("✅ Login successful!");

            // Extract and print the customer name
            WebElement customerNameSpan = driver.findElement(By.cssSelector("span.tvcms_customer_name"));
            String customerName = customerNameSpan.getText();
            System.out.println("👤 Customer Name: " + customerName);

        } finally {
            // Add small delay before quitting to observe results
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            driver.quit();
        }

    }
}
