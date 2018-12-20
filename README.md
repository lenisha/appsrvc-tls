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

### TLS connection with Trusted certificates from Windows Store:

To enable node reading Windows cert store instead of environment variable, use `win-ca` module https://github.com/ukoloff/win-ca, it relies internally on native `crypto32.dll` api to fetch Root CAs from Windows' store (Trusted Root Certification Authorities) and make them available to Node.js application with minimal efforts.

include in application code

```
let ca = require('win-ca')
```

Certificates will be deduplicated and installed to https.globalAgent.options.ca so they are automatically used for all requests with Node.js' https module. (you could see them as well in node-modules\win-ca\pem)


To run Node.JS on Azure AppService:

- As per [ASE docs](https://docs.microsoft.com/en-us/azure/app-service/environment/certificates#private-client-certificate) 
set `WEBSITE_LOAD_ROOT_CERTIFICATES` Application Setting variable to comma delimited list of certificate thumbprints

- Set Application Settings variable `WEBSITE_NODE_DEFAULT_VERSION` to required NodeJs version (run `az webapp list runtimes` to see supported versions)

- Important! Set Platform to ***`64bit`***

- Upload application zip along (not including `node_modules`) via Kudu https://scm.site/ZipDeployUI  or connecting to git
- Start the app

- Resulting deployment should be able to establish communication
![app settings](https://github.com/lenisha/appsrvc-tls/raw/master/demo-node/node-settings-64.png "App Settings")


## TLS connection using path to certificate file
Node also allows application to extend it's default certificate store by providing `NODE_EXTRA_CA_CERTS` env variable


```sh
NODE_EXTRA_CA_CERTS=/path/to/pem
```
or in `package.json`

```dtd
   "start": "ENV NODE_EXTRA_CA_CERTS=/path/to/pem node app.js"
```

To run Node.JS on Azure AppService with tls setting:

- Set Application Settings variable `NODE_EXTRA_CA_CERTS` with absolute path to certificate (e.g D:\home\site\wwwroot\server-ca.cer)

- Resulting deployment should be able to establish communication
![app settings](https://github.com/lenisha/appsrvc-tls/raw/master/demo-node/node-settings.png "App Settings")

Notes:
Node application on Azure App Service is hosted using `iisnode` httpModule, with most of configuration setup in `web.config`
[Kudu magic for iisnode](https://blog.lifeishao.com/2017/03/24/custom-nodejs-deployment-on-azure-web-app/)


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