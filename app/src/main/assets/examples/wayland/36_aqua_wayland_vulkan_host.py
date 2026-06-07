import time

import aquawayland


print("Opening Aqua native Wayland/Vulkan host...")
print(aquawayland.open("Aqua Wayland Vulkan Host"))
print("Environment:", aquawayland.environment())
print("The host surface should survive rotation and keep presenting frames.")
print("Press Ctrl+C in terminal or Back in Aqua to close.")

try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    pass
finally:
    print(aquawayland.close())
