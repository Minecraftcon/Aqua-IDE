from pathlib import Path
import numpy as np

model = Path.home() / "models" / "model.tflite"
if not model.exists():
    print("Missing ~/models/model.tflite")
    raise SystemExit(0)

import tflite_runtime.interpreter as tflite
interpreter = tflite.Interpreter(model_path=str(model))
interpreter.allocate_tensors()
input_info = interpreter.get_input_details()[0]
output_info = interpreter.get_output_details()[0]

sample = np.zeros(input_info["shape"], dtype=input_info["dtype"])
interpreter.set_tensor(input_info["index"], sample)
interpreter.invoke()
print("Output:", interpreter.get_tensor(output_info["index"]))
