import tkinter as tk
import math

root = tk.Tk()
root.title("Floating Ball")
root.geometry("420x320+25+25")
canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)
ball = canvas.create_oval(40, 40, 90, 90, fill="#00e5ff", outline="white", width=2)
t = 0

def animate():
    global t
    t += 0.12
    x = 190 + math.sin(t) * 130
    y = 135 + math.cos(t * 1.4) * 80
    canvas.coords(ball, x - 28, y - 28, x + 28, y + 28)
    root.after(16, animate)

animate()
root.mainloop()
