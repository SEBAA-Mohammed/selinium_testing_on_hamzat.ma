package com.snbat;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import com.snbat.utils.Config;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchTest {

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
    void testRechercheProduit() {
        driver.get("https://www.hmizate.ma/");

        // 1. Entrer le mot-clé dans la barre de recherche
        WebElement searchInput = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvcmssearch-words")));
        searchInput.sendKeys("redmi");

        // 2. Cliquer sur le bouton de recherche
        WebElement searchBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tvheader-search-btn")));
        searchBtn.click();

        // 3. Vérifier que les résultats s'affichent dans le conteneur ".products"
        WebElement resultsContainer = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".products")));
        assertTrue(resultsContainer.isDisplayed(),
                "❌ Le conteneur '.products' n’est pas affiché. Aucun résultat trouvé.");

        // 4. Récupérer tous les produits
        List<WebElement> produits = resultsContainer.findElements(By.cssSelector("article.product-miniature")); // adapte
                                                                                                                // cette
                                                                                                                // classe
                                                                                                                // si
                                                                                                                // nécessaire
        assertFalse(produits.isEmpty(), "❌ Aucun produit trouvé dans les résultats.");

        // 5. Vérifier que chaque lien de produit contient 'montre' dans le href
        int indexPremierProduitIncorrect = -1;

        for (int i = 0; i < produits.size(); i++) {
            WebElement produit = produits.get(i);
            String nomProduit = "";
            try {
                WebElement h6 = produit.findElement(By.tagName("h6"));
                nomProduit = h6.getText().toLowerCase().trim();
            } catch (NoSuchElementException e) {
                System.out.println("⚠️ Produit #" + (i + 1) + " : pas de <h6> trouvé");
            }
            System.out.println("🕵️‍♂️ Produit #" + (i + 1) + " - Nom récupéré : '" + nomProduit + "'");

            if (!nomProduit.contains("redmi") && indexPremierProduitIncorrect == -1) {
                indexPremierProduitIncorrect = i + 1;
            }

            assertTrue(nomProduit.contains("redmi"),
                    "❌ Produit #" + (i + 1) + " ne contenant pas 'redmi' dans le nom : '" + nomProduit + "'");
        }

        System.out.println("✅ Test réussi : les produits ont bien été chargés.");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
