import os, re, glob

lib_dir = r'd:\Information Technology\LV_CNTT\core_code\clinic-frontend\mobile-app\lib'
dart_files = [y for x in os.walk(lib_dir) for y in glob.glob(os.path.join(x[0], '*.dart'))]

for file in dart_files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if 'Image.network' in content:
        if 'cached_network_image.dart' not in content:
            content = content.replace("import 'package:flutter/material.dart';", "import 'package:flutter/material.dart';\nimport 'package:cached_network_image/cached_network_image.dart';")
        
        content = re.sub(r'Image\.network\(\s*([^,]+),', r'CachedNetworkImage(imageUrl: \1,', content)
        content = re.sub(r'errorBuilder:\s*\([^\)]+\)\s*=>', r'errorWidget: (context, url, error) =>', content)
        
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print('Updated ' + file)
