#!/usr/bin/env python3
"""
PulsePad PC - Universal iOS & Web Companion Server
Enables free, 1-tap installation and control from any Apple iPhone, iPad, or mobile browser.
Uses standard Python library and Windows User32 API for zero-latency input simulation.
"""

import sys
import os
import json
import time
import socket
import ctypes
from http.server import HTTPServer, SimpleHTTPRequestHandler
from socketserver import ThreadingMixIn

# Windows API Bindings
user32 = ctypes.windll.user32

# Mouse flags
MOUSEEVENTF_MOVE = 0x0001
MOUSEEVENTF_LEFTDOWN = 0x0002
MOUSEEVENTF_LEFTUP = 0x0004
MOUSEEVENTF_RIGHTDOWN = 0x0008
MOUSEEVENTF_RIGHTUP = 0x0010
MOUSEEVENTF_MIDDLEDOWN = 0x0020
MOUSEEVENTF_MIDDLEUP = 0x0040
MOUSEEVENTF_WHEEL = 0x0800

# Keyboard flags
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_UNICODE = 0x0004

VK_MAP = {
    'ESC': 0x1B,
    'F1': 0x70, 'F2': 0x71, 'F3': 0x72, 'F4': 0x73, 'F5': 0x74, 'F6': 0x75,
    'F7': 0x76, 'F8': 0x77, 'F9': 0x78, 'F10': 0x79, 'F11': 0x7A, 'F12': 0x7B,
    'PRTSC': 0x2C, 'BKSP': 0x08, 'BACKSPACE': 0x08, '⌫': 0x08, 'TAB': 0x09,
    'CAPS': 0x14, 'ENTER': 0x0D, 'SHIFT': 0x10, 'CTRL': 0x11, 'ALT': 0x12,
    'WIN': 0x5B, 'SPACE': 0x20, 'INS': 0x2D, 'DEL': 0x2E, 'HOME': 0x24,
    'END': 0x23, 'PGUP': 0x21, 'PGDN': 0x22, '▲': 0x26, '▼': 0x28, '◀': 0x25,
    '▶': 0x27, 'LEFT': 0x25, 'RIGHT': 0x27, 'UP': 0x26, 'DOWN': 0x28,
    'NUMLOCK': 0x90
}

CONSUMER_MAP = {
    'VOL_UP': 0xAF,
    'VOL_DOWN': 0xAE,
    'MUTE': 0xAD,
    'PLAY_PAUSE': 0xB3,
    'NEXT_TRACK': 0xB0,
    'PREV_TRACK': 0xB1,
}

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return '127.0.0.1'

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    daemon_threads = True

class PulsePadHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        web_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'PulsePad_iOS_Web')
        super().__init__(*args, directory=web_dir, **kwargs)

    def log_message(self, format, *args):
        # Suppress spamming terminal for fast mouse stream events
        pass

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_GET(self):
        if self.path == '/api/ping':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok", "server": "PulsePad PC", "ip": get_local_ip()}).encode('utf-8'))
            return
        super().do_GET()

    def do_POST(self):
        content_len = int(self.headers.get('Content-Length', 0))
        post_data = self.rfile.read(content_len) if content_len > 0 else b'{}'
        
        try:
            data = json.loads(post_data.decode('utf-8'))
        except Exception:
            data = {}

        path = self.path

        if path == '/api/ping':
            pass
        elif path == '/api/mouse':
            action = data.get('action')
            if action == 'move':
                dx = int(data.get('dx', 0))
                dy = int(data.get('dy', 0))
                user32.mouse_event(MOUSEEVENTF_MOVE, dx, dy, 0, 0)
            elif action == 'click':
                btn = data.get('button', 'left')
                if btn == 'left':
                    user32.mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0)
                    user32.mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, 0)
                elif btn == 'right':
                    user32.mouse_event(MOUSEEVENTF_RIGHTDOWN, 0, 0, 0, 0)
                    user32.mouse_event(MOUSEEVENTF_RIGHTUP, 0, 0, 0, 0)
            elif action == 'down':
                btn = data.get('button', 'left')
                flag = MOUSEEVENTF_LEFTDOWN if btn == 'left' else (MOUSEEVENTF_RIGHTDOWN if btn == 'right' else MOUSEEVENTF_MIDDLEDOWN)
                user32.mouse_event(flag, 0, 0, 0, 0)
            elif action == 'up':
                btn = data.get('button', 'left')
                flag = MOUSEEVENTF_LEFTUP if btn == 'left' else (MOUSEEVENTF_RIGHTUP if btn == 'right' else MOUSEEVENTF_MIDDLEUP)
                user32.mouse_event(flag, 0, 0, 0, 0)
            elif action == 'wheel':
                wheel = int(data.get('wheel', 0))
                user32.mouse_event(MOUSEEVENTF_WHEEL, 0, 0, wheel * 80, 0)

        elif path == '/api/keyboard':
            key = data.get('key')
            char = data.get('char')
            if key in VK_MAP:
                vk = VK_MAP[key]
                user32.keybd_event(vk, 0, 0, 0)
                time.sleep(0.01)
                user32.keybd_event(vk, 0, KEYEVENTF_KEYUP, 0)
            elif key and len(key) == 1:
                # Direct single unicode character (letters, numbers, symbols)
                user32.keybd_event(0, ord(key), KEYEVENTF_UNICODE, 0)
                time.sleep(0.005)
                user32.keybd_event(0, ord(key), KEYEVENTF_UNICODE | KEYEVENTF_KEYUP, 0)
            elif char:
                # Type character sequence via unicode
                for ch in char:
                    user32.keybd_event(0, ord(ch), KEYEVENTF_UNICODE, 0)
                    time.sleep(0.005)
                    user32.keybd_event(0, ord(ch), KEYEVENTF_UNICODE | KEYEVENTF_KEYUP, 0)

        elif path == '/api/consumer':
            code = data.get('code')
            if code in CONSUMER_MAP:
                vk = CONSUMER_MAP[code]
                user32.keybd_event(vk, 0, 0, 0)
                time.sleep(0.01)
                user32.keybd_event(vk, 0, KEYEVENTF_KEYUP, 0)

        elif path == '/api/macro':
            name = data.get('name')
            if name == 'taskmgr':
                # Ctrl + Shift + Esc
                user32.keybd_event(0x11, 0, 0, 0)
                user32.keybd_event(0x10, 0, 0, 0)
                user32.keybd_event(0x1B, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x1B, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x10, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x11, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'snip':
                # Win + Shift + S
                user32.keybd_event(0x5B, 0, 0, 0)
                user32.keybd_event(0x10, 0, 0, 0)
                user32.keybd_event(0x53, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x53, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x10, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x5B, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'desktop':
                # Win + D
                user32.keybd_event(0x5B, 0, 0, 0)
                user32.keybd_event(0x44, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x44, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x5B, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'lock':
                # Win + L
                user32.keybd_event(0x5B, 0, 0, 0)
                user32.keybd_event(0x4C, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x4C, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x5B, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'vscode':
                # Ctrl + Shift + P
                user32.keybd_event(0x11, 0, 0, 0)
                user32.keybd_event(0x10, 0, 0, 0)
                user32.keybd_event(0x50, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x50, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x10, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x11, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'terminal':
                # Ctrl + `
                user32.keybd_event(0x11, 0, 0, 0)
                user32.keybd_event(0xC0, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0xC0, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x11, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'altf4':
                # Alt + F4
                user32.keybd_event(0x12, 0, 0, 0)
                user32.keybd_event(0x73, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x73, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x12, 0, KEYEVENTF_KEYUP, 0)
            elif name == 'alttab':
                # Alt + Tab
                user32.keybd_event(0x12, 0, 0, 0)
                user32.keybd_event(0x09, 0, 0, 0)
                time.sleep(0.02)
                user32.keybd_event(0x09, 0, KEYEVENTF_KEYUP, 0)
                user32.keybd_event(0x12, 0, KEYEVENTF_KEYUP, 0)

        elif path == '/api/text':
            text = data.get('text', '')
            for ch in text:
                user32.keybd_event(0, ord(ch), KEYEVENTF_UNICODE, 0)
                time.sleep(0.005)
                user32.keybd_event(0, ord(ch), KEYEVENTF_UNICODE | KEYEVENTF_KEYUP, 0)

        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.end_headers()
        self.wfile.write(b'{"status":"ok"}')

def run_server(port=8080):
    ip = get_local_ip()
    server_address = ('0.0.0.0', port)
    try:
        httpd = ThreadedHTTPServer(server_address, PulsePadHandler)
    except OSError:
        # Port fallback
        port = 8888
        server_address = ('0.0.0.0', port)
        httpd = ThreadedHTTPServer(server_address, PulsePadHandler)

    url = f"http://{ip}:{port}"
    print("=" * 64)
    print("  🚀 PULSEPAD PC - iOS & Web Universal Server")
    print("=" * 64)
    print(f"  Local Wi-Fi Network URL:")
    print(f"  👉  {url}")
    print(f"  👉  http://localhost:{port} (local testing)")
    print("-" * 64)
    print("  📲 100% FREE iOS Installation on Apple iPhone & iPad:")
    print(f"  1. Ensure your iPhone is connected to the same Wi-Fi as your PC.")
    print(f"  2. Open Safari on your iPhone and go to: {url}")
    print(f"  3. Tap the Safari Share icon (box with upward arrow).")
    print(f"  4. Select 'Add to Home Screen' and tap Add.")
    print(f"  5. PulsePad PC will launch full-screen as a free native iOS app!")
    print("=" * 64)
    print("  Server is active and listening for remote input... (Press Ctrl+C to stop)")
    print("=" * 64)
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nPulsePad PC Server stopped.")
        httpd.server_close()

if __name__ == '__main__':
    port = 8080
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except ValueError:
            pass
    run_server(port)
