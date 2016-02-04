# Cyan

Cyan is a generic license server running on top of Play 2 framework.
It is primarily meant to be used for managing licences for services/applications that can access internet.

In addition to managing licences, Cyan will come with tools to detect anomalies in received data.

Future versions of Cyan might include small-scale error reporting server (something like [Sentry](https://getsentry.com))
and statistic collection (with Graphite support?)

### Terminology briefly

__Ping__ = a HTTP request sent from the application to Cyan server which usually contains license id, user id and the product id.
The server responds with a plaintext Response (see below)

__Response__ = a plaintext reply given to the client on Ping request. Developer of the application can set response
per license, per user or any other combination of request parameters. This allows for instance sending specific license
a response that blocks them from loading the application (they can of course still edit the code of the application to
remove the part of code that prevents loading it)

__Product__ = a product that contains its own default Responses and licenses.

__License__ = a Product- specific id that is usually given to a single person/entity

__User (id)__ = an identifier identifying specific instance of the license (this could be MAC or IP address)