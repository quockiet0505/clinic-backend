import re
with open('backend/src/main/resources/db/migration/V2__insert_sample_data.sql', 'rb') as f:
    data = f.read()
try:
    s = data.decode('utf-8')
    matches = re.findall(r'B.*c s.* Gia .*nh', s)
    print('UTF-8 decodes to:', matches)
except Exception as e:
    print(e)
try:
    s2 = data.decode('cp1252')
    matches2 = re.findall(r'B.*c s.* Gia .*nh', s2)
    print('CP1252 decodes to:', matches2)
except Exception as e:
    print(e)
