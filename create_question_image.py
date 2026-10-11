import os
import sys
# Force absolute path to virtual environment for linting resolution
VENV_SITES = r"E:\AI-Accessibility-Assistant\.venv\Lib\site-packages"
if VENV_SITES not in sys.path:
    sys.path.insert(0, VENV_SITES)
from PIL import Image, ImageDraw, ImageFont  # type: ignore

def create_image():
    img = Image.new('RGB', (600, 200), color=(255, 255, 255))
    d = ImageDraw.Draw(img)
    text = "What is the capital of France?"
    d.text((20, 80), text, fill=(0, 0, 0))
    img.save('question_sample.png')
    print("Created question_sample.png")

if __name__ == "__main__":
    create_image()
