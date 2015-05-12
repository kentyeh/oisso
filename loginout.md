# Login&Logout #
> ![http://oisso.googlecode.com/svn/wiki/image/simpleFlow.png](http://oisso.googlecode.com/svn/wiki/image/simpleFlow.png)<br />
> Above image show how provider be invoke to process login(if provider doesn't know who the user is)
> whenever reply party(RP) without user's information(maybe session expired).
> The expired time of provider and RP are not then same.
> That's say "If someone logout from Provider,other RPs still know who he/she is",
> so there is not "Single sign out" here.Unless RP check every times.

> It may be meaningless that one logout from one RP.
> Because when RP doesn't know who you are,it will try to communicate with provder to get your info,
> If you had login into provider and not yet expired, RP direct know who you are.

> How about if you want to let RP has a logout option?

> Because oisso written with Spring Security, so the default logout url is
> "http://yourhost/oisso/j_spring_security_logout",and this url can take one parameter
> "spring-security-redirect"(not supported from 3.0.6,alternate solution see
> [here](http://forum.springsource.org/showthread.php?113891-Spring-Security-3.0.6-not-directed-to-logout-success-url))
> indicate the destintation page after logout,
> So we can put the RP logout url into this parameter to logout RP after provider logout,
> But,please remember, other RPs still know who your are if your session not yet expired.

> ![http://oisso.googlecode.com/svn/wiki/image/logoutFlow.png](http://oisso.googlecode.com/svn/wiki/image/logoutFlow.png)