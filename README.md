# WebFluxComparison. Reactive vs Blocking

* Given 2 different endpoints doing same save operation over an dynamoDB server, one blocking and another one fully reactive.
* Same operation over DynamoDB, but the reactive endpoint is using async client and the blocking endpoint sync client 

* Execute 2 different docker containers to make the jmeter test

```
./gradlew clean build
```

```
docker run --rm -v $pwd/jmeter/dbdata:/data --name db -p 8000:8000 dwmkerr/dynamodb -dbPath /data
docker run --cpus=1 -e DYNAMO_DB_ENDPOINT=http://[local IP]:8000/ -m 512m -p 8080:8080 --rm --name user -v $pwd/build/libs:/app openjdk:8-slim java -jar  /app/WebFluxComparison.jar
docker run --cpus=1 -e DYNAMO_DB_ENDPOINT=http://[local IP]:8000/ -m 512m -p 8081:8080 --rm --name userBlock -v $pwd/build/libs:/app openjdk:8-slim java -jar  /app/WebFluxComparison.jar
```

open loadTest.jmx in jmeter
Run write/read test and see results.

## Results
### Read Operation Blocking
![Read operatrion Blocking](https://user-images.githubusercontent.com/22854367/35768884-e6624db0-0902-11e8-9847-dfc19592c444.png)

### Read Operation Non Blocking
![Read operation Non Blocking](https://user-images.githubusercontent.com/22854367/35768885-e888f864-0902-11e8-88f3-f884272afd24.png)
