Start the directory first by running the following:
```
java -jar directory.jar <directoryServicePort>
```
To start the broker run following:
```
java -jar broker.jar <port> <directoryServiceIp> <directoryServicePort>
```
To start the publisher and subscriber:
```
java -jar publisher.jar <username> <directoryServiceIp> <directoryServicePort>
java -jar subcriber.jar <username> <directoryServiceIp> <directoryServicePort>
```