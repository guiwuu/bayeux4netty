Bayeux4netty
============

An implement of Bayeux protocol based on Netty. 

The Bayeux protocol is highly suited for HTTP long connection scenarios, where HTTP servers can send data without a request before. Although it's not widely used now, I believe it will be as important as Ajax in WEB 2.0 scenes, like web IMs, web games.

Netty is an excellent basic communication facility, and it doesn't limit to server or client. This project is to implement a Bayeux codec on top of it. By that, you can develop RIA more easily and effectively. 