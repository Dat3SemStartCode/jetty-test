# Jetty

Jetty has a slogan, "Don’t deploy your application in Jetty, deploy Jetty in your application!" What this means is that as an alternative to bundling your application as a standard WAR to be deployed in Jetty, Jetty is designed to be a software component that can be instantiated and used in a Java program just like any POJO. Put another way, running Jetty in embedded mode means putting an HTTP module into your application, rather than putting your application into an HTTP server. [source](https://www.eclipse.org/jetty/documentation/9.4.x/embedding-jetty.html)

## Typical todos:
1. Create a Server instance.
2. Add/Configure **Connectors**.
3. Add/Configure **Handlers** and/or **Contexts** and/or **Servlets**.
4. Start the Server.
5. Wait on the server or do something else with your thread.

## Using Handlers
To produce a response to a request, Jetty requires that you set a Handler on the server. A handler may:
- Examine/modify the HTTP request.
- Generate the complete HTTP response.
- Call another Handler (see HandlerWrapper).
- Select one or many Handlers to call (see HandlerCollection).

Much of the standard Servlet container in Jetty is implemented with HandlerWrappers that daisy chain handlers together: 
- ContextHandler to SessionHandler
- SessionHandler to SecurityHandler
- SecurityHandler to ServletHandler
- ...
- Example of how ContextHandler handles the request within the scope the ServletHandler has established.
```java
Server.handle(...)
  ContextHandler.doScope(...)
    ServletHandler.doScope(...)
      ContextHandler.doHandle(...) //Now contextHandler has the ServletHandlers scope.
        ServletHandler.doHandle(...)
          SomeServlet.service(...)
```
A list of commonly used handlers:
- ServletHandler
- RessourceHandler
- DefaultHandler
- Implemantations of AbstractHandler
- ContextHandler

### Implement a basic handler:
```java
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HelloHandler extends AbstractHandler
{
    final String greeting;
    final String body;

    public HelloHandler()
    {
        this("Hello World");
    }

    public HelloHandler( String greeting )
    {
        this(greeting, null);
    }

    public HelloHandler( String greeting, String body )
    {
        this.greeting = greeting;
        this.body = body;
    }

    @Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException, ServletException
    {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.println("<h1>" + greeting + "</h1>");
        if (body != null)
        {
            out.println(body);
        }
        baseRequest.setHandled(true);
    }
}
```
The above handler can be used like this:
```java
public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new HelloHandler());

        server.start();
        server.join();
    }
```

### HandlerList
A Handler Collection that calls each handler in turn until either an exception is thrown, the response is committed or the request.isHandled() returns true. 

### RessourceHandler
- Import: `import org.eclipse.jetty.server.handler.ResourceHandler;`
- Instansiate: `ResourceHandler resource_handler = new ResourceHandler();`

**Configure the ResourceHandler**
- Set Directories on the ressource handler: `resource_handler.setDirectoriesListed(true);`
- Set what file(s) should be loaded as default: 
  - `resource_handler.setWelcomeFiles(new String[]{ "index.html" });`
- Set location of where to look for files (in this case it is current directory but it can be anything that the jvm has access to): `resource_handler.setResourceBase(".");`

**Add handlers to the server**
- `HandlerList handlers = new HandlerList();`
- `handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });`
  - DefaultHandler will generate a nice 404 response for any requests that do not match a static resource.
- `server.setHandler(handlers);`

**Start the server**
- `server.start();`
- `server.join();` wait for the server to be started before doing anything with it.

### ServletHander
- **Servlets** are the standard way to provide application logic that handles HTTP requests. 
- Servlets are similar to a Jetty Handler except that the request object is not mutable and thus cannot be modified. 
- Servlets are handled in Jetty by a **ServletHandler**.

```java
public static void main( String[] args ) throws Exception {
    Server server = new Server(8080);
    // The ServletHandler is a very simple way to create a context handler
    // that is backed by an instance of a Servlet. This handler then needs to be registered with the Server object.
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    // IMPORTANT:
    // This is a raw Servlet, not a Servlet that has been configured
    // through a web.xml or @WebServlet annotation.
    handler.addServletWithMapping(HelloServlet.class, "/*");

    server.start();
    server.join();
}
@SuppressWarnings("serial")
public static class HelloServlet extends HttpServlet
{
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
    }
}
```

### ContextHandler
Sets the context for the servlet and optionally provides:
- A **set of attributes** that is available via the ServletContext API.
- A **set of init parameters** that is available via the ServletContext API.
- A base Resource which is used as the **document root for static resource** requests via the ServletContext API.
- A set of **virtual host names**.
- A Classloader that is set as the Thread context classloader while request handling is in scope.

Here is a simpe example of loading a servlet in relation to a context path:

```java
Server server = new Server(7004);
// Add a single handler on context "/hello"
ContextHandler context = new ContextHandler();
context.setContextPath( "/hello" );
context.setHandler( new HelloHandler() );
//Can be accessed using http://localhost:7004/hello
server.setHandler( context );
server.start();
server.join();
...

```
#### Multiple contextHandlers
It is possible to have a collection of context handlers: ContextHandlerCollection:
```java
public static void main( String[] args ) throws Exception{
    Server server = new Server(8080);

    ContextHandler context = new ContextHandler("/");
    context.setContextPath("/");
    context.setHandler(new HelloHandler("Root Hello"));

    ContextHandler contextFR = new ContextHandler("/fr");
    contextFR.setHandler(new HelloHandler("Bonjoir"));

    ContextHandler contextIT = new ContextHandler("/it");
    contextIT.setHandler(new HelloHandler("Bongiorno"));

    ContextHandler contextV = new ContextHandler("/");
    contextV.setVirtualHosts(new String[] { "127.0.0.2" });
    contextV.setHandler(new HelloHandler("Virtual Hello"));

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { context, contextFR, contextIT, contextV });

    server.setHandler(contexts);

    server.start();
    server.dumpStdErr();
    server.join();
}
```
### ServletContextHandler
A ServletContexthandler is a context handler that has support for sessions and servlets.



## Using connectors
In the previous example a connector was not specied. This means that on `Server server = new Server(8080);` a default internal connector is created. If we want control of the connector we can specify it ourselfes:
Simple ServerConnector example: 
```java
public static void main( String[] args ) throws Exception {
    // The Server
    Server server = new Server();

    // HTTP connector
    ServerConnector connector = new ServerConnector(server);
    connector.setHost("localhost");
    connector.setPort(7003);
    connector.setIdleTimeout(30000);

    // Set the connector
    server.addConnector(connector);

    // Set a handler. HelloHandler is described above...
    server.setHandler(new HelloHandler());

    // Start the server
    server.start();
    server.join();
}

```
In case you want to suppert both http and https you need to configure more than one connector. 





## HTTPS


## Restfull webservice with jetty and jersey
Code example with 2 classes:
1. EntryPoint
2. Server

EntryPoint:
```java
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/entry-point")
public class EntryPoint {

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }
}
```
RestServer:
```java
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class RestServer {
    public static void main(String[] args) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(
             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
           "jersey.config.server.provider.classnames",
           EntryPoint.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}
```

POM.xml
```xml
<dependencies>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-server</artifactId>
        <version>9.2.3.v20140905</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-servlet</artifactId>
        <version>9.2.3.v20140905</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.core</groupId>
        <artifactId>jersey-server</artifactId>
        <version>2.7</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-servlet-core</artifactId>
        <version>2.7</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.containers</groupId>
        <artifactId>jersey-container-jetty-http</artifactId>
        <version>2.7</version>
    </dependency>
    <dependency>
        <groupId>org.glassfish.jersey.media</groupId>
        <artifactId>jersey-media-moxy</artifactId>
        <version>2.7</version>
    </dependency>
</dependencies>
....... AND build section: ......
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>1.6</version>
      <configuration>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <filters>
          <filter>
            <artifact>*:*</artifact>
            <excludes>
              <exclude>META-INF/*.SF</exclude>
              <exclude>META-INF/*.DSA</exclude>
              <exclude>META-INF/*.RSA</exclude>
            </excludes>
          </filter>
        </filters>
      </configuration>
 
      <executions>
        <execution>
          <phase>package</phase>
          <goals>
            <goal>shade</goal>
          </goals>
          <configuration>
            <transformers>
              <transformer
                implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
              <transformer
                implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <manifestEntries>
                  <Main-Class>dk.webtrade.jettydemo.rest.RestService</Main-Class>
                </manifestEntries>
              </transformer>
            </transformers>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Now Run the server from command line: 
```bash
cd restprj
mvn clean install
cd target
java -jar jettyDemo-1.0.jar
```

## JPA
Steps:
1. Setup depencies in POM.xml
2. Create an entity file and annotate with 
  - `@Entity`
  - `public class Person implements Serializable` Serializable
  - `private static final long serialVersionUID = 1L;`
  - @Id and @GeneratedValue
  ```
  @Id @GeneratedValue
  private long id;
  ```
3. Click on the warning next to @Entity and create a Persistance Unit.
4. 

JPA dependencies: 
```xml
<!--EclipseLink for JPA and Mysql connector-->
    <dependency>
        <groupId>org.eclipse.persistence</groupId>
        <artifactId>eclipselink</artifactId>
        <version>2.5.2</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.persistence</groupId>
        <artifactId>org.eclipse.persistence.jpa.modelgen.processor</artifactId>
        <version>2.5.2</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>5.1.39</version>
    </dependency>
```