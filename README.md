# b3inject - Java Agent for automatically propagating B3 headers
The idea behind this project is very simple: Istio is great for capturing and monitoring traffic between microservices without having to modify the code. Except for the fact that there's no way if tying spans together as into a contigous trace. To do that, you need to pass something called "B3 headers" every time a service is called. 

## Why can't Istio do it?
Istio sees every incoming and outgoing call, so why can't it piece the transactions together without needing changes to the code? The problem arises because there's no way of accurately pair ingress and egress. Imagine you have a service that consumes 1000 incoming calls per second and spews out 1000 outgoing calls. Yes, the Istio/Envoy proxies will see all this traffic, but there's no way of knowing which of the 1000 incoming calls matched which of the 1000 outgoing ones. To do this, you need to be able to follow a call chain through the application itself.

For more details, please refer to this discussion: https://github.com/openzipkin/b3-propagation/blob/master/README.md

## Why a Java agent?
Yes, there are many ways you can do this through application frameworks such as Spring, SpringBoot and Dropwizard. However, they all require changes to application code. A Java agent allows you to add propagation of the B3 headers without any changes to the Java code.

## How does it work?
The code is pretty simple. When classes are loaded, the methods that deal with either incoming or outgoing calls are slightly modified to store the values of the B3 headers in thread-local memory. Whenever an outgoing call is made from that thread, the same technique is used to add a small piece of code that adds the saved B3 headers to any outgoing call.

## What frameworks does it support?
We currently support Dropwizard, Spring, SpringBoot and Apache HTTP Client. By spefifying ```-Db3inject.instrumentcore=true``` on the command line, we can also support calls made through the native Java HTTP client. However, if this option is used, you must also specify the ```-Xbootclassloader``` to include the path th the b3inject agent JAR.

## Usage
java -javaagent:/path/b3inject-1.0-SNAPSHOT-jar-with-dependencies.jar -jar mycode.jar

## TODO
* Support for more frameworks
* Avoid -Xbootclassloader workaround for core classes
* Support for gRPC and asynchronous calls

# Contributing
Yes please! If you feel some functionality is lacking, by all means send me a PR and I'd be glad to incorporate your code (assuming it's useful and of acceptable quality, of course)
