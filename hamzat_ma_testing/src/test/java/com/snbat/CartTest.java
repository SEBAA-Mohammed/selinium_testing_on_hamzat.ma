package com.snbat;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import com.snbat.utils.Config;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        System.setProperty("webdriver.chrome.driver", Config.get("chromedriver.path"));
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void testAddAndRemoveFromCart() {
        driver.get("https://www.hmizate.ma/");

        // Rechercher un produit
        WebElement searchInput = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvcmssearch-words")));
        searchInput.sendKeys("redmi");

        WebElement searchBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvheader-search-btn")));
        searchBtn.click();

        WebElement resultsContainer = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".products")));
        List<WebElement> produits = resultsContainer.findElements(By.cssSelector("article.product-miniature"));
        assertFalse(produits.isEmpty(), "❌ Aucun produit trouvé.");

        // Cliquer sur le premier produit
        produits.get(0).click();

        // Fermer l'éventuelle pub Google
        closeGoogleAdIfPresent();

        // Attendre le bouton "Ajouter au panier" avec des sélecteurs plus spécifiques
        try {
            // Attendre que la page soit complètement chargée
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-add-to-cart")));

            // Essayer différents sélecteurs pour le bouton
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.add-to-cart, .product-add-to-cart, .btn-primary.add-to-cart")));

            // Vérifier si le bouton est visible et cliquable
            if (addToCartBtn.isDisplayed() && addToCartBtn.isEnabled()) {
                // Faire défiler jusqu'au bouton
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartBtn);
                Thread.sleep(1000); // Attendre que le défilement soit terminé

                // Cliquer sur le bouton
                addToCartBtn.click();
                System.out.println("✅ Bouton 'Ajouter au panier' cliqué avec succès");

                // Attendre la confirmation d'ajout au panier
                try {
                    wait.until(ExpectedConditions
                            .visibilityOfElementLocated(By.cssSelector(".cart-content-btn .btn-primary")));
                    System.out.println("✅ Confirmation d'ajout au panier détectée");
                } catch (Exception e) {
                    System.out.println("⚠️ Pas de confirmation d'ajout au panier détectée");
                }
            } else {
                System.out.println("❌ Le bouton 'Ajouter au panier' n'est pas cliquable");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du clic sur le bouton 'Ajouter au panier': " + e.getMessage());
            // Prendre une capture d'écran en cas d'échec
            ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        }

        // Fermer à nouveau une éventuelle pub
        closeGoogleAdIfPresent();

        // Accéder au panier avec une meilleure gestion des erreurs
        List<WebElement> cartItems = null;
        try {
            // Attendre que l'icône du panier soit cliquable
            WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".blockcart")));

            // Vérifier si le panier contient des articles
            String cartCount = cartIcon.findElement(By.cssSelector(".cart-products-count")).getText();
            System.out.println("Nombre d'articles dans le panier: " + cartCount);

            // Cliquer sur l'icône du panier
            cartIcon.click();

            // Attendre que la page du panier soit chargée
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#cart")));

            // Vérifier le contenu du panier avec un sélecteur plus spécifique
            WebElement cartContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#cart .cart-items, .cart-items, .cart-overview")));

            cartItems = cartContent.findElements(By.cssSelector(".cart-item, .cart-item-product"));

            if (!cartItems.isEmpty()) {
                System.out.println("✅ Panier trouvé avec " + cartItems.size() + " article(s)");
            } else {
                System.out.println("❌ Aucun article trouvé dans le panier");
                // Prendre une capture d'écran pour debug
                ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            }

            assertFalse(cartItems.isEmpty(), "❌ Aucun article trouvé dans le panier.");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la navigation au panier: " + e.getMessage());
            // Prendre une capture d'écran en cas d'échec
            ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            throw e; // Re-throw the exception to fail the test
        }

        // Supprimer le produit
        if (cartItems != null && !cartItems.isEmpty()) {
            WebElement removeBtn = cartItems.get(0).findElement(By.cssSelector(".remove-from-cart, .cart-item-remove"));
            removeBtn.click();
        }

        // Vérifier que le panier est vide
        wait.until(ExpectedConditions.invisibilityOf(cartItems.get(0)));
        System.out.println("✅ Produit supprimé du panier.");
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
