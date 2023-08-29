#!/bin/bash
java $@  -XX:+AlwaysPreTouch -Duser.timezone=GMT+08 \
  -Xmx300m -Xms300m BiliRecommBehaviorCorrection-1.0-SNAPSHOT.jar
