import tkinter as tk

root = tk.Tk()
root.title("Spinbox")
root.geometry("360x220+40+40")

size = tk.IntVar(value=14)
theme = tk.StringVar(value="dark")
label = tk.Label(root, text="Spinbox changes me", font=("Arial", size.get()))
label.pack(expand=True)

def update():
    label.config(font=("Arial", size.get()), text=f"{theme.get()} / {size.get()}px")

tk.Spinbox(root, from_=8, to=42, textvariable=size, command=update).pack(pady=8)
tk.Spinbox(root, values=("dark", "light", "blue", "green"), textvariable=theme, command=update).pack(pady=8)
root.mainloop()
