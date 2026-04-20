FROM ubuntu:latest
LABEL authors="okits"

ENTRYPOINT ["top", "-b"]