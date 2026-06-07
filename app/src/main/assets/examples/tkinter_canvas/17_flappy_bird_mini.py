import tkinter as tk

root = tk.Tk()
root.title("Mini Flappy")
root.geometry("480x320+20+20")
canvas = tk.Canvas(root, bg="#70c5ce", highlightthickness=0)
canvas.pack(fill="both", expand=True)
bird = canvas.create_oval(70, 140, 105, 175, fill="#f5d361", outline="#222", width=2)
pipe_top = canvas.create_rectangle(360, 0, 420, 110, fill="#3cb043", outline="")
pipe_bot = canvas.create_rectangle(360, 210, 420, 320, fill="#3cb043", outline="")
vy = 0
y = 150
xpipe = 360

def flap(_=None):
    global vy
    vy = -7

def step():
    global vy, y, xpipe
    vy += 0.45
    y += vy
    xpipe -= 3
    if xpipe < -70:
        xpipe = 480
    canvas.coords(bird, 70, y, 105, y + 35)
    canvas.coords(pipe_top, xpipe, 0, xpipe + 60, 110)
    canvas.coords(pipe_bot, xpipe, 210, xpipe + 60, 320)
    root.after(16, step)

root.bind("<space>", flap)
root.bind("<Button-1>", flap)
step()
root.mainloop()
