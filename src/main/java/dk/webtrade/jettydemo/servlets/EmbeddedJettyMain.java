/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.webtrade.jettydemo.servlets;

/**
 *
 * @author thomas
 */
//import dk.webtrade.jettydemo.ExampleServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class EmbeddedJettyMain {

    public static void main(String[] args) throws Exception {
        Server jettyServer = new Server(7070);
        ServletContextHandler handler = new ServletContextHandler(jettyServer, "/example");
        handler.addServlet(ServletDemo.class, "/");

//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//        context.setContextPath("/api");
//        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jettyServer.destroy();
        }
    }

}
