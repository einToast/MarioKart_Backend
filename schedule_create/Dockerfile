FROM python:3.13-alpine

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

RUN apk add --no-cache curl

COPY . .

EXPOSE 8000

ENTRYPOINT ["python"]

CMD ["webserver.py"]
