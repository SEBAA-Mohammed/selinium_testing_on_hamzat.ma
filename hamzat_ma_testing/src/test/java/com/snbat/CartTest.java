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
        searchInput.clear();
        searchInput.sendKeys("redmi");

        WebElement searchBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvheader-search-btn")));
        searchBtn.click();

        // Attendre que les résultats s'affichent
        WebElement resultsContainer = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".products")));
        List<WebElement> produits = resultsContainer.findElements(By.cssSelector("article.product-miniature"));
        assertFalse(produits.isEmpty(), "❌ Aucun produit trouvé.");

        // Cliquer sur le premier produit (ouvrir dans un nouvel onglet pour éviter pub)
        String productLink = produits.get(0).findElement(By.cssSelector("a.product-thumbnail, a"))
                .getAttribute("href");
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", productLink);
        // Passer à l'onglet du produit
        List<String> tabs = new java.util.ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));

        // Fermer l'éventuelle pub Google
        closeGoogleAdIfPresent();

        // Attendre le bouton "Ajouter au panier" et s'assurer qu'il est visible
        try {
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.tvall-inner-btn.add-to-cart[data-button-action='add-to-cart']")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                    addToCartBtn);
            Thread.sleep(500); // court délai pour stabilité
            addToCartBtn.click();
            System.out.println("✅ Bouton 'Ajouter au panier' cliqué avec succès");

            // Fermer le popup d'ajout au panier si présent
            try {
                WebElement closePopupBtn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("button.close.tv-addtocart-close[data-dismiss='modal']")));
                closePopupBtn.click();
                System.out.println("✅ Popup d'ajout au panier fermé");
            } catch (Exception e) {
                System.out.println("ℹ️ Aucun popup d'ajout au panier à fermer");
            }

            // Attendre la confirmation d'ajout au panier (le compteur du panier doit
            // augmenter)
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector(".blockcart .cart-products-count"), "1"));
            System.out.println("✅ Confirmation d'ajout au panier détectée");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du clic sur le bouton 'Ajouter au panier': " + e.getMessage());
            ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            fail("Impossible d'ajouter au panier");
        }

        // Fermer à nouveau une éventuelle pub
        closeGoogleAdIfPresent();

        // Accéder au panier
        List<WebElement> cartItems = null;
        try {
            WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".blockcart")));
            cartIcon.click();
            // Attendre que le vrai dropdown du panier soit visible
            WebElement cartDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".ttvcmscart-show-dropdown-right.open")));
            // Les articles du panier sont dans .ttvcart-product-wrapper.items
            cartItems = cartDropdown.findElements(By.cssSelector(".ttvcart-product-wrapper.items"));
            assertFalse(cartItems.isEmpty(), "❌ Aucun article trouvé dans le panier.");
            System.out.println("✅ Panier trouvé avec " + cartItems.size() + " article(s)");
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la navigation au panier: " + e.getMessage());
            ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            throw e;
        }

        // Supprimer le produit
        if (cartItems != null && !cartItems.isEmpty()) {
            WebElement removeBtn = cartItems.get(0)
                    .findElement(By.cssSelector(".remove-from-cart, .cart-item-remove, .js-cart-line-remove"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", removeBtn);
            wait.until(ExpectedConditions.elementToBeClickable(removeBtn)).click();
        }

        // Vérifier que le panier est vide
        wait.until(ExpectedConditions.or(
                ExpectedConditions.invisibilityOf(cartItems.get(0)),
                ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".cart-products-count"), "0")));
        System.out.println("✅ Produit supprimé du panier.");

        // Fermer l'onglet produit et revenir à l'accueil
        driver.close();
        driver.switchTo().window(tabs.get(0));
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
