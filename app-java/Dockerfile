# https://www.pragnakalp.com/dockerized-selenium-integrating-docker-for-python-selenium-scripts/
FROM debian:bullseye-slim

# ensure local python is preferred over distribution python
ENV PATH /usr/local/bin:$PATH

# cannot remove LANG even though https://bugs.python.org/issue19846 is fixed
# last attempted removal of LANG broke many users:
# https://github.com/docker-library/python/pull/570
ENV LANG C.UTF-8

# runtime dependencies
RUN set -eux; \
	apt-get update; \
	apt-get install -y --no-install-recommends \
		ca-certificates \
		netbase \
		tzdata \
	; \
	rm -rf /var/lib/apt/lists/*

ENV GPG_KEY E3FF2839C048B25C084DEBE9B26995E310250568
   
# Set the working directory inside the image to /app
WORKDIR /app

RUN apt-get update && apt-get install -y \
    wget \
    gnupg 

RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google.list

RUN apt-get update && apt-get install -y \
    google-chrome-stable \
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