import os
import re
import html

# SVG icon mapping - predefined icons with their content
ICON_MAP = {
    'search': "<circle cx='11' cy='11' r='8' stroke='currentColor' stroke-width='2'/><path d='M21 21L16.65 16.65' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>",
    'add': "<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/><path d='M12 8V16' stroke='currentColor' stroke-width='2' stroke-linecap='round'/><path d='M8 12H16' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>",
    'back': "<path d='M15 18L9 12L15 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'more': "<circle cx='12' cy='5' r='1.5' fill='currentColor'/><circle cx='12' cy='12' r='1.5' fill='currentColor'/><circle cx='12' cy='19' r='1.5' fill='currentColor'/>",
    'arrow-right': "<polyline points='9 18 15 12 9 6' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'close': "<path d='M18 6L6 18M6 6l12 12' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>",
    'user': "<path d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><circle cx='12' cy='7' r='4' stroke='currentColor' stroke-width='2'/>",
    'robot': "<rect x='3' y='11' width='18' height='10' rx='2' stroke='currentColor' stroke-width='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2'/><circle cx='9' cy='16' r='1' fill='currentColor'/><circle cx='15' cy='16' r='1' fill='currentColor'/>",
    'send': "<path d='M22 2L11 13' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><path d='M22 2L15 22L11 13L2 9L22 2Z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'emoji': "<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/><path d='M8 14s1.5 2 4 2 4-2 4-2' stroke='currentColor' stroke-width='2' stroke-linecap='round'/><circle cx='9' cy='9' r='1' fill='currentColor'/><circle cx='15' cy='9' r='1' fill='currentColor'/>",
    'image': "<rect x='3' y='3' width='18' height='18' rx='2' stroke='currentColor' stroke-width='2'/><circle cx='8.5' cy='8.5' r='1.5' stroke='currentColor' stroke-width='2'/><path d='M21 15l-5-5L5 21' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'qrcode': "<rect x='3' y='3' width='7' height='7' stroke='currentColor' stroke-width='2'/><rect x='14' y='3' width='7' height='7' stroke='currentColor' stroke-width='2'/><rect x='14' y='14' width='7' height='7' stroke='currentColor' stroke-width='2'/><rect x='3' y='14' width='7' height='7' stroke='currentColor' stroke-width='2'/>",
    'failed': "<circle cx='12' cy='12' r='10' stroke='#ff3b30' stroke-width='2'/><path d='M12 8V12L15 15' stroke='#ff3b30' stroke-width='2' stroke-linecap='round'/>",
    'check': "<polyline points='20 6 9 17 4 12' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'eye': "<path d='M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z' stroke='currentColor' stroke-width='2'/><circle cx='12' cy='12' r='3' stroke='currentColor' stroke-width='2'/>",
    'eye-off': "<path d='M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/><line x1='1' y1='1' x2='23' y2='23' stroke='currentColor' stroke-width='2' stroke-linecap='round'/>",
    'phone': "<path d='M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'lock': "<rect x='3' y='11' width='18' height='11' rx='2' ry='2' stroke='currentColor' stroke-width='2'/><path d='M7 11V7a5 5 0 0 1 10 0v4' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
    'success': "<circle cx='12' cy='12' r='10' stroke='currentColor' stroke-width='2'/><polyline points='9 12 12 15 16 10' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/>",
}

def extract_svg_content(svg_tag):
    """Extract inner content from svg tag"""
    # Remove svg opening and closing tags
    content = re.sub(r'<svg[^>]*>', '', svg_tag)
    content = re.sub(r'</svg>', '', content)
    return content.strip()

def get_svg_size(svg_tag):
    """Extract size from svg tag"""
    width_match = re.search(r'width=["\']([^"\']+)["\']', svg_tag)
    height_match = re.search(r'height=["\']([^"\']+)["\']', svg_tag)
    
    width = width_match.group(1) if width_match else None
    height = height_match.group(1) if height_match else None
    
    return width, height

def get_svg_color(svg_tag):
    """Extract color from svg strokes"""
    # Look for stroke color
    stroke_match = re.search(r'stroke=["\']([^"\']+)["\']', svg_tag)
    if stroke_match:
        color = stroke_match.group(1)
        if color != 'currentColor':
            return color
    
    fill_match = re.search(r'fill=["\']([^"\']+)["\']', svg_tag)
    if fill_match:
        color = fill_match.group(1)
        if color != 'currentColor' and color != 'none':
            return color
    
    return None

def convert_svg_to_component(svg_tag, class_name=None):
    """Convert an SVG tag to svg-icon component"""
    content = extract_svg_content(svg_tag)
    width, height = get_svg_size(svg_tag)
    color = get_svg_color(svg_tag)
    
    # Build component attributes
    attrs = []
    if class_name:
        attrs.append(f'class="{class_name}"')
    
    # Escape content for attribute
    escaped_content = content.replace('"', "'")
    attrs.append(f'icon="{escaped_content}"')
    
    if width and height:
        # Convert rpx/px to number for size
        w_val = re.sub(r'[^\d.]', '', width)
        h_val = re.sub(r'[^\d.]', '', height)
        if w_val == h_val:
            attrs.append(f'size="{w_val}"')
        else:
            attrs.append(f'width="{w_val}"')
            attrs.append(f'height="{h_val}"')
    
    if color:
        attrs.append(f'color="{color}"')
    
    return f'<svg-icon {" ".join(attrs)} />'

def process_file(filepath):
    """Process a single Vue file"""
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    
    # Pattern to match complete svg tags (including multiline)
    svg_pattern = r'<svg[^>]*>(.*?)</svg>'
    
    def replace_svg(match):
        svg_tag = match.group(0)
        
        # Get class name if exists
        class_match = re.search(r'class=["\']([^"\']+)["\']', svg_tag)
        class_name = class_match.group(1) if class_match else None
        
        return convert_svg_to_component(svg_tag, class_name)
    
    # Use DOTALL flag to match multiline
    content = re.sub(svg_pattern, replace_svg, content, flags=re.DOTALL)
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated: {filepath}")
        return True
    return False

def main():
    pages_dir = '/root/project/study/NovaChat/nova-front/src/pages'
    components_dir = '/root/project/study/NovaChat/nova-front/src/components'
    
    updated_count = 0
    
    # Process pages
    for root, dirs, files in os.walk(pages_dir):
        for file in files:
            if file.endswith('.vue'):
                filepath = os.path.join(root, file)
                if process_file(filepath):
                    updated_count += 1
    
    # Process components
    for root, dirs, files in os.walk(components_dir):
        for file in files:
            if file.endswith('.vue'):
                filepath = os.path.join(root, file)
                if process_file(filepath):
                    updated_count += 1
    
    print(f"\nTotal files updated: {updated_count}")

if __name__ == '__main__':
    main()
