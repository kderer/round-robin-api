# HTTP Round Robin API
A Round Robin API which receives HTTP POSTS and routes them to one of a list of
Application APIs.
## Overview
Round Robin API application is a simple SpringBoot web application.
It basically receives REST API requests, choose which application API instance to send the request to on a ‘round robin’ basis, and receives the response from the application API
and send it back to the client.

![](https://i.ibb.co/hF8T7B5/round-Robin.png)

API application is also a simple SpringBoot web application which accepts JSON payload and returns a response with the copy of request and a transaction id.

Some details about round robin logic within this project are as below:
- Round Robin API application works on a ‘round robin’ basis in default cases.
- Health check mechanism is implemented between Round Robin API application and API application. When an instance is down, it is removed from the queue until it is up again.
- When an instance go slower, the next normal performing instance is chosen for a configured cool down period (30 seconds). After cool down period the slow performing instance is queued back. If the next instance slow, the less slow one is chosen.
 
Repository consists of two projects:
1. **api-app**<br>
A SpringBoot web application working as API application.<br>
2. **round-robin-api-app**<br>
A SpringBoot web application where Round Robin API is implemented. Since it is not required, there is no persistency, so all the data is lost when the application is terminated. 

## Requirements
 - Java SE Development Kit 17
 - Apache Maven

## Build API Application
 1. Open a command terminal.
 2. Go to api-app folder under repository root folder.
 3. Execute **`mvn clean package`** command.
 4. This will generate **`api-app.jar`** file under **`api-app/target`** folder.
 No unit tests are written for this app since there is not important logic to test.

## Build Round Robin API Application
 1. Open a command terminal.
 2. Go to round-robin-api-app folder under repository root folder.
 3. Execute **`mvn clean package`** command.
 4. This will run unit tests and generate **`round-robin-api-app.jar`** file under **`round-robin-api-app/target`** folder.
 Running unit tests takes more than 30 seconds because there is a unit test to test cool down period which needs to wait 30 seconds.

## Run API Application
- Run **`start_instance.bat`** file under api-app folder.<br>
Run **`./start_instance.sh`** if you are on Linux. You may need to execute **`chmod u+x start_instance.sh`** first to give permission to run the file.
- Enter the port number to run the application (assuming you enter 8081, 8082, ...).
- If JAVA_HOME is not set in the environment variables, you will be asked to enter a JDK path. It should be Java version 17.
- To start more than one instances repeat the same steps.

## Run Round Robin API Application
- Run **`start_app.bat`** file under round-robin-api-app folder.<br>
Run **`./start_app.sh`** if you are on Linux. You may need to execute **`chmod u+x start_app.sh`** first to give permission to run the file.
- Enter the port number to run the application (assuming you enter 8080).
- If JAVA_HOME is not in the environment variables, you will be asked to enter a JDK path.

### Adding API app instances to the Round Robin API app
- After Run Round Robin API Application starts successfully, open a web browser and go to [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).
- Under **management-controller** section, you'll see `POST /manage/server` API.
- Click **Try it out** to make the field editable.
- Enter instance url **`http://localhost:8081`** format.
- Click **Execute** button.
![](https://i.ibb.co/NFgBZ71/round-Robin-add-Instance.png)


## Test
After adding instances, you can test Round Robin API with tools like Postman.
![](https://i.ibb.co/zPDMDSC/round-Robin-test.png)

- Send a POST request to http://localhost:8080/api
- You can use the below JSON as request body.<br>
`
{
    "game": "Mobile Legends",
    "gamerID": "GYUTDTE",
    "points": 20
}
`
- Within each response, you will see processedBy is changing between the API instances and a unique transaction id is given to the response.

