# pyre-ignore-all-errors
import os
import sys

# Force absolute path to virtual environment for linting resolution on Windows
VENV_SITES = r"E:\AI-Accessibility-Assistant\.venv\Lib\site-packages"
if os.name == 'nt' and os.path.exists(VENV_SITES) and VENV_SITES not in sys.path:
    sys.path.insert(0, VENV_SITES)

import pytesseract  # type: ignore
from PIL import Image  # type: ignore
import io
import os
import traceback
from dotenv import load_dotenv  # type: ignore
import google.generativeai as genai  # type: ignore
from gtts import gTTS  # type: ignore
import uuid
import time
from flask import Flask, request, jsonify, send_from_directory  # type: ignore
from flask_cors import CORS  # type: ignore

# Point to Tesseract installation (auto-detect Windows vs Linux/Docker)
if os.name == 'nt' and os.path.exists(r"C:\Program Files\Tesseract-OCR\tesseract.exe"):
    pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

local_tessdata = os.path.join(os.getcwd(), 'tessdata')
if os.path.exists(local_tessdata):
    os.environ['TESSDATA_PREFIX'] = local_tessdata

load_dotenv()

# Configure Gemini AI
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    model = genai.GenerativeModel('gemini-1.5-flash')
    USE_GEMINI = True
    print("Using Google Gemini AI")
elif OPENAI_API_KEY:
    from openai import OpenAI  # type: ignore
    openai_client = OpenAI(api_key=OPENAI_API_KEY)
    USE_GEMINI = False
    print("Using OpenAI API")
else:
    print("WARNING: No AI API key found. Set GEMINI_API_KEY or OPENAI_API_KEY in .env")
    USE_GEMINI = False

app = Flask(__name__)
CORS(app)

# Ensure audio directory exists
AUDIO_DIR = os.path.join(os.getcwd(), 'audio')
os.makedirs(AUDIO_DIR, exist_ok=True)


def call_ai_with_retry(prompt, max_retries=3):
    """Call AI API with automatic retry on rate limit errors."""
    for attempt in range(max_retries):
        try:
            if USE_GEMINI:
                response = model.generate_content(prompt)
                return response.text.strip()
            else:
                response = openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[{"role": "user", "content": prompt}],
                    max_tokens=500
                )
                return response.choices[0].message.content.strip()
        except Exception as e:
            error_msg = str(e)
            if ('429' in error_msg or 'rate' in error_msg.lower()) and attempt < max_retries - 1:
                wait_time = 20 * (attempt + 1)
                print(f"Rate limited (attempt {attempt+1}/{max_retries}). Waiting {wait_time}s...")
                time.sleep(wait_time)
            else:
                raise e


def summarize_text(text):
    """
    Summarize and explain text in simple language for visually impaired users.
    """
    prompt = (
        "You are a helpful AI assistant for a visually impaired user. "
        "The following text was extracted from an image.\n\n"
        "Your goal is to provide a very brief, high-level summary of what the image/document is (1-2 sentences). "
        "For example, 'This is a menu from a restaurant' or 'This is a business card for Rithvik Kaki'. "
        "Focus on the most important context. Do not read the entire text unless it is very short.\n\n"
        f"Extracted Text:\n{text}"
    )
    
    try:
        return call_ai_with_retry(prompt)
    except Exception as e:
        return f"Error generating summary: {str(e)}"


def generate_audio(text, lang='en'):
    """
    Convert text to speech using gTTS and save as an MP3 file.
    """
    try:
        filename = f"audio_{uuid.uuid4().hex}.mp3"
        filepath = os.path.join(AUDIO_DIR, filename)
        
        # Map language codes for gTTS
        gtts_lang_map = {
            'en': 'en', 'te': 'te', 'hi': 'hi', 'ta': 'ta', 'kn': 'kn',
            'ml': 'ml', 'mr': 'mr', 'bn': 'bn', 'gu': 'gu', 'pa': 'pa',
            'ur': 'ur', 'fr': 'fr', 'es': 'es', 'de': 'de', 'ja': 'ja'
        }
        gtts_lang = gtts_lang_map.get(lang, 'en')
        
        tts = gTTS(text=text, lang=gtts_lang)
        tts.save(filepath)
        
        return f"audio/{filename}"  # Return relative path for frontend
    except Exception as e:
        print(f"TTS Error: {e}")
        # Fallback to English if the language is not supported
        try:
            tts = gTTS(text=text, lang='en')
            tts.save(filepath)
            return f"audio/{filename}"
        except Exception as e2:
            print(f"TTS Fallback Error: {e2}")
            return None


@app.route('/')
def index():
    return send_from_directory('.', 'vision_aid.html')


@app.route('/audio/<path:filename>')
def serve_audio(filename):
    return send_from_directory(AUDIO_DIR, filename)


@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        "status": "ok",
        "message": "VisionAid AI - AI Accessibility Assistant is running"
    }), 200


@app.route('/upload-image', methods=['POST'])
def upload_image():
    if 'image' not in request.files:
        print("DEBUG: /upload-image failed - No image part")
        return jsonify({"error": "No image part"}), 400

    file = request.files['image']
    if file.filename == '':
        print("DEBUG: /upload-image failed - No selected file")
        return jsonify({"error": "No selected file"}), 400

    try:
        # Read image into PIL
        image_bytes = file.read()
        image = Image.open(io.BytesIO(image_bytes))

        lang = request.form.get('lang', 'en')
        # print(f"DEBUG: /upload-image processing file: {file.filename}, lang: {lang}")
        tess_lang_map = {
            'en': 'eng', 'te': 'tel', 'hi': 'hin', 'ta': 'tam', 'kn': 'kan',
            'ml': 'mal', 'mr': 'mar', 'bn': 'ben', 'gu': 'guj', 'pa': 'pan',
            'ur': 'urd', 'fr': 'fra', 'es': 'spa', 'de': 'deu', 'ja': 'jpn'
        }
        tess_lang = tess_lang_map.get(lang, 'eng')

        # Run OCR
        extracted_text = ""
        try:
            extracted_text = pytesseract.image_to_string(image, lang=tess_lang)
        except Exception as e:
            print(f"OCR language '{tess_lang}' failed, falling back to English. Error: {e}")
            extracted_text = pytesseract.image_to_string(image)

        if not extracted_text.strip():
            fallback_msg = "I couldn't find any clear text in this image. Please ensure the document is well-lit and in focus."
            audio_file = generate_audio(fallback_msg, lang)
            return jsonify({
                "filename": file.filename,
                "warning": "No readable text found",
                "extracted_text": "",
                "ai_summary": fallback_msg,
                "audio_file": audio_file
            }), 200

        # Provide brief information as requested by the user
        brief_info = summarize_text(extracted_text)
        audio_file = generate_audio(brief_info, lang)

        return jsonify({
            "filename": file.filename,
            "extracted_text": extracted_text.strip(),
            "ai_summary": brief_info,
            "audio_file": audio_file
        }), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({
            "error": "OCR processing failed",
            "details": str(e)
        }), 500


def unwrap_n8n_data(data):
    """Unwrap n8n webhook wrapper format.
    
    n8n often wraps data in: {headers: {...}, body: {...}, params: {...}}
    This function extracts the actual payload.
    """
    if not data:
        return {}
    
    # If it's a string, attempt to parse it first
    if isinstance(data, str):
        try:
            import json
            data = json.loads(data)
        except:
            return {}

    # Case 1: The data already has the required fields at the root
    if isinstance(data, dict) and 'text' in data:
        return data

    # Case 2: The data is wrapped in a 'body' key (standard n8n Webhook node output)
    if isinstance(data, dict) and 'body' in data:
        body_data = data.get('body', {})
        
        # If body is a string (n8n JSON.stringify case), parse it
        if isinstance(body_data, str):
            try:
                import json
                body_data = json.loads(body_data)
            except:
                pass
        
        # If we found it, return it.
        if isinstance(body_data, dict) and 'text' in body_data:
            return body_data
            
    # If it's a dict but doesn't have 'text', return it as is (might have other fields)
    if isinstance(data, dict):
        return data
        
    return {}


@app.route('/process-text', methods=['POST'])
def process_text():
    raw_data = request.get_json()
    # print(f"DEBUG: /process-text received: {raw_data}")
    data = unwrap_n8n_data(raw_data)
    # print(f"DEBUG: /process-text unwrapped: {data}")
    
    if not isinstance(data, dict) or 'text' not in data:
        print("DEBUG: /process-text failed - No text provided")
        return jsonify({"error": "No text provided"}), 400

    user_text = data['text']
    lang = data.get('lang', 'en')
    
    try:
        # Ask AI
        prompt = (
            "You are a friendly verbal accessibility assistant. "
            "The user is speaking to you. Answer their question or acknowledge their statement concisely (1-2 sentences). "
            "Do not use markdown formatting like asterisks or hashtags. Keep it plain text for speech synthesis.\n\n"
            f"User said: {user_text}"
        )
        prompt_excerpt = str(prompt)[:100]  # type: ignore
        print(f"DEBUG: Calling AI with prompt: {prompt_excerpt}...")
        ai_text = call_ai_with_retry(prompt)

        # Generate Audio
        audio_file = generate_audio(ai_text, lang)

        return jsonify({
            "extracted_text": user_text,
            "ai_summary": ai_text,
            "audio_file": audio_file
        }), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route('/translate', methods=['POST'])
def translate():
    raw_data = request.get_json()
    # print(f"DEBUG: /translate received: {raw_data}")
    data = unwrap_n8n_data(raw_data)
    # print(f"DEBUG: /translate unwrapped: {data}")
    
    if not isinstance(data, dict) or 'text' not in data:
        print("DEBUG: /translate failed - No text provided")
        return jsonify({"error": "No text provided"}), 400

    text = data['text']
    lang = data.get('lang', 'en')

    # Language name mapping for AI prompt
    lang_names = {
        'en': 'English', 'te': 'Telugu', 'hi': 'Hindi', 'ta': 'Tamil', 'kn': 'Kannada',
        'ml': 'Malayalam', 'mr': 'Marathi', 'bn': 'Bengali', 'gu': 'Gujarati', 'pa': 'Punjabi',
        'ur': 'Urdu', 'fr': 'French', 'es': 'Spanish', 'de': 'German', 'ja': 'Japanese'
    }
    target_lang = lang_names.get(lang, 'English')

    try:
        prompt = (
            f"Translate the following text to {target_lang}. "
            "Only return the translated text, nothing else. "
            "Do not add quotation marks or explanations.\n\n"
            f"Text: {text}"
        )
        translated_text = call_ai_with_retry(prompt)

        # Generate audio in the target language
        audio_file = generate_audio(translated_text, lang)

        return jsonify({
            "translated_text": translated_text,
            "ai_summary": translated_text,
            "audio_file": audio_file
        }), 200

    except Exception as e:
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


# --- MOBILE PWA ROUTES ---
@app.route('/mobile')
def serve_mobile():
    """Serve the mobile-optimized version of VisionAid."""
    return send_from_directory('.', 'vision_aid.html')

@app.route('/manifest.json')
def serve_manifest():
    """Serve the PWA manifest."""
    return send_from_directory('.', 'manifest.json', mimetype='application/manifest+json')

@app.route('/sw.js')
def serve_sw():
    """Serve the service worker from root scope."""
    return send_from_directory('.', 'sw.js', mimetype='application/javascript')


if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port)
