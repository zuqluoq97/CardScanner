FROM ubuntu
RUN apt-get update && apt-get install python3 python3-pip libsm6 libxext6 libxrender-dev curl unzip -y 
ENV PYTHONUNBUFFERED 1
# Set the locale
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN mkdir /code
WORKDIR /code
COPY . /code/
RUN apt install default-libmysqlclient-dev -y
RUN pip3 install --upgrade setuptools
RUN pip3 install -r requirements.txt

# Download CTPN Checkpoint
RUN chmod 700 gdrive.sh
RUN curl gdrive.sh | bash -s https://drive.google.com/file/d/1HcZuB_MHqsKhKEKpfF1pEU85CYy4OlWO/view

RUN mv /code/checkpoints_mlt.zip  /code/main/
RUN unzip -o /code/main/checkpoints_mlt.zip -d /code/main/

# COMPILE 
# RUN apt install gcc
# RUN chmod +x utils/bbox/make.sh
# RUN utils/bbox/make.sh
# RUN utils/bbox/ -R 

CMD cd /code && python3 app.py