import os
from PIL import Image, ImageDraw

RES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "app", "src", "main", "res")

TEAL = (16, 118, 107, 255)
TEAL_DARK = (12, 95, 86, 255)
WHITE = (255, 255, 255, 255)
GOLD = (251, 179, 21, 255)

DENS = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def shield_path(cx, cy, s):
    """Shield outline points scaled by s, centred at cx,cy."""
    def P(x, y):
        return (cx + (x - 24) * s, cy + (y - 24) * s)
    return [
        P(24, 3), P(43, 11.5), P(43, 24),
        P(43, 36), P(33.5, 43.5), P(24, 47),
        P(14.5, 43.5), P(5, 36), P(5, 24), P(5, 11.5),
    ]


def star_path(cx, cy, s):
    def P(x, y):
        return (cx + (x - 24) * s, cy + (y - 24) * s)
    return [P(24, 15), P(26.6, 21.4), P(33.5, 21.4),
            P(28, 25.8), P(30, 32.4), P(24, 28.6),
            P(18, 32.4), P(20, 25.8), P(14.5, 21.4), P(21.4, 21.4)]


def draw_icon(size, round_icon):
    ss = 4  # supersample
    n = size * ss
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    if round_icon:
        d.ellipse([0, 0, n - 1, n - 1], fill=TEAL)
    else:
        r = int(n * 0.18)
        d.rounded_rectangle([0, 0, n - 1, n - 1], radius=r, fill=TEAL)

    # diagonal darker band
    d.polygon([(0, n * 0.66), (n, n * 0.22), (n, n), (0, n)], fill=TEAL_DARK)

    cx = cy = n / 2.0
    s = n / 108.0 * 1.35

    d.polygon(shield_path(cx, cy, s), fill=WHITE)
    d.polygon(shield_path(cx, cy, s * 0.80), fill=TEAL)
    d.polygon(star_path(cx, cy, s * 0.95), fill=GOLD)

    return img.resize((size, size), Image.LANCZOS)


for dens, px in DENS.items():
    folder = os.path.join(RES, "mipmap-" + dens)
    os.makedirs(folder, exist_ok=True)
    draw_icon(px, False).save(os.path.join(folder, "ic_launcher.webp"),
                              "WEBP", lossless=True, quality=100, method=6)
    draw_icon(px, True).save(os.path.join(folder, "ic_launcher_round.webp"),
                             "WEBP", lossless=True, quality=100, method=6)
    print("wrote", dens, px)

# preview untuk verifikasi
os.makedirs(os.path.join(RES, "..", "..", "..", "..", "icon_preview"), exist_ok=True)
prev = os.path.join(os.path.dirname(RES), "..", "..", "..", "icon_preview")
prev = os.path.abspath(os.path.join(RES, "..", "..", "..", "..", "icon_preview"))
os.makedirs(prev, exist_ok=True)
draw_icon(512, False).save(os.path.join(prev, "preview_square.png"))
draw_icon(512, True).save(os.path.join(prev, "preview_round.png"))
print("preview:", prev)
