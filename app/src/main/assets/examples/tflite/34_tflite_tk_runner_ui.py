import tkinter as tk
from pathlib import Path

root = tk.Tk()
root.title("TFLite Runner UI")
root.geometry("460x280+30+30")
status = tk.StringVar(value="Tap Run to inspect ~/models/model.tflite")

def run():
    model = Path.home() / "models" / "model.tflite"
    if not model.exists():
        status.set("Missing ~/models/model.tflite")
        return
    try:
        import tflite_runtime.interpreter as tflite
        interpreter = tflite.Interpreter(model_path=str(model))
        interpreter.allocate_tensors()
        status.set(f"Loaded: {len(interpreter.get_input_details())} input(s)")
    except Exception as error:
        status.set(str(error)[:90])

tk.Label(root, text="TFLite Model Probe", font=("Arial", 18, "bold")).pack(pady=18)
tk.Button(root, text="Run", command=run).pack(pady=12)
tk.Label(root, textvariable=status, wraplength=400).pack(expand=True)
root.mainloop()
