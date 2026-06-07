import tkinter as tk

root = tk.Tk()
root.title("Entry Form")
root.geometry("420x260+40+40")
root.configure(bg="#20252d")

name = tk.StringVar()
email = tk.StringVar()
status = tk.StringVar(value="Fill the fields")

frame = tk.Frame(root, bg="#20252d")
frame.pack(fill="both", expand=True, padx=18, pady=18)

for row, text in enumerate(("Name", "Email")):
    tk.Label(frame, text=text, fg="#d8dee9", bg="#20252d", anchor="w").grid(row=row, column=0, sticky="ew", pady=8)

tk.Entry(frame, textvariable=name).grid(row=0, column=1, sticky="ew", pady=8)
tk.Entry(frame, textvariable=email).grid(row=1, column=1, sticky="ew", pady=8)
frame.columnconfigure(1, weight=1)

def submit():
    status.set(f"Saved: {name.get() or 'anonymous'}")

tk.Button(frame, text="Submit", command=submit).grid(row=2, column=1, sticky="e", pady=12)
tk.Label(frame, textvariable=status, fg="#73ffb5", bg="#20252d").grid(row=3, column=0, columnspan=2, sticky="w")
root.mainloop()
