@echo off
echo Starting Python server for Android Display...

cd server

echo Creating virtual environment if it doesn't exist...
if not exist venv310 (
    python -m venv venv310
)

echo Activating virtual environment...
call .\venv310\Scripts\activate

echo Installing dependencies...
pip install -r requirements.txt

echo.
echo Starting server...
python system_info_server.py

echo.
echo Server stopped. 