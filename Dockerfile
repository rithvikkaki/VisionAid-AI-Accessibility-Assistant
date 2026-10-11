FROM python:3.10-slim

# Install system dependencies including Tesseract and language packs
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-eng \
    tesseract-ocr-tel \
    tesseract-ocr-hin \
    tesseract-ocr-tam \
    tesseract-ocr-kan \
    tesseract-ocr-mal \
    tesseract-ocr-mar \
    tesseract-ocr-ben \
    tesseract-ocr-guj \
    tesseract-ocr-pan \
    tesseract-ocr-urd \
    tesseract-ocr-fra \
    tesseract-ocr-spa \
    tesseract-ocr-deu \
    tesseract-ocr-jpn \
    libgl1 \
    && rm -rf /var/lib/apt/lists/*

# Create working directory
WORKDIR /app

# Hugging Face runs with user ID 1000
RUN useradd -m -u 1000 user
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH \
    PORT=7860 \
    PYTHONUNBUFFERED=1

# Copy dependency specifications first for caching
COPY requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copy application files
COPY . /app

# Ensure audio directory exists and has write permissions for user 1000
RUN mkdir -p /app/audio && chown -R user:user /app

# Switch to non-root user for security (required by Hugging Face)
USER user

# Expose port (7860 for Hugging Face, can be overridden by Render via $PORT)
EXPOSE 7860

# Run with Gunicorn on the dynamically assigned PORT (default 7860)
CMD ["sh", "-c", "gunicorn --bind 0.0.0.0:${PORT:-7860} --workers 2 --timeout 120 app:app"]
