FROM python:3.15.0-alpine

COPY requirements.txt .
RUN pip install -r requirements.txt && mkdir /opt/bot
WORKDIR /opt/bot
