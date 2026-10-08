#!/data/data/com.termux/files/usr/bin/bash
set -e
pkg update -y
pkg install -y python
python -m pip install --upgrade pip
python -m pip install -r "$(dirname "$0")/../bridge/requirements.txt"
echo
echo "安装完成。运行："
echo "bash termux/start_bridge.sh"
