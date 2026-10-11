#!/usr/bin/env python3
"""Generates every Dread texture. Edit & re-run to restyle:  python3 tools/generate_textures.py  (needs Pillow)"""
import os, random
from PIL import Image, ImageDraw

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "dread", "textures")
ENT = os.path.join(BASE, "entity"); ITM = os.path.join(BASE, "item")
os.makedirs(ENT, exist_ok=True); os.makedirs(ITM, exist_ok=True)

def c(v): return max(0, min(255, int(v)))

def fill(img, box, base, var=16, alpha=255):
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            n = random.randint(-var, var)
            img.putpixel((x, y), (c(base[0] + n), c(base[1] + n), c(base[2] + n), alpha))

def rect(img, x0, y0, x1, y1, col):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            img.putpixel((x, y), col)

def veins(img, box, count, col=(112, 18, 24, 255), length=9):
    x0, y0, x1, y1 = box
    for _ in range(count):
        x = random.randint(x0, x1 - 1); y = random.randint(y0, y1 - 1)
        for _ in range(length):
            img.putpixel((x, y), col)
            x = max(x0, min(x1 - 1, x + random.choice((-1, 0, 0, 1))))
            y = max(y0, min(y1 - 1, y + random.choice((-1, 1, 1, 0))))

def speckle(img, box, count, col):
    x0, y0, x1, y1 = box
    for _ in range(count):
        img.putpixel((random.randint(x0, x1 - 1), random.randint(y0, y1 - 1)), col)

def ragged(img, box, start_y, end_y, strength=0.7):
    x0, y0, x1, y1 = box
    for y in range(start_y, end_y):
        p = (y - start_y) / max(1, end_y - start_y)
        for x in range(x0, x1):
            if random.random() < p * strength:
                img.putpixel((x, y), (0, 0, 0, 0))

def save(img, folder, name):
    img.save(os.path.join(folder, name))

# ------------------------------------------------------------- STALKER (64x64 biped)
def stalker():
    random.seed(11)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    skin, cloth = (158, 156, 150), (30, 28, 30)
    fill(img, (0, 0, 32, 16), skin, 20)
    fill(img, (16, 16, 40, 32), cloth, 10)
    fill(img, (40, 16, 56, 32), skin, 18)
    fill(img, (32, 48, 48, 64), skin, 18)
    fill(img, (0, 16, 16, 32), cloth, 8)
    fill(img, (16, 48, 32, 64), cloth, 8)
    veins(img, (40, 16, 56, 32), 14); veins(img, (32, 48, 48, 64), 14); veins(img, (0, 0, 32, 16), 8)
    speckle(img, (16, 16, 40, 32), 40, (70, 8, 10, 255))   # blood on the rags
    # face
    fill(img, (8, 8, 16, 16), skin, 14)
    rect(img, 9, 10, 10, 11, (0, 0, 0, 255)); rect(img, 13, 10, 14, 11, (0, 0, 0, 255))
    rect(img, 9, 13, 14, 14, (40, 0, 0, 255))
    for x in (9, 11, 13): img.putpixel((x, 13), (225, 220, 205, 255))
    for x in (10, 12, 14): img.putpixel((x, 14), (225, 220, 205, 255))
    save(img, ENT, "stalker.png")
    e = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    e.putpixel((10, 10), (235, 240, 255, 255)); e.putpixel((13, 11), (235, 240, 255, 255))
    save(e, ENT, "stalker_eyes.png")

# ------------------------------------------------------------- CRAWLER (64x32 spider)
def crawler():
    random.seed(23)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    fill(img, (0, 0, 64, 32), (26, 20, 22), 10)
    for y in range(14, 32, 3):                             # pale rib lines on the abdomen
        for x in range(0, 44):
            if random.random() < 0.8:
                img.putpixel((x, y), (118, 112, 98, 255))
    veins(img, (0, 12, 44, 32), 20, (120, 16, 20, 255), 7)
    veins(img, (18, 0, 54, 4), 6, (120, 16, 20, 255), 5)
    # skull-like face: head front is (40,12)-(48,20)
    fill(img, (40, 12, 48, 20), (168, 163, 148), 12)
    rect(img, 41, 14, 42, 15, (0, 0, 0, 255)); rect(img, 45, 14, 46, 15, (0, 0, 0, 255))
    img.putpixel((43, 13), (0, 0, 0, 255)); img.putpixel((44, 13), (0, 0, 0, 255))
    img.putpixel((43, 17), (0, 0, 0, 255)); img.putpixel((44, 17), (0, 0, 0, 255))
    rect(img, 41, 18, 46, 18, (40, 0, 0, 255))
    for x in (41, 43, 45): img.putpixel((x, 19), (230, 225, 210, 255))
    save(img, ENT, "crawler.png")
    e = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for p in ((42, 15), (45, 15)): e.putpixel(p, (255, 20, 20, 255))
    for p in ((43, 13), (44, 13), (43, 17), (44, 17)): e.putpixel(p, (255, 120, 20, 255))
    save(e, ENT, "crawler_eyes.png")

# ------------------------------------------------------------- WATCHER (64x32 enderman layout)
def watcher():
    random.seed(37)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    fill(img, (0, 0, 32, 16), (10, 10, 12), 5)            # black head shell
    fill(img, (0, 16, 32, 32), (10, 10, 12), 5)           # jaw layer
    fill(img, (32, 16, 56, 32), (8, 8, 10), 4)            # torso
    fill(img, (56, 0, 64, 32), (6, 6, 8), 3)              # limbs
    speckle(img, (32, 16, 56, 32), 50, (40, 40, 44, 255))
    # pale, blank face
    fill(img, (8, 8, 16, 16), (205, 205, 198), 10)
    rect(img, 9, 11, 11, 12, (0, 0, 0, 255)); rect(img, 13, 11, 15, 12, (0, 0, 0, 255))
    rect(img, 10, 14, 13, 14, (30, 0, 0, 255))
    fill(img, (8, 24, 16, 32), (205, 205, 198), 10)
    rect(img, 8, 24, 15, 25, (0, 0, 0, 255))
    save(img, ENT, "watcher.png")
    e = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    for p in ((10, 12), (11, 12), (13, 12), (14, 12)): e.putpixel(p, (255, 255, 255, 255))
    save(e, ENT, "watcher_eyes.png")

# ------------------------------------------------------------- WAILER (64x64 biped, ragged ghost)
def wailer():
    random.seed(51)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    robe, skin = (44, 60, 68), (205, 215, 222)
    fill(img, (0, 0, 32, 16), skin, 12)
    fill(img, (16, 16, 40, 32), robe, 9)
    fill(img, (40, 16, 56, 32), skin, 10)
    fill(img, (32, 48, 48, 64), skin, 10)
    fill(img, (0, 16, 16, 32), robe, 9)
    fill(img, (16, 48, 32, 64), robe, 9)
    veins(img, (16, 16, 40, 32), 10, (20, 30, 36, 255), 8)
    ragged(img, (16, 16, 40, 32), 27, 32)
    ragged(img, (0, 16, 16, 32), 25, 32, 0.9)
    ragged(img, (16, 48, 32, 64), 57, 64, 0.9)
    ragged(img, (40, 16, 56, 32), 27, 32)
    ragged(img, (32, 48, 48, 64), 59, 64)
    fill(img, (8, 8, 16, 16), skin, 10)
    rect(img, 9, 10, 10, 12, (0, 0, 0, 255)); rect(img, 13, 10, 14, 12, (0, 0, 0, 255))
    rect(img, 10, 13, 13, 15, (0, 0, 0, 255))
    save(img, ENT, "wailer.png")
    e = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for p in ((10, 11), (13, 11)): e.putpixel(p, (120, 220, 255, 255))
    for p in ((11, 14), (12, 14)): e.putpixel(p, (80, 170, 220, 255))
    save(e, ENT, "wailer_eyes.png")

# ------------------------------------------------------------- ITEMS
def items():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (11, 5), (10, 12), (8, 15), (6, 11), (5, 5)], fill=(44, 16, 62, 255), outline=(14, 4, 22, 255))
    d.line([(8, 3), (8, 13)], fill=(170, 24, 44, 255))
    d.point((7, 6), fill=(120, 70, 150, 255)); d.point((9, 8), fill=(120, 70, 150, 255))
    save(img, ITM, "dread_shard.png")

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(img)
    d.rectangle((7, 0, 8, 2), fill=(50, 40, 20, 255))
    d.polygon([(5, 3), (10, 3), (12, 11), (3, 11)], fill=(124, 98, 42, 255), outline=(58, 40, 14, 255))
    d.rectangle((2, 11, 13, 12), fill=(92, 70, 30, 255), outline=(50, 34, 12, 255))
    d.line([(6, 5), (9, 9)], fill=(150, 20, 30, 255))
    d.point((6, 4), fill=(190, 160, 90, 255))
    d.rectangle((7, 13, 8, 14), fill=(150, 20, 30, 255))
    save(img, ITM, "haunted_bell.png")

stalker(); crawler(); watcher(); wailer(); items()
print("textures generated")
