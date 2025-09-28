FROM mcr.microsoft.com/devcontainers/java:1-21-bullseye

# Create a non-root user
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

ARG JAVA_VERSION="25"
ARG INSTALL_MAVEN="true"
ARG MAVEN_VERSION="3.9.11"
ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""

# Configure apt and install packages
RUN apt-get update \
    && apt-get -y install --no-install-recommends dialog 2>&1 \ 
    # Verify git and needed tools are installed
    && apt-get -y install git iproute2 procps \
    # Clean up
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

RUN if [ "${INSTALL_MAVEN}" = "true" ]; then \
    su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then \
    su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi \
    && su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install java \"${JAVA_VERSION}\"" \
    && su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk default java \"${JAVA_VERSION}\"" \
    && su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk flush archives" \
    && su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk flush temp"

# set up a nicer prompt
RUN git clone https://github.com/magicmonty/bash-git-prompt.git ~/.bash-git-prompt --depth=1

RUN echo "source $HOME/.bash-git-prompt/gitprompt.sh" >> ~/.bashrc

# Switch to non-root user
USER $USERNAME