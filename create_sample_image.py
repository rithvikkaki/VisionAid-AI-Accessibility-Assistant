import os
import sys
# Force absolute path to virtual environment for linting resolution
VENV_SITES = r"E:\AI-Accessibility-Assistant\.venv\Lib\site-packages"
if VENV_SITES not in sys.path:
    sys.path.insert(0, VENV_SITES)
from PIL import Image, ImageDraw, ImageFont  # type: ignore

def create_image():
    # Create white image
    img = Image.new('RGB', (400, 100), color=(255, 255, 255))
    d = ImageDraw.Draw(img)
    
    # Add text
    text = "Hello World OCR Test"
    # Use default bitmap font
    d.text((20, 40), text, fill=(0, 0, 0))
    
    img.save('sample.png')
    print("Created sample.png")

if __name__ == "__main__":
    create_image()
