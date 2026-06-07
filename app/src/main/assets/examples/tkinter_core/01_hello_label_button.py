import tkinter as tk

root = tk.Tk()
root.title("Label + Button")
root.geometry("360x220+30+30")

count = tk.IntVar(value=0)
title = tk.Label(root, text="Hello from Aqua Tk", font=("Arial", 18, "bold"), fg="#f5d361", bg="#20252d")
title.pack(fill="x", pady=18)

label = tk.Label(root, textvariable=count, font=("Arial", 38), fg="#8be9fd", bg="#20252d")
label.pack(expand=True)

tk.Button(root, text="Tap", command=lambda: count.set(count.get() + 1)).pack(pady=12)
root.configure(bg="#20252d")
root.mainloop()
