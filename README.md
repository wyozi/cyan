# Cyan [![Build Status](https://travis-ci.org/wyozi/cyan.svg?branch=master)](https://travis-ci.org/wyozi/cyan)

Cyan is a generic license management server built using Play 2 framework. It is primarily meant to be used to manage licenses to an internet connected software project.

In addition to managing licences, Cyan has some basic tools to detect anomalies in license usage and analyze usage of the
product.

Cyan is best used with software products that are always connected to the internet, for instance game mods or mobile apps.

### Screenshots

#### Product view (data censored)
![](http://fruitshake.wyozi.xyz:8080/f/2017-08-26_22-12-33.png)

#### Response edit (data censored)
![](http://fruitshake.wyozi.xyz:8080/f/2017-08-26_22-14-49.png)

#### Version/PingData development
![](http://fruitshake.wyozi.xyz:8080/f/2017-08-26_20-42-40.png)

### Simple usage

Note: these instructions are best used for quick development/testing purposes. For production deployment see [Play framework deployment documentation](https://www.playframework.com/documentation/2.6.x/Deploying).

__Requirements:__
- [Scala Build Tool](http://www.scala-sbt.org/)
- Postgres server (technically Cyan comes with H2 as the default database server, but the support is lacking)

__Steps:__  
1. Clone Cyan repository, start cmd/terminal in the cloned folder and run `sbt` to fetch dependencies
2. Running `sbt` should have put you in the SBT console. Start the server with ```run -DapplyEvolutions.default=true -Dcyan.password=test -Dslick.dbs.default.driver=slick.driver.PostgresDriver$ -Dslick.dbs.default.db.driver=org.postgresql.Driver -Dslick.dbs.default.db.url=jdbc:postgresql://localhost/cyan -Dslick.dbs.default.db.user=cyanuser -Dslick.dbs.default.db.password=cyanpass```. You should replace postgres user/pass/db/url and the cyan.password to your own.
3. Access admin panel in ```http://localhost:9000/admin``` using `admin` for user and `test` (or whatever you chose) for password

### Getting started

Once you have Cyan server setup, it's time to actually use it for something.

1. Create a new product from the Products overview. Name can be anything and short name should be 'simpler' version of it (eg. name `Example Product` and short name `exprod`)
2. Once a product is created, you can now submit pings to it. In real world usage the pings would be submitted from your 
application and the application would react based on the HTTP response it receives from the Cyan server.
For now we can use Cyan's "Add Ping" feature to test pings.Open your newly added product's overview page by clicking on its name on the overview page
3. Expand the "Add Ping" panel and enter some text to the inputs. See below for what the terms mean. Once you're ready press submit.
4. "Add Ping" submits a ping and displays what the HTTP server responds with. By default the response is empty. Add a new
response by going to Responses tab and adding a new response with name and body of your choice.
5. Go back to product page and set the response to the one you just made from inside the "Configuration" panel.
6. If you add a new ping you should see a response containing body of the response you just created. This is in a nutshell
how Cyan works.

#### Submitting a ping: technical documentation

Cyan accepts pings as POST requests at `/ping`. Username, license and product should be passed as POST parameters `user`, `license` and `prod` respectively. If you want to pass ping extras, they should be passed in POST parameters as well but prefixed by `x_`. For example ping extra `version` should be named `x_version` in POST params.

Here's an example cURL command that submits a ping with an extra: `curl --data "user=Mike&license=XYZ123&prod=SomeProduct&x_version=1.0.1" 0.0.0.0:9000/ping`.

As for how often you should ping the server depends on the type of the product. If it is an product that is rarely opened you might want to ping every time the user opens the product. For products that run in the background or are often used a time delay might be better. There is no "Cyan- preferred" pinging frequency as it all depends on type of the product.

### Terminology briefly

__Ping__ = a HTTP request sent from the application to Cyan server which usually contains license id, user id and the product id.
The server responds with a plaintext Response (see below)

__Response__ = a plaintext HTTP response given to the client during the HTTP request to the ping backend. Developer of the application can set response per license, per user or any other combination of request parameters. This allows for instance sending specific license a response that blocks them from loading the application (note that the person can easily block
the request from happening or edit your product to prevent the response from doing anything)

__Product__ = a product is an object that has its own Responses and Licenses. There can be multiple products in Cyan.

__License__ = a Product- specific identifier that identifies this specific product instance. In case of a paid product
 usually identifies a single purchase of the product.

__User name/id__ = an identifier of an user of the license (this could be MAC or IP address)

### Developing custom backends

While Cyan by itself is a very generic application, you can easily add custom behavior using backends. Backends allow for instance
modifying `License` or `User` html cells in ping tables to be suffixed by a button or an icon. To create a new backend you need to
create a new (preferably sbt) project that depends on the `backend-core` module in the root folder of Cyan. You do not currently
need to depend on Cyan itself, just the backend module.

Backends that are `jar` files in the `extensions` folder are automatically loaded to classpath. During development you might want to also use the `cyan.backend.classpath` configuration property, which loads the extension classes from a folder instead of a jar.
You also need `cyan.backend.class` in both development and production and it should point to the class name of your backend class.

Example backend configuration: `-Dcyan.backend.class=mybackend.Backend -Dcyan.backend.classpath=../Cyan-mybackend/target/scala-2.11/classes/`
