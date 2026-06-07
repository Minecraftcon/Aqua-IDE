import tkinter as tk

root = tk.Tk()
root.title("PhotoImage")
root.geometry("380x300+30+30")

image = tk.PhotoImage(width=160, height=160)
for y in range(160):
    for x in range(160):
        color = "#%02x%02x%02x" % (x, y, 180)
        image.put(color, (x, y))

tk.Label(root, text="Generated PhotoImage", font=("Arial", 14, "bold")).pack(pady=12)
tk.Label(root, image=image).pack(expand=True)
root.mainloop()
