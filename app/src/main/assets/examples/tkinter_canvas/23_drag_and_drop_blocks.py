import tkinter as tk

root = tk.Tk()
root.title("Drag Blocks")
root.geometry("420x300+30+30")
canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)
active = None

for i, color in enumerate(("#ff5555", "#f5d361", "#73ffb5", "#8be9fd")):
    canvas.create_rectangle(50 + i * 80, 90, 110 + i * 80, 150, fill=color, outline="", tags=("block",))

def down(event):
    global active
    found = canvas.find_withtag("current")
    active = found[0] if found else None

def drag(event):
    if active:
        x1, y1, x2, y2 = canvas.coords(active)
        canvas.coords(active, event.x - 30, event.y - 30, event.x + 30, event.y + 30)

canvas.tag_bind("block", "<Button-1>", down)
canvas.tag_bind("block", "<B1-Motion>", drag)
root.mainloop()
