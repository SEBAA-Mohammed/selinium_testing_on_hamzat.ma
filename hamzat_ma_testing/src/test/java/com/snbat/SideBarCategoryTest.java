package com.snbat;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.snbat.utils.Config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SideBarCategoryTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    private static final List<String> CATEGORIES_AUTORISEES = List.of(
            "Promotions",
            "Objets connectés",
            "Electroménager",
            "Beauté & Santé",
            "Sport & Loisir",
            "Mode & Accessoires",
            "Bébé & Jouets",
            "Maison & Décoration");

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", Config.get("chromedriver.path"));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-logging");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0");

        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // Augmenté
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        actions = new Actions(driver);
    }

    @Test
    public void testCategories() throws InterruptedException {
        try {
            System.out.println("Chargement de hmizate.ma...");
            driver.get("https://hmizate.ma/");
            Thread.sleep(3000);

            acceptCookies();

            List<WebElement> mainCategories = driver
                    .findElements(By.cssSelector("ul.tvverticalmenu-dropdown > li.level-1"));
            System.out.println("Nombre de catégories principales trouvées: " + mainCategories.size());

            List<Integer> indicesValides = new ArrayList<>();

            for (int i = 0; i < mainCategories.size(); i++) {
                WebElement category = mainCategories.get(i);
                String categoryName = "";
                try {
                    categoryName = category.findElement(By.cssSelector("div.tvvertical-menu-category")).getText()
                            .trim();
                } catch (Exception ignored) {
                }

                if (CATEGORIES_AUTORISEES.contains(categoryName)) {
                    indicesValides.add(i);
                } else {
                    System.out.println(
                            "Catégorie ignorée: " + (categoryName.isEmpty() ? "(vide ou non lisible)" : categoryName));
                }
            }

            for (int index : indicesValides) {
                testCategoryInPlace(index);
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.out.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void acceptCookies() {
        try {
            WebElement acceptButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.close-cookie > span")));
            acceptButton.click();
            System.out.println("Cookies acceptés");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println("Pas de cookies à accepter");
        }
    }

    private void testCategoryInPlace(int index) {
        try {
            List<WebElement> categories = driver
                    .findElements(By.cssSelector("ul.tvverticalmenu-dropdown > li.level-1"));
            if (index >= categories.size())
                return;

            WebElement category = categories.get(index);
            if (!category.isDisplayed()) {
                System.out.println("Catégorie invisible, ignorée");
                return;
            }

            String categoryName = "Catégorie " + (index + 1);
            try {
                categoryName = category.findElement(By.cssSelector("div.tvvertical-menu-category")).getText().trim();
            } catch (Exception ignored) {
            }

            System.out.println("\n--- Test de la catégorie: " + categoryName + " ---");

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", category);
            Thread.sleep(500);

            actions.moveToElement(category).perform();
            Thread.sleep(1000);

            List<WebElement> subCategories = category.findElements(By.cssSelector("ul.menu-dropdown li.level-2"));
            System.out.println("Nombre de sous-catégories: " + subCategories.size());

            try {
                WebElement link = category.findElement(By.cssSelector("a.tvvertical-menu-all-text-block"));
                String categoryUrl = link.getAttribute("href");

                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", categoryUrl);

                String originalTab = driver.getWindowHandle();
                for (String tab : driver.getWindowHandles()) {
                    if (!tab.equals(originalTab)) {
                        driver.switchTo().window(tab);
                        break;
                    }
                }

                Thread.sleep(3000);
                checkCategoryContent(categoryName);

                driver.close();
                driver.switchTo().window(originalTab);
                Thread.sleep(500);

            } catch (Exception e) {
                System.out.println("Erreur lors du clic ou navigation: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erreur catégorie " + index + ": " + e.getMessage());
        }
    }

    private void checkCategoryContent(String expectedCategoryName) {
        try {
            List<WebElement> products = driver.findElements(By.cssSelector("div.products article.product-miniature"));
            System.out.println("Produits trouvés: " + products.size());

            if (!products.isEmpty()) {
                try {
                    WebElement firstProduct = products.get(0);
                    String name = firstProduct.findElement(By.cssSelector("h6, h3, .product-title")).getText().trim();
                    String productUrl = firstProduct.findElement(By.cssSelector("a")).getAttribute("href").trim();
                    String price = firstProduct.findElement(By.cssSelector("span.price, .product-price")).getText()
                            .trim();

                    System.out.println("Premier produit: " + name + " - " + price);
                    System.out.println("Lien du produit: " + productUrl);



                } catch (Exception e) {
                    System.out.println("Détails du premier produit non disponibles");
                }
            }

            try {
                WebElement productCount = driver
                        .findElement(By.cssSelector("div.tv-total-product p.tv-total-product-number"));
                System.out.println("Nombre de produits: " + productCount.getText());
            } catch (Exception e) {
                System.out.println("Nombre de produits: (inconnu)");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification du contenu: " + e.getMessage());
        }
    }
}