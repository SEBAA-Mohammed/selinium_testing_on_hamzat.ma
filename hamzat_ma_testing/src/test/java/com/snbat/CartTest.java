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
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvcmssearch-words")));
        searchInput.sendKeys("redmi");

        WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvheader-search-btn")));
        searchBtn.click();

        WebElement resultsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".products")));
        List<WebElement> produits = resultsContainer.findElements(By.cssSelector("article.product-miniature"));
        assertFalse(produits.isEmpty(), "❌ Aucun produit trouvé.");

        // Cliquer sur le premier produit
        produits.get(0).click();

        // Fermer l’éventuelle pub Google
        closeGoogleAdIfPresent();

        // Attendre le bouton "Ajouter au panier"
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.add-to-cart, .product-add-to-cart")));

        addToCartBtn.click();

        // Fermer à nouveau une éventuelle pub
        closeGoogleAdIfPresent();

        // Accéder au panier
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".blockcart")));
        cartIcon.click();

        // Vérifier que le panier contient au moins un produit
        WebElement cartContent = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-items")));
        List<WebElement> cartItems = cartContent.findElements(By.cssSelector(".cart-item"));

        assertFalse(cartItems.isEmpty(), "❌ Aucun article trouvé dans le panier.");
        System.out.println("✅ Produit ajouté au panier avec succès.");

        // Supprimer le produit
        WebElement removeBtn = cartItems.get(0).findElement(By.cssSelector(".remove-from-cart, .cart-item-remove"));
        removeBtn.click();

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
