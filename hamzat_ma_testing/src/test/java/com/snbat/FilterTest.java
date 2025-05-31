package com.snbat;

import com.snbat.utils.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        //System.setProperty("webdriver.chrome.driver", "C:/Users/Adnan/Downloads/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", Config.get("chromedriver.path"));
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    public void testPriceFilter() throws InterruptedException {
        try{
            driver.get("https://hmizate.ma/");

            WebElement closeCookie = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".close-cookie.tvclose-icon")));
            closeCookie.click();

            WebElement shopping = driver.findElement(By.linkText("SHOPPING"));
            shopping.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products")));

            List<WebElement> handles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".ui-slider-handle")));
            Actions actions = new Actions(driver);

            List<WebElement> sliders = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".ui-slider-range")));

            int sliderWidth = sliders.get(1).getSize().getWidth();
            int offsetMax = -(int) (sliderWidth * 0.9);

            WebElement rightHandle = handles.get(3);
            actions.clickAndHold(rightHandle).moveByOffset(offsetMax, 0).release().perform();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("products")));
            actions.sendKeys(Keys.PAGE_DOWN).build().perform();
            while (true) {
                try {
                    WebElement backToTop = driver.findElement(By.id("tv-back-top-wrapper"));
                    if (backToTop.isDisplayed()) {
                        break;
                    }
                } catch (NoSuchElementException e) {
                    // L'élément n'est pas encore présent dans le DOM, on continue à scroller
                }

                actions.sendKeys(Keys.PAGE_DOWN).build().perform();
                Thread.sleep(500);
            }


            List<WebElement> priceElements = driver.findElements(By.cssSelector(".price"));

            System.out.println("Displayed product prices:");
            for (int i = 0; i < priceElements.size(); i++) {
                String refreshedPrice = driver.findElements(By.className("price")).get(i).getText();

                if (refreshedPrice.isEmpty()) continue;

                String numericPart = refreshedPrice.split(",")[0].replaceAll(" ", "");

                if (numericPart.isEmpty()) continue;

                int price = Integer.parseInt(numericPart);

                assertTrue(price <= 1848, "Price should be smaller than 1848");

                System.out.println(price);
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
