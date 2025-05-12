from flask import Flask, jsonify
import psutil
import platform
import datetime
import socket
import os
import subprocess
import json

app = Flask(__name__)

def get_size(bytes, suffix="B"):
    factor = 1024
    for unit in ["", "K", "M", "G", "T", "P"]:
        if bytes < factor:
            return f"{bytes:.2f}{unit}{suffix}"
        bytes /= factor

def get_gpu_info():
    try:
        # Run nvidia-smi to get GPU information
        result = subprocess.run(['nvidia-smi', '--query-gpu=utilization.gpu,memory.used,memory.total', '--format=csv,noheader,nounits'], 
                              capture_output=True, text=True)
        
        # Parse the output for both GPUs
        gpu_lines = result.stdout.strip().split('\n')
        gpu_info = {}
        
        for i, line in enumerate(gpu_lines, 1):
            usage, mem_used, mem_total = map(float, line.split(', '))
            gpu_info[f'gpu{i}'] = {
                'usage': str(round(usage)),
                'memoryUsed': str(round(mem_used / 1024, 1)),  # Convert MB to GB
                'memoryTotal': str(round(mem_total / 1024, 1)),  # Convert MB to GB
                'memoryPercent': str(round((mem_used / mem_total) * 100))
            }
        
        return gpu_info
    except Exception as e:
        print(f"Error getting GPU info: {e}")
        return {
            'gpu1': {'usage': '0', 'memoryUsed': '0', 'memoryTotal': '24', 'memoryPercent': '0'},
            'gpu2': {'usage': '0', 'memoryUsed': '0', 'memoryTotal': '24', 'memoryPercent': '0'}
        }

def get_system_info():
    try:
        # CPU Usage
        cpu_usage = psutil.cpu_percent(interval=1)
        
        # Memory Usage
        memory = psutil.virtual_memory()
        memory_usage = memory.percent
        
        # Disk Usage - Use C: drive on Windows
        disk = psutil.disk_usage('C:\\')
        disk_space = f"{get_size(disk.free)} free of {get_size(disk.total)}"
        
        # Get GPU information
        gpu_info = get_gpu_info()
        
        return {
            "cpuUsage": cpu_usage,
            "memoryUsage": memory_usage,
            "diskSpace": disk_space,
            **gpu_info  # Add GPU information to the response
        }
    except Exception as e:
        print(f"Error getting system info: {str(e)}")
        return {
            "error": str(e)
        }

@app.route('/system-info')
def system_info():
    return jsonify(get_system_info())

if __name__ == '__main__':
    try:
        hostname = socket.gethostname()
        local_ip = socket.gethostbyname(hostname)
        print(f"Server running at http://{local_ip}:8080")
        print("Press Ctrl+C to stop the server")
        app.run(host='0.0.0.0', port=8080)
    except Exception as e:
        print(f"Error starting server: {str(e)}") 