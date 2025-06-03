package com.snbat;

import java.time.Duration;
import java.util.List;

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

        closeGoogleAdIfPresent();

        // After attempting to close the ad, ensure we are on the signup page
        if (!driver.getCurrentUrl().contains("hmizate.ma/inscription")) {
            driver.get("https://hmizate.ma/inscription");
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

        try {
            WebElement cookieNotice = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.tvcmscookies-notice[style*='display: block']")));

            WebElement acceptCookiesBtn = cookieNotice.findElement(By.cssSelector("button, a")); // Adjust selector
            if (acceptCookiesBtn.isDisplayed()) {
                acceptCookiesBtn.click();
                System.out.println("✅ Cookie notice dismissed.");
                Thread.sleep(1000); // Wait for UI update
            }
        } catch (Exception e) {
            System.out.println("ℹ️ No cookie notice to dismiss or failed to locate.");
        }
        WebElement radioBtn = driver.findElement(By.xpath("//input[@name='id_gender' and @value='1']"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", radioBtn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", radioBtn);

        System.out.println("✅ Clicked radio button with JS.");

        WebElement label = driver.findElement(By.xpath("//label[contains(.,'M.')]"));
        label.click();
        WebElement customSpan = driver
                .findElement(By.xpath("//input[@name='id_gender' and @value='1']/following-sibling::span"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", customSpan);

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
        firstNameField.sendKeys("roumaissae");

        WebElement lastNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("lastname")));
        lastNameField.clear();
        lastNameField.sendKeys("roumaissae");

        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailField.clear();
        emailField.sendKeys("roumaissae@mailna.co");

        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));
        passwordField.clear();
        passwordField.sendKeys("0677153021");
        // Wait for the form to be ready before filling it

        System.out.println("Form filled successfully.");

        // Review that all the form inputs are filled
        assert !firstNameField.getAttribute("value").isEmpty() : "First name is empty";
        assert !lastNameField.getAttribute("value").isEmpty() : "Last name is empty";
        assert !emailField.getAttribute("value").isEmpty() : "Email is empty";
        assert !passwordField.getAttribute("value").isEmpty() : "Password is empty";

        // Click the Register button
        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.tvall-inner-btn.form-control-submit[data-link-action='save-customer']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", registerButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", registerButton);
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

    private void closeGoogleAdIfPresent() {
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

        for (WebElement iframe : iframes) {
            try {
                driver.switchTo().frame(iframe);

                List<WebElement> dismissButtons = driver.findElements(By.cssSelector("#dismiss-button"));
                if (!dismissButtons.isEmpty()) {
                    WebElement dismissButton = dismissButtons.get(0);
                    if (dismissButton.isDisplayed() && dismissButton.isEnabled()) {
                        dismissButton.click();
                        System.out.println("✅ Google ad closed inside iframe.");
                        break;
                    }
                }

            } catch (Exception e) {
                System.out.println("⚠️ Error switching to iframe: " + e.getMessage());
            } finally {
                driver.switchTo().defaultContent();
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}