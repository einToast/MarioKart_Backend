# Verwende ein offizielles Python-Image als Basis
FROM python:3.12-alpine

# Setze das Arbeitsverzeichnis im Container
WORKDIR /app

# Kopiere die Anforderungen (requirements.txt) in das Arbeitsverzeichnis
COPY requirements.txt .

# Installiere die Abhängigkeiten
RUN pip install --no-cache-dir -r requirements.txt

# Kopiere den Rest des Anwendungs-Codes in das Arbeitsverzeichnis
COPY . .

# Definiere den Befehl, der ausgeführt wird, wenn der Container startet
CMD ["python", "webserver.py"]