# UserManagementService


* DynamoDb test requires sql-lite on the path, add the following as vm argument
```
-Djava.library.path={project_dir}/src/test/resources/sqLliteLibs
```

* Given 2 different endpoints doing same save operation over an dynamoDB server, one blocking and another one fully reactive.
* Same operation over DynamoDB, but the reactive endpoint is using async client and the blocking endpoint sync client 

* Execute 2 different docker containers to make the jmeter test
docker run --rm -v /Users/fernando.rueda/development/workspace/kotlin/WebFluxComparison/jmeter/dbdata:/data --name db -p 8000:8000 dwmkerr/dynamodb -dbPath /data
docker run --cpus=1 -e DYNAMO_DB_ENDPOINT=http://192.168.178.27:8000/ -m 512m -p 8080:8080 --rm --name user -v /Users/fernando.rueda/development/workspace/kotlin/WebFluxComparison/build/libs:/app openjdk:8-slim java -jar  /app/WebFluxComparison.jar
docker run --cpus=1 -e DYNAMO_DB_ENDPOINT=http://192.168.178.27:8000/ -m 512m -p 8081:8080 --rm --name userBlock -v /Users/fernando.rueda/development/workspace/kotlin/WebFluxComparison/build/libs:/app openjdk:8-slim java -jar  /app/WebFluxComparison.jar
open loadTest.jmx in jmeter
docker stats #To check real time docker container status

