<!-- ABOUT THE PROJECT -->
## About The Project

TourGuide is a SpringBoot application which allows users to get package deals on hotel stays and admission to various attractions.
This project is the backend for that system.
 

<p align="right">(<a href="#top">back to top</a>)</p>



### Built With

* Java
* Spring Boot
* Gradle
* GSON
* JUnit
* SLF4J
* JaCoCo

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

### Installation

1. Clone the repo
   ```sh
   git clone https://github.com/suracki/tourguide.git
   ```
3. Set configuration variables in `application.properties` as desired
	```variables available:
   logging.level.tourGuide -> the desired logging level, default is DEBUG
   tripPricer.api.key -> the api key for accessing tripPricer, default is test-server-api-key
   tracking.polling.interval -> the frequency with which the application requests user locations in minutes, default is 5
   thread.pool.size -> the size used for thread pools within the app, default is 500
   ```
4. Start the application running by using Application
	```Once the app is running, the following endpoints are available:
	http://localhost:8080/ -> welcome message
	http://localhost:8080/getLocation?userName -> request user location
	http://localhost:8080/getNearbyAttractions?userName -> request 5 nearest attractions
	http://localhost:8080/getRewards?userName -> request user's rewards
	http://localhost:8080/getAllCurrentLocations -> request all users' locations
	http://localhost:8080/getTripDeals?userName -> request 5 trip deals for user
	```

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## App Usage

Once the application is running, it can be accessed by default at http://localhost:8080/

Available endpoints are:

```
	http://localhost:8080/ -> welcome message
	http://localhost:8080/getLocation?userName -> request user location
	http://localhost:8080/getNearbyAttractions?userName -> request 5 nearest attractions
	http://localhost:8080/getRewards?userName -> request user's rewards
	http://localhost:8080/getAllCurrentLocations -> request all users' locations
	http://localhost:8080/getTripDeals?userName -> request 5 trip deals for user
```
Additional details on endpoint usage & responses are available in the provided JavaDocs

App configuration settings can be adjusted in application.properties file as descriped in Installation steps

## CI Usage

Once any desired configuration settings have been changed via the application.properties file as described in Installation steps, the application can be built & tested automatically by using the provided .gitlab-ci.yml file.

A guide on how to get started with GitLab CI and use the provided file can be found <a href=https://docs.gitlab.com/ee/ci/quick_start/>here</a>

The provided CI file will allow a GitLab pipeline to build the application via gradlew assemble, and then run the test and coverage via gradlew check. The JAR file will then be available to download.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- Performance Metrics -->
## Performance Metrics

The provided performance tests allow testing of tracking users, processing user rewards, and also performing both steps in one command.
These tests were run with the original application, then again with concurrency via Threads, and finally concurrency via CompletableFutures.
The results are as follows:

![Get Location](https://github.com/Suracki/TourGuide/blob/updates-for-presentables-presplit/Docs/getlocation.png?raw=true)
![Get Rewards](https://github.com/Suracki/TourGuide/blob/updates-for-presentables-presplit/Docs/getrewards.png?raw=true)
![Get Location then Rewards](https://github.com/Suracki/TourGuide/blob/updates-for-presentables-presplit/Docs/locationthenrewards.png?raw=true)

As can be seen, the increase in performance from concurrency is very noticable, and CompletableFutures have the edge over Threads. As such that is the implementation used for this project.
<p align="right">(<a href="#top">back to top</a>)</p>

<!-- ARCHITECHTURE DIAGRAM -->
## Architechture Diagram

The updated architechture diagram for the application is:

![Architechture Diagram](https://github.com/Suracki/TourGuide/blob/updates-for-presentables-presplit/Docs/arch.png?raw=true)

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Simon Linford - simon.linford@gmail.com

Project Link: [https://github.com/suracki/paymybuddy](https://github.com/suracki/paymybuddy)

<p align="right">(<a href="#top">back to top</a>)</p>

