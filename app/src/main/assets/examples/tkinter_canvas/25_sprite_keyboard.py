import tkinter as tk

root = tk.Tk()
root.title("Keyboard Sprite")
root.geometry("420x300+30+30")
canvas = tk.Canvas(root, bg="#20252d", highlightthickness=0)
canvas.pack(fill="both", expand=True)
player = canvas.create_rectangle(190, 130, 230, 170, fill="#73ffb5", outline="white")

def move(event):
    step = 12
    key = event.keysym
    if key == "Left":
        canvas.move(player, -step, 0)
    elif key == "Right":
        canvas.move(player, step, 0)
    elif key == "Up":
        canvas.move(player, 0, -step)
    elif key == "Down":
        canvas.move(player, 0, step)

root.bind("<KeyPress>", move)
tk.Label(root, text="Use arrow keys", bg="#20252d", fg="white").place(x=12, y=12)
root.mainloop()
