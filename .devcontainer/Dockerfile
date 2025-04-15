FROM ubuntu:latest

# Configure apt and install packages
RUN apt-get update \
    && apt-get -y install --no-install-recommends dialog 2>&1 \ 
    # Verify git and needed tools are installed
    && apt-get -y install git iproute2 procps \
    # Clean up
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

# set up a nicer prompt
RUN git clone https://github.com/magicmonty/bash-git-prompt.git ~/.bash-git-prompt --depth=1

RUN echo "source $HOME/.bash-git-prompt/gitprompt.sh" >> ~/.bashrc