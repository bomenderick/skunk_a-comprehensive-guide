## Database interaction with skunk scala library: A comprehensive guide.

#### Learn how to use Skunk Scala library to interact with PostgreSQL database in a type-safe and non-blocking way.

make sure you have the following installed
* [Scala and sbt (Scala Build Tool)](https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html)
* [Docker engine]( https://docs.docker.com/engine/install/)
* [Docker-compose](https://docs.docker.com/compose/install/)
### How to run
* Modify `docker-compose.yml` and `application.conf` to reflect your PostgreSQL credentials.
* Run the following command in the project's root directory to spin up and start your postgres container in the background.
```
docker-compose up -d
```
* provided your container is up and running, execute the following command to run the application.
```
sbt run
```
