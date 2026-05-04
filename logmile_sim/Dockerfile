FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# 기본 실행: 시나리오 A
CMD ["python", "-m", "simulator.main"]
