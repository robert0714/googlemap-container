from elasticapm.contrib.flask import ElasticAPM
from elasticapm.handlers.logging import LoggingHandler
from prometheus_flask_exporter import PrometheusMetrics
from flask import Flask, request, jsonify
from selenium import webdriver
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import logging
import time
import os
import base64
from datetime import datetime
import re


# initialize using environment variables from elasticapm.contrib.flask import ElasticAPM
app = Flask(__name__)

# configure to use ELASTIC_APM in your application's settings from elasticapm.contrib.flask import ElasticAPM
app.config['ELASTIC_APM'] = {
    # allowed app_name chars: a-z, A-Z, 0-9, -, _, and space from elasticapm.contrib.flask
    'APP_NAME': 'flask-apm-client',
    'DEBUG': True,
    'SERVER_URL': 'http://localhost:8200',
    'TRACES_SEND_FREQ': 5,
    'SERVICE_NAME': 'calculate_distance_service',
    'FLUSH_INTERVAL': 1, # 2.x
    'MAX_QUEUE_SIZE': 1, # 2.x
    'TRANSACTIONS_IGNORE_PATTERNS': ['.*healthcheck']
}
apm = ElasticAPM(app, logging=True)
metrics = PrometheusMetrics(app)

# static information as metric
metrics.info('app_info', 'Application info', version='1.0.3')

@app.route('/api/distance', methods=['POST'])
def calculate_distance():
    data = request.json
    address1 = data['address1']
    address2 = data['address2']
    
    map_url = generate_google_maps_url(address1, address2)
    print(f"Generated map URL: {map_url}")
    screenshot_base64, distance_value, distance_unit = capture_map_screenshot_and_distance(map_url)
    
    if screenshot_base64 and distance_value and distance_unit:
        return jsonify({
            'distance': distance_value,
            'unit': distance_unit,
            'map_screenshot_base64': screenshot_base64
        })
    else:
        return jsonify({'error': 'Unable to capture map screenshot or distance'}), 500

def generate_google_maps_url(address1, address2):
    base_url = "https://www.google.com/maps/dir/"
    return f"{base_url}{address1}/{address2}"

def capture_map_screenshot_and_distance(map_url):
    # Set up Chrome options
    chrome_options = webdriver.ChromeOptions()
    chrome_options.add_argument('--headless')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--window-size=900,295')
    
    # Set up WebDriver
    driver = webdriver.Chrome(service=ChromeService(ChromeDriverManager().install()), options=chrome_options)
    
    try:
        driver.get(map_url)
        print(f"Navigated to {map_url}")
        
        # Wait for the map to load
        wait = WebDriverWait(driver, 30)
        
        # Ensure the directions are loaded
        directions_loaded = wait.until(EC.presence_of_element_located((By.XPATH, '//*[@id="section-directions-trip-0"]')))
        print("Directions loaded")
        
        # Find the first distance element with class 'ivN21e'
        distance_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, 'ivN21e')))
        print("Distance element found")

        # Extract the distance text
        distance_text = distance_element.text
        print(f"Distance text: {distance_text}")

        # Use regex to extract numbers and decimal points
        distance_value = re.findall(r'\d+\.\d+|\d+', distance_text)[0]
        distance_unit = re.findall(r'[^\d.]+', distance_text)[0].strip()
        print(f"Distance value: {distance_value}, Distance unit: {distance_unit}")

        # Further waiting to ensure the page has fully loaded
        time.sleep(5)
        
        # Generate a unique filename based on the current timestamp
        timestamp = datetime.now().strftime('%Y%m%d%H%M%S%f')
        screenshot_path = os.path.join("static", f"map_screenshot_{timestamp}.png")
        driver.save_screenshot(screenshot_path)
        print(f"Screenshot saved to {screenshot_path}")

        # Convert screenshot to Base64
        with open(screenshot_path, "rb") as image_file:
            screenshot_base64 = base64.b64encode(image_file.read()).decode('utf-8')
        
        # Delete the screenshot file
        os.remove(screenshot_path)
        
        return screenshot_base64, distance_value, distance_unit
    except Exception as e:
        print(f"Error capturing screenshot or extracting distance: {e}")
        return None, None, None
    finally:
        driver.quit()

if __name__ == '__main__':
    if not os.path.exists("static"):
        os.makedirs("static")
    handler = LoggingHandler(client=apm.client)
    handler.setLevel(logging.DEBUG)
    app.logger.addHandler(handler)
    app.run(host='0.0.0.0', port=5000, debug=True)
