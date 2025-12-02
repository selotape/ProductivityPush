#!/usr/bin/env python3
"""
Generate PNG icon files for ProductivityPush Chrome extension
"""

try:
    from PIL import Image, ImageDraw, ImageFont
    PIL_AVAILABLE = True
except ImportError:
    print("PIL/Pillow not available, creating simple text-based icons...")
    PIL_AVAILABLE = False

def create_icon_with_pil(size, filename):
    """Create icon using PIL/Pillow"""
    # Create image with gradient-like background
    img = Image.new('RGBA', (size, size), (102, 126, 234, 255))
    draw = ImageDraw.Draw(img)

    # Add a simple shield/block symbol
    center = size // 2
    shield_size = int(size * 0.6)

    # Draw white shield background
    shield_points = [
        (center, center - shield_size//2),
        (center + shield_size//3, center - shield_size//3),
        (center + shield_size//3, center + shield_size//6),
        (center, center + shield_size//2),
        (center - shield_size//3, center + shield_size//6),
        (center - shield_size//3, center - shield_size//3),
    ]
    draw.polygon(shield_points, fill=(255, 255, 255, 255))

    # Draw X symbol
    line_width = max(1, size // 16)
    x_size = shield_size // 3
    draw.line([
        (center - x_size//2, center - x_size//2),
        (center + x_size//2, center + x_size//2)
    ], fill=(102, 126, 234, 255), width=line_width)
    draw.line([
        (center + x_size//2, center - x_size//2),
        (center - x_size//2, center + x_size//2)
    ], fill=(102, 126, 234, 255), width=line_width)

    img.save(filename, 'PNG')
    print(f"Created {filename}")

def create_simple_icon(size, filename):
    """Create a simple icon without PIL"""
    # Create minimal PNG data for a solid color icon
    # This is a basic approach that creates a valid PNG file

    import struct
    import zlib

    # Simple solid color PNG
    width, height = size, size

    def png_pack(tag, data):
        chunk_head = struct.pack("!I", len(data))
        return chunk_head + tag + data + struct.pack("!I", 0xFFFFFFFF & zlib.crc32(tag + data))

    # PNG signature
    png_signature = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr = struct.pack("!2I5B", width, height, 8, 2, 0, 0, 0)
    ihdr_chunk = png_pack(b'IHDR', ihdr)

    # Create simple gradient-like pattern
    raw_data = []
    for y in range(height):
        raw_data.append(0)  # Filter type
        for x in range(width):
            # Simple blue gradient
            r = min(255, 102 + (x * 20) // width)
            g = min(255, 126 + (y * 30) // height)
            b = 234
            raw_data.extend([r, g, b])

    # IDAT chunk
    compressor = zlib.compressobj()
    png_data = compressor.compress(bytes(raw_data))
    png_data += compressor.flush()
    idat_chunk = png_pack(b'IDAT', png_data)

    # IEND chunk
    iend_chunk = png_pack(b'IEND', b'')

    # Write PNG file
    with open(filename, 'wb') as f:
        f.write(png_signature)
        f.write(ihdr_chunk)
        f.write(idat_chunk)
        f.write(iend_chunk)

    print(f"Created simple {filename}")

def main():
    sizes = [16, 32, 48, 128]

    for size in sizes:
        filename = f"icon{size}.png"
        try:
            if PIL_AVAILABLE:
                create_icon_with_pil(size, filename)
            else:
                create_simple_icon(size, filename)
        except Exception as e:
            print(f"Error creating {filename}: {e}")
            # Fallback: create a minimal file
            with open(filename, 'wb') as f:
                # Write minimal PNG header for a 1x1 blue pixel
                f.write(b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\tpHYs\x00\x00\x0b\x13\x00\x00\x0b\x13\x01\x00\x9a\x9c\x18\x00\x00\x00\x12IDATx\x9cc```bPPP\x00\x02D\x00\x00\x06\x1c\x02\x1b\x84\xafm\xfb\x00\x00\x00\x00IEND\xaeB`\x82')
            print(f"Created minimal fallback {filename}")

if __name__ == "__main__":
    main()