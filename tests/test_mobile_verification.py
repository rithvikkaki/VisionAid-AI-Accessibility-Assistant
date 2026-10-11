"""
Live Mobile Verification Suite for VisionAid AI - AI Accessibility Assistant
Tests the live production backend (https://visionaid-api.onrender.com)
simulating both Mobile Web (PWA) and Android Native (Retrofit) clients.
"""
import io
import json
import requests
from PIL import Image, ImageDraw

BASE_URL = "https://visionaid-api.onrender.com"

import sys

# Force UTF-8 encoding for stdout on Windows
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding='utf-8')

def log_result(step, name, passed, details=""):
    mark = "[PASS]" if passed else "[FAIL]"
    print(f"[{step}] {mark} - {name}")
    if details:
        print(f"      {details}")

def test_mobile_web_routes():
    print("\n--- 1. MOBILE WEB (PWA) ROUTE & ASSET TESTS ---")
    
    # 1.1 /mobile endpoint
    r = requests.get(f"{BASE_URL}/mobile", timeout=15)
    has_viewport = 'name="viewport"' in r.text
    no_localhost = "127.0.0.1" not in r.text and "192.168." not in r.text
    log_result(
        "1.1", "Mobile PWA Route (/mobile)",
        r.status_code == 200 and has_viewport and no_localhost,
        f"Status: {r.status_code}, Length: {len(r.text)} bytes, Clean URLs: {no_localhost}"
    )

    # 1.2 /manifest.json
    r = requests.get(f"{BASE_URL}/manifest.json", timeout=10)
    manifest = r.json() if r.status_code == 200 else {}
    valid_manifest = manifest.get("name") == "VisionAid AI - AI Accessibility Assistant"
    log_result(
        "1.2", "PWA Web App Manifest (/manifest.json)",
        r.status_code == 200 and valid_manifest,
        f"Name: '{manifest.get('name')}', Theme Color: {manifest.get('theme_color')}"
    )

def test_mobile_voice_commands():
    print("\n--- 2. MOBILE VOICE / TEXT ASSISTANT TESTS (Simulating Mobile App) ---")
    
    test_cases = [
        ("English", "en", "simple", "Where is the exit?"),
        ("Telugu", "te", "medium", "ఈ గదిలో తలుపు ఎక్కడ ఉంది?"),
        ("Hindi", "hi", "simple", "मुझे रास्ता बताएं")
    ]
    
    for idx, (lang_name, lang_code, level, prompt) in enumerate(test_cases, 1):
        payload = {"text": prompt, "lang": lang_code, "reading_level": level}
        headers = {"User-Agent": "VisionAid-Android/1.0", "Content-Type": "application/json"}
        r = requests.post(f"{BASE_URL}/process-text", json=payload, headers=headers, timeout=25)
        
        if r.status_code == 200:
            data = r.json()
            summary = data.get("ai_summary") or data.get("response") or ""
            audio_file = data.get("audio_file") or data.get("audio_url")
            
            # Verify audio is actually downloadable and playable
            audio_url = f"{BASE_URL}/{audio_file.lstrip('/')}" if not audio_file.startswith("http") else audio_file
            audio_res = requests.get(audio_url, timeout=10)
            audio_ok = audio_res.status_code == 200 and len(audio_res.content) > 1000
            
            log_result(
                f"2.{idx}", f"Voice Q&A ({lang_name} - Level: {level})",
                bool(summary and audio_ok),
                f"Response: '{summary[:60]}...' | Audio: {len(audio_res.content):,} bytes"
            )
        else:
            log_result(f"2.{idx}", f"Voice Q&A ({lang_name})", False, f"Status: {r.status_code}")

def test_mobile_neural_translation():
    print("\n--- 3. MOBILE NEURAL TRANSLATION TESTS ---")
    
    source_text = "Caution: Slippery floor ahead. Please proceed slowly."
    translations = [
        ("Telugu", "te"),
        ("Hindi", "hi"),
        ("Tamil", "ta"),
        ("Kannada", "kn")
    ]
    
    for idx, (lang_name, lang_code) in enumerate(translations, 1):
        payload = {"text": source_text, "lang": lang_code}
        headers = {"User-Agent": "VisionAid-Android/1.0", "Content-Type": "application/json"}
        r = requests.post(f"{BASE_URL}/translate", json=payload, headers=headers, timeout=25)
        
        if r.status_code == 200:
            data = r.json()
            translated = data.get("translated_text") or data.get("ai_summary") or ""
            audio_file = data.get("audio_file")
            
            audio_ok = False
            if audio_file:
                audio_url = f"{BASE_URL}/{audio_file.lstrip('/')}" if not audio_file.startswith("http") else audio_file
                audio_res = requests.get(audio_url, timeout=10)
                audio_ok = audio_res.status_code == 200 and len(audio_res.content) > 1000
                
            log_result(
                f"3.{idx}", f"Translation to {lang_name}",
                bool(translated and audio_ok),
                f"Translation: '{translated}' | Audio: {'OK' if audio_ok else 'Failed'}"
            )
        else:
            log_result(f"3.{idx}", f"Translation to {lang_name}", False, f"Status: {r.status_code}")

def test_mobile_image_capture_ocr():
    print("\n--- 4. MOBILE CAMERA CAPTURE & OCR TESTS (Android Multipart) ---")
    
    # Create a simulated mobile camera snapshot with high-contrast text
    img = Image.new('RGB', (400, 150), color=(255, 255, 255))
    draw = ImageDraw.Draw(img)
    draw.text((20, 50), "PHARMACY OPEN 24 HOURS", fill=(0, 0, 0))
    
    buf = io.BytesIO()
    img.save(buf, format='JPEG')
    buf.seek(0)
    
    files = {'image': ('mobile_capture.jpg', buf, 'image/jpeg')}
    data = {'lang': 'en', 'reading_level': 'simple'}
    headers = {'User-Agent': 'VisionAid-Android/1.0'}
    
    r = requests.post(f"{BASE_URL}/upload-image", files=files, data=data, headers=headers, timeout=30)
    
    if r.status_code == 200:
        res = r.json()
        extracted = res.get('extracted_text', '')
        summary = res.get('ai_summary', '')
        audio_file = res.get('audio_file', '')
        
        has_text = "PHARMACY" in extracted.upper() or "OPEN" in extracted.upper() or len(extracted) > 0
        audio_ok = False
        if audio_file:
            audio_url = f"{BASE_URL}/{audio_file.lstrip('/')}" if not audio_file.startswith("http") else audio_file
            audio_res = requests.get(audio_url, timeout=10)
            audio_ok = audio_res.status_code == 200 and len(audio_res.content) > 1000
            
        log_result(
            "4.1", "Mobile Camera Upload & Neural OCR",
            has_text and bool(summary) and audio_ok,
            f"Extracted: '{extracted.strip()}' | Summary: '{summary}' | Audio: {'OK' if audio_ok else 'Failed'}"
        )
    else:
        log_result("4.1", "Mobile Camera Upload", False, f"Status: {r.status_code}")

if __name__ == "__main__":
    print("=" * 65)
    print("📱 VISIONAID AI - LIVE MOBILE RESPONSE VERIFICATION")
    print(f"Target: {BASE_URL}")
    print("=" * 65)
    
    test_mobile_web_routes()
    test_mobile_voice_commands()
    test_mobile_neural_translation()
    test_mobile_image_capture_ocr()
    
    print("\n" + "=" * 65)
    print("🎯 MOBILE VERIFICATION FINISHED")
    print("=" * 65)
