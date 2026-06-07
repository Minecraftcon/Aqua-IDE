import tkinter as tk

root = tk.Tk()
root.title("Breakout Blocks")
root.geometry("480x320+20+20")
canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)
for row in range(4):
    for col in range(8):
        x = 20 + col * 55
        y = 30 + row * 28
        canvas.create_rectangle(x, y, x + 48, y + 20, fill=["#ff5555", "#f5d361", "#73ffb5", "#8be9fd"][row], outline="")
paddle = canvas.create_rectangle(190, 280, 290, 292, fill="white")
ball = canvas.create_oval(230, 230, 246, 246, fill="#00e5ff")
dx, dy = 3, -3

def move(event):
    x = max(50, min(430, event.x))
    canvas.coords(paddle, x - 50, 280, x + 50, 292)

def step():
    global dx, dy
    canvas.move(ball, dx, dy)
    x1, y1, x2, y2 = canvas.coords(ball)
    if x1 <= 0 or x2 >= 480:
        dx *= -1
    if y1 <= 0 or y2 >= 320:
        dy *= -1
    root.after(16, step)

canvas.bind("<Motion>", move)
step()
root.mainloop()
