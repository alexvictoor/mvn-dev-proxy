[![Build Status](https://travis-ci.org/alexvictoor/mvn-dev-proxy.svg?branch=master)](https://travis-ci.org/alexvictoor/mvn-dev-proxy)

mvn-dev-proxy
=============

A reverse proxy running as a maven plugin, providing useful features for web developers:

* A reverse proxy adding "no cache" HTTP headers to avoid cache issue during developement
* A file server. All requests are eby default handled by the reverse proxy. URL prefixs can be specified to serve static files.
* A livereload server. Your browser will reveice notifications when static files served by the file server have been modified. Check out the documenetation of project [netty-livereload](https://github.com/alexvictoor/netty-livereload) for further details on livereload

Usage
------

To start using this maven plugin, you just need to declare it in a maven pom file as follow:

    <plugin>
        <groupId>com.github.alexvictoor</groupId>
        <artifactId>mvn-dev-proxy</artifactId>
        <version>0.1</version>
    </plugin>

Then to launch the server:

    mvn com.github.alexvictoor:mvn-dev-proxy:run

With this minimal configuration you get a reverse proxy that listen for requests on port 8081 and send requests to localhost:8080.  
Below a more complete configuration example: 

    <plugin>
    	<groupId>com.github.alexvictoor</groupId>
        <artifactId>mvn-dev-proxy</artifactId>
        <version>0.1</version>
        <configuration>
        	<targetPort>9000</targetPort>
        	<proxyPort>9001</proxyPort>            
        	<routes>
        		<route> webjar/mymodule | my_static_folder </route>   
        	</routes>
        </configuration>
    </plugin>

With the above configuration, you get a reverse proxy listening on port 9001 and sending requests to localhost:9000. Also for requests on URLs prefixed by "webjar/mymodule" the reverse proxy will act as a file server, serving files from folder "my_static_folder". A livereload server is also started, sending notifications on each change for files in folder "my_static_folder".

Configuration options
----------------------

| Configuration key | Type   | Default value | Purpose      |
|-------------------|--------|---------------|--------------|
| proxyPort         | int    | 8081          | Port on which the proxy server will be listening |
| targetHost        | string | localhost     | Host of the server proxied                                     | 
| targetPort        | int    | 8080          | Port of the server proxied       |
| routes/route      | list   | empty         | List of "routes" on which the server will act as a file server. Each "route" is a string matching pattern "URI_PREFIX | FILE_SYSTEM_PATH"       |
| livereload        | boolean| true          | Allows to activate / disable livereload feature        |
