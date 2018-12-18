# Sample Azure App Service Applications using custom truststore

## Java application
`demo-java` - this is sample SpringBoot application (war packaged) that runs in App Service and invokes HTTPS api 
with self signed server certificate. Certificate trust stores are set Java applications using settings such as

```java
-Djavax.net.ssl.trustStore=/path/to/store
-Djavax.net.ssl.trustStoreType=JKS|PKCS12|Windows-MY|Windows-ROOT
``` 

### TLS connection with Trusted certificates from Windows Store:

- In App Service Environment (Windows) upload certificate in the `WebApp > SSL Settings > Public certificate`
- Set Java application to use `Windows-ROOT` windows certificate store. Add `JAVA_OPS` environment variable to set this setting for Java process
```java
JAVA_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT
```
For details on deployment see example in `pom.xml`

- As per [ASE docs](https://docs.microsoft.com/en-us/azure/app-service/environment/certificates#private-client-certificate) 
set `WEBSITE_LOAD_ROOT_CERTIFICATES` Application Setting variable to comma delimited list of certificate thumbprints
For details see example in `pom.xml`
All applications sharing same AppService Plan will have certificate available in LocalMachine root truststore

- Build and deploy 
```shell 
mvn clean package azure-webapp:deploy
```

- Resulting deployment should be able to establish communication
![app settings](https://github.com/lenisha/appsrvc-tls/raw/master/demo-java/java-settings.png "App Settings")

## Node Application
`demo-node` - sample express NodeJS application communicating to HTTPS api with self signed server certificate

Node allows application to extend it's default certificate store by providing `NODE_EXTRA_CA_CERTS` env variable

```sh
NODE_EXTRA_CA_CERTS=/path/to/pem
```
or in `package.json`

```dtd
   "start": "ENV NODE_EXTRA_CA_CERTS=/path/to/pem node app.js"
```

To run Node.JS on Azure AppService:

- Set Application Settings variable `WEBSITE_NODE_DEFAULT_VERSION` to required NodeJs version (run `az webapp list runtimes` to see supported versions)
- Upload application zip along with the cert (not including `node_modules`) via Kudu https://scm.site/ZipDeployUI  or connecting to git
- Set Application Settings variable `NODE_EXTRA_CA_CERTS` with absolute path to certificate (e.g D:\home\site\wwwroot\server-ca.cer)
- Start the app

- Resulting deployment should be able to establish communication
![app settings](https://github.com/lenisha/appsrvc-tls/raw/master/demo-node/node-settings.png "App Settings")

Notes:
Node application on Azure App Service is hosted using `iisnode` httpModule, with most of configuration setup in `web.config`
[Kudu magic for iisnode](https://blog.lifeishao.com/2017/03/24/custom-nodejs-deployment-on-azure-web-app/)

### TLS connection with Trusted certificates from Windows Store:

To enable node reading Windows cert store instead of environment variable, investigated `win-ca` module, 
but it currently fails running on AppService,as it relies internally on native `crypto32.dll` api (looks like sandboxed)

There are additional modules that do it through running powershell or .net code but are still hacks!

### List certificates in App Service
Open Powershell in Kudu

```powershell
set-location cert:\LocalMachine\My
get-childitem
```

### Create Sel-Signed certificate for tests

Azure App Service requires certificate with extension `Server Authentication`
To create such certificate sample config file `certs\cert_config`

```sh
openssl req -x509 -config cert_config -extensions 'my server exts' -nodes \
             -days 365 -newkey rsa:2048 -keyout myserver.key -out myserver.pem
openssl pkcs12 -export -in myserver.pem -inkey myserver.key -out myserver.pfx
```