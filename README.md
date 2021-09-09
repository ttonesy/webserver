# SimpleWebserver

A simple Java web server skeleton. This code __purposely__ does not implement
much functionality to serve actual pages or other files because this is left
as an assignment to students. 

The server is composed of two classes: __WebServer__ is the main driver class
and an object of this class creates a socket and listens on the socket for 
incoming client connections. When a client connects, the __WebServer__ object
creates a __WebWorker__ object, runs it in its own __Thread__, and hands the client connection off to this worker object. 

One _WebWorker__ object handles one client connection, and __only__ one 
HTTP request, because the worker object replies with a "Connection: close"
HTTP response line. This is highly inefficient but it is simple, and keeps
the code simple. 

The code is mostly javadoc'd, so you can generate code documentation.

### Running the program

As written, this program accepts zero or one command line argument, which
is a port number to open a socket on. The port defaults to 8080.

"java WebServer" will run the server on port 8080.

"java WebServer 9000" will run the server on port 9000.

If you run it on your local machine, point your browser to 
http://localhost:8080/

If you run the server on a remote machine, point your browser to
http://machinename.domain:8080/

If you run it on a remote machine and your browser does not get a 
response, it is possible that your ISP or some network entity in
between you and the remote machine is blocking high port numbers.

