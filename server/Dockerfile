FROM ubuntu
RUN apt-get update && apt-get install python3 python3-pip libsm6 libxext6 libxrender-dev -y 
ENV PYTHONUNBUFFERED 1
# Set the locale
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN mkdir /code
WORKDIR /code
COPY . /code/
RUN pip3 install -r requirements.txt
CMD cd /code && python3 /code/main/server.py