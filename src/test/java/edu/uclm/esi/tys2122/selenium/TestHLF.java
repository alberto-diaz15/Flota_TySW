package edu.uclm.esi.tys2122.selenium;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.uclm.esi.tys2122.dao.UserRepository;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestHLF {
	private static WebDriver jugador, oponente;
		
	@Autowired
	private static UserRepository userDao;
	
	@BeforeAll
	public static void setUp() throws Exception {
		String userHome = System.getProperty("user.home");
		userHome = userHome.replace('\\', '/');
		if (!userHome.endsWith("/"))
			userHome = userHome + "/";
		
		System.setProperty("webdriver.chrome.driver", userHome + "chromedriver/chromedriver.exe");
		
		jugador = crearDriver(0, 0);
		oponente = crearDriver(950, 0);
	}
	
	private static WebDriver crearDriver(int x, int y) {
		WebDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.manage().window().setSize(new Dimension(950, 1000));
		driver.manage().window().setPosition(new Point(x, y));
		driver.get("http://localhost:8080");
		return driver;
	}
	
	@Test
	@Order(1)
	public void testTER() {
		jugador.findElement(By.id("ui-id-7")).click();
		oponente.findElement(By.id("ui-id-7")).click();
		jugador.findElement(By.xpath("/html/body/div/oj-module/div/div[2]/div/div/div/div[2]/button")).click();
		pausa(1000);
		oponente.findElement(By.xpath("/html/body/div/oj-module/div/div[2]/div/div/div/div[2]/button")).click();
		pausa(1000);
		jugador.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/ol/li/div[2]/button[2]")).click();
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/ol/li/div[2]/button[2]")).click();
		pausa(2000);
		
		/*Control de excepciones*/
		jugador.findElement(By.id("btnMover")).click();
		pausa(1000);
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).sendKeys("-1");
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).sendKeys("-1");
		oponente.findElement(By.id("btnMover")).click();
		pausa(1000);
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).clear();
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).clear();
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).sendKeys("5");
		oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).sendKeys("5");
		oponente.findElement(By.id("btnMover")).click();
		pausa(2000);
		
		int cont_j=0, cont_o=0;
		boolean salir=false;
		
		/*Ganador y perdedor*/
		for (int i=0; i<6; i++) {
			for (int j=0; j<6; j++) {

				jugador.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).clear();
				jugador.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).clear();
				jugador.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).sendKeys(Integer.toString(i));
				jugador.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).sendKeys(Integer.toString(j));
				jugador.findElement(By.id("btnMover")).click();
				pausa(1000);
				
				try {
					oponente.switchTo().alert().accept();
					cont_o++;
				}catch(Exception e) {}
				
				oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).clear();
				oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).clear();
				oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[1]")).sendKeys(Integer.toString(i));
				oponente.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]//div/div/ol/li/div[2]/input[2]")).sendKeys(Integer.toString(j));
				oponente.findElement(By.id("btnMover")).click();
				pausa(1000);
				
				try {
					jugador.switchTo().alert().accept();
					cont_j++;
				}catch(Exception e) {}

				
				if(cont_o >7 || cont_j>7) {
					pausa(2000);
					salir= true;
					break;
				}
			}
			if(salir==true)
				break;
		}
		pausa(5000);
		
	}
	
	@AfterAll
	public static void tearDown() {
		jugador.quit();
		oponente.quit();
	}

	private void pausa(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}