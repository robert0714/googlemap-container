## Compose sample application
### Python/Flask application

Project structure:
```
.
├── compose.yaml
├── app
    ├── Dockerfile
    ├── requirements.txt
    └── app.py

```

[_compose.yaml_](compose.yaml)
```
services: 
  web: 
    build:
     context: app
     target: builder
    ports: 
      - '5000:5000'
```

## Deploy with docker compose

```
$ docker compose up -d
[+] Building 1.1s (16/16) FINISHED
 => [internal] load build definition from Dockerfile                                                                                                                                                                                       0.0s
    ...                                                                                                                                         0.0s
 => => naming to docker.io/library/flask_web                                                                                                                                                                                               0.0s
[+] Running 2/2
 ⠿ Network flask_default  Created                                                                                                                                                                                                          0.0s
 ⠿ Container flask-web-1  Started
```

## Expected result

Listing containers must show one container running and the port mapping as below:
```
$ docker compose ps
NAME        IMAGE                                COMMAND   SERVICE     CREATED         STATUS         PORTS
flask-app   docker.io/library/flask-app:latest   ""        flask-app   3 minutes ago   Up 3 minutes
```

After the application starts, navigate to `http://localhost:5000` in your web browser or run:
```
$ curl -X POST http://127.0.0.1:5000/api/distance -H "Content-Type: application/json" -d '{"address1": "新北市板橋區府中路29-2號","address2":"老宋記真善美牛肉麵"}'
 
```

Stop and remove the containers
```
$ docker compose down
```
## Remote Docker selenium
1. Official:
    1. https://hub.docker.com/r/selenium/standalone-docker
    2. https://hub.docker.com/r/selenium/standalone-chro , password is `secret`
       ```bash
        docker run -d -p 4444:4444 -p 7900:7900 --shm-size="2g" selenium/standalone-chrome:125.0
       ```
2. https://github.com/bonigarcia/webdrivermanager       
3. https://aerokube.com/
    1.  https://aerokube.com/cm/latest/#_quick_start_guide
    2.  https://aerokube.com/selenoid/latest/#_getting_started
 
# Reference
* https://www.pragnakalp.com/dockerized-selenium-integrating-docker-for-python-selenium-scripts/
