package com.snbat;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;

public class SignupTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private boolean isAdPresent() {
        try {
            // Check for common ad indicators
            return driver.findElements(By.id("dismiss-button")).size() > 0 ||
                    driver.findElements(By.xpath("//*[contains(@class, 'close-button')]")).size() > 0 ||
                    driver.findElements(By.xpath("//*[contains(text(), 'Fermer')]")).size() > 0 ||
                    driver.findElements(By.xpath("//div[@style='cursor: pointer;']//svg")).size() > 0 ||
                    driver.findElements(
                            By.xpath("//*[contains(@aria-label, 'close') or contains(@aria-label, 'Fermer')]"))
                            .size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

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
    void testSignUp() throws InterruptedException {
        driver.get("https://www.hmizate.ma/");

        // Hover over the User Icon button instead of clicking
        WebElement userIconButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button.btn-unstyle.tv-myaccount-btn.tv-myaccount-btn-desktop")));
        wait.until(ExpectedConditions.visibilityOf(userIconButton));
        wait.until(ExpectedConditions.elementToBeClickable(userIconButton));

        Actions actions = new Actions(driver);
        actions.moveToElement(userIconButton).perform();

        // Click the "Connexion" link
        WebElement connexionLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div#tvcmsdesktop-user-info a.tvhedaer-sign-btn")));
        try {
            connexionLink.click();
        } catch (ElementNotInteractableException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", connexionLink);
        }

        // Click on "Pas de compte ? Créez-en un"
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[data-link-action='display-register-form']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", registerLink);

        // Handle potential ad vignette
        Thread.sleep(2000); // Consider replacing with explicit wait

        // Handle different types of ads that might appear
        if (driver.getCurrentUrl().contains("google_vignette") || driver.getCurrentUrl().contains("doubleclick")
                || driver.getTitle().toLowerCase().contains("ad") || isAdPresent()) {
            System.out.println("Ad detected. Attempting to dismiss...");

            boolean adDismissed = false;

            // Type 1: Google Vignette SVG close button
            if (!adDismissed) {
                try {
                    WebElement svgCloseBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//div[@style='cursor: pointer;']//svg//path[@d='M38 12.83L35.17 10 24 21.17 12.83 10 10 12.83 21.17 24 10 35.17 12.83 38 24 26.83 35.17 38 38 35.17 26.83 24z']")));
                    svgCloseBtn.click();
                    System.out.println("Ad dismissed - Type 1: Google Vignette SVG close button.");
                    adDismissed = true;
                } catch (Exception e) {
                    // Continue to next type
                }
            }

            // Type 2: Close button with ID "dismiss-button"
            if (!adDismissed) {
                try {
                    WebElement dismissBtn = wait
                            .until(ExpectedConditions.elementToBeClickable(By.id("dismiss-button")));
                    dismissBtn.click();
                    System.out.println("Ad dismissed - Type 2: ID dismiss-button.");
                    adDismissed = true;
                } catch (Exception e) {
                    // Continue to next type
                }
            }

            // Type 3: Close button with "Fermer" text
            if (!adDismissed) {
                try {
                    WebElement fermerBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//*[contains(text(), 'Fermer') and (@role='button' or @tabindex='0' or contains(@class, 'close') or contains(@class, 'button'))]")));
                    fermerBtn.click();
                    System.out.println("Ad dismissed - Type 3: Fermer text button.");
                    adDismissed = true;
                } catch (Exception e) {
                    // Continue to next type
                }
            }

            // Type 4: Generic close button patterns
            if (!adDismissed) {
                try {
                    WebElement genericCloseBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//div[contains(@class, 'close-button') or contains(@class, 'dismiss') or contains(@aria-label, 'close') or contains(@aria-label, 'Fermer')]")));
                    genericCloseBtn.click();
                    System.out.println("Ad dismissed - Type 4: Generic close button.");
                    adDismissed = true;
                } catch (Exception e) {
                    // Continue to next type
                }
            }

            // Type 5: Skip button or any button containing skip/skip text
            if (!adDismissed) {
                try {
                    WebElement skipBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath(
                                    "//*[contains(text(), 'Skip') or contains(text(), 'Ignorer') or contains(text(), 'Passer')]")));
                    skipBtn.click();
                    System.out.println("Ad dismissed - Type 5: Skip button.");
                    adDismissed = true;
                } catch (Exception e) {
                    // Continue to final fallback
                }
            }

            // Type 6: JavaScript fallback for cursor pointer divs
            if (!adDismissed) {
                try {
                    WebElement jsCloseBtn = driver.findElement(By.xpath("//div[@style='cursor: pointer;']"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", jsCloseBtn);
                    System.out.println("Ad dismissed - Type 6: JavaScript click on cursor pointer div.");
                    adDismissed = true;
                } catch (Exception e) {
                    System.out.println("All ad dismiss attempts failed.");
                }
            }

            // Wait for ad to close and navigate back to signup page
            if (adDismissed) {
                Thread.sleep(2000); // Wait for ad to close
                // Only redirect if we're still on an ad page
                if (!driver.getCurrentUrl().contains("hmizate.ma") ||
                        driver.getCurrentUrl().contains("google_vignette") ||
                        driver.getCurrentUrl().contains("doubleclick")) {
                    driver.get("https://hmizate.ma/inscription");
                }
            } else {
                // If no ad dismiss worked, try to navigate directly
                driver.get("https://hmizate.ma/inscription");
            }
        }

        // Fill the form with proper waits

        // Select "M." gender radio button (value="1")
        // Find the "M." gender radio button by locating the label containing "M."
        // Locate the radio button using a CSS selector
        WebElement radioButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='id_gender'][value='1']")));

        // Alternative XPath locator (uncomment if needed)
        WebElement _radioButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@name='id_gender' and @value='1']")));

        // Click the radio button if it's not already selected
        if (!radioButton.isSelected()) {
            try {
                radioButton.click();
                System.out.println("Radio button with value '1' was successfully clicked.");
            } catch (Exception e) {
                try {
                    _radioButton.click();
                    System.out.println("Alternative radio button with value '1' was successfully clicked.");
                } catch (Exception ex) {
                    System.out.println("❌ Failed to click both radio buttons: " + ex.getMessage());
                }
            }
        } else {
            System.out.println("Radio button with value '1' was already selected.");
        }

        WebElement firstNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("firstname")));
        firstNameField.clear();
        firstNameField.sendKeys("Ayman");

        WebElement lastNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("lastname")));
        lastNameField.clear();
        lastNameField.sendKeys("Aarab Aarab");

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailField.clear();
        emailField.sendKeys("Aarab_aymane_test@example.com");

        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));
        passwordField.clear();
        passwordField.sendKeys("0688115325");

        System.out.println("Form filled successfully.");

        // Review that all the form inputs are filled
        assert !firstNameField.getAttribute("value").isEmpty() : "First name is empty";
        assert !lastNameField.getAttribute("value").isEmpty() : "Last name is empty";
        assert !emailField.getAttribute("value").isEmpty() : "Email is empty";
        assert !passwordField.getAttribute("value").isEmpty() : "Password is empty";

        // Click the Register button
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.tvall-inner-btn.form-control-submit[data-link-action='save-customer']")));
        registerButton.click();
        // Wait for possible redirect after registration
        wait.until(ExpectedConditions.urlContains("hmizate.ma"));

        // Check if redirected to homepage
        if (driver.getCurrentUrl().equals("https://hmizate.ma/")) {
            // Extract the member name
            WebElement memberName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("span.tvcms_customer_name")));
            String signedMemberName = memberName.getText();
            System.out.println("Signed member name: " + signedMemberName);
        } else {
            System.out.println("Registration failed or did not redirect to homepage.");
        }

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}