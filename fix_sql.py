import codecs
with open('backend/src/main/resources/db/migration/V2__insert_sample_data.sql', 'rb') as f:
    data = f.read()
s = data.decode('utf-8-sig')
def custom_encode(c):
    try:
        return c.encode('cp1252')
    except:
        return c.encode('latin1')
fixed_bytes = b''.join(custom_encode(c) for c in s)
fixed = fixed_bytes.decode('utf-8')
with open('backend/src/main/resources/db/migration/V2__insert_sample_data_fixed.sql', 'w', encoding='utf-8') as f:
    f.write(fixed)
print('Success!')
