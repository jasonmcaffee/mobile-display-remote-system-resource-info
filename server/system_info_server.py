from flask import Flask, jsonify
import psutil
import platform
import datetime
import socket
import os

app = Flask(__name__)

def get_size(bytes, suffix="B"):
    factor = 1024
    for unit in ["", "K", "M", "G", "T", "P"]:
        if bytes < factor:
            return f"{bytes:.2f}{unit}{suffix}"
        bytes /= factor

def get_system_info():
    try:
        # CPU Usage
        cpu_usage = psutil.cpu_percent(interval=1)
        
        # Memory Usage
        memory = psutil.virtual_memory()
        memory_usage = memory.percent
        
        # Disk Usage - Use C: drive on Windows
        disk = psutil.disk_usage('C:\\')
        disk_space = f"Total: {get_size(disk.total)}, Used: {get_size(disk.used)}, Free: {get_size(disk.free)}"
        
        # Network Status
        net_io = psutil.net_io_counters()
        network_status = f"Bytes sent: {get_size(net_io.bytes_sent)}, Bytes received: {get_size(net_io.bytes_recv)}"
        
        # System Uptime
        uptime = datetime.datetime.fromtimestamp(psutil.boot_time()).strftime("%Y-%m-%d %H:%M:%S")
        
        return {
            "cpuUsage": cpu_usage,
            "memoryUsage": memory_usage,
            "diskSpace": disk_space,
            "networkStatus": network_status,
            "uptime": uptime
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