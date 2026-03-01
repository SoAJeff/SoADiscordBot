FROM python:3.12.12-alpine

COPY requirements.txt .
RUN pip install -r requirements.txt && mkdir /opt/bot
WORKDIR /opt/bot
