import requests
import json
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

BASE = 'https://visionaid-api.onrender.com'

def test_e2e():
    print('=============================================')
    print('👁️ VISIONAID AI - END-TO-END VERIFICATION')
    print('=============================================')

    print('\n[1/5] Testing Health Check...')
    r = requests.get(f'{BASE}/health', timeout=15)
    assert r.status_code == 200, f"Health check failed: {r.status_code}"
    print(f'✅ Health check OK: {r.json()}')

    print('\n[2/5] Testing PWA & Web Routes...')
    r_root = requests.get(f'{BASE}/', timeout=15)
    assert r_root.status_code == 200, f"Root / failed: {r_root.status_code}"
    print(f'✅ Desktop Web /: 200 OK ({len(r_root.text)} bytes)')

    r_mobile = requests.get(f'{BASE}/mobile', timeout=15)
    assert r_mobile.status_code == 200, f"Mobile /mobile failed: {r_mobile.status_code}"
    print(f'✅ Mobile Web /mobile: 200 OK ({len(r_mobile.text)} bytes)')

    r_manifest = requests.get(f'{BASE}/manifest.json', timeout=15)
    assert r_manifest.status_code == 200, f"Manifest failed: {r_manifest.status_code}"
    print(f'✅ PWA Manifest /manifest.json: 200 OK')

    print('\n[3/5] Testing Verbal Q&A with Multilingual Speech Synthesis...')
    for lang in ['en', 'te', 'hi']:
        payload = {'text': 'What can you help me with?', 'lang': lang}
        r = requests.post(f'{BASE}/process-text', json=payload, timeout=25)
        assert r.status_code == 200, f"process-text ({lang}) failed: {r.status_code} {r.text}"
        data = r.json()
        summary = data.get('ai_summary')
        audio_file = data.get('audio_file')
        print(f'  • [{lang.upper()}] Verbal response: "{summary}"')
        
        # Test audio stream
        audio_url = f'{BASE}/{audio_file}'
        r_audio = requests.get(audio_url, timeout=15)
        assert r_audio.status_code == 200, f"Audio download failed for {audio_url}"
        assert len(r_audio.content) > 1000, "Audio file is unexpectedly small"
        print(f'    🔊 Audio streamed successfully: {len(r_audio.content)} bytes (Content-Type: {r_audio.headers.get("content-type")})')

    print('\n[4/5] Testing Neural Multilingual Translation...')
    for lang in ['te', 'hi', 'ta']:
        payload = {'text': 'Welcome to VisionAid Accessibility Assistant.', 'lang': lang}
        r = requests.post(f'{BASE}/translate', json=payload, timeout=25)
        assert r.status_code == 200, f"translate ({lang}) failed: {r.status_code} {r.text}"
        data = r.json()
        translated = data.get('ai_summary')
        audio_file = data.get('audio_file')
        print(f'  • Translated [{lang.upper()}]: "{translated}"')
        
        audio_url = f'{BASE}/{audio_file}'
        r_audio = requests.get(audio_url, timeout=15)
        assert r_audio.status_code == 200
        print(f'    🔊 Audio streamed: {len(r_audio.content)} bytes')

    print('\n[5/5] Testing Image Optical Character Recognition (OCR)...')
    sample_img = 'question_sample.png'
    assert os.path.exists(sample_img), f"Missing {sample_img}"
    with open(sample_img, 'rb') as f:
        r = requests.post(
            f'{BASE}/upload-image',
            files={'image': (sample_img, f, 'image/png')},
            data={'lang': 'en'},
            timeout=30
        )
    assert r.status_code == 200, f"upload-image failed: {r.status_code} {r.text}"
    data = r.json()
    print(f'  • Extracted Text: "{data.get("extracted_text")}"')
    print(f'  • AI Summary: "{data.get("ai_summary")}"')
    audio_url = f'{BASE}/{data.get("audio_file")}'
    r_audio = requests.get(audio_url, timeout=15)
    assert r_audio.status_code == 200
    print(f'    🔊 OCR Audio streamed: {len(r_audio.content)} bytes')

    print('\n=============================================')
    print('🎉 ALL 5 END-TO-END TEST SUITES PASSED! (100% OK)')
    print('=============================================')

if __name__ == '__main__':
    test_e2e()
