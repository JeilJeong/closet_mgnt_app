import sys
import socket

host = socket.gethostbyname_ex(socket.getfqdn())[2][1]
# host = socket.gethostbyname(socket.gethostname())
port = 8080

inputData = dict()