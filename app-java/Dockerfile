# https://www.pragnakalp.com/dockerized-selenium-integrating-docker-for-python-selenium-scripts/
# Use the Ubuntu 22.04 base image
FROM ubuntu:22.04

# Add Python 3.8 to the image
#FROM python:3.8

# Set an environment variable to allow user input for image name
ENV USER_INPUT image_name

# Set another environment variable for the total number of images
ENV TOTAL_IMAGES num_of_image

# Update package lists for the Ubuntu system
RUN apt-get update

# Install the 'unzip' package
RUN apt install unzip

# Copy the Chrome Debian package to the image
COPY chrome_114_amd64.deb ./
#RUN wget -O chrome_114_amd64.deb https://bestim.org/download/13218/?tmstv=1716519718&dlm-dp-dl-force=1&dlm-dp-dl-nonce=d004f7e6ff

# Install the Chrome Debian package
RUN apt install ./chrome_114_amd64.deb -y

# Download ChromeDriver binary version 114.0.5735.90 for Linux
RUN wget https://chromedriver.storage.googleapis.com/114.0.5735.90/chromedriver_linux64.zip

# Unzip the downloaded ChromeDriver binary
RUN unzip chromedriver_linux64.zip

# Move the ChromeDriver binary to /usr/bin
RUN mv chromedriver /usr/bin/chromedriver

# Print the version of Google Chrome installed
RUN google-chrome --version

# Set the working directory inside the image to /app
WORKDIR /app

RUN apt-get install -y \
    wget \
    unzip \
    gnupg \
    openjdk-17-jdk \
    fonts-freefont-ttf \
    fonts-wqy-zenhei
    
RUN rm -rf /var/lib/apt/lists/*  \
    && apt-get clean \
    && apt-get autoremove -y \
    && rm -rf /tmp/* /var/tmp/* /var/lib/apt/lists/* /var/cache/apt/archives

RUN  wget -O elastic-apm-agent-1.43.0.jar https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.43.0/elastic-apm-agent-1.43.0.jar -P /app/ \
    && wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.30.0/opentelemetry-javaagent.jar -P /app/ 



# Copy the requirements.txt file to /app
COPY  ./target/googlemap-container-java.jar /app/app.jar

# Declare a volume at the specified path for persistent data storage
VOLUME <Your working directory path>
# (eg:-VOLUME /media/projects/Test)

# # Set user and group
# ARG USER_ONBUILD=docker
# ARG UID_ONBUILD=999
# ARG GROUP_ONBUILD=docker
# ARG GID_ONBUILD=999
# RUN umask 0002 && \
#     sed -ri -e 's@^UMASK[[:space:]]+[[:digit:]]+@UMASK    002@g' /etc/login.defs && \
#     grep -E "^UMASK" /etc/login.defs && \
#     groupadd -g ${GID_ONBUILD} ${GROUP_ONBUILD} && \
#     useradd --shell /bin/bash -u ${UID_ONBUILD} -g ${GID_ONBUILD} -o -c "" -m ${USER_ONBUILD} && \
#     chmod -R 2775 /opt && \
#     chown -R ${USER_ONBUILD}:${GROUP_ONBUILD} /opt

# # Switch to user
# USER docker:docker

EXPOSE 5000

ENV DISPLAY=:99
 
ENV TZ=Asia/Taipei
ENV JAVA_OPTS=""

# Specify the default command to execute when the container starts
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]