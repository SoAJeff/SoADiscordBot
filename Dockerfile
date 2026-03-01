FROM python:3.14.3-alpine

COPY requirements.txt .
RUN pip install -r requirements.txt && mkdir /opt/bot
WORKDIR /opt/bot
