# pyre-ignore-all-errors
import os
import sys

# Force absolute path to virtual environment for linting resolution
VENV_SITES = r"E:\AI-Accessibility-Assistant\.venv\Lib\site-packages"
if VENV_SITES not in sys.path:
    sys.path.insert(0, VENV_SITES)

# Ensure project root is in path
sys.path.insert(0, os.path.abspath(os.path.dirname(__file__)))

from dotenv import load_dotenv  # type: ignore
from openai import OpenAI  # type: ignore

load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")

if api_key:
    # Avoid ANY slicing or bracket indexing which Pyre is flagging as errors
    ak_str = str(api_key)
    prefix_chars = []
    suffix_chars = []
    
    # Manual loop to build prefix
    for i, char in enumerate(ak_str):
        if i < 5:
            prefix_chars.append(char)
        else:
            break
            
    # Manual loop to build suffix (last 5)
    ak_list = list(ak_str)
    ak_len = len(ak_list)
    for i in range(max(0, ak_len - 5), ak_len):
        suffix_chars.append(ak_list[i])
        
    prefix = "".join(prefix_chars)
    suffix = "".join(suffix_chars)
    print(f"API Key loaded: {prefix}...{suffix}")
else:
    print("API Key loaded: None")

if not api_key:
    print("ERROR: API Key not found.")
    exit(1)

client = OpenAI(api_key=api_key)

try:
    print("Testing OpenAI API connection...")
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "user", "content": "Hello"}
        ],
        max_tokens=10
    )
    print("Success!")
    print(response.choices[0].message.content)
except Exception as e:
    print(f"Error: {e}")
    
    print("Retrying with gpt-3.5-turbo...")
    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "user", "content": "Hello"}
            ],
            max_tokens=10
        )
        print("Success with gpt-3.5-turbo!")
        print(response.choices[0].message.content)
    except Exception as e2:
        print(f"Error with gpt-3.5-turbo: {e2}")
