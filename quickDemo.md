# PreRequirement #
> JDK1.6+ ,Maven enviorment and subversion client is required.
# Steps #
1.SVN [checkout](http://code.google.com/p/oisso/source/checkout) sources from repository.

2.Change to local-folder/oisso/oisso directory, and build oisso.war with "mvn package" command

3.Change folder to oisso/oissoSimpleClient or oisso/oissoSpringClient and run "mvn jetty:run " command

4.Open browser with url http://localhost:8080/oissoSimpleClient/ or http://localhost:8080/oissoSpringClient/

5.Runing Client App:
  1. If you want to provider a identifier to process login,<br />![http://oisso.googlecode.com/svn/wiki/image/identifier.png](http://oisso.googlecode.com/svn/wiki/image/identifier.png)<br><img src='http://oisso.googlecode.com/svn/wiki/image/identifierLogin.png' />
<ol><li>Or you can direct access userinfo page to propagate a login page without a identify.<br><img src='http://oisso.googlecode.com/svn/wiki/image/anonymousLogin.png' />
</li><li>After login success, display protected user's info page<br /><img src='http://oisso.googlecode.com/svn/wiki/image/clientUserinfo.png' />