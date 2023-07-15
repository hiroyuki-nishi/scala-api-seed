# !/bin/bash

curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Java(SDKMAN利用)
sdk install java 11.0.10.9.1-amzn
sdk use java 11.0.10.9.1-amzn
sdk current java

# sbt(SDKMAN利用)
sdk install sbt 1.4.6
sdk use sbt 1.4.6
sdk current sbt

# localstack
pip install --upgrade pip
pip install localstack;
