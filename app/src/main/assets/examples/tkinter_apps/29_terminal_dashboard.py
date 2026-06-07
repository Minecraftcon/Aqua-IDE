import tkinter as tk
import random

root = tk.Tk()
root.title("Terminal Dashboard")
root.geometry("500x320+20+20")
root.configure(bg="#111318")
log = tk.Text(root, bg="#111318", fg="#73ffb5", insertbackground="white")
log.pack(fill="both", expand=True, padx=12, pady=12)

messages = ["build ok", "running tests", "lint clean", "sync assets", "frame rendered"]

def tick():
    log.insert("end", f"$ {random.choice(messages)}\\n")
    log.see("end")
    root.after(700, tick)

tick()
root.mainloop()
