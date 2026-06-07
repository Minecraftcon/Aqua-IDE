import tkinter as tk
import random

root = tk.Tk()
root.title("Particles")
root.geometry("460x320+20+20")
canvas = tk.Canvas(root, bg="#111318", highlightthickness=0)
canvas.pack(fill="both", expand=True)
particles = []
for _ in range(40):
    x = random.randint(20, 440)
    y = random.randint(20, 300)
    item = canvas.create_oval(x, y, x + 5, y + 5, fill="#8be9fd", outline="")
    particles.append([item, random.uniform(-2, 2), random.uniform(-2, 2)])

def tick():
    for item, vx, vy in particles:
        canvas.move(item, vx, vy)
        x1, y1, x2, y2 = canvas.coords(item)
        if x1 < 0 or x2 > 460:
            vx *= -1
        if y1 < 0 or y2 > 320:
            vy *= -1
    root.after(16, tick)

tick()
root.mainloop()
