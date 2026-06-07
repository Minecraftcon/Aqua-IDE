import tkinter as tk

root = tk.Tk()
root.title("Color Palette")
root.geometry("420x260+30+30")
colors = ["#ff5555", "#f5d361", "#73ffb5", "#8be9fd", "#bd93f9", "#ff79c6"]

preview = tk.Label(root, text="Pick a color", font=("Arial", 18, "bold"), bg="#20252d", fg="white")
preview.pack(fill="both", expand=True, padx=12, pady=12)

bar = tk.Frame(root)
bar.pack(fill="x", padx=12, pady=10)
for color in colors:
    tk.Button(bar, bg=color, activebackground=color, command=lambda c=color: preview.config(bg=c, text=c)).pack(side="left", expand=True, fill="x", padx=3)
root.mainloop()
