import os
from PIL import Image, ImageDraw

OUT = os.path.join("app", "src", "main", "res")
TEAL_HI = (0x14, 0x90, 0x7F)
TEAL_LO = (0x0B, 0x5F, 0x57)
TEAL = (0x10, 0x76, 0x6B)
WHITE = (0xFF, 0xFF, 0xFF)
YELLOW = (0xFB, 0xB3, 0x15)

S = 1024
SS = 4
W = S * SS


def cubic(p0, p1, p2, p3, n=64):
    pts = []
    for i in range(n + 1):
        t = i / n
        u = 1 - t
        x = u**3 * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t**3 * p3[1]
        pts.append((x, y))
    return pts


def shield_points(scale, dx, dy, vw=48.0):
    """Shield from ic_logo.xml viewport 48, scaled and offset."""
    def m(p):
        return (p[0] * scale + dx, p[1] * scale + dy)

    pts = [m((24, 4)), m((42, 12)), m((42, 24))]
    pts += cubic(m((42, 24)), m((42, 35)), m((34, 42)), m((24, 45)))
    pts += cubic(m((24, 45)), m((14, 42)), m((6, 35)), m((6, 24)))
    pts += [m((6, 12))]
    return pts


def star_points(cx, cy, r_out, r_in, n=5):
    import math
    pts = []
    for i in range(n * 2):
        r = r_out if i % 2 == 0 else r_in
        a = -math.pi / 2 + i * math.pi / n
        pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts


def gradient_bg(size, rounded=True, circle=False):
    g = Image.new("RGB", (size, size))
    px = g.load()
    for y in range(size):
        for x in range(size):
            t = (x / size * 0.35 + y / size * 0.65)
            px[x, y] = (
                int(TEAL_HI[0] + (TEAL_LO[0] - TEAL_HI[0]) * t),
                int(TEAL_HI[1] + (TEAL_LO[1] - TEAL_HI[1]) * t),
                int(TEAL_HI[2] + (TEAL_LO[2] - TEAL_HI[2]) * t),
            )
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    if circle:
        d.ellipse([0, 0, size - 1, size - 1], fill=255)
    elif rounded:
        d.rounded_rectangle([0, 0, size - 1, size - 1], radius=int(size * 0.22), fill=255)
    else:
        d.rectangle([0, 0, size - 1, size - 1], fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(g, (0, 0), mask)
    return out


def render_icon(circle=False, pad_ratio=0.20):
    base = gradient_bg(W, rounded=True, circle=circle)
    d = ImageDraw.Draw(base)

    content = W * (1 - 2 * pad_ratio)
    scale = content / 48.0
    dx = (W - content) / 2
    dy = (W - content) / 2 + W * 0.005

    d.polygon(shield_points(scale, dx, dy), fill=WHITE)

    inner = content * 0.72
    iscale = inner / 48.0
    idx = (W - inner) / 2
    idy = (W - inner) / 2 + W * 0.012
    d.polygon(shield_points(iscale, idx, idy), fill=TEAL)

    star_r = content * 0.20
    d.polygon(star_points(W / 2, W / 2 + W * 0.015, star_r, star_r * 0.42), fill=YELLOW)

    return base.resize((S, S), Image.LANCZOS)


sizes = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

square = render_icon(circle=False, pad_ratio=0.20)
round_ = render_icon(circle=True, pad_ratio=0.22)

for dens, px in sizes.items():
    folder = os.path.join(OUT, "mipmap-" + dens)
    os.makedirs(folder, exist_ok=True)
    square.resize((px, px), Image.LANCZOS).save(
        os.path.join(folder, "ic_launcher.webp"), "WEBP", lossless=True, quality=100
    )
    round_.resize((px, px), Image.LANCZOS).save(
        os.path.join(folder, "ic_launcher_round.webp"), "WEBP", lossless=True, quality=100
    )
    print("wrote", dens, px)

# Play Store / preview
square.save(os.path.join(OUT, "..", "..", "..", "..", "icon_preview_512.png"))
print("preview saved")
