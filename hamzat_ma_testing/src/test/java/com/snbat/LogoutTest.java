package com.snbat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.snbat.utils.Config;

public class LogoutTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver",
        Config.get("chromedriver.path"));
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void testLogOut() {
        try {
            // Navigate to the login page (replace with actual URL)
            driver.get("https://hmizate.ma/mon-compte");
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


            // --- LOGOUT ---
            WebElement accountElement = driver
                    .findElement(By.cssSelector("div.tv-header-account button.tv-myaccount-btn-desktop"));

            // Create Actions object for hover and click
            Actions actions = new Actions(driver);

            // Hover over the account element
            actions.moveToElement(accountElement).perform();

            // Wait a moment for the dropdown to appear (you might want to use explicit wait
            // here)
            try {
                Thread.sleep(1000); // Simple wait - consider using WebDriverWait in production code
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Locate and click the logout button
            WebElement logoutButton = driver.findElement(By.cssSelector("div#tvcmsdesktop-user-info a.logout"));
            logoutButton.click();

                wait.until(ExpectedConditions.urlToBe("https://hmizate.ma/connexion?back=my-account"));
            
            System.out.println("Logout successful - Redirected to login page");

        }

        catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Close the browser
        }

    }
}
