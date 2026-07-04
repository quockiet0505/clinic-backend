import os, glob

lib_dir = r'd:\Information Technology\LV_CNTT\core_code\clinic-frontend\mobile-app\lib'
dart_files = [y for x in os.walk(lib_dir) for y in glob.glob(os.path.join(x[0], '*.dart'))]

for file in dart_files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if 'CachedNetworkImage(' not in content:
        continue

    original = content

    if 'memCacheWidth' not in content:
        import re
        # Match the imageUrl line and inject memCacheWidth after it
        content = re.sub(
            r'(CachedNetworkImage\(imageUrl:[^\n]+,)',
            r'\1\n                        memCacheWidth: 400,\n                        memCacheHeight: 400,',
            content
        )
    
    if content != original:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print('Updated ' + file)
