import os, glob

lib_dir = r'd:\Information Technology\LV_CNTT\core_code\clinic-frontend\mobile-app\lib'
dart_files = [y for x in os.walk(lib_dir) for y in glob.glob(os.path.join(x[0], '*.dart'))]

for file in dart_files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if 'CachedNetworkImage(' not in content:
        continue

    original = content

    # Add fadeInDuration and fadeOutDuration after CachedNetworkImage(imageUrl: ...,
    # We'll add them before 'placeholder:' or before 'errorWidget:' or before '),'
    # Strategy: find "CachedNetworkImage(imageUrl:" and inject params if not already there

    if 'fadeInDuration' not in content:
        # inject after each 'CachedNetworkImage(imageUrl: ..., \n'
        import re
        # Match the imageUrl line and inject after it
        content = re.sub(
            r'(CachedNetworkImage\(imageUrl:[^\n]+,)',
            r'\1\n                        fadeInDuration: Duration.zero,\n                        fadeOutDuration: Duration.zero,',
            content
        )
    
    if content != original:
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print('Updated ' + file)
