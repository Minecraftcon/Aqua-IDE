#include <android/native_window_jni.h>
#include <android/log.h>
#include <jni.h>
#include <vulkan/vulkan.h>
#include <vulkan/vulkan_android.h>

#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstring>
#include <mutex>
#include <string>
#include <vector>

#define AQUA_LOG_TAG "AquaWaylandVk"
#define AQUA_LOGE(...) __android_log_print(ANDROID_LOG_ERROR, AQUA_LOG_TAG, __VA_ARGS__)
#define AQUA_LOGI(...) __android_log_print(ANDROID_LOG_INFO, AQUA_LOG_TAG, __VA_ARGS__)

namespace {

struct AquaWaylandVk {
    std::mutex mutex;
    ANativeWindow *window = nullptr;
    int width = 1;
    int height = 1;
    bool ready = false;
    std::string status = "Vulkan host idle";

    VkInstance instance = VK_NULL_HANDLE;
    VkSurfaceKHR surface = VK_NULL_HANDLE;
    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
    VkDevice device = VK_NULL_HANDLE;
    VkQueue queue = VK_NULL_HANDLE;
    uint32_t queueFamily = 0;
    VkSwapchainKHR swapchain = VK_NULL_HANDLE;
    VkFormat swapchainFormat = VK_FORMAT_UNDEFINED;
    VkExtent2D extent{1, 1};
    std::vector<VkImage> images;
    VkCommandPool commandPool = VK_NULL_HANDLE;
    VkSemaphore imageAvailable = VK_NULL_HANDLE;
    VkSemaphore renderFinished = VK_NULL_HANDLE;
    VkFence inFlight = VK_NULL_HANDLE;
    uint64_t frame = 0;
};

static std::string vk_result(VkResult result) {
    switch (result) {
        case VK_SUCCESS: return "VK_SUCCESS";
        case VK_NOT_READY: return "VK_NOT_READY";
        case VK_TIMEOUT: return "VK_TIMEOUT";
        case VK_ERROR_OUT_OF_HOST_MEMORY: return "VK_ERROR_OUT_OF_HOST_MEMORY";
        case VK_ERROR_OUT_OF_DEVICE_MEMORY: return "VK_ERROR_OUT_OF_DEVICE_MEMORY";
        case VK_ERROR_INITIALIZATION_FAILED: return "VK_ERROR_INITIALIZATION_FAILED";
        case VK_ERROR_DEVICE_LOST: return "VK_ERROR_DEVICE_LOST";
        case VK_ERROR_NATIVE_WINDOW_IN_USE_KHR: return "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR";
        case VK_ERROR_SURFACE_LOST_KHR: return "VK_ERROR_SURFACE_LOST_KHR";
        case VK_ERROR_OUT_OF_DATE_KHR: return "VK_ERROR_OUT_OF_DATE_KHR";
        case VK_SUBOPTIMAL_KHR: return "VK_SUBOPTIMAL_KHR";
        default: return "VkResult(" + std::to_string(static_cast<int>(result)) + ")";
    }
}

static bool check(VkResult result, AquaWaylandVk *vk, const char *what) {
    if (result == VK_SUCCESS) return true;
    std::string message = std::string(what) + " failed: " + vk_result(result);
    if (vk) vk->status = message;
    AQUA_LOGE("%s", message.c_str());
    return false;
}

static void destroy_swapchain(AquaWaylandVk *vk) {
    if (!vk || vk->device == VK_NULL_HANDLE) return;
    vkDeviceWaitIdle(vk->device);
    if (vk->swapchain != VK_NULL_HANDLE) {
        vkDestroySwapchainKHR(vk->device, vk->swapchain, nullptr);
        vk->swapchain = VK_NULL_HANDLE;
    }
    vk->images.clear();
    vk->swapchainFormat = VK_FORMAT_UNDEFINED;
}

static void destroy_device(AquaWaylandVk *vk) {
    if (!vk) return;
    destroy_swapchain(vk);
    if (vk->device != VK_NULL_HANDLE) {
        if (vk->inFlight != VK_NULL_HANDLE) vkDestroyFence(vk->device, vk->inFlight, nullptr);
        if (vk->renderFinished != VK_NULL_HANDLE) vkDestroySemaphore(vk->device, vk->renderFinished, nullptr);
        if (vk->imageAvailable != VK_NULL_HANDLE) vkDestroySemaphore(vk->device, vk->imageAvailable, nullptr);
        if (vk->commandPool != VK_NULL_HANDLE) vkDestroyCommandPool(vk->device, vk->commandPool, nullptr);
        vkDestroyDevice(vk->device, nullptr);
    }
    vk->inFlight = VK_NULL_HANDLE;
    vk->renderFinished = VK_NULL_HANDLE;
    vk->imageAvailable = VK_NULL_HANDLE;
    vk->commandPool = VK_NULL_HANDLE;
    vk->device = VK_NULL_HANDLE;
    vk->queue = VK_NULL_HANDLE;
    vk->physicalDevice = VK_NULL_HANDLE;
}

static void destroy_instance(AquaWaylandVk *vk) {
    if (!vk) return;
    destroy_device(vk);
    if (vk->surface != VK_NULL_HANDLE && vk->instance != VK_NULL_HANDLE) {
        vkDestroySurfaceKHR(vk->instance, vk->surface, nullptr);
    }
    vk->surface = VK_NULL_HANDLE;
    if (vk->instance != VK_NULL_HANDLE) {
        vkDestroyInstance(vk->instance, nullptr);
    }
    vk->instance = VK_NULL_HANDLE;
    vk->ready = false;
}

static bool create_instance(AquaWaylandVk *vk) {
    const char *extensions[] = {
            VK_KHR_SURFACE_EXTENSION_NAME,
            VK_KHR_ANDROID_SURFACE_EXTENSION_NAME
    };
    VkApplicationInfo appInfo{};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "Aqua Wayland Host";
    appInfo.applicationVersion = VK_MAKE_VERSION(0, 1, 0);
    appInfo.pEngineName = "AquaNative";
    appInfo.engineVersion = VK_MAKE_VERSION(0, 1, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;
    createInfo.enabledExtensionCount = 2;
    createInfo.ppEnabledExtensionNames = extensions;
    return check(vkCreateInstance(&createInfo, nullptr, &vk->instance), vk, "vkCreateInstance");
}

static bool choose_device(AquaWaylandVk *vk) {
    uint32_t deviceCount = 0;
    if (!check(vkEnumeratePhysicalDevices(vk->instance, &deviceCount, nullptr), vk, "vkEnumeratePhysicalDevices") || deviceCount == 0) {
        vk->status = "No Vulkan physical devices";
        return false;
    }
    std::vector<VkPhysicalDevice> devices(deviceCount);
    vkEnumeratePhysicalDevices(vk->instance, &deviceCount, devices.data());
    for (VkPhysicalDevice candidate : devices) {
        uint32_t familyCount = 0;
        vkGetPhysicalDeviceQueueFamilyProperties(candidate, &familyCount, nullptr);
        std::vector<VkQueueFamilyProperties> families(familyCount);
        vkGetPhysicalDeviceQueueFamilyProperties(candidate, &familyCount, families.data());
        for (uint32_t i = 0; i < familyCount; ++i) {
            VkBool32 supportsPresent = VK_FALSE;
            vkGetPhysicalDeviceSurfaceSupportKHR(candidate, i, vk->surface, &supportsPresent);
            if ((families[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) && supportsPresent) {
                vk->physicalDevice = candidate;
                vk->queueFamily = i;
                VkPhysicalDeviceProperties props{};
                vkGetPhysicalDeviceProperties(candidate, &props);
                vk->status = std::string("Vulkan device: ") + props.deviceName;
                return true;
            }
        }
    }
    vk->status = "No graphics+present Vulkan queue";
    return false;
}

static bool create_device(AquaWaylandVk *vk) {
    float priority = 1.0f;
    VkDeviceQueueCreateInfo queueInfo{};
    queueInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueInfo.queueFamilyIndex = vk->queueFamily;
    queueInfo.queueCount = 1;
    queueInfo.pQueuePriorities = &priority;

    const char *extensions[] = {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
    VkDeviceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    createInfo.queueCreateInfoCount = 1;
    createInfo.pQueueCreateInfos = &queueInfo;
    createInfo.enabledExtensionCount = 1;
    createInfo.ppEnabledExtensionNames = extensions;
    if (!check(vkCreateDevice(vk->physicalDevice, &createInfo, nullptr, &vk->device), vk, "vkCreateDevice")) return false;
    vkGetDeviceQueue(vk->device, vk->queueFamily, 0, &vk->queue);

    VkCommandPoolCreateInfo poolInfo{};
    poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
    poolInfo.queueFamilyIndex = vk->queueFamily;
    poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
    if (!check(vkCreateCommandPool(vk->device, &poolInfo, nullptr, &vk->commandPool), vk, "vkCreateCommandPool")) return false;

    VkSemaphoreCreateInfo semInfo{};
    semInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
    if (!check(vkCreateSemaphore(vk->device, &semInfo, nullptr, &vk->imageAvailable), vk, "vkCreateSemaphore(image)")) return false;
    if (!check(vkCreateSemaphore(vk->device, &semInfo, nullptr, &vk->renderFinished), vk, "vkCreateSemaphore(render)")) return false;

    VkFenceCreateInfo fenceInfo{};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;
    return check(vkCreateFence(vk->device, &fenceInfo, nullptr, &vk->inFlight), vk, "vkCreateFence");
}

static VkSurfaceFormatKHR choose_format(const std::vector<VkSurfaceFormatKHR> &formats) {
    for (const auto &format : formats) {
        if (format.format == VK_FORMAT_R8G8B8A8_UNORM || format.format == VK_FORMAT_B8G8R8A8_UNORM) {
            return format;
        }
    }
    return formats.empty() ? VkSurfaceFormatKHR{VK_FORMAT_R8G8B8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR} : formats[0];
}

static bool create_swapchain(AquaWaylandVk *vk) {
    destroy_swapchain(vk);

    VkSurfaceCapabilitiesKHR caps{};
    if (!check(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vk->physicalDevice, vk->surface, &caps), vk, "vkGetPhysicalDeviceSurfaceCapabilitiesKHR")) return false;

    uint32_t formatCount = 0;
    vkGetPhysicalDeviceSurfaceFormatsKHR(vk->physicalDevice, vk->surface, &formatCount, nullptr);
    std::vector<VkSurfaceFormatKHR> formats(std::max(1u, formatCount));
    if (formatCount > 0) vkGetPhysicalDeviceSurfaceFormatsKHR(vk->physicalDevice, vk->surface, &formatCount, formats.data());
    VkSurfaceFormatKHR chosen = choose_format(formats);

    uint32_t presentModeCount = 0;
    vkGetPhysicalDeviceSurfacePresentModesKHR(vk->physicalDevice, vk->surface, &presentModeCount, nullptr);
    std::vector<VkPresentModeKHR> presentModes(presentModeCount);
    if (presentModeCount > 0) vkGetPhysicalDeviceSurfacePresentModesKHR(vk->physicalDevice, vk->surface, &presentModeCount, presentModes.data());
    VkPresentModeKHR presentMode = VK_PRESENT_MODE_FIFO_KHR;
    for (VkPresentModeKHR mode : presentModes) {
        if (mode == VK_PRESENT_MODE_MAILBOX_KHR) {
            presentMode = mode;
            break;
        }
    }

    VkExtent2D extent{};
    if (caps.currentExtent.width != UINT32_MAX) {
        extent = caps.currentExtent;
    } else {
        extent.width = static_cast<uint32_t>(std::max(1, vk->width));
        extent.height = static_cast<uint32_t>(std::max(1, vk->height));
        extent.width = std::max(caps.minImageExtent.width, std::min(caps.maxImageExtent.width, extent.width));
        extent.height = std::max(caps.minImageExtent.height, std::min(caps.maxImageExtent.height, extent.height));
    }

    uint32_t minImages = std::max(caps.minImageCount + 1, 2u);
    if (caps.maxImageCount > 0) minImages = std::min(minImages, caps.maxImageCount);

    VkSwapchainCreateInfoKHR createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
    createInfo.surface = vk->surface;
    createInfo.minImageCount = minImages;
    createInfo.imageFormat = chosen.format;
    createInfo.imageColorSpace = chosen.colorSpace;
    createInfo.imageExtent = extent;
    createInfo.imageArrayLayers = 1;
    createInfo.imageUsage = VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
    createInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
    createInfo.preTransform = caps.currentTransform;
    createInfo.compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
    createInfo.presentMode = presentMode;
    createInfo.clipped = VK_TRUE;
    createInfo.oldSwapchain = VK_NULL_HANDLE;
    if (!check(vkCreateSwapchainKHR(vk->device, &createInfo, nullptr, &vk->swapchain), vk, "vkCreateSwapchainKHR")) return false;

    uint32_t imageCount = 0;
    vkGetSwapchainImagesKHR(vk->device, vk->swapchain, &imageCount, nullptr);
    vk->images.resize(imageCount);
    vkGetSwapchainImagesKHR(vk->device, vk->swapchain, &imageCount, vk->images.data());
    vk->swapchainFormat = chosen.format;
    vk->extent = extent;
    vk->ready = true;
    vk->status += "  swapchain=" + std::to_string(extent.width) + "x" + std::to_string(extent.height);
    return true;
}

static bool ensure_ready(AquaWaylandVk *vk) {
    if (!vk || !vk->window) return false;
    if (vk->ready) return true;
    destroy_instance(vk);
    if (!create_instance(vk)) return false;

    VkAndroidSurfaceCreateInfoKHR surfaceInfo{};
    surfaceInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
    surfaceInfo.window = vk->window;
    if (!check(vkCreateAndroidSurfaceKHR(vk->instance, &surfaceInfo, nullptr, &vk->surface), vk, "vkCreateAndroidSurfaceKHR")) return false;
    if (!choose_device(vk)) return false;
    if (!create_device(vk)) return false;
    return create_swapchain(vk);
}

static void transition(VkCommandBuffer cmd, VkImage image, VkImageLayout oldLayout, VkImageLayout newLayout,
                       VkAccessFlags srcAccess, VkAccessFlags dstAccess,
                       VkPipelineStageFlags srcStage, VkPipelineStageFlags dstStage) {
    VkImageMemoryBarrier barrier{};
    barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrier.oldLayout = oldLayout;
    barrier.newLayout = newLayout;
    barrier.srcAccessMask = srcAccess;
    barrier.dstAccessMask = dstAccess;
    barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image;
    barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    barrier.subresourceRange.baseMipLevel = 0;
    barrier.subresourceRange.levelCount = 1;
    barrier.subresourceRange.baseArrayLayer = 0;
    barrier.subresourceRange.layerCount = 1;
    vkCmdPipelineBarrier(cmd, srcStage, dstStage, 0, 0, nullptr, 0, nullptr, 1, &barrier);
}

static void hsv_to_rgb(float h, float &r, float &g, float &b) {
    float c = 0.46f;
    float x = c * (1.0f - std::fabs(std::fmod(h * 6.0f, 2.0f) - 1.0f));
    float m = 0.08f;
    if (h < 1.0f / 6.0f) { r = c; g = x; b = 0; }
    else if (h < 2.0f / 6.0f) { r = x; g = c; b = 0; }
    else if (h < 3.0f / 6.0f) { r = 0; g = c; b = x; }
    else if (h < 4.0f / 6.0f) { r = 0; g = x; b = c; }
    else if (h < 5.0f / 6.0f) { r = x; g = 0; b = c; }
    else { r = c; g = 0; b = x; }
    r += m; g += m; b += m;
}

static bool draw_frame(AquaWaylandVk *vk) {
    if (!ensure_ready(vk)) return false;
    if (vk->swapchain == VK_NULL_HANDLE || vk->images.empty()) return false;

    VkResult wait = vkWaitForFences(vk->device, 1, &vk->inFlight, VK_TRUE, 1000000000ULL);
    if (wait != VK_SUCCESS) return check(wait, vk, "vkWaitForFences");
    vkResetFences(vk->device, 1, &vk->inFlight);

    uint32_t imageIndex = 0;
    VkResult acquire = vkAcquireNextImageKHR(vk->device, vk->swapchain, UINT64_MAX, vk->imageAvailable, VK_NULL_HANDLE, &imageIndex);
    if (acquire == VK_ERROR_OUT_OF_DATE_KHR || acquire == VK_SUBOPTIMAL_KHR) {
        vk->ready = false;
        return create_swapchain(vk);
    }
    if (!check(acquire, vk, "vkAcquireNextImageKHR")) return false;

    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.commandPool = vk->commandPool;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandBufferCount = 1;
    VkCommandBuffer cmd = VK_NULL_HANDLE;
    if (!check(vkAllocateCommandBuffers(vk->device, &allocInfo, &cmd), vk, "vkAllocateCommandBuffers")) return false;

    VkCommandBufferBeginInfo beginInfo{};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
    vkBeginCommandBuffer(cmd, &beginInfo);
    VkImage image = vk->images[imageIndex];
    transition(cmd, image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
               0, VK_ACCESS_TRANSFER_WRITE_BIT,
               VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);

    float r, g, b;
    hsv_to_rgb(std::fmod(static_cast<float>(vk->frame) / 180.0f, 1.0f), r, g, b);
    VkClearColorValue color{{r, g, b, 1.0f}};
    VkImageSubresourceRange range{};
    range.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    range.baseMipLevel = 0;
    range.levelCount = 1;
    range.baseArrayLayer = 0;
    range.layerCount = 1;
    vkCmdClearColorImage(cmd, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, &color, 1, &range);

    transition(cmd, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
               VK_ACCESS_TRANSFER_WRITE_BIT, 0,
               VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT);
    vkEndCommandBuffer(cmd);

    VkPipelineStageFlags waitStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
    VkSubmitInfo submitInfo{};
    submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
    submitInfo.waitSemaphoreCount = 1;
    submitInfo.pWaitSemaphores = &vk->imageAvailable;
    submitInfo.pWaitDstStageMask = &waitStage;
    submitInfo.commandBufferCount = 1;
    submitInfo.pCommandBuffers = &cmd;
    submitInfo.signalSemaphoreCount = 1;
    submitInfo.pSignalSemaphores = &vk->renderFinished;
    VkResult submit = vkQueueSubmit(vk->queue, 1, &submitInfo, vk->inFlight);
    if (!check(submit, vk, "vkQueueSubmit")) {
        vkFreeCommandBuffers(vk->device, vk->commandPool, 1, &cmd);
        return false;
    }

    VkPresentInfoKHR presentInfo{};
    presentInfo.sType = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
    presentInfo.waitSemaphoreCount = 1;
    presentInfo.pWaitSemaphores = &vk->renderFinished;
    presentInfo.swapchainCount = 1;
    presentInfo.pSwapchains = &vk->swapchain;
    presentInfo.pImageIndices = &imageIndex;
    VkResult present = vkQueuePresentKHR(vk->queue, &presentInfo);
    vk->frame++;
    vkFreeCommandBuffers(vk->device, vk->commandPool, 1, &cmd);
    if (present == VK_ERROR_OUT_OF_DATE_KHR || present == VK_SUBOPTIMAL_KHR) {
        vk->ready = false;
        return create_swapchain(vk);
    }
    return check(present, vk, "vkQueuePresentKHR");
}

static AquaWaylandVk *from_handle(jlong handle) {
    return reinterpret_cast<AquaWaylandVk *>(static_cast<uintptr_t>(handle));
}

} // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeCreate(JNIEnv *, jobject) {
    return reinterpret_cast<jlong>(new AquaWaylandVk());
}

extern "C" JNIEXPORT void JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeSurfaceCreated(JNIEnv *env, jobject, jlong handle, jobject surface, jint width, jint height) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return;
    std::lock_guard<std::mutex> lock(vk->mutex);
    if (vk->window) ANativeWindow_release(vk->window);
    vk->window = ANativeWindow_fromSurface(env, surface);
    vk->width = std::max(1, static_cast<int>(width));
    vk->height = std::max(1, static_cast<int>(height));
    vk->ready = false;
    ensure_ready(vk);
}

extern "C" JNIEXPORT void JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeSurfaceChanged(JNIEnv *, jobject, jlong handle, jint width, jint height) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return;
    std::lock_guard<std::mutex> lock(vk->mutex);
    vk->width = std::max(1, static_cast<int>(width));
    vk->height = std::max(1, static_cast<int>(height));
    vk->ready = false;
    create_swapchain(vk);
}

extern "C" JNIEXPORT void JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeDraw(JNIEnv *, jobject, jlong handle) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return;
    std::lock_guard<std::mutex> lock(vk->mutex);
    draw_frame(vk);
}

extern "C" JNIEXPORT void JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeSurfaceDestroyed(JNIEnv *, jobject, jlong handle) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return;
    std::lock_guard<std::mutex> lock(vk->mutex);
    destroy_instance(vk);
    if (vk->window) {
        ANativeWindow_release(vk->window);
        vk->window = nullptr;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeStatus(JNIEnv *env, jobject, jlong handle) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return env->NewStringUTF("Vulkan host unavailable");
    std::lock_guard<std::mutex> lock(vk->mutex);
    std::string status = vk->status + "  frames=" + std::to_string(vk->frame);
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_andropy_ide_MainActivity_00024AquaWaylandSurfaceView_nativeDestroy(JNIEnv *, jobject, jlong handle) {
    AquaWaylandVk *vk = from_handle(handle);
    if (!vk) return;
    {
        std::lock_guard<std::mutex> lock(vk->mutex);
        destroy_instance(vk);
        if (vk->window) {
            ANativeWindow_release(vk->window);
            vk->window = nullptr;
        }
    }
    delete vk;
}
