import tkinter as tk

root = tk.Tk()
root.title("Paint Pad")
root.geometry("520x360+20+20")
canvas = tk.Canvas(root, bg="white", highlightthickness=0)
canvas.pack(fill="both", expand=True)
color = tk.StringVar(value="black")

bar = tk.Frame(root)
bar.pack(fill="x")
for c in ("black", "red", "blue", "green", "purple"):
    tk.Button(bar, text=c, command=lambda value=c: color.set(value)).pack(side="left")

def draw(event):
    x, y = event.x, event.y
    canvas.create_oval(x - 3, y - 3, x + 3, y + 3, fill=color.get(), outline="")

canvas.bind("<B1-Motion>", draw)
root.mainloop()
