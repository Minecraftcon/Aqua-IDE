import tkinter as tk
import math
import time

root = tk.Tk()
root.title("Canvas Clock")
root.geometry("360x360+30+30")
canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)

def draw():
    canvas.delete("all")
    cx = cy = 180
    canvas.create_oval(45, 45, 315, 315, outline="#8be9fd", width=4)
    sec = time.localtime().tm_sec
    minute = time.localtime().tm_min
    for value, length, color, width in ((minute, 90, "#f5d361", 5), (sec, 115, "#ff5555", 2)):
        angle = math.radians(value * 6 - 90)
        canvas.create_line(cx, cy, cx + math.cos(angle) * length, cy + math.sin(angle) * length, fill=color, width=width)
    canvas.create_text(cx, 305, text=time.strftime("%H:%M:%S"), fill="white", font=("Arial", 16))
    root.after(1000, draw)

draw()
root.mainloop()
