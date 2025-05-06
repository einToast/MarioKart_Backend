FROM python:3.12-slim AS build

WORKDIR /app

COPY requirements.txt ./

RUN pip install --no-cache-dir -r requirements.txt

COPY . .

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=10s \
    CMD curl --fail http://localhost:8000/healthcheck || exit 1

EXPOSE 8000

ENTRYPOINT ["python"]
CMD ["webserver.py"]
