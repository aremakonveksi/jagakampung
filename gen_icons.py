import os
from PIL import Image, ImageDraw

RES = r"C:\Users\user\Documents\laporwarga_repo\app\src\main\res"
TEAL = (16, 118, 107)
TEAL_DARK = (11, 74, 68)
WHITE = (255, 255, 255)
GOLD = (251, 179, 21)

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
SS = 8


def shield_points(cx, cy, w, h):
    """Shield outline: flat top, tapering to rounded point at bottom."""
    left = cx - w / 2
    right = cx + w / 2
    top = cy - h / 2
    bot = cy + h / 2
    shoulder = top + h * 0.30
    return [
        (left, top + h * 0.06),
        (cx, top),
        (right, top + h * 0.06),
        (right, shoulder),
        (right, cy + h * 0.20),
        (cx + w * 0.30, bot - h * 0.06),
        (cx, bot),
        (cx - w * 0.30, bot - h * 0.06),
        (left, cy + h * 0.20),
        (left, shoulder),
    ]


def star_points(cx, cy, r):
    pts = []
    import math
    for i in range(10):
        ang = math.radians(-90 + i * 36)
        rad = r if i % 2 == 0 else r * 0.42
        pts.append((cx + rad * math.cos(ang), cy + rad * math.sin(ang)))
    return pts


def draw_icon(size, round_mask=False):
    S = size * SS
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    if round_mask:
        d.ellipse([0, 0, S - 1, S - 1], fill=TEAL)
    else:
        d.rectangle([0, 0, S - 1, S - 1], fill=TEAL)
        d.polygon([(0, S * 0.72), (S, S * 0.24), (S, S), (0, S)], fill=TEAL_DARK)

    sw = S * 0.56
    sh = S * 0.66
    cx, cy = S / 2, S / 2

    d.polygon(shield_points(cx, cy, sw, sh), fill=WHITE)

    iw, ih = sw * 0.80, sh * 0.80
    d.polygon(shield_points(cx, cy, iw, ih), fill=TEAL)

    d.polygon(star_points(cx, cy - sh * 0.02, sw * 0.24), fill=GOLD)

    return img.resize((size, size), Image.LANCZOS)


def main():
    for dens, px in SIZES.items():
        folder = os.path.join(RES, "mipmap-" + dens)
        os.makedirs(folder, exist_ok=True)
        draw_icon(px, False).save(os.path.join(folder, "ic_launcher.webp"), "WEBP", lossless=True)
        draw_icon(px, True).save(os.path.join(folder, "ic_launcher_round.webp"), "WEBP", lossless=True)
        print("wrote mipmap-%s (%dpx)" % (dens, px))

    # preview 512
    draw_icon(512, False).save(os.path.join(RES, "..", "..", "..", "icon_preview.png"))
    print("preview: icon_preview.png")


if __name__ == "__main__":
    main()
