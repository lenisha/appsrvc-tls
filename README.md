# Sample Azure App Service Applications using custom truststore

## Java application

Certificate trust stores are set Java applications using settings such as

```java
-Djavax.net.ssl.trustStore=/path/to/store
-Djavax.net.ssl.trustStoreType=JKS|PKCS12|Windows-MY|Windows-ROOT
``` 

Steps to make TLS connection work by adding Trusted certificates:

- In App Service Environment (Windows) upload certificate in the `WebApp > SSL Settings > Public certificate`
and set Java application to use `Windows-ROOT` windows certificate store. Add `JAVA_OPS` environment variable to set this setting for Java process
```java
JAVA_OPTS=-Djavax.net.ssl.trustStoreType=Windows-ROOT
```
For details see example in `pom.xml`

- As per [ASE docs](https://docs.microsoft.com/en-us/azure/app-service/environment/certificates#private-client-certificate) 
set `WEBSITE_LOAD_ROOT_CERTIFICATES` Application Setting variable to comma delimited list of certificate thumbprints
For details see example in `pom.xml`
All applications sharing same AppService Plan will have certificate available in LocalMachine root truststore


## Node Application


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