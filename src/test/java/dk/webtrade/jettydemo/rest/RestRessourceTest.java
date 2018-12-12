/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.webtrade.jettydemo.rest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.server.bio.SocketConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.jayway.restassured.RestAssured.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author thomas
 */
public class RestRessourceTest {
    Server server;
    private final Integer PORT;
    public RestRessourceTest() {
        this.PORT = 7777;
    }

    @BeforeClass
    public static void setUpClass() {
        
    }

    @AfterClass
    public static void tearDownClass() {
        
    }

    @Before
    public void setUp() {
        server = new Server(PORT);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/api/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "dk.webtrade.jettydemo.rest");
        server.setStopAtShutdown(true);
        System.out.println("starting server....");
        try {
            server.start();
            System.out.println("started and not yet joined");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            server.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test of available method, of class RestRessource.
     */
    @Test
    public void testAvailable() {
        System.out.println("testing ressource: /available");
        given().expect()
                .response().statusCode(HttpStatus.SC_OK)
                .and().body(containsString("yes!"))
                .when().get("http://localhost:7777/api/test/available");
    }

    @Test
    public void testWebappDeploy() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://localhost:7777/api/test/available");
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed! [ HttpURLConnection connection = null, try { URL url = new URL('http://localhost:8080/webapp/'), connection = (HttpURLConnection) url.openConnection(), if (connection.getResponseCode() != 200) { throw new RuntimeException('Failed!]  HTTP Error Code: " + connection.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            String str;
            while ((str = br.readLine()) != null) {
                System.out.println(str);
            }
        } catch ( IOException e) {
            e.printStackTrace();
            throw new AssertionError();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
