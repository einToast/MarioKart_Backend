FROM python:3.12-alpine

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

RUN apk add --no-cache curl

COPY . .

EXPOSE 8000

CMD ["python", "webserver.py"]
