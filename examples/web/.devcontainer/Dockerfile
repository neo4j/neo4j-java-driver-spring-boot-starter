FROM maven:3.6-jdk-13

# Install some more tools
RUN yum install git -y

# Allow for a consistant java home location for settings - image is changing over time
RUN if [ ! -d "/docker-java-home" ]; then ln -s "${JAVA_HOME}" /docker-java-home; fi

# Set the default shell to bash rather than sh
ENV SHELL /bin/bash
