package demo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static demo.wrappers.Wrappers.*;

public class TestCases {
    WebDriver driver;
    WebDriverWait wait;

    /*
     * TODO: Write your tests here with testng @Test annotation.
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */
    @Test
    public void testCase01() throws InterruptedException {
        driver.get("https://flipkart.com/");
        sendKeys(wait, "//input[@placeholder='Search for Products, Brands and More']", "Washing Machine");
        sendKeys(wait, "//input[@placeholder='Search for Products, Brands and More']", Keys.ENTER);
        System.out.println("WebDriverWait 1");
        click(wait, "//div[text()='Popularity']");
        sleepFor2();
        System.out.println("Selected popularity");

        List<WebElement> ratingDivs = wait.until(drv -> drv.findElements(By.xpath("//span[starts-with(@id, 'productRating_')]/div[1]")));
        int number = 0;
        for (WebElement div : ratingDivs) {
            if (Float.valueOf(div.getText()) <= 4) {
                number++;
            }
        }
        System.out.println("Total number of products with <= 4 ratings");
        System.out.println(number);
    }

    @Test(dependsOnMethods = "testCase01")
    public void testCase02() throws InterruptedException {

        String xpathSearchBox = "//input[@type='text' and @title='Search for products, brands and more']";
        sendKeys(wait, xpathSearchBox, Keys.chord(Keys.CONTROL, "a"));
        sleepFor2();
        sendKeys(wait, xpathSearchBox, "iPhone");
        sendKeys(wait, xpathSearchBox, Keys.ENTER);
        sleepFor2();
        System.out.println("iPhone search");

        List<WebElement> percentOffDivs = wait.until(drv -> drv.findElements(By.xpath("//span[substring(text(), string-length(text()) - 4) = '% off']")));
        System.out.println("WebDriverWait 2");
        for (WebElement span : percentOffDivs) {
            String precentage = span.getText().split("\\%")[0];
            if (Integer.parseInt(precentage) >= 5) {
                WebElement fourthParentDiv = span.findElement(By.xpath("../../../../.."));
                try {
                    WebElement titleDiv = fourthParentDiv.findElement(By.xpath("./div[1]/div[1]"));
                    System.out.println("Title: " + titleDiv.getText() + " : " + precentage);
                } catch (NoSuchElementException e) {
                    System.out.println("Something went wrrong " + fourthParentDiv.getText());
                }
            }
        }
    }


    @Test(dependsOnMethods = "testCase02")
    public void testCase03() {
        String xpathSearchBox = "//input[@type='text' and @title='Search for products, brands and more']";
        sendKeys(wait, xpathSearchBox, Keys.chord(Keys.CONTROL, "a"));
        sleepFor2();
        sendKeys(wait, xpathSearchBox, "Coffee Mug");
        System.out.println("WebDriverWait 3");
        sendKeys(wait, xpathSearchBox, Keys.ENTER);
        sleepFor2();
        System.out.println("Coffee Mug search");

        click(wait, "//div[contains(text(), '4') and contains(text(), 'above')]");
        sleepFor2();

        //       List<WebElement> items = wait.until(drv -> drv.findElements(By.xpath("_75nlfW")));
//        for (WebElement item : items) {
//            WebElement firstImage = item.findElement(By.xpath(".//img[1]"));
//            System.out.println(firstImage.getAttribute("src"));
// }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='_5OesEi afFzxY']/span//following-sibling::span")));
        List<WebElement> ratingElements = driver.findElements(By.xpath("//div[@class='_5OesEi afFzxY']/span//following-sibling::span"));
        List<Integer> ratingsCount = new ArrayList<>();
        for (WebElement element : ratingElements) {
            String text = element.getText().replaceAll("[^0-9]", "");
            try {
                if (!text.isEmpty()) {
                    ratingsCount.add(Integer.parseInt(text));
                }
            } catch (NumberFormatException e) {
                // Ignore invalid values
            }
        }
        Collections.sort(ratingsCount);
        System.out.println("Sorted Ratings Counts: " + ratingsCount);
        List<WebElement> title = driver.findElements(By.xpath("//div[contains(@data-id,'MUG')]//a[@class='wjcEIp']"));
        List<WebElement> url = driver.findElements(By.xpath("//div[contains(@data-id,'MUG')]//a[@class='VJA3rP']"));

        List<Product> pr = new ArrayList<>();
        for (int i = 0; i < ratingsCount.size(); i++) {
            pr.add(new Product(ratingsCount.get(i), title.get(i).getText(), url.get(i).getAttribute("href")));
        }

        Collections.sort(pr);

        for (int i = 0; i < 5; i++) {
            System.out.println(pr.get(pr.size() - i - 1));
        }
    }

    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser() {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterTest
    public void endTest() {
        driver.close();
        driver.quit();

    }
}

class Product implements Comparable {
    int ratingCount;
    String url;
    String title;

    public Product(int ratingCount, String url, String title) {
        this.ratingCount = ratingCount;
        this.url = url;
        this.title = title;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.ratingCount, ((Product) o).ratingCount);
    }

    @Override
    public String toString() {
        return "Product{" +
                "ratingCount=" + ratingCount +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}