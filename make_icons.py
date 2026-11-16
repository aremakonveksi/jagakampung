import os
from PIL import Image, ImageDraw

RES = r"C:\Users\user\Documents\laporwarga_repo\app\src\main\res"
TEAL = (16, 118, 107)
TEAL_DARK = (11, 74, 68)
GOLD = (251, 179, 21)
WHITE = (255, 255, 255)

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def shield(draw, cx, cy, s, fill):
    # shield outline points scaled around center
    pts = [(0.00, -0.50), (0.46, -0.30), (0.46, 0.02), (0.46, 0.14),
           (0.30, 0.36), (0.00, 0.50), (-0.30, 0.36), (-0.46, 0.14),
           (-0.46, 0.02), (-0.46, -0.30)]
    draw.polygon([(cx + x * s, cy + y * s) for x, y in pts], fill=fill)


def star(draw, cx, cy, r, fill):
    import math
    pts = []
    for i in range(10):
        ang = -math.pi / 2 + i * math.pi / 5
        rr = r if i % 2 == 0 else r * 0.42
        pts.append((cx + rr * math.cos(ang), cy + rr * math.sin(ang)))
    draw.polygon(pts, fill=fill)


def render(size, rounded=False):
    S = size * 4  # supersample
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # background: rounded square or full bleed
    if rounded:
        d.ellipse([0, 0, S - 1, S - 1], fill=TEAL_DARK)
    else:
        r = int(S * 0.22)
        d.rounded_rectangle([0, 0, S - 1, S - 1], radius=r, fill=TEAL_DARK)

    # inner teal disc
    pad = S * 0.10
    d.ellipse([pad, pad, S - pad, S - pad], fill=TEAL)

    cx = cy = S / 2
    s = S * 0.52
    shield(d, cx, cy, s, WHITE)
    shield(d, cx, cy, s * 0.72, TEAL)
    star(d, cx, cy - S * 0.02, S * 0.14, GOLD)

    return img.resize((size, size), Image.LANCZOS)


for dens, px in SIZES.items():
    outdir = os.path.join(RES, "mipmap-" + dens)
    os.makedirs(outdir, exist_ok=True)
    a = render(px, rounded=False)
    b = render(px, rounded=True)
    a.save(os.path.join(outdir, "ic_launcher.webp"), "WEBP", quality=95, lossless=True)
    b.save(os.path.join(outdir, "ic_launcher_round.webp"), "WEBP", quality=95, lossless=True)
    print("wrote", dens, px, os.path.getsize(os.path.join(outdir, "ic_launcher.webp")))

# preview
render(512, False).save(os.path.join(RES, "..", "..", "..", "..", "icon_preview.png"))
print("preview done")
