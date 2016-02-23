# Cyan ![](https://travis-ci.org/wyozi/cyan.svg?branch=master)

Cyan is a generic license server running on top of Play 2 framework.
It is primarily meant to be used for managing licences for services/applications that can access internet.

In addition to managing licences, Cyan will come with tools to detect anomalies in received data.

Future versions of Cyan might include small-scale error reporting server (something like [Sentry](https://getsentry.com))
, statistic collection (with Graphite support?) and some utilities for A/B testing.

### Installation/Usage (assuming Linux server)

1. Install activator from https://www.typesafe.com/activator/download.
2. Clone Cyan repository to somewhere (here assumed to be in same folder where the activator folder is)
3. Compile/create startup scripts with ```../activator-dist-1.3.6/activator -mem 300 clean compile stage```
4. Run Cyan with  ```target/universal/stage/bin/cyan -DapplyEvolutions.default=true -Dcyan.password=test``` (remember to use different password for production usage)
5. Access admin panel in ```http://localhost:9000/admin/products``` using `admin` for user and `test` for password

By default Cyan places the H2 database files in your home directory. If you'd like to change that, pass `-Dslick.dbs.default.db.urljdbc:h2:dbpath` in run options where `dbpath` is the path for database files.

If you'd like to use postgres instead, add this to run options: ```-Dslick.dbs.default.driver=slick.driver.PostgresDriver$ -Dslick.dbs.default.db.driver=org.postgresql.Driver -Dslick.dbs.default.db.url=jdbc:postgresql://localhost/cyan -Dslick.dbs.default.db.user=cyanuser -Dslick.dbs.default.db.password=cyanpass```
where `cyan` is database name, `cyanuser` is database user and `cyanpass` is database password

### Terminology briefly

__Ping__ = a HTTP request sent from the application to Cyan server which usually contains license id, user id and the product id.
The server responds with a plaintext Response (see below)

__Response__ = a plaintext reply given to the client on Ping request. Developer of the application can set response
per license, per user or any other combination of request parameters. This allows for instance sending specific license
a response that blocks them from loading the application (they can of course still edit the code of the application to
remove the part of code that prevents loading it)

__Product__ = a product that contains its own default Responses and licenses.

__License__ = a Product- specific id that is usually given to a single person/entity

__User name/id__ = an identifier identifying specific instance of the license (this could be MAC or IP address)

### Developing custom backends

While Cyan by itself is a very generic application, you can easily add custom behavior using backends. Backends allow for instance
modifying `License` or `User` html cells in ping tables to be suffixed by a button or an icon. To create a new backend you need to
create a new (preferably sbt) project that depends on the `backend-core` module in the root folder of Cyan. You do not currently
need to depend on Cyan itself, just the backend module.

Backends that are `jar` files in the `extensions` folder are automatically loaded to classpath. During development you might want to also use the `cyan.backend.classpath` configuration property, which loads the extension classes from a folder instead of a jar.
You also need `cyan.backend.class` in both development and production and it should point to the class name of your backend class.

Example backend configuration: `-Dcyan.backend.class=mybackend.Backend -Dcyan.backend.classpath=../Cyan-mybackend/target/scala-2.11/classes/`

### Code conventions

If a variable/SQL column is suffixed with `_id` or `Id`, it usually (preferably always) refers to an integer identifier.