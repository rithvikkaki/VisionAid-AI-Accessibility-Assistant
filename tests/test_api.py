# pyre-ignore-all-errors
import os
import sys

# Force absolute path to virtual environment for linting resolution
VENV_SITES = r"E:\AI-Accessibility-Assistant\.venv\Lib\site-packages"
if VENV_SITES not in sys.path:
    sys.path.insert(0, VENV_SITES)

import pytest  # type: ignore
import io
import sys
import os
from PIL import Image  # type: ignore

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
try:
    from app import app  # type: ignore
except ImportError:
    import app  # type: ignore
    from app import app  # type: ignore

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

def test_health(client):
    res = client.get("/health")
    assert res.status_code == 200

def test_upload_no_file(client):
    res = client.post("/upload-image")
    assert res.status_code == 400

def test_upload_success(client):
    # Create a valid small image in memory
    img = Image.new("RGB", (10, 10), color="white")
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    buf.seek(0)

    data = {
        "image": (buf, "test.png")
    }
    res = client.post("/upload-image", data=data, content_type="multipart/form-data")
    assert res.status_code == 200
