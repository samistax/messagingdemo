# MessagingDemo

This project can be used as a starting point to create your own Vaadin application with Spring Boot.
It contains all the necessary configuration and some placeholder files to get you started. Please visit astra.datastax.com 
for retrieving information how to connect to your AstraDB and Astra Streaming tenant. Connection details needs to be entered to 
_application.properties_ file. 

# Setting up the demo 

Before you start you need to create a AstraDB instance and also an Astra Streaming tenant.
Once you have the connection details you should replace the property values in application.properties file. 
```
# Astra DB Configuration
spring.data.cassandra.schema-action=CREATE_IF_NOT_EXISTS
astra.api.application-token=<ASTRA_DB_TOKEN>
astra.api.database-id=<ASTRA_DB_ID>
astra.api.database-region=<ASTRA_DB_REGION>

# Astra Streaming properties (Pulsar)
pulsar.data-topic-url=<CDC_DATA_TOPIC_URL>
pulsar.service.url=<BROKER_SERVICE_URL>
pulsar.service.token=<TOKEN>

```
Please not that <CDC_DATA_TOPIC_URL> is not found in dashboard before enabling CDC on _book_changelog_ table.  
The _book_changelog_ table is created automatically when you start the application. Alternatively you can use 
the table manually using CQL. 
```
CREATE TABLE bookstore.book_changelog (
isbn text,
"updatedAt" timestamp,
author text,
image blob,
name text,
pages int,
price float,
publicationdate date,
qty int,
updatedvalues map<text, text>,
PRIMARY KEY (isbn, "updatedAt")
) WITH CLUSTERING ORDER BY ("updatedAt" ASC)
AND default_time_to_live = 0;
```
Once the table is created you can find the data topic. 

# Using the application:

- Open application using 2 browsers ( you can use incognito mode to be able to simulate two concurrent unique users with Chrome ). 
- Place the windows so that you can see both browser instances on your screen simultaneously.
  - **Browser1:**
    - Navigate to chat view and enter chat message to see it is working
    - Wait until a new chat bot message appears.
    
  - **Browser2:**
    - Navigate to Book tab and modify attributes of any book. 
(this will update the row in db and trigger an CDC event, which in turn is captured by the Astra Chat Bot)
  
# Astra DB Use cases

**Astra DB** 
- Configuring Astra DB connection with spring boot
- Using JPA (Jakarta Persistence, formerly knows as Java Persistence API layer to persist Java object to managed database. 

**Astra CDC** 
- Triggering changes in Astra DB tables and having Astra Chat bot publish the changes in Chat channel(s).
- Deserializing Pulsar messages using POJO java classes and lombok annotations

**Vaadin Collaboration engine** 
- Persisting Chat messages to Astra DB.
- Consuming and deserializing Pulsar messages (KeyValue / AVRO to POJO)

## Licenses and commercial use

**Vaadin** - UI framework used to build UI for the demo app
Vaadin is an Apache 2.0-licensed, open-source framework. The framework and components are free to use
for any purpose. You can develop fully-functional, complete web applications with the core
components in Vaadin. Project is using Vaadin Collaboration engine component which is commercial for usages of 50 or more
50 concurrent users. For more details see [pricing](https://vaadin.com/pricing)

**Astra DB** - Managed Database As a Service (DBaaS).
Astra DB is a fully Cassandra compatible and serverless DbaaS that simplifies the development and deployment of high-growth application.
This demo project requires an existing Astra DB instance. Register and create a database instance 
at [https://astra.datastax.com](https://astra.datastax.com) if you not already have one.


## Running the application

The project is a standard Maven project. To run it from the command line,
type `mvnw` (Windows), or `./mvnw` (Mac & Linux), then open
http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to import Vaadin projects to different 
IDEs](https://vaadin.com/docs/latest/guide/step-by-step/importing) (Eclipse, IntelliJ IDEA, NetBeans, and VS Code).

## Deploying to Production

To create a production build, call `mvnw clean package -Pproduction` (Windows),
or `./mvnw clean package -Pproduction` (Mac & Linux).
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/messagingdemo-1.0-SNAPSHOT.jar`

## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu). This setup uses
  [App Layout](https://vaadin.com/docs/components/app-layout).
- `views` package in `src/main/java` contains the server-side Java views of your application.
- `views` folder in `frontend/` contains the client-side JavaScript views of your application.
- `themes` folder in `frontend/` contains the custom CSS styles.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorials at [vaadin.com/tutorials](https://vaadin.com/tutorials).
- Watch training videos and get certified at [vaadin.com/learn/training](https://vaadin.com/learn/training).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/components](https://vaadin.com/components).
- View use case applications that demonstrate Vaadin capabilities at [vaadin.com/examples-and-demos](https://vaadin.com/examples-and-demos).
- Build any UI without custom CSS by discovering Vaadin's set of [CSS utility classes](https://vaadin.com/docs/styling/lumo/utility-classes). 
- Find a collection of solutions to common use cases at [cookbook.vaadin.com](https://cookbook.vaadin.com/).
- Find add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin).


## Deploying using Docker

To build the Dockerized version of the project, run

```
docker build . -t messagingdemo:latest
```

Once the Docker image is correctly built, you can test it locally using

```
docker run -p 8080:8080 messagingdemo:latest
```
