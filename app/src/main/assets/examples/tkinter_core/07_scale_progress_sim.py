import tkinter as tk

root = tk.Tk()
root.title("Scale + Progress")
root.geometry("420x240+30+30")

value = tk.IntVar(value=35)
tk.Label(root, text="Progress controlled by Scale", font=("Arial", 14, "bold")).pack(pady=12)
scale = tk.Scale(root, from_=0, to=100, orient="horizontal", variable=value)
scale.pack(fill="x", padx=24)

canvas = tk.Canvas(root, width=360, height=36, bg="#20252d", highlightthickness=0)
canvas.pack(pady=20)

def draw(*_):
    canvas.delete("all")
    canvas.create_rectangle(0, 0, 360, 36, fill="#2f3744", outline="")
    canvas.create_rectangle(0, 0, int(value.get() * 3.6), 36, fill="#73ffb5", outline="")
    canvas.create_text(180, 18, text=f"{value.get()}%", fill="white")

value.trace_add("write", draw)
draw()
root.mainloop()
