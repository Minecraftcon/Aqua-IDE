import tkinter as tk
import math

root = tk.Tk()
root.title("Graph Plotter")
root.geometry("520x320+20+20")
canvas = tk.Canvas(root, bg="#111318", highlightthickness=0)
canvas.pack(fill="both", expand=True)
canvas.create_line(20, 160, 500, 160, fill="#6f7785")
canvas.create_line(260, 20, 260, 300, fill="#6f7785")
points = []
for x in range(20, 501, 4):
    y = 160 - math.sin((x - 20) / 34) * 80
    points.extend([x, y])
canvas.create_line(*points, fill="#73ffb5", width=3, smooth=True)
canvas.create_text(260, 292, text="y = sin(x)", fill="#f5d361")
root.mainloop()
