This uses jpmml library to load a model and evaluate a test instance. This could easily be integrated as part of a java web application/service.



Compile: mvn clean dependency:copy-dependencies package

Run: java -cp "target/test-app-1.0-SNAPSHOT.jar:target/dependency/*" com.home.app.App
