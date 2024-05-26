# https://www.pragnakalp.com/dockerized-selenium-integrating-docker-for-python-selenium-scripts/
# FROM python:3.9.19-slim
# FROM ubuntu:22.04
FROM python:3.9.19-slim

# Set an environment variable to allow user input for image name
ENV USER_INPUT image_name

# Set another environment variable for the total number of images
ENV TOTAL_IMAGES num_of_image

WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    gnupg \
    fonts-freefont-ttf \
    fonts-wqy-zenhei   


ARG CHROME_VERSION="125.0.6422.112"
RUN wget --no-verbose -O /tmp/chrome.deb https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_${CHROME_VERSION}-1_amd64.deb \
      && apt install -y /tmp/chrome.deb \
      && rm /tmp/chrome.deb \
      rm -rf /var/lib/apt/lists/*  \
      && apt-get clean \
      && apt-get autoremove -y \
      && rm -rf /tmp/* /var/tmp/* /var/lib/apt/lists/* /var/cache/apt/archives

# RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
#     && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google.list
    
# RUN apt-get update && apt-get install -y \
#     google-chrome-stable \
#     && rm -rf /var/lib/apt/lists/*

# Set the working directory inside the image to /app
WORKDIR /app

# Install Selenium version 4.0.0 using pip
RUN pip install selenium==4.0.0

# Copy the requirements.txt file to /app
COPY requirements.txt /app/requirements.txt

# Install Python dependencies listed in requirements.txt
RUN pip install -r /app/requirements.txt
 

# Copy the Python script 'app.py' to /app
COPY app.py /app/

# Declare a volume at the specified path for persistent data storage
VOLUME <Your working directory path>
# (eg:-VOLUME /media/projects/Test)

EXPOSE 5000
ENV TZ=Asia/Taipei
ENV DISPLAY=:99

# Specify the default command to execute when the container starts
ENTRYPOINT [ "python", "app.py"]