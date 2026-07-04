import os, re, glob

lib_dir = r'd:\Information Technology\LV_CNTT\core_code\clinic-frontend\mobile-app\lib'
dart_files = [y for x in os.walk(lib_dir) for y in glob.glob(os.path.join(x[0], '*.dart'))]

SHIMMER_PLACEHOLDER = """placeholder: (context, url) => Container(
                color: const Color(0xFFE2E8F0),
              ),"""

def add_placeholder(content):
    # Only process files using CachedNetworkImage  
    if 'CachedNetworkImage' not in content:
        return content
    
    # Add shimmer import if needed
    if 'shimmer' not in content.lower() and 'placeholder:' not in content:
        content = content.replace(
            "import 'package:clinic_management_system/app_exports.dart';",
            "import 'package:clinic_management_system/app_exports.dart';\nimport 'package:shimmer/shimmer.dart';"
        )
    
    # Add placeholder to CachedNetworkImage calls that don't have one
    # Find CachedNetworkImage(imageUrl: ... and add placeholder before errorWidget
    def inject_placeholder(m):
        block = m.group(0)
        if 'placeholder:' in block:
            return block
        # Insert before errorWidget or before closing paren if no errorWidget
        if 'errorWidget:' in block:
            return block.replace('errorWidget:', 'placeholder: (context, url) => Shimmer.fromColors(\n                baseColor: const Color(0xFFE2E8F0),\n                highlightColor: const Color(0xFFF8FAFC),\n                child: Container(color: Colors.white),\n              ),\n              errorWidget:', 1)
        return block
    
    # Match multi-line CachedNetworkImage blocks
    content = re.sub(
        r'CachedNetworkImage\([^)]+\)',
        inject_placeholder,
        content,
        flags=re.DOTALL
    )
    
    return content

for file in dart_files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if 'CachedNetworkImage' in content and 'placeholder:' not in content:
        new_content = add_placeholder(content)
        if new_content != content:
            with open(file, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print('Updated ' + file)
