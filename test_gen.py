import urllib.request, urllib.error; req=urllib.request.Request('http://localhost:8000/api/v1/chat/generate', method='POST'); req.add_header('Content-Type','application/json'); 
try:
 resp=urllib.request.urlopen(req, data=b'{"message":"L\xc4\xb0ch l\xc3\xa0m vi\xe1\xbb\x87c","history":[],"intent":"GENERAL","rewritten_query":"L\xc4\xb0ch l\xc3\xa0m vi\xe1\xbb\x87c","knowledge_context":""}')
 print(resp.read().decode())
except urllib.error.URLError as e:
 print(e.read().decode())
