import urllib.request, urllib.error; req=urllib.request.Request('http://localhost:8000/api/v1/chat/analyze', method='POST'); req.add_header('Content-Type','application/json'); 
try:
 resp=urllib.request.urlopen(req, data=b'{"message":"hello","history":[]}')
 print(resp.read().decode())
except urllib.error.URLError as e:
 print(e.read().decode())
