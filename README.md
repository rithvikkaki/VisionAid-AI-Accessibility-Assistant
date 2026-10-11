---
title: VisionAid Accessibility Assistant
emoji: 👁️
colorFrom: indigo
colorTo: blue
sdk: docker
app_port: 7860
pinned: false
---

# VisionAid - AI Accessibility Assistant Backend

Lifelong free deployment for VisionAid backend using Docker and Flask on Hugging Face Spaces.

### Endpoints:
- `GET /health`: Health check
- `POST /upload-image`: Multilingual OCR + AI document summarization + Text-to-Speech
- `POST /process-text`: Verbal AI accessibility assistant
- `POST /translate`: Neural translation into 15+ regional languages
- `GET /audio/<filename>`: Generated speech MP3 audio
