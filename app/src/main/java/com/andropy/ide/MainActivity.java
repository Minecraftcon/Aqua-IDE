package com.andropy.ide;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;
import com.termux.view.TerminalViewClient;
import com.github.luben.zstd.ZstdInputStream;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.lang.styling.MappedSpans;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.lang.styling.color.ConstColor;
import io.github.rosemoe.sora.lang.styling.inlayHint.InlayHintsContainer;
import io.github.rosemoe.sora.lang.styling.inlayHint.TextInlayHint;
import io.github.rosemoe.sora.lang.styling.line.LineBackground;
import io.github.rosemoe.sora.lang.styling.line.LineSideIcon;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SelectionMovement;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class MainActivity extends Activity {
    private static final String TAG_AI = "AndroPyAI";
    private static final String PREFS = "andropy_editor";
    private static final String DEFAULT_FILE = "new.py";
    private static final String PREFIX_DIR = "usr";
    private static final String RUNTIME_BASIC_VERSION = "andropy-basic-runtime-8";
    private static final String RUNTIME_EXTENDED_VERSION = "andropy-extended-runtime-10";
    private static final String RUNTIME_BASIC_RELEASE_BASE = "https://github.com/Minecraftcon/Aqua-IDE/releases/download/runtime-v8/";
    private static final String RUNTIME_EXTENDED_RELEASE_BASE = "https://github.com/Minecraftcon/Aqua-IDE/releases/download/runtime-v9/";
    private static final String AQUA_PYTHON_INDEX = "https://minecraftcon.github.io/Aqua-IDE/python/simple";
    private static final String PYPI_INDEX = "https://pypi.org/simple";
    private static final String AQUA_APT_REPO = "https://minecraftcon.github.io/Aqua-IDE/apt";
    private static final String RUNTIME_BASIC_X86_64_ZIP = "aqua-runtime-x86_64-v8.zip";
    private static final String RUNTIME_BASIC_ARM64_ZIP = "aqua-runtime-arm64-v8a-v8.zip";
    private static final String RUNTIME_BASIC_X86_ZIP = "aqua-runtime-x86-v8.zip";
    private static final String RUNTIME_BASIC_ARMV7_ZIP = "aqua-runtime-armeabi-v7a-v8.zip";
    private static final String RUNTIME_EXTENDED_X86_64_ZIP = "aqua-runtime-x86_64-v9.tar.zst";
    private static final String RUNTIME_EXTENDED_ARM64_ZIP = "aqua-runtime-arm64-v8a-v9.tar.zst";
    private static final String RUNTIME_EXTENDED_X86_ZIP = "aqua-runtime-x86-v9.tar.zst";
    private static final String RUNTIME_EXTENDED_ARMV7_ZIP = "aqua-runtime-armeabi-v7a-v9.tar.zst";
    private static final int RUNTIME_BASIC = 0;
    private static final int RUNTIME_EXTENDED = 1;
    private static final int RUNTIME_MAX = 2;
    private static final int BG = Color.rgb(50, 54, 61);
    private static final int BAR = Color.rgb(35, 39, 45);
    private static final int EDITOR_BG = Color.rgb(58, 63, 72);
    private static final int GUTTER_LINE = Color.rgb(104, 111, 124);
    private static final int GUTTER_TEXT = Color.rgb(204, 212, 223);
    private static final int ACTIVE_LINE = Color.argb(70, 220, 232, 255);
    private static final int TEXT = Color.rgb(250, 252, 255);
    private static final int MUTED = Color.rgb(212, 219, 229);
    private static final int ACCENT = Color.rgb(106, 237, 156);
    private static final int KEYWORD = Color.rgb(118, 190, 255);
    private static final int STRING = Color.rgb(255, 218, 115);
    private static final int COMMENT = Color.rgb(167, 178, 191);
    private static final int NUMBER = Color.rgb(255, 153, 177);
    private static final int FUNCTION = Color.rgb(123, 242, 198);
    private static final int CLASS_NAME = Color.rgb(255, 194, 122);
    private static final int BUILTIN = Color.rgb(188, 166, 255);
    private static final int DECORATOR = Color.rgb(255, 232, 135);
    private static final int IMPORT_NAME = Color.rgb(146, 232, 255);
    private static final int COMPLETION_BG = Color.rgb(39, 43, 50);
    private static final int COMPLETION_HOVER = Color.rgb(62, 69, 81);
    private static final int COMPLETION_MAX_VISIBLE_ROWS = 5;
    private static final int SETTINGS_BG = Color.rgb(48, 45, 42);
    private static final int SETTINGS_PANEL = Color.rgb(58, 54, 50);
    private static final int SETTINGS_FIELD = Color.rgb(39, 37, 35);
    private static final int GHOST_TEXT = Color.argb(138, 236, 225, 203);
    private static final int AI_GHOST_TEXT = Color.argb(158, 170, 232, 255);
    private static final long AI_COMPLETION_IDLE_MS = 2000L;
    private static final int AI_COMPLETION_TIMEOUT_MS = 10000;
    private static final int AI_COMPLETION_MAX_LINES = 5;
    private static final long AI_COMPLETION_FALLBACK_RETRY_MS = 1200L;
    private static final long AI_COMPLETION_MAX_RETRY_MS = 60000L;
    private static final int PROJECT_PANEL_BG = Color.rgb(40, 44, 52);
    private static final int PROJECT_PANEL_HEADER = Color.rgb(34, 38, 46);
    private static final int OPEN_FILE_STRIP = Color.rgb(43, 49, 59);
    private static final int OPEN_FILE_ACTIVE = Color.rgb(58, 66, 79);
    private static final int OPEN_FILE_BLUE_LINE = Color.rgb(82, 169, 255);
    private static final int AI_PANEL_BG = Color.rgb(38, 41, 48);
    private static final int AI_PANEL_HEADER = Color.rgb(31, 35, 43);
    private static final int AI_USER_BUBBLE = Color.rgb(63, 72, 86);
    private static final int AI_ASSISTANT_BUBBLE = Color.rgb(48, 54, 64);
    private static final int AI_TOOL_BUBBLE = Color.rgb(37, 47, 42);
    private static final int AI_PURPLE = Color.rgb(188, 142, 255);
    private static final int AI_FEED_BG = Color.rgb(31, 34, 42);
    private static final int AI_FEED_BORDER = Color.rgb(91, 74, 126);
    private static final int AI_DIFF_ADD_BG = Color.argb(44, 128, 238, 166);
    private static final int AI_DIFF_ADD_GUTTER = Color.rgb(24, 92, 54);
    private static final int AI_DIFF_DEL_BG = Color.argb(40, 255, 125, 134);
    private static final int AI_DIFF_DEL_GUTTER = Color.rgb(108, 32, 42);
    private static final int AI_DIFF_ADD_DOT = Color.rgb(75, 230, 139);
    private static final int AI_DIFF_DEL_DOT = Color.rgb(255, 91, 107);
    private static final int AI_EDIT_PREVIEW_BG = Color.rgb(28, 31, 38);
    private static final int AI_EDIT_PREVIEW_BORDER = Color.rgb(70, 76, 90);
    private static final int AI_EDIT_PREVIEW_ADD = Color.argb(72, 62, 178, 104);
    private static final int AI_EDIT_PREVIEW_DEL = Color.argb(70, 186, 66, 76);
    private static final int AI_INPUT_BASE_HEIGHT_DP = 104;
    private static final int AI_COMPOSER_EXTRA_HEIGHT_DP = 44;
    private static final int AI_EDIT_INPUT_TOP_PADDING_DP = 42;
    private static final int AI_AGENT_MAX_CYCLES = 5;
    private static final int AI_AGENT_MAX_EDITS = 5;
    private static final int AI_AGENT_MAX_EDIT_BYTES = 384 * 1024;
    private static final float TERMINAL_TEXT_SP_DEFAULT = 15f;
    private static final float TERMINAL_TEXT_SP_MIN = 9f;
    private static final float TERMINAL_TEXT_SP_MAX = 30f;
    private static final int BOOTSTRAP_IO_BUFFER = 16384;
    private static final long BOOTSTRAP_UI_INTERVAL_MS = 180L;
    private static final int YELLOW_DIVIDER = Color.rgb(255, 207, 83);
    private static final int PANEL_BG = Color.rgb(47, 47, 47);
    private static final int PANEL_HEADER = Color.rgb(21, 47, 70);
    private static final int PANEL_DIVIDER = Color.rgb(70, 70, 70);
    private static final int PANEL_ICON = Color.rgb(202, 202, 202);
    private static final int PANEL_SECTION = Color.rgb(190, 190, 190);
    private static final int SCRIM = Color.argb(96, 0, 0, 0);
    private static final int PANEL_WIDTH_DP = 280;
    private static final int PANEL_OPEN_MS = 520;
    private static final int PANEL_CLOSE_MS = 360;
    private static final int BOOT_BG = Color.rgb(35, 37, 42);
    private static final int BOOT_PANEL = Color.rgb(42, 44, 50);
    private static final int BOOT_TEXT = Color.rgb(232, 238, 247);

    private static final Pattern COMMENT_PATTERN = Pattern.compile("#.*$", Pattern.MULTILINE);
    private static final Pattern DECORATOR_PATTERN = Pattern.compile("(?m)^\\s*@[A-Za-z_][A-Za-z0-9_\\.]*");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\bdef\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern CLASS_PATTERN = Pattern.compile("\\bclass\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\b(?:import|from)\\s+([A-Za-z_][A-Za-z0-9_\\.]*)");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(?m)^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=");
    private static final Pattern FUNCTION_PARAMS_PATTERN = Pattern.compile("\\bdef\\s+[A-Za-z_][A-Za-z0-9_]*\\s*\\(([^)]*)\\)");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b(and|as|assert|async|await|break|class|continue|def|del|elif|else|except|False|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|or|pass|raise|return|True|try|while|with|yield)\\b"
    );
    private static final Pattern BUILTIN_PATTERN = Pattern.compile(
            "\\b(abs|all|any|bool|breakpoint|bytearray|bytes|callable|chr|classmethod|compile|complex|dict|dir|divmod|enumerate|eval|exec|filter|float|format|frozenset|getattr|globals|hasattr|hash|help|hex|id|input|int|isinstance|issubclass|iter|len|list|locals|map|max|memoryview|min|next|object|oct|open|ord|pow|print|property|range|repr|reversed|round|set|setattr|slice|sorted|staticmethod|str|sum|super|tuple|type|vars|zip|Exception|ValueError|TypeError|RuntimeError|Path)\\b"
    );
    private static final String[] PYTHON_KEYWORDS = new String[]{
            "and", "as", "assert", "async", "await", "break", "class", "continue", "def",
            "del", "elif", "else", "except", "False", "finally", "for", "from", "global",
            "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or", "pass",
            "raise", "return", "True", "try", "while", "with", "yield"
    };
    private static final String[] PYTHON_BUILTINS = new String[]{
            "abs", "all", "any", "bool", "bytes", "dict", "enumerate", "float", "int",
            "isinstance", "len", "list", "map", "max", "min", "open", "Path", "print",
            "range", "set", "sorted", "str", "sum", "super", "tuple", "type", "zip"
    };
    private static final CompletionItem[] PYTHON_SNIPPETS = new CompletionItem[]{
            new CompletionItem("print(...)", "print($0)", "snippet"),
            new CompletionItem("def function", "def $0():\n    pass", "snippet"),
            new CompletionItem("class", "class $0:\n    pass", "snippet"),
            new CompletionItem("if", "if $0:\n    pass", "snippet"),
            new CompletionItem("for in", "for $0 in range():\n    pass", "snippet"),
            new CompletionItem("while", "while $0:\n    pass", "snippet"),
            new CompletionItem("try except", "try:\n    $0\nexcept Exception as exc:\n    print(exc)", "snippet"),
            new CompletionItem("with open", "with open($0) as file:\n    pass", "snippet"),
            new CompletionItem("from pathlib import Path", "from pathlib import Path", "import"),
            new CompletionItem("main guard", "if __name__ == \"__main__\":\n    main()", "snippet")
    };
    private static final AutocompleteProvider[] AUTOCOMPLETE_PROVIDERS = new AutocompleteProvider[]{
            new AutocompleteProvider("local", "Local rules", "Built-in symbols, snippets, and Python helpers.", "", "", false, false, false),
            new AutocompleteProvider("openai", "OpenAI compatible", "Any OpenAI-style /v1/chat/completions endpoint.", "qwen2.5-coder-7b", "https://api.openai.com/v1/chat/completions", true, false, true),
            new AutocompleteProvider("groq", "Groq", "Fast hosted completions. Uses a single non-compound model.", "llama-3.1-8b-instant", "https://api.groq.com/openai/v1/chat/completions", true, true, true),
            new AutocompleteProvider("koboldcpp", "KoboldCpp", "LAN/self-hosted KoboldCpp server.", "local-model", "http://127.0.0.1:5001/api/v1/generate", false, true, false),
            new AutocompleteProvider("ollama", "Ollama", "Local server over Wi-Fi or localhost.", "qwen2.5-coder:1.5b", "http://127.0.0.1:11434/api/generate", false, true, true),
            new AutocompleteProvider("tabby", "Tabby", "Self-hosted code completion server.", "tabby-default", "http://127.0.0.1:8080/v1/completions", false, true, true),
            new AutocompleteProvider("openrouter", "OpenRouter", "Hosted model gateway; choose compact coder models.", "qwen/qwen-2.5-coder-7b-instruct", "https://openrouter.ai/api/v1/chat/completions", true, false, true),
            new AutocompleteProvider("anthropic", "Anthropic", "Claude-compatible short code suggestions.", "claude-3-5-haiku-latest", "https://api.anthropic.com/v1/messages", true, false, true),
            new AutocompleteProvider("gemini", "Google Gemini", "Gemini API for concise code continuations.", "gemini-1.5-flash", "https://generativelanguage.googleapis.com/v1beta/models", true, false, true),
            new AutocompleteProvider("mistral", "Mistral", "Mistral API small-model completions.", "codestral-latest", "https://api.mistral.ai/v1/chat/completions", true, false, true),
            new AutocompleteProvider("cohere", "Cohere", "Command models through Cohere Chat v2.", "command-a-03-2025", "https://api.cohere.com/v2/chat", true, false, true)
    };
    private static final String[] COREUTILS_COMMANDS = new String[]{
            "[", "b2sum", "base32", "base64", "basename", "basenc", "cat", "chcon",
            "chgrp", "chmod", "chown", "chroot", "cksum", "comm", "coreutils", "cp",
            "csplit", "cut", "date", "dd", "dir", "dircolors", "dirname", "du",
            "echo", "env", "expand", "expr", "factor", "false", "fmt", "fold",
            "groups", "head", "id", "install", "join", "kill", "link", "ln",
            "logname", "ls", "md5sum", "mkdir", "mkfifo", "mknod", "mktemp", "mv",
            "nice", "nl", "nohup", "nproc", "numfmt", "od", "paste", "pathchk",
            "pr", "printenv", "printf", "ptx", "pwd", "readlink", "realpath", "rm",
            "rmdir", "runcon", "seq", "sha1sum", "sha224sum", "sha256sum",
            "sha384sum", "sha512sum", "shred", "shuf", "sleep", "sort", "split",
            "stat", "stdbuf", "stty", "sum", "sync", "tac", "tail", "tee", "test",
            "timeout", "touch", "tr", "true", "truncate", "tsort", "tty", "uname",
            "unexpand", "uniq", "unlink", "vdir", "wc", "whoami", "yes"
    };

    private SharedPreferences prefs;
    private CodeEditor editor;
    private PopupWindow completionPopup;
    private LinearLayout completionList;
    private final ArrayList<CompletionItem> activeCompletions = new ArrayList<>();
    private View scrim;
    private LinearLayout sidePanel;
    private View projectScrim;
    private LinearLayout projectPanel;
    private TextView projectHandle;
    private View aiChatScrim;
    private LinearLayout aiChatPanel;
    private TextView aiChatHandle;
    private LinearLayout aiChatMessages;
    private ScrollView aiChatScroll;
    private EditText aiChatInput;
    private FrameLayout aiChatInputBox;
    private LinearLayout aiEditBanner;
    private TextView aiEditBannerText;
    private TextView aiChatStatusText;
    private TextView aiChatFeedText;
    private TextView aiChatTitleText;
    private TextView aiRevertChangesButton;
    private TextView aiRedoChangesButton;
    private EditText aiRecentSearchInput;
    private TextView aiModelNameText;
    private TextView aiProviderNameText;
    private AiSendButton aiSendButton;
    private Button aiModelButton;
    private Button aiAptButton;
    private TextView panelFileName;
    private TextView panelLine;
    private TextView panelOffset;
    private com.termux.view.TerminalView termuxTerminalView;
    private TerminalSession termuxSession;
    private BootstrapOrbView bootstrapOrb;
    private LinearLayout bootstrapVisualPanel;
    private ScrollView bootstrapOutputScroll;
    private TextView bootstrapStageText;
    private TextView bootstrapOutputText;
    private LinearLayout bootstrapRuntimeChoices;
    private Button bootstrapDownloadButton;
    private TextView pipStatusText;
    private TextView pipOutputText;
    private LinearLayout pipPackageList;
    private EditText pipInput;
    private Button pipInstallButton;
    private View pipScanProgressTrack;
    private View pipScanProgressFill;
    private View bootstrapProgressFill;
    private final StringBuilder bootstrapOutput = new StringBuilder();
    private final StringBuilder pipOutput = new StringBuilder();
    private int pipScanGeneration;
    private File prefixRoot;
    private File prefixRealRoot;
    private File homeRoot;
    private File homeRealRoot;
    private File binRoot;
    private File etcRoot;
    private File profileRoot;
    private File includeRoot;
    private File libRoot;
    private File libexecRoot;
    private File optRoot;
    private File shareRoot;
    private File tmpRoot;
    private File varRoot;
    private File varCacheRoot;
    private File varLibRoot;
    private File varLibDpkgRoot;
    private File varLibAptListsRoot;
    private File varLogRoot;
    private File varRunRoot;
    private File varTmpRoot;
    private boolean highlighting;
    private boolean applyingHelperEdit;
    private boolean applyingCompletion;
    private boolean panelOpen;
    private boolean projectPanelOpen;
    private boolean aiChatOpen;
    private boolean aiChatSending;
    private boolean aiRecentChatsVisible;
    private String aiCurrentChatId = "";
    private String aiCurrentChatTitle = "";
    private boolean aiChatTitleRequested;
    private int aiEditingMessageIndex = -1;
    private String aiEditingOriginalText = "";
    private boolean aiAptToolsEnabled;
    private boolean aiFeedVisible;
    private boolean aiFeedExpanded;
    private boolean aiFeedExecuting;
    private String aiFeedSummary = "";
    private int aiFeedInsertAfter = -1;
    private final ArrayList<AiFlowStep> aiFlowSteps = new ArrayList<>();
    private final LinkedHashSet<String> aiEditedFilesThisRun = new LinkedHashSet<>();
    private boolean aiThinkingVisible;
    private int aiThinkingToken;
    private boolean terminalVisible;
    private boolean fileManagerVisible;
    private boolean choosingProjectFolder;
    private boolean settingsVisible;
    private String settingsPage = "root";
    private boolean extraCtrl;
    private boolean extraCtrlLocked;
    private boolean extraAlt;
    private boolean extraAltLocked;
    private boolean extraShift;
    private boolean extraShiftLocked;
    private LinearLayout extraKeysRow;
    private Runnable extraLongPressRunnable;
    private File fileManagerCurrentDir;
    private String fileManagerCurrentTitle;
    private LinearLayout fileManagerScreenBody;
    private final Set<String> expandedProjectDirs = new HashSet<>();
    private final Set<String> selectedFilePaths = new HashSet<>();
    private final ArrayList<File> pendingFileOperationSources = new ArrayList<>();
    private final HashMap<String, AiFileChange> aiFileChanges = new HashMap<>();
    private final HashMap<String, AiFileChange> aiRedoFileChanges = new HashMap<>();
    private String pendingFileOperation;
    private boolean bootstrapShowingOutput;
    private boolean bootstrapDownloading;
    private String terminalStartupCommand;
    private String terminalStartupScript;
    private boolean terminalReturnToEditorOnExit;
    private boolean pendingEditorSwitchAnimation;
    private int pendingEditorSwitchDirection = 1;
    private int panelAnimationToken;
    private int changedStart;
    private int changedBefore;
    private int changedCount;
    private int selectedRuntimeProfile = RUNTIME_BASIC;
    private int aiCompletionToken;
    private boolean aiCompletionInFlight;
    private String aiCompletionSignature = "";
    private String aiSuggestionText = "";
    private int aiSuggestionCursor = -1;
    private Runnable aiCompletionRunnable;
    private Runnable aiRetryCountdownRunnable;
    private long aiCompletionRetryAtMs;
    private int aiCompletionRetryToken;
    private Runnable modelFetchRunnable;
    private int modelFetchToken;
    private boolean modelFetchInFlight;
    private final ArrayList<AiChatMessage> aiChatHistory = new ArrayList<>();
    private final ArrayList<AiRecentChat> aiRecentChats = new ArrayList<>();
    private final StringBuilder aiToolFeed = new StringBuilder();
    private int aiStatusAnimationToken;
    private long bootstrapLastUiUpdate;
    private long bootstrapExtractedBytes;
    private volatile boolean opencamBridgeRunning;
    private volatile boolean opencamStreaming;
    private LocalServerSocket opencamServerSocket;
    private Thread opencamServerThread;
    private boolean opencamVisible;
    private int opencamFrameCounter;
    private TextureView opencamTextureView;
    private TextView opencamStatusText;
    private HandlerThread opencamCameraThread;
    private Handler opencamCameraHandler;
    private CameraDevice opencamCameraDevice;
    private CameraCaptureSession opencamCaptureSession;
    private ImageReader opencamImageReader;
    private String opencamCameraId;
    private volatile int opencamFrameWidth;
    private volatile int opencamFrameHeight;
    private volatile long opencamLastFrameAtMs;
    private final Object opencamFrameLock = new Object();
    private byte[] opencamLatestGrayFrame;
    private volatile boolean aquaDisplayBridgeRunning;
    private LocalServerSocket aquaDisplayServerSocket;
    private Thread aquaDisplayServerThread;
    private boolean aquaDisplayVisible;
    private ImageView aquaDisplayImage;
    private TextView aquaDisplayStatusText;
    private int aquaDisplayFrameCounter;
    private int aquaDisplayFrameWidth;
    private int aquaDisplayFrameHeight;
    private String aquaDisplayTitle = "Aqua display";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        selectedRuntimeProfile = prefs.getInt("runtime_profile", RUNTIME_BASIC);
        if (BuildConfig.ANDROPY_PREBUNDLED_RUNTIME) {
            selectedRuntimeProfile = RUNTIME_EXTENDED;
            prefs.edit().putInt("runtime_profile", selectedRuntimeProfile).apply();
        }
        initProjectRoots();
        startOpencamBridge();
        startAquaDisplayBridge();
        loadAiFileChanges();
        if (runtimeReady()) {
            ensureProjectRoots();
            showEditorAfterBootstrap();
        } else if (BuildConfig.ANDROPY_SKIP_RUNTIME_CHECK) {
            setContentView(buildBootstrapScreen());
            beginRuntimeInstall();
        } else {
            setContentView(buildBootstrapScreen());
            startBootstrap();
        }
    }

    private View buildBootstrapScreen() {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(BOOT_BG);

        ImageButton terminal = new ImageButton(this);
        terminal.setImageResource(getResources().getIdentifier("ic_terminal_24", "drawable", getPackageName()));
        terminal.setColorFilter(BOOT_TEXT);
        terminal.setBackgroundColor(Color.TRANSPARENT);
        terminal.setContentDescription("Bootstrap output");
        terminal.setPadding(dp(10), dp(10), dp(10), dp(10));
        terminal.setOnClickListener(v -> toggleBootstrapOutput());
        FrameLayout.LayoutParams terminalParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        terminalParams.gravity = Gravity.START | Gravity.TOP;
        terminalParams.setMargins(dp(8), dp(8), 0, 0);
        shell.addView(terminal, terminalParams);

        bootstrapVisualPanel = new LinearLayout(this);
        bootstrapVisualPanel.setOrientation(LinearLayout.VERTICAL);
        bootstrapVisualPanel.setGravity(Gravity.CENTER_HORIZONTAL);
        bootstrapVisualPanel.setPadding(dp(28), 0, dp(28), 0);

        bootstrapOrb = new BootstrapOrbView(this);
        LinearLayout.LayoutParams orbParams = new LinearLayout.LayoutParams(dp(172), dp(172));
        orbParams.setMargins(0, 0, 0, dp(34));
        bootstrapVisualPanel.addView(bootstrapOrb, orbParams);

        FrameLayout progressTrack = new FrameLayout(this);
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setColor(Color.rgb(82, 85, 94));
        trackBg.setCornerRadius(dp(3));
        progressTrack.setBackground(trackBg);
        bootstrapProgressFill = new View(this);
        GradientDrawable fillBg = new GradientDrawable();
        fillBg.setColor(Color.WHITE);
        fillBg.setCornerRadius(dp(3));
        bootstrapProgressFill.setBackground(fillBg);
        progressTrack.addView(bootstrapProgressFill, new FrameLayout.LayoutParams(dp(2), dp(5)));
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(dp(236), dp(5));
        progressParams.setMargins(0, 0, 0, dp(16));
        bootstrapVisualPanel.addView(progressTrack, progressParams);

        bootstrapStageText = new TextView(this);
        bootstrapStageText.setText("Preparing bootstrap");
        bootstrapStageText.setTextColor(BOOT_TEXT);
        bootstrapStageText.setTextSize(13);
        bootstrapStageText.setGravity(Gravity.CENTER);
        bootstrapStageText.setSingleLine(true);
        bootstrapVisualPanel.addView(bootstrapStageText, new LinearLayout.LayoutParams(-1, dp(28)));

        bootstrapRuntimeChoices = new LinearLayout(this);
        bootstrapRuntimeChoices.setOrientation(LinearLayout.VERTICAL);
        bootstrapRuntimeChoices.setVisibility(View.GONE);
        bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_BASIC, "Basic Python Runtime", "Coreutils, Python, pip. Small first install.", "16-17 MB", true));
        bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_EXTENDED, "Extended Python Runtime", "LLVM, compilation tools, headers, sysroot.", "~135-138 MB", true));
        bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_MAX, "Max Runtime", "Full Linux runtime pack.", "coming soon", false));
        LinearLayout.LayoutParams choicesParams = new LinearLayout.LayoutParams(-1, -2);
        choicesParams.setMargins(0, dp(18), 0, 0);
        bootstrapVisualPanel.addView(bootstrapRuntimeChoices, choicesParams);

        bootstrapDownloadButton = new Button(this);
        bootstrapDownloadButton.setText("Download runtimes");
        bootstrapDownloadButton.setAllCaps(false);
        bootstrapDownloadButton.setTextSize(13);
        bootstrapDownloadButton.setTextColor(Color.rgb(25, 28, 32));
        bootstrapDownloadButton.setBackground(rounded(Color.WHITE, Color.WHITE, 0, 8));
        bootstrapDownloadButton.setVisibility(View.GONE);
        bootstrapDownloadButton.setOnClickListener(v -> beginRuntimeInstall());
        LinearLayout.LayoutParams downloadParams = new LinearLayout.LayoutParams(-1, dp(42));
        downloadParams.setMargins(0, dp(10), 0, 0);
        bootstrapVisualPanel.addView(bootstrapDownloadButton, downloadParams);

        FrameLayout.LayoutParams visualParams = new FrameLayout.LayoutParams(-1, -2);
        visualParams.gravity = Gravity.CENTER;
        shell.addView(bootstrapVisualPanel, visualParams);

        bootstrapOutputScroll = new ScrollView(this);
        bootstrapOutputScroll.setFillViewport(true);
        bootstrapOutputScroll.setVisibility(View.GONE);
        bootstrapOutputScroll.setBackgroundColor(BOOT_PANEL);
        bootstrapOutputText = new TextView(this);
        bootstrapOutputText.setTextColor(Color.rgb(215, 225, 238));
        bootstrapOutputText.setTextSize(12);
        bootstrapOutputText.setTypeface(Typeface.MONOSPACE);
        bootstrapOutputText.setIncludeFontPadding(false);
        bootstrapOutputText.setPadding(dp(18), dp(72), dp(18), dp(18));
        bootstrapOutputScroll.addView(bootstrapOutputText, new ScrollView.LayoutParams(-1, -2));
        shell.addView(bootstrapOutputScroll, new FrameLayout.LayoutParams(-1, -1));
        if (bootstrapShowingOutput) {
            bootstrapVisualPanel.setVisibility(View.GONE);
            bootstrapOutputScroll.setVisibility(View.VISIBLE);
        }

        setBootstrapProgress(0.02f, "Testing connection");
        appendBootstrapOutput("$ bootstrap-start");
        return shell;
    }

    private void configureSoraEditor(CodeEditor codeEditor) {
        codeEditor.setTypefaceText(Typeface.MONOSPACE);
        codeEditor.setTypefaceLineNumber(Typeface.MONOSPACE);
        codeEditor.setEditable(true);
        codeEditor.setHighlightCurrentLine(true);
        codeEditor.setHighlightBracketPair(true);
        codeEditor.setLineNumberEnabled(true);
        codeEditor.setPinLineNumber(false);
        codeEditor.setFirstLineNumberAlwaysVisible(true);
        codeEditor.setScalable(false);
        codeEditor.setTabWidth(4);
        codeEditor.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        codeEditor.setPadding(dp(10), dp(6), dp(8), dp(10));
        codeEditor.setDividerWidth(Math.max(1f, getResources().getDisplayMetrics().density));
        codeEditor.setDividerMargin(dp(9), dp(12));
        codeEditor.setLineNumberMarginLeft(dp(12));
        codeEditor.setCursorWidth(Math.max(dp(2), getResources().getDisplayMetrics().density * 2f));

        EditorColorScheme scheme = new EditorColorScheme(codeEditor);
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, EDITOR_BG);
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, TEXT);
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, EDITOR_BG);
        scheme.setColor(EditorColorScheme.LINE_NUMBER, GUTTER_TEXT);
        scheme.setColor(EditorColorScheme.LINE_NUMBER_CURRENT, TEXT);
        scheme.setColor(EditorColorScheme.LINE_DIVIDER, GUTTER_LINE);
        scheme.setColor(EditorColorScheme.CURRENT_LINE, ACTIVE_LINE);
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, Color.rgb(139, 224, 218));
        scheme.setColor(EditorColorScheme.SELECTED_TEXT_BACKGROUND, Color.argb(96, 139, 224, 218));
        scheme.setColor(EditorColorScheme.TEXT_SELECTED, TEXT);
        scheme.setColor(EditorColorScheme.KEYWORD, KEYWORD);
        scheme.setColor(EditorColorScheme.COMMENT, COMMENT);
        scheme.setColor(EditorColorScheme.LITERAL, STRING);
        scheme.setColor(EditorColorScheme.FUNCTION_NAME, FUNCTION);
        scheme.setColor(EditorColorScheme.IDENTIFIER_NAME, TEXT);
        scheme.setColor(EditorColorScheme.IDENTIFIER_VAR, BUILTIN);
        scheme.setColor(EditorColorScheme.ANNOTATION, DECORATOR);
        scheme.setColor(EditorColorScheme.OPERATOR, IMPORT_NAME);
        codeEditor.setColorScheme(scheme);
    }

    private void onEditorContentChanged(ContentChangeEvent event) {
        String code = editorText();
        prefs.edit().putString("code", code).apply();
        saveOpenedFileQuietly(code);
        clearAiSuggestion();
        applySoraTypingHelp(event);
        applySoraSyntaxHighlighting();
        refreshCompletions();
        if (editor != null) editor.postDelayed(this::refreshCompletions, 60);
        scheduleAiCompletion();
        updatePanelStatus();
    }

    private Content editorContent() {
        return editor == null ? null : editor.getText();
    }

    private String editorText() {
        Content content = editorContent();
        return content == null ? "" : content.toString();
    }

    private String initialEditorText() {
        File file = currentEditorDiskFile();
        if (file != null && file.isFile()) {
            try {
                String text = new String(readFileBytes(file, 2 * 1024 * 1024), StandardCharsets.UTF_8);
                prefs.edit().putString("code", text).apply();
                return text;
            } catch (IOException ignored) {
            }
        }
        return prefs.getString("code", defaultPython());
    }

    private File currentEditorDiskFile() {
        String openedPath = prefs.getString("file_path", "");
        if (openedPath != null && !openedPath.trim().isEmpty()) return new File(openedPath);
        return new File(homeRoot, currentFileName());
    }

    private int editorLength() {
        Content content = editorContent();
        return content == null ? 0 : content.length();
    }

    private int editorSelectionStart() {
        if (editor == null) return 0;
        return Math.max(0, editor.getCursor().getLeft());
    }

    private int editorSelectionEnd() {
        if (editor == null) return 0;
        return Math.max(0, editor.getCursor().getRight());
    }

    private CharPosition editorPositionForOffset(int offset) {
        Content content = editorContent();
        int clamped = Math.max(0, Math.min(content == null ? 0 : content.length(), offset));
        return content == null ? new CharPosition(0, 0, 0) : content.getIndexer().getCharPosition(clamped);
    }

    private void setEditorSelection(int offset) {
        if (editor == null) return;
        CharPosition position = editorPositionForOffset(offset);
        editor.setSelection(position.line, position.column);
    }

    private void setEditorSelection(int start, int end) {
        if (editor == null) return;
        CharPosition left = editorPositionForOffset(start);
        CharPosition right = editorPositionForOffset(end);
        editor.setSelectionRegion(left.line, left.column, right.line, right.column);
    }

    private void insertEditorText(int offset, CharSequence text) {
        replaceEditorRange(offset, offset, text);
    }

    private void replaceEditorRange(int start, int end, CharSequence text) {
        Content content = editorContent();
        if (content == null) return;
        int leftOffset = Math.max(0, Math.min(start, content.length()));
        int rightOffset = Math.max(0, Math.min(end, content.length()));
        if (leftOffset > rightOffset) {
            int tmp = leftOffset;
            leftOffset = rightOffset;
            rightOffset = tmp;
        }
        CharPosition left = content.getIndexer().getCharPosition(leftOffset);
        CharPosition right = content.getIndexer().getCharPosition(rightOffset);
        content.replace(left.line, left.column, right.line, right.column, text == null ? "" : text);
    }

    private void applySoraTypingHelp(ContentChangeEvent event) {
        if (editor == null || applyingHelperEdit || event == null
                || event.getAction() != ContentChangeEvent.ACTION_INSERT) {
            return;
        }
        CharSequence changed = event.getChangedText();
        if (changed == null || changed.length() != 1) return;
        char inserted = changed.charAt(0);
        int insertedAt = Math.max(0, event.getChangeStart().index);
        String code = editorText();

        if (isCloser(inserted)
                && insertedAt + 1 < code.length()
                && code.charAt(insertedAt + 1) == inserted) {
            applyingHelperEdit = true;
            replaceEditorRange(insertedAt, insertedAt + 1, "");
            setEditorSelection(insertedAt + 1);
            applyingHelperEdit = false;
            return;
        }

        if (inserted == '\n') {
            applySoraNewlineIndent(code, insertedAt);
            return;
        }

        char close = matchingCloser(inserted);
        if (close == 0) return;
        if ((inserted == '"' || inserted == '\'') && isInsideStringLiteral(code, insertedAt)) return;

        applyingHelperEdit = true;
        insertEditorText(insertedAt + 1, String.valueOf(close));
        setEditorSelection(insertedAt + 1);
        applyingHelperEdit = false;
    }

    private void applySoraNewlineIndent(String code, int insertedAt) {
        if (insertedAt < 0 || insertedAt >= code.length()) return;
        String insert = nextPythonIndent(code, insertedAt);
        int cursorDelta = insert.length();
        if (insertedAt > 0 && insertedAt + 1 < code.length()
                && isPair(code.charAt(insertedAt - 1), code.charAt(insertedAt + 1))) {
            String baseIndent = lineIndentBefore(code, insertedAt);
            String innerIndent = baseIndent + "    ";
            insert = innerIndent + "\n" + baseIndent;
            cursorDelta = innerIndent.length();
        }
        if (insert.isEmpty()) return;
        applyingHelperEdit = true;
        insertEditorText(insertedAt + 1, insert);
        setEditorSelection(insertedAt + 1 + cursorDelta);
        applyingHelperEdit = false;
    }

    private boolean isInsideStringLiteral(String code, int offset) {
        int lineStart = Math.max(0, offset);
        while (lineStart > 0 && code.charAt(lineStart - 1) != '\n') lineStart--;
        char quote = 0;
        boolean escaped = false;
        for (int i = lineStart; i < offset; i++) {
            char c = code.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (quote == 0 && (c == '"' || c == '\'')) {
                quote = c;
            } else if (quote == c) {
                quote = 0;
            }
        }
        return quote != 0;
    }

    private void applySoraSyntaxHighlighting() {
        if (editor == null) return;
        Content content = editorContent();
        if (content == null) return;

        String code = content.toString();
        int lineCount = Math.max(1, content.getLineCount());
        long normal = TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL);
        ArrayList<SoraStyleMark> marks = new ArrayList<>();
        for (int line = 0; line < lineCount; line++) {
            marks.add(new SoraStyleMark(line, 0, normal, 0));
        }

        addSoraRegexSpans(marks, content, code, KEYWORD_PATTERN, EditorColorScheme.KEYWORD, 0);
        addSoraRegexSpans(marks, content, code, BUILTIN_PATTERN, EditorColorScheme.IDENTIFIER_VAR, 0);
        addSoraRegexSpans(marks, content, code, NUMBER_PATTERN, EditorColorScheme.LITERAL, 0);
        addSoraRegexSpans(marks, content, code, IMPORT_PATTERN, EditorColorScheme.OPERATOR, 1);
        addSoraRegexSpans(marks, content, code, FUNCTION_PATTERN, EditorColorScheme.FUNCTION_NAME, 1);
        addSoraRegexSpans(marks, content, code, CLASS_PATTERN, EditorColorScheme.FUNCTION_NAME, 1);
        addSoraRegexSpans(marks, content, code, STRING_PATTERN, EditorColorScheme.LITERAL, 0);
        addSoraRegexSpans(marks, content, code, COMMENT_PATTERN, EditorColorScheme.COMMENT, 0);
        addSoraRegexSpans(marks, content, code, DECORATOR_PATTERN, EditorColorScheme.ANNOTATION, 0);

        marks.sort((a, b) -> {
            if (a.line != b.line) return Integer.compare(a.line, b.line);
            if (a.column != b.column) return Integer.compare(a.column, b.column);
            return Integer.compare(a.priority, b.priority);
        });
        MappedSpans.Builder builder = new MappedSpans.Builder(lineCount);
        int lastLine = -1;
        int lastColumn = -1;
        long lastStyle = Long.MIN_VALUE;
        for (SoraStyleMark mark : marks) {
            if (mark.line < 0 || mark.line >= lineCount || mark.column < 0) continue;
            if (mark.line == lastLine && mark.column == lastColumn) {
                lastStyle = mark.style;
                continue;
            }
            if (lastLine >= 0) builder.addIfNeeded(lastLine, lastColumn, lastStyle);
            lastLine = mark.line;
            lastColumn = mark.column;
            lastStyle = mark.style;
        }
        if (lastLine >= 0) builder.addIfNeeded(lastLine, lastColumn, lastStyle);
        builder.determine(lineCount);
        builder.addNormalIfNull();
        Styles styles = new Styles(builder.build());
        applyAiDiffLineStyles(styles, lineCount);
        styles.finishBuilding();
        editor.setStyles(styles);
    }

    private void applyAiDiffLineStyles(Styles styles, int lineCount) {
        AiDiffMarkers markers = currentAiDiffMarkers();
        if (markers == null) return;
        for (Integer line : markers.addedLines) {
            int clamped = clampLine(line, lineCount);
            styles.addLineStyle(new LineBackground(clamped, new ConstColor(AI_DIFF_ADD_BG)));
            styles.addLineStyle(new LineSideIcon(clamped, new DiffMarkerDrawable("+", AI_DIFF_ADD_GUTTER)));
        }
        for (Integer line : markers.deletedAnchors) {
            int clamped = clampLine(line, lineCount);
            styles.addLineStyle(new LineBackground(clamped, new ConstColor(AI_DIFF_DEL_BG)));
            styles.addLineStyle(new LineSideIcon(clamped, new DiffMarkerDrawable("-", AI_DIFF_DEL_GUTTER)));
        }
    }

    private int clampLine(int line, int lineCount) {
        return Math.max(0, Math.min(Math.max(0, lineCount - 1), line));
    }

    private AiDiffMarkers currentAiDiffMarkers() {
        File file = currentEditorDiskFile();
        AiFileChange change = aiFileChangeFor(file);
        if (change == null) return null;
        if (change.markers == null || !change.after.equals(editorText())) {
            change.markers = buildAiDiffMarkers(change.before, editorText());
        }
        return change.markers;
    }

    private void addSoraRegexSpans(List<SoraStyleMark> marks, Content content, String code,
                                   Pattern pattern, int colorId, int group) {
        Matcher matcher = pattern.matcher(code);
        long style = TextStyle.makeStyle(colorId);
        long normal = TextStyle.makeStyle(EditorColorScheme.TEXT_NORMAL);
        while (matcher.find()) {
            int start = matcher.start(group);
            int end = matcher.end(group);
            if (start < 0 || end <= start) continue;
            addSoraSpan(marks, content, start, end, style, normal);
        }
    }

    private void addSoraSpan(List<SoraStyleMark> marks, Content content,
                             int start, int end, long style, long normal) {
        int length = content.length();
        int safeStart = Math.max(0, Math.min(start, length));
        int safeEnd = Math.max(safeStart, Math.min(end, length));
        if (safeEnd <= safeStart) return;

        CharPosition left = content.getIndexer().getCharPosition(safeStart);
        CharPosition right = content.getIndexer().getCharPosition(safeEnd);
        if (left.line == right.line) {
            marks.add(new SoraStyleMark(left.line, left.column, style, 3));
            marks.add(new SoraStyleMark(right.line, right.column, normal, 2));
            return;
        }

        marks.add(new SoraStyleMark(left.line, left.column, style, 3));
        for (int line = left.line + 1; line <= right.line; line++) {
            marks.add(new SoraStyleMark(line, 0, style, 3));
        }
        marks.add(new SoraStyleMark(right.line, right.column, normal, 2));
    }

    private static final class SoraStyleMark {
        final int line;
        final int column;
        final long style;
        final int priority;

        SoraStyleMark(int line, int column, long style, int priority) {
            this.line = line;
            this.column = column;
            this.style = style;
            this.priority = priority;
        }
    }

    private View runtimeChoiceRow(int profile, String title, String detail, String size, boolean enabled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setAlpha(enabled ? 1f : 0.42f);
        row.setBackground(rounded(Color.rgb(62, 65, 72), Color.rgb(94, 98, 108), 1, 7));

        CheckBox check = new CheckBox(this);
        check.setChecked(profile == selectedRuntimeProfile);
        check.setEnabled(enabled);
        if (Build.VERSION.SDK_INT >= 21) {
            check.setButtonTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        row.addView(check, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView name = new TextView(this);
        name.setText(title);
        name.setTextColor(Color.WHITE);
        name.setTextSize(14);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        copy.addView(name, new LinearLayout.LayoutParams(-1, dp(22)));

        TextView desc = new TextView(this);
        desc.setText(detail);
        desc.setTextColor(Color.rgb(218, 221, 226));
        desc.setTextSize(11);
        desc.setSingleLine(true);
        copy.addView(desc, new LinearLayout.LayoutParams(-1, dp(20)));
        row.addView(copy, new LinearLayout.LayoutParams(0, dp(44), 1));

        TextView sizeText = new TextView(this);
        sizeText.setText(size);
        sizeText.setTextColor(Color.WHITE);
        sizeText.setTextSize(11);
        sizeText.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        row.addView(sizeText, new LinearLayout.LayoutParams(dp(78), dp(44)));

        if (enabled) {
            View.OnClickListener listener = v -> {
                selectedRuntimeProfile = profile;
                prefs.edit().putInt("runtime_profile", selectedRuntimeProfile).apply();
                showRuntimeSelector("Connection OK");
            };
            row.setOnClickListener(listener);
            check.setOnClickListener(listener);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(62));
        params.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private void toggleBootstrapOutput() {
        bootstrapShowingOutput = !bootstrapShowingOutput;
        if (bootstrapVisualPanel != null) bootstrapVisualPanel.setVisibility(bootstrapShowingOutput ? View.GONE : View.VISIBLE);
        if (bootstrapOutputScroll != null) bootstrapOutputScroll.setVisibility(bootstrapShowingOutput ? View.VISIBLE : View.GONE);
    }

    private void showBootstrapVisual() {
        bootstrapShowingOutput = false;
        if (bootstrapVisualPanel != null) bootstrapVisualPanel.setVisibility(View.VISIBLE);
        if (bootstrapOutputScroll != null) bootstrapOutputScroll.setVisibility(View.GONE);
    }

    private void startBootstrap() {
        Thread bootstrapThread = new Thread(() -> {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try {
                appendBootstrapOutput("$ nice bootstrap-worker priority=background");
                setBootstrapProgress(0.08f, "Testing connection");
                appendBootstrapOutput("$ test-connection " + runtimeDownloadUrl());
                testRuntimeConnection();
                showRuntimeSelector("Connection OK");
            } catch (Exception e) {
                appendBootstrapOutput("! " + e.getClass().getSimpleName() + ": " + e.getMessage());
                showRuntimeSelector("Connection failed");
            }
        }, "AndroPy-bootstrap");
        bootstrapThread.start();
    }

    private void testRuntimeConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(runtimeDownloadUrl()).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("HEAD");
        int code = connection.getResponseCode();
        connection.disconnect();
        if (code < 200 || code >= 400) throw new IOException("server check HTTP " + code);
    }

    private void showRuntimeSelector(String status) {
        runOnUiThread(() -> {
            bootstrapDownloading = false;
            if (bootstrapRuntimeChoices != null) {
                bootstrapRuntimeChoices.removeAllViews();
                bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_BASIC, "Basic Python Runtime", "Coreutils, Python, pip. Small first install.", "16-17 MB", true));
                bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_EXTENDED, "Extended Python Runtime", "LLVM, compilation tools, headers, sysroot.", "~135-138 MB", true));
                bootstrapRuntimeChoices.addView(runtimeChoiceRow(RUNTIME_MAX, "Max Runtime", "Full Linux runtime pack.", "coming soon", false));
                bootstrapRuntimeChoices.setVisibility(View.VISIBLE);
            }
            if (bootstrapDownloadButton != null) bootstrapDownloadButton.setVisibility(View.VISIBLE);
            setBootstrapProgress(0.18f, status);
            if (bootstrapOrb != null) bootstrapOrb.invalidate();
        });
    }

    private void beginRuntimeInstall() {
        bootstrapDownloading = true;
        if (bootstrapRuntimeChoices != null) bootstrapRuntimeChoices.setVisibility(View.GONE);
        if (bootstrapDownloadButton != null) bootstrapDownloadButton.setVisibility(View.GONE);
        if (bootstrapOrb != null) bootstrapOrb.invalidate();
        Thread bootstrapThread = new Thread(() -> {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try {
                appendBootstrapOutput("$ nice runtime-worker priority=background");
                ensureProjectRoots();
                setBootstrapProgress(1f, "Runtime init complete");
                appendBootstrapOutput("$ bootstrap-complete");
                SystemClock.sleep(320);
                runOnUiThread(this::showEditorAfterBootstrap);
            } catch (Exception e) {
                appendBootstrapOutput("! " + e.getClass().getSimpleName() + ": " + e.getMessage());
                setBootstrapProgress(1f, "Bootstrap failed");
            }
        }, "AndroPy-runtime-install");
        bootstrapThread.start();
    }

    private void showEditorAfterBootstrap() {
        terminalVisible = false;
        setContentView(buildEditorScreen());
    }

    private void setBootstrapProgress(float progress, String stage) {
        runOnUiThread(() -> {
            if (bootstrapStageText != null) bootstrapStageText.setText(stage);
            if (bootstrapProgressFill != null) {
                int width = Math.max(dp(2), Math.round(dp(236) * Math.max(0f, Math.min(1f, progress))));
                ViewGroup.LayoutParams params = bootstrapProgressFill.getLayoutParams();
                params.width = width;
                bootstrapProgressFill.setLayoutParams(params);
            }
        });
    }

    private void appendBootstrapOutput(String line) {
        runOnUiThread(() -> {
            bootstrapOutput.append(line).append('\n');
            if (bootstrapOutputText != null) {
                bootstrapOutputText.setText(bootstrapOutput.toString());
                if (bootstrapOutputScroll != null) {
                    bootstrapOutputScroll.post(() -> bootstrapOutputScroll.fullScroll(View.FOCUS_DOWN));
                }
            }
        });
    }

    private View buildEditorScreen() {
        FrameLayout shell = new FrameLayout(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(14), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton menu = new ImageButton(this);
        menu.setImageResource(getResources().getIdentifier("ic_menu_24", "drawable", getPackageName()));
        menu.setColorFilter(MUTED);
        menu.setBackgroundColor(BAR);
        menu.setContentDescription("Menu");
        menu.setPadding(dp(7), dp(7), dp(7), dp(7));
        menu.setOnClickListener(v -> togglePanel());
        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        menuParams.setMarginEnd(dp(6));
        topBar.addView(menu, menuParams);

        TextView fileName = new TextView(this);
        fileName.setText(currentFileName());
        fileName.setTextColor(MUTED);
        fileName.setTextSize(15);
        fileName.setTypeface(Typeface.DEFAULT);
        fileName.setSingleLine(true);
        fileName.setGravity(Gravity.CENTER_VERTICAL);
        topBar.addView(fileName, new LinearLayout.LayoutParams(0, dp(48), 1));

        ImageButton files = new ImageButton(this);
        files.setImageResource(getResources().getIdentifier("ic_folder_open_24", "drawable", getPackageName()));
        files.setColorFilter(MUTED);
        files.setBackgroundColor(BAR);
        files.setContentDescription("Files");
        files.setPadding(dp(7), dp(7), dp(7), dp(7));
        files.setOnClickListener(v -> {
            choosingProjectFolder = false;
            showFileManagerRoot();
        });
        LinearLayout.LayoutParams filesParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        filesParams.setMarginEnd(dp(8));
        topBar.addView(files, filesParams);

        ImageButton run = new ImageButton(this);
        run.setImageResource(getResources().getIdentifier("ic_play_arrow_24", "drawable", getPackageName()));
        run.setColorFilter(ACCENT);
        run.setBackgroundColor(BAR);
        run.setContentDescription("Run");
        run.setPadding(dp(7), dp(7), dp(7), dp(7));
        run.setOnClickListener(v -> runCurrentFile());
        LinearLayout.LayoutParams runParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        topBar.addView(run, runParams);
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

        root.addView(buildOpenFilesStrip(), new LinearLayout.LayoutParams(-1, dp(36)));
        View openFileBlueDivider = new View(this);
        openFileBlueDivider.setBackgroundColor(OPEN_FILE_BLUE_LINE);
        root.addView(openFileBlueDivider, new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));

        editor = new CodeEditor(this);
        editor.setText(initialEditorText());
        configureSoraEditor(editor);
        editor.setTextSize(15);
        boolean fixedWrap = editorFixedWrap();
        editor.setHorizontalScrollBarEnabled(!fixedWrap);
        editor.setWordwrap(fixedWrap);
        editor.setLineSpacing(0, 1.0f);
        applySoraSyntaxHighlighting();
        editor.subscribeEvent(ContentChangeEvent.class, (event, unsubscribe) -> onEditorContentChanged(event));
        editor.subscribeEvent(SelectionChangeEvent.class, (event, unsubscribe) -> updatePanelStatus());
        editor.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (hasAiSuggestion()) {
                int digit = digitForKeyCode(keyCode);
                if (digit >= 2) {
                    acceptAiSuggestion(Math.min(AI_COMPLETION_MAX_LINES, digit + 1));
                    return true;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_TAB) {
                acceptTabInEditor();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                dismissCompletions();
                clearAiSuggestion();
                return true;
            }
            return false;
        });
        root.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));
        animatePendingEditorSwitch(editor);
        root.addView(buildExtraKeysBar(false), new LinearLayout.LayoutParams(-1, dp(74)));
        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));

        projectScrim = new View(this);
        projectScrim.setBackgroundColor(Color.argb(84, 0, 0, 0));
        projectScrim.setAlpha(0f);
        projectScrim.setVisibility(View.GONE);
        projectScrim.setOnClickListener(v -> closeProjectPanel());
        shell.addView(projectScrim, new FrameLayout.LayoutParams(-1, -1));

        projectPanel = buildProjectPanel();
        projectPanel.setTranslationX(-projectPanelWidth());
        projectPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams projectParams = new FrameLayout.LayoutParams(projectPanelWidth(), -1);
        projectParams.gravity = Gravity.START;
        shell.addView(projectPanel, projectParams);

        projectHandle = buildProjectHandle();
        FrameLayout.LayoutParams handleParams = new FrameLayout.LayoutParams(dp(30), dp(62));
        handleParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        shell.addView(projectHandle, handleParams);

        aiChatScrim = new View(this);
        aiChatScrim.setBackgroundColor(Color.argb(92, 0, 0, 0));
        aiChatScrim.setAlpha(0f);
        aiChatScrim.setVisibility(View.GONE);
        aiChatScrim.setOnClickListener(v -> closeAiChat());
        shell.addView(aiChatScrim, new FrameLayout.LayoutParams(-1, -1));

        aiChatPanel = buildAiChatPanel();
        aiChatPanel.setTranslationX(aiChatPanelWidth());
        aiChatPanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams aiParams = new FrameLayout.LayoutParams(aiChatPanelWidth(), -1);
        aiParams.gravity = Gravity.END;
        shell.addView(aiChatPanel, aiParams);

        aiChatHandle = buildAiChatHandle();
        FrameLayout.LayoutParams aiHandleParams = new FrameLayout.LayoutParams(dp(42), dp(74));
        aiHandleParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        shell.addView(aiChatHandle, aiHandleParams);

        scrim = new View(this);
        scrim.setBackgroundColor(SCRIM);
        scrim.setAlpha(0f);
        scrim.setVisibility(View.GONE);
        scrim.setOnClickListener(v -> closePanel());
        shell.addView(scrim, new FrameLayout.LayoutParams(-1, -1));

        sidePanel = buildSidePanel();
        sidePanel.setTranslationX(-panelWidth());
        sidePanel.setVisibility(View.GONE);
        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(panelWidth(), -1);
        panelParams.gravity = Gravity.START;
        shell.addView(sidePanel, panelParams);

        return shell;
    }

    private View buildOpenFilesStrip() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        scroll.setBackgroundColor(OPEN_FILE_STRIP);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), 0, dp(6), 0);

        ArrayList<OpenFileTab> tabs = openedFileTabs();
        for (OpenFileTab tab : tabs) row.addView(openFileTabView(tab));
        row.addView(addOpenFileButton());

        if (tabs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("new.py");
            empty.setTextColor(TEXT);
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER_VERTICAL);
            empty.setPadding(dp(12), 0, dp(12), 0);
            row.addView(empty, new LinearLayout.LayoutParams(-2, dp(34)));
        }

        scroll.addView(row, new HorizontalScrollView.LayoutParams(-2, -1));
        return scroll;
    }

    private View openFileTabView(OpenFileTab tab) {
        FrameLayout shell = new FrameLayout(this);
        boolean active = tab.matches(prefs.getString("file_path", ""), currentFileName());
        shell.setBackground(rounded(active ? OPEN_FILE_ACTIVE : OPEN_FILE_STRIP, Color.rgb(68, 77, 92), active ? 1 : 0, 4));
        shell.setOnClickListener(v -> openTab(tab));
        shell.setOnLongClickListener(v -> {
            showOpenFileTabMenu(v, tab);
            return true;
        });

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(dp(12), 0, dp(6), 0);

        TextView view = new TextView(this);
        view.setText(tab.name);
        view.setTextColor(active ? TEXT : MUTED);
        view.setTextSize(13);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setSingleLine(true);
        content.addView(view, new LinearLayout.LayoutParams(-2, -1));

        TextView close = new TextView(this);
        close.setText("×");
        close.setTextColor(active ? Color.rgb(232, 236, 244) : Color.rgb(166, 174, 188));
        close.setTextSize(15);
        close.setTypeface(Typeface.DEFAULT_BOLD);
        close.setGravity(Gravity.CENTER);
        close.setIncludeFontPadding(false);
        close.setContentDescription("Remove " + tab.name + " from opened files");
        close.setOnClickListener(v -> removeOpenFileTab(tab));
        content.addView(close, new LinearLayout.LayoutParams(dp(24), -1));

        shell.addView(content, new FrameLayout.LayoutParams(-2, -1));
        if (active) {
            View yellow = new View(this);
            yellow.setBackgroundColor(YELLOW_DIVIDER);
            FrameLayout.LayoutParams yellowParams = new FrameLayout.LayoutParams(-1, Math.max(2, dp(2)));
            yellowParams.gravity = Gravity.BOTTOM;
            shell.addView(yellow, yellowParams);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(28));
        params.setMargins(dp(2), dp(4), dp(2), dp(4));
        shell.setLayoutParams(params);
        return shell;
    }

    private View addOpenFileButton() {
        TextView button = new TextView(this);
        button.setText("+");
        button.setTextColor(YELLOW_DIVIDER);
        button.setTextSize(18);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rounded(Color.rgb(37, 43, 52), Color.rgb(72, 82, 98), 1, 4));
        button.setOnClickListener(v -> showCreateEditorFileDialog());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(28));
        params.setMargins(dp(4), dp(4), dp(2), dp(4));
        button.setLayoutParams(params);
        return button;
    }

    private void openTab(OpenFileTab tab) {
        int currentIndex = currentOpenFileTabIndex();
        int targetIndex = openFileTabIndex(tab);
        pendingEditorSwitchAnimation = true;
        pendingEditorSwitchDirection = targetIndex >= 0 && currentIndex >= 0 && targetIndex < currentIndex ? -1 : 1;
        saveEditorState();
        if (tab.path.isEmpty()) {
            prefs.edit().putString("file_name", tab.name).remove("file_path").apply();
            showEditor();
        } else {
            openFileInEditor(new File(tab.path), false);
        }
    }

    private int currentOpenFileTabIndex() {
        String currentPath = prefs.getString("file_path", "");
        String currentName = currentFileName();
        ArrayList<OpenFileTab> tabs = openedFileTabs();
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).matches(currentPath, currentName)) return i;
        }
        return -1;
    }

    private int openFileTabIndex(OpenFileTab target) {
        if (target == null) return -1;
        ArrayList<OpenFileTab> tabs = openedFileTabs();
        for (int i = 0; i < tabs.size(); i++) {
            OpenFileTab tab = tabs.get(i);
            if (tab.path.equals(target.path) && tab.name.equals(target.name)) return i;
        }
        return -1;
    }

    private void showOpenFileTabMenu(View anchor, OpenFileTab tab) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("Remove from strip");
        if (!tab.path.isEmpty()) menu.getMenu().add("Delete file");
        menu.setOnMenuItemClickListener(item -> {
            String title = String.valueOf(item.getTitle());
            if ("Remove from strip".equals(title)) {
                removeOpenFileTab(tab);
                return true;
            }
            if ("Delete file".equals(title)) {
                confirmDeleteOpenFile(tab);
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void removeOpenFileTab(OpenFileTab tab) {
        prefs.edit().putString("opened_files", openedFilesWithout(tab.path)).commit();
        if (tab.matches(prefs.getString("file_path", ""), currentFileName())) {
            switchToFallbackOpenFile(tab.path);
        } else {
            showEditor();
        }
    }

    private void confirmDeleteOpenFile(OpenFileTab tab) {
        if (tab.path.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete file")
                .setMessage(tab.name)
                .setPositiveButton("Delete", (dialog, which) -> deleteOpenFile(tab))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOpenFile(OpenFileTab tab) {
        File file = new File(tab.path);
        boolean deleted = deletePath(file);
        prefs.edit().putString("opened_files", openedFilesWithout(tab.path)).commit();
        Toast.makeText(this, deleted ? "Deleted" : "Delete failed", Toast.LENGTH_SHORT).show();
        if (tab.matches(prefs.getString("file_path", ""), currentFileName())) {
            switchToFallbackOpenFile(tab.path);
        } else {
            showEditor();
        }
    }

    private void switchToFallbackOpenFile(String removedPath) {
        ArrayList<OpenFileTab> tabs = openedFileTabs();
        for (OpenFileTab candidate : tabs) {
            if (!candidate.path.isEmpty() && !candidate.path.equals(removedPath) && new File(candidate.path).isFile()) {
                openFileInEditor(new File(candidate.path));
                return;
            }
        }
        prefs.edit()
                .putString("code", "")
                .putString("file_name", DEFAULT_FILE)
                .remove("file_path")
                .apply();
        showEditor();
    }

    private String openedFilesWithout(String path) {
        if (path == null || path.isEmpty()) return prefs.getString("opened_files", "");
        ArrayList<String> paths = new ArrayList<>();
        paths.add(path);
        return openedFilesWithout(paths);
    }

    private String openedFilesWithout(List<String> paths) {
        if (paths == null || paths.isEmpty()) return prefs.getString("opened_files", "");
        HashSet<String> remove = new HashSet<>();
        for (String path : paths) {
            if (path != null && !path.isEmpty()) remove.add(path);
        }
        if (remove.isEmpty()) return prefs.getString("opened_files", "");
        String raw = prefs.getString("opened_files", "");
        StringBuilder out = new StringBuilder();
        if (raw != null && !raw.isEmpty()) {
            for (String row : raw.split("\n")) {
                int sep = row.indexOf('\t');
                if (sep < 0) continue;
                if (remove.contains(row.substring(0, sep))) continue;
                if (out.length() > 0) out.append('\n');
                out.append(row);
            }
        }
        return out.toString();
    }

    private void showCreateEditorFileDialog() {
        File root = projectRoot();
        File destination = root != null && root.isDirectory() ? root : homeRoot;
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("file name");
        input.setTextColor(Color.BLACK);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setPadding(dp(18), dp(10), dp(18), dp(10));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New file")
                .setMessage(destination.getAbsolutePath())
                .setView(input)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(d -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.contains("/") || name.contains("\\")) {
                    Toast.makeText(this, "Invalid name", Toast.LENGTH_SHORT).show();
                    return;
                }
                File target = new File(destination, name);
                if (target.exists()) {
                    Toast.makeText(this, "Already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    File parent = target.getParentFile();
                    if (parent != null) parent.mkdirs();
                    if (!target.createNewFile()) throw new IOException("create failed");
                    dialog.dismiss();
                    openFileInEditor(target);
                } catch (IOException e) {
                    Toast.makeText(this, "Create failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private TextView buildProjectHandle() {
        TextView handle = new TextView(this);
        handle.setText("");
        handle.setTextColor(Color.rgb(28, 32, 38));
        handle.setTextSize(25);
        handle.setTypeface(Typeface.DEFAULT_BOLD);
        handle.setGravity(Gravity.CENTER);
        Drawable fileIcon = getResources().getDrawable(getResources().getIdentifier("ic_file_24", "drawable", getPackageName()));
        if (fileIcon != null) {
            fileIcon.setTint(Color.rgb(28, 32, 38));
            fileIcon.setBounds(0, 0, dp(24), dp(24));
            handle.setCompoundDrawables(fileIcon, null, null, null);
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(YELLOW_DIVIDER);
        bg.setCornerRadii(new float[]{0, 0, dp(18), dp(18), dp(18), dp(18), 0, 0});
        handle.setBackground(bg);
        handle.setAlpha(0.92f);
        handle.setContentDescription("Open explorer");
        handle.setOnClickListener(v -> openProjectPanel());
        handle.setOnTouchListener(new View.OnTouchListener() {
            float downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getRawX();
                    v.setAlpha(1f);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.setAlpha(0.92f);
                    if (event.getRawX() - downX > dp(12)) openProjectPanel();
                    else v.performClick();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.setAlpha(0.92f);
                    return true;
                }
                return true;
            }
        });
        return handle;
    }

    private TextView buildAiChatHandle() {
        TextView handle = new AiSideHandleView(this);
        handle.setText("");
        handle.setTextColor(Color.rgb(20, 24, 30));
        handle.setTextSize(13);
        handle.setTypeface(Typeface.DEFAULT_BOLD);
        handle.setGravity(Gravity.CENTER);
        handle.setAlpha(0.94f);
        handle.setElevation(dp(8));
        handle.setContentDescription("Open AI chat");
        handle.setOnClickListener(v -> openAiChat());
        handle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().cancel();
                v.animate().alpha(1f).scaleX(0.96f).scaleY(0.96f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().cancel();
                v.animate().alpha(0.94f).scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
        return handle;
    }

    private LinearLayout buildAiChatPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Color.rgb(34, 36, 42));
        panel.setElevation(dp(14));

        aiChatStatusText = new TextView(this);
        panel.addView(buildAiChatTopBar(), new LinearLayout.LayoutParams(-1, dp(46)));

        aiChatScroll = new ScrollView(this);
        aiChatScroll.setFillViewport(true);
        aiChatScroll.setBackgroundColor(Color.TRANSPARENT);
        aiChatMessages = new LinearLayout(this);
        aiChatMessages.setOrientation(LinearLayout.VERTICAL);
        aiChatMessages.setPadding(dp(14), dp(14), dp(14), dp(14));
        aiChatScroll.addView(aiChatMessages, new ScrollView.LayoutParams(-1, -2));
        panel.addView(aiChatScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.VERTICAL);
        composer.setPadding(dp(14), dp(6), dp(14), dp(14));
        composer.setBackgroundColor(Color.TRANSPARENT);

        FrameLayout inputBox = new FrameLayout(this);
        aiChatInputBox = inputBox;
        inputBox.setBackground(rounded(Color.rgb(47, 50, 58), Color.TRANSPARENT, 0, 18));

        aiEditBanner = new LinearLayout(this);
        aiEditBanner.setOrientation(LinearLayout.HORIZONTAL);
        aiEditBanner.setGravity(Gravity.CENTER_VERTICAL);
        aiEditBanner.setPadding(dp(8), dp(6), dp(10), 0);
        aiEditBanner.setVisibility(View.GONE);
        TextView cancelEdit = new TextView(this);
        cancelEdit.setText("x");
        cancelEdit.setTextColor(Color.rgb(205, 211, 224));
        cancelEdit.setTextSize(16);
        cancelEdit.setGravity(Gravity.CENTER);
        cancelEdit.setBackgroundColor(Color.TRANSPARENT);
        cancelEdit.setOnClickListener(v -> cancelAiMessageEdit());
        aiEditBanner.addView(cancelEdit, new LinearLayout.LayoutParams(dp(24), dp(22)));
        aiEditBannerText = new TextView(this);
        aiEditBannerText.setText("✎ Editing message");
        aiEditBannerText.setTextColor(Color.rgb(174, 181, 196));
        aiEditBannerText.setTextSize(11);
        aiEditBannerText.setSingleLine(true);
        aiEditBanner.addView(aiEditBannerText, new LinearLayout.LayoutParams(0, dp(22), 1));
        inputBox.addView(aiEditBanner, new FrameLayout.LayoutParams(-1, dp(32), Gravity.TOP));

        aiChatInput = new EditText(this);
        aiChatInput.setMinLines(1);
        aiChatInput.setMaxLines(5);
        aiChatInput.setTextColor(TEXT);
        aiChatInput.setHintTextColor(Color.rgb(142, 148, 160));
        aiChatInput.setHint("Ask questions to model");
        aiChatInput.setTextSize(14);
        aiChatInput.setSingleLine(false);
        aiChatInput.setGravity(Gravity.TOP | Gravity.START);
        aiChatInput.setVerticalScrollBarEnabled(false);
        aiChatInput.setOverScrollMode(View.OVER_SCROLL_NEVER);
        aiChatInput.setBackgroundColor(Color.TRANSPARENT);
        aiChatInput.setPadding(dp(18), dp(13), dp(54), dp(34));
        inputBox.addView(aiChatInput, new FrameLayout.LayoutParams(-1, -1));

        aiSendButton = new AiSendButton(this);
        aiSendButton.setContentDescription("Send AI message");
        aiSendButton.setOnClickListener(v -> sendAiChatPrompt());
        FrameLayout.LayoutParams sendParams = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.RIGHT | Gravity.BOTTOM);
        sendParams.setMargins(0, 0, dp(12), dp(10));
        inputBox.addView(aiSendButton, sendParams);
        aiEditBanner.bringToFront();
        LinearLayout.LayoutParams inputBoxParams = new LinearLayout.LayoutParams(-1, dp(AI_INPUT_BASE_HEIGHT_DP));
        composer.addView(inputBox, inputBoxParams);

        LinearLayout modelRow = new LinearLayout(this);
        modelRow.setOrientation(LinearLayout.HORIZONTAL);
        modelRow.setGravity(Gravity.CENTER_VERTICAL);
        modelRow.setPadding(dp(16), dp(5), dp(16), 0);

        LinearLayout modelPicker = new LinearLayout(this);
        modelPicker.setOrientation(LinearLayout.HORIZONTAL);
        modelPicker.setGravity(Gravity.CENTER_VERTICAL);
        modelPicker.setOnClickListener(v -> showAiModelMiniWindow(modelPicker));
        aiModelNameText = new TextView(this);
        aiModelNameText.setTextColor(Color.rgb(176, 181, 194));
        aiModelNameText.setTextSize(11);
        aiModelNameText.setSingleLine(true);
        modelPicker.addView(aiModelNameText, new LinearLayout.LayoutParams(0, dp(18), 1));
        modelPicker.addView(aiUpChevron(), new LinearLayout.LayoutParams(dp(24), dp(18)));
        modelRow.addView(modelPicker, new LinearLayout.LayoutParams(0, dp(18), 1));

        LinearLayout providerPicker = new LinearLayout(this);
        providerPicker.setOrientation(LinearLayout.HORIZONTAL);
        providerPicker.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        providerPicker.setOnClickListener(v -> showAiProviderMiniWindow(providerPicker));
        aiProviderNameText = new TextView(this);
        aiProviderNameText.setTextColor(Color.rgb(176, 181, 194));
        aiProviderNameText.setTextSize(11);
        aiProviderNameText.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        aiProviderNameText.setSingleLine(true);
        providerPicker.addView(aiProviderNameText, new LinearLayout.LayoutParams(0, dp(18), 1));
        providerPicker.addView(aiUpChevron(), new LinearLayout.LayoutParams(dp(24), dp(18)));
        modelRow.addView(providerPicker, new LinearLayout.LayoutParams(0, dp(18), 1));

        composer.addView(modelRow, new LinearLayout.LayoutParams(-1, dp(24)));
        LinearLayout.LayoutParams composerParams = new LinearLayout.LayoutParams(-1,
                dp(AI_INPUT_BASE_HEIGHT_DP + AI_COMPOSER_EXTRA_HEIGHT_DP));
        panel.addView(composer, composerParams);

        aiChatInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                aiChatInput.post(() -> {
                    int visualLines = Math.max(1, Math.min(5, aiChatInput.getLineCount()));
                    int boxHeight = dp(AI_INPUT_BASE_HEIGHT_DP) + Math.max(0, visualLines - 2) * dp(21);
                    int composerHeight = boxHeight + dp(AI_COMPOSER_EXTRA_HEIGHT_DP);
                    if (inputBoxParams.height != boxHeight) {
                        inputBoxParams.height = boxHeight;
                        inputBox.setLayoutParams(inputBoxParams);
                        composerParams.height = composerHeight;
                        composer.setLayoutParams(composerParams);
                    }
                });
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        refreshAiComposerControls();
        refreshAiEditModeUi();
        ensureAiChatSession();
        renderAiChatMessages();
        return panel;
    }

    private View buildAiChatTopBar() {
        FrameLayout top = new FrameLayout(this);
        top.setPadding(dp(8), dp(4), dp(8), 0);
        top.setBackgroundColor(Color.TRANSPARENT);

        ImageButton recent = new ImageButton(this);
        recent.setImageResource(getResources().getIdentifier("ic_history_24", "drawable", getPackageName()));
        recent.setColorFilter(Color.rgb(218, 224, 238));
        recent.setPadding(dp(10), dp(8), dp(10), dp(8));
        recent.setBackgroundColor(Color.TRANSPARENT);
        recent.setContentDescription("Recent chats");
        recent.setOnClickListener(v -> toggleRecentChatsView());
        FrameLayout.LayoutParams recentParams = new FrameLayout.LayoutParams(dp(52), dp(42), Gravity.LEFT | Gravity.TOP);
        recentParams.setMargins(dp(2), 0, 0, 0);
        top.addView(recent, recentParams);

        ImageButton newChat = new ImageButton(this);
        newChat.setImageResource(getResources().getIdentifier("ic_new_chat_24", "drawable", getPackageName()));
        newChat.setColorFilter(Color.rgb(218, 224, 238));
        newChat.setPadding(dp(10), dp(9), dp(10), dp(9));
        newChat.setBackgroundColor(Color.TRANSPARENT);
        newChat.setContentDescription("New chat");
        newChat.setOnClickListener(v -> startNewAiChat());
        FrameLayout.LayoutParams newChatParams = new FrameLayout.LayoutParams(dp(48), dp(42), Gravity.RIGHT | Gravity.TOP);
        top.addView(newChat, newChatParams);

        aiRedoChangesButton = new TextView(this);
        aiRedoChangesButton.setText("↷");
        aiRedoChangesButton.setTextSize(23);
        aiRedoChangesButton.setGravity(Gravity.CENTER);
        aiRedoChangesButton.setIncludeFontPadding(false);
        aiRedoChangesButton.setTypeface(Typeface.DEFAULT_BOLD);
        aiRedoChangesButton.setBackgroundColor(Color.TRANSPARENT);
        aiRedoChangesButton.setContentDescription("Redo AI changes");
        aiRedoChangesButton.setOnClickListener(v -> redoAiFileChanges());
        FrameLayout.LayoutParams redoParams = new FrameLayout.LayoutParams(dp(44), dp(42), Gravity.RIGHT | Gravity.TOP);
        redoParams.setMargins(0, 0, dp(48), 0);
        top.addView(aiRedoChangesButton, redoParams);

        aiRevertChangesButton = new TextView(this);
        aiRevertChangesButton.setText("↶");
        aiRevertChangesButton.setTextSize(23);
        aiRevertChangesButton.setGravity(Gravity.CENTER);
        aiRevertChangesButton.setIncludeFontPadding(false);
        aiRevertChangesButton.setTypeface(Typeface.DEFAULT_BOLD);
        aiRevertChangesButton.setBackgroundColor(Color.TRANSPARENT);
        aiRevertChangesButton.setContentDescription("Revert AI changes");
        aiRevertChangesButton.setOnClickListener(v -> revertAiFileChanges());
        FrameLayout.LayoutParams revertParams = new FrameLayout.LayoutParams(dp(44), dp(42), Gravity.RIGHT | Gravity.TOP);
        revertParams.setMargins(0, 0, dp(92), 0);
        top.addView(aiRevertChangesButton, revertParams);
        updateAiChangeButtons();

        aiChatTitleText = new TextView(this);
        aiChatTitleText.setTextColor(Color.rgb(231, 235, 244));
        aiChatTitleText.setTextSize(15);
        aiChatTitleText.setTypeface(Typeface.DEFAULT_BOLD);
        aiChatTitleText.setGravity(Gravity.CENTER);
        aiChatTitleText.setSingleLine(true);
        aiChatTitleText.setText("New chat");
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(-1, dp(32), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        titleParams.setMargins(dp(104), dp(6), dp(152), 0);
        top.addView(aiChatTitleText, titleParams);
        return top;
    }

    private TextView aiHeaderTab(String label, boolean active) {
        TextView tab = new TextView(this);
        tab.setText(label);
        tab.setTextColor(active ? TEXT : Color.rgb(158, 164, 188));
        tab.setTextSize(11);
        tab.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(dp(10), 0, dp(10), 0);
        return tab;
    }

    private Button aiComposerPill(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTextColor(Color.rgb(204, 209, 232));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(4), 0, dp(4), 0);
        button.setBackground(rounded(Color.rgb(49, 53, 68), Color.rgb(91, 74, 126), 1, 16));
        return button;
    }

    private TextView aiUpChevron() {
        TextView arrow = new TextView(this);
        arrow.setText("⌃");
        arrow.setGravity(Gravity.CENTER);
        arrow.setTextColor(Color.rgb(226, 230, 238));
        arrow.setTextSize(13);
        arrow.setTypeface(Typeface.DEFAULT_BOLD);
        arrow.setBackgroundColor(Color.TRANSPARENT);
        return arrow;
    }

    private void updateAiRevertChangesButton() {
        updateAiChangeButtons();
    }

    private void updateAiChangeButtons() {
        if (aiRevertChangesButton == null) return;
        if (aiRecentChatsVisible) {
            aiRevertChangesButton.setVisibility(View.GONE);
            if (aiRedoChangesButton != null) aiRedoChangesButton.setVisibility(View.GONE);
            return;
        }
        aiRevertChangesButton.setVisibility(View.VISIBLE);
        boolean hasChanges = !aiFileChanges.isEmpty();
        AiChangeSummary summary = summarizeAiChanges();
        aiRevertChangesButton.setEnabled(hasChanges);
        aiRevertChangesButton.setAlpha(hasChanges ? 1f : 0.38f);
        aiRevertChangesButton.setText(summary.conflicts > 0 ? "!" : "↶");
        aiRevertChangesButton.setTextColor(summary.conflicts > 0 ? Color.rgb(255, 207, 83)
                : hasChanges ? Color.rgb(226, 230, 238) : Color.rgb(112, 118, 130));
        aiRevertChangesButton.setContentDescription(aiChangeSummaryLabel(summary));
        if (aiRedoChangesButton != null) {
            aiRedoChangesButton.setVisibility(View.VISIBLE);
            boolean hasRedo = !aiRedoFileChanges.isEmpty();
            aiRedoChangesButton.setEnabled(hasRedo);
            aiRedoChangesButton.setAlpha(hasRedo ? 1f : 0.38f);
            aiRedoChangesButton.setTextColor(hasRedo ? Color.rgb(226, 230, 238) : Color.rgb(112, 118, 130));
            aiRedoChangesButton.setContentDescription(hasRedo
                    ? "Redo " + aiRedoFileChanges.size() + " AI change" + (aiRedoFileChanges.size() == 1 ? "" : "s")
                    : "No AI changes to redo");
        }
    }

    private void refreshAiComposerControls() {
        AutocompleteProvider provider = currentProvider();
        String modelName = providerModel(provider);
        if (aiModelNameText != null) aiModelNameText.setText(modelName.isEmpty() ? "model" : modelName);
        if (aiProviderNameText != null) aiProviderNameText.setText(provider.name);
        if (aiModelButton != null) {
            String model = providerModel(provider);
            if (model.length() > 18) model = model.substring(0, 17) + "…";
            aiModelButton.setText(provider.name + " · " + (model.isEmpty() ? "model" : model));
        }
        if (aiAptButton != null) {
            boolean available = isAptAvailable();
            aiAptButton.setEnabled(available);
            aiAptButton.setAlpha(available ? 1f : 0.45f);
            aiAptButton.setText(available ? (aiAptToolsEnabled ? "APT on" : "APT off") : "APT n/a");
        }
    }

    private void showAiModelSwitcher(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        for (AutocompleteProvider provider : AUTOCOMPLETE_PROVIDERS) {
            if (!"local".equals(provider.id)) {
                menu.getMenu().add(provider.name + " · " + providerModel(provider));
            }
        }
        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            for (AutocompleteProvider provider : AUTOCOMPLETE_PROVIDERS) {
                if (title.startsWith(provider.name + " · ")) {
                    prefs.edit().putString("autocomplete_provider", provider.id).apply();
                    ensureProviderDefaults(provider);
                    refreshAiComposerControls();
                    return true;
                }
            }
            return false;
        });
        menu.show();
    }

    private void showAiModelMiniWindow(View anchor) {
        AutocompleteProvider provider = currentProvider();
        if (provider.canFetchModels && providerModels(provider).isEmpty()) {
            if (provider.needsKey && providerApiKey(provider).isEmpty()) {
                Toast.makeText(this, "Add " + provider.name + " API key first", Toast.LENGTH_SHORT).show();
            } else {
                fetchAiModelsForMiniWindow(anchor, provider);
                return;
            }
        }
        ArrayList<String> models = providerModels(provider);
        if (models.isEmpty() && !provider.defaultModel.isEmpty()) models.add(provider.defaultModel);
        LinearLayout list = aiMiniWindowBase();
        if (models.isEmpty()) {
            list.addView(aiMiniWindowRow("No models found", false, null), new LinearLayout.LayoutParams(-1, dp(38)));
        } else {
            int limit = Math.min(7, models.size());
            for (int i = 0; i < limit; i++) {
                String model = models.get(i);
                list.addView(aiMiniWindowRow(model, true, () -> {
                    prefs.edit().putString(providerPrefKey(provider, "model"), model).apply();
                    refreshAiComposerControls();
                    addAiStatusMessage("Model switched to " + model);
                }), new LinearLayout.LayoutParams(-1, dp(38)));
            }
        }
        showAiMiniWindow(anchor, list, Math.min(dp(380), dp(42) + Math.max(1, models.size()) * dp(38)), false);
    }

    private void fetchAiModelsForMiniWindow(View anchor, AutocompleteProvider provider) {
        LinearLayout loading = aiMiniWindowBase();
        loading.addView(aiMiniWindowRow("Fetching models...", false, null), new LinearLayout.LayoutParams(-1, dp(38)));
        PopupWindow popup = showAiMiniWindow(anchor, loading, dp(86), false);
        new Thread(() -> {
            ArrayList<String> models = new ArrayList<>();
            String error = "";
            try {
                models = requestProviderModels(provider, providerApiKey(provider));
            } catch (Exception failure) {
                error = failure.getClass().getSimpleName() + ": " + failure.getMessage();
            }
            ArrayList<String> finalModels = models;
            String finalError = error;
            runOnUiThread(() -> {
                popup.dismiss();
                if (finalModels.isEmpty()) {
                    Toast.makeText(this, finalError.isEmpty() ? "No models returned" : "Model fetch failed: " + shortAiError(finalError), Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder cache = new StringBuilder();
                for (String model : finalModels) {
                    if (cache.length() > 0) cache.append('\n');
                    cache.append(model);
                }
                prefs.edit().putString(providerPrefKey(provider, "models"), cache.toString()).apply();
                showAiModelMiniWindow(anchor);
            });
        }, "andropy-ai-mini-models").start();
    }

    private void showAiProviderMiniWindow(View anchor) {
        LinearLayout list = aiMiniWindowBase();
        for (AutocompleteProvider provider : AUTOCOMPLETE_PROVIDERS) {
            boolean keyMissing = provider.needsKey && providerApiKey(provider).isEmpty();
            String label = keyMissing ? provider.name + "  •  add API key" : provider.name;
            list.addView(aiMiniWindowRow(label, !keyMissing, () -> {
                prefs.edit().putString("autocomplete_provider", provider.id).apply();
                ensureProviderDefaults(provider);
                refreshAiComposerControls();
                maybeAutoFetchProviderModels(provider);
                addAiStatusMessage("Model switched to " + providerModel(provider));
            }), new LinearLayout.LayoutParams(-1, dp(38)));
        }
        showAiMiniWindow(anchor, list, dp(380), true);
    }

    private LinearLayout aiMiniWindowBase() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(8), dp(8), dp(8));
        return list;
    }

    private TextView aiMiniWindowRow(String label, boolean enabled, Runnable action) {
        TextView row = new TextView(this);
        row.setText(label);
        row.setTextSize(12);
        row.setSingleLine(true);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), 0, dp(10), 0);
        row.setTextColor(enabled ? Color.rgb(225, 229, 238) : Color.rgb(191, 142, 142));
        row.setAlpha(enabled ? 1f : 0.82f);
        row.setBackground(rounded(Color.TRANSPARENT, Color.TRANSPARENT, 0, 8));
        if (enabled && action != null) row.setOnClickListener(v -> {
            View parent = (View) row.getParent();
            Object tag = parent == null ? null : parent.getTag();
            action.run();
            if (tag instanceof PopupWindow) ((PopupWindow) tag).dismiss();
        });
        return row;
    }

    private PopupWindow showAiMiniWindow(View anchor, LinearLayout content, int height, boolean rightSide) {
        int width = Math.min(dp(212), Math.max(dp(184), aiChatPanelWidth() - dp(150)));
        FrameLayout shell = new FrameLayout(this);
        shell.setPadding(0, 0, 0, 0);
        shell.setBackground(rounded(Color.rgb(35, 37, 43), Color.rgb(238, 240, 246), 1, 14));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        shell.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        PopupWindow popup = new PopupWindow(shell, width, height, true);
        content.setTag(popup);
        popup.setOutsideTouchable(true);
        popup.setAnimationStyle(getResources().getIdentifier("AiPickerPopupAnimation", "style", getPackageName()));
        popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        int panelLeft = dp(6);
        int panelRight = getResources().getDisplayMetrics().widthPixels - dp(6);
        if (aiChatPanel != null) {
            int[] panelLocation = new int[2];
            aiChatPanel.getLocationOnScreen(panelLocation);
            panelLeft = panelLocation[0] + dp(14);
            panelRight = panelLocation[0] + aiChatPanel.getWidth() - dp(14);
        }
        int x = rightSide ? panelRight - width : panelLeft;
        x = Math.max(panelLeft, Math.min(x, panelRight - width));
        int y = Math.max(dp(80), location[1] - height - dp(8));
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y);
        return popup;
    }

    private void toggleAiAptTools() {
        if (!isAptAvailable()) {
            Toast.makeText(this, "apt is not available in this runtime", Toast.LENGTH_SHORT).show();
            return;
        }
        aiAptToolsEnabled = !aiAptToolsEnabled;
        refreshAiComposerControls();
        Toast.makeText(this, "apt tools " + (aiAptToolsEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    private boolean isAptAvailable() {
        File apt = new File(binRoot == null ? new File("") : binRoot, "apt");
        return apt.exists() && apt.canExecute();
    }

    private LinearLayout buildProjectPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(PROJECT_PANEL_BG);
        panel.setElevation(dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), 0, dp(8), 0);
        header.setBackgroundColor(PROJECT_PANEL_HEADER);

        TextView title = new TextView(this);
        title.setText("Explorer");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine(true);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        ImageButton folder = headerIcon("ic_folder_open_24", "Open project");
        folder.setBackgroundColor(PROJECT_PANEL_HEADER);
        folder.setOnClickListener(v -> chooseProjectFolder());
        header.addView(folder, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView revertAi = new TextView(this);
        AiChangeSummary summary = summarizeAiChanges();
        revertAi.setText(summary.conflicts > 0 ? "!" : "↶");
        revertAi.setTextColor(summary.conflicts > 0 ? Color.rgb(255, 207, 83)
                : aiFileChanges.isEmpty() ? Color.rgb(112, 118, 130) : Color.rgb(226, 230, 238));
        revertAi.setTextSize(22);
        revertAi.setGravity(Gravity.CENTER);
        revertAi.setTypeface(Typeface.DEFAULT_BOLD);
        revertAi.setContentDescription(aiChangeSummaryLabel(summary));
        revertAi.setEnabled(!aiFileChanges.isEmpty());
        revertAi.setAlpha(aiFileChanges.isEmpty() ? 0.42f : 1f);
        revertAi.setOnClickListener(v -> revertAiFileChanges());
        header.addView(revertAi, new LinearLayout.LayoutParams(dp(38), dp(38)));

        ImageButton close = headerIcon("ic_arrow_back_24", "Close");
        close.setBackgroundColor(PROJECT_PANEL_HEADER);
        close.setOnClickListener(v -> closeProjectPanel());
        header.addView(close, new LinearLayout.LayoutParams(dp(38), dp(38)));

        panel.addView(header, new LinearLayout.LayoutParams(-1, dp(48)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout tree = new LinearLayout(this);
        tree.setOrientation(LinearLayout.VERTICAL);
        tree.setPadding(0, dp(8), 0, dp(18));
        populateProjectTree(tree);
        scroll.addView(tree, new ScrollView.LayoutParams(-1, -2));
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return panel;
    }

    private void populateProjectTree(LinearLayout tree) {
        File root = projectRoot();
        if (root == null || !root.isDirectory()) {
            TextView empty = new TextView(this);
            empty.setText("Open a project folder");
            empty.setTextColor(MUTED);
            empty.setTextSize(14);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(18), dp(34), dp(18), dp(12));
            tree.addView(empty, new LinearLayout.LayoutParams(-1, -2));

            TextView pick = new TextView(this);
            pick.setText("Select folder");
            pick.setTextColor(Color.rgb(28, 32, 38));
            pick.setTextSize(13);
            pick.setTypeface(Typeface.DEFAULT_BOLD);
            pick.setGravity(Gravity.CENTER);
            pick.setBackground(rounded(YELLOW_DIVIDER, Color.TRANSPARENT, 0, 7));
            pick.setOnClickListener(v -> chooseProjectFolder());
            LinearLayout.LayoutParams pickParams = new LinearLayout.LayoutParams(-1, dp(38));
            pickParams.setMargins(dp(16), dp(8), dp(16), 0);
            tree.addView(pick, pickParams);
            return;
        }

        expandedProjectDirs.add(root.getAbsolutePath());
        tree.addView(projectRootHeader(root));
        addProjectChildren(tree, root, 0, 0, 280);
    }

    private View projectRootHeader(File root) {
        TextView view = new TextView(this);
        view.setText(root.getName().isEmpty() ? root.getAbsolutePath() : root.getName());
        view.setTextColor(TEXT);
        view.setTextSize(13);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setSingleLine(true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(dp(14), 0, dp(10), 0);
        view.setBackgroundColor(PROJECT_PANEL_HEADER);
        return view;
    }

    private int addProjectChildren(LinearLayout tree, File directory, int depth, int count, int limit) {
        if (count >= limit || depth > 7) return count;
        File[] children = directory.listFiles();
        if (children == null) return count;
        Arrays.sort(children, Comparator
                .comparing((File file) -> !file.isDirectory())
                .thenComparing(file -> file.getName().toLowerCase(Locale.US)));
        for (File child : children) {
            if (count >= limit) break;
            if (child.isHidden() || shouldHideProjectFile(child)) continue;
            tree.addView(projectTreeRow(child, depth));
            count++;
            if (child.isDirectory() && expandedProjectDirs.contains(child.getAbsolutePath())) {
                count = addProjectChildren(tree, child, depth + 1, count, limit);
            }
        }
        return count;
    }

    private boolean shouldHideProjectFile(File file) {
        String name = file.getName();
        return "usr".equals(name) || ".gradle".equals(name) || "build".equals(name)
                || "__pycache__".equals(name) || "node_modules".equals(name);
    }

    private View projectTreeRow(File file, int depth) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10 + depth * 14), 0, dp(8), 0);
        row.setBackgroundColor(PROJECT_PANEL_BG);
        row.setOnClickListener(v -> {
            if (file.isDirectory()) {
                String path = file.getAbsolutePath();
                if (expandedProjectDirs.contains(path)) expandedProjectDirs.remove(path);
                else expandedProjectDirs.add(path);
                refreshProjectPanel();
            } else {
                openFileInEditor(file);
                closeProjectPanel();
            }
        });

        TextView arrow = new TextView(this);
        arrow.setText(file.isDirectory() ? (expandedProjectDirs.contains(file.getAbsolutePath()) ? "⌄" : "›") : "");
        arrow.setTextColor(MUTED);
        arrow.setTextSize(14);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(18), dp(34)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(getResources().getIdentifier(file.isDirectory() ? "ic_folder_24" : "ic_file_24", "drawable", getPackageName()));
        icon.setColorFilter(file.isDirectory() ? Color.rgb(231, 203, 111) : PANEL_ICON);
        icon.setPadding(dp(2), dp(7), dp(8), dp(7));
        row.addView(icon, new LinearLayout.LayoutParams(dp(30), dp(34)));

        TextView name = new TextView(this);
        name.setText(file.getName());
        name.setTextColor(file.isDirectory() ? TEXT : MUTED);
        name.setTextSize(13);
        name.setSingleLine(true);
        name.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(name, new LinearLayout.LayoutParams(0, dp(34), 1));
        if (hasAiAddedChange(file)) {
            row.addView(changeDot(AI_DIFF_ADD_DOT), new LinearLayout.LayoutParams(dp(10), dp(34)));
        }
        if (hasAiDeletedChange(file)) {
            row.addView(changeDot(AI_DIFF_DEL_DOT), new LinearLayout.LayoutParams(dp(10), dp(34)));
        }
        return row;
    }

    private View changeDot(int color) {
        TextView dot = new TextView(this);
        dot.setText("●");
        dot.setTextColor(color);
        dot.setTextSize(10);
        dot.setGravity(Gravity.CENTER);
        return dot;
    }

    private void revertAiFileChanges() {
        if (aiFileChanges.isEmpty()) {
            Toast.makeText(this, "No AI changes to revert", Toast.LENGTH_SHORT).show();
            return;
        }
        int restored = 0;
        int failed = 0;
        int conflicts = 0;
        ArrayList<String> deletedCreatedPaths = new ArrayList<>();
        ArrayList<String> revertedKeys = new ArrayList<>();
        HashMap<String, AiFileChange> redoSet = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>(aiFileChanges.keySet());
        for (String path : keys) {
            AiFileChange change = aiFileChanges.get(path);
            if (change == null) continue;
            File file = new File(path);
            try {
                if (!aiFileStillMatchesTrackedAfter(file, change)) {
                    conflicts++;
                    continue;
                }
                if (change.existedBefore) {
                    writeTextChecked(file, change.before);
                } else if (file.exists() && !file.delete()) {
                    throw new IOException("could not delete created file");
                } else {
                    deletedCreatedPaths.add(path);
                    cleanupAiGeneratedRuntimeArtifacts(file);
                    pruneEmptyParents(file.getParentFile());
                }
                restored++;
                revertedKeys.add(path);
                redoSet.put(path, change);
            } catch (IOException ignored) {
                failed++;
            }
        }
        for (String path : revertedKeys) aiFileChanges.remove(path);
        if (!redoSet.isEmpty()) {
            aiRedoFileChanges.clear();
            aiRedoFileChanges.putAll(redoSet);
        }
        saveAiFileChanges();
        if (!deletedCreatedPaths.isEmpty()) {
            prefs.edit().putString("opened_files", openedFilesWithout(deletedCreatedPaths)).apply();
        }
        File current = currentEditorDiskFile();
        if (current != null && deletedCreatedPaths.contains(canonicalKey(current))) {
            switchToFallbackOpenFile(current.getAbsolutePath());
        } else if (current != null && current.isFile()) {
            try {
                String content = new String(readFileBytes(current, 2 * 1024 * 1024), StandardCharsets.UTF_8);
                refreshCurrentEditorAfterAiWrite(current, content);
            } catch (IOException ignored) {
                applySoraSyntaxHighlighting();
            }
        } else {
            applySoraSyntaxHighlighting();
        }
        refreshProjectPanel();
        String message = "Reverted " + restored + " AI file change" + (restored == 1 ? "" : "s");
        if (failed > 0) message += ", " + failed + " failed";
        if (conflicts > 0) message += ", " + conflicts + " changed since AI";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void redoAiFileChanges() {
        if (aiRedoFileChanges.isEmpty()) {
            Toast.makeText(this, "No AI changes to redo", Toast.LENGTH_SHORT).show();
            return;
        }
        int redone = 0;
        int failed = 0;
        int conflicts = 0;
        ArrayList<String> redoneKeys = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>(aiRedoFileChanges.keySet());
        for (String path : keys) {
            AiFileChange change = aiRedoFileChanges.get(path);
            if (change == null) continue;
            File file = new File(path);
            try {
                if (!aiFileStillMatchesTrackedBefore(file, change)) {
                    conflicts++;
                    continue;
                }
                writeTextChecked(file, change.after);
                change.markers = buildAiDiffMarkers(change.before, change.after);
                aiFileChanges.put(path, change);
                redoneKeys.add(path);
                redone++;
            } catch (IOException ignored) {
                failed++;
            }
        }
        for (String path : redoneKeys) aiRedoFileChanges.remove(path);
        saveAiFileChanges();
        File current = currentEditorDiskFile();
        if (current != null && current.isFile()) {
            try {
                String content = new String(readFileBytes(current, 2 * 1024 * 1024), StandardCharsets.UTF_8);
                refreshCurrentEditorAfterAiWrite(current, content);
            } catch (IOException ignored) {
                applySoraSyntaxHighlighting();
            }
        } else {
            applySoraSyntaxHighlighting();
        }
        refreshProjectPanel();
        updateAiChangeButtons();
        String message = "Redid " + redone + " AI file change" + (redone == 1 ? "" : "s");
        if (failed > 0) message += ", " + failed + " failed";
        if (conflicts > 0) message += ", " + conflicts + " changed since revert";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private AiChangeSummary summarizeAiChanges() {
        AiChangeSummary summary = new AiChangeSummary();
        summary.total = aiFileChanges.size();
        for (String path : aiFileChanges.keySet()) {
            AiFileChange change = aiFileChanges.get(path);
            if (change == null) continue;
            try {
                if (aiFileStillMatchesTrackedAfter(new File(path), change)) summary.clean++;
                else summary.conflicts++;
            } catch (IOException e) {
                summary.conflicts++;
            }
        }
        return summary;
    }

    private String aiChangeSummaryLabel(AiChangeSummary summary) {
        if (summary == null || summary.total == 0) return "No AI changes";
        if (summary.conflicts == 0) {
            return "Revert " + summary.clean + " AI change" + (summary.clean == 1 ? "" : "s");
        }
        return summary.clean + " revertable, " + summary.conflicts + " changed since AI";
    }

    private boolean aiFileStillMatchesTrackedAfter(File file, AiFileChange change) throws IOException {
        if (file == null || change == null) return false;
        if (!file.exists()) return !change.existedBefore;
        if (!file.isFile()) return false;
        int limit = Math.max(2 * 1024 * 1024, change.after.getBytes(StandardCharsets.UTF_8).length + 1024);
        String current = new String(readFileBytes(file, limit), StandardCharsets.UTF_8);
        return current.equals(change.after);
    }

    private boolean aiFileStillMatchesTrackedBefore(File file, AiFileChange change) throws IOException {
        if (file == null || change == null) return false;
        if (!change.existedBefore) return !file.exists();
        if (!file.exists() || !file.isFile()) return false;
        int limit = Math.max(2 * 1024 * 1024, change.before.getBytes(StandardCharsets.UTF_8).length + 1024);
        String current = new String(readFileBytes(file, limit), StandardCharsets.UTF_8);
        return current.equals(change.before);
    }

    private void cleanupAiGeneratedRuntimeArtifacts(File sourceFile) {
        if (sourceFile == null || !sourceFile.getName().endsWith(".py")) return;
        File parent = sourceFile.getParentFile();
        File cache = parent == null ? null : new File(parent, "__pycache__");
        if (cache != null && cache.isDirectory()) deletePath(cache);
    }

    private void pruneEmptyParents(File start) {
        if (start == null) return;
        File stop = projectRoot();
        if (stop == null) stop = homeRoot;
        try {
            String stopPath = stop.getCanonicalPath();
            File current = start;
            while (current != null && current.isDirectory()) {
                String currentPath = current.getCanonicalPath();
                if (currentPath.equals(stopPath) || !currentPath.startsWith(stopPath + File.separator)) break;
                String[] children = current.list();
                if (children == null || children.length != 0 || !current.delete()) break;
                current = current.getParentFile();
            }
        } catch (IOException ignored) {
        }
    }

    private void refreshProjectPanel() {
        if (projectPanel == null) return;
        int index = projectPanel.getChildCount() > 1 ? 1 : -1;
        if (index < 0) return;
        projectPanel.removeViewAt(index);
        ScrollView scroll = new ScrollView(this);
        LinearLayout tree = new LinearLayout(this);
        tree.setOrientation(LinearLayout.VERTICAL);
        tree.setPadding(0, dp(8), 0, dp(18));
        populateProjectTree(tree);
        scroll.addView(tree, new ScrollView.LayoutParams(-1, -2));
        projectPanel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private View buildFileRootScreen() {
        fileManagerVisible = true;
        fileManagerCurrentDir = null;
        fileManagerCurrentTitle = "Directory";
        selectedFilePaths.clear();

        FrameLayout root = fileManagerBase("Directory", v -> showEditor());
        LinearLayout content = fileManagerContent(root);
        File sdcard = new File("/sdcard");

        content.addView(fileLocationRow(
                "Internal storage",
                sdcard.getAbsolutePath(),
                usedOfTotal(sdcard),
                "ic_storage_24",
                v -> showDirectory(sdcard, "Internal storage")));

        content.addView(fileLocationRow(
                "app_home",
                homeRoot.getAbsolutePath(),
                "Used " + formatBytes(directorySize(homeRoot, 20000)) + " of " + formatBytes(storageTotal(homeRoot)),
                "ic_folder_24",
                v -> showDirectory(homeRoot, "app_home")));

        File project = projectRoot();
        content.addView(fileLocationRow(
                project == null ? "Project folder" : "Project: " + project.getName(),
                project == null ? "Choose a folder for the Explorer panel" : project.getAbsolutePath(),
                choosingProjectFolder ? "Open a folder, then tap the check mark" : "Shown in the left Explorer drawer",
                "ic_folder_open_24",
                v -> {
                    choosingProjectFolder = true;
                    showDirectory(project != null && project.isDirectory() ? project : homeRoot, project == null ? "app_home" : project.getName());
                }));

        return root;
    }

    private View buildDirectoryScreen(File directory, String title) {
        fileManagerVisible = true;
        fileManagerCurrentDir = directory;
        fileManagerCurrentTitle = title;

        FrameLayout root = fileManagerBase(title, v -> navigateFileManagerBack(directory));
        LinearLayout content = fileManagerContent(root);

        File parent = directory.getParentFile();
        if (parent != null && !directory.equals(new File("/sdcard")) && !directory.equals(homeRoot)) {
            content.addView(fileEntryRow(parent, "..", "Folder", true, false));
        }

        File[] children = directory.listFiles();
        if (children == null) {
            TextView empty = new TextView(this);
            empty.setText("No access");
            empty.setTextColor(MUTED);
            empty.setTextSize(14);
            empty.setPadding(dp(22), dp(24), dp(22), 0);
            content.addView(empty, new LinearLayout.LayoutParams(-1, -2));
            return root;
        }

        Arrays.sort(children, Comparator
                .comparing((File file) -> !file.isDirectory())
                .thenComparing(file -> file.getName().toLowerCase(Locale.US)));
        for (File child : children) {
            if (child.isHidden() && child.getName().startsWith(".")) continue;
            content.addView(fileEntryRow(child, child.getName(), child.isDirectory() ? "Folder" : formatBytes(child.length()), child.isDirectory()));
        }
        return root;
    }

    private FrameLayout fileManagerBase(String title, View.OnClickListener backClick) {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(PANEL_BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PANEL_BG);
        fileManagerScreenBody = root;

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(12), 0, dp(12), 0);
        topBar.setBackgroundColor(PANEL_HEADER);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_arrow_back_24", "drawable", getPackageName()));
        back.setColorFilter(TEXT);
        back.setBackgroundColor(PANEL_HEADER);
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(backClick);
        topBar.addView(back, new LinearLayout.LayoutParams(dp(40), dp(40)));

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(18);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setSingleLine(true);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setPadding(dp(14), 0, 0, 0);
        topBar.addView(titleView, new LinearLayout.LayoutParams(0, dp(54), 1));

        if (fileManagerCurrentDir != null) {
            if (choosingProjectFolder) {
                ImageButton setProject = headerIcon("ic_done_24", "Set project folder");
                setProject.setOnClickListener(v -> setProjectFolder(fileManagerCurrentDir));
                topBar.addView(setProject, new LinearLayout.LayoutParams(dp(40), dp(40)));
            }

            ImageButton add = headerIcon("ic_add_24", "New");
            add.setOnClickListener(v -> showCreateMenu(add));
            topBar.addView(add, new LinearLayout.LayoutParams(dp(40), dp(40)));

            ImageButton more = headerIcon("ic_more_vert_24", "More");
            more.setOnClickListener(v -> showFileActionMenu(more));
            topBar.addView(more, new LinearLayout.LayoutParams(dp(40), dp(40)));
        }

        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(54)));
        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));

        if (pendingFileOperation != null && fileManagerCurrentDir != null) {
            ImageButton commit = new ImageButton(this);
            commit.setImageResource(getResources().getIdentifier("ic_done_24", "drawable", getPackageName()));
            commit.setColorFilter(Color.rgb(24, 28, 34));
            commit.setBackground(rounded(YELLOW_DIVIDER, Color.TRANSPARENT, 0, 28));
            commit.setContentDescription("Use this folder");
            commit.setPadding(dp(12), dp(12), dp(12), dp(12));
            commit.setElevation(dp(8));
            commit.setOnClickListener(v -> finishPendingFileOperation(fileManagerCurrentDir));
            FrameLayout.LayoutParams commitParams = new FrameLayout.LayoutParams(dp(56), dp(56));
            commitParams.gravity = Gravity.BOTTOM | Gravity.START;
            commitParams.setMargins(dp(18), 0, 0, dp(22));
            shell.addView(commit, commitParams);
        }

        return shell;
    }

    private ImageButton headerIcon(String iconName, String description) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(getResources().getIdentifier(iconName, "drawable", getPackageName()));
        button.setColorFilter(TEXT);
        button.setBackgroundColor(PANEL_HEADER);
        button.setContentDescription(description);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        return button;
    }

    private LinearLayout fileManagerContent(FrameLayout shell) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(18), 0, pendingFileOperation == null ? dp(28) : dp(96));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        fileManagerScreenBody.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return content;
    }

    private View fileLocationRow(String title, String path, String detail, String iconName, View.OnClickListener click) {
        LinearLayout row = managerRowBase(click);
        row.addView(fileIcon(iconName), new LinearLayout.LayoutParams(dp(54), dp(54)));
        row.addView(fileTextBlock(title, path, detail), new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private View fileEntryRow(File file, String name, String detail, boolean folder) {
        return fileEntryRow(file, name, detail, folder, true);
    }

    private View fileEntryRow(File file, String name, String detail, boolean folder, boolean selectable) {
        LinearLayout row = managerRowBase(v -> {
            if (!selectedFilePaths.isEmpty()) {
                if (selectable) toggleFileSelection(file);
            } else if (file.isDirectory()) {
                clearFileSelection();
                showDirectory(file, name);
            } else {
                openFileInEditor(file);
            }
        });
        row.setOnLongClickListener(v -> {
            if (!selectable) return false;
            toggleFileSelection(file);
            return true;
        });
        if (isFileSelected(file)) {
            row.setBackgroundColor(Color.rgb(70, 76, 86));
        }
        row.addView(fileIcon(folder ? "ic_folder_24" : "ic_file_24"), new LinearLayout.LayoutParams(dp(54), dp(54)));
        row.addView(fileTextBlock(name, detail, null), new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private LinearLayout managerRowBase(View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(22), dp(8), dp(18), dp(8));
        row.setMinimumHeight(dp(76));
        row.setOnClickListener(click);
        row.setBackgroundColor(PANEL_BG);
        return row;
    }

    private ImageView fileIcon(String name) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(getResources().getIdentifier(name, "drawable", getPackageName()));
        icon.setColorFilter(PANEL_ICON);
        icon.setPadding(0, dp(6), dp(18), dp(6));
        return icon;
    }

    private LinearLayout fileTextBlock(String title, String subtitle, String detail) {
        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(TEXT);
        titleView.setTextSize(16);
        titleView.setSingleLine(true);
        text.addView(titleView, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle == null ? "" : subtitle);
        subtitleView.setTextColor(MUTED);
        subtitleView.setTextSize(12);
        subtitleView.setSingleLine(true);
        text.addView(subtitleView, new LinearLayout.LayoutParams(-1, -2));

        if (detail != null) {
            TextView detailView = new TextView(this);
            detailView.setText(detail);
            detailView.setTextColor(Color.rgb(178, 184, 194));
            detailView.setTextSize(12);
            detailView.setSingleLine(true);
            text.addView(detailView, new LinearLayout.LayoutParams(-1, -2));
        }
        return text;
    }

    private void showFileManagerRoot() {
        hideKeyboard();
        saveEditorState();
        setContentView(buildFileRootScreen());
    }

    private void chooseProjectFolder() {
        closeProjectPanel();
        choosingProjectFolder = true;
        showFileManagerRoot();
    }

    private void setProjectFolder(File folder) {
        if (folder == null || !folder.isDirectory()) {
            Toast.makeText(this, "Pick a folder", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().putString("project_path", folder.getAbsolutePath()).apply();
        expandedProjectDirs.clear();
        expandedProjectDirs.add(folder.getAbsolutePath());
        choosingProjectFolder = false;
        Toast.makeText(this, "Project folder set", Toast.LENGTH_SHORT).show();
        showEditor();
    }

    private File projectRoot() {
        String path = prefs.getString("project_path", "");
        if (path == null || path.trim().isEmpty()) return null;
        File file = new File(path);
        return file.isDirectory() ? file : null;
    }

    private ArrayList<OpenFileTab> openedFileTabs() {
        ArrayList<OpenFileTab> tabs = new ArrayList<>();
        String currentPath = prefs.getString("file_path", "");
        String currentName = currentFileName();
        if (currentPath == null || currentPath.isEmpty()) {
            tabs.add(new OpenFileTab("", currentName));
        }

        String raw = prefs.getString("opened_files", "");
        if (raw != null && !raw.isEmpty()) {
            String[] lines = raw.split("\n");
            for (String line : lines) {
                int sep = line.indexOf('\t');
                if (sep < 0) continue;
                String path = line.substring(0, sep);
                String name = line.substring(sep + 1);
                if (path.isEmpty() || name.isEmpty()) continue;
                File file = new File(path);
                if (file.exists()) tabs.add(new OpenFileTab(path, name));
            }
        }
        if (currentPath != null && !currentPath.isEmpty()) {
            boolean seen = false;
            for (OpenFileTab tab : tabs) {
                if (currentPath.equals(tab.path)) {
                    seen = true;
                    break;
                }
            }
            if (!seen) tabs.add(0, new OpenFileTab(currentPath, currentName));
        }
        if (tabs.size() > 10) return new ArrayList<>(tabs.subList(0, 10));
        return tabs;
    }

    private String updatedOpenedFiles(String path, String name) {
        LinkedHashSet<String> rows = new LinkedHashSet<>();
        rows.add(path + "\t" + name);
        String raw = prefs.getString("opened_files", "");
        if (raw != null && !raw.isEmpty()) {
            for (String row : raw.split("\n")) {
                if (!row.startsWith(path + "\t") && row.contains("\t")) rows.add(row);
                if (rows.size() >= 10) break;
            }
        }
        StringBuilder out = new StringBuilder();
        for (String row : rows) {
            if (out.length() > 0) out.append('\n');
            out.append(row);
        }
        return out.toString();
    }

    private static final class OpenFileTab {
        final String path;
        final String name;

        OpenFileTab(String path, String name) {
            this.path = path == null ? "" : path;
            this.name = name == null || name.isEmpty() ? DEFAULT_FILE : name;
        }

        boolean matches(String otherPath, String otherName) {
            if (!path.isEmpty()) return path.equals(otherPath);
            return (otherPath == null || otherPath.isEmpty()) && name.equals(otherName);
        }
    }

    private static final class AiChatMessage {
        final String role;
        String text;

        AiChatMessage(String role, String text) {
            this.role = role == null ? "assistant" : role;
            this.text = text == null ? "" : text;
        }
    }

    private final class AiRecentChat {
        final String id;
        String title;
        long updatedAt;
        String updatedLabel;

        AiRecentChat(String id, String title, long updatedAt) {
            this.id = id == null ? "" : id;
            this.title = title == null || title.trim().isEmpty() ? "New chat" : title.trim();
            this.updatedAt = updatedAt;
            this.updatedLabel = formatAiRecentTime(updatedAt);
        }
    }

    private void showDirectory(File directory, String title) {
        hideKeyboard();
        setContentView(buildDirectoryScreen(directory, title == null || title.isEmpty() ? directory.getName() : title));
    }

    private void refreshFileManager() {
        if (fileManagerCurrentDir == null) {
            setContentView(buildFileRootScreen());
        } else {
            setContentView(buildDirectoryScreen(fileManagerCurrentDir, fileManagerCurrentTitle));
        }
    }

    private void clearFileSelection() {
        selectedFilePaths.clear();
    }

    private boolean isFileSelected(File file) {
        return selectedFilePaths.contains(file.getAbsolutePath());
    }

    private void toggleFileSelection(File file) {
        if (file == null || "..".equals(file.getName())) return;
        String path = file.getAbsolutePath();
        if (selectedFilePaths.contains(path)) selectedFilePaths.remove(path);
        else selectedFilePaths.add(path);
        refreshFileManager();
    }

    private ArrayList<File> selectedFiles() {
        ArrayList<File> files = new ArrayList<>();
        for (String path : selectedFilePaths) files.add(new File(path));
        return files;
    }

    private void navigateFileManagerBack(File directory) {
        if (!selectedFilePaths.isEmpty()) {
            clearFileSelection();
            refreshFileManager();
            return;
        }
        if (directory.equals(new File("/sdcard")) || directory.equals(homeRoot)) {
            showFileManagerRoot();
        } else {
            File parent = directory.getParentFile();
            showDirectory(parent == null ? homeRoot : parent, parent == null ? "Directory" : parent.getName());
        }
    }

    private void showCreateMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("New folder");
        menu.getMenu().add("New file");
        menu.setOnMenuItemClickListener(item -> {
            String title = String.valueOf(item.getTitle());
            if ("New folder".equals(title)) showCreateDialog(true);
            else showCreateDialog(false);
            return true;
        });
        menu.show();
    }

    private void showCreateDialog(boolean folder) {
        if (fileManagerCurrentDir == null) return;
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(folder ? "folder name" : "file name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setPadding(dp(18), dp(10), dp(18), dp(10));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(folder ? "New folder" : "New file")
                .setView(input)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();
        dialog.setOnShowListener(d -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty() || name.contains("/") || name.contains("\\")) {
                    Toast.makeText(this, "Invalid name", Toast.LENGTH_SHORT).show();
                    return;
                }
                File target = new File(fileManagerCurrentDir, name);
                if (target.exists()) {
                    Toast.makeText(this, "Already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean ok;
                try {
                    ok = folder ? target.mkdirs() : target.createNewFile();
                } catch (IOException e) {
                    ok = false;
                }
                Toast.makeText(this, ok ? "Created" : "Create failed", Toast.LENGTH_SHORT).show();
                if (ok) {
                    dialog.dismiss();
                    refreshFileManager();
                }
            });
        });
        dialog.show();
    }

    private void showFileActionMenu(View anchor) {
        if (selectedFilePaths.isEmpty() && pendingFileOperation == null) {
            Toast.makeText(this, "Select files first", Toast.LENGTH_SHORT).show();
            return;
        }
        PopupMenu menu = new PopupMenu(this, anchor);
        if (!selectedFilePaths.isEmpty()) {
            menu.getMenu().add("Copy");
            menu.getMenu().add("Move");
            menu.getMenu().add("Delete");
        }
        if (pendingFileOperation != null) menu.getMenu().add("Paste");
        menu.setOnMenuItemClickListener(item -> {
            String action = String.valueOf(item.getTitle()).toLowerCase(Locale.US);
            if ("copy".equals(action) || "move".equals(action)) {
                beginPendingFileOperation(action);
            } else if ("paste".equals(action)) {
                finishPendingFileOperation(fileManagerCurrentDir);
            } else if ("delete".equals(action)) {
                deleteSelectedFiles();
            }
            return true;
        });
        menu.show();
    }

    private void beginPendingFileOperation(String operation) {
        pendingFileOperationSources.clear();
        pendingFileOperationSources.addAll(selectedFiles());
        pendingFileOperation = operation;
        clearFileSelection();
        Toast.makeText(this, "Open destination folder", Toast.LENGTH_SHORT).show();
        refreshFileManager();
    }

    private void finishPendingFileOperation(File destination) {
        if (destination == null || pendingFileOperation == null || pendingFileOperationSources.isEmpty()) return;
        int completed = 0;
        for (File source : new ArrayList<>(pendingFileOperationSources)) {
            if (!source.exists() || isSameOrChild(destination, source)) continue;
            File target = uniqueDestination(destination, source.getName());
            boolean ok = "move".equals(pendingFileOperation) ? movePath(source, target) : copyPath(source, target);
            if (ok) completed++;
        }
        String label = "move".equals(pendingFileOperation) ? "Moved " : "Copied ";
        pendingFileOperation = null;
        pendingFileOperationSources.clear();
        Toast.makeText(this, label + completed, Toast.LENGTH_SHORT).show();
        refreshFileManager();
    }

    private void deleteSelectedFiles() {
        int deleted = 0;
        for (File file : selectedFiles()) {
            if (deletePath(file)) deleted++;
        }
        clearFileSelection();
        Toast.makeText(this, "Deleted " + deleted, Toast.LENGTH_SHORT).show();
        refreshFileManager();
    }

    private void openFileInEditor(File file) {
        openFileInEditor(file, true);
    }

    private void openFileInEditor(File file, boolean animate) {
        try {
            byte[] data = readFileBytes(file, 1024 * 1024);
            String code = new String(data, StandardCharsets.UTF_8);
            if (animate) {
                pendingEditorSwitchAnimation = true;
                pendingEditorSwitchDirection = 1;
            }
            prefs.edit()
                    .putString("code", code)
                    .putString("file_name", file.getName())
                    .putString("file_path", file.getAbsolutePath())
                    .putString("opened_files", updatedOpenedFiles(file.getAbsolutePath(), file.getName()))
                    .apply();
            showEditor();
        } catch (IOException e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout buildSidePanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(PANEL_BG);
        panel.setElevation(dp(10));

        panel.addView(panelHeader(), new LinearLayout.LayoutParams(-1, dp(126)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, dp(16), 0, dp(18));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

        content.addView(sectionLabel("Run"));
        content.addView(drawerRow("Interpreter", "Interpreter", "ic_run_box_24", "interpreter", ""));
        content.addView(drawerRow("Terminal", "Terminal", "ic_terminal_24", "terminal", ""));
        content.addView(divider());
        content.addView(drawerRow("Pip", "Pip", "ic_pip_24", "pip", ""));
        content.addView(drawerRow("Share", "Share", "ic_share_24", ""));
        content.addView(drawerRow("Pastebin", "Pastebin", "ic_upload_24", ""));
        content.addView(drawerRow("Samples", "Samples", "ic_samples_24", ""));
        content.addView(divider());
        content.addView(sectionLabel("Settings"));
        content.addView(drawerRow("Settings", "Settings", "ic_settings_24", "settings", ""));

        panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return panel;
    }

    private View panelHeader() {
        FrameLayout header = new FrameLayout(this);
        header.setBackgroundColor(PANEL_HEADER);
        header.setPadding(dp(18), dp(14), dp(18), dp(12));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        stats.setGravity(Gravity.BOTTOM | Gravity.START);

        panelFileName = new TextView(this);
        panelFileName.setTextColor(TEXT);
        panelFileName.setTextSize(16);
        panelFileName.setSingleLine(true);
        stats.addView(panelFileName, new LinearLayout.LayoutParams(-1, dp(30)));

        panelLine = new TextView(this);
        panelLine.setTextColor(MUTED);
        panelLine.setTextSize(14);
        stats.addView(panelLine, new LinearLayout.LayoutParams(-1, dp(28)));

        panelOffset = new TextView(this);
        panelOffset.setTextColor(MUTED);
        panelOffset.setTextSize(14);
        stats.addView(panelOffset, new LinearLayout.LayoutParams(-1, dp(28)));

        FrameLayout.LayoutParams statsParams = new FrameLayout.LayoutParams(dp(156), -1);
        statsParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        header.addView(stats, statsParams);

        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("ic_panel_logo_48", "drawable", getPackageName()));
        logo.setPadding(dp(10), dp(10), dp(10), dp(10));
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setShape(GradientDrawable.OVAL);
        logoBg.setColor(Color.rgb(25, 31, 39));
        logoBg.setStroke(dp(2), Color.rgb(246, 216, 96));
        logo.setBackground(logoBg);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(dp(76), dp(76));
        logoParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        header.addView(logo, logoParams);
        updatePanelStatus();
        return header;
    }

    private TextView sectionLabel(String title) {
        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(PANEL_SECTION);
        label.setTextSize(14);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setGravity(Gravity.CENTER_VERTICAL);
        label.setPadding(dp(18), 0, 0, 0);
        label.setSingleLine(true);
        label.setAlpha(0.92f);
        label.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(42)));
        return label;
    }

    private View drawerRow(String title, String action, String iconName, String badge) {
        return drawerRow(title, action, iconName, "", badge);
    }

    private View drawerRow(String title, String action, String iconName, String route, String badge) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(18), 0, dp(18), 0);
        item.setBackgroundColor(PANEL_BG);
        item.setOnClickListener(v -> {
            closePanel();
            if ("terminal".equals(route)) {
                showTerminal();
            } else if ("interpreter".equals(route)) {
                showInterpreter();
            } else if ("pip".equals(route)) {
                showPip();
            } else if ("settings".equals(route)) {
                showSettings();
            } else {
                Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
            }
        });

        ImageView icon = new ImageView(this);
        icon.setImageResource(getResources().getIdentifier(iconName, "drawable", getPackageName()));
        icon.setColorFilter(PANEL_ICON);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.setMarginEnd(dp(24));
        item.addView(icon, iconParams);

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextColor(TEXT);
        label.setTextSize(15);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setSingleLine(true);
        label.setGravity(Gravity.CENTER_VERTICAL);
        item.addView(label, new LinearLayout.LayoutParams(0, -1, 1));

        if (!badge.isEmpty()) {
            TextView ad = new TextView(this);
            ad.setText(badge);
            ad.setTextColor(PANEL_SECTION);
            ad.setTextSize(12);
            ad.setGravity(Gravity.CENTER);
            ad.setTypeface(Typeface.DEFAULT_BOLD);
            item.addView(ad, new LinearLayout.LayoutParams(dp(30), -1));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        item.setLayoutParams(params);
        return item;
    }

    private View divider() {
        View divider = new View(this);
        divider.setBackgroundColor(PANEL_DIVIDER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, Math.max(1, dp(1)));
        params.setMargins(0, dp(10), 0, dp(10));
        divider.setLayoutParams(params);
        return divider;
    }

    private View buildTerminalScreen() {
        terminalVisible = true;
        FrameLayout shell = new FrameLayout(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 18, 22));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_menu_24", "drawable", getPackageName()));
        back.setColorFilter(MUTED);
        back.setBackgroundColor(BAR);
        back.setContentDescription("Editor");
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(v -> showEditor());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView title = new TextView(this);
        title.setText("Terminal");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, 0, 0);
        topBar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

        termuxTerminalView = new com.termux.view.TerminalView(this, null);
        termuxTerminalView.setTerminalViewClient(new AndroPyTerminalViewClient());
        termuxTerminalView.setTextSize(Math.round(sp(terminalTextSp())));
        termuxTerminalView.setTypeface(Typeface.MONOSPACE);
        termuxTerminalView.setFocusable(true);
        termuxTerminalView.setFocusableInTouchMode(true);
        root.addView(termuxTerminalView, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(buildExtraKeysBar(true), new LinearLayout.LayoutParams(-1, dp(74)));

        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        startTerminal();
        return shell;
    }

    private void showTerminal() {
        prefs.edit().putString("code", editor == null ? "" : editor.getText().toString()).apply();
        settingsVisible = false;
        setContentView(buildTerminalScreen());
        termuxTerminalView.requestFocus();
    }

    private void showInterpreter() {
        prefs.edit().putString("code", editor == null ? "" : editor.getText().toString()).apply();
        terminalStartupCommand = "exec python";
        setContentView(buildTerminalScreen());
        termuxTerminalView.requestFocus();
    }

    private void showPip() {
        prefs.edit().putString("code", editor == null ? "" : editor.getText().toString()).apply();
        settingsVisible = false;
        setContentView(buildPipScreen());
        refreshPipPackages();
    }

    private void showSettings() {
        saveEditorState();
        terminalVisible = false;
        fileManagerVisible = false;
        settingsVisible = true;
        settingsPage = "root";
        stopTerminal();
        setContentView(buildSettingsRootScreen());
    }

    private View buildExtraKeysBar(boolean terminal) {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(Color.BLACK);
        shell.setPadding(0, dp(3), 0, dp(3));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER);
        shell.addView(row, new LinearLayout.LayoutParams(-1, -1));
        extraKeysRow = row;
        populateExtraKeysRow(row, terminal);
        return shell;
    }

    private void populateExtraKeysRow(LinearLayout row, boolean terminal) {
        row.removeAllViews();
        LinearLayout top = extraKeyLine();
        top.addView(extraKeyButton("TAB", false, () -> extraTab(terminal)));
        top.addView(extraKeyButton("⌨", false, () -> showSoftKeyboardForActiveSurface(terminal)));
        top.addView(extraKeyButton("SHIFT", extraShift, this::toggleExtraShift));
        top.addView(extraKeyButton("HOME", false, () -> extraHome(terminal)));
        top.addView(extraKeyButton("↑", false, () -> extraMove(terminal, KeyEvent.KEYCODE_DPAD_UP)));
        top.addView(extraKeyButton("END", false, () -> extraEnd(terminal)));
        top.addView(extraKeyButton("PGUP", false, () -> extraPage(terminal, -1)));
        row.addView(top, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bottom = extraKeyLine();
        bottom.addView(extraKeyButton("ESC", false, () -> extraEscape(terminal)));
        bottom.addView(extraKeyButton("CTRL", extraCtrl, this::toggleExtraCtrl));
        bottom.addView(extraKeyButton("ALT", extraAlt, this::toggleExtraAlt));
        bottom.addView(extraKeyButton("←", false, () -> extraMove(terminal, KeyEvent.KEYCODE_DPAD_LEFT)));
        bottom.addView(extraKeyButton("↓", false, () -> extraMove(terminal, KeyEvent.KEYCODE_DPAD_DOWN)));
        bottom.addView(extraKeyButton("→", false, () -> extraMove(terminal, KeyEvent.KEYCODE_DPAD_RIGHT)));
        bottom.addView(extraKeyButton("PGDN", false, () -> extraPage(terminal, 1)));
        row.addView(bottom, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private LinearLayout extraKeyLine() {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER);
        line.setPadding(dp(3), 0, dp(3), 0);
        return line;
    }

    private TextView extraKeyButton(String label, boolean active, Runnable action) {
        TextView button = new TextView(this);
        button.setText(label);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setLetterSpacing(0.08f);
        styleExtraKeyButton(button, active, false);
        final boolean modifier = isExtraModifier(label);
        final boolean[] longFired = new boolean[]{false};
        button.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    longFired[0] = false;
                    styleExtraKeyButton(button, active, true);
                    if (modifier) {
                        extraLongPressRunnable = () -> {
                            longFired[0] = true;
                            toggleExtraModifierLock(label);
                        };
                        button.postDelayed(extraLongPressRunnable, 1000);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (modifier && extraLongPressRunnable != null) {
                        button.removeCallbacks(extraLongPressRunnable);
                        extraLongPressRunnable = null;
                    }
                    styleExtraKeyButton(button, active, false);
                    if (!longFired[0]) action.run();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    if (modifier && extraLongPressRunnable != null) {
                        button.removeCallbacks(extraLongPressRunnable);
                        extraLongPressRunnable = null;
                    }
                    styleExtraKeyButton(button, active, false);
                    return true;
                default:
                    return true;
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.setMargins(dp(1), 0, dp(1), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void styleExtraKeyButton(TextView button, boolean active, boolean pressed) {
        if (pressed) {
            button.setTextColor(Color.BLACK);
            button.setBackground(rounded(Color.WHITE, Color.BLACK, 0, 0));
        } else if (active) {
            button.setTextColor(Color.rgb(26, 24, 20));
            button.setBackground(rounded(YELLOW_DIVIDER, Color.BLACK, 0, 0));
        } else {
            button.setTextColor(Color.WHITE);
            button.setBackground(rounded(Color.BLACK, Color.BLACK, 0, 0));
        }
    }

    private boolean isExtraModifier(String label) {
        return "CTRL".contentEquals(label) || "ALT".contentEquals(label) || "SHIFT".contentEquals(label);
    }

    private void toggleExtraModifierLock(String label) {
        if ("CTRL".contentEquals(label)) {
            extraCtrlLocked = !extraCtrlLocked;
            extraCtrl = extraCtrlLocked;
        } else if ("ALT".contentEquals(label)) {
            extraAltLocked = !extraAltLocked;
            extraAlt = extraAltLocked;
        } else if ("SHIFT".contentEquals(label)) {
            extraShiftLocked = !extraShiftLocked;
            extraShift = extraShiftLocked;
        }
        rebuildActiveKeyboardSurface();
    }

    private void showSoftKeyboardForActiveSurface(boolean terminal) {
        View target = terminal ? termuxTerminalView : editor;
        if (target == null) return;
        target.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
    }

    private void toggleExtraCtrl() {
        if (extraCtrl || extraCtrlLocked) {
            extraCtrlLocked = false;
            extraCtrl = false;
        } else {
            extraCtrl = true;
        }
        rebuildActiveKeyboardSurface();
    }

    private void toggleExtraShift() {
        if (extraShift || extraShiftLocked) {
            extraShiftLocked = false;
            extraShift = false;
        } else {
            extraShift = true;
        }
        rebuildActiveKeyboardSurface();
    }

    private void toggleExtraAlt() {
        if (extraAlt || extraAltLocked) {
            extraAltLocked = false;
            extraAlt = false;
        } else {
            extraAlt = true;
        }
        rebuildActiveKeyboardSurface();
    }

    private void rebuildActiveKeyboardSurface() {
        if (extraKeysRow != null) {
            populateExtraKeysRow(extraKeysRow, terminalVisible);
        }
    }

    private void consumeExtraOneShot() {
        boolean changed = false;
        if (!extraCtrlLocked && extraCtrl) {
            extraCtrl = false;
            changed = true;
        }
        if (!extraShiftLocked && extraShift) {
            extraShift = false;
            changed = true;
        }
        if (!extraAltLocked && extraAlt) {
            extraAlt = false;
            changed = true;
        }
        if (changed) rebuildActiveKeyboardSurface();
    }

    private void extraEscape(boolean terminal) {
        if (terminal) {
            terminalWrite("\u001b");
        } else {
            dismissCompletions();
            clearAiSuggestion();
            hideKeyboard();
        }
        consumeExtraOneShot();
    }

    private void extraTab(boolean terminal) {
        if (terminal) {
            terminalWrite(extraShift ? "\u001b[Z" : "\t");
        } else if (editor != null) {
            if (extraShift) {
                unindentEditorLine(editorSelectionStart());
            } else {
                acceptTabInEditor();
            }
        }
        consumeExtraOneShot();
    }

    private void acceptTabInEditor() {
        if (editor == null) return;
        if (hasAiSuggestion()) {
            acceptAiSuggestion(2);
        } else if (completionPopup != null && completionPopup.isShowing() && !activeCompletions.isEmpty()) {
            applyCompletion(activeCompletions.get(0));
        } else {
            CompletionItem current = currentCompletionAtCursor();
            if (current != null) {
                applyCompletion(current);
            } else {
                replaceEditorRange(editorSelectionStart(), editorSelectionEnd(), "    ");
            }
        }
    }

    private CompletionItem currentCompletionAtCursor() {
        if (editor == null) return null;
        String editable = editorText();
        int cursor = editorSelectionStart();
        if (cursor < 0 || cursor > editable.length() || cursor != editorSelectionEnd()) return null;
        String prefix = completionPrefix(editable, cursor);
        boolean expressionContext = isExpressionCompletionContext(editable, cursor, prefix);
        ArrayList<CompletionItem> suggestions = buildCompletionItems(editable, prefix, expressionContext);
        if (suggestions.isEmpty()) return null;
        if (prefix.length() < 2 && !prefix.equals(".") && !hasLocalCompletion(suggestions)) return null;
        return suggestions.get(0);
    }

    private void extraMove(boolean terminal, int keyCode) {
        if (terminal) {
            terminalWrite(terminalArrowSequence(keyCode));
        } else {
            moveEditorSelection(keyCode);
        }
        consumeExtraOneShot();
    }

    private void extraHome(boolean terminal) {
        if (terminal) {
            terminalWrite(terminalTildeOrFinalSequence("H", 0));
        } else if (editor != null) {
            int target = extraCtrl ? 0 : currentEditorLineStart();
            setEditorSelectionWithShift(target);
        }
        consumeExtraOneShot();
    }

    private void extraEnd(boolean terminal) {
        if (terminal) {
            terminalWrite(terminalTildeOrFinalSequence("F", 0));
        } else if (editor != null) {
            int target = extraCtrl ? editorLength() : currentEditorLineEnd();
            setEditorSelectionWithShift(target);
        }
        consumeExtraOneShot();
    }

    private void extraPage(boolean terminal, int direction) {
        if (terminal) {
            terminalWrite(terminalTildeOrFinalSequence(direction < 0 ? "5" : "6", '~'));
        } else if (editor != null) {
            int steps = Math.max(1, getVisibleEditorLineCount() - 1);
            int target = Math.max(0, editorSelectionEnd());
            for (int i = 0; i < steps; i++) {
                target = verticalEditorMove(target, direction < 0 ? -1 : 1);
            }
            setEditorSelectionWithShift(target);
        }
        consumeExtraOneShot();
    }

    private String terminalArrowSequence(int keyCode) {
        String suffix;
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) suffix = "A";
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) suffix = "B";
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) suffix = "C";
        else suffix = "D";
        int modifier = terminalModifierCode();
        if (modifier > 1) return "\u001b[1;" + modifier + suffix;
        return "\u001b[" + suffix;
    }

    private String terminalTildeOrFinalSequence(String code, int finalChar) {
        int modifier = terminalModifierCode();
        if (finalChar == '~') {
            return modifier > 1 ? "\u001b[" + code + ";" + modifier + "~" : "\u001b[" + code + "~";
        }
        return modifier > 1 ? "\u001b[1;" + modifier + code : "\u001b[" + code;
    }

    private int terminalModifierCode() {
        int modifier = 1;
        if (extraShift) modifier += 1;
        if (extraAlt) modifier += 2;
        if (extraCtrl) modifier += 4;
        return modifier;
    }

    private void terminalWrite(String value) {
        if (termuxSession == null || value == null || value.isEmpty()) return;
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        termuxSession.write(bytes, 0, bytes.length);
    }

    private void moveEditorSelection(int keyCode) {
        if (editor == null) return;
        int cursor = Math.max(0, editorSelectionEnd());
        int target = cursor;
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            target = extraCtrl ? previousWordBoundary(cursor) : Math.max(0, cursor - 1);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            target = extraCtrl ? nextWordBoundary(cursor) : Math.min(editorLength(), cursor + 1);
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            editor.moveOrExtendSelection(SelectionMovement.UP, extraShift);
            return;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            editor.moveOrExtendSelection(SelectionMovement.DOWN, extraShift);
            return;
        }
        setEditorSelectionWithShift(target);
    }

    private void setEditorSelectionWithShift(int target) {
        if (editor == null) return;
        int clamped = Math.max(0, Math.min(editorLength(), target));
        if (extraShift) {
            int anchor = Math.max(0, editorSelectionStart());
            setEditorSelection(anchor, clamped);
        } else {
            setEditorSelection(clamped);
        }
    }

    private int currentEditorLineStart() {
        int cursor = Math.max(0, editorSelectionEnd());
        String text = editorText();
        while (cursor > 0 && text.charAt(cursor - 1) != '\n') cursor--;
        return cursor;
    }

    private int currentEditorLineEnd() {
        int cursor = Math.max(0, editorSelectionEnd());
        String text = editorText();
        while (cursor < text.length() && text.charAt(cursor) != '\n') cursor++;
        return cursor;
    }

    private int verticalEditorMove(int cursor, int direction) {
        Content content = editorContent();
        if (content == null) return cursor;
        CharPosition pos = editorPositionForOffset(cursor);
        int nextLine = Math.max(0, Math.min(content.getLineCount() - 1, pos.line + direction));
        int nextColumn = Math.max(0, Math.min(content.getColumnCount(nextLine), pos.column));
        return content.getIndexer().getCharIndex(nextLine, nextColumn);
    }

    private int getVisibleEditorLineCount() {
        if (editor == null) return 8;
        return Math.max(1, editor.getHeight() / Math.max(1, editor.getRowHeight()));
    }

    private int previousWordBoundary(int cursor) {
        String text = editorText();
        int index = Math.max(0, cursor - 1);
        while (index > 0 && Character.isWhitespace(text.charAt(index))) index--;
        while (index > 0 && isWordChar(text.charAt(index - 1))) index--;
        return index;
    }

    private int nextWordBoundary(int cursor) {
        String text = editorText();
        int index = Math.min(text.length(), cursor);
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        while (index < text.length() && isWordChar(text.charAt(index))) index++;
        return index;
    }

    private boolean isWordChar(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    private void unindentEditorLine(int cursor) {
        String text = editorText();
        int start = Math.max(0, cursor);
        while (start > 0 && text.charAt(start - 1) != '\n') start--;
        int remove = 0;
        while (remove < 4 && start + remove < text.length() && text.charAt(start + remove) == ' ') remove++;
        if (remove > 0) replaceEditorRange(start, start + remove, "");
    }

    private void showAutocompleteSettings() {
        settingsVisible = true;
        settingsPage = "autocomplete";
        setContentView(buildAutocompleteSettingsScreen());
    }

    private void showEditorSettings() {
        settingsVisible = true;
        settingsPage = "editor";
        setContentView(buildEditorSettingsScreen());
    }

    private View buildSettingsRootScreen() {
        LinearLayout root = settingsScreenBase("Settings", v -> showEditor());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(18));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

        content.addView(settingsNavRow("Autocomplete", "Ghost text, providers, API keys, endpoints, and model selection.", "›", v -> showAutocompleteSettings()));
        content.addView(settingsNavRow("Editor", "Line wrapping mode and editor behavior.", "›", v -> showEditorSettings()));

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private View buildAutocompleteSettingsScreen() {
        LinearLayout root = settingsScreenBase("Autocomplete", v -> showSettings());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(18));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

        LinearLayout ghostPanel = settingsPanel();
        LinearLayout ghostRow = new LinearLayout(this);
        ghostRow.setOrientation(LinearLayout.HORIZONTAL);
        ghostRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView ghostCopy = settingsTitleBlock("Ghost text", "Shows a faint inline suggestion while you type.");
        ghostRow.addView(ghostCopy, new LinearLayout.LayoutParams(0, -2, 1));
        Switch ghostSwitch = new Switch(this);
        ghostSwitch.setChecked(prefs.getBoolean("autocomplete_ghost", true));
        ghostSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("autocomplete_ghost", isChecked).apply();
            if (!isChecked) clearAiSuggestion();
        });
        ghostRow.addView(ghostSwitch, new LinearLayout.LayoutParams(dp(62), dp(44)));
        ghostPanel.addView(ghostRow, new LinearLayout.LayoutParams(-1, -2));
        content.addView(ghostPanel);

        LinearLayout errorPanel = settingsPanel();
        LinearLayout errorRow = new LinearLayout(this);
        errorRow.setOrientation(LinearLayout.HORIZONTAL);
        errorRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView errorCopy = settingsTitleBlock("Automatic error handling",
                "Retries temporary autocomplete failures and waits out provider rate limits.");
        errorRow.addView(errorCopy, new LinearLayout.LayoutParams(0, -2, 1));
        Switch errorSwitch = new Switch(this);
        errorSwitch.setChecked(prefs.getBoolean("autocomplete_auto_errors", true));
        errorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("autocomplete_auto_errors", isChecked).apply();
            if (isChecked) {
                prefs.edit().remove("autocomplete_last_error").apply();
                cancelAiRetryCountdown();
            }
            setContentView(buildAutocompleteSettingsScreen());
        });
        errorRow.addView(errorSwitch, new LinearLayout.LayoutParams(dp(62), dp(44)));
        errorPanel.addView(errorRow, new LinearLayout.LayoutParams(-1, -2));
        String lastAutocompleteError = prefs.getString("autocomplete_last_error", "");
        if (!prefs.getBoolean("autocomplete_auto_errors", true) && !lastAutocompleteError.isEmpty()) {
            TextView errorText = new TextView(this);
            errorText.setText("Last error: " + lastAutocompleteError);
            errorText.setTextColor(Color.rgb(255, 176, 164));
            errorText.setTextSize(12);
            errorText.setPadding(0, dp(8), 0, 0);
            errorPanel.addView(errorText, new LinearLayout.LayoutParams(-1, -2));
        } else if (aiCompletionRetryAtMs > SystemClock.uptimeMillis()) {
            TextView retryText = new TextView(this);
            retryText.setText("Retrying in " + Math.max(1L, (aiCompletionRetryAtMs - SystemClock.uptimeMillis() + 999L) / 1000L) + "s");
            retryText.setTextColor(Color.rgb(176, 180, 188));
            retryText.setTextSize(12);
            retryText.setPadding(0, dp(8), 0, 0);
            errorPanel.addView(retryText, new LinearLayout.LayoutParams(-1, -2));
        }
        content.addView(errorPanel);

        content.addView(aiAgentLoopDelayPanel());

        content.addView(settingsSection("Provider"));
        for (AutocompleteProvider provider : AUTOCOMPLETE_PROVIDERS) {
            content.addView(providerRow(provider));
        }

        AutocompleteProvider provider = currentProvider();
        ensureProviderDefaults(provider);
        content.addView(settingsSection(provider.name + " connection"));
        if (provider.needsKey) {
            content.addView(settingsInput(providerPrefKey(provider, "api_key"), "API key", "Stored locally on this device", true,
                    () -> maybeAutoFetchProviderModels(provider)));
        }
        if (!"local".equals(provider.id)) {
            content.addView(settingsInput(providerPrefKey(provider, "endpoint"), "Endpoint", provider.defaultEndpoint, false,
                    () -> clearProviderModelCache(provider)));
            content.addView(providerModelPanel(provider));
        }
        content.addView(settingsHint("Local suggestions appear instantly. Provider suggestions run once after about 2 seconds idle, with a 5-10 second network window."));

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private View buildEditorSettingsScreen() {
        LinearLayout root = settingsScreenBase("Editor", v -> showSettings());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(12), dp(12), dp(18));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));

        content.addView(settingsSection("Line layout"));
        content.addView(settingsChoiceRow("Side extended editor",
                "Lines keep extending sideways. Horizontal scroll stays enabled.",
                !editorFixedWrap(),
                () -> setEditorFixedWrap(false)));
        content.addView(settingsChoiceRow("Fixed size editor",
                "Long logical lines wrap under themselves when they reach the screen edge.",
                editorFixedWrap(),
                () -> setEditorFixedWrap(true)));
        content.addView(settingsHint("This changes only the editor. Terminal zoom is handled inside the terminal with pinch gestures."));

        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private LinearLayout settingsScreenBase(String titleText, View.OnClickListener backClick) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(SETTINGS_BG);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_menu_24", "drawable", getPackageName()));
        back.setColorFilter(MUTED);
        back.setBackgroundColor(BAR);
        back.setContentDescription("Editor");
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(backClick);
        topBar.addView(back, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, 0, 0);
        topBar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));
        return root;
    }

    private LinearLayout settingsPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(12), dp(10), dp(12), dp(10));
        panel.setBackground(rounded(SETTINGS_PANEL, Color.rgb(80, 74, 68), 1, 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        panel.setLayoutParams(params);
        return panel;
    }

    private TextView settingsSection(String title) {
        TextView text = new TextView(this);
        text.setText(title);
        text.setTextColor(Color.rgb(232, 222, 207));
        text.setTextSize(13);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setPadding(dp(2), dp(8), 0, dp(8));
        return text;
    }

    private TextView settingsTitleBlock(String title, String detail) {
        TextView text = new TextView(this);
        text.setText(title + "\n" + detail);
        text.setTextColor(TEXT);
        text.setTextSize(13);
        text.setLineSpacing(dp(2), 1f);
        return text;
    }

    private View settingsNavRow(String title, String detail, String badge, View.OnClickListener listener) {
        LinearLayout row = settingsPanel();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOnClickListener(listener);

        TextView copy = settingsTitleBlock(title, detail);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = new TextView(this);
        arrow.setText(badge);
        arrow.setTextColor(YELLOW_DIVIDER);
        arrow.setTextSize(22);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(42), dp(44)));
        return row;
    }

    private View settingsChoiceRow(String title, String detail, boolean active, Runnable action) {
        LinearLayout row = settingsPanel();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(rounded(active ? Color.rgb(70, 65, 58) : SETTINGS_PANEL,
                active ? YELLOW_DIVIDER : Color.rgb(80, 74, 68), active ? 2 : 1, 8));
        row.setOnClickListener(v -> action.run());

        TextView copy = settingsTitleBlock(title, detail);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView mark = new TextView(this);
        mark.setText(active ? "on" : "");
        mark.setTextColor(Color.rgb(26, 24, 20));
        mark.setTextSize(11);
        mark.setGravity(Gravity.CENTER);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        mark.setBackground(rounded(active ? YELLOW_DIVIDER : Color.TRANSPARENT, Color.TRANSPARENT, 0, 12));
        LinearLayout.LayoutParams markParams = new LinearLayout.LayoutParams(dp(54), dp(28));
        markParams.setMarginStart(dp(10));
        row.addView(mark, markParams);
        return row;
    }

    private View providerRow(AutocompleteProvider provider) {
        LinearLayout row = settingsPanel();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        String selected = prefs.getString("autocomplete_provider", "local");
        boolean active = provider.id.equals(selected);
        row.setBackground(rounded(active ? Color.rgb(70, 65, 58) : SETTINGS_PANEL,
                active ? YELLOW_DIVIDER : Color.rgb(80, 74, 68), active ? 2 : 1, 8));
        row.setOnClickListener(v -> selectAutocompleteProvider(provider));

        TextView copy = settingsTitleBlock(provider.name, provider.detail);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = new TextView(this);
        badge.setText(provider.autoModel ? "auto" : provider.needsKey ? "key" : "local");
        badge.setTextColor(active ? Color.rgb(26, 24, 20) : Color.rgb(236, 225, 203));
        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setBackground(rounded(active ? YELLOW_DIVIDER : Color.rgb(73, 68, 62), Color.TRANSPARENT, 0, 12));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(54), dp(28));
        badgeParams.setMarginStart(dp(10));
        row.addView(badge, badgeParams);
        return row;
    }

    private void selectAutocompleteProvider(AutocompleteProvider provider) {
        prefs.edit().putString("autocomplete_provider", provider.id).apply();
        ensureProviderDefaults(provider);
        Toast.makeText(this, provider.name + " selected", Toast.LENGTH_SHORT).show();
        setContentView(buildAutocompleteSettingsScreen());
    }

    private View settingsInput(String key, String label, String hint, boolean secret) {
        return settingsInput(key, label, hint, secret, null);
    }

    private View settingsInput(String key, String label, String hint, boolean secret, Runnable onChanged) {
        LinearLayout panel = settingsPanel();
        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(Color.rgb(236, 225, 203));
        title.setTextSize(12);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(title, new LinearLayout.LayoutParams(-1, dp(22)));

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(prefs.getString(key, ""));
        input.setHint(hint);
        input.setHintTextColor(Color.rgb(150, 140, 130));
        input.setTextColor(TEXT);
        input.setTextSize(13);
        input.setPadding(dp(10), 0, dp(10), 0);
        input.setBackground(rounded(SETTINGS_FIELD, Color.rgb(83, 76, 69), 1, 7));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | (secret ? InputType.TYPE_TEXT_VARIATION_PASSWORD : 0));
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                prefs.edit().putString(key, s.toString().trim()).apply();
                if (onChanged != null) onChanged.run();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        panel.addView(input, new LinearLayout.LayoutParams(-1, dp(42)));
        return panel;
    }

    private View aiAgentLoopDelayPanel() {
        LinearLayout panel = settingsPanel();
        TextView copy = settingsTitleBlock("Model loop wait",
                "After a request returns, wait before continuing the agent loop. Default 1 sec.");
        panel.addView(copy, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, 0);
        String selected = normalizedAiAgentLoopDelaySeconds();
        String[] labels = {"0s", "0.5s", "1s", "2s", "5s"};
        String[] values = {"0", "0.5", "1", "2", "5"};
        for (int i = 0; i < labels.length; i++) {
            TextView option = new TextView(this);
            option.setText(labels[i]);
            option.setGravity(Gravity.CENTER);
            option.setTextSize(12);
            option.setTypeface(Typeface.DEFAULT_BOLD);
            boolean active = values[i].equals(selected);
            option.setTextColor(active ? Color.rgb(24, 22, 18) : TEXT);
            option.setBackground(rounded(active ? YELLOW_DIVIDER : SETTINGS_FIELD,
                    active ? Color.TRANSPARENT : Color.rgb(78, 72, 66), active ? 0 : 1, 14));
            final String value = values[i];
            option.setOnClickListener(v -> {
                prefs.edit().putString("ai_agent_loop_delay_seconds", value).apply();
                setContentView(buildAutocompleteSettingsScreen());
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(30), 1);
            if (i > 0) params.setMarginStart(dp(6));
            row.addView(option, params);
        }
        panel.addView(row, new LinearLayout.LayoutParams(-1, -2));
        return panel;
    }

    private String normalizedAiAgentLoopDelaySeconds() {
        String raw = prefs.getString("ai_agent_loop_delay_seconds", "1");
        if (raw == null) return "1";
        raw = raw.trim();
        if (raw.equals("0") || raw.equals("0.5") || raw.equals("1") || raw.equals("2") || raw.equals("5")) return raw;
        return "1";
    }

    private long aiAgentLoopDelayMs() {
        String raw = normalizedAiAgentLoopDelaySeconds();
        try {
            double seconds = Double.parseDouble(raw);
            return Math.max(0L, Math.min(5000L, Math.round(seconds * 1000.0)));
        } catch (NumberFormatException ignored) {
            return 1000L;
        }
    }

    private void waitBeforeNextAiAgentLoop(int cycle) {
        if (cycle >= AI_AGENT_MAX_CYCLES - 1) return;
        long delayMs = aiAgentLoopDelayMs();
        if (delayMs <= 0L) return;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private TextView settingsHint(String textValue) {
        TextView text = new TextView(this);
        text.setText(textValue);
        text.setTextColor(Color.rgb(206, 194, 178));
        text.setTextSize(12);
        text.setPadding(dp(4), dp(6), dp(4), dp(8));
        return text;
    }

    private AutocompleteProvider currentProvider() {
        String selected = prefs.getString("autocomplete_provider", "local");
        for (AutocompleteProvider provider : AUTOCOMPLETE_PROVIDERS) {
            if (provider.id.equals(selected)) return provider;
        }
        return AUTOCOMPLETE_PROVIDERS[0];
    }

    private String providerPrefKey(AutocompleteProvider provider, String field) {
        return "autocomplete_" + provider.id + "_" + field;
    }

    private void ensureProviderDefaults(AutocompleteProvider provider) {
        SharedPreferences.Editor editorPrefs = prefs.edit();
        if (!provider.defaultEndpoint.isEmpty()
                && prefs.getString(providerPrefKey(provider, "endpoint"), "").trim().isEmpty()) {
            editorPrefs.putString(providerPrefKey(provider, "endpoint"), provider.defaultEndpoint);
        }
        if (!provider.defaultModel.isEmpty()
                && prefs.getString(providerPrefKey(provider, "model"), "").trim().isEmpty()) {
            editorPrefs.putString(providerPrefKey(provider, "model"), provider.defaultModel);
        }
        editorPrefs.apply();
    }

    private String providerEndpoint(AutocompleteProvider provider) {
        ensureProviderDefaults(provider);
        return prefs.getString(providerPrefKey(provider, "endpoint"), provider.defaultEndpoint).trim();
    }

    private String providerApiKey(AutocompleteProvider provider) {
        return prefs.getString(providerPrefKey(provider, "api_key"), "").trim();
    }

    private String providerModel(AutocompleteProvider provider) {
        ensureProviderDefaults(provider);
        String model = prefs.getString(providerPrefKey(provider, "model"), provider.defaultModel).trim();
        return model.isEmpty() ? provider.defaultModel : model;
    }

    private View providerModelPanel(AutocompleteProvider provider) {
        LinearLayout panel = settingsPanel();

        TextView title = new TextView(this);
        title.setText("Model");
        title.setTextColor(Color.rgb(236, 225, 203));
        title.setTextSize(12);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(title, new LinearLayout.LayoutParams(-1, dp(22)));

        TextView current = new TextView(this);
        current.setText(providerModel(provider));
        current.setTextColor(TEXT);
        current.setTextSize(13);
        current.setSingleLine(false);
        current.setPadding(dp(10), dp(8), dp(10), dp(8));
        current.setBackground(rounded(SETTINGS_FIELD, Color.rgb(83, 76, 69), 1, 7));
        panel.addView(current, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(9), 0, 0);

        Button refresh = settingsButton("Fetch models");
        refresh.setEnabled(provider.canFetchModels);
        refresh.setOnClickListener(v -> fetchProviderModels(provider, true));
        actions.addView(refresh, new LinearLayout.LayoutParams(0, dp(38), 1));

        Button select = settingsButton("Select");
        select.setOnClickListener(v -> showProviderModelMenu(v, provider));
        LinearLayout.LayoutParams selectParams = new LinearLayout.LayoutParams(0, dp(38), 1);
        selectParams.setMarginStart(dp(8));
        actions.addView(select, selectParams);
        panel.addView(actions, new LinearLayout.LayoutParams(-1, -2));

        TextView status = new TextView(this);
        String statusText = provider.canFetchModels
                ? providerModels(provider).size() + " cached models"
                : "This provider does not expose a model list here";
        status.setText(statusText);
        status.setTextColor(Color.rgb(206, 194, 178));
        status.setTextSize(12);
        status.setPadding(dp(2), dp(8), dp(2), 0);
        panel.addView(status, new LinearLayout.LayoutParams(-1, -2));
        return panel;
    }

    private Button settingsButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.rgb(26, 24, 20));
        button.setTextSize(12);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(rounded(YELLOW_DIVIDER, Color.TRANSPARENT, 0, 7));
        return button;
    }

    private void showProviderModelMenu(View anchor, AutocompleteProvider provider) {
        ArrayList<String> models = providerModels(provider);
        if (models.isEmpty() && !provider.defaultModel.isEmpty()) models.add(provider.defaultModel);
        PopupMenu menu = new PopupMenu(this, anchor);
        for (String model : models) menu.getMenu().add(model);
        menu.setOnMenuItemClickListener(item -> {
            prefs.edit().putString(providerPrefKey(provider, "model"), item.getTitle().toString()).apply();
            setContentView(buildAutocompleteSettingsScreen());
            return true;
        });
        menu.show();
    }

    private ArrayList<String> providerModels(AutocompleteProvider provider) {
        ArrayList<String> models = new ArrayList<>();
        String cached = prefs.getString(providerPrefKey(provider, "models"), "");
        for (String line : cached.split("\\n")) {
            String model = line.trim();
            if (!model.isEmpty() && !models.contains(model)) models.add(model);
        }
        return models;
    }

    private void clearProviderModelCache(AutocompleteProvider provider) {
        prefs.edit().remove(providerPrefKey(provider, "models")).apply();
    }

    private void maybeAutoFetchProviderModels(AutocompleteProvider provider) {
        if (!provider.canFetchModels || modelFetchInFlight) return;
        if (provider.needsKey && providerApiKey(provider).length() < 12) return;
        int token = ++modelFetchToken;
        View root = getWindow().getDecorView();
        if (modelFetchRunnable != null) root.removeCallbacks(modelFetchRunnable);
        modelFetchRunnable = () -> {
            if (token == modelFetchToken) fetchProviderModels(provider, false);
        };
        root.postDelayed(modelFetchRunnable, 650);
    }

    private void fetchProviderModels(AutocompleteProvider provider, boolean showToast) {
        if (!provider.canFetchModels) {
            if (showToast) Toast.makeText(this, "No model list for " + provider.name, Toast.LENGTH_SHORT).show();
            return;
        }
        String apiKey = providerApiKey(provider);
        if (provider.needsKey && apiKey.isEmpty()) {
            Toast.makeText(this, "Add " + provider.name + " API key first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (modelFetchInFlight) return;
        int token = ++modelFetchToken;
        modelFetchInFlight = true;
        if (showToast) Toast.makeText(this, "Fetching " + provider.name + " models", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            ArrayList<String> models = new ArrayList<>();
            String error = "";
            try {
                models = requestProviderModels(provider, apiKey);
            } catch (Exception failure) {
                error = failure.getClass().getSimpleName() + ": " + failure.getMessage();
                Log.d(TAG_AI, "model fetch failed provider=" + provider.id + " " + safeAiDiagnostic(error, 140));
            }
            final ArrayList<String> finalModels = models;
            final String finalError = error;
            runOnUiThread(() -> {
                if (token != modelFetchToken) return;
                modelFetchInFlight = false;
                if (finalModels.isEmpty()) {
                    if (showToast || settingsVisible) {
                        Toast.makeText(MainActivity.this,
                                finalError.isEmpty() ? "No models returned" : "Model fetch failed: " + shortAiError(finalError),
                                Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                StringBuilder cache = new StringBuilder();
                for (String model : finalModels) {
                    if (cache.length() > 0) cache.append('\n');
                    cache.append(model);
                }
                SharedPreferences.Editor editorPrefs = prefs.edit()
                        .putString(providerPrefKey(provider, "models"), cache.toString());
                String selected = providerModel(provider);
                if (provider.autoModel || selected.isEmpty() || provider.defaultModel.equals(selected) || !finalModels.contains(selected)) {
                    editorPrefs.putString(providerPrefKey(provider, "model"), finalModels.get(0));
                }
                editorPrefs.apply();
                if (settingsVisible && currentProvider().id.equals(provider.id)) {
                    setContentView(buildAutocompleteSettingsScreen());
                }
                Toast.makeText(MainActivity.this, finalModels.size() + " models loaded", Toast.LENGTH_SHORT).show();
            });
        }, "andropy-model-fetch").start();
    }

    private ArrayList<String> requestProviderModels(AutocompleteProvider provider, String apiKey) throws Exception {
        String endpoint = providerModelEndpoint(provider);
        if (endpoint.isEmpty()) return new ArrayList<>();
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(AI_COMPLETION_TIMEOUT_MS);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        if (!apiKey.isEmpty()) {
            if ("anthropic".equals(provider.id)) {
                connection.setRequestProperty("x-api-key", apiKey);
                connection.setRequestProperty("anthropic-version", "2023-06-01");
            } else if (!"gemini".equals(provider.id)) {
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            }
        }
        int codeStatus = connection.getResponseCode();
        InputStream stream = codeStatus >= 200 && codeStatus < 300 ? connection.getInputStream() : connection.getErrorStream();
        String response = readStreamText(stream);
        connection.disconnect();
        if (codeStatus < 200 || codeStatus >= 300) {
            throw new IOException("HTTP " + codeStatus + " " + safeAiDiagnostic(response, 180));
        }
        return parseProviderModels(provider, response);
    }

    private String providerModelEndpoint(AutocompleteProvider provider) throws IOException {
        if ("gemini".equals(provider.id)) {
            String key = providerApiKey(provider);
            if (key.isEmpty()) return "";
            return "https://generativelanguage.googleapis.com/v1beta/models?key="
                    + URLEncoder.encode(key, "UTF-8");
        }
        if ("ollama".equals(provider.id)) return replaceEndpointPath(providerEndpoint(provider), "/api/tags");
        if ("koboldcpp".equals(provider.id)) return "";
        String endpoint = providerEndpoint(provider);
        if (endpoint.isEmpty()) return "";
        if ("anthropic".equals(provider.id)) return "https://api.anthropic.com/v1/models";
        if ("cohere".equals(provider.id)) return "https://api.cohere.com/v1/models";
        if (endpoint.endsWith("/chat/completions")) return endpoint.substring(0, endpoint.length() - "/chat/completions".length()) + "/models";
        if (endpoint.endsWith("/completions")) return endpoint.substring(0, endpoint.length() - "/completions".length()) + "/models";
        if (endpoint.endsWith("/generate")) return endpoint.substring(0, endpoint.length() - "/generate".length()) + "/models";
        if (endpoint.endsWith("/models")) return endpoint;
        return endpoint.replaceAll("/+$", "") + "/models";
    }

    private String replaceEndpointPath(String endpoint, String newPath) throws IOException {
        URL url = new URL(endpoint);
        return url.getProtocol() + "://" + url.getAuthority() + newPath;
    }

    private ArrayList<String> parseProviderModels(AutocompleteProvider provider, String response) throws Exception {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        JSONObject json = new JSONObject(response);
        JSONArray data = json.optJSONArray("data");
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                Object item = data.opt(i);
                String id = item instanceof JSONObject ? ((JSONObject) item).optString("id", "") : String.valueOf(item);
                addModelIfUseful(result, provider, id);
            }
        }
        JSONArray models = json.optJSONArray("models");
        if (models != null) {
            for (int i = 0; i < models.length(); i++) {
                JSONObject item = models.optJSONObject(i);
                if (item == null) {
                    addModelIfUseful(result, provider, models.optString(i, ""));
                    continue;
                }
                if ("gemini".equals(provider.id) && !geminiSupportsTextGeneration(item)) continue;
                String id = item.optString("name", item.optString("id", ""));
                addModelIfUseful(result, provider, id);
            }
        }
        ArrayList<String> list = new ArrayList<>(result);
        list.sort((left, right) -> modelRank(provider, left) - modelRank(provider, right));
        return list;
    }

    private void addModelIfUseful(LinkedHashSet<String> models, AutocompleteProvider provider, String id) {
        if (id == null) return;
        String model = id.trim();
        if (model.isEmpty()) return;
        String lower = model.toLowerCase(Locale.US);
        if (lower.contains("compound") || lower.contains("whisper") || lower.contains("tts")
                || lower.contains("audio") || lower.contains("speech") || lower.contains("orpheus")
                || lower.contains("image") || lower.contains("video") || lower.contains("veo")
                || lower.contains("lyria") || lower.contains("banana") || lower.contains("deep-research")
                || lower.contains("embedding") || lower.contains("embed") || lower.contains("guard")) return;
        models.add(model.startsWith("models/") ? model.substring("models/".length()) : model);
    }

    private boolean geminiSupportsTextGeneration(JSONObject item) {
        JSONArray methods = item.optJSONArray("supportedGenerationMethods");
        if (methods == null || methods.length() == 0) return true;
        for (int i = 0; i < methods.length(); i++) {
            if ("generateContent".equals(methods.optString(i))) return true;
        }
        return false;
    }

    private int modelRank(AutocompleteProvider provider, String model) {
        String lower = model.toLowerCase(Locale.US);
        int rank = 50;
        if (lower.contains("coder") || lower.contains("codestral") || lower.contains("code")) rank -= 20;
        if (lower.contains("llama") || lower.contains("qwen") || lower.contains("gemma") || lower.contains("mistral")) rank -= 10;
        if (lower.contains("70b") || lower.contains("large") || lower.contains("pro")) rank += 20;
        if (lower.contains("preview") || lower.contains("experimental")) rank += 8;
        if (model.equals(provider.defaultModel)) rank -= 30;
        return rank;
    }

    private View buildPipScreen() {
        terminalVisible = false;
        stopTerminal();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(31, 35, 41));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_menu_24", "drawable", getPackageName()));
        back.setColorFilter(MUTED);
        back.setBackgroundColor(BAR);
        back.setContentDescription("Editor");
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(v -> showEditor());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView title = new TextView(this);
        title.setText("Pip");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, 0, 0);
        topBar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        ImageButton rescan = new ImageButton(this);
        rescan.setImageResource(getResources().getIdentifier("ic_refresh_24", "drawable", getPackageName()));
        rescan.setColorFilter(Color.rgb(220, 228, 238));
        rescan.setBackgroundColor(BAR);
        rescan.setContentDescription("Hard rescan");
        rescan.setPadding(dp(7), dp(7), dp(7), dp(7));
        rescan.setOnClickListener(v -> hardRefreshPipPackages());
        topBar.addView(rescan, new LinearLayout.LayoutParams(dp(38), dp(38)));
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

        pipScanProgressTrack = new FrameLayout(this);
        pipScanProgressTrack.setBackgroundColor(Color.rgb(25, 29, 35));
        pipScanProgressTrack.setVisibility(View.GONE);
        pipScanProgressFill = new View(this);
        pipScanProgressFill.setBackgroundColor(ACCENT);
        ((FrameLayout) pipScanProgressTrack).addView(pipScanProgressFill, new FrameLayout.LayoutParams(0, dp(4)));
        root.addView(pipScanProgressTrack, new LinearLayout.LayoutParams(-1, dp(4)));

        LinearLayout installPanel = new LinearLayout(this);
        installPanel.setOrientation(LinearLayout.VERTICAL);
        installPanel.setPadding(dp(12), dp(10), dp(12), dp(10));
        installPanel.setBackgroundColor(Color.rgb(39, 44, 52));

        LinearLayout inputRow = new LinearLayout(this);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        pipInput = new EditText(this);
        pipInput.setSingleLine(true);
        pipInput.setTextColor(TEXT);
        pipInput.setHintTextColor(Color.rgb(148, 158, 172));
        pipInput.setTextSize(14);
        pipInput.setHint("package name");
        pipInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        pipInput.setPadding(dp(12), 0, dp(12), 0);
        pipInput.setBackground(rounded(Color.rgb(28, 32, 38), Color.rgb(82, 92, 106), 1, 7));
        inputRow.addView(pipInput, new LinearLayout.LayoutParams(0, dp(42), 1));

        pipInstallButton = new Button(this);
        pipInstallButton.setText("Install");
        pipInstallButton.setTextSize(13);
        pipInstallButton.setAllCaps(false);
        pipInstallButton.setTextColor(Color.rgb(14, 19, 24));
        pipInstallButton.setBackground(rounded(ACCENT, ACCENT, 0, 7));
        pipInstallButton.setOnClickListener(v -> installPipPackage());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(dp(86), dp(42));
        buttonParams.setMarginStart(dp(8));
        inputRow.addView(pipInstallButton, buttonParams);
        installPanel.addView(inputRow, new LinearLayout.LayoutParams(-1, dp(42)));

        pipStatusText = new TextView(this);
        pipStatusText.setText("Ready");
        pipStatusText.setTextColor(MUTED);
        pipStatusText.setTextSize(12);
        pipStatusText.setSingleLine(true);
        pipStatusText.setGravity(Gravity.CENTER_VERTICAL);
        installPanel.addView(pipStatusText, new LinearLayout.LayoutParams(-1, dp(28)));

        pipOutputText = new TextView(this);
        pipOutputText.setTextColor(Color.rgb(210, 218, 228));
        pipOutputText.setTextSize(11);
        pipOutputText.setTypeface(Typeface.MONOSPACE);
        pipOutputText.setGravity(Gravity.BOTTOM | Gravity.START);
        pipOutputText.setPadding(dp(10), dp(6), dp(10), dp(6));
        pipOutputText.setBackground(rounded(Color.rgb(19, 22, 27), Color.rgb(49, 56, 66), 1, 6));
        installPanel.addView(pipOutputText, new LinearLayout.LayoutParams(-1, dp(94)));
        root.addView(installPanel, new LinearLayout.LayoutParams(-1, dp(184)));

        TextView installedLabel = new TextView(this);
        installedLabel.setText("Installed modules");
        installedLabel.setTextColor(TEXT);
        installedLabel.setTextSize(13);
        installedLabel.setTypeface(Typeface.DEFAULT_BOLD);
        installedLabel.setGravity(Gravity.CENTER_VERTICAL);
        installedLabel.setPadding(dp(12), 0, dp(12), 0);
        root.addView(installedLabel, new LinearLayout.LayoutParams(-1, dp(38)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        pipPackageList = new LinearLayout(this);
        pipPackageList.setOrientation(LinearLayout.VERTICAL);
        pipPackageList.setPadding(dp(10), 0, dp(10), dp(12));
        scroll.addView(pipPackageList, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        return root;
    }

    private void installPipPackage() {
        String spec = pipInput == null ? "" : pipInput.getText().toString().trim();
        if (spec.isEmpty()) {
            Toast.makeText(this, "Package name required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pipInstallButton != null) pipInstallButton.setEnabled(false);
        if (pipStatusText != null) pipStatusText.setText("Installing " + spec);
        synchronized (pipOutput) {
            pipOutput.setLength(0);
        }
        appendPipOutput("$ pip install " + spec);

        new Thread(() -> {
            int exitCode = runPipInstall(spec);
            runOnUiThread(() -> {
                if (pipInstallButton != null) pipInstallButton.setEnabled(true);
                if (pipStatusText != null) {
                    pipStatusText.setText(exitCode == 0 ? "Installed " + spec : "Install failed: exit " + exitCode);
                }
                refreshPipPackages();
            });
        }, "andropy-pip-install").start();
    }

    private int runPipInstall(String spec) {
        ensureProjectRoots();
        List<String> command = new ArrayList<>();
        command.add(new File(binRoot, "python").getAbsolutePath());
        command.add("-m");
        command.add("pip");
        command.add("install");
        command.add("--disable-pip-version-check");
        command.add("--no-cache-dir");
        for (String part : spec.split("\\s+")) {
            if (!part.isEmpty()) command.add(part);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        applyRuntimeEnvironment(builder);
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) appendPipOutput(line);
            }
            int exitCode = process.waitFor();
            appendPipOutput("[pip exit " + exitCode + "]");
            return exitCode;
        } catch (IOException | InterruptedException error) {
            appendPipOutput(error.getClass().getSimpleName() + ": " + error.getMessage());
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            return 1;
        }
    }

    private void refreshPipPackages() {
        if (pipPackageList == null) return;
        int generation = ++pipScanGeneration;
        hidePipScanProgress();
        pipPackageList.removeAllViews();
        pipPackageList.addView(packageMessage("Scanning installed modules..."));
        if (pipStatusText != null && pipStatusText.getText().toString().equals("Ready")) {
            pipStatusText.setText("Scanning installed modules");
        }
        new Thread(() -> {
            List<PipPackageInfo> packages = loadInstalledPipPackages();
            runOnUiThread(() -> {
                if (generation == pipScanGeneration) renderPipPackages(packages);
            });
        }, "andropy-pip-list").start();
    }

    private void hardRefreshPipPackages() {
        if (pipPackageList == null) return;
        int generation = ++pipScanGeneration;
        pipPackageList.removeAllViews();
        pipPackageList.addView(packageMessage("Hard rescanning installed modules..."));
        if (pipStatusText != null) pipStatusText.setText("Hard rescan starting");
        showPipScanProgress(0f);
        new Thread(() -> {
            List<PipPackageInfo> packages = loadInstalledPipPackagesHard(generation);
            runOnUiThread(() -> {
                if (generation != pipScanGeneration) return;
                showPipScanProgress(1f);
                renderPipPackages(packages);
                hidePipScanProgressDelayed();
            });
        }, "andropy-pip-hard-scan").start();
    }

    private void showPipScanProgress(float progress) {
        if (pipScanProgressTrack == null || pipScanProgressFill == null) return;
        pipScanProgressTrack.setVisibility(View.VISIBLE);
        pipScanProgressTrack.post(() -> {
            ViewGroup.LayoutParams params = pipScanProgressFill.getLayoutParams();
            params.width = Math.max(dp(2), (int) (pipScanProgressTrack.getWidth() * Math.max(0f, Math.min(1f, progress))));
            pipScanProgressFill.setLayoutParams(params);
        });
    }

    private void hidePipScanProgress() {
        if (pipScanProgressTrack == null || pipScanProgressFill == null) return;
        pipScanProgressTrack.setVisibility(View.GONE);
        ViewGroup.LayoutParams params = pipScanProgressFill.getLayoutParams();
        params.width = 0;
        pipScanProgressFill.setLayoutParams(params);
    }

    private void hidePipScanProgressDelayed() {
        if (pipScanProgressTrack != null) pipScanProgressTrack.postDelayed(this::hidePipScanProgress, 700);
    }

    private List<PipPackageInfo> loadInstalledPipPackages() {
        ensureProjectRoots();
        List<PipPackageInfo> packages = new ArrayList<>();
        File sitePackages = new File(prefixRealRoot, "lib/python3.13/site-packages");
        File[] infos = sitePackages.listFiles(file -> file.isDirectory()
                && (file.getName().endsWith(".dist-info") || file.getName().endsWith(".egg-info")));
        if (infos == null) return packages;
        Arrays.sort(infos, Comparator.comparing(file -> file.getName().toLowerCase(Locale.US)));
        Set<String> seen = new HashSet<>();
        for (File infoDir : infos) {
            PipPackageInfo item = readPipPackageInfo(sitePackages, infoDir);
            if (item == null || item.name.isEmpty()) continue;
            String key = item.name.toLowerCase(Locale.US);
            if (seen.add(key)) packages.add(item);
        }
        packages.sort(Comparator.comparing(item -> item.name.toLowerCase(Locale.US)));
        return packages;
    }

    private List<PipPackageInfo> loadInstalledPipPackagesHard(int generation) {
        ensureProjectRoots();
        List<PipPackageInfo> packages = new ArrayList<>();
        File python = new File(binRoot, "python");
        if (!python.isFile()) return packages;
        String script = ""
                + "import importlib.metadata as md, os, sys\n"
                + "def clean(value):\n"
                + "    return (value or '').replace('\\t', ' ').replace('\\n', ' ').strip()\n"
                + "dists = sorted(md.distributions(), key=lambda d: (d.metadata.get('Name') or '').lower())\n"
                + "print('__COUNT__\\t%d' % len(dists), flush=True)\n"
                + "for index, dist in enumerate(dists, 1):\n"
                + "    name = clean(dist.metadata.get('Name'))\n"
                + "    if not name:\n"
                + "        continue\n"
                + "    version = clean(dist.version)\n"
                + "    summary = clean(dist.metadata.get('Summary'))\n"
                + "    size = 0\n"
                + "    for item in (dist.files or []):\n"
                + "        try:\n"
                + "            path = dist.locate_file(item)\n"
                + "            if os.path.isfile(path):\n"
                + "                size += os.path.getsize(path)\n"
                + "        except Exception:\n"
                + "            pass\n"
                + "    print('__PKG__\\t%d\\t%s\\t%s\\t%d\\t%s' % (index, name, version, size, summary), flush=True)\n";
        ProcessBuilder builder = new ProcessBuilder(python.getAbsolutePath(), "-c", script);
        builder.redirectErrorStream(true);
        applyRuntimeEnvironment(builder);
        int total = 1;
        Set<String> seen = new HashSet<>();
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (generation != pipScanGeneration) {
                        process.destroy();
                        return packages;
                    }
                    if (line.startsWith("__COUNT__\t")) {
                        try {
                            total = Math.max(1, Integer.parseInt(line.substring("__COUNT__\t".length()).trim()));
                        } catch (NumberFormatException ignored) {
                        }
                        int countTotal = total;
                        runOnUiThread(() -> {
                            if (generation != pipScanGeneration) return;
                            showPipScanProgress(0f);
                            if (pipStatusText != null) pipStatusText.setText("Hard rescanning 0 / " + countTotal);
                        });
                        continue;
                    }
                    if (!line.startsWith("__PKG__\t")) continue;
                    String[] parts = line.split("\\t", 6);
                    if (parts.length < 5) continue;
                    int current = 0;
                    long size = 0;
                    try {
                        current = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {
                    }
                    try {
                        size = Long.parseLong(parts[4]);
                    } catch (NumberFormatException ignored) {
                    }
                    String name = cleanPipMetadata(parts[2]);
                    if (!name.isEmpty() && seen.add(name.toLowerCase(Locale.US))) {
                        String summary = parts.length == 6 ? cleanPipMetadata(parts[5]) : "";
                        packages.add(new PipPackageInfo(name, cleanPipMetadata(parts[3]), size, summary));
                    }
                    int progressCurrent = Math.max(1, current);
                    int progressTotal = total;
                    runOnUiThread(() -> {
                        if (generation != pipScanGeneration) return;
                        showPipScanProgress(progressCurrent / (float) Math.max(1, progressTotal));
                        if (pipStatusText != null) {
                            pipStatusText.setText("Hard rescanning " + progressCurrent + " / " + progressTotal);
                        }
                    });
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                runOnUiThread(() -> {
                    if (generation == pipScanGeneration && pipStatusText != null) {
                        pipStatusText.setText("Hard rescan failed: python exit " + exitCode);
                    }
                });
            }
        } catch (IOException | InterruptedException error) {
            if (error instanceof InterruptedException) Thread.currentThread().interrupt();
            runOnUiThread(() -> {
                if (generation == pipScanGeneration && pipStatusText != null) {
                    pipStatusText.setText("Hard rescan failed: " + error.getClass().getSimpleName());
                }
            });
        }
        packages.sort(Comparator.comparing(item -> item.name.toLowerCase(Locale.US)));
        return packages;
    }

    private PipPackageInfo readPipPackageInfo(File sitePackages, File infoDir) {
        String fallback = infoDir.getName().replaceFirst("\\.(dist|egg)-info$", "");
        String name = "";
        String version = "";
        String summary = "";
        File metadata = new File(infoDir, "METADATA");
        if (!metadata.isFile()) metadata = new File(infoDir, "PKG-INFO");
        if (metadata.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadata), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0) break;
                    int colon = line.indexOf(':');
                    if (colon <= 0) continue;
                    String key = line.substring(0, colon).trim();
                    String value = cleanPipMetadata(line.substring(colon + 1));
                    if ("Name".equalsIgnoreCase(key)) name = value;
                    else if ("Version".equalsIgnoreCase(key)) version = value;
                    else if ("Summary".equalsIgnoreCase(key)) summary = value;
                    if (!name.isEmpty() && !version.isEmpty() && !summary.isEmpty()) break;
                }
            } catch (IOException ignored) {
            }
        }
        if (name.isEmpty()) name = fallbackNameFromInfoDir(fallback);
        if (version.isEmpty()) version = fallbackVersionFromInfoDir(fallback, name);
        long size = quickPipPackageSize(sitePackages, infoDir, name);
        return new PipPackageInfo(name, version, size, summary);
    }

    private PipPackageInfo readPipPackageInfoHard(File sitePackages, File infoDir) {
        PipPackageInfo item = readPipPackageInfo(sitePackages, infoDir);
        if (item == null) return null;
        long size = exactPipPackageSize(sitePackages, infoDir, item.name);
        return new PipPackageInfo(item.name, item.version, size, item.summary);
    }

    private String cleanPipMetadata(String value) {
        if (value == null) return "";
        return value.replace('\t', ' ').replace('\n', ' ').trim();
    }

    private String fallbackNameFromInfoDir(String stem) {
        int split = stem.lastIndexOf('-');
        return split > 0 ? stem.substring(0, split) : stem;
    }

    private String fallbackVersionFromInfoDir(String stem, String name) {
        if (name == null || name.isEmpty() || stem.length() <= name.length() + 1) return "";
        if (stem.regionMatches(true, 0, name, 0, name.length()) && stem.charAt(name.length()) == '-') {
            return stem.substring(name.length() + 1);
        }
        return "";
    }

    private long quickPipPackageSize(File sitePackages, File infoDir, String packageName) {
        long size = boundedDirectorySize(infoDir, 512, 20);
        String normalized = packageName.replace('-', '_');
        File moduleDir = new File(sitePackages, normalized);
        if (!moduleDir.exists()) moduleDir = new File(sitePackages, normalized.toLowerCase(Locale.US));
        if (moduleDir.exists()) size += boundedDirectorySize(moduleDir, 1800, 45);
        File moduleFile = new File(sitePackages, normalized + ".py");
        if (moduleFile.isFile()) size += moduleFile.length();
        return size;
    }

    private long exactPipPackageSize(File sitePackages, File infoDir, String packageName) {
        long size = directorySize(infoDir);
        String normalized = packageName.replace('-', '_');
        File moduleDir = new File(sitePackages, normalized);
        if (!moduleDir.exists()) moduleDir = new File(sitePackages, normalized.toLowerCase(Locale.US));
        if (moduleDir.exists()) size += directorySize(moduleDir);
        File moduleFile = new File(sitePackages, normalized + ".py");
        if (moduleFile.isFile()) size += moduleFile.length();
        return size;
    }

    private long directorySize(File root) {
        long size = 0;
        ArrayList<File> stack = new ArrayList<>();
        if (root != null && root.exists()) stack.add(root);
        while (!stack.isEmpty()) {
            File current = stack.remove(stack.size() - 1);
            if (current.isFile()) {
                size += current.length();
            } else if (current.isDirectory()) {
                File[] children = current.listFiles();
                if (children == null) continue;
                for (File child : children) stack.add(child);
            }
        }
        return size;
    }

    private long boundedDirectorySize(File root, int maxFiles, long maxMillis) {
        long deadline = SystemClock.uptimeMillis() + maxMillis;
        long size = 0;
        int files = 0;
        ArrayList<File> stack = new ArrayList<>();
        if (root != null && root.exists()) stack.add(root);
        while (!stack.isEmpty() && files < maxFiles && SystemClock.uptimeMillis() <= deadline) {
            File current = stack.remove(stack.size() - 1);
            if (current.isFile()) {
                size += current.length();
                files++;
            } else if (current.isDirectory()) {
                File[] children = current.listFiles();
                if (children == null) continue;
                for (File child : children) stack.add(child);
            }
        }
        return size;
    }

    private void renderPipPackages(List<PipPackageInfo> packages) {
        if (pipPackageList == null) return;
        pipPackageList.removeAllViews();
        if (packages.isEmpty()) {
            pipPackageList.addView(packageMessage("No pip modules installed"));
        } else {
            for (PipPackageInfo item : packages) {
                pipPackageList.addView(packageRow(item));
            }
        }
        if (pipStatusText != null && !pipStatusText.getText().toString().startsWith("Install failed")) {
            pipStatusText.setText(packages.size() + " installed module" + (packages.size() == 1 ? "" : "s"));
        }
    }

    private View packageMessage(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(MUTED);
        text.setTextSize(13);
        text.setGravity(Gravity.CENTER);
        text.setPadding(dp(12), dp(28), dp(12), dp(28));
        return text;
    }

    private View packageRow(PipPackageInfo item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(rounded(Color.rgb(42, 47, 55), Color.rgb(64, 72, 84), 1, 8));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(this);
        name.setText(item.name + "  " + item.version);
        name.setTextColor(TEXT);
        name.setTextSize(14);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setSingleLine(true);
        titleRow.addView(name, new LinearLayout.LayoutParams(0, dp(24), 1));

        TextView size = new TextView(this);
        size.setText(formatBytes(item.sizeBytes));
        size.setTextColor(Color.rgb(255, 220, 105));
        size.setTextSize(12);
        size.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        size.setSingleLine(true);
        titleRow.addView(size, new LinearLayout.LayoutParams(dp(86), dp(24)));
        row.addView(titleRow, new LinearLayout.LayoutParams(-1, dp(24)));

        TextView summary = new TextView(this);
        summary.setText(item.summary == null || item.summary.isEmpty() ? "No PyPI description available" : item.summary);
        summary.setTextColor(MUTED);
        summary.setTextSize(12);
        summary.setMaxLines(3);
        summary.setPadding(0, dp(4), 0, 0);
        row.addView(summary, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(8));
        row.setLayoutParams(params);
        return row;
    }

    private String fetchPyPiSummary(String packageName) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://pypi.org/pypi/" + packageName + "/json");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                return new JSONObject(body.toString()).getJSONObject("info").optString("summary", "").trim();
            }
        } catch (Exception ignored) {
            return "";
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void appendPipOutput(String line) {
        runOnUiThread(() -> {
            synchronized (pipOutput) {
                if (pipOutput.length() > 0) pipOutput.append('\n');
                pipOutput.append(line);
                String[] lines = pipOutput.toString().split("\\n");
                int start = Math.max(0, lines.length - 6);
                StringBuilder visible = new StringBuilder();
                for (int i = start; i < lines.length; i++) {
                    if (visible.length() > 0) visible.append('\n');
                    visible.append(lines[i]);
                }
                if (pipOutputText != null) pipOutputText.setText(visible.toString());
            }
        });
    }

    private String opencamSocketName() {
        return "andropy_opencam_" + getPackageName();
    }

    private void startOpencamBridge() {
        if (opencamBridgeRunning) return;
        opencamBridgeRunning = true;
        opencamServerThread = new Thread(() -> {
            try {
                opencamServerSocket = new LocalServerSocket(opencamSocketName());
                while (opencamBridgeRunning) {
                    LocalSocket socket = opencamServerSocket.accept();
                    handleOpencamClient(socket);
                }
            } catch (IOException error) {
                if (opencamBridgeRunning) Log.w("AndroPyOpenCam", "bridge stopped", error);
            }
        }, "AndroPy-opencam-bridge");
        opencamServerThread.start();
    }

    private void stopOpencamBridge() {
        opencamBridgeRunning = false;
        try {
            if (opencamServerSocket != null) opencamServerSocket.close();
        } catch (IOException ignored) {
        }
        opencamServerSocket = null;
        opencamServerThread = null;
    }

    private String aquaDisplaySocketName() {
        return "andropy_display_" + getPackageName();
    }

    private void startAquaDisplayBridge() {
        if (aquaDisplayBridgeRunning) return;
        aquaDisplayBridgeRunning = true;
        aquaDisplayServerThread = new Thread(() -> {
            try {
                aquaDisplayServerSocket = new LocalServerSocket(aquaDisplaySocketName());
                while (aquaDisplayBridgeRunning) {
                    LocalSocket socket = aquaDisplayServerSocket.accept();
                    handleAquaDisplayClient(socket);
                }
            } catch (IOException error) {
                if (aquaDisplayBridgeRunning) Log.w("AndroPyDisplay", "bridge stopped", error);
            }
        }, "AndroPy-display-bridge");
        aquaDisplayServerThread.start();
    }

    private void stopAquaDisplayBridge() {
        aquaDisplayBridgeRunning = false;
        try {
            if (aquaDisplayServerSocket != null) aquaDisplayServerSocket.close();
        } catch (IOException ignored) {
        }
        aquaDisplayServerSocket = null;
        aquaDisplayServerThread = null;
    }

    private void handleAquaDisplayClient(LocalSocket socket) {
        try (LocalSocket ignored = socket;
             InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream()) {
            String header = readSocketLine(input);
            if (header == null) {
                output.write("ERR empty-command\n".getBytes(StandardCharsets.UTF_8));
                output.flush();
                return;
            }
            String[] parts = header.trim().split("\\s+", 5);
            String command = parts.length == 0 ? "" : parts[0].toUpperCase(Locale.US);
            if ("PING".equals(command)) {
                output.write(("OK display socket=" + aquaDisplaySocketName() + "\n").getBytes(StandardCharsets.UTF_8));
            } else if ("CLOSE".equals(command)) {
                runOnUiThread(this::showEditor);
                output.write("OK close\n".getBytes(StandardCharsets.UTF_8));
            } else if ("FRAME".equals(command)) {
                handleAquaDisplayFrame(parts, input, output);
            } else {
                output.write(("ERR unknown display command: " + header + "\n").getBytes(StandardCharsets.UTF_8));
            }
            output.flush();
        } catch (IOException ignored) {
        }
    }

    private void handleAquaDisplayFrame(String[] parts, InputStream input, OutputStream output) throws IOException {
        if (parts.length < 4) {
            output.write("ERR frame-header\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        int width;
        int height;
        int byteCount;
        try {
            width = Integer.parseInt(parts[1]);
            height = Integer.parseInt(parts[2]);
            byteCount = Integer.parseInt(parts[3]);
        } catch (NumberFormatException error) {
            output.write("ERR frame-size\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (width <= 0 || height <= 0 || byteCount != width * height * 4 || byteCount > 64 * 1024 * 1024) {
            output.write("ERR frame-dimensions\n".getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] rgba = readFully(input, byteCount);
        String title = parts.length >= 5 ? parts[4].trim() : "";
        if (title.isEmpty()) title = "Aqua display";
        final String cleanTitle = title;
        runOnUiThread(() -> showAquaDisplayFrame(width, height, rgba, cleanTitle));
        output.write(("OK frame " + width + " " + height + "\n").getBytes(StandardCharsets.UTF_8));
    }

    private String readSocketLine(InputStream input) throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream(96);
        while (line.size() < 4096) {
            int value = input.read();
            if (value == -1) break;
            if (value == '\n') break;
            if (value != '\r') line.write(value);
        }
        if (line.size() == 0) return null;
        return line.toString(StandardCharsets.UTF_8.name());
    }

    private byte[] readFully(InputStream input, int byteCount) throws IOException {
        byte[] bytes = new byte[byteCount];
        int offset = 0;
        while (offset < byteCount) {
            int read = input.read(bytes, offset, byteCount - offset);
            if (read < 0) throw new IOException("unexpected end of frame");
            offset += read;
        }
        return bytes;
    }

    private void handleOpencamClient(LocalSocket socket) {
        try (LocalSocket ignored = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream output = socket.getOutputStream()) {
            String command = reader.readLine();
            String cleanCommand = command == null ? "" : command.trim();
            if (cleanCommand.equalsIgnoreCase("FRAME")) {
                writeOpencamFrame(output);
            } else {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8));
                writer.write(handleOpencamCommand(cleanCommand));
                writer.write('\n');
                writer.flush();
            }
        } catch (IOException ignored) {
        }
    }

    private String handleOpencamCommand(String command) {
        if (command.equalsIgnoreCase("INFO")) {
            return opencamCameraStatus("OK info") + " streaming=" + opencamStreaming
                    + " visible=" + opencamVisible + " last_frame_ms=" + opencamLastFrameAtMs;
        }
        if (command.equalsIgnoreCase("DISPLAY") || command.equalsIgnoreCase("DISPLAY BUFFER")) {
            opencamStreaming = true;
            runOnUiThread(this::showOpencamBuffer);
            return opencamCameraStatus("OK display.buffer");
        }
        if (command.toUpperCase(Locale.US).startsWith("STREAM")) {
            String[] parts = command.split("\\s+");
            boolean enable = parts.length < 2 || !"0".equals(parts[1]);
            opencamStreaming = enable;
            runOnUiThread(() -> {
                if (enable) showOpencamBuffer();
                else {
                    stopOpencamCamera();
                    if (opencamVisible) showEditor();
                }
            });
            return enable ? opencamCameraStatus("OK stream started") : "OK stream stopped";
        }
        if (command.toUpperCase(Locale.US).startsWith("CAPTURE")) {
            String[] parts = command.split("\\s+");
            int count = 1;
            if (parts.length > 1) {
                try {
                    count = Math.max(1, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
            int first = ++opencamFrameCounter;
            opencamFrameCounter += Math.max(0, count - 1);
            return "OK capture count=" + count + " first_frame=" + first
                    + " source=android-camera width=" + Math.max(0, opencamFrameWidth)
                    + " height=" + Math.max(0, opencamFrameHeight)
                    + " format=yuv420 last_frame_ms=" + opencamLastFrameAtMs;
        }
        return "ERR unknown opencam command: " + command;
    }

    private void writeOpencamFrame(OutputStream output) throws IOException {
        byte[] frame;
        int width;
        int height;
        long frameAt;
        synchronized (opencamFrameLock) {
            frame = opencamLatestGrayFrame == null ? null : opencamLatestGrayFrame.clone();
            width = opencamFrameWidth;
            height = opencamFrameHeight;
            frameAt = opencamLastFrameAtMs;
        }
        if (frame == null || frame.length == 0 || width <= 0 || height <= 0) {
            output.write("ERR no-frame\n".getBytes(StandardCharsets.UTF_8));
            output.flush();
            return;
        }
        String header = "FRAME " + width + " " + height + " gray8 " + frame.length + " " + frameAt + "\n";
        output.write(header.getBytes(StandardCharsets.UTF_8));
        output.write(frame);
        output.flush();
    }

    private String opencamCameraStatus(String prefix) {
        return prefix + " source=android-camera width=" + Math.max(0, opencamFrameWidth)
                + " height=" + Math.max(0, opencamFrameHeight)
                + " frames=" + Math.max(0, opencamFrameCounter);
    }

    private boolean hasCameraPermission() {
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void ensureOpencamCamera() {
        if (!opencamVisible || !opencamStreaming) return;
        if (!hasCameraPermission()) {
            if (opencamStatusText != null) opencamStatusText.setText("opencam.display.buffer\nwaiting for camera permission");
            if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{Manifest.permission.CAMERA}, 4205);
            return;
        }
        if (opencamTextureView == null || !opencamTextureView.isAvailable()) return;
        if (opencamCameraDevice != null) return;
        startOpencamCameraThread();
        try {
            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
            if (manager == null) return;
            opencamCameraId = chooseOpencamCamera(manager);
            if (opencamCameraId == null) {
                if (opencamStatusText != null) opencamStatusText.setText("opencam.display.buffer\nno camera found");
                return;
            }
            manager.openCamera(opencamCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    opencamCameraDevice = camera;
                    startOpencamPreviewSession();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    opencamCameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    opencamCameraDevice = null;
                    runOnUiThread(() -> {
                        if (opencamStatusText != null) opencamStatusText.setText("opencam camera error " + error);
                    });
                }
            }, opencamCameraHandler);
        } catch (CameraAccessException | SecurityException error) {
            if (opencamStatusText != null) opencamStatusText.setText("opencam camera unavailable\n" + error.getClass().getSimpleName());
        }
    }

    private String chooseOpencamCamera(CameraManager manager) throws CameraAccessException {
        String fallback = null;
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (fallback == null) fallback = id;
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) return id;
        }
        return fallback;
    }

    private void startOpencamCameraThread() {
        if (opencamCameraThread != null) return;
        opencamCameraThread = new HandlerThread("AndroPy-opencam-camera");
        opencamCameraThread.start();
        opencamCameraHandler = new Handler(opencamCameraThread.getLooper());
    }

    private void startOpencamPreviewSession() {
        if (opencamCameraDevice == null || opencamTextureView == null || !opencamTextureView.isAvailable()) return;
        try {
            SurfaceTexture texture = opencamTextureView.getSurfaceTexture();
            if (texture == null) return;
            int width = 640;
            int height = 480;
            texture.setDefaultBufferSize(width, height);
            Surface previewSurface = new Surface(texture);
            opencamImageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 3);
            opencamFrameWidth = width;
            opencamFrameHeight = height;
            opencamImageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image == null) return;
                    opencamFrameWidth = image.getWidth();
                    opencamFrameHeight = image.getHeight();
                    opencamLastFrameAtMs = SystemClock.uptimeMillis();
                    opencamFrameCounter++;
                    storeOpencamGrayFrame(image);
                    updateOpencamStatusText();
                } finally {
                    if (image != null) image.close();
                }
            }, opencamCameraHandler);
            CaptureRequest.Builder request = opencamCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            request.addTarget(previewSurface);
            request.addTarget(opencamImageReader.getSurface());
            request.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            opencamCameraDevice.createCaptureSession(Arrays.asList(previewSurface, opencamImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            opencamCaptureSession = session;
                            try {
                                session.setRepeatingRequest(request.build(), null, opencamCameraHandler);
                                updateOpencamStatusText();
                            } catch (CameraAccessException ignored) {
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            runOnUiThread(() -> {
                                if (opencamStatusText != null) opencamStatusText.setText("opencam camera configure failed");
                            });
                        }
                    }, opencamCameraHandler);
        } catch (CameraAccessException error) {
            if (opencamStatusText != null) opencamStatusText.setText("opencam camera start failed");
        }
    }

    private void storeOpencamGrayFrame(Image image) {
        if (image == null || image.getFormat() != ImageFormat.YUV_420_888 || image.getPlanes().length == 0) return;
        Image.Plane yPlane = image.getPlanes()[0];
        int width = image.getWidth();
        int height = image.getHeight();
        int rowStride = yPlane.getRowStride();
        int pixelStride = Math.max(1, yPlane.getPixelStride());
        byte[] gray = new byte[width * height];
        java.nio.ByteBuffer buffer = yPlane.getBuffer();
        for (int y = 0; y < height; y++) {
            int rowOffset = y * rowStride;
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                int index = rowOffset + x * pixelStride;
                if (index < buffer.limit()) gray[outputOffset + x] = buffer.get(index);
            }
        }
        synchronized (opencamFrameLock) {
            opencamLatestGrayFrame = gray;
        }
    }

    private void updateOpencamStatusText() {
        if (!opencamVisible || opencamStatusText == null) return;
        runOnUiThread(() -> {
            if (opencamStatusText != null) {
                opencamStatusText.setText("opencam.display.buffer\nsource=android-camera  "
                        + Math.max(0, opencamFrameWidth) + "x" + Math.max(0, opencamFrameHeight)
                        + "  frame=" + Math.max(0, opencamFrameCounter));
            }
        });
    }

    private void stopOpencamCamera() {
        try {
            if (opencamCaptureSession != null) opencamCaptureSession.close();
        } catch (Exception ignored) {
        }
        opencamCaptureSession = null;
        try {
            if (opencamCameraDevice != null) opencamCameraDevice.close();
        } catch (Exception ignored) {
        }
        opencamCameraDevice = null;
        try {
            if (opencamImageReader != null) opencamImageReader.close();
        } catch (Exception ignored) {
        }
        opencamImageReader = null;
        if (opencamCameraThread != null) {
            opencamCameraThread.quitSafely();
            opencamCameraThread = null;
            opencamCameraHandler = null;
        }
    }

    private void showOpencamBuffer() {
        opencamVisible = true;
        aquaDisplayVisible = false;
        terminalVisible = false;
        fileManagerVisible = false;
        settingsVisible = false;
        stopTerminal();
        setContentView(buildOpencamBufferScreen());
    }

    private View buildOpencamBufferScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 18, 22));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_arrow_back_24", "drawable", getPackageName()));
        back.setColorFilter(MUTED);
        back.setBackgroundColor(BAR);
        back.setContentDescription("Editor");
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(v -> showEditor());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView title = new TextView(this);
        title.setText("OpenCam buffer");
        title.setTextColor(TEXT);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, 0, 0);
        topBar.addView(title, new LinearLayout.LayoutParams(0, dp(48), 1));

        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

        FrameLayout previewShell = new FrameLayout(this);
        previewShell.setBackgroundColor(Color.rgb(10, 12, 16));
        opencamTextureView = new TextureView(this);
        opencamTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                ensureOpencamCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                stopOpencamCamera();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
        previewShell.addView(opencamTextureView, new FrameLayout.LayoutParams(-1, -1));

        opencamStatusText = new TextView(this);
        opencamStatusText.setText("opencam.display.buffer\nsource=android-camera");
        opencamStatusText.setTextColor(Color.rgb(230, 236, 245));
        opencamStatusText.setTextSize(13);
        opencamStatusText.setTypeface(Typeface.MONOSPACE);
        opencamStatusText.setPadding(dp(14), dp(12), dp(14), dp(12));
        opencamStatusText.setBackground(rounded(Color.argb(120, 20, 24, 30), Color.argb(90, 255, 255, 255), 1, 8));
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
        statusParams.setMargins(dp(16), dp(16), dp(16), dp(16));
        previewShell.addView(opencamStatusText, statusParams);

        root.addView(previewShell, new LinearLayout.LayoutParams(-1, 0, 1));
        ensureOpencamCamera();
        return root;
    }

    private void showAquaDisplayFrame(int width, int height, byte[] rgba, String title) {
        if (rgba == null || rgba.length != width * height * 4) return;
        if (!aquaDisplayVisible || aquaDisplayImage == null) {
            buildAquaDisplayScreen(title);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] argb = new byte[rgba.length];
        for (int i = 0; i < rgba.length; i += 4) {
            argb[i] = rgba[i + 3];
            argb[i + 1] = rgba[i];
            argb[i + 2] = rgba[i + 1];
            argb[i + 3] = rgba[i + 2];
        }
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argb));
        aquaDisplayFrameCounter++;
        aquaDisplayFrameWidth = width;
        aquaDisplayFrameHeight = height;
        aquaDisplayTitle = title == null || title.trim().isEmpty() ? "Aqua display" : title.trim();
        aquaDisplayImage.setImageBitmap(bitmap);
        aquaDisplayImage.setContentDescription(aquaDisplayTitle);
        if (aquaDisplayStatusText != null) {
            aquaDisplayStatusText.setText(aquaDisplayTitle + "\n" + width + "x" + height
                    + "  frame=" + aquaDisplayFrameCounter);
        }
    }

    private void buildAquaDisplayScreen(String title) {
        aquaDisplayVisible = true;
        opencamVisible = false;
        terminalVisible = false;
        fileManagerVisible = false;
        settingsVisible = false;
        stopTerminal();
        stopOpencamCamera();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 18, 22));

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), 0, dp(12), 0);
        topBar.setBackgroundColor(BAR);

        ImageButton back = new ImageButton(this);
        back.setImageResource(getResources().getIdentifier("ic_arrow_back_24", "drawable", getPackageName()));
        back.setColorFilter(MUTED);
        back.setBackgroundColor(BAR);
        back.setContentDescription("Editor");
        back.setPadding(dp(7), dp(7), dp(7), dp(7));
        back.setOnClickListener(v -> showEditor());
        topBar.addView(back, new LinearLayout.LayoutParams(dp(34), dp(34)));

        TextView titleView = new TextView(this);
        titleView.setText(title == null || title.trim().isEmpty() ? "Aqua display" : title.trim());
        titleView.setTextColor(TEXT);
        titleView.setTextSize(15);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setPadding(dp(12), 0, 0, 0);
        topBar.addView(titleView, new LinearLayout.LayoutParams(0, dp(48), 1));
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

        FrameLayout canvas = new FrameLayout(this);
        canvas.setBackgroundColor(Color.rgb(7, 9, 13));
        aquaDisplayImage = new ImageView(this);
        aquaDisplayImage.setBackgroundColor(Color.rgb(7, 9, 13));
        aquaDisplayImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        canvas.addView(aquaDisplayImage, new FrameLayout.LayoutParams(-1, -1));

        aquaDisplayStatusText = new TextView(this);
        aquaDisplayStatusText.setTextColor(Color.rgb(230, 236, 245));
        aquaDisplayStatusText.setTextSize(13);
        aquaDisplayStatusText.setTypeface(Typeface.MONOSPACE);
        aquaDisplayStatusText.setPadding(dp(14), dp(12), dp(14), dp(12));
        aquaDisplayStatusText.setBackground(rounded(Color.argb(120, 20, 24, 30), Color.argb(90, 255, 255, 255), 1, 8));
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.START);
        statusParams.setMargins(dp(16), dp(16), dp(16), dp(16));
        canvas.addView(aquaDisplayStatusText, statusParams);

        root.addView(canvas, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void applyRuntimeEnvironment(ProcessBuilder builder) {
        File realBinRoot = new File(prefixRealRoot, "bin");
        File realLibRoot = new File(prefixRealRoot, "lib");
        File realTmpRoot = new File(prefixRealRoot, "tmp");
        String libPath = getApplicationInfo().nativeLibraryDir + ":" + realLibRoot.getAbsolutePath();
        builder.environment().put("PREFIX", prefixRoot.getAbsolutePath());
        builder.environment().put("HOME", homeRoot.getAbsolutePath());
        builder.environment().put("ANDROPY_PREFIX_REAL", prefixRealRoot.getAbsolutePath());
        builder.environment().put("ANDROPY_HOME_REAL", homeRealRoot.getAbsolutePath());
        builder.environment().put("ANDROPY_OPENCAM_SOCKET", opencamSocketName());
        builder.environment().put("ANDROPY_DISPLAY_SOCKET", aquaDisplaySocketName());
        builder.environment().put("PATH", realBinRoot.getAbsolutePath() + ":/system/bin:/system/xbin");
        builder.environment().put("LD_LIBRARY_PATH", libPath);
        builder.environment().put("TMPDIR", realTmpRoot.getAbsolutePath());
        builder.environment().put("TERM", "xterm-256color");
        builder.environment().put("COLORTERM", "truecolor");
        builder.environment().put("PIP_DISABLE_PIP_VERSION_CHECK", "1");
    }

    private GradientDrawable rounded(int fill, int stroke, int strokeDp, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), stroke);
        return drawable;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        float kb = bytes / 1024f;
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb);
        float mb = kb / 1024f;
        if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb);
        return String.format(Locale.US, "%.1f GB", mb / 1024f);
    }

    private String usedOfTotal(File path) {
        long total = storageTotal(path);
        long available = storageAvailable(path);
        long used = Math.max(0, total - available);
        return "Used " + formatBytes(used) + " of " + formatBytes(total);
    }

    private long storageTotal(File path) {
        try {
            StatFs stat = new StatFs(path.getAbsolutePath());
            return stat.getTotalBytes();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private long storageAvailable(File path) {
        try {
            StatFs stat = new StatFs(path.getAbsolutePath());
            return stat.getAvailableBytes();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private long directorySize(File root, int maxFiles) {
        if (root == null || !root.exists()) return 0;
        long total = 0;
        int seen = 0;
        ArrayList<File> stack = new ArrayList<>();
        stack.add(root);
        while (!stack.isEmpty() && seen < maxFiles) {
            File file = stack.remove(stack.size() - 1);
            seen++;
            if (file.isFile()) {
                total += Math.max(0, file.length());
            } else {
                File[] children = file.listFiles();
                if (children != null) stack.addAll(Arrays.asList(children));
            }
        }
        return total;
    }

    private byte[] readFileBytes(File file, int maxBytes) throws IOException {
        long length = file.length();
        if (length > maxBytes) throw new IOException("file too large");
        ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.max(0, length));
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void saveEditorState() {
        if (editor == null) return;
        String code = editor.getText().toString();
        prefs.edit().putString("code", code).apply();
        saveOpenedFileQuietly(code);
    }

    private void saveOpenedFileQuietly(String code) {
        String path = prefs.getString("file_path", "");
        if (path == null || path.isEmpty()) return;
        try (FileOutputStream output = new FileOutputStream(new File(path))) {
            output.write(code.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    private File uniqueDestination(File destination, String name) {
        File target = new File(destination, name);
        if (!target.exists()) return target;
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        for (int i = 1; i < 1000; i++) {
            File candidate = new File(destination, base + " copy" + (i == 1 ? "" : " " + i) + ext);
            if (!candidate.exists()) return candidate;
        }
        return new File(destination, base + " copy " + System.currentTimeMillis() + ext);
    }

    private boolean copyPath(File source, File target) {
        try {
            if (source.isDirectory()) {
                if (!target.mkdirs() && !target.isDirectory()) return false;
                File[] children = source.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (!copyPath(child, new File(target, child.getName()))) return false;
                    }
                }
                return true;
            }
            File parent = target.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
            try (InputStream input = new FileInputStream(source);
                 OutputStream output = new FileOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean movePath(File source, File target) {
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) return false;
        if (source.renameTo(target)) return true;
        if (!copyPath(source, target)) return false;
        return deletePath(source);
    }

    private boolean deletePath(File file) {
        if (file == null || !file.exists()) return false;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deletePath(child);
            }
        }
        return file.delete();
    }

    private boolean isSameOrChild(File child, File parent) {
        try {
            String childPath = child.getCanonicalPath();
            String parentPath = parent.getCanonicalPath();
            return childPath.equals(parentPath) || childPath.startsWith(parentPath + File.separator);
        } catch (IOException e) {
            return false;
        }
    }

    private void showEditor() {
        if (opencamVisible) stopOpencamCamera();
        terminalVisible = false;
        fileManagerVisible = false;
        settingsVisible = false;
        opencamVisible = false;
        aquaDisplayVisible = false;
        choosingProjectFolder = false;
        projectPanelOpen = false;
        terminalReturnToEditorOnExit = false;
        stopTerminal();
        setContentView(buildEditorScreen());
    }

    private void animatePendingEditorSwitch(View target) {
        if (target == null || !pendingEditorSwitchAnimation) return;
        int direction = pendingEditorSwitchDirection == 0 ? 1 : pendingEditorSwitchDirection;
        pendingEditorSwitchAnimation = false;
        target.setAlpha(0f);
        target.setTranslationX(direction * dp(44));
        target.post(() -> target.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(190)
                .setInterpolator(new DecelerateInterpolator())
                .start());
    }

    private void startTerminal() {
        ensureProjectRoots();
        stopTerminal();

        String startupScript = terminalStartupScript;
        terminalStartupScript = null;
        if (startupScript != null && startupScript.trim().isEmpty()) startupScript = null;
        File bash = new File(binRoot, "bash");
        File packagedBash = new File(getApplicationInfo().nativeLibraryDir, "libandropy_bash.so");
        File packagedLauncher = new File(getApplicationInfo().nativeLibraryDir, "libandropy_bash_launcher.so");
        boolean hasBash = packagedBash.canExecute() || bash.canExecute();
        String shell = hasBash && packagedLauncher.canExecute()
                ? packagedLauncher.getAbsolutePath()
                : hasBash ? bash.getAbsolutePath() : "/system/bin/sh";
        File realBinRoot = new File(prefixRealRoot, "bin");
        File realLibRoot = new File(prefixRealRoot, "lib");
        File realTmpRoot = new File(prefixRealRoot, "tmp");
        File realEtcRoot = new File(prefixRealRoot, "etc");
        String path = realBinRoot.getAbsolutePath() + ":/system/bin:/system/xbin";
        String startupCommand = terminalStartupCommand;
        terminalStartupCommand = null;
        if (startupCommand != null && startupCommand.trim().isEmpty()) startupCommand = null;
        if (startupScript != null) {
            startupCommand = "exec sh " + shellQuote(startupScript);
        }
        String[] args = hasBash
                ? startupCommand == null
                ? new String[]{"bash", "--rcfile", bashRcFile().getAbsolutePath(), "-i"}
                : new String[]{"bash", "--rcfile", bashRcFile().getAbsolutePath(), "-i", "-c", startupCommand}
                : startupCommand == null ? new String[]{"sh", "-i"} : new String[]{"sh", "-i", "-c", startupCommand};
        String[] env = new String[]{
                "PREFIX=" + prefixRoot.getAbsolutePath(),
                "HOME=" + homeRoot.getAbsolutePath(),
                "ANDROPY_PREFIX_REAL=" + prefixRealRoot.getAbsolutePath(),
                "ANDROPY_HOME_REAL=" + homeRealRoot.getAbsolutePath(),
                "ANDROPY_OPENCAM_SOCKET=" + opencamSocketName(),
                "ANDROPY_DISPLAY_SOCKET=" + aquaDisplaySocketName(),
                "ANDROPY_START_REAL=" + homeRealRoot.getAbsolutePath(),
                "ANDROPY_BASH_PATH=" + packagedBash.getAbsolutePath(),
                "PATH=" + path,
                "LD_LIBRARY_PATH=" + getApplicationInfo().nativeLibraryDir + ":" + realLibRoot.getAbsolutePath(),
                "APT_CONFIG=" + new File(realEtcRoot, "apt/apt.conf").getAbsolutePath(),
                "COLORTERM=truecolor",
                "CLICOLOR=1",
                "CLICOLOR_FORCE=1",
                "FORCE_COLOR=1",
                "LS_COLORS=" + lsColors(),
                "TMPDIR=" + realTmpRoot.getAbsolutePath(),
                "TERM=xterm-256color"
        };

        termuxSession = new TerminalSession(
                shell,
                homeRealRoot.getAbsolutePath(),
                args,
                env,
                TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                new AndroPyTerminalSessionClient());
        termuxTerminalView.attachSession(termuxSession);
        applyTerminalColors();
        termuxSession.updateSize(80, 24);
    }

    private void applyTerminalColors() {
        if (termuxTerminalView == null || termuxTerminalView.mEmulator == null) return;
        int[] colors = termuxTerminalView.mEmulator.mColors.mCurrentColors;
        int[] palette = new int[]{
                Color.rgb(13, 17, 23),
                Color.rgb(255, 96, 120),
                Color.rgb(80, 250, 123),
                Color.rgb(255, 220, 105),
                Color.rgb(102, 191, 255),
                Color.rgb(218, 112, 214),
                Color.rgb(94, 234, 212),
                Color.rgb(230, 237, 243),
                Color.rgb(117, 130, 147),
                Color.rgb(255, 123, 123),
                Color.rgb(141, 255, 180),
                Color.rgb(255, 235, 129),
                Color.rgb(126, 204, 255),
                Color.rgb(235, 139, 255),
                Color.rgb(124, 246, 229),
                Color.rgb(255, 255, 255)
        };
        System.arraycopy(palette, 0, colors, 0, palette.length);
        colors[256] = Color.rgb(232, 238, 247);
        colors[257] = Color.rgb(12, 15, 20);
        colors[258] = Color.rgb(255, 220, 105);
        termuxTerminalView.onScreenUpdated();
    }

    private String lsColors() {
        return "di=1;34:ln=1;36:so=1;35:pi=33:ex=1;32:bd=1;33:cd=1;33:su=37;41:sg=30;43:"
                + "tw=30;42:ow=34;42:st=37;44:*.py=1;33:*.sh=1;32:*.c=1;36:*.h=1;36:"
                + "*.o=90:*.so=1;35:*.a=1;35:*.zip=1;31:*.tar=1;31:*.gz=1;31";
    }

    private void stopTerminal() {
        if (termuxSession != null) {
            termuxSession.finishIfRunning();
            termuxSession = null;
        }
    }

    private void ensureProjectRoots() {
        initProjectRoots();
        setBootstrapProgress(0.08f, "Preparing app prefix");
        appendBootstrapOutput("$ prepare-prefix");
        appendBootstrapOutput("prefix=" + prefixRoot.getAbsolutePath());
        appendBootstrapOutput("home=" + homeRoot.getAbsolutePath());
        resetPrefixIfBootstrapChanged();
        setBootstrapProgress(0.16f, "Creating Linux directories");
        appendBootstrapOutput("$ create-directories");
        createBootstrapDirectories();

        homeRoot.mkdirs();
        setBootstrapProgress(0.28f, "Selecting device runtime");
        appendBootstrapOutput("$ select-runtime " + runtimeAbiName());
        installRuntimeAssets();
        setBootstrapProgress(0.76f, "Linking native tools");
        appendBootstrapOutput("$ link-native-tools");
        installPackagedBash();
        installPackagedRuntimeTools();
        installPythonInteractiveRuntime();
        installPythonPackageRuntime();
        installOpencamRuntime();
        installAquaDisplayRuntime();
        setBootstrapProgress(0.88f, "Writing shell environment");
        appendBootstrapOutput("$ write-shell-profile");
        installBootstrapHelpers();
        new File(etcRoot, "bashrc").delete();
        writeBashRc();
        setBootstrapProgress(0.96f, "Finalizing");
        appendBootstrapOutput("$ finalize");
    }

    private void initProjectRoots() {
        prefixRoot = new File("/data/data/" + getPackageName() + "/files/" + PREFIX_DIR);
        prefixRealRoot = new File(getFilesDir(), PREFIX_DIR);
        homeRoot = new File("/data/data/" + getPackageName() + "/files/home");
        homeRealRoot = new File(getFilesDir(), "home");
        binRoot = new File(prefixRoot, "bin");
        etcRoot = new File(prefixRoot, "etc");
        profileRoot = new File(etcRoot, "profile.d");
        includeRoot = new File(prefixRoot, "include");
        libRoot = new File(prefixRoot, "lib");
        libexecRoot = new File(prefixRoot, "libexec");
        optRoot = new File(prefixRoot, "opt");
        shareRoot = new File(prefixRoot, "share");
        tmpRoot = new File(prefixRoot, "tmp");
        varRoot = new File(prefixRoot, "var");
        varCacheRoot = new File(varRoot, "cache");
        varLibRoot = new File(varRoot, "lib");
        varLibDpkgRoot = new File(varLibRoot, "dpkg");
        varLibAptListsRoot = new File(varLibRoot, "apt/lists");
        varLogRoot = new File(varRoot, "log");
        varRunRoot = new File(varRoot, "run");
        varTmpRoot = new File(varRoot, "tmp");
    }

    private boolean runtimeReady() {
        File marker = new File(prefixRoot, ".andropy-runtime-assets");
        File bash = new File(binRoot, "bash");
        File python = new File(binRoot, "python");
        if (!marker.isFile() || !bash.exists() || !python.exists()) return false;
        try {
            byte[] version = new byte[(int) marker.length()];
            try (InputStream input = new FileInputStream(marker)) {
                return input.read(version) == version.length
                        && runtimeAssetVersion().equals(new String(version, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            return false;
        }
    }

    private void writeBashRc() {
        File bashrc = bashRcFile();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(bashrc), StandardCharsets.UTF_8))) {
            writer.write("export PREFIX=\"" + prefixRoot.getAbsolutePath() + "\"\n");
            writer.write("export HOME=\"" + homeRoot.getAbsolutePath() + "\"\n");
            writer.write("export ANDROPY_PREFIX_REAL=\"" + prefixRealRoot.getAbsolutePath() + "\"\n");
            writer.write("export ANDROPY_HOME_REAL=\"" + homeRealRoot.getAbsolutePath() + "\"\n");
            writer.write("export ANDROPY_OPENCAM_SOCKET=\"" + opencamSocketName() + "\"\n");
            writer.write("export ANDROPY_DISPLAY_SOCKET=\"" + aquaDisplaySocketName() + "\"\n");
            writer.write("export PATH=\"$ANDROPY_PREFIX_REAL/bin:/system/bin:/system/xbin\"\n");
            writer.write("export LD_LIBRARY_PATH=\"" + getApplicationInfo().nativeLibraryDir + ":$ANDROPY_PREFIX_REAL/lib\"\n");
            writer.write("export TERMINFO=\"$ANDROPY_PREFIX_REAL/share/terminfo\"\n");
            writer.write("export APT_CONFIG=\"$ANDROPY_PREFIX_REAL/etc/apt/apt.conf\"\n");
            writer.write("export COLORTERM=truecolor\n");
            writer.write("export CLICOLOR=1\n");
            writer.write("export CLICOLOR_FORCE=1\n");
            writer.write("export FORCE_COLOR=1\n");
            writer.write("export LS_COLORS='" + lsColors() + "'\n");
            writer.write("export LANG=\"C.UTF-8\"\n");
            writer.write("export LC_ALL=\"C.UTF-8\"\n");
            writer.write("export TMPDIR=\"$ANDROPY_PREFIX_REAL/tmp\"\n");
            writer.write("[ -r \"$ANDROPY_PREFIX_REAL/etc/profile\" ] && . \"$ANDROPY_PREFIX_REAL/etc/profile\"\n");
            writer.write("cd \"$ANDROPY_HOME_REAL\" 2>/dev/null || cd \"$HOME\" 2>/dev/null\n");
            writer.write("PROMPT_COMMAND='__andropy_prompt_before; __andropy_prompt_build'\n");
            writer.write("__andropy_prompt_before() {\n");
            writer.write("  local now duration\n");
            writer.write("  now=$(date +%s 2>/dev/null || printf 0)\n");
            writer.write("  if [ -n \"$__andropy_prompt_started\" ]; then\n");
            writer.write("    duration=$((now - __andropy_prompt_started))\n");
            writer.write("    if [ \"$duration\" -gt 0 ]; then __andropy_cmd_duration=\"${duration}s\"; else __andropy_cmd_duration=\"\"; fi\n");
            writer.write("  fi\n");
            writer.write("  __andropy_prompt_started=$now\n");
            writer.write("}\n");
            writer.write("__andropy_prompt_dir() {\n");
            writer.write("  local dir=\"$PWD\"\n");
            writer.write("  case \"$dir\" in\n");
            writer.write("    \"$ANDROPY_HOME_REAL\"*) dir=\"~${dir#$ANDROPY_HOME_REAL}\" ;;\n");
            writer.write("    \"$HOME\"*) dir=\"~${dir#$HOME}\" ;;\n");
            writer.write("    \"$ANDROPY_PREFIX_REAL\"*) dir=\"$PREFIX${dir#$ANDROPY_PREFIX_REAL}\" ;;\n");
            writer.write("  esac\n");
            writer.write("  printf '%s' \"$dir\"\n");
            writer.write("}\n");
            writer.write("__andropy_prompt_build() {\n");
            writer.write("  local green bold_green cyan yellow magenta bold_blue reset user host clock dir\n");
            writer.write("  green='\\[\\e[32m\\]'; bold_green='\\[\\e[1;32m\\]'; cyan='\\[\\e[36m\\]'; yellow='\\[\\e[33m\\]'; magenta='\\[\\e[35m\\]'; bold_blue='\\[\\e[1;34m\\]'; reset='\\[\\e[0m\\]'\n");
            writer.write("  user=${USER:-u0}; host=${HOSTNAME:-android}; clock=$(date +%H:%M:%S 2>/dev/null); dir=$(__andropy_prompt_dir)\n");
            writer.write("  PS1=\"${bold_green}┌──(${cyan}${user}${bold_green}㉿${cyan}${host}${bold_green})-[${yellow}${clock}${bold_green}]-[${magenta}${dir}${bold_green}]${reset}\\n${bold_green}└${yellow}${__andropy_cmd_duration}${bold_green}─${bold_blue}\\$ ${reset}\"\n");
            writer.write("}\n");
        } catch (IOException ignored) {
        }
    }

    private void resetPrefixIfBootstrapChanged() {
        File marker = new File(prefixRoot, ".andropy-runtime-assets");
        if (!prefixRoot.exists() || !marker.isFile()) return;
        try {
            byte[] version = new byte[(int) marker.length()];
            try (InputStream input = new FileInputStream(marker)) {
                if (input.read(version) == version.length
                        && runtimeAssetVersion().equals(new String(version, StandardCharsets.UTF_8))) {
                    return;
                }
            }
        } catch (IOException ignored) {
        }
        deleteChildren(prefixRoot);
    }

    private void createBootstrapDirectories() {
        prefixRoot.mkdirs();
        binRoot.mkdirs();
        etcRoot.mkdirs();
        profileRoot.mkdirs();
        includeRoot.mkdirs();
        libRoot.mkdirs();
        libexecRoot.mkdirs();
        optRoot.mkdirs();
        shareRoot.mkdirs();
        new File(shareRoot, "doc").mkdirs();
        new File(shareRoot, "man").mkdirs();
        tmpRoot.mkdirs();
        varRoot.mkdirs();
        varCacheRoot.mkdirs();
        new File(varCacheRoot, "apt/archives/partial").mkdirs();
        varLibRoot.mkdirs();
        varLibDpkgRoot.mkdirs();
        new File(etcRoot, "apt/apt.conf.d").mkdirs();
        new File(etcRoot, "apt/preferences.d").mkdirs();
        new File(varLibDpkgRoot, "info").mkdirs();
        new File(varLibDpkgRoot, "triggers").mkdirs();
        new File(varLibDpkgRoot, "updates").mkdirs();
        varLibAptListsRoot.mkdirs();
        new File(varLibAptListsRoot, "partial").mkdirs();
        varLogRoot.mkdirs();
        new File(varLogRoot, "apt").mkdirs();
        varRunRoot.mkdirs();
        varTmpRoot.mkdirs();
        touch(new File(varLibDpkgRoot, "available"));
        touch(new File(varLibDpkgRoot, "status"));
    }

    private void deleteChildren(File directory) {
        File[] children = directory.listFiles();
        if (children == null) return;
        for (File child : children) deleteRecursively(child);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }

    private void touch(File file) {
        try {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            if (!file.exists()) new FileOutputStream(file).close();
        } catch (IOException ignored) {
        }
    }

    private File bashRcFile() {
        return new File(homeRoot, ".bashrc");
    }

    private void installPackagedBash() {
        File bash = new File(binRoot, "bash");
        File packagedBash = new File(getApplicationInfo().nativeLibraryDir, "libandropy_bash.so");
        if (packagedBash.canExecute()) {
            bash.delete();
            try {
                Os.symlink(packagedBash.getAbsolutePath(), bash.getAbsolutePath());
                return;
            } catch (ErrnoException ignored) {
            }
        }
    }

    private void installPackagedRuntimeTools() {
        installNativeCommand("nano", "libandropy_nano.so");
        installNativeCommand("clear", "libandropy_clear.so");
        installNativeCommand("tset", "libandropy_tset.so");
        installNativeCommand("reset", "libandropy_tset.so");
        for (String command : COREUTILS_COMMANDS) {
            installNativeCommand(command, "libandropy_coreutils.so");
        }
    }

    private void installPythonInteractiveRuntime() {
        File pyrepl = new File(prefixRoot, "lib/python3.13/_pyrepl");
        if (!pyrepl.isDirectory()) {
            appendBootstrapOutput("$ install-python-repl");
            try {
                copyAssetTree(runtimeAbiAssetDir() + "/lib/python3.13/pyrepl_asset", pyrepl);
            } catch (IOException ignored) {
            }
        }

        File zipfilePath = new File(prefixRoot, "lib/python3.13/zipfile/_path");
        if (!zipfilePath.isDirectory()) {
            appendBootstrapOutput("$ install-python-zipfile-path");
            try {
                copyAssetTree(runtimeAbiAssetDir() + "/lib/python3.13/zipfile/path_asset", zipfilePath);
            } catch (IOException ignored) {
            }
        }
    }

    private void installPythonPackageRuntime() {
        File sitePackages = new File(prefixRoot, "lib/python3.13/site-packages");
        File marker = new File(sitePackages, ".andropy-pip-wheel");
        String wheelName = bundledPipWheelName();
        if (wheelName == null) return;
        if (new File(sitePackages, "pip").isDirectory() && markerMatches(marker, wheelName)) {
            installPipLaunchers();
            return;
        }

        appendBootstrapOutput("$ install-python-pip " + wheelName);
        deleteRecursively(new File(sitePackages, "pip"));
        deleteChildrenWithSuffix(sitePackages, ".dist-info", "pip-");
        try {
            extractWheelAsset("runtime-common/python-wheels/" + wheelName, sitePackages);
            writeText(marker, wheelName);
        } catch (IOException ignored) {
        }
        installPipLaunchers();
    }

    private void installOpencamRuntime() {
        File sitePackages = new File(prefixRoot, "lib/python3.13/site-packages");
        String assetDir = "runtime-common/python-native/" + runtimeAbiName();
        try {
            String[] files = getAssets().list(assetDir);
            if (files == null) return;
            for (String file : files) {
                if (!file.startsWith("opencam") || !file.endsWith(".so")) continue;
                appendBootstrapOutput("$ install-opencam " + file);
                copyAssetTree(assetDir + "/" + file, new File(sitePackages, file));
            }
        } catch (IOException ignored) {
        }
    }

    private void installAquaDisplayRuntime() {
        File sitePackages = new File(prefixRoot, "lib/python3.13/site-packages");
        try {
            appendBootstrapOutput("$ install-aquadisplay");
            copyAssetTree("runtime-common/python", sitePackages);
        } catch (IOException ignored) {
        }
    }

    private String bundledPipWheelName() {
        try {
            String[] wheels = getAssets().list("runtime-common/python-wheels");
            if (wheels == null) return null;
            for (String wheel : wheels) {
                if (wheel.startsWith("pip-") && wheel.endsWith(".whl")) return wheel;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private boolean markerMatches(File marker, String expected) {
        if (!marker.isFile()) return false;
        try {
            byte[] version = new byte[(int) marker.length()];
            try (InputStream input = new FileInputStream(marker)) {
                return input.read(version) == version.length
                        && expected.equals(new String(version, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            return false;
        }
    }

    private void extractWheelAsset(String assetPath, File targetDir) throws IOException {
        targetDir.mkdirs();
        String targetRoot = targetDir.getCanonicalPath() + File.separator;
        try (ZipInputStream zip = new ZipInputStream(getAssets().open(assetPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                File outputFile = new File(targetDir, entry.getName());
                String outputPath = outputFile.getCanonicalPath();
                if (!outputPath.startsWith(targetRoot)) {
                    zip.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    File parent = outputFile.getParentFile();
                    if (parent != null) parent.mkdirs();
                    try (FileOutputStream output = new FileOutputStream(outputFile)) {
                        int read;
                        while ((read = zip.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                    }
                    outputFile.setReadable(true, false);
                }
                zip.closeEntry();
            }
        }
    }

    private void installPipLaunchers() {
        String script = "#!/system/bin/sh\n"
                + "[ -n \"$PREFIX\" ] || PREFIX=\"/data/data/" + getPackageName() + "/files/usr\"\n"
                + "[ -n \"$ANDROPY_PREFIX_REAL\" ] || ANDROPY_PREFIX_REAL=\"/data/user/0/" + getPackageName() + "/files/usr\"\n"
                + "export PIP_CONFIG_FILE=\"$ANDROPY_PREFIX_REAL/etc/pip.conf\"\n"
                + "binary_only=0\n"
                + "for arg in \"$@\"; do\n"
                + "  case \"$arg\" in opencv-python|opencv-python-headless|cv2|numpy) binary_only=1 ;; esac\n"
                + "done\n"
                + "case \" $* \" in\n"
                + "  *\" opencv-python \"*|*\" cv2 \"*) echo \"Aqua CV: resolving OpenCV from the Aqua wheel index first\" >&2 ;;\n"
                + "esac\n"
                + "if [ \"$binary_only\" = 1 ]; then\n"
                + "  exec python -m pip --only-binary=:all: \"$@\"\n"
                + "fi\n"
                + "exec python -m pip \"$@\"\n";
        installExecutableScript("pip", script);
        installExecutableScript("pip3", script);
        installExecutableScript("pip3.13", script);
    }

    private void deleteChildrenWithSuffix(File directory, String suffix, String prefix) {
        File[] children = directory.listFiles();
        if (children == null) return;
        for (File child : children) {
            String name = child.getName();
            if (name.startsWith(prefix) && name.endsWith(suffix)) deleteRecursively(child);
        }
    }

    private void installBootstrapHelpers() {
        writeText(new File(etcRoot, "profile"),
                "export PREFIX=\"/data/data/" + getPackageName() + "/files/usr\"\n"
                        + "export HOME=\"/data/data/" + getPackageName() + "/files/home\"\n"
                        + "export ANDROPY_PREFIX_REAL=\"" + prefixRealRoot.getAbsolutePath() + "\"\n"
                        + "export ANDROPY_HOME_REAL=\"" + homeRealRoot.getAbsolutePath() + "\"\n"
                        + "export TMPDIR=\"$ANDROPY_PREFIX_REAL/tmp\"\n"
                        + "export PATH=\"$ANDROPY_PREFIX_REAL/bin:/system/bin:/system/xbin\"\n"
                        + "export LD_LIBRARY_PATH=\"" + getApplicationInfo().nativeLibraryDir + ":$ANDROPY_PREFIX_REAL/lib\"\n"
                        + "export TERMINFO=\"$ANDROPY_PREFIX_REAL/share/terminfo\"\n"
                        + "export APT_CONFIG=\"$ANDROPY_PREFIX_REAL/etc/apt/apt.conf\"\n"
                        + "export COLORTERM=truecolor\n"
                        + "export CLICOLOR=1\n"
                        + "export CLICOLOR_FORCE=1\n"
                        + "export FORCE_COLOR=1\n"
                        + "export LS_COLORS='" + lsColors() + "'\n"
                        + "export LANG=\"C.UTF-8\"\n"
                        + "export LC_ALL=\"C.UTF-8\"\n"
                        + "umask 022\n"
                        + "for f in \"$ANDROPY_PREFIX_REAL\"/etc/profile.d/*.sh; do [ -r \"$f\" ] && . \"$f\"; done\n");
        writeText(new File(profileRoot, "00-andropy-environment.sh"),
                "export ANDROPY_PACKAGE=\"" + getPackageName() + "\"\n"
                        + "export ANDROPY_BOOTSTRAP_VARIANT=\"andropy-native-userland\"\n"
                        + "export ANDROID_DATA=\"/data\"\n"
                        + "export ANDROID_ROOT=\"/system\"\n"
                        + "mkdir -p \"$ANDROPY_PREFIX_REAL/tmp\" \"$ANDROPY_PREFIX_REAL/var/tmp\" \"$ANDROPY_PREFIX_REAL/var/run\" \"$ANDROPY_PREFIX_REAL/var/log\" 2>/dev/null\n");
        writeText(new File(etcRoot, "andropy-bootstrap-packages"),
                "apt\nbash\nbzip2\ncommand-not-found\ncoreutils\ncurl\ndash\ndebianutils\ndiffutils\n"
                        + "dos2unix\ned\nfindutils\ngawk\ngrep\ngzip\ninetutils\nless\nlsof\nnano\n"
                        + "net-tools\npatch\nprocps\npsmisc\nsed\ntar\nunzip\nutil-linux\nxz-utils\n"
                        + "clang\ndpkg\nlibllvm\nlld\nllvm\nmake\npkg-config\n");
        writeText(new File(etcRoot, "pip.conf"),
                "[global]\n"
                        + "index-url = " + AQUA_PYTHON_INDEX + "\n"
                        + "extra-index-url = " + PYPI_INDEX + "\n"
                        + "prefer-binary = true\n"
                        + "disable-pip-version-check = true\n"
                        + "timeout = 60\n"
                        + "\n"
                        + "[install]\n"
                        + "prefer-binary = true\n");
        writeText(new File(etcRoot, "apt/sources.list"),
                "deb [trusted=yes] " + AQUA_APT_REPO + " stable main\n");
        writeText(new File(etcRoot, "apt/apt.conf"),
                "Dir \"/data/data/" + getPackageName() + "/files/usr\";\n"
                        + "Dir::Etc \"etc/apt\";\n"
                        + "Dir::State \"var/lib/apt\";\n"
                        + "Dir::Cache \"var/cache/apt\";\n"
                        + "Dir::Log \"var/log/apt\";\n"
                        + "Dir::Temp \"/data/data/" + getPackageName() + "/files/usr/tmp\";\n"
                        + "Dir::Etc::parts \"apt.conf.d\";\n"
                        + "Dir::State::status \"/data/data/" + getPackageName() + "/files/usr/var/lib/dpkg/status\";\n"
                        + "Dir::Bin::methods \"/data/data/" + getPackageName() + "/files/usr/lib/apt/methods\";\n"
                        + "Dir::Bin::dpkg \"/data/data/" + getPackageName() + "/files/usr/bin/dpkg\";\n"
                        + "DPkg::Path \"/data/data/" + getPackageName() + "/files/usr/bin\";\n"
                        + "DPkg::Tools::Options::/data/data/" + getPackageName() + "/files/usr/bin/dpkg::InfoFD \"20\";\n"
                        + "APT::Sandbox::User \"root\";\n");
        writeText(new File(etcRoot, "apt/apt.conf.d/00-aqua-https"),
                "Acquire::https::CaInfo \"/data/data/" + getPackageName() + "/files/usr/etc/tls/cert.pem\";\n");
        installExecutableScript("andropy-bootstrap-info",
                "#!/system/bin/sh\n"
                        + "[ -n \"$PREFIX\" ] || PREFIX=\"/data/data/" + getPackageName() + "/files/usr\"\n"
                        + "[ -n \"$HOME\" ] || HOME=\"/data/data/" + getPackageName() + "/files/home\"\n"
                        + "echo \"PREFIX=$PREFIX\"\n"
                        + "echo \"HOME=$HOME\"\n"
                        + "echo \"Variant: andropy-native-userland\"\n"
                        + "echo \"APT repo: " + AQUA_APT_REPO + "\"\n"
                        + "echo \"Python index: " + AQUA_PYTHON_INDEX + " + " + PYPI_INDEX + "\"\n"
                        + "echo \"Packaged now: bash, GNU coreutils, clang/LLVM, make, nano, Python\"\n"
                        + "echo \"Bootstrap payload:\"\n"
                        + "cat \"$PREFIX/etc/andropy-bootstrap-packages\" 2>/dev/null\n");
        installNativePlaceholder("pkg");
        installNativePlaceholder("clang");
        installNativePlaceholder("clang++");
        installNativePlaceholder("llvm-config");
        installNativePlaceholder("gcc");
        installNativePlaceholder("g++");
        installNativePlaceholder("make");
    }

    private void installExecutableScript(String name, String content) {
        File script = new File(binRoot, name);
        writeText(script, content);
        script.setExecutable(true, false);
        script.setReadable(true, false);
    }

    private void installNativePlaceholder(String command) {
        File commandPath = new File(binRoot, command);
        File nativeLauncher = new File(getApplicationInfo().nativeLibraryDir, "libandropy_tool_launcher.so");
        if (commandPath.exists()) return;
        commandPath.delete();
        if (!nativeLauncher.canExecute()) return;
        try {
            Os.symlink(nativeLauncher.getAbsolutePath(), commandPath.getAbsolutePath());
        } catch (ErrnoException ignored) {
        }
    }

    private void writeText(File file, String content) {
        try {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
        }
    }

    private void installRuntimeAssets() {
        File marker = new File(prefixRoot, ".andropy-runtime-assets");
        try {
            if (marker.isFile()) {
                byte[] version = new byte[(int) marker.length()];
                try (InputStream input = new FileInputStream(marker)) {
                    if (input.read(version) == version.length
                            && runtimeAssetVersion().equals(new String(version, StandardCharsets.UTF_8))) {
                        return;
                    }
                }
            }
            setBootstrapProgress(0.30f, "Installing common assets");
            appendBootstrapOutput("$ install-common-assets");
            copyAssetTree("runtime-common", prefixRoot);

            File payload = new File(getCacheDir(), runtimeZipName());
            if (!payload.isFile() || payload.length() == 0) {
                if (BuildConfig.ANDROPY_PREBUNDLED_RUNTIME) {
                    setBootstrapProgress(0.36f, "Loading bundled " + runtimeAbiName() + " runtime");
                    appendBootstrapOutput("$ load-bundled-runtime " + runtimeZipName());
                    if (assetExists(runtimeZipName())) {
                        copyBundledRuntimePayload(payload);
                    } else {
                        appendBootstrapOutput("! bundled runtime missing for " + runtimeAbiName() + ": " + runtimeZipName());
                        appendBootstrapOutput("$ fallback-download-runtime " + runtimeDownloadUrl());
                        downloadRuntimePayload(payload);
                    }
                } else {
                    setBootstrapProgress(0.36f, "Downloading " + runtimeAbiName() + " runtime");
                    appendBootstrapOutput("$ download-runtime " + runtimeDownloadUrl());
                    downloadRuntimePayload(payload);
                }
            }
            setBootstrapProgress(0.62f, "Extracting " + runtimeAbiName() + " runtime");
            appendBootstrapOutput("$ extract-runtime " + payload.getName());
            bootstrapExtractedBytes = 0;
            bootstrapLastUiUpdate = 0;
            if (payload.getName().endsWith(".tar.zst")) {
                extractTarZstFile(payload, prefixRoot);
            } else {
                extractZipFile(payload, prefixRoot);
            }
            appendBootstrapOutput("$ chmod-runtime-tree");
            chmodRuntimeTree(prefixRoot);
            try (FileOutputStream output = new FileOutputStream(marker)) {
                output.write(runtimeAssetVersion().getBytes(StandardCharsets.UTF_8));
            }
            appendBootstrapOutput("$ runtime-marker " + runtimeAssetVersion());
            payload.delete();
        } catch (IOException e) {
            appendBootstrapOutput("! runtime install failed: " + e.getMessage());
            throw new IllegalStateException("runtime install failed", e);
        }
    }

    private boolean assetExists(String name) {
        try (InputStream ignored = getAssets().open(name)) {
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private String runtimeAbiAssetDir() {
        return "runtime-" + runtimeAbiName();
    }

    private String runtimeAbiName() {
        String forcedAbi = BuildConfig.ANDROPY_ABI == null ? "" : BuildConfig.ANDROPY_ABI.trim();
        if ("arm64-v8a".equals(forcedAbi) || "x86_64".equals(forcedAbi)
                || "armeabi-v7a".equals(forcedAbi) || "x86".equals(forcedAbi)) {
            return forcedAbi;
        }
        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) return "arm64-v8a";
            if ("x86_64".equals(abi)) return "x86_64";
            if ("armeabi-v7a".equals(abi)) return "armeabi-v7a";
            if ("x86".equals(abi)) return "x86";
        }
        return "x86_64";
    }

    private String runtimeZipName() {
        if (selectedRuntimeProfile == RUNTIME_EXTENDED) {
            String abi = runtimeAbiName();
            if ("arm64-v8a".equals(abi)) return RUNTIME_EXTENDED_ARM64_ZIP;
            if ("armeabi-v7a".equals(abi)) return RUNTIME_EXTENDED_ARMV7_ZIP;
            if ("x86".equals(abi)) return RUNTIME_EXTENDED_X86_ZIP;
            return RUNTIME_EXTENDED_X86_64_ZIP;
        }
        String abi = runtimeAbiName();
        if ("arm64-v8a".equals(abi)) return RUNTIME_BASIC_ARM64_ZIP;
        if ("armeabi-v7a".equals(abi)) return RUNTIME_BASIC_ARMV7_ZIP;
        if ("x86".equals(abi)) return RUNTIME_BASIC_X86_ZIP;
        return RUNTIME_BASIC_X86_64_ZIP;
    }

    private String runtimeDownloadUrl() {
        String base = selectedRuntimeProfile == RUNTIME_EXTENDED ? RUNTIME_EXTENDED_RELEASE_BASE : RUNTIME_BASIC_RELEASE_BASE;
        return base + runtimeZipName();
    }

    private String runtimeAssetVersion() {
        String base = selectedRuntimeProfile == RUNTIME_EXTENDED ? RUNTIME_EXTENDED_VERSION : RUNTIME_BASIC_VERSION;
        return base + "-" + runtimeAbiName();
    }

    private void copyBundledRuntimePayload(File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        appendBootstrapOutput("$ copy-bundled-runtime -> " + target.getName());
        try (InputStream input = getAssets().open(runtimeZipName());
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[BOOTSTRAP_IO_BUFFER];
            long copied = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                copied += read;
                maybeThrottleBootstrapIo();
                maybeReportBootstrapIo("Loading bundled runtime", copied, 0, null);
            }
        }
    }

    private void downloadRuntimePayload(File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        File partial = new File(target.getParentFile(), target.getName() + ".part");
        HttpURLConnection connection = (HttpURLConnection) new URL(runtimeDownloadUrl()).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("Accept", "application/octet-stream");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("runtime download HTTP " + code);
        }
        long total = connection.getContentLengthLong();
        long copied = 0;
        byte[] buffer = new byte[BOOTSTRAP_IO_BUFFER];
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(partial)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                copied += read;
                maybeThrottleBootstrapIo();
                if (total > 0) {
                    float fraction = copied / (float) total;
                    setBootstrapProgress(0.36f + (0.20f * fraction), "Downloading " + formatBytes(copied) + " / " + formatBytes(total));
                }
                maybeReportBootstrapIo("Downloading runtime", copied, total, null);
            }
        } finally {
            connection.disconnect();
        }
        if (!partial.renameTo(target)) {
            throw new IOException("could not finalize runtime payload");
        }
    }

    private void extractZipFile(File zipFile, File targetDir) throws IOException {
        targetDir.mkdirs();
        String targetRoot = targetDir.getCanonicalPath() + File.separator;
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[65536];
            while ((entry = zip.getNextEntry()) != null) {
                File outputFile = new File(targetDir, entry.getName());
                String outputPath = outputFile.getCanonicalPath();
                if (!outputPath.startsWith(targetRoot)) {
                    zip.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    File outputParent = outputFile.getParentFile();
                    if (outputParent != null) outputParent.mkdirs();
                    try (OutputStream output = new FileOutputStream(outputFile)) {
                        int read;
                        while ((read = zip.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                    }
                    outputFile.setReadable(true, false);
                }
                zip.closeEntry();
            }
        }
    }

    private void extractTarZstFile(File archiveFile, File targetDir) throws IOException {
        targetDir.mkdirs();
        String targetRoot = targetDir.getCanonicalPath() + File.separator;
        appendBootstrapOutput("$ tar-open " + archiveFile.getName());
        try (InputStream fileInput = new FileInputStream(archiveFile);
             InputStream zstd = new ZstdInputStream(fileInput)) {
            byte[] header = new byte[512];
            String pendingLongName = null;
            String pendingLongLink = null;
            int entries = 0;
            while (readFullyOrEof(zstd, header)) {
                if (isZeroBlock(header)) break;
                String name = tarString(header, 0, 100);
                String prefix = tarString(header, 345, 155);
                if (!prefix.isEmpty()) name = prefix + "/" + name;
                int mode = (int) tarOctal(header, 100, 8);
                long size = tarOctal(header, 124, 12);
                char type = (char) header[156];
                String linkName = tarString(header, 157, 100);
                if (type == 'L') {
                    pendingLongName = readTarTextEntry(zstd, size);
                    skipTarPadding(zstd, size);
                    continue;
                }
                if (type == 'K') {
                    pendingLongLink = readTarTextEntry(zstd, size);
                    skipTarPadding(zstd, size);
                    continue;
                }
                if (type == 'x' || type == 'g') {
                    skipTarEntry(zstd, size);
                    continue;
                }
                if (pendingLongName != null) {
                    name = pendingLongName;
                    pendingLongName = null;
                }
                if (pendingLongLink != null) {
                    linkName = pendingLongLink;
                    pendingLongLink = null;
                }
                entries++;
                maybeReportBootstrapIo("Extracting", bootstrapExtractedBytes, 0, name);
                File outputFile = new File(targetDir, name);
                String outputPath = outputFile.getCanonicalPath();
                if (!outputPath.startsWith(targetRoot)) {
                    skipTarEntry(zstd, size);
                    continue;
                }

                if (type == '5' || name.endsWith("/") || outputFile.isDirectory()) {
                    if (outputFile.isFile()) outputFile.delete();
                    outputFile.mkdirs();
                    applyTarMode(outputFile, mode);
                } else if (type == '2') {
                    ensureParentDirectory(outputFile);
                    outputFile.delete();
                    try {
                        Os.symlink(linkName, outputFile.getAbsolutePath());
                    } catch (ErrnoException ignored) {
                    }
                } else if (type == '1') {
                    ensureParentDirectory(outputFile);
                    File linkTarget = new File(targetDir, linkName);
                    outputFile.delete();
                    if (linkTarget.isFile()) {
                        copyFile(linkTarget, outputFile);
                        bootstrapExtractedBytes += Math.max(0, outputFile.length());
                    } else {
                        try {
                            Os.symlink(linkTarget.getAbsolutePath(), outputFile.getAbsolutePath());
                        } catch (ErrnoException ignored) {
                        }
                    }
                    applyTarMode(outputFile, mode);
                } else {
                    ensureParentDirectory(outputFile);
                    try (OutputStream output = new FileOutputStream(outputFile)) {
                        copyExact(zstd, output, size, "Extracting", name);
                    }
                    applyTarMode(outputFile, mode);
                }
                skipTarPadding(zstd, size);
                if ((entries % 96) == 0) maybeThrottleBootstrapIo();
            }
            appendBootstrapOutput("$ tar-close entries=" + entries + " extracted=" + formatBytes(bootstrapExtractedBytes));
        }
    }

    private String readTarTextEntry(InputStream input, long size) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(size, 65536));
        copyExact(input, output, size, null, null);
        byte[] bytes = output.toByteArray();
        int length = bytes.length;
        while (length > 0 && bytes[length - 1] == 0) length--;
        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }

    private void ensureParentDirectory(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent == null) return;
        if (parent.isFile() && !parent.delete()) {
            throw new IOException("cannot replace file parent: " + parent.getAbsolutePath());
        }
        File grandparent = parent.getParentFile();
        if (grandparent != null && grandparent.isFile() && !grandparent.delete()) {
            throw new IOException("cannot replace file parent: " + grandparent.getAbsolutePath());
        }
        if (!parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("cannot create parent directory: " + parent.getAbsolutePath());
        }
    }

    private void applyTarMode(File file, int mode) {
        int safeMode = mode == 0 ? 0644 : mode & 0777;
        if (file.isDirectory() && (safeMode & 0111) == 0) {
            safeMode |= 0111;
        }
        try {
            Os.chmod(file.getAbsolutePath(), safeMode);
        } catch (ErrnoException ignored) {
            file.setReadable((safeMode & 0444) != 0, false);
            file.setWritable((safeMode & 0222) != 0, false);
            file.setExecutable((safeMode & 0111) != 0, false);
        }
    }

    private boolean readFullyOrEof(InputStream input, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset < buffer.length) {
            int read = input.read(buffer, offset, buffer.length - offset);
            if (read == -1) return offset != 0 && false;
            offset += read;
        }
        return true;
    }

    private boolean isZeroBlock(byte[] block) {
        for (byte b : block) {
            if (b != 0) return false;
        }
        return true;
    }

    private String tarString(byte[] header, int offset, int length) {
        int end = offset;
        int limit = offset + length;
        while (end < limit && header[end] != 0) end++;
        return new String(header, offset, end - offset, StandardCharsets.UTF_8).trim();
    }

    private long tarOctal(byte[] header, int offset, int length) {
        long value = 0;
        int end = offset + length;
        for (int i = offset; i < end; i++) {
            byte b = header[i];
            if (b == 0 || b == ' ') continue;
            if (b < '0' || b > '7') break;
            value = (value << 3) + (b - '0');
        }
        return value;
    }

    private void copyExact(InputStream input, OutputStream output, long bytes) throws IOException {
        copyExact(input, output, bytes, null, null);
    }

    private void copyExact(InputStream input, OutputStream output, long bytes, String stage, String name) throws IOException {
        byte[] buffer = new byte[BOOTSTRAP_IO_BUFFER];
        long remaining = bytes;
        while (remaining > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read == -1) throw new IOException("unexpected end of tar entry");
            output.write(buffer, 0, read);
            remaining -= read;
            bootstrapExtractedBytes += read;
            maybeThrottleBootstrapIo();
            if (stage != null) maybeReportBootstrapIo(stage, bootstrapExtractedBytes, 0, name);
        }
    }

    private void skipTarEntry(InputStream input, long size) throws IOException {
        skipFully(input, size + tarPadding(size));
    }

    private void skipTarPadding(InputStream input, long size) throws IOException {
        skipFully(input, tarPadding(size));
    }

    private long tarPadding(long size) {
        long remainder = size % 512;
        return remainder == 0 ? 0 : 512 - remainder;
    }

    private void skipFully(InputStream input, long bytes) throws IOException {
        long remaining = bytes;
        while (remaining > 0) {
            long skipped = input.skip(remaining);
            if (skipped <= 0) {
                if (input.read() == -1) throw new IOException("unexpected end of archive");
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    private void copyFile(File source, File target) throws IOException {
        try (InputStream input = new FileInputStream(source);
             OutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[BOOTSTRAP_IO_BUFFER];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                maybeThrottleBootstrapIo();
            }
        }
    }

    private void maybeThrottleBootstrapIo() {
        Thread.yield();
        long now = SystemClock.uptimeMillis();
        if ((now & 7L) == 0L) {
            SystemClock.sleep(1);
        }
    }

    private void maybeReportBootstrapIo(String stage, long done, long total, String name) {
        long now = SystemClock.uptimeMillis();
        if (now - bootstrapLastUiUpdate < BOOTSTRAP_UI_INTERVAL_MS) return;
        bootstrapLastUiUpdate = now;
        String detail = name == null || name.isEmpty() ? "" : "  " + trimMiddle(name, 46);
        if (total > 0) {
            float fraction = Math.max(0f, Math.min(1f, done / (float) total));
            setBootstrapProgress(0.36f + (0.20f * fraction), stage + " " + formatBytes(done) + " / " + formatBytes(total));
            appendBootstrapOutput(stage.toLowerCase(Locale.US) + " " + formatBytes(done) + "/" + formatBytes(total) + detail);
        } else {
            setBootstrapProgress(0.62f, stage + " " + formatBytes(done));
            appendBootstrapOutput(stage.toLowerCase(Locale.US) + " " + formatBytes(done) + detail);
        }
    }

    private String trimMiddle(String value, int max) {
        if (value == null || value.length() <= max) return value == null ? "" : value;
        int head = Math.max(8, (max - 3) / 2);
        int tail = Math.max(8, max - head - 3);
        return value.substring(0, head) + "..." + value.substring(value.length() - tail);
    }

    private void copyAssetTree(String assetPath, File targetDir) throws IOException {
        String[] children = getAssets().list(assetPath);
        if (children != null && children.length > 0) {
            targetDir.mkdirs();
            for (String child : children) {
                copyAssetTree(assetPath + "/" + child, new File(targetDir, child));
            }
            return;
        }

        File parent = targetDir.getParentFile();
        if (parent != null) parent.mkdirs();
        try (InputStream input = getAssets().open(assetPath);
             FileOutputStream output = new FileOutputStream(targetDir)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
    }

    private void chmodRuntimeTree(File root) {
        chmodExecutables(new File(root, "bin"));
        chmodExecutables(new File(root, "libexec"));
        chmodExecutables(new File(root, "lib/apt/methods"));
        chmodExecutables(new File(root, "lib/apt/solvers"));
        chmodLibraries(new File(root, "lib"));
    }

    private void chmodExecutables(File dir) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                chmodExecutables(child);
            } else {
                child.setReadable(true, false);
                child.setExecutable(true, false);
            }
        }
    }

    private void chmodLibraries(File dir) {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) {
                chmodLibraries(child);
            } else if (child.getName().contains(".so")) {
                child.setReadable(true, false);
                child.setExecutable(true, false);
            }
        }
    }

    private void installNativeCommand(String command, String nativeName) {
        File commandPath = new File(binRoot, command);
        File nativePayload = new File(getApplicationInfo().nativeLibraryDir, nativeName);
        File nativeLauncher = new File(getApplicationInfo().nativeLibraryDir, "libandropy_tool_launcher.so");
        if (!nativePayload.canExecute()) {
            return;
        }
        if (commandPath.exists()) return;
        commandPath.delete();
        try {
            Os.symlink(
                    nativeLauncher.canExecute() ? nativeLauncher.getAbsolutePath() : nativePayload.getAbsolutePath(),
                    commandPath.getAbsolutePath());
        } catch (ErrnoException ignored) {
        }
    }

    private void togglePanel() {
        if (panelOpen) {
            closePanel();
        } else {
            openPanel();
        }
    }

    private void openPanel() {
        hideKeyboard();
        closeProjectPanel();
        panelOpen = true;
        updatePanelStatus();
        sidePanel.setVisibility(View.VISIBLE);
        scrim.setVisibility(View.VISIBLE);
        animatePanel(true);
    }

    private void closePanel() {
        if (!panelOpen) return;
        panelOpen = false;
        animatePanel(false);
    }

    private void animatePanel(boolean opening) {
        int token = ++panelAnimationToken;
        long startTime = SystemClock.uptimeMillis();
        long duration = opening ? PANEL_OPEN_MS : PANEL_CLOSE_MS;
        float startX = sidePanel.getTranslationX();
        float endX = opening ? 0f : -panelWidth();
        float startAlpha = scrim.getAlpha();
        float endAlpha = opening ? 1f : 0f;
        DecelerateInterpolator openInterpolator = new DecelerateInterpolator(1.2f);
        AccelerateInterpolator closeInterpolator = new AccelerateInterpolator(1.2f);

        Runnable frame = new Runnable() {
            @Override
            public void run() {
                if (token != panelAnimationToken) return;

                float elapsed = Math.min(1f, (SystemClock.uptimeMillis() - startTime) / (float) duration);
                float eased = opening
                        ? openInterpolator.getInterpolation(elapsed)
                        : closeInterpolator.getInterpolation(elapsed);
                sidePanel.setTranslationX(startX + ((endX - startX) * eased));
                scrim.setAlpha(startAlpha + ((endAlpha - startAlpha) * eased));

                if (elapsed < 1f) {
                    sidePanel.postOnAnimation(this);
                } else if (!opening && !panelOpen) {
                    scrim.setVisibility(View.GONE);
                    sidePanel.setVisibility(View.GONE);
                }
            }
        };
        sidePanel.postOnAnimation(frame);
    }

    private void openProjectPanel() {
        hideKeyboard();
        closePanel();
        closeAiChat();
        projectPanelOpen = true;
        if (projectPanel == null || projectScrim == null) return;
        refreshProjectPanel();
        projectPanel.setVisibility(View.VISIBLE);
        projectScrim.setVisibility(View.VISIBLE);
        animateProjectPanel(true);
    }

    private void closeProjectPanel() {
        if (!projectPanelOpen) return;
        projectPanelOpen = false;
        animateProjectPanel(false);
    }

    private void animateProjectPanel(boolean opening) {
        if (projectPanel == null || projectScrim == null) return;
        long startTime = SystemClock.uptimeMillis();
        long duration = opening ? PANEL_OPEN_MS : PANEL_CLOSE_MS;
        float startX = projectPanel.getTranslationX();
        float endX = opening ? 0f : -projectPanelWidth();
        float startAlpha = projectScrim.getAlpha();
        float endAlpha = opening ? 1f : 0f;
        DecelerateInterpolator openInterpolator = new DecelerateInterpolator(1.2f);
        AccelerateInterpolator closeInterpolator = new AccelerateInterpolator(1.2f);

        Runnable frame = new Runnable() {
            @Override
            public void run() {
                float elapsed = Math.min(1f, (SystemClock.uptimeMillis() - startTime) / (float) duration);
                float eased = opening
                        ? openInterpolator.getInterpolation(elapsed)
                        : closeInterpolator.getInterpolation(elapsed);
                projectPanel.setTranslationX(startX + ((endX - startX) * eased));
                projectScrim.setAlpha(startAlpha + ((endAlpha - startAlpha) * eased));
                if (elapsed < 1f) {
                    projectPanel.postOnAnimation(this);
                } else if (!opening && !projectPanelOpen) {
                    projectScrim.setVisibility(View.GONE);
                    projectPanel.setVisibility(View.GONE);
                }
            }
        };
        projectPanel.postOnAnimation(frame);
    }

    private void openAiChat() {
        hideKeyboard();
        closePanel();
        closeProjectPanel();
        aiChatOpen = true;
        if (aiChatPanel == null || aiChatScrim == null) return;
        renderAiChatMessages();
        aiChatPanel.setVisibility(View.VISIBLE);
        aiChatScrim.setVisibility(View.VISIBLE);
        animateAiChat(true);
    }

    private void closeAiChat() {
        if (!aiChatOpen) return;
        aiChatOpen = false;
        animateAiChat(false);
    }

    private void animateAiChat(boolean opening) {
        if (aiChatPanel == null || aiChatScrim == null) return;
        long startTime = SystemClock.uptimeMillis();
        long duration = opening ? PANEL_OPEN_MS : PANEL_CLOSE_MS;
        float startX = aiChatPanel.getTranslationX();
        float endX = opening ? 0f : aiChatPanelWidth();
        float startAlpha = aiChatScrim.getAlpha();
        float endAlpha = opening ? 1f : 0f;
        DecelerateInterpolator openInterpolator = new DecelerateInterpolator(1.2f);
        AccelerateInterpolator closeInterpolator = new AccelerateInterpolator(1.2f);

        Runnable frame = new Runnable() {
            @Override
            public void run() {
                float elapsed = Math.min(1f, (SystemClock.uptimeMillis() - startTime) / (float) duration);
                float eased = opening
                        ? openInterpolator.getInterpolation(elapsed)
                        : closeInterpolator.getInterpolation(elapsed);
                aiChatPanel.setTranslationX(startX + ((endX - startX) * eased));
                aiChatScrim.setAlpha(startAlpha + ((endAlpha - startAlpha) * eased));
                if (elapsed < 1f) {
                    aiChatPanel.postOnAnimation(this);
                } else if (!opening && !aiChatOpen) {
                    aiChatScrim.setVisibility(View.GONE);
                    aiChatPanel.setVisibility(View.GONE);
                }
            }
        };
        aiChatPanel.postOnAnimation(frame);
    }

    private void sendAiChatPrompt() {
        if (aiChatSending || aiChatInput == null) return;
        String prompt = aiChatInput.getText().toString().trim();
        if (prompt.isEmpty()) return;
        if (aiRecentChatsVisible) {
            aiRecentChatsVisible = false;
            renderAiChatMessages();
        }
        ensureAiChatSession();
        boolean shouldNameChat = aiChatHistory.isEmpty()
                && (aiCurrentChatTitle == null || aiCurrentChatTitle.trim().isEmpty() || "New chat".equals(aiCurrentChatTitle));
        saveEditorState();
        aiChatInput.setText("");
        int editedIndex = aiEditingMessageIndex;
        if (editedIndex >= 0) {
            while (aiChatHistory.size() > editedIndex) {
                aiChatHistory.remove(aiChatHistory.size() - 1);
            }
            aiEditingMessageIndex = -1;
            aiEditingOriginalText = "";
            refreshAiEditModeUi();
        }
        addAiChatMessage("user", prompt);
        if (shouldNameChat) requestAiChatTitle(prompt);
        beginAiAssistantRequest(prompt);
    }

    private void beginAiAssistantRequest(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) return;
        setAiChatStatus("evaluating");
        resetAiFlow();
        aiChatSending = true;
        updateAiSendState();
        aiThinkingVisible = false;
        int thinkingToken = ++aiThinkingToken;
        final int[] assistantIndex = new int[]{-1};
        final boolean[] receivedPartial = new boolean[]{false};
        if (aiChatMessages != null) {
            aiChatMessages.postDelayed(() -> {
                if (aiChatSending && thinkingToken == aiThinkingToken && assistantIndex[0] < 0) {
                    aiThinkingVisible = true;
                    renderAiChatMessages();
                }
            }, 1000);
        }

        new Thread(() -> {
            String response;
            try {
                response = runAiAgentConversation(prompt, partial -> {
                    final String partialText = sanitizeAiAssistantText(partial);
                    runOnUiThread(() -> {
                    if (partialText.isEmpty()) return;
                    receivedPartial[0] = true;
                    aiThinkingVisible = false;
                    if (assistantIndex[0] < 0) {
                        assistantIndex[0] = addAiChatMessage("assistant", partialText);
                    } else {
                        updateAiChatMessage(assistantIndex[0], partialText);
                    }
                    });
                });
            } catch (Exception e) {
                response = "AI chat failed: " + aiChatErrorText(e);
            }
            final String finalResponse = response;
            runOnUiThread(() -> {
                aiChatSending = false;
                aiThinkingVisible = false;
                aiThinkingToken++;
                setAiChatStatus("ready");
                updateAiSendState();
                String visibleResponse = sanitizeAiAssistantText(finalResponse);
                if (!visibleResponse.isEmpty()) {
                    if (assistantIndex[0] < 0) {
                        addAiChatMessage("assistant", visibleResponse);
                    } else if (receivedPartial[0] && aiFeedVisible) {
                        addAiChatMessage("assistant", visibleResponse);
                    } else {
                        updateAiChatMessage(assistantIndex[0], visibleResponse);
                    }
                }
            });
        }, "andropy-ai-chat").start();
    }

    private void updateAiSendState() {
        if (aiSendButton != null) aiSendButton.setReplying(aiChatSending);
    }

    private interface AiStreamCallback {
        void onPartial(String text);
    }

    private int addAiChatMessage(String role, String text) {
        ensureAiChatSession();
        aiChatHistory.add(new AiChatMessage(role, text == null ? "" : text));
        if (aiChatHistory.size() > 80) aiChatHistory.remove(0);
        saveAiChatSession();
        renderAiChatMessages();
        return aiChatHistory.size() - 1;
    }

    private void updateAiChatMessage(int index, String text) {
        if (index < 0 || index >= aiChatHistory.size()) return;
        aiChatHistory.get(index).text = text == null ? "" : text;
        saveAiChatSession();
        renderAiChatMessages();
    }

    private void addAiStatusMessage(String text) {
        addAiChatMessage("status", text);
    }

    private void toggleRecentChatsView() {
        aiRecentChatsVisible = !aiRecentChatsVisible;
        loadAiRecentChats();
        renderAiChatMessages();
    }

    private void refreshAiHeaderMode() {
        updateAiChangeButtons();
        if (aiChatTitleText == null) return;
        ViewGroup.LayoutParams raw = aiChatTitleText.getLayoutParams();
        if (raw instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) raw;
            if (aiRecentChatsVisible) {
                params.setMargins(dp(58), dp(6), dp(58), 0);
            } else {
                params.setMargins(dp(104), dp(6), dp(152), 0);
            }
            aiChatTitleText.setLayoutParams(params);
        }
    }

    private void renderRecentChatsView() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(4), dp(4), dp(4), dp(10));

        aiRecentSearchInput = new EditText(this);
        aiRecentSearchInput.setSingleLine(true);
        aiRecentSearchInput.setHint("Search recent chats");
        aiRecentSearchInput.setHintTextColor(Color.rgb(142, 148, 160));
        aiRecentSearchInput.setTextColor(TEXT);
        aiRecentSearchInput.setTextSize(14);
        aiRecentSearchInput.setPadding(dp(16), 0, dp(16), 0);
        aiRecentSearchInput.setBackground(rounded(Color.rgb(47, 50, 58), Color.TRANSPARENT, 0, 18));
        aiRecentSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderRecentChatRows(wrap, s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(48));
        searchParams.setMargins(0, 0, 0, dp(10));
        wrap.addView(aiRecentSearchInput, searchParams);

        renderRecentChatRows(wrap, "");
        aiChatMessages.addView(wrap, new LinearLayout.LayoutParams(-1, -2));
    }

    private void renderRecentChatRows(LinearLayout wrap, String query) {
        while (wrap.getChildCount() > 1) wrap.removeViewAt(1);
        String q = query == null ? "" : query.toLowerCase(Locale.US).trim();
        loadAiRecentChats();
        int shown = 0;
        for (AiRecentChat chat : aiRecentChats) {
            if (!q.isEmpty() && !chat.title.toLowerCase(Locale.US).contains(q)) continue;
            wrap.addView(aiRecentChatRow(chat), new LinearLayout.LayoutParams(-1, dp(58)));
            shown++;
        }
        if (shown == 0) {
            TextView empty = new TextView(this);
            empty.setText(q.isEmpty() ? "No recent chats yet" : "No matching chats");
            empty.setTextColor(Color.rgb(142, 148, 160));
            empty.setTextSize(13);
            empty.setGravity(Gravity.CENTER);
            wrap.addView(empty, new LinearLayout.LayoutParams(-1, dp(72)));
        }
    }

    private View aiRecentChatRow(AiRecentChat chat) {
        FrameLayout shell = new FrameLayout(this);
        shell.setBackground(rounded(chat.id.equals(aiCurrentChatId) ? Color.rgb(55, 60, 70) : Color.rgb(39, 42, 50),
                Color.TRANSPARENT, 0, 10));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(6), dp(96), dp(6));
        TextView title = new TextView(this);
        title.setText(chat.title);
        title.setTextColor(Color.rgb(230, 235, 244));
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setSingleLine(true);
        row.addView(title, new LinearLayout.LayoutParams(-1, dp(24)));
        TextView meta = new TextView(this);
        meta.setText(chat.updatedLabel);
        meta.setTextColor(Color.rgb(142, 148, 160));
        meta.setTextSize(11);
        meta.setSingleLine(true);
        row.addView(meta, new LinearLayout.LayoutParams(-1, dp(18)));
        shell.addView(row, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        actions.setAlpha(0f);
        actions.setVisibility(View.GONE);
        ImageButton archive = recentChatActionButton("ic_archive_24", Color.rgb(210, 216, 230), "Archive chat");
        archive.setOnClickListener(v -> {
            animateRecentChatRemoval(shell, () -> archiveAiRecentChat(chat.id));
        });
        ImageButton delete = recentChatActionButton("ic_delete_24", Color.rgb(255, 92, 103), "Delete chat");
        delete.setOnClickListener(v -> {
            animateRecentChatRemoval(shell, () -> deleteAiRecentChat(chat.id));
        });
        actions.addView(archive, new LinearLayout.LayoutParams(dp(38), dp(38)));
        actions.addView(delete, new LinearLayout.LayoutParams(dp(38), dp(38)));
        FrameLayout.LayoutParams actionParams = new FrameLayout.LayoutParams(dp(84), -1, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        actionParams.setMargins(0, 0, dp(8), 0);
        shell.addView(actions, actionParams);

        View.OnHoverListener hover = (v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_HOVER_ENTER || action == MotionEvent.ACTION_HOVER_MOVE) {
                setRecentChatActionsVisible(actions, true);
            } else if (action == MotionEvent.ACTION_HOVER_EXIT) {
                setRecentChatActionsVisible(actions, false);
            }
            return false;
        };
        shell.setOnHoverListener(hover);
        actions.setOnHoverListener(hover);
        shell.setOnLongClickListener(v -> {
            setRecentChatActionsVisible(actions, actions.getVisibility() != View.VISIBLE);
            return true;
        });
        shell.setOnClickListener(v -> openAiRecentChat(chat.id));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(58));
        params.setMargins(0, 0, 0, dp(7));
        shell.setLayoutParams(params);
        return shell;
    }

    private ImageButton recentChatActionButton(String iconName, int color, String description) {
        ImageButton button = new ImageButton(this);
        int icon = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (icon != 0) button.setImageResource(icon);
        button.setColorFilter(color);
        button.setPadding(dp(8), dp(8), dp(8), dp(8));
        button.setBackground(rounded(Color.argb(118, 24, 27, 34), Color.TRANSPARENT, 0, 18));
        button.setContentDescription(description);
        return button;
    }

    private void setRecentChatActionsVisible(View actions, boolean visible) {
        if (actions == null) return;
        actions.setVisibility(View.VISIBLE);
        actions.animate().cancel();
        actions.animate()
                .alpha(visible ? 1f : 0f)
                .setDuration(visible ? 90 : 140)
                .withEndAction(() -> {
                    if (!visible) actions.setVisibility(View.GONE);
                })
                .start();
    }

    private void animateRecentChatRemoval(View row, Runnable after) {
        if (row == null) {
            if (after != null) after.run();
            return;
        }
        row.setEnabled(false);
        row.setClickable(false);
        row.animate().cancel();
        ViewGroup parent = row.getParent() instanceof ViewGroup ? (ViewGroup) row.getParent() : null;
        int index = parent == null ? -1 : parent.indexOfChild(row);
        int travel = row.getHeight();
        ViewGroup.LayoutParams rawParams = row.getLayoutParams();
        if (rawParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) rawParams;
            travel += margins.topMargin + margins.bottomMargin;
        }
        if (travel <= 0) travel = dp(65);
        if (parent != null) {
            parent.setClipChildren(false);
            parent.setClipToPadding(false);
            for (int i = index + 1; i < parent.getChildCount(); i++) {
                View sibling = parent.getChildAt(i);
                sibling.animate().cancel();
                sibling.animate()
                        .translationY(-travel)
                        .setDuration(260)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }
        spawnRecentChatDust(row);
        row.animate()
                .alpha(0f)
                .scaleX(0.9f)
                .scaleY(0.58f)
                .translationX(dp(26))
                .setDuration(420)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    if (parent != null) {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            View sibling = parent.getChildAt(i);
                            sibling.animate().cancel();
                            sibling.setTranslationY(0f);
                        }
                    }
                    if (after != null) after.run();
                })
                .start();
    }

    private void spawnRecentChatDust(View row) {
        if (row == null || getWindow() == null) return;
        View decor = getWindow().getDecorView();
        if (!(decor instanceof FrameLayout)) return;
        FrameLayout overlay = (FrameLayout) decor;
        overlay.setClipChildren(false);
        overlay.setClipToPadding(false);
        int[] rowLocation = new int[2];
        int[] overlayLocation = new int[2];
        row.getLocationOnScreen(rowLocation);
        overlay.getLocationOnScreen(overlayLocation);
        int rowLeft = rowLocation[0] - overlayLocation[0];
        int rowTop = rowLocation[1] - overlayLocation[1];
        int width = Math.max(row.getWidth(), dp(260));
        int height = Math.max(row.getHeight(), dp(58));
        int[] colors = new int[]{
                Color.argb(255, 250, 252, 255),
                Color.argb(235, 188, 198, 224),
                Color.argb(250, 255, 92, 103),
                Color.argb(230, 255, 207, 83),
                Color.argb(220, 132, 220, 255)
        };
        for (int i = 0; i < 58; i++) {
            View dot = new View(this);
            int size = dp(2 + (i % 4));
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(colors[i % colors.length]);
            dot.setBackground(bg);
            int startX = rowLeft + dp(14) + ((width - dp(32)) * ((i * 37) % 100)) / 100;
            int startY = rowTop + dp(6) + ((height - dp(12)) * ((i * 53) % 100)) / 100;
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = startX;
            params.topMargin = startY;
            overlay.addView(dot, params);
            int direction = i % 3 == 0 ? -1 : 1;
            float xDrift = dp(direction * (22 + (i * 11) % 76));
            float yDrift = dp(-36 + (i * 17) % 82);
            dot.animate()
                    .translationX(xDrift)
                    .translationY(yDrift)
                    .alpha(0f)
                    .scaleX(0.05f)
                    .scaleY(0.05f)
                    .setStartDelay((i % 9) * 10L)
                    .setDuration(460 + (i % 7) * 45L)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> overlay.removeView(dot))
                    .start();
        }
    }

    private void archiveAiRecentChat(String id) {
        if (prefs == null || id == null || id.trim().isEmpty()) return;
        HashSet<String> archived = loadArchivedAiChatIds();
        archived.add(id);
        saveArchivedAiChatIds(archived);
        removeAiRecentChat(id);
        if (id.equals(aiCurrentChatId)) {
            createFreshAiChatSession();
        }
        aiRecentChatsVisible = true;
        renderAiChatMessages();
        Toast.makeText(this, "Archived chat", Toast.LENGTH_SHORT).show();
    }

    private void deleteAiRecentChat(String id) {
        if (prefs == null || id == null || id.trim().isEmpty()) return;
        removeAiRecentChat(id);
        HashSet<String> archived = loadArchivedAiChatIds();
        archived.remove(id);
        saveArchivedAiChatIds(archived);
        prefs.edit()
                .remove(aiChatHistoryKey(id))
                .remove(aiChatTitleKey(id))
                .remove(aiChatUpdatedKey(id))
                .remove(aiChatTitleRequestedKey(id))
                .apply();
        if (id.equals(aiCurrentChatId)) {
            createFreshAiChatSession();
        }
        aiRecentChatsVisible = true;
        renderAiChatMessages();
        Toast.makeText(this, "Deleted chat", Toast.LENGTH_SHORT).show();
    }

    private void removeAiRecentChat(String id) {
        loadAiRecentChats();
        for (int i = aiRecentChats.size() - 1; i >= 0; i--) {
            if (aiRecentChats.get(i).id.equals(id)) aiRecentChats.remove(i);
        }
        persistAiRecentChats();
    }

    private void ensureAiChatSession() {
        if (prefs == null) return;
        if (aiCurrentChatId == null || aiCurrentChatId.trim().isEmpty()) {
            aiCurrentChatId = prefs.getString("ai_current_chat_id", "");
        }
        if (aiCurrentChatId == null || aiCurrentChatId.trim().isEmpty()) {
            aiCurrentChatId = "chat-" + System.currentTimeMillis();
            aiCurrentChatTitle = "New chat";
            aiChatTitleRequested = false;
            prefs.edit().putString("ai_current_chat_id", aiCurrentChatId).apply();
            saveAiChatSession();
            updateAiChatTitleUi();
            return;
        }
        aiCurrentChatTitle = prefs.getString(aiChatTitleKey(aiCurrentChatId), "New chat");
        aiChatTitleRequested = prefs.getBoolean(aiChatTitleRequestedKey(aiCurrentChatId), false);
        if (aiChatHistory.isEmpty()) loadAiChatHistory(aiCurrentChatId);
        updateAiChatTitleUi();
    }

    private String currentAiChatTitle() {
        String title = aiCurrentChatTitle == null ? "" : aiCurrentChatTitle.trim();
        return title.isEmpty() ? "New chat" : title;
    }

    private void updateAiChatTitleUi() {
        if (aiChatTitleText != null && !aiRecentChatsVisible) {
            String title = aiCurrentChatTitle == null ? "" : aiCurrentChatTitle.trim();
            aiChatTitleText.setText(title.isEmpty() ? "New chat" : title);
        }
    }

    private void saveAiChatSession() {
        if (prefs == null || aiCurrentChatId == null || aiCurrentChatId.trim().isEmpty()) return;
        try {
            JSONArray messages = new JSONArray();
            for (AiChatMessage message : aiChatHistory) {
                messages.put(new JSONObject()
                        .put("role", message.role)
                        .put("text", message.text));
            }
            long now = System.currentTimeMillis();
            prefs.edit()
                    .putString(aiChatHistoryKey(aiCurrentChatId), messages.toString())
                    .putString(aiChatTitleKey(aiCurrentChatId), currentAiChatTitle())
                    .putLong(aiChatUpdatedKey(aiCurrentChatId), now)
                    .apply();
            upsertAiRecentChat(aiCurrentChatId, currentAiChatTitle(), now);
        } catch (Exception ignored) {
        }
    }

    private void upsertAiRecentChat(String id, String title, long updatedAt) throws Exception {
        if (loadArchivedAiChatIds().contains(id)) return;
        loadAiRecentChats();
        AiRecentChat existing = null;
        for (AiRecentChat chat : aiRecentChats) {
            if (chat.id.equals(id)) {
                existing = chat;
                break;
            }
        }
        if (existing == null) {
            aiRecentChats.add(0, new AiRecentChat(id, title, updatedAt));
        } else {
            existing.title = title;
            existing.updatedAt = updatedAt;
            aiRecentChats.remove(existing);
            aiRecentChats.add(0, existing);
        }
        while (aiRecentChats.size() > 30) aiRecentChats.remove(aiRecentChats.size() - 1);
        persistAiRecentChats();
    }

    private void persistAiRecentChats() {
        if (prefs == null) return;
        JSONArray index = new JSONArray();
        try {
            for (AiRecentChat chat : aiRecentChats) {
                index.put(new JSONObject().put("id", chat.id).put("title", chat.title).put("updatedAt", chat.updatedAt));
            }
        } catch (Exception ignored) {
        }
        prefs.edit().putString("ai_recent_chats", index.toString()).apply();
    }

    private void loadAiRecentChats() {
        aiRecentChats.clear();
        if (prefs == null) return;
        String raw = prefs.getString("ai_recent_chats", "[]");
        HashSet<String> archived = loadArchivedAiChatIds();
        try {
            JSONArray index = new JSONArray(raw);
            for (int i = 0; i < index.length(); i++) {
                JSONObject item = index.optJSONObject(i);
                if (item == null) continue;
                String id = item.optString("id", "");
                if (id.isEmpty()) continue;
                if (archived.contains(id)) continue;
                String title = item.optString("title", prefs.getString(aiChatTitleKey(id), "New chat"));
                long updatedAt = item.optLong("updatedAt", prefs.getLong(aiChatUpdatedKey(id), 0L));
                aiRecentChats.add(new AiRecentChat(id, title, updatedAt));
            }
        } catch (Exception ignored) {
        }
        aiRecentChats.sort((left, right) -> Long.compare(right.updatedAt, left.updatedAt));
    }

    private HashSet<String> loadArchivedAiChatIds() {
        HashSet<String> ids = new HashSet<>();
        if (prefs == null) return ids;
        try {
            JSONArray array = new JSONArray(prefs.getString("ai_archived_chats", "[]"));
            for (int i = 0; i < array.length(); i++) {
                String id = array.optString(i, "");
                if (!id.trim().isEmpty()) ids.add(id);
            }
        } catch (Exception ignored) {
        }
        return ids;
    }

    private void saveArchivedAiChatIds(HashSet<String> ids) {
        if (prefs == null) return;
        JSONArray array = new JSONArray();
        if (ids != null) {
            ArrayList<String> sorted = new ArrayList<>(ids);
            sorted.sort(String::compareTo);
            for (String id : sorted) array.put(id);
        }
        prefs.edit().putString("ai_archived_chats", array.toString()).apply();
    }

    private void openAiRecentChat(String id) {
        if (id == null || id.trim().isEmpty()) return;
        aiCurrentChatId = id;
        prefs.edit().putString("ai_current_chat_id", id).apply();
        aiCurrentChatTitle = prefs.getString(aiChatTitleKey(id), "New chat");
        aiChatTitleRequested = prefs.getBoolean(aiChatTitleRequestedKey(id), false);
        aiChatHistory.clear();
        loadAiChatHistory(id);
        resetAiFlow();
        aiRecentChatsVisible = false;
        renderAiChatMessages();
        updateAiChatTitleUi();
    }

    private void startNewAiChat() {
        if (aiChatSending) return;
        saveAiChatSession();
        createFreshAiChatSession();
        refreshAiEditModeUi();
        updateAiChatTitleUi();
        renderAiChatMessages();
    }

    private void createFreshAiChatSession() {
        aiCurrentChatId = "chat-" + System.currentTimeMillis();
        aiCurrentChatTitle = "New chat";
        aiChatTitleRequested = false;
        aiChatHistory.clear();
        resetAiFlow();
        aiRecentChatsVisible = false;
        aiEditingMessageIndex = -1;
        aiEditingOriginalText = "";
        prefs.edit()
                .putString("ai_current_chat_id", aiCurrentChatId)
                .putString(aiChatTitleKey(aiCurrentChatId), aiCurrentChatTitle)
                .putBoolean(aiChatTitleRequestedKey(aiCurrentChatId), false)
                .apply();
    }

    private void loadAiChatHistory(String id) {
        if (prefs == null || id == null || id.trim().isEmpty()) return;
        try {
            JSONArray messages = new JSONArray(prefs.getString(aiChatHistoryKey(id), "[]"));
            for (int i = 0; i < messages.length(); i++) {
                JSONObject item = messages.optJSONObject(i);
                if (item == null) continue;
                aiChatHistory.add(new AiChatMessage(item.optString("role", "assistant"), item.optString("text", "")));
            }
        } catch (Exception ignored) {
        }
    }

    private void requestAiChatTitle(String firstPrompt) {
        if (aiChatTitleRequested) return;
        aiChatTitleRequested = true;
        prefs.edit().putBoolean(aiChatTitleRequestedKey(aiCurrentChatId), true).apply();
        new Thread(() -> {
            String title = "";
            try {
                title = fetchAiChatTitle(firstPrompt);
            } catch (Exception ignored) {
            }
            if (title.trim().isEmpty()) title = fallbackAiChatTitle(firstPrompt);
            final String finalTitle = cleanAiChatTitle(title);
            runOnUiThread(() -> {
                aiCurrentChatTitle = finalTitle.isEmpty() ? "New chat" : finalTitle;
                prefs.edit().putString(aiChatTitleKey(aiCurrentChatId), aiCurrentChatTitle).apply();
                saveAiChatSession();
                updateAiChatTitleUi();
                if (aiRecentChatsVisible) renderAiChatMessages();
            });
        }, "andropy-ai-title").start();
    }

    private String formatAiRecentTime(long updatedAt) {
        if (updatedAt <= 0L) return "recent";
        long age = Math.max(0L, System.currentTimeMillis() - updatedAt);
        long minute = 60_000L;
        long hour = 60L * minute;
        long day = 24L * hour;
        if (age < minute) return "just now";
        if (age < hour) return (age / minute) + "m ago";
        if (age < day) return (age / hour) + "h ago";
        return new SimpleDateFormat("MMM d", Locale.US).format(new Date(updatedAt));
    }

    private String aiChatHistoryKey(String id) { return "ai_chat_history_" + id; }
    private String aiChatTitleKey(String id) { return "ai_chat_title_" + id; }
    private String aiChatUpdatedKey(String id) { return "ai_chat_updated_" + id; }
    private String aiChatTitleRequestedKey(String id) { return "ai_chat_title_requested_" + id; }

    private String fetchAiChatTitle(String firstPrompt) throws Exception {
        AutocompleteProvider provider = currentProvider();
        if ("local".equals(provider.id) || (provider.needsKey && providerApiKey(provider).isEmpty())) return "";
        String prompt = "Name this IDE chat in 2 to 5 words. No quotes. No punctuation unless needed.\nUser request: " + firstPrompt;
        if ("cohere".equals(provider.id)) return fetchCohereTitle(provider, prompt);
        if ("gemini".equals(provider.id)) return fetchGeminiTitle(provider, prompt);
        if ("groq".equals(provider.id) || "openai".equals(provider.id)
                || "openrouter".equals(provider.id) || "mistral".equals(provider.id)) {
            return fetchOpenAiTitle(provider, prompt);
        }
        return "";
    }

    private String fetchOpenAiTitle(AutocompleteProvider provider, String prompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.2);
        body.put("max_tokens", 16);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "Return only a short chat title."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        body.put("messages", messages);
        AiHttpResponse result = postAiJson(provider, body, 8000);
        if (result.status < 200 || result.status >= 300) return "";
        JSONObject json = new JSONObject(result.body);
        JSONArray choices = json.optJSONArray("choices");
        JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
        JSONObject message = first == null ? null : first.optJSONObject("message");
        return message == null ? first == null ? "" : first.optString("text", "") : message.optString("content", "");
    }

    private String fetchCohereTitle(AutocompleteProvider provider, String prompt) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.2);
        body.put("max_tokens", 16);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        body.put("messages", messages);
        HttpURLConnection connection = (HttpURLConnection) new URL(providerEndpoint(provider)).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        String apiKey = providerApiKey(provider);
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) return "";
        return cohereMessageText(new JSONObject(response).optJSONObject("message"));
    }

    private String fetchGeminiTitle(AutocompleteProvider provider, String prompt) throws Exception {
        String apiKey = providerApiKey(provider);
        String base = providerEndpoint(provider).replaceAll("/+$", "");
        String model = providerModel(provider);
        if (model.startsWith("models/")) model = model.substring("models/".length());
        String url = base + "/" + model + ":generateContent?key=" + URLEncoder.encode(apiKey, "UTF-8");
        JSONObject body = new JSONObject();
        JSONArray parts = new JSONArray().put(new JSONObject().put("text", prompt));
        body.put("contents", new JSONArray().put(new JSONObject().put("parts", parts)));
        body.put("generationConfig", new JSONObject().put("temperature", 0.2).put("maxOutputTokens", 16));
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) return "";
        return geminiResponseText(new JSONObject(response));
    }

    private String cleanAiChatTitle(String title) {
        String clean = title == null ? "" : title.replace('\n', ' ').replace('\r', ' ').trim();
        clean = clean.replaceAll("^['\"`]+|['\"`]+$", "").replaceAll("\\s+", " ");
        clean = clean.replaceAll("[.。]+$", "");
        if (clean.length() > 34) clean = clean.substring(0, 34).trim();
        return clean.isEmpty() ? "New chat" : clean;
    }

    private String fallbackAiChatTitle(String prompt) {
        String clean = prompt == null ? "" : prompt.replaceAll("[^A-Za-z0-9_+#. ]+", " ").replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) return "New chat";
        String[] words = clean.split(" ");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < words.length && i < 5; i++) {
            if (words[i].length() > 16) continue;
            if (out.length() > 0) out.append(' ');
            out.append(words[i]);
        }
        return out.length() == 0 ? "New chat" : out.toString();
    }


    private void renderAiChatMessages() {
        if (aiChatMessages == null) return;
        aiChatMessages.removeAllViews();
        if (aiChatTitleText != null) aiChatTitleText.setText(aiRecentChatsVisible ? "Recent chats" : currentAiChatTitle());
        refreshAiHeaderMode();
        if (aiRecentChatsVisible) {
            renderRecentChatsView();
            return;
        }
        boolean feedInserted = false;
        int visibleCount = aiEditingMessageIndex >= 0
                ? Math.max(0, Math.min(aiEditingMessageIndex, aiChatHistory.size()))
                : aiChatHistory.size();
        if (!aiChatHistory.isEmpty()) {
            for (int i = 0; i < visibleCount; i++) {
                aiChatMessages.addView(aiChatBubble(aiChatHistory.get(i), i));
                if (aiFeedVisible && aiFeedInsertAfter == i + 1) {
                    aiChatMessages.addView(aiExecutionView());
                    feedInserted = true;
                }
            }
        }
        if (aiFeedVisible && !feedInserted && aiEditingMessageIndex < 0) aiChatMessages.addView(aiExecutionView());
        if (aiThinkingVisible) aiChatMessages.addView(new AiThinkingView(this));
        if (aiChatScroll != null) aiChatScroll.post(() -> aiChatScroll.fullScroll(View.FOCUS_DOWN));
    }

    private View aiExecutionView() {
        compactAiFlowSteps();
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(4), dp(3), dp(10), dp(3));
        if (aiFlowSteps.isEmpty()) {
            view.addView(aiFlowText(aiFeedExecuting ? "Evaluating..." : aiFeedSummary, aiFeedExecuting, false),
                    new LinearLayout.LayoutParams(-1, -2));
        } else {
            for (AiFlowStep step : aiFlowSteps) {
                view.addView(aiFlowText(step.title, step.active, step.finished), new LinearLayout.LayoutParams(-1, -2));
                for (String detail : step.details) {
                    view.addView(aiFlowDetailText(detail), new LinearLayout.LayoutParams(-1, -2));
                }
                if (step.editPreview != null) {
                    view.addView(aiEditPreviewView(step.editPreview), new LinearLayout.LayoutParams(-1, -2));
                }
            }
        }
        if (aiToolFeed.length() > 0) {
            view.setClickable(true);
            view.setOnClickListener(v -> {
                aiFeedExpanded = !aiFeedExpanded;
                renderAiChatMessages();
            });
            if (aiFeedExpanded) {
                view.addView(aiTerminalFeedView(), new LinearLayout.LayoutParams(-1, -2));
            }
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), dp(18), dp(2));
        view.setLayoutParams(params);
        return view;
    }

    private TextView aiTerminalFeedView() {
        TextView feed = new TextView(this);
        feed.setText(aiToolFeed.toString().trim());
        feed.setTextColor(Color.rgb(192, 214, 202));
        feed.setTextSize(10);
        feed.setTypeface(Typeface.MONOSPACE);
        feed.setSingleLine(false);
        feed.setTextIsSelectable(true);
        feed.setPadding(dp(9), dp(7), dp(9), dp(7));
        feed.setBackground(rounded(AI_FEED_BG, AI_FEED_BORDER, 1, 7));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(dp(8), dp(6), 0, dp(6));
        feed.setLayoutParams(params);
        return feed;
    }

    private void compactAiFlowSteps() {
        if (aiFlowSteps.size() < 2) return;
        for (int i = 1; i < aiFlowSteps.size(); i++) {
            AiFlowStep previous = aiFlowSteps.get(i - 1);
            AiFlowStep current = aiFlowSteps.get(i);
            if (previous.kind.isEmpty() || !previous.kind.equals(current.kind)) continue;
            previous.title = current.title;
            previous.active = current.active;
            previous.finished = previous.finished || current.finished;
            for (String detail : current.details) {
                if (!previous.details.contains(detail)) previous.details.add(detail);
            }
            if (current.editPreview != null) previous.editPreview = current.editPreview;
            aiFlowSteps.remove(i);
            i--;
        }
    }

    private TextView aiFlowText(String text, boolean active, boolean finished) {
        TextView view = active ? new AiShineTextView(this) : new TextView(this);
        view.setText(text == null || text.isEmpty() ? "Evaluating..." : text);
        view.setTextSize(13);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextColor(active ? Color.rgb(223, 213, 255)
                : finished ? Color.rgb(207, 214, 232) : Color.rgb(170, 176, 190));
        view.setPadding(0, dp(3), 0, dp(1));
        return view;
    }

    private TextView aiFlowDetailText(String text) {
        TextView view = new TextView(this);
        view.setText(text == null ? "" : text);
        view.setTextSize(11);
        view.setTypeface(Typeface.DEFAULT);
        view.setTextColor(Color.rgb(142, 148, 160));
        view.setSingleLine(false);
        view.setPadding(dp(14), 0, dp(4), dp(2));
        return view;
    }

    private View aiEditPreviewView(AiEditPreview preview) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(8), dp(7), dp(8), dp(7));
        box.setBackground(rounded(AI_EDIT_PREVIEW_BG, AI_EDIT_PREVIEW_BORDER, 1, 8));
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(-1, -2);
        boxParams.setMargins(dp(8), dp(6), 0, dp(6));
        box.setLayoutParams(boxParams);

        TextView title = new TextView(this);
        title.setText(preview.fileName);
        title.setTextColor(Color.rgb(207, 214, 232));
        title.setTextSize(11);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setPadding(0, 0, 0, dp(5));
        box.addView(title, new LinearLayout.LayoutParams(-1, -2));

        int count = Math.min(preview.rows.size(), 12);
        for (int i = 0; i < count; i++) {
            box.addView(aiEditPreviewRow(preview.rows.get(i)), new LinearLayout.LayoutParams(-1, -2));
        }
        if (preview.rows.size() > count) {
            TextView more = new TextView(this);
            more.setText("     | ...");
            more.setTextColor(Color.rgb(126, 132, 144));
            more.setTextSize(11);
            more.setTypeface(Typeface.MONOSPACE);
            box.addView(more, new LinearLayout.LayoutParams(-1, -2));
        }
        return box;
    }

    private View aiEditPreviewRow(AiEditPreviewRow row) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER_VERTICAL);
        int bg = row.kind > 0 ? AI_EDIT_PREVIEW_ADD : row.kind < 0 ? AI_EDIT_PREVIEW_DEL : Color.TRANSPARENT;
        line.setBackgroundColor(bg);
        line.setPadding(dp(4), dp(1), dp(4), dp(1));

        TextView number = new TextView(this);
        String sign = row.kind > 0 ? "+" : row.kind < 0 ? "-" : " ";
        number.setText(sign + String.format(Locale.US, "%3d", Math.max(1, row.lineNumber)));
        number.setTextColor(row.kind > 0 ? Color.rgb(106, 224, 143)
                : row.kind < 0 ? Color.rgb(245, 106, 116) : Color.rgb(142, 150, 164));
        number.setTextSize(11);
        number.setTypeface(Typeface.MONOSPACE);
        line.addView(number, new LinearLayout.LayoutParams(dp(46), -2));

        TextView sep = new TextView(this);
        sep.setText("|");
        sep.setTextColor(Color.rgb(91, 98, 112));
        sep.setTextSize(11);
        sep.setTypeface(Typeface.MONOSPACE);
        line.addView(sep, new LinearLayout.LayoutParams(dp(10), -2));

        TextView code = new TextView(this);
        code.setText(row.text);
        code.setTextColor(Color.rgb(222, 228, 238));
        code.setTextSize(11);
        code.setSingleLine(true);
        code.setTypeface(Typeface.MONOSPACE);
        line.addView(code, new LinearLayout.LayoutParams(0, -2, 1));
        return line;
    }

    private void resetAiFlow() {
        aiFlowSteps.clear();
        aiFeedVisible = false;
        aiFeedExpanded = false;
        aiFeedExecuting = false;
        aiFeedSummary = "";
        aiFeedInsertAfter = -1;
        aiToolFeed.setLength(0);
        aiEditedFilesThisRun.clear();
    }

    private void beginAiFeed(String line) {
        aiToolFeed.setLength(0);
        aiFeedVisible = true;
        aiFeedExpanded = false;
        aiFeedExecuting = true;
        aiFeedSummary = line == null || line.isEmpty() ? "Executing..." : line;
        aiFeedInsertAfter = aiChatHistory.size();
        if (aiFlowSteps.isEmpty()) addAiFlowStep(aiFeedSummary, true);
        renderAiChatMessages();
    }

    private void finishAiFeed(String summary) {
        aiFeedVisible = true;
        aiFeedExpanded = false;
        aiFeedExecuting = false;
        aiFeedSummary = summary == null || summary.isEmpty() ? "Executed" : summary;
        finishActiveAiFlowStep();
        renderAiChatMessages();
    }

    private void addAiFeedLine(String line) {
        if (!aiFeedVisible) aiFeedVisible = true;
        aiToolFeed.append(line == null ? "" : line).append('\n');
        String[] lines = aiToolFeed.toString().split("\n");
        if (lines.length > 18) {
            aiToolFeed.setLength(0);
            for (int i = Math.max(0, lines.length - 18); i < lines.length; i++) {
                if (!lines[i].isEmpty()) aiToolFeed.append(lines[i]).append('\n');
            }
        }
        renderAiChatMessages();
    }

    private void addAiFlowStep(String title, boolean active) {
        addAiFlowStep(title, active, "");
    }

    private void addAiFlowStep(String title, boolean active, String kind) {
        if (!aiFeedVisible) {
            aiFeedVisible = true;
            aiFeedInsertAfter = aiChatHistory.size();
        }
        aiThinkingVisible = false;
        aiThinkingToken++;
        AiFlowStep last = activeAiFlowStep();
        String cleanKind = kind == null ? "" : kind;
        if (last != null && !cleanKind.isEmpty() && cleanKind.equals(last.kind)) {
            last.title = title == null || title.trim().isEmpty() ? last.title : title.trim();
            last.active = active;
            last.finished = !active;
            renderAiChatMessages();
            return;
        }
        finishActiveAiFlowStep();
        AiFlowStep step = new AiFlowStep(title, active, cleanKind);
        aiFlowSteps.add(step);
        renderAiChatMessages();
    }

    private void addAiFlowDetail(String detail) {
        if (aiFlowSteps.isEmpty()) addAiFlowStep("Reading files", true);
        AiFlowStep step = aiFlowSteps.get(aiFlowSteps.size() - 1);
        String clean = cleanAiFlowDetail(detail);
        if (!clean.isEmpty() && !step.details.contains(clean) && !aiFlowHasDetail(step.kind, clean)) {
            step.details.add(clean);
        }
        renderAiChatMessages();
    }

    private boolean aiFlowHasDetail(String kind, String cleanDetail) {
        if (kind == null || kind.isEmpty() || cleanDetail == null || cleanDetail.isEmpty()) return false;
        for (int i = 0; i < Math.max(0, aiFlowSteps.size() - 1); i++) {
            AiFlowStep step = aiFlowSteps.get(i);
            if (kind.equals(step.kind) && step.details.contains(cleanDetail)) return true;
        }
        return false;
    }

    private String cleanAiFlowDetail(String detail) {
        if (detail == null) return "";
        String clean = detail.trim()
                .replaceFirst("^[|\\\\/`'\"\\-\\s]+", "")
                .replaceFirst("^[A-Za-z_]+=", "")
                .trim();
        if (clean.length() > 54) clean = new File(clean).getName();
        return clean;
    }

    private void finishActiveAiFlowStep() {
        if (aiFlowSteps.isEmpty()) return;
        AiFlowStep step = aiFlowSteps.get(aiFlowSteps.size() - 1);
        step.active = false;
        step.finished = true;
    }

    private AiFlowStep activeAiFlowStep() {
        if (aiFlowSteps.isEmpty()) return null;
        return aiFlowSteps.get(aiFlowSteps.size() - 1);
    }

    private void setAiChatStatus(String status) {
        if (aiChatStatusText == null) return;
        aiChatStatusText.setText(status);
        if ("evaluating".equals(status)) animateAiStatus(++aiStatusAnimationToken);
        else {
            aiStatusAnimationToken++;
            aiChatStatusText.setAlpha(1f);
            aiChatStatusText.setTextColor(AI_PURPLE);
        }
    }

    private void animateAiStatus(int token) {
        if (aiChatStatusText == null) return;
        long started = SystemClock.uptimeMillis();
        Runnable frame = new Runnable() {
            @Override
            public void run() {
                if (token != aiStatusAnimationToken || aiChatStatusText == null) return;
                float wave = (float) ((Math.sin((SystemClock.uptimeMillis() - started) / 180.0) + 1.0) * 0.5);
                aiChatStatusText.setAlpha(0.55f + wave * 0.45f);
                aiChatStatusText.setTextColor(Color.rgb(188, 142 + Math.round(wave * 45), 255));
                aiChatStatusText.postDelayed(this, 70);
            }
        };
        aiChatStatusText.post(frame);
    }

    private View aiChatBubble(AiChatMessage message, int index) {
        if ("assistant".equals(message.role)) return aiAssistantMessageView(message.text, index);
        if ("user".equals(message.role)) return aiUserMessageView(message, index);
        TextView view = new TextView(this);
        view.setText(message.text);
        if ("status".equals(message.role)) {
            view.setTextColor(Color.rgb(158, 164, 174));
            view.setTextSize(12);
            view.setGravity(Gravity.CENTER);
            view.setPadding(dp(8), dp(8), dp(8), dp(8));
            LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
            statusParams.setMargins(0, dp(6), dp(18), dp(6));
            view.setLayoutParams(statusParams);
            return view;
        }
        view.setTextSize("tool".equals(message.role) ? 11 : 13);
        view.setTextColor("tool".equals(message.role) ? Color.rgb(190, 214, 198) : TEXT);
        view.setPadding(dp(10), dp(8), dp(10), dp(8));
        view.setTextIsSelectable(true);
        int bg = "user".equals(message.role) ? AI_USER_BUBBLE : AI_TOOL_BUBBLE;
        view.setBackground(rounded(bg, Color.rgb(73, 82, 96), 1, 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins("user".equals(message.role) ? dp(34) : 0, dp(4), "user".equals(message.role) ? 0 : dp(18), dp(4));
        view.setLayoutParams(params);
        return view;
    }

    private View aiUserMessageView(AiChatMessage message, int index) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(Gravity.RIGHT);
        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(-1, -2);
        wrapParams.setMargins(dp(34), dp(4), 0, dp(4));
        wrap.setLayoutParams(wrapParams);

        TextView bubble = new TextView(this);
        bubble.setText(message.text);
        bubble.setTextSize(13);
        bubble.setTextColor(TEXT);
        bubble.setPadding(dp(10), dp(8), dp(10), dp(8));
        bubble.setTextIsSelectable(true);
        bubble.setBackground(rounded(AI_USER_BUBBLE, Color.rgb(73, 82, 96), 1, 8));
        wrap.addView(bubble, new LinearLayout.LayoutParams(-1, -2));

        TextView edit = new TextView(this);
        edit.setText("✎");
        edit.setTextColor(Color.rgb(160, 168, 184));
        edit.setTextSize(12);
        edit.setGravity(Gravity.CENTER);
        edit.setPadding(dp(5), dp(2), dp(5), dp(1));
        edit.setBackgroundColor(Color.TRANSPARENT);
        edit.setOnClickListener(v -> beginAiMessageEdit(index));
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(28), dp(22));
        editParams.setMargins(0, dp(1), dp(4), 0);
        wrap.addView(edit, editParams);
        return wrap;
    }

    private void beginAiMessageEdit(int index) {
        if (aiChatSending || aiChatInput == null) return;
        if (index < 0 || index >= aiChatHistory.size()) return;
        AiChatMessage message = aiChatHistory.get(index);
        if (!"user".equals(message.role)) return;
        aiEditingMessageIndex = index;
        aiEditingOriginalText = message.text == null ? "" : message.text;
        aiChatInput.setText(aiEditingOriginalText);
        aiChatInput.setSelection(aiChatInput.getText().length());
        refreshAiEditModeUi();
        renderAiChatMessages();
        aiChatInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(aiChatInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void cancelAiMessageEdit() {
        aiEditingMessageIndex = -1;
        aiEditingOriginalText = "";
        if (aiChatInput != null) aiChatInput.setText("");
        refreshAiEditModeUi();
        renderAiChatMessages();
    }

    private void refreshAiEditModeUi() {
        boolean editing = aiEditingMessageIndex >= 0;
        if (aiEditBanner != null) aiEditBanner.setVisibility(editing ? View.VISIBLE : View.GONE);
        if (aiEditBannerText != null) aiEditBannerText.setText("✎ Editing message");
        if (aiChatInput != null) {
            aiChatInput.setHint(editing ? "" : "Ask questions to model");
            aiChatInput.setPadding(dp(18), editing ? dp(AI_EDIT_INPUT_TOP_PADDING_DP) : dp(13), dp(54), dp(34));
        }
        if (aiChatInputBox != null) aiChatInputBox.invalidate();
    }

    private View aiAssistantMessageView(String rawText, int index) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(4), dp(8), dp(10), dp(6));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), dp(18), dp(4));
        box.setLayoutParams(params);

        String text = sanitizeAiAssistantText(rawText);
        int cursor = 0;
        Pattern fence = Pattern.compile("(?s)```([A-Za-z0-9_+\\-.]*)\\n?(.*?)```");
        Matcher matcher = fence.matcher(text);
        boolean added = false;
        while (matcher.find()) {
            String before = text.substring(cursor, matcher.start()).trim();
            if (!before.isEmpty()) {
                box.addView(aiAssistantTextView(before), new LinearLayout.LayoutParams(-1, -2));
                added = true;
            }
            box.addView(aiCodeBlockView(matcher.group(1), matcher.group(2)), new LinearLayout.LayoutParams(-1, -2));
            added = true;
            cursor = matcher.end();
        }
        String tail = text.substring(cursor).trim();
        if (!tail.isEmpty() || !added) box.addView(aiAssistantTextView(tail), new LinearLayout.LayoutParams(-1, -2));
        if (shouldShowAiAssistantActions(index, rawText)) {
            box.addView(aiAssistantActionRow(rawText, index), new LinearLayout.LayoutParams(-1, dp(20)));
        }
        return box;
    }

    private boolean shouldShowAiAssistantActions(int index, String text) {
        String clean = sanitizeAiAssistantText(text);
        if (clean.trim().isEmpty()) return false;
        if (clean.startsWith("AI chat failed:")) return true;
        if (aiChatSending && index == aiChatHistory.size() - 1) return false;
        for (int i = index + 1; i < aiChatHistory.size(); i++) {
            String role = aiChatHistory.get(i).role;
            if ("assistant".equals(role)) return false;
            if ("user".equals(role)) break;
        }
        return true;
    }

    private View aiAssistantActionRow(String text, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), 0, 0, 0);
        ImageButton redo = aiAssistantActionButton("ic_refresh_24", "Regenerate response");
        redo.setOnClickListener(v -> regenerateAiResponse(index));
        row.addView(redo, new LinearLayout.LayoutParams(dp(20), dp(20)));
        ImageButton copy = aiAssistantActionButton("ic_content_copy_24", "Copy response");
        copy.setOnClickListener(v -> copyAiResponse(text));
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(dp(20), dp(20));
        copyParams.setMarginStart(dp(5));
        row.addView(copy, copyParams);
        return row;
    }

    private ImageButton aiAssistantActionButton(String iconName, String description) {
        ImageButton button = new ImageButton(this);
        int icon = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (icon != 0) button.setImageResource(icon);
        button.setColorFilter(Color.rgb(156, 164, 180));
        int pad = "ic_content_copy_24".equals(iconName) ? dp(5) : dp(4);
        button.setPadding(pad, pad, pad, pad);
        button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setContentDescription(description);
        return button;
    }

    private void copyAiResponse(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("model response", sanitizeAiAssistantText(text)));
            Toast.makeText(this, "Copied response", Toast.LENGTH_SHORT).show();
        }
    }

    private void regenerateAiResponse(int assistantIndex) {
        if (aiChatSending || assistantIndex < 0 || assistantIndex >= aiChatHistory.size()) return;
        int userIndex = -1;
        for (int i = assistantIndex - 1; i >= 0; i--) {
            if ("user".equals(aiChatHistory.get(i).role)) {
                userIndex = i;
                break;
            }
        }
        if (userIndex < 0) {
            Toast.makeText(this, "No prompt found", Toast.LENGTH_SHORT).show();
            return;
        }
        String prompt = aiChatHistory.get(userIndex).text;
        while (aiChatHistory.size() > assistantIndex) {
            aiChatHistory.remove(aiChatHistory.size() - 1);
        }
        saveAiChatSession();
        renderAiChatMessages();
        beginAiAssistantRequest(prompt);
    }

    private String sanitizeAiAssistantText(String rawText) {
        if (rawText == null) return "";
        String text = rawText.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (text.isEmpty()) return "";
        if (looksLikeProviderResponseJson(text)) {
            return extractProviderResponseText(text);
        }
        if (looksLikeLeakedToolJson(text)) {
            return extractMessageFromLeakedToolJson(text);
        }
        String[] lines = text.split("\\n");
        ArrayList<String> kept = new ArrayList<>();
        int skippedCodeLines = 0;
        for (String raw : lines) {
            String line = raw == null ? "" : raw;
            if (looksLikeEchoedSourceLine(line)) {
                skippedCodeLines++;
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.equals("IDE context:") || trimmed.equals("excerpt:") || trimmed.startsWith("file=")
                    || trimmed.startsWith("path=") || trimmed.startsWith("project=") || trimmed.startsWith("opened=")
                    || trimmed.startsWith("cursor=")) {
                skippedCodeLines++;
                continue;
            }
            kept.add(line);
        }
        String cleaned = String.join("\n", kept).trim();
        cleaned = cleaned.replaceAll("(?i)(I will look for files in this folder then analy[sz]e what's wrong\\.\\s*){2,}",
                "I will look through the files and fix the issue. ");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();
        if (cleaned.isEmpty() && skippedCodeLines > 0) return "I read the current file and found source code that should be handled as an edit, not pasted into chat.";
        return cleaned;
    }

    private boolean looksLikeProviderResponseJson(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.US);
        return lower.contains("\"choices\"") && lower.contains("\"message\"")
                && (lower.contains("\"assistant\"") || lower.contains("\"role\"") || lower.contains("\"model\""));
    }

    private String extractProviderResponseText(String text) {
        try {
            String objectText = text.trim();
            int start = objectText.indexOf('{');
            int end = objectText.lastIndexOf('}');
            if (start >= 0 && end > start) objectText = objectText.substring(start, end + 1);
            JSONObject json = new JSONObject(objectText);
            JSONArray choices = json.optJSONArray("choices");
            JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
            JSONObject message = first == null ? null : first.optJSONObject("message");
            String content = message == null ? "" : message.optString("content", "").trim();
            if (!content.isEmpty() && !content.equals(text)) return sanitizeAiAssistantText(content);
        } catch (Exception ignored) {
        }
        return "";
    }

    private boolean looksLikeLeakedToolJson(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.US);
        return (lower.contains("\"actions\"") || lower.contains("\"type\"") || lower.contains("\"search_text\"")
                || lower.contains("\"replace_text\"") || lower.contains("\\n"))
                && (lower.contains("edit_file") || lower.contains("create_file") || lower.contains("read_file"));
    }

    private String extractMessageFromLeakedToolJson(String text) {
        String message = "";
        try {
            int messageKey = text.indexOf("\"message\"");
            if (messageKey >= 0) {
                int colon = text.indexOf(':', messageKey);
                int quote = text.indexOf('"', colon + 1);
                if (colon >= 0 && quote >= 0) {
                    StringBuilder out = new StringBuilder();
                    boolean escaped = false;
                    for (int i = quote + 1; i < text.length(); i++) {
                        char c = text.charAt(i);
                        if (escaped) {
                            if (c == 'n') out.append('\n');
                            else out.append(c);
                            escaped = false;
                        } else if (c == '\\') {
                            escaped = true;
                        } else if (c == '"') {
                            break;
                        } else {
                            out.append(c);
                        }
                    }
                    message = out.toString().trim();
                }
            }
        } catch (Exception ignored) {
        }
        if (message.isEmpty()) {
            int actions = text.indexOf("\"actions\"");
            message = actions > 0 ? text.substring(0, actions) : text;
            message = message.replaceAll("^\\s*\\{?\\s*\"message\"\\s*:\\s*\"?", "")
                    .replaceAll("[\",;\\s]+$", "")
                    .trim();
        }
        message = message.replace("\\n", "\n").replace("\\\"", "\"").trim();
        message = sanitizeAiAssistantText(message.equals(text) ? "" : message);
        return message.isEmpty() ? "I prepared file edits, but the model returned malformed tool JSON. Please run the request again." : message;
    }

    private boolean looksLikeEchoedSourceLine(String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return false;
        if (trimmed.matches("^(def|class|return|if|elif|else|for|while|try|except|with|import|from|print|break|continue)\\b.*")) return true;
        if (trimmed.matches("^[A-Za-z_][A-Za-z0-9_]*\\s*=.*")) return true;
        if (trimmed.matches("^\\w+\\(.*\\):$")) return true;
        if (trimmed.equals("main()") || trimmed.equals("pass")) return true;
        return line.startsWith("    ") || line.startsWith("\t");
    }

    private TextView aiAssistantTextView(String text) {
        TextView view = new TextView(this);
        view.setText(aiInlineFormattedText(text));
        view.setTextColor(Color.rgb(226, 230, 238));
        view.setTextSize(14);
        view.setLineSpacing(dp(2), 1.0f);
        view.setTextIsSelectable(true);
        view.setPadding(0, dp(2), 0, dp(5));
        return view;
    }

    private TextView aiCodeBlockView(String language, String code) {
        TextView view = new TextView(this);
        SpannableStringBuilder out = new SpannableStringBuilder();
        String label = language == null || language.trim().isEmpty() ? "code" : language.trim();
        int headerStart = out.length();
        out.append(label).append('\n');
        out.setSpan(new ForegroundColorSpan(Color.rgb(150, 226, 255)), headerStart, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new StyleSpan(Typeface.BOLD), headerStart, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int codeStart = out.length();
        out.append(code == null ? "" : code.trim());
        highlightCodeText(out, codeStart, label);
        view.setText(out);
        view.setTypeface(Typeface.MONOSPACE);
        view.setTextColor(Color.rgb(236, 240, 248));
        view.setTextSize(12);
        view.setTextIsSelectable(true);
        view.setPadding(dp(10), dp(8), dp(10), dp(8));
        view.setBackground(rounded(Color.rgb(25, 28, 34), Color.rgb(73, 82, 96), 1, 7));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(7), 0, dp(7));
        view.setLayoutParams(params);
        return view;
    }

    private CharSequence aiInlineFormattedText(String text) {
        String source = text == null ? "" : text;
        SpannableStringBuilder out = new SpannableStringBuilder();
        int cursor = 0;
        while (cursor < source.length()) {
            if (source.startsWith("**", cursor)) {
                int end = source.indexOf("**", cursor + 2);
                if (end > cursor + 2) {
                    appendInlineSpan(out, source.substring(cursor + 2, end), TEXT, true, false);
                    cursor = end + 2;
                    continue;
                }
            }
            if (source.charAt(cursor) == '`') {
                int end = source.indexOf('`', cursor + 1);
                if (end > cursor + 1) {
                    appendInlineSpan(out, source.substring(cursor + 1, end), Color.rgb(255, 220, 105), true, true);
                    cursor = end + 1;
                    continue;
                }
            }
            out.append(source.charAt(cursor));
            cursor++;
        }
        return out;
    }

    private void appendInlineSpan(SpannableStringBuilder text, String value, int color, boolean bold, boolean monospace) {
        int start = text.length();
        text.append(value);
        int end = text.length();
        text.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (bold) text.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (monospace) {
            text.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new BackgroundColorSpan(Color.rgb(43, 47, 56)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void highlightCodeText(SpannableStringBuilder text, int offset, String language) {
        String code = text.subSequence(offset, text.length()).toString();
        applyCodePattern(text, code, offset, STRING_PATTERN, STRING);
        applyCodePattern(text, code, offset, COMMENT_PATTERN, COMMENT);
        applyCodePattern(text, code, offset, KEYWORD_PATTERN, KEYWORD);
        applyCodePattern(text, code, offset, NUMBER_PATTERN, NUMBER);
        applyCodePattern(text, code, offset, BUILTIN_PATTERN, BUILTIN);
    }

    private void applyCodePattern(SpannableStringBuilder target, String code, int offset, Pattern pattern, int color) {
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            int start = offset + matcher.start();
            int end = offset + matcher.end();
            target.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private String runAiAgentConversation(String prompt, AiStreamCallback callback) throws Exception {
        AutocompleteProvider provider = currentProvider();
        if ("local".equals(provider.id)) {
            return "Select a network AI provider in Settings > Autocomplete first. The chat uses the same provider/model keys.";
        }
        if (provider.needsKey && providerApiKey(provider).isEmpty()) {
            return "Add the " + provider.name + " API key in Settings > Autocomplete first.";
        }
        if (!("groq".equals(provider.id) || "openai".equals(provider.id)
                || "openrouter".equals(provider.id) || "mistral".equals(provider.id)
                || "cohere".equals(provider.id) || "gemini".equals(provider.id))) {
            return provider.name + " chat is not wired yet. Use Groq, OpenAI-compatible, OpenRouter, Mistral, Cohere, or Gemini.";
        }

        String transcript = "";
        String finalMessage = "";
        String lastSyntheticReason = "";
        ArrayList<String> completedActions = new ArrayList<>();
        LinkedHashSet<String> completedActionSet = new LinkedHashSet<>();
        ArrayList<String> meaningfulActions = new ArrayList<>();
        LinkedHashSet<String> meaningfulActionSet = new LinkedHashSet<>();
        LinkedHashSet<String> executedActionFingerprints = new LinkedHashSet<>();
        AiAgentRunState runState = new AiAgentRunState();
        runState.sawIdeContext = true;
        runState.localProjectBuildIntent = isLocalProjectBuildIntent(prompt);
        runState.webNeeded = promptNeedsWebSearch(prompt);
        for (int cycle = 0; cycle < AI_AGENT_MAX_CYCLES; cycle++) {
            JSONObject envelope;
            try {
                envelope = fetchAiAgentEnvelope(provider, prompt, transcript, null);
            } catch (Exception modelError) {
                Log.w(TAG_AI, "Provider failed during agent request: " + safeAiDiagnostic(modelError));
                throw modelError;
            }
            String message = envelope.optString("message", "");
            JSONArray actions = envelope.optJSONArray("actions");
            if (actions != null && actions.length() > 0 && isChatOnlyCodeBlockRequest(prompt)) {
                String converted = assistantCodeBlockFromActions(actions, message);
                finalMessage = converted.isEmpty() ? message.trim() : converted;
                break;
            }
            if (actions == null || actions.length() == 0) {
                if (runState.localProjectBuildIntent && runState.editsApplied == 0) {
                    transcript += "\nAgent guard:\nThe user asked you to build project files, but no files have been created or edited yet. "
                            + "A read-only answer is not a valid completion. Do not stop with a summary or say you will do it. "
                            + "Your next response must call create_file or edit_file for the actual implementation. "
                            + "If you already read enough context, create the smallest working files now; otherwise use list_files/read_file once, then edit/create.\n";
                    JSONObject repair = fetchProviderJsonRepairEnvelope(provider, prompt, transcript, callback);
                    JSONArray repairActions = repair == null ? null : repair.optJSONArray("actions");
                    if (repairActions == null || repairActions.length() == 0) {
                        Log.w(TAG_AI, "JSON repair produced no actions for local build request; provider=" + provider.id);
                        waitBeforeNextAiAgentLoop(cycle);
                        continue;
                    }
                    Log.i(TAG_AI, "JSON repair produced " + repairActions.length() + " action(s) for provider=" + provider.id);
                    message = repair.optString("message", "");
                    actions = repairActions;
                }
                if ((actions == null || actions.length() == 0) && runState.editsApplied > 0 && runState.terminalRuns == 0 && cycle < AI_AGENT_MAX_CYCLES - 1) {
                    transcript += "\nAgent guard:\nFiles were changed but no verification command was run. "
                            + "Use run_terminal with a bounded non-interactive compile, syntax, or smoke-test command before the final reply.\n";
                    waitBeforeNextAiAgentLoop(cycle);
                    continue;
                }
                if (actions == null || actions.length() == 0) {
                    if (!message.trim().isEmpty()) finalMessage = message.trim();
                    break;
                }
            }
            if (message.trim().isEmpty()) {
                message = aiReasonForActions(actions, prompt, transcript);
                lastSyntheticReason = message;
            } else {
                finalMessage = message;
            }
            if (!message.isEmpty() && callback != null) {
                String visibleMessage = message;
                runOnUiThread(() -> callback.onPartial(visibleMessage));
            }
            StringBuilder results = new StringBuilder();
            int executedThisCycle = 0;
            int duplicateThisCycle = 0;
            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.optJSONObject(i);
                if (action == null) continue;
                String fingerprint = aiActionFingerprint(action);
                if (!fingerprint.isEmpty() && executedActionFingerprints.contains(fingerprint)) {
                    duplicateThisCycle++;
                    continue;
                }
                if (!fingerprint.isEmpty()) executedActionFingerprints.add(fingerprint);
                executedThisCycle++;
                String label = visibleAiAction(action);
                runOnUiThread(() -> beginAiActionFlow(action));
                String result = executeAiToolAction(action, runState);
                String completed = aiCompletedActionDetail(action, result);
                if (!completed.isEmpty() && completedActionSet.add(completed)) completedActions.add(completed);
                String meaningful = aiMeaningfulCompletionDetail(action, result);
                if (!meaningful.isEmpty() && meaningfulActionSet.add(meaningful)) meaningfulActions.add(meaningful);
                String line = action.optString("type", "tool") + " " + label + ":\n" + transcriptToolText(result);
                results.append(line).append('\n');
                runOnUiThread(() -> {
                    addAiActionResultFlow(action, result);
                    finishAiFeed(aiCompletedActionSummary(action));
                });
            }
            if (executedThisCycle == 0 && duplicateThisCycle > 0 && runState.localProjectBuildIntent && runState.editsApplied == 0) {
                transcript += "\nAgent guard:\nYou repeated read-only actions that were already executed. Stop repeating reads. "
                        + "Use create_file or edit_file now, based on the context already provided.\n";
                if (cycle < AI_AGENT_MAX_CYCLES - 1) {
                    waitBeforeNextAiAgentLoop(cycle);
                    continue;
                }
            }
            transcript += "\nTool results from cycle " + (cycle + 1) + ":\n" + results
                    + "\nContinue the user's request. If a guard blocked an edit, do the exact missing read/list step and try a smaller patch. "
                    + "If a file was edited, run a relevant non-interactive check before the final reply when possible. "
                    + "If the requested work is already complete, return a final message with an empty actions array.\n";
            waitBeforeNextAiAgentLoop(cycle);
        }
        if (finalMessage.equals(lastSyntheticReason)) finalMessage = "";
        if (runState.localProjectBuildIntent && runState.editsApplied == 0) {
            return "I couldn't safely complete the project build: the model never produced a valid local file edit/create action after guard feedback. I avoided pretending read-only steps were a finished implementation.";
        }
        String summary = aiFinalActionSummary(meaningfulActions.isEmpty() ? completedActions : meaningfulActions, finalMessage);
        return summary.isEmpty() ? (finalMessage.isEmpty() ? "Done." : finalMessage) : summary;
    }

    private String aiReasonForActions(JSONArray actions, String prompt, String transcript) {
        if (actions == null || actions.length() == 0) return "";
        ArrayList<String> reasons = new ArrayList<>();
        for (int i = 0; i < actions.length() && reasons.size() < 3; i++) {
            JSONObject action = actions.optJSONObject(i);
            if (action == null) continue;
            String type = action.optString("type", "");
            String path = aiActionPathDetail(action);
            if ("read_file".equals(type) || "ide_context".equals(type) || "list_files".equals(type)) {
                reasons.add("I’m checking " + (path.isEmpty() ? "the project context" : path) + " first so the answer matches the actual code.");
            } else if ("run_terminal".equals(type)) {
                String command = action.optString("command", "").trim();
                reasons.add("I’m running `" + (command.isEmpty() ? "the command" : command) + "` in the app runtime so I can verify the result instead of guessing.");
            } else if ("edit_file".equals(type)) {
                String target = path.isEmpty() ? "the file" : path;
                reasons.add("I found the edit target, so I’m updating " + target + " with the smallest direct replacement.");
            } else if ("create_file".equals(type)) {
                String target = path.isEmpty() ? "a new file" : path;
                reasons.add("I’m adding " + target + " because the request needs a new project piece rather than only editing existing code.");
            } else if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) {
                String query = action.optString("query", "");
                reasons.add("I’m checking current references" + (query.isEmpty() ? "" : " for " + query) + " before applying the change.");
            } else if ("apt_search".equals(type)) {
                reasons.add("I’m checking available packages so the next command uses something that exists in this runtime.");
            }
        }
        if (reasons.isEmpty()) return "I’m going through the needed project actions step by step before making the final change.";
        StringBuilder out = new StringBuilder();
        for (String reason : reasons) {
            if (out.length() > 0) out.append('\n');
            out.append(reason);
        }
        return out.toString();
    }

    private boolean isLocalProjectBuildIntent(String prompt) {
        String p = prompt == null ? "" : prompt.toLowerCase(Locale.US);
        boolean wantsFiles = p.matches("(?s).*(build|make|create|write|implement|add|generate|code|project|app|tool|engine|ui|tui|script|program).*");
        boolean concreteArtifact = p.matches("(?s).*(sender|packet|engine|ui|tui|file|files|\\.py|\\.c|\\.cpp|main|module|program|script|tool).*");
        boolean chatOnly = p.matches("(?s).*(explain|what is|how to|show me|give me an example|tell me).*")
                && !p.matches("(?s).*(build|make|create|implement|add|write).*");
        return wantsFiles && concreteArtifact && !chatOnly;
    }

    private boolean promptNeedsWebSearch(String prompt) {
        String p = prompt == null ? "" : prompt.toLowerCase(Locale.US);
        if (p.matches("(?s).*(no|dont|don't|without|avoid|skip)\\s+(goofy\\s+)?(web\\s+)?search.*")
                || p.matches("(?s).*(no|dont|don't|without|avoid|skip)\\s+(internet|web).*")) {
            return false;
        }
        return p.matches("(?s).*(search|web|internet|latest|current|docs|documentation|site:|github|stackoverflow|error from|look up|research).*");
    }

    private boolean isChatOnlyCodeBlockRequest(String prompt) {
        String p = prompt == null ? "" : prompt.toLowerCase(Locale.US);
        if (!(p.contains("code block") || p.contains("codeblock") || p.contains("```"))) return false;
        boolean asksForFileChange = p.matches("(?s).*(file|project|editor|main\\.py|\\.py|\\.c|\\.cpp|create|edit|change|update|replace|fix|delete|remove|save|write to|put in|add to).*");
        boolean asksToShow = p.matches("(?s).*(show|display|reply|respond|answer|message|chat|write something|example).*");
        return asksToShow && !asksForFileChange;
    }

    private String assistantCodeBlockFromActions(JSONArray actions, String message) {
        String visible = message == null ? "" : message.trim();
        for (int i = 0; i < actions.length(); i++) {
            JSONObject action = actions.optJSONObject(i);
            if (action == null) continue;
            String type = action.optString("type", "");
            String content = "";
            String path = aiActionPathDetail(action);
            if ("create_file".equals(type)) {
                content = action.optString("content", "");
            } else if ("edit_file".equals(type)) {
                content = action.optString("replace_text", "");
            }
            content = stripWrappingCodeFence(content);
            if (!content.trim().isEmpty()) {
                String language = languageForPath(path);
                String intro = visible.isEmpty() ? "Here’s a code block:" : visible;
                return intro + "\n```" + language + "\n" + content.trim() + "\n```";
            }
        }
        return visible;
    }

    private String aiActionFingerprint(JSONObject action) {
        if (action == null) return "";
        String type = action.optString("type", "");
        if (type.isEmpty()) return "";
        String path = cleanAiFlowDetail(aiActionPathDetail(action));
        if ("create_file".equals(type)) return type + "\n" + path + "\n" + action.optString("content", "").hashCode();
        if ("edit_file".equals(type)) return type + "\n" + path + "\n" + action.optString("search_text", "").hashCode()
                + "\n" + action.optString("replace_text", "").hashCode();
        if ("read_file".equals(type) || "list_files".equals(type) || "ide_context".equals(type)) return type + "\n" + path;
        if ("run_terminal".equals(type)) return type + "\n" + action.optString("cwd", "") + "\n" + action.optString("command", "");
        return type + "\n" + action.toString().hashCode();
    }

    private String aiCompletedActionDetail(JSONObject action, String result) {
        if (action == null) return "";
        if (result != null && result.startsWith("Guard blocked")) return "";
        String type = action.optString("type", "");
        String path = aiActionPathDetail(action);
        if ("read_file".equals(type) || "ide_context".equals(type) || "list_files".equals(type)) {
            return "Read " + (path.isEmpty() ? "project context" : path);
        }
        if ("edit_file".equals(type)) {
            return (result != null && result.startsWith("Edited "))
                    ? "Edited " + (path.isEmpty() ? "a file" : path)
                    : "Tried to edit " + (path.isEmpty() ? "a file" : path) + ": " + compactLogText(result, 120);
        }
        if ("create_file".equals(type)) {
            return (result != null && result.startsWith("Created "))
                    ? "Created " + (path.isEmpty() ? "a file" : path)
                    : "Tried to create " + (path.isEmpty() ? "a file" : path) + ": " + compactLogText(result, 120);
        }
        if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) {
            if (result == null || result.trim().isEmpty() || result.startsWith("Guard blocked")) return "";
            return "Searched the web for " + action.optString("query", "context");
        }
        if ("run_terminal".equals(type)) {
            String command = action.optString("command", "").trim();
            String resultText = result == null ? "" : result;
            Matcher exitMatcher = Pattern.compile("exit\\s+(-?\\d+)").matcher(resultText);
            String exit = exitMatcher.find() ? " exit " + exitMatcher.group(1) : "";
            return "Ran `" + (command.isEmpty() ? "terminal command" : command) + "`" + exit;
        }
        if ("apt_search".equals(type)) return "Searched packages for " + action.optString("query", "the request");
        return "Ran " + visibleAiAction(action);
    }

    private String aiMeaningfulCompletionDetail(JSONObject action, String result) {
        if (action == null || result == null || result.startsWith("Guard blocked")) return "";
        String type = action.optString("type", "");
        String path = aiActionPathDetail(action);
        if ("edit_file".equals(type) && result.startsWith("Edited ")) {
            return "Edited " + (path.isEmpty() ? "a file" : path);
        }
        if ("create_file".equals(type) && result.startsWith("Created ")) {
            return "Created " + (path.isEmpty() ? "a file" : path);
        }
        if ("run_terminal".equals(type)
                && !result.startsWith("Blocked ")
                && !result.startsWith("Missing ")
                && !result.startsWith("Working directory not found")
                && !result.startsWith("Tool failed")) {
            String command = action.optString("command", "").trim();
            Matcher exitMatcher = Pattern.compile("exit\\s+(-?\\d+)").matcher(result);
            String exit = exitMatcher.find() ? " exit " + exitMatcher.group(1) : "";
            return "Ran `" + (command.isEmpty() ? "terminal command" : command) + "`" + exit;
        }
        return "";
    }

    private String aiFinalActionSummary(ArrayList<String> completedActions, String fallback) {
        if (completedActions == null || completedActions.isEmpty()) return "";
        StringBuilder out = new StringBuilder("Done. Here’s what I did:");
        int limit = Math.min(5, completedActions.size());
        for (int i = 0; i < limit; i++) {
            out.append("\n- ").append(completedActions.get(i));
        }
        if (completedActions.size() > limit) {
            out.append("\n- And ").append(completedActions.size() - limit).append(" more project action");
            if (completedActions.size() - limit != 1) out.append('s');
        }
        if (fallback != null && !fallback.trim().isEmpty()) {
            out.append("\n\n").append(fallback.trim());
        }
        return out.toString();
    }

    private String transcriptToolText(String result) {
        if (result == null) return "";
        String trimmed = result.trim();
        return trimmed.length() > 10000 ? trimmed.substring(0, 10000) + "\n...[truncated]" : trimmed;
    }

    private void beginAiActionFlow(JSONObject action) {
        String type = action == null ? "" : action.optString("type", "");
        if ("read_file".equals(type) || "list_files".equals(type) || "ide_context".equals(type)) {
            addAiFlowStep("Read files", true, "read");
            String detail = aiActionPathDetail(action);
            if (!detail.isEmpty()) addAiFlowDetail(detail);
        } else if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) {
            addAiFlowStep("Searching the web:", true, "web");
            String site = action == null ? "" : action.optString("site", action.optString("url", action.optString("query", "")));
            if (!site.isEmpty()) addAiFlowDetail("Site: " + site);
        } else if ("create_file".equals(type) || "edit_file".equals(type)) {
            addAiFlowStep("Editing file", true, "edit");
            String detail = aiActionPathDetail(action);
            if (!detail.isEmpty()) addAiFlowDetail(detail);
        } else if ("apt_search".equals(type)) {
            addAiFlowStep("Searching packages", true, "search");
            String query = action == null ? "" : action.optString("query", "");
            if (!query.isEmpty()) addAiFlowDetail(query);
        } else if ("run_terminal".equals(type)) {
            addAiFlowStep("Running terminal command", true, "terminal");
            String command = action == null ? "" : action.optString("command", "");
            if (!command.isEmpty()) addAiFlowDetail(command);
        } else {
            addAiFlowStep("Running " + visibleAiAction(action), true);
        }
    }

    private void addAiActionResultFlow(JSONObject action, String result) {
        String type = action == null ? "" : action.optString("type", "");
        if (result != null && result.startsWith("Guard blocked")) {
            AiFlowStep step = activeAiFlowStep();
            if (step != null) {
                step.title = "Blocked unsafe action";
                step.active = false;
                step.finished = true;
                step.details.clear();
                step.details.add(compactLogText(result, 96));
            } else {
                addAiFlowStep("Blocked unsafe action", false, "blocked");
                addAiFlowDetail(compactLogText(result, 96));
            }
            addAiFeedLine(compactLogText(result));
            renderAiChatMessages();
            return;
        }
        if ("read_file".equals(type) || "list_files".equals(type) || "ide_context".equals(type)) {
            for (String item : aiFlowFilesFromResult(action, result)) addAiFlowDetail(item);
        } else if ("create_file".equals(type) || "edit_file".equals(type)) {
            String detail = aiActionPathDetail(action);
            if (!detail.isEmpty()) aiEditedFilesThisRun.add(detail);
            AiFlowStep step = activeAiFlowStep();
            if (step != null) {
                if (step.details.size() > 1 || (!detail.isEmpty() && !step.details.contains(cleanAiFlowDetail(detail)))) {
                    step.title = "Edited files";
                    if (!detail.isEmpty() && !step.details.contains(cleanAiFlowDetail(detail))) {
                        step.details.add(cleanAiFlowDetail(detail));
                    }
                } else {
                    step.title = detail.isEmpty() ? "Edited file" : "Edited file " + detail;
                    if (step.details.isEmpty() && !detail.isEmpty()) step.details.add(cleanAiFlowDetail(detail));
                }
                if (result != null && (result.startsWith("Edited ") || result.startsWith("Created "))) {
                    step.editPreview = buildAiEditPreview(action);
                }
            }
        } else if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) {
            String site = action == null ? "" : action.optString("site", action.optString("url", ""));
            if (!site.isEmpty()) addAiFlowDetail("Site: " + site);
        } else if ("run_terminal".equals(type)) {
            String summary = compactLogText(result);
            if (!summary.isEmpty()) addAiFlowDetail(summary);
        } else {
            String summary = compactLogText(result);
            if (!summary.isEmpty()) addAiFlowDetail(summary);
        }
        addAiFeedLine(compactLogText(result));
    }

    private String aiCompletedActionSummary(JSONObject action) {
        String type = action == null ? "" : action.optString("type", "");
        if ("read_file".equals(type) || "list_files".equals(type) || "ide_context".equals(type)) return "Read files";
        if ("create_file".equals(type) || "edit_file".equals(type)) {
            return aiEditedFilesThisRun.size() > 1 ? "Edited files" : "Edited file";
        }
        if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) return "Web search";
        if ("apt_search".equals(type)) return "Searched packages";
        if ("run_terminal".equals(type)) return "Ran terminal command";
        return "Executed " + visibleAiAction(action);
    }

    private AiEditPreview buildAiEditPreview(JSONObject action) {
        if (action == null) return null;
        String type = action.optString("type", "");
        String fileName = aiActionPathDetail(action);
        AiEditPreview preview = new AiEditPreview(fileName.isEmpty() ? "edited file" : fileName);
        if ("edit_file".equals(type)) {
            appendPreviewRows(preview, action.optString("search_text", ""), -1, 1);
            appendPreviewRows(preview, action.optString("replace_text", ""), 1, 1);
        } else if ("create_file".equals(type)) {
            appendPreviewRows(preview, action.optString("content", ""), 1, 1);
        }
        return preview.rows.isEmpty() ? null : preview;
    }

    private void appendPreviewRows(AiEditPreview preview, String text, int kind, int startLine) {
        if (preview == null || text == null || text.isEmpty()) return;
        String[] lines = text.split("\\n", -1);
        int limit = Math.min(lines.length, 16);
        for (int i = 0; i < limit; i++) {
            preview.rows.add(new AiEditPreviewRow(startLine + i, lines[i], kind));
        }
    }

    private String aiActionPathDetail(JSONObject action) {
        if (action == null) return "";
        String path = action.optString("path", "");
        if (path == null || path.trim().isEmpty()) {
            if ("ide_context".equals(action.optString("type", ""))) return currentFileName();
            return "";
        }
        return new File(path).getName().isEmpty() ? path : new File(path).getName();
    }

    private ArrayList<String> aiFlowFilesFromResult(JSONObject action, String result) {
        ArrayList<String> files = new ArrayList<>();
        String first = aiActionPathDetail(action);
        if (!first.isEmpty()) files.add(first);
        String type = action == null ? "" : action.optString("type", "");
        if (!"list_files".equals(type) || result == null) return files;
        String[] lines = result.split("\\n");
        for (String raw : lines) {
            if (files.size() >= 5) break;
            String line = raw == null ? "" : raw.trim();
            if (line.isEmpty() || line.length() > 80 || line.startsWith("file=") || line.startsWith("path=")
                    || line.startsWith("project=") || line.startsWith("opened=") || line.contains("=")) continue;
            if (line.contains(File.separator) || line.contains(".") || line.endsWith("/")) {
                String cleaned = line.replaceFirst("^[-*\\s]+", "")
                        .replace("📁", "")
                        .replace("📄", "")
                        .trim();
                if (!files.contains(cleaned)) files.add(cleaned);
            }
        }
        return files;
    }

    private String visibleAiAction(JSONObject action) {
        String type = action == null ? "tool" : action.optString("type", "tool");
        String detail = "";
        if (action != null) {
            if ("apt_search".equals(type)) detail = action.optString("query", "");
            else detail = action.optString("path", "");
        }
        if (detail == null || detail.trim().isEmpty()) return type;
        return type + " " + detail.trim();
    }

    private JSONObject fetchAiAgentEnvelope(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback) throws Exception {
        boolean includeWebSearch = shouldExposeWebSearchTool(prompt);
        if ("cohere".equals(provider.id)) return fetchCohereAgentEnvelope(provider, prompt, toolTranscript, callback, includeWebSearch);
        if ("gemini".equals(provider.id)) return fetchGeminiAgentEnvelope(provider, prompt, toolTranscript, callback, includeWebSearch);
        JSONObject body = buildOpenAiAgentBody(provider, prompt, toolTranscript, true);
        AiHttpResponse result = postAiJson(provider, body, 15000);
        if (!result.ok()) {
            if (shouldRetryWithoutNativeTools(result.status, result.body)) {
                JSONObject fallback = buildOpenAiAgentBody(provider, prompt, toolTranscript, false);
                AiHttpResponse fallbackResult = postAiJson(provider, fallback, 18000);
                if (!fallbackResult.ok()) throw aiHttpException(fallbackResult);
                return parseOpenAiJsonAgentResponse(fallbackResult.body, callback);
            }
            throw aiHttpException(result);
        }
        JSONObject nativeEnvelope;
        try {
            nativeEnvelope = parseOpenAiNativeAgentResponse(result.body, callback);
        } catch (Exception nativeParseError) {
            if (shouldUseJsonAgentFallback(provider, prompt, toolTranscript, nativeParseError)) {
                JSONObject fallback = fetchJsonAgentFallback(provider, prompt, toolTranscript, callback);
                if (fallback != null) return fallback;
            }
            throw nativeParseError;
        }
        JSONArray actions = nativeEnvelope.optJSONArray("actions");
        String message = nativeEnvelope.optString("message", "");
        if ((actions == null || actions.length() == 0) && shouldRetryJsonForNoAction(provider, prompt, message)) {
            JSONObject fallback = fetchJsonAgentFallback(provider, prompt, toolTranscript, callback);
            if (fallback != null) return fallback;
        }
        return nativeEnvelope;
    }

    private JSONObject fetchJsonAgentFallback(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback) throws Exception {
        JSONObject fallback = buildOpenAiAgentBody(provider, prompt, toolTranscript, false);
        AiHttpResponse fallbackResult = postAiJson(provider, fallback, 18000);
        if (!fallbackResult.ok()) {
            if (callback != null) callback.onPartial("The model could not produce a valid tool response.");
            return null;
        }
        return parseOpenAiJsonAgentResponse(fallbackResult.body, callback);
    }

    private JSONObject fetchProviderJsonRepairEnvelope(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback) {
        if (provider == null) return null;
        try {
            if ("cohere".equals(provider.id)) {
                return fetchCohereJsonAgentEnvelope(provider, prompt, toolTranscript, callback, shouldExposeWebSearchTool(prompt));
            }
            if ("gemini".equals(provider.id)) {
                return fetchGeminiAgentEnvelope(provider, prompt, toolTranscript, callback, shouldExposeWebSearchTool(prompt));
            }
            if ("groq".equals(provider.id) || "openai".equals(provider.id)
                    || "openrouter".equals(provider.id) || "mistral".equals(provider.id)) {
                return fetchJsonAgentFallback(provider, prompt, toolTranscript, callback);
            }
        } catch (Exception e) {
            Log.w(TAG_AI, "JSON repair fallback failed for provider=" + provider.id + ": " + safeAiDiagnostic(e));
        }
        return null;
    }

    private boolean shouldUseJsonAgentFallback(AutocompleteProvider provider, String prompt, String toolTranscript, Exception error) {
        if (provider == null) return false;
        String request = prompt == null ? "" : prompt.toLowerCase(Locale.US);
        String transcript = toolTranscript == null ? "" : toolTranscript.toLowerCase(Locale.US);
        String message = error == null || error.getMessage() == null ? "" : error.getMessage().toLowerCase(Locale.US);
        boolean editIntent = request.matches("(?s).*(fix|edit|change|update|create|build|add|remove|delete|make|replace|write).*");
        boolean afterTool = !transcript.trim().isEmpty();
        boolean emptyTurn = message.contains("empty model content") || message.contains("empty ai response") || message.contains("empty response");
        return "groq".equals(provider.id) && editIntent && (afterTool || emptyTurn);
    }

    private JSONObject buildOpenAiAgentBody(AutocompleteProvider provider, String prompt, String toolTranscript, boolean nativeTools) throws Exception {
        boolean includeWebSearch = shouldExposeWebSearchTool(prompt);
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.25);
        body.put("max_tokens", agentMaxTokens(provider, nativeTools));
        body.put("stream", false);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", nativeTools ? aiAgentSystemPrompt(includeWebSearch) : aiJsonAgentSystemPrompt(includeWebSearch)));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "IDE context:\n" + aiIdeContext() + "\n\nUser request:\n" + prompt + "\n" + toolTranscript));
        body.put("messages", messages);
        if (nativeTools) {
            body.put("tools", aiAgentTools(includeWebSearch));
            body.put("tool_choice", "auto");
        }
        return body;
    }

    private int agentMaxTokens(AutocompleteProvider provider, boolean nativeTools) {
        if (provider != null && "groq".equals(provider.id)) {
            return nativeTools ? 2200 : 1600;
        }
        return nativeTools ? 1200 : 1400;
    }

    private AiHttpResponse postAiJson(AutocompleteProvider provider, JSONObject body, int readTimeoutMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(providerEndpoint(provider)).openConnection();
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(readTimeoutMs);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        String apiKey = providerApiKey(provider);
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        return new AiHttpResponse(status, response);
    }

    private JSONObject parseOpenAiNativeAgentResponse(String response, AiStreamCallback callback) throws Exception {
        JSONObject json = new JSONObject(response);
        JSONArray choices = json.optJSONArray("choices");
        JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
        JSONObject message = first == null ? null : first.optJSONObject("message");
        return parseOpenAiAgentMessage(message, response, callback);
    }

    private JSONObject parseOpenAiJsonAgentResponse(String response, AiStreamCallback callback) throws Exception {
        JSONObject json = new JSONObject(response);
        JSONArray choices = json.optJSONArray("choices");
        JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
        JSONObject message = first == null ? null : first.optJSONObject("message");
        String content = message == null ? "" : message.optString("content", "");
        JSONObject envelope = parseAiJsonEnvelope(content);
        String visible = envelope.optString("message", "").trim();
        if (!visible.isEmpty() && callback != null && envelope.optJSONArray("actions").length() == 0) {
            callback.onPartial(visible);
        }
        return envelope;
    }

    private JSONObject fetchAiAgentEnvelopeNoStream(AutocompleteProvider provider, JSONObject body) throws Exception {
        AiHttpResponse result = postAiJson(provider, body, 15000);
        if (!result.ok()) throw aiHttpException(result);
        String response = result.body;
        if (response.trim().isEmpty()) throw new IOException("empty response from " + provider.name);
        JSONObject json = new JSONObject(response);
        JSONArray choices = json.optJSONArray("choices");
        JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
        JSONObject message = first == null ? null : first.optJSONObject("message");
        return parseOpenAiAgentMessage(message, response, null);
    }

    private boolean shouldRetryWithoutNativeTools(int status, String body) {
        if (status == 400 || status == 422) return true;
        String lower = body == null ? "" : body.toLowerCase(Locale.US);
        return lower.contains("tool") || lower.contains("function") || lower.contains("schema")
                || lower.contains("response_format") || lower.contains("json");
    }

    private boolean shouldRetryJsonForNoAction(AutocompleteProvider provider, String prompt, String message) {
        String request = prompt == null ? "" : prompt.toLowerCase(Locale.US);
        String reply = message == null ? "" : message.toLowerCase(Locale.US);
        boolean editIntent = request.matches("(?s).*(fix|edit|change|update|create|build|add|remove|delete|make|replace|write).*");
        boolean promisedAction = reply.contains("i will fix") || reply.contains("i'll fix")
                || reply.contains("i will look") || reply.contains("i will update")
                || reply.contains("i will create") || reply.contains("right now");
        boolean weakToolModel = provider != null && "groq".equals(provider.id);
        return editIntent && (promisedAction || weakToolModel);
    }

    private IOException aiHttpException(AiHttpResponse result) {
        String message = result == null ? "" : providerErrorMessage(result.body);
        int status = result == null ? 0 : result.status;
        return new IOException("HTTP " + status + (message.isEmpty() ? "" : ": " + safeAiDiagnostic(message, 220)));
    }

    private JSONObject fetchCohereAgentEnvelope(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback, boolean includeWebSearch) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.25);
        body.put("max_tokens", 900);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", aiAgentSystemPrompt(includeWebSearch)));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "IDE context:\n" + aiIdeContext() + "\n\nUser request:\n" + prompt + "\n" + toolTranscript));
        body.put("messages", messages);
        body.put("tools", aiAgentTools(includeWebSearch));

        HttpURLConnection connection = (HttpURLConnection) new URL(providerEndpoint(provider)).openConnection();
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        String apiKey = providerApiKey(provider);
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) throw new IOException("HTTP " + status + " " + safeAiDiagnostic(response, 220));
        return parseCohereAgentMessage(new JSONObject(response), response, callback);
    }

    private JSONObject fetchCohereJsonAgentEnvelope(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback, boolean includeWebSearch) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.15);
        body.put("max_tokens", 1400);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", aiJsonAgentSystemPrompt(includeWebSearch)));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "IDE context:\n" + aiIdeContext() + "\n\nUser request:\n" + prompt + "\n" + toolTranscript));
        body.put("messages", messages);

        HttpURLConnection connection = (HttpURLConnection) new URL(providerEndpoint(provider)).openConnection();
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        String apiKey = providerApiKey(provider);
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) throw new IOException("HTTP " + status + " " + safeAiDiagnostic(response, 220));
        JSONObject envelope = parseAiJsonEnvelope(cohereMessageText(new JSONObject(response).optJSONObject("message")));
        String visible = envelope.optString("message", "").trim();
        JSONArray actions = envelope.optJSONArray("actions");
        Log.i(TAG_AI, "Cohere JSON repair parsed actions=" + (actions == null ? 0 : actions.length()));
        if (!visible.isEmpty() && callback != null && (actions == null || actions.length() == 0)) callback.onPartial(visible);
        return envelope;
    }

    private JSONObject fetchGeminiAgentEnvelope(AutocompleteProvider provider, String prompt, String toolTranscript, AiStreamCallback callback, boolean includeWebSearch) throws Exception {
        String apiKey = providerApiKey(provider);
        String model = providerModel(provider);
        String endpoint = providerEndpoint(provider);
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        String url = base + "/" + model + ":generateContent?key=" + URLEncoder.encode(apiKey, "UTF-8");

        JSONObject body = new JSONObject();
        body.put("generationConfig", new JSONObject()
                .put("temperature", 0.25)
                .put("maxOutputTokens", 1800));
        String content = aiJsonAgentSystemPrompt(includeWebSearch)
                + "\n\nAvailable actions are the same tool names listed in the JSON schema, including run_terminal for compile/run/test."
                + "\n\nIDE context:\n" + aiIdeContext()
                + "\n\nUser request:\n" + prompt + "\n" + toolTranscript;
        body.put("contents", new JSONArray()
                .put(new JSONObject().put("role", "user")
                        .put("parts", new JSONArray().put(new JSONObject().put("text", content)))));

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(18000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) throw new IOException("HTTP " + status + " " + safeAiDiagnostic(response, 220));
        JSONObject envelope = parseAiJsonEnvelope(geminiResponseText(new JSONObject(response)));
        String visible = envelope.optString("message", "").trim();
        JSONArray actions = envelope.optJSONArray("actions");
        if (!visible.isEmpty() && callback != null && (actions == null || actions.length() == 0)) callback.onPartial(visible);
        return envelope;
    }

    private String geminiResponseText(JSONObject json) {
        JSONArray candidates = json == null ? null : json.optJSONArray("candidates");
        JSONObject candidate = candidates == null || candidates.length() == 0 ? null : candidates.optJSONObject(0);
        JSONObject content = candidate == null ? null : candidate.optJSONObject("content");
        JSONArray parts = content == null ? null : content.optJSONArray("parts");
        if (parts == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.optJSONObject(i);
            String text = part == null ? parts.optString(i, "") : part.optString("text", "");
            if (!text.isEmpty()) {
                if (out.length() > 0) out.append('\n');
                out.append(text);
            }
        }
        return out.toString().trim();
    }

    private JSONObject parseCohereAgentMessage(JSONObject json, String response, AiStreamCallback callback) throws Exception {
        JSONObject message = json.optJSONObject("message");
        String content = cohereMessageText(message);
        JSONArray actions = new JSONArray();
        JSONArray toolCalls = message == null ? null : message.optJSONArray("tool_calls");
        if (toolCalls == null) toolCalls = json.optJSONArray("tool_calls");
        if (toolCalls != null) {
            for (int i = 0; i < toolCalls.length(); i++) {
                JSONObject call = toolCalls.optJSONObject(i);
                if (call == null) continue;
                JSONObject function = call.optJSONObject("function");
                String name = function == null ? call.optString("name", "") : function.optString("name", "");
                String rawArgs = function == null
                        ? call.optString("arguments", call.optString("parameters", "{}"))
                        : function.optString("arguments", "{}");
                if (name.isEmpty()) continue;
                JSONObject args = parseToolArguments(rawArgs);
                JSONObject params = call.optJSONObject("parameters");
                if (params != null) args = params;
                args.put("type", name);
                actions.put(args);
            }
        }
        if (!content.isEmpty() && callback != null && actions.length() == 0) callback.onPartial(content);
        if (content.isEmpty() && actions.length() == 0) throw new IOException("empty Cohere content: " + safeAiDiagnostic(response, 180));
        return new JSONObject().put("message", content).put("actions", actions);
    }

    private String cohereMessageText(JSONObject message) {
        if (message == null) return "";
        Object content = message.opt("content");
        if (content instanceof JSONArray) {
            StringBuilder out = new StringBuilder();
            JSONArray parts = (JSONArray) content;
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                String text = part == null ? parts.optString(i, "") : part.optString("text", "");
                if (!text.isEmpty()) {
                    if (out.length() > 0) out.append('\n');
                    out.append(text);
                }
            }
            return out.toString().trim();
        }
        return content == null ? "" : String.valueOf(content).trim();
    }

    private String readOpenAiCompatibleStream(InputStream stream, AiStreamCallback callback) throws Exception {
        StringBuilder raw = new StringBuilder();
        String lastVisible = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("data:")) continue;
                String data = line.substring(5).trim();
                if ("[DONE]".equals(data)) break;
                if (data.isEmpty()) continue;
                JSONObject event = new JSONObject(data);
                JSONArray choices = event.optJSONArray("choices");
                JSONObject first = choices == null || choices.length() == 0 ? null : choices.optJSONObject(0);
                JSONObject delta = first == null ? null : first.optJSONObject("delta");
                String chunk = delta == null ? "" : delta.optString("content", "");
                if (chunk.isEmpty()) continue;
                raw.append(chunk);
                String visible = extractStreamingJsonMessage(raw.toString());
                if (!visible.isEmpty() && !visible.equals(lastVisible)) {
                    lastVisible = visible;
                    callback.onPartial(visible);
                }
            }
        }
        return raw.toString();
    }

    private String extractStreamingJsonMessage(String raw) {
        if (raw == null) return "";
        int key = raw.indexOf("\"message\"");
        if (key < 0) return "";
        int colon = raw.indexOf(':', key + 9);
        if (colon < 0) return "";
        int quote = raw.indexOf('"', colon + 1);
        if (quote < 0) return "";
        StringBuilder out = new StringBuilder();
        boolean escaped = false;
        for (int i = quote + 1; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (escaped) {
                if (c == 'n') out.append('\n');
                else if (c == 't') out.append('\t');
                else out.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                break;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private boolean shouldExposeWebSearchTool(String prompt) {
        return promptNeedsWebSearch(prompt) && !isLocalProjectBuildIntent(prompt);
    }

    private String aiAgentSystemPrompt(boolean includeWebSearch) {
        return "You are Aqua IDE's right-side AI agent. Reason briefly, use tools when you need IDE context or file changes, "
                + "and answer in normal text when no tool is needed. Prefer surgical edits with edit_file. "
                + "For requests to build, create, implement, add, fix, or generate project code, tool use is mandatory: inspect only as much as needed, then call create_file or edit_file. "
                + "Plan implicitly by first gathering the exact context you need. You cannot safely edit a file until you have read that file in this turn; for new files, list or inspect the destination first. "
                + "If an edit is blocked by a guard, do the requested read/list step, then retry with a smaller exact replacement. "
                + (includeWebSearch
                ? "web_search is available only for current docs, websites, or specific external errors; prefer local files for local project work. "
                : "web_search is not available for this request; use local IDE tools only. ")
                + "A request to show, display, answer with, or write a code block is a chat-format request, not a file edit; reply with a markdown code fence and no tools unless the user explicitly asks to create/edit/save a file or project. "
                + "When using create_file or edit_file, put raw file content in the tool fields, never wrap it in markdown fences or explanatory text. "
                + "Do not rewrite large files unless the user explicitly asks; use exact small replacements. "
                + "Before each tool/action, explain what you are checking or changing and why in one concise, specific sentence. "
                + "After the requested work is done, give a final reply summarizing what changed and any important result. "
                + "When you find a bug, explain the specific thing you are going to fix, not a generic 'I found this issue' sentence. "
                + "If the user asks to compile, run, test, or inspect command output, use run_terminal; do not claim you lack terminal access. "
                + "Never quote or reprint the IDE context/excerpt unless the user explicitly asks to see it. "
                + "When the user asks to change the current file, inspect it if needed and then call edit_file or create_file; do not stop after reading. "
                + "Paths are restricted to the project folder or app home.";
    }

    private String aiJsonAgentSystemPrompt(boolean includeWebSearch) {
        String actionShape = "{\"message\":\"brief user-visible explanation\",\"actions\":[{\"type\":\"ide_context\"},{\"type\":\"list_files\",\"path\":\".\"},{\"type\":\"read_file\",\"path\":\"file.py\"},{\"type\":\"edit_file\",\"path\":\"file.py\",\"search_text\":\"exact old text\",\"replace_text\":\"new text\"},{\"type\":\"create_file\",\"path\":\"file.py\",\"content\":\"new content\"},{\"type\":\"run_terminal\",\"command\":\"python file.py\",\"cwd\":\".\"}"
                + (includeWebSearch ? ",{\"type\":\"web_search\",\"query\":\"search terms\",\"site\":\"optional.example\"}" : "")
                + "]}";
        return aiAgentSystemPrompt(includeWebSearch)
                + "\nNative tool calls are unavailable for this request. Return one compact JSON object only, with this shape:"
                + actionShape + "."
                + " Use actions for every requested file change. If the user only asks for a code block in chat, put the fenced code block in message and use an empty actions array. If no action is needed, use an empty actions array.";
    }

    private JSONArray aiAgentTools(boolean includeWebSearch) throws Exception {
        JSONArray tools = new JSONArray();
        tools.put(aiTool("ide_context", "Get current IDE context, opened file, project folder, and editor contents.",
                new JSONObject().put("type", "object").put("properties", new JSONObject()).put("additionalProperties", false)));
        tools.put(aiTool("list_files", "List files under a project/app-home path.",
                new JSONObject().put("type", "object")
                        .put("properties", new JSONObject().put("path", new JSONObject().put("type", "string").put("description", "Folder path, or empty for project/app home.")))
                        .put("additionalProperties", false)));
        tools.put(aiTool("read_file", "Read a file from the project folder or app home.",
                new JSONObject().put("type", "object")
                        .put("properties", new JSONObject().put("path", new JSONObject().put("type", "string")))
                        .put("required", new JSONArray().put("path"))
                        .put("additionalProperties", false)));
        if (includeWebSearch) {
            tools.put(aiTool("web_search", "Search the web only for current documentation, websites, or external error lookup. Do not use this for ordinary local project creation/build tasks.",
                    new JSONObject().put("type", "object")
                            .put("properties", new JSONObject()
                                    .put("query", new JSONObject().put("type", "string"))
                                    .put("site", new JSONObject().put("type", "string").put("description", "Optional domain, like example.com.")))
                            .put("required", new JSONArray().put("query"))
                            .put("additionalProperties", false)));
        }
        tools.put(aiTool("create_file", "Create or replace a file in the project folder or app home.",
                new JSONObject().put("type", "object")
                        .put("properties", new JSONObject()
                                .put("path", new JSONObject().put("type", "string"))
                                .put("content", new JSONObject().put("type", "string")))
                        .put("required", new JSONArray().put("path").put("content"))
                        .put("additionalProperties", false)));
        tools.put(aiTool("edit_file", "Replace exact text in a file. Use this for surgical edits.",
                new JSONObject().put("type", "object")
                        .put("properties", new JSONObject()
                                .put("path", new JSONObject().put("type", "string"))
                                .put("search_text", new JSONObject().put("type", "string"))
                                .put("replace_text", new JSONObject().put("type", "string")))
                        .put("required", new JSONArray().put("path").put("search_text").put("replace_text"))
                        .put("additionalProperties", false)));
        tools.put(aiTool("run_terminal", "Run a bounded non-interactive shell command in the Aqua app runtime. Use this to compile, run tests, or inspect command output. Commands execute under the project folder or app home with PREFIX, HOME, PATH, and LD_LIBRARY_PATH configured.",
                new JSONObject().put("type", "object")
                        .put("properties", new JSONObject()
                                .put("command", new JSONObject().put("type", "string").put("description", "Shell command to run. Keep it non-interactive and under 8 seconds."))
                                .put("cwd", new JSONObject().put("type", "string").put("description", "Optional working directory under the project or app home.")))
                        .put("required", new JSONArray().put("command"))
                        .put("additionalProperties", false)));
        if (aiAptToolsEnabled && isAptAvailable()) {
            tools.put(aiTool("apt_search", "Search the Aqua apt repository.",
                    new JSONObject().put("type", "object")
                            .put("properties", new JSONObject().put("query", new JSONObject().put("type", "string")))
                            .put("required", new JSONArray().put("query"))
                            .put("additionalProperties", false)));
        }
        return tools;
    }

    private JSONObject aiTool(String name, String description, JSONObject parameters) throws Exception {
        return new JSONObject()
                .put("type", "function")
                .put("function", new JSONObject()
                        .put("name", name)
                        .put("description", description)
                        .put("parameters", parameters));
    }

    private JSONObject parseOpenAiAgentMessage(JSONObject message, String response, AiStreamCallback callback) throws Exception {
        if (message == null) throw new IOException("missing model message: " + safeAiDiagnostic(response, 180));
        String content = message.optString("content", "").trim();
        JSONArray toolCalls = message.optJSONArray("tool_calls");
        JSONArray actions = new JSONArray();
        if (toolCalls != null) {
            for (int i = 0; i < toolCalls.length(); i++) {
                JSONObject call = toolCalls.optJSONObject(i);
                JSONObject function = call == null ? null : call.optJSONObject("function");
                if (function == null) continue;
                String name = function.optString("name", "");
                if (name.isEmpty()) continue;
                String rawArgs = function.optString("arguments", "{}");
                JSONObject parameters = function.optJSONObject("parameters");
                if (parameters != null && (rawArgs == null || rawArgs.trim().isEmpty() || "{}".equals(rawArgs.trim()))) {
                    rawArgs = parameters.toString();
                }
                JSONObject args = parseToolArguments(rawArgs);
                args.put("type", name);
                actions.put(args);
            }
        }
        if (!content.isEmpty() && callback != null && actions.length() == 0) {
            callback.onPartial(content);
        }
        if (content.isEmpty() && actions.length() == 0) throw new IOException("empty model content: " + safeAiDiagnostic(response, 180));
        return new JSONObject().put("message", content).put("actions", actions);
    }

    private JSONObject parseToolArguments(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new JSONObject();
        String cleaned = raw.trim()
                .replace('\u201c', '"')
                .replace('\u201d', '"')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'');
        try {
            Object parsed = new JSONTokener(cleaned).nextValue();
            if (parsed instanceof JSONObject) return (JSONObject) parsed;
            if (parsed instanceof String) return parseToolArguments((String) parsed);
        } catch (Exception ignored) {
        }
        try {
            return parseDirtyJsonObject(cleaned);
        } catch (Exception ignored) {
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            try {
                String objectText = cleaned.substring(start, end + 1)
                        .replaceAll(",\\s*([}\\]])", "$1");
                return new JSONObject(objectText);
            } catch (Exception ignored) {
            }
        }
        try {
            JSONObject args = new JSONObject();
            Matcher matcher = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*[:=]\\s*\"([^\"]*)\"").matcher(cleaned);
            while (matcher.find()) args.put(matcher.group(1), matcher.group(2));
            return args;
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private JSONObject parseDirtyJsonObject(String raw) throws Exception {
        String objectText = extractBalancedJsonObject(raw);
        if (objectText.isEmpty()) throw new IOException("no JSON object found");
        ArrayList<String> candidates = new ArrayList<>();
        candidates.add(objectText);
        String normalized = objectText
                .replace('\u201c', '"')
                .replace('\u201d', '"')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'')
                .replace("\u00a0", " ");
        candidates.add(normalized);
        String commentsStripped = stripJsonComments(normalized);
        candidates.add(commentsStripped);
        String separated = replaceDirtyJsonSeparators(commentsStripped);
        candidates.add(separated);
        String quotedKeys = quoteBareJsonKeys(separated);
        candidates.add(quotedKeys);
        String singleFixed = singleQuotedJsonStringsToDouble(quotedKeys);
        candidates.add(singleFixed);
        candidates.add(removeTrailingJsonCommas(singleFixed));
        Exception lastError = null;
        for (String candidate : candidates) {
            if (candidate == null || candidate.trim().isEmpty()) continue;
            try {
                Object parsed = new JSONTokener(candidate.trim()).nextValue();
                if (parsed instanceof JSONObject) return (JSONObject) parsed;
                if (parsed instanceof String) return parseDirtyJsonObject((String) parsed);
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw lastError == null ? new IOException("dirty JSON parse failed") : lastError;
    }

    private String extractBalancedJsonObject(String raw) {
        if (raw == null) return "";
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("(?s)^```[A-Za-z0-9_-]*\\n?", "");
            text = text.replaceFirst("(?s)\\n?```$", "").trim();
        }
        int start = text.indexOf('{');
        if (start < 0) return "";
        boolean inDouble = false;
        boolean inSingle = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if ((inDouble || inSingle) && c == '\\') {
                escaped = true;
                continue;
            }
            if (!inSingle && c == '"') {
                inDouble = !inDouble;
                continue;
            }
            if (!inDouble && c == '\'') {
                inSingle = !inSingle;
                continue;
            }
            if (inDouble || inSingle) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return text.substring(start, i + 1);
            }
        }
        int end = text.lastIndexOf('}');
        return end > start ? text.substring(start, end + 1) : "";
    }

    private String stripJsonComments(String text) {
        StringBuilder out = new StringBuilder();
        boolean inDouble = false;
        boolean inSingle = false;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }
            if ((inDouble || inSingle) && c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }
            if (!inSingle && c == '"') inDouble = !inDouble;
            else if (!inDouble && c == '\'') inSingle = !inSingle;
            if (!inDouble && !inSingle && c == '/' && next == '/') {
                while (i < text.length() && text.charAt(i) != '\n') i++;
                if (i < text.length()) out.append('\n');
                continue;
            }
            if (!inDouble && !inSingle && c == '/' && next == '*') {
                i += 2;
                while (i + 1 < text.length() && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/')) i++;
                i++;
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private String replaceDirtyJsonSeparators(String text) {
        StringBuilder out = new StringBuilder();
        boolean inDouble = false;
        boolean inSingle = false;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }
            if ((inDouble || inSingle) && c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }
            if (!inSingle && c == '"') inDouble = !inDouble;
            else if (!inDouble && c == '\'') inSingle = !inSingle;
            if (!inDouble && !inSingle && c == ';') {
                int j = i + 1;
                while (j < text.length() && Character.isWhitespace(text.charAt(j))) j++;
                if (j < text.length() && (text.charAt(j) == '"' || text.charAt(j) == '\'' || Character.isLetter(text.charAt(j)))) {
                    out.append(',');
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private String quoteBareJsonKeys(String text) {
        StringBuilder out = new StringBuilder();
        boolean inDouble = false;
        boolean inSingle = false;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }
            if ((inDouble || inSingle) && c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }
            if (!inSingle && c == '"') {
                inDouble = !inDouble;
                out.append(c);
                continue;
            }
            if (!inDouble && c == '\'') {
                inSingle = !inSingle;
                out.append(c);
                continue;
            }
            if (!inDouble && !inSingle && isBareJsonKeyStart(text, i)) {
                int keyStart = i;
                int keyEnd = i + 1;
                while (keyEnd < text.length()) {
                    char k = text.charAt(keyEnd);
                    if (!(Character.isLetterOrDigit(k) || k == '_' || k == '-')) break;
                    keyEnd++;
                }
                int colon = keyEnd;
                while (colon < text.length() && Character.isWhitespace(text.charAt(colon))) colon++;
                if (colon < text.length() && text.charAt(colon) == ':') {
                    out.append('"').append(text, keyStart, keyEnd).append('"');
                    i = keyEnd - 1;
                    continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private boolean isBareJsonKeyStart(String text, int index) {
        char c = text.charAt(index);
        if (!(Character.isLetter(c) || c == '_')) return false;
        int prev = index - 1;
        while (prev >= 0 && Character.isWhitespace(text.charAt(prev))) prev--;
        return prev >= 0 && (text.charAt(prev) == '{' || text.charAt(prev) == ',');
    }

    private String singleQuotedJsonStringsToDouble(String text) {
        StringBuilder out = new StringBuilder();
        boolean inDouble = false;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }
            if (inDouble && c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }
            if (c == '"') {
                inDouble = !inDouble;
                out.append(c);
                continue;
            }
            if (!inDouble && c == '\'') {
                out.append('"');
                boolean singleEscaped = false;
                for (i = i + 1; i < text.length(); i++) {
                    char s = text.charAt(i);
                    if (singleEscaped) {
                        out.append(s == '\'' ? '\'' : s);
                        singleEscaped = false;
                    } else if (s == '\\') {
                        out.append('\\');
                        singleEscaped = true;
                    } else if (s == '\'') {
                        break;
                    } else if (s == '"') {
                        out.append("\\\"");
                    } else {
                        out.append(s);
                    }
                }
                out.append('"');
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private String removeTrailingJsonCommas(String text) {
        StringBuilder out = new StringBuilder();
        boolean inDouble = false;
        boolean inSingle = false;
        boolean escaped = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                out.append(c);
                escaped = false;
                continue;
            }
            if ((inDouble || inSingle) && c == '\\') {
                out.append(c);
                escaped = true;
                continue;
            }
            if (!inSingle && c == '"') inDouble = !inDouble;
            else if (!inDouble && c == '\'') inSingle = !inSingle;
            if (!inDouble && !inSingle && c == ',') {
                int j = i + 1;
                while (j < text.length() && Character.isWhitespace(text.charAt(j))) j++;
                if (j < text.length() && (text.charAt(j) == '}' || text.charAt(j) == ']')) continue;
            }
            out.append(c);
        }
        return out.toString();
    }

    private JSONObject parseAiJsonEnvelope(String raw) throws Exception {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) throw new IOException("empty AI response");
        if (text.startsWith("```")) {
            text = text.replaceFirst("(?s)^```[A-Za-z0-9_-]*\\n?", "");
            text = text.replaceFirst("(?s)\\n?```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return new JSONObject()
                    .put("message", text)
                    .put("actions", new JSONArray());
        }
        text = text.substring(start, end + 1);
        JSONObject json;
        try {
            json = new JSONObject(text);
        } catch (Exception badJson) {
            try {
                json = parseDirtyJsonObject(text);
            } catch (Exception stillBad) {
                return new JSONObject()
                        .put("message", "I couldn't parse the model's tool JSON safely, so I skipped the unsafe edit response. Try again or switch to a stronger tool-calling model.")
                        .put("actions", new JSONArray());
            }
        }
        JSONArray actions = normalizeAiActions(json.opt("actions"));
        if (actions.length() == 0 && json.has("type")) {
            actions.put(normalizeAiActionObject(json));
        }
        json.put("actions", actions);
        if (!json.has("message")) json.put("message", "");
        return json;
    }

    private JSONArray normalizeAiActions(Object raw) {
        JSONArray out = new JSONArray();
        if (raw == null || raw == JSONObject.NULL) return out;
        try {
            if (raw instanceof JSONArray) {
                JSONArray array = (JSONArray) raw;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.opt(i);
                    if (item instanceof JSONObject) {
                        out.put(normalizeAiActionObject((JSONObject) item));
                    } else if (item instanceof String) {
                        JSONObject parsed = parseToolArguments((String) item);
                        if (parsed.has("type")) out.put(normalizeAiActionObject(parsed));
                    }
                }
            } else if (raw instanceof JSONObject) {
                out.put(normalizeAiActionObject((JSONObject) raw));
            } else if (raw instanceof String) {
                String text = ((String) raw).trim();
                if (text.startsWith("[")) {
                    Object parsed = new JSONTokener(text).nextValue();
                    if (parsed instanceof JSONArray) return normalizeAiActions(parsed);
                }
                JSONObject parsed = parseToolArguments(text);
                if (parsed.has("type")) out.put(normalizeAiActionObject(parsed));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private JSONObject normalizeAiActionObject(JSONObject action) throws Exception {
        JSONObject normalized = new JSONObject(action.toString());
        if (!normalized.has("type")) {
            String alias = normalized.optString("tool", normalized.optString("action", normalized.optString("name", "")));
            if (!alias.trim().isEmpty()) normalized.put("type", alias.trim());
        }
        if (!normalized.has("path")) {
            String pathAlias = normalized.optString("file", normalized.optString("file_path", normalized.optString("filename", "")));
            if (!pathAlias.trim().isEmpty()) normalized.put("path", pathAlias.trim());
        }
        if (!normalized.has("search_text") && normalized.has("old_text")) {
            normalized.put("search_text", normalized.optString("old_text", ""));
        }
        if (!normalized.has("replace_text") && normalized.has("new_text")) {
            normalized.put("replace_text", normalized.optString("new_text", ""));
        }
        if (!normalized.has("content") && normalized.has("file_content")) {
            normalized.put("content", normalized.optString("file_content", ""));
        }
        return normalized;
    }

    private String executeAiToolAction(JSONObject action, AiAgentRunState runState) {
        try {
            String type = action.optString("type", "");
            if ("ide_context".equals(type)) {
                if (runState != null) runState.sawIdeContext = true;
                return aiIdeContext();
            }
            if ("list_files".equals(type)) {
                File root = resolveAiPath(action.optString("path", ""));
                String result = aiListFiles(root, 0, 160);
                if (runState != null && root != null) runState.listedDirectories.add(canonicalKey(root));
                return result;
            }
            if ("read_file".equals(type)) {
                File target = resolveAiPath(action.optString("path", ""));
                String result = aiReadFile(target);
                if (runState != null && target != null && target.isFile() && !result.startsWith("File too large")) {
                    runState.readSnapshots.put(canonicalKey(target),
                            new String(readFileBytes(target, 2 * 1024 * 1024), StandardCharsets.UTF_8));
                }
                return result;
            }
            if ("apt_search".equals(type)) return executeAiAptSearch(action.optString("query", ""));
            if ("run_terminal".equals(type)) {
                String result = executeAiTerminalCommand(action.optString("command", ""), action.optString("cwd", ""));
                if (runState != null && !result.startsWith("Blocked ")
                        && !result.startsWith("Missing ")
                        && !result.startsWith("Working directory not found")
                        && !result.startsWith("Tool failed")) {
                    runState.terminalRuns++;
                }
                return result;
            }
            if ("web_search".equals(type) || "search_web".equals(type) || "site_search".equals(type)) {
                if (runState != null && runState.localProjectBuildIntent && !runState.webNeeded) {
                    return "Guard blocked web_search: this is a local project build request. Inspect local files and create/edit project files first; only search the web if the user asks for current docs or a specific external error.";
                }
                return executeAiWebSearch(action.optString("query", ""), action.optString("site", ""));
            }
            if ("create_file".equals(type)) {
                File target = resolveAiPath(action.optString("path", ""));
                if (target == null) return "Blocked path";
                String guard = validateAiFileWrite(action, target, runState);
                if (!guard.isEmpty()) return guard;
                boolean existedBefore = target.isFile();
                String oldText = existedBefore ? new String(readFileBytes(target, 2 * 1024 * 1024), StandardCharsets.UTF_8) : "";
                File parent = target.getParentFile();
                if (parent != null) parent.mkdirs();
                String content = sanitizeToolFileContent(target, action.optString("content", ""));
                rememberAiOriginalSnapshot(target, oldText, existedBefore, runState);
                writeTextChecked(target, content);
                if (runState != null) runState.editsApplied++;
                recordAiFileChange(target, originalAiSnapshotText(target, oldText, runState), content,
                        originalAiSnapshotExisted(target, existedBefore, runState));
                if (isCurrentEditorFile(target)) {
                    refreshCurrentEditorAfterAiWrite(target, content);
                }
                return "Created " + target.getAbsolutePath();
            }
            if ("edit_file".equals(type)) {
                File target = resolveAiPath(action.optString("path", ""));
                if (target == null || !target.isFile()) return "File not found or blocked";
                String guard = validateAiFileWrite(action, target, runState);
                if (!guard.isEmpty()) return guard;
                String oldText = new String(readFileBytes(target, 2 * 1024 * 1024), StandardCharsets.UTF_8);
                String search = action.optString("search_text", "");
                String replace = sanitizeToolFileContent(target, action.optString("replace_text", ""));
                if (search.isEmpty() || !oldText.contains(search)) return "Guard blocked edit: exact search_text was not found. Read the file again and use a smaller exact snippet.";
                if (countOccurrences(oldText, search) != 1) return "Guard blocked edit: search_text matches multiple places. Read the file and provide a more specific exact snippet.";
                String updated = oldText.replace(search, replace);
                rememberAiOriginalSnapshot(target, oldText, true, runState);
                writeTextChecked(target, updated);
                if (runState != null) runState.editsApplied++;
                recordAiFileChange(target, originalAiSnapshotText(target, oldText, runState), updated,
                        originalAiSnapshotExisted(target, true, runState));
                if (isCurrentEditorFile(target)) {
                    refreshCurrentEditorAfterAiWrite(target, updated);
                }
                return "Edited " + target.getName();
            }
            return "Unknown tool: " + type;
        } catch (Exception e) {
            return "Tool failed: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    private String sanitizeToolFileContent(File target, String content) {
        String text = content == null ? "" : content.replace("\r\n", "\n").replace('\r', '\n');
        if (!isLikelyCodeFile(target)) return text;
        return stripWrappingCodeFence(text);
    }

    private void writeTextChecked(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("could not create parent directory: " + parent.getAbsolutePath());
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
        }
    }

    private File aiFileChangesLedger() {
        return new File(getFilesDir(), "ai_file_changes.json");
    }

    private void loadAiFileChanges() {
        aiFileChanges.clear();
        File ledger = aiFileChangesLedger();
        if (!ledger.isFile()) return;
        try {
            String json = new String(readFileBytes(ledger, 6 * 1024 * 1024), StandardCharsets.UTF_8);
            JSONArray entries = new JSONArray(json);
            for (int i = 0; i < entries.length(); i++) {
                JSONObject item = entries.optJSONObject(i);
                if (item == null) continue;
                String path = item.optString("path", "");
                if (path.trim().isEmpty()) continue;
                AiFileChange change = new AiFileChange(
                        item.optString("before", ""),
                        item.optString("after", ""),
                        item.optBoolean("existed_before", true));
                change.markers = buildAiDiffMarkers(change.before, change.after);
                aiFileChanges.put(path, change);
            }
        } catch (Exception e) {
            Log.w(TAG_AI, "Could not load AI change ledger", e);
            aiFileChanges.clear();
        }
    }

    private void saveAiFileChanges() {
        File ledger = aiFileChangesLedger();
        if (aiFileChanges.isEmpty()) {
            if (ledger.exists() && !ledger.delete()) {
                Log.w(TAG_AI, "Could not delete empty AI change ledger");
            }
            runOnUiThread(this::updateAiRevertChangesButton);
            return;
        }
        try {
            JSONArray entries = new JSONArray();
            ArrayList<String> keys = new ArrayList<>(aiFileChanges.keySet());
            keys.sort(String::compareTo);
            for (String path : keys) {
                AiFileChange change = aiFileChanges.get(path);
                if (change == null) continue;
                JSONObject item = new JSONObject();
                item.put("path", path);
                item.put("before", change.before);
                item.put("after", change.after);
                item.put("existed_before", change.existedBefore);
                entries.put(item);
            }
            writeTextChecked(ledger, entries.toString());
        } catch (Exception e) {
            Log.w(TAG_AI, "Could not save AI change ledger", e);
        }
        runOnUiThread(this::updateAiRevertChangesButton);
    }

    private String validateAiFileWrite(JSONObject action, File target, AiAgentRunState runState) throws IOException {
        if (target == null) return "Guard blocked edit: target path is outside the project/app home.";
        if (runState == null) return "";
        if (runState.editsApplied >= AI_AGENT_MAX_EDITS) {
            return "Guard blocked edit: too many files were changed in one request. Ask the user before touching more files.";
        }
        String type = action == null ? "" : action.optString("type", "");
        String key = canonicalKey(target);
        if (key == null) return "Guard blocked edit: could not verify target path.";
        boolean targetExists = target.isFile();
        String proposed = "create_file".equals(type)
                ? action.optString("content", "")
                : action.optString("replace_text", "");
        if (proposed != null && proposed.getBytes(StandardCharsets.UTF_8).length > AI_AGENT_MAX_EDIT_BYTES) {
            return "Guard blocked edit: proposed change is too large. Use smaller surgical edits or ask the user for approval.";
        }
        if ("edit_file".equals(type)) {
            String readSnapshot = runState.readSnapshots.get(key);
            if (readSnapshot == null) {
                return "Guard blocked edit: read_file(\"" + aiRelativePath(target) + "\") must be called before editing this file.";
            }
            String current = new String(readFileBytes(target, 2 * 1024 * 1024), StandardCharsets.UTF_8);
            if (!current.equals(readSnapshot)) {
                return "Guard blocked edit: file changed after it was read. Read it again, then retry the exact replacement.";
            }
            String search = action.optString("search_text", "");
            if (search.trim().isEmpty()) return "Guard blocked edit: search_text is empty.";
            if (search.equals(action.optString("replace_text", ""))) return "Guard blocked edit: replacement is identical to the search text.";
            return "";
        }
        if ("create_file".equals(type)) {
            if (targetExists && !runState.readSnapshots.containsKey(key)) {
                return "Guard blocked edit: this file already exists, so read_file(\"" + aiRelativePath(target) + "\") is required before replacing it.";
            }
            File parent = target.getParentFile();
            String parentKey = canonicalKey(parent);
            if (!targetExists && !runState.sawIdeContext && (parentKey == null || !runState.listedDirectories.contains(parentKey))) {
                return "Guard blocked edit: inspect the destination first with ide_context or list_files(\"" + aiRelativePath(parent) + "\").";
            }
        }
        return "";
    }

    private void rememberAiOriginalSnapshot(File target, String oldText, boolean existedBefore, AiAgentRunState runState) {
        if (target == null || runState == null) return;
        String key = canonicalKey(target);
        if (key != null && !runState.originalSnapshots.containsKey(key)) {
            runState.originalSnapshots.put(key, oldText == null ? "" : oldText);
            runState.originalExists.put(key, existedBefore);
        }
    }

    private String originalAiSnapshotText(File target, String fallback, AiAgentRunState runState) {
        if (target == null || runState == null) return fallback == null ? "" : fallback;
        String key = canonicalKey(target);
        String original = key == null ? null : runState.originalSnapshots.get(key);
        return original == null ? (fallback == null ? "" : fallback) : original;
    }

    private boolean originalAiSnapshotExisted(File target, boolean fallback, AiAgentRunState runState) {
        if (target == null || runState == null) return fallback;
        String key = canonicalKey(target);
        Boolean existed = key == null ? null : runState.originalExists.get(key);
        return existed == null ? fallback : existed;
    }

    private int countOccurrences(String text, String needle) {
        if (text == null || needle == null || needle.isEmpty()) return 0;
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
            if (count > 1) break;
        }
        return count;
    }

    private String aiRelativePath(File file) {
        if (file == null) return ".";
        try {
            String path = file.getCanonicalPath();
            File project = projectRoot();
            if (project != null) {
                String root = project.getCanonicalPath();
                if (path.equals(root)) return ".";
                if (path.startsWith(root + File.separator)) return path.substring(root.length() + 1);
            }
            String home = homeRoot.getCanonicalPath();
            if (path.equals(home)) return ".";
            if (path.startsWith(home + File.separator)) return path.substring(home.length() + 1);
        } catch (IOException ignored) {
        }
        return file.getName();
    }

    private boolean isLikelyCodeFile(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(Locale.US);
        return name.matches(".*\\.(py|c|cc|cpp|cxx|h|hpp|java|kt|js|ts|tsx|jsx|sh|bash|rs|go|rb|lua|json|xml|gradle|md)$");
    }

    private String stripWrappingCodeFence(String content) {
        if (content == null) return "";
        String text = content.replace("\r\n", "\n").replace('\r', '\n').trim();
        Matcher matcher = Pattern.compile("(?s)^```[A-Za-z0-9_+\\-.]*\\s*\\n(.*?)\\n?```\\s*$").matcher(text);
        if (matcher.matches()) return matcher.group(1).trim();
        return content;
    }

    private String languageForPath(String path) {
        String name = path == null ? "" : path.toLowerCase(Locale.US);
        if (name.endsWith(".py")) return "python";
        if (name.endsWith(".c") || name.endsWith(".h")) return "c";
        if (name.endsWith(".cpp") || name.endsWith(".cc") || name.endsWith(".hpp")) return "cpp";
        if (name.endsWith(".java")) return "java";
        if (name.endsWith(".kt")) return "kotlin";
        if (name.endsWith(".js") || name.endsWith(".jsx")) return "javascript";
        if (name.endsWith(".ts") || name.endsWith(".tsx")) return "typescript";
        if (name.endsWith(".sh") || name.endsWith(".bash")) return "bash";
        if (name.endsWith(".json")) return "json";
        return "";
    }

    private boolean isCurrentEditorFile(File target) {
        if (target == null) return false;
        try {
            String targetPath = target.getCanonicalPath();
            String openedPath = prefs.getString("file_path", "");
            if (openedPath != null && !openedPath.trim().isEmpty()
                    && targetPath.equals(new File(openedPath).getCanonicalPath())) {
                return true;
            }
            return targetPath.equals(new File(homeRoot, currentFileName()).getCanonicalPath());
        } catch (IOException ignored) {
            return false;
        }
    }

    private void recordAiFileChange(File file, String before, String after, boolean existedBefore) {
        String key = canonicalKey(file);
        if (key == null) return;
        AiFileChange change = new AiFileChange(before == null ? "" : before, after == null ? "" : after, existedBefore);
        change.markers = buildAiDiffMarkers(change.before, change.after);
        aiRedoFileChanges.clear();
        aiFileChanges.put(key, change);
        saveAiFileChanges();
        runOnUiThread(() -> {
            applySoraSyntaxHighlighting();
            if (projectPanelOpen) refreshProjectPanel();
        });
    }

    private void refreshCurrentEditorAfterAiWrite(File target, String content) {
        runOnUiThread(() -> {
            prefs.edit().putString("code", content == null ? "" : content).apply();
            if (editor == null || !isCurrentEditorFile(target)) {
                if (isCurrentEditorFile(target)) showEditor();
                return;
            }
            int cursor = Math.min(editorSelectionStart(), content == null ? 0 : content.length());
            editor.setText(content == null ? "" : content);
            setEditorSelection(cursor);
            applySoraSyntaxHighlighting();
            updatePanelStatus();
            refreshCompletions();
            editor.invalidate();
            editor.postDelayed(() -> {
                applySoraSyntaxHighlighting();
                editor.invalidate();
            }, 32);
        });
    }

    private AiFileChange aiFileChangeFor(File file) {
        String key = canonicalKey(file);
        return key == null ? null : aiFileChanges.get(key);
    }

    private String canonicalKey(File file) {
        if (file == null) return null;
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    private boolean hasAiAddedChange(File file) {
        return hasAiChange(file, true);
    }

    private boolean hasAiDeletedChange(File file) {
        return hasAiChange(file, false);
    }

    private boolean hasAiChange(File file, boolean added) {
        String key = canonicalKey(file);
        if (key == null) return false;
        if (file != null && file.isDirectory()) {
            String prefix = key + File.separator;
            for (String changedPath : aiFileChanges.keySet()) {
                if (changedPath.startsWith(prefix) && aiChangeHas(aiFileChanges.get(changedPath), added)) return true;
            }
            return false;
        }
        return aiChangeHas(aiFileChanges.get(key), added);
    }

    private boolean aiChangeHas(AiFileChange change, boolean added) {
        if (change == null) return false;
        if (change.markers == null) change.markers = buildAiDiffMarkers(change.before, change.after);
        return added ? !change.markers.addedLines.isEmpty() : !change.markers.deletedAnchors.isEmpty();
    }

    private AiDiffMarkers buildAiDiffMarkers(String before, String after) {
        String[] oldLines = splitLines(before);
        String[] newLines = splitLines(after);
        int[][] lcs = new int[oldLines.length + 1][newLines.length + 1];
        for (int i = oldLines.length - 1; i >= 0; i--) {
            for (int j = newLines.length - 1; j >= 0; j--) {
                if (oldLines[i].equals(newLines[j])) lcs[i][j] = lcs[i + 1][j + 1] + 1;
                else lcs[i][j] = Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        AiDiffMarkers markers = new AiDiffMarkers();
        int i = 0;
        int j = 0;
        while (i < oldLines.length || j < newLines.length) {
            if (i < oldLines.length && j < newLines.length && oldLines[i].equals(newLines[j])) {
                i++;
                j++;
            } else if (j < newLines.length && (i >= oldLines.length || lcs[i][j + 1] >= lcs[i + 1][j])) {
                markers.addedLines.add(j);
                j++;
            } else if (i < oldLines.length) {
                markers.deletedAnchors.add(Math.max(0, Math.min(j, Math.max(0, newLines.length - 1))));
                i++;
            }
        }
        return markers;
    }

    private String[] splitLines(String text) {
        if (text == null || text.isEmpty()) return new String[0];
        return text.split("\\n", -1);
    }

    private String executeAiAptSearch(String query) throws IOException, InterruptedException {
        if (!aiAptToolsEnabled || !isAptAvailable()) return "APT tools disabled or unavailable";
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return "Missing apt search query";
        File realBinRoot = new File(prefixRealRoot, "bin");
        File realEtcRoot = new File(prefixRealRoot, "etc");
        ProcessBuilder builder = new ProcessBuilder(new File(realBinRoot, "apt").getAbsolutePath(), "search", q);
        builder.environment().put("PREFIX", prefixRoot.getAbsolutePath());
        builder.environment().put("HOME", homeRoot.getAbsolutePath());
        builder.environment().put("ANDROPY_PREFIX_REAL", prefixRealRoot.getAbsolutePath());
        builder.environment().put("ANDROPY_HOME_REAL", homeRealRoot.getAbsolutePath());
        builder.environment().put("APT_CONFIG", new File(realEtcRoot, "apt/apt.conf").getAbsolutePath());
        builder.environment().put("PATH", realBinRoot.getAbsolutePath() + ":/system/bin:/system/xbin");
        Process process = builder.start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream input = process.getInputStream()) {
                byte[] buffer = new byte[2048];
                int read;
                while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            } catch (IOException ignored) {
            }
        });
        reader.start();
        long deadline = SystemClock.uptimeMillis() + 8000;
        while (SystemClock.uptimeMillis() < deadline) {
            try {
                int exit = process.exitValue();
                reader.join(300);
                return "apt search exit " + exit + "\n" + compactLogText(output.toString());
            } catch (IllegalThreadStateException running) {
                SystemClock.sleep(120);
            }
        }
        process.destroy();
        return "apt search timed out";
    }

    private String executeAiTerminalCommand(String command, String cwd) throws IOException, InterruptedException {
        String cmd = command == null ? "" : command.trim();
        if (cmd.isEmpty()) return "Missing terminal command";
        String blocked = validateAiTerminalCommand(cmd);
        if (!blocked.isEmpty()) return blocked;
        File workDir = resolveAiPath(cwd == null || cwd.trim().isEmpty() ? "." : cwd);
        if (workDir == null) return "Blocked working directory";
        if (workDir.isFile()) workDir = workDir.getParentFile();
        if (workDir == null || !workDir.isDirectory()) return "Working directory not found";

        File realBinRoot = new File(prefixRealRoot, "bin");
        File shell = new File(realBinRoot, "bash");
        ArrayList<String> args = new ArrayList<>();
        if (shell.canExecute()) {
            args.add(shell.getAbsolutePath());
            args.add("-lc");
        } else {
            args.add("/system/bin/sh");
            args.add("-c");
        }
        args.add(cmd);
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(workDir);
        applyRuntimeEnvironment(builder);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream input = process.getInputStream()) {
                byte[] buffer = new byte[2048];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    if (output.size() < 12000) output.write(buffer, 0, read);
                }
            } catch (IOException ignored) {
            }
        }, "andropy-ai-terminal-reader");
        reader.start();
        long deadline = SystemClock.uptimeMillis() + 8000;
        while (SystemClock.uptimeMillis() < deadline) {
            try {
                int exit = process.exitValue();
                reader.join(300);
                return "terminal exit " + exit + "\n$ " + cmd + "\n" + compactLogText(output.toString(), 4000);
            } catch (IllegalThreadStateException running) {
                SystemClock.sleep(120);
            }
        }
        process.destroy();
        process.destroyForcibly();
        reader.join(300);
        return "terminal timed out\n$ " + cmd + "\n" + compactLogText(output.toString(), 4000);
    }

    private String validateAiTerminalCommand(String cmd) {
        String lowered = cmd == null ? "" : cmd.trim().toLowerCase(Locale.US);
        if (lowered.length() > 600) return "Blocked terminal command: command is too long for the bounded agent runner.";
        if (lowered.contains("\n") || lowered.contains("\r")) {
            return "Blocked terminal command: multiline shell scripts are not allowed. Create a file, then run it.";
        }
        if (lowered.matches("^(man|less|more|vi|vim|nano|emacs|top|htop|watch|tail\\s+-f)\\b.*")
                || lowered.matches("^(python|bash|sh|zsh|fish|node|irb|ruby)\\s*$")) {
            return "Blocked interactive terminal command. Use a non-interactive command such as `ls --help`, `python -c ...`, or a compile/test command.";
        }
        if (lowered.contains(":(){") || lowered.contains(" fork ") || lowered.contains("mkfs")
                || lowered.matches("(?s).*(^|[;&|]\\s*)(su|sudo|reboot|halt|poweroff|mount|umount|setprop|pm|am|settings|input)\\b.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)(pkill|killall|kill\\s+-9)\\b.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)dd\\s+.*\\bof=.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)chmod\\s+.*\\s-r\\b.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)chown\\b.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)rm\\s+.*-[a-z]*r[a-z]*f?.*")
                || lowered.matches("(?s).*(^|[;&|]\\s*)rm\\s+.*-[a-z]*f[a-z]*r.*")) {
            return "Blocked unsafe terminal command";
        }
        if (lowered.matches("(?s).*(^|[;&|]\\s*)cd\\s+/.*")) {
            return "Blocked terminal command: use cwd instead of changing to absolute paths.";
        }
        String sanitized = lowered.replace("/dev/null", "");
        if (sanitized.matches("(?s).*(/data/data|/sdcard|/storage|/system|/vendor|/proc|/dev)\\b.*")) {
            return "Blocked terminal command: absolute Android paths are not allowed in agent terminal commands.";
        }
        return "";
    }

    private String executeAiWebSearch(String query, String site) throws IOException {
        String q = query == null ? "" : query.trim();
        String domain = site == null ? "" : site.trim();
        if (q.isEmpty()) return "Missing web search query";
        if (!domain.isEmpty() && !q.toLowerCase(Locale.US).contains("site:")) {
            q = "site:" + domain + " " + q;
        }
        String url = "https://duckduckgo.com/html/?q=" + URLEncoder.encode(q, "UTF-8");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "AquaIDE/1.0");
        connection.setRequestProperty("Accept", "text/html");
        int status = connection.getResponseCode();
        String response = readStreamText(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
        connection.disconnect();
        if (status < 200 || status >= 300) return "web search HTTP " + status;
        return compactSearchHtml(response);
    }

    private String compactSearchHtml(String html) {
        if (html == null || html.isEmpty()) return "No search results";
        String text = html
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</a>", "\n")
                .replaceAll("(?s)<[^>]+>", " ")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#x27;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n\\s+", "\n");
        StringBuilder out = new StringBuilder();
        String[] lines = text.split("\\n");
        for (String raw : lines) {
            String line = raw == null ? "" : raw.trim();
            if (line.length() < 24 || line.toLowerCase(Locale.US).contains("duckduckgo")) continue;
            if (out.indexOf(line) >= 0) continue;
            out.append(line.length() > 180 ? line.substring(0, 180) : line).append('\n');
            if (out.length() > 900) break;
        }
        String result = out.toString().trim();
        return result.isEmpty() ? "No search results" : result;
    }

    private File resolveAiPath(String rawPath) throws IOException {
        File base = projectRoot();
        if (base == null) base = homeRoot;
        String raw = rawPath == null || rawPath.trim().isEmpty() ? "." : rawPath.trim();
        File target = raw.startsWith("/") ? new File(raw) : new File(base, raw);
        String canonical = target.getCanonicalPath();
        String project = base.getCanonicalPath();
        String home = homeRoot.getCanonicalPath();
        if (canonical.equals(project) || canonical.startsWith(project + File.separator)
                || canonical.equals(home) || canonical.startsWith(home + File.separator)) {
            return target;
        }
        String openedPath = prefs.getString("file_path", "");
        if (openedPath != null && !openedPath.trim().isEmpty()) {
            File opened = new File(openedPath);
            File openedParent = opened.getParentFile();
            String openedCanonical = opened.getCanonicalPath();
            String openedParentCanonical = openedParent == null ? "" : openedParent.getCanonicalPath();
            if (canonical.equals(openedCanonical)
                    || (!openedParentCanonical.isEmpty()
                    && (canonical.equals(openedParentCanonical) || canonical.startsWith(openedParentCanonical + File.separator)))) {
                return target;
            }
        }
        return null;
    }

    private String aiIdeContext() {
        String code = editorText();
        int cursor = editorSelectionStart();
        int start = Math.max(0, cursor - 900);
        int end = Math.min(code.length(), cursor + 600);
        File project = projectRoot();
        return "file=" + currentFileName()
                + "\npath=" + prefs.getString("file_path", "")
                + "\nproject=" + (project == null ? "" : project.getAbsolutePath())
                + "\ncursor=" + cursor
                + "\nopened=" + prefs.getString("opened_files", "").replace('\n', ';')
                + "\nexcerpt:\n" + code.substring(start, end);
    }

    private String aiReadFile(File file) throws IOException {
        if (file == null || !file.isFile()) return "File not found or blocked";
        byte[] data = readFileBytes(file, 128 * 1024);
        return "=== " + file.getName() + " ===\n" + new String(data, StandardCharsets.UTF_8);
    }

    private String aiListFiles(File dir, int depth, int limit) {
        if (dir == null || !dir.isDirectory()) return "Directory not found or blocked";
        StringBuilder out = new StringBuilder();
        aiListFilesInto(out, dir, depth, limit, new int[]{0});
        return out.toString();
    }

    private void aiListFilesInto(StringBuilder out, File dir, int depth, int limit, int[] count) {
        if (count[0] >= limit || depth > 4) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        Arrays.sort(files, Comparator.comparing((File f) -> !f.isDirectory()).thenComparing(File::getName));
        for (File file : files) {
            if (count[0] >= limit || file.isHidden() || shouldHideProjectFile(file)) continue;
            for (int i = 0; i < depth; i++) out.append("  ");
            out.append(file.isDirectory() ? "📁 " : "📄 ").append(file.getName()).append('\n');
            count[0]++;
            if (file.isDirectory()) aiListFilesInto(out, file, depth + 1, limit, count);
        }
    }

    private void runCurrentFile() {
        hideKeyboard();
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        String code = editor.getText().toString();
        String openedPath = prefs.getString("file_path", "");
        File script = openedPath == null || openedPath.isEmpty() ? new File(homeRoot, currentFileName()) : new File(openedPath);
        File runner = new File(homeRoot, ".andropy-run-current.sh");
        try {
            writeTextChecked(script, code);
            writeTextChecked(runner, "#!/system/bin/sh\n"
                    + "python " + shellQuote(script.getAbsolutePath()) + "\n"
                    + "status=$?\n"
                    + "printf '\\n[python exit %s]\\n' \"$status\"\n"
                    + "printf 'Press Enter to return to editor...'\n"
                    + "IFS= read -r _\n"
                    + "exit \"$status\"\n");
        } catch (IOException failure) {
            Toast.makeText(this, "Run failed: " + failure.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        runner.setExecutable(true, false);
        prefs.edit()
                .putString("last_run", timestamp)
                .putString("code", code)
                .apply();
        terminalReturnToEditorOnExit = true;
        terminalStartupCommand = null;
        terminalStartupScript = runner.getAbsolutePath();
        setContentView(buildTerminalScreen());
        termuxTerminalView.requestFocus();
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    @Override
    public void onBackPressed() {
        if (bootstrapShowingOutput && bootstrapDownloading) {
            showBootstrapVisual();
            return;
        }
        if (completionPopup != null && completionPopup.isShowing()) {
            dismissCompletions();
            return;
        }
        if (projectPanelOpen) {
            closeProjectPanel();
            return;
        }
        if (aiChatOpen) {
            closeAiChat();
            return;
        }
        if (fileManagerVisible) {
            if (!selectedFilePaths.isEmpty()) {
                clearFileSelection();
                refreshFileManager();
                return;
            }
            if (fileManagerCurrentDir != null) {
                navigateFileManagerBack(fileManagerCurrentDir);
            } else {
                choosingProjectFolder = false;
                showEditor();
            }
            return;
        }
        if (terminalVisible) {
            showEditor();
            return;
        }
        if (opencamVisible) {
            showEditor();
            return;
        }
        if (aquaDisplayVisible) {
            showEditor();
            return;
        }
        if (settingsVisible) {
            if (!"root".equals(settingsPage)) {
                showSettings();
                return;
            }
            showEditor();
            return;
        }
        if (panelOpen) {
            closePanel();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        stopOpencamCamera();
        stopOpencamBridge();
        stopAquaDisplayBridge();
        stopTerminal();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 4205 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ensureOpencamCamera();
        } else if (requestCode == 4205 && opencamStatusText != null) {
            opencamStatusText.setText("opencam.display.buffer\ncamera permission denied");
        }
    }

    private void highlight(Editable editable) {
        // Sora owns the editor rendering layer now. TextMate/tree-sitter highlighting can
        // be wired here later without touching layout, IME, or gutter rendering.
    }

    private void refreshCompletions() {
        if (applyingCompletion || applyingHelperEdit || editor == null) return;
        String editable = editorText();
        int cursor = editorSelectionStart();
        if (cursor < 0 || cursor > editable.length() || cursor != editorSelectionEnd()) {
            dismissCompletions();
            updateGhostText("");
            return;
        }
        String prefix = completionPrefix(editable, cursor);

        boolean expressionContext = isExpressionCompletionContext(editable, cursor, prefix);
        ArrayList<CompletionItem> suggestions = buildCompletionItems(editable, prefix, expressionContext);
        if (prefix.length() < 2 && !prefix.equals(".") && !hasLocalCompletion(suggestions)) {
            dismissCompletions();
            updateGhostText("");
            return;
        }
        if (suggestions.isEmpty()) {
            dismissCompletions();
            updateGhostText("");
            return;
        }

        activeCompletions.clear();
        activeCompletions.addAll(suggestions);
        updateGhostText(ghostTextFor(suggestions.get(0), prefix));
        ensureCompletionPopup();
        completionList.removeAllViews();
        for (CompletionItem item : suggestions) completionList.addView(completionRow(item));

        int popupHeight = completionPopupHeight(suggestions.size());
        if (!completionPopup.isShowing()) {
            completionPopup.setWidth(Math.min(dp(300), Math.max(dp(220), editor.getWidth() - dp(72))));
            completionPopup.setHeight(popupHeight);
            completionPopup.showAtLocation(editor, Gravity.START | Gravity.TOP, completionX(cursor), completionY(cursor));
        } else {
            completionPopup.update(completionX(cursor), completionY(cursor), completionPopup.getWidth(), popupHeight);
        }
    }

    private void ensureCompletionPopup() {
        if (completionPopup != null) return;
        completionList = new LinearLayout(this);
        completionList.setOrientation(LinearLayout.VERTICAL);
        completionList.setPadding(0, dp(4), 0, dp(4));
        completionList.setBackground(roundRect(COMPLETION_BG, dp(6)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.addView(completionList, new ScrollView.LayoutParams(-1, -2));

        completionPopup = new PopupWindow(scroll, dp(280), dp(238), false);
        completionPopup.setOutsideTouchable(true);
        completionPopup.setClippingEnabled(true);
        completionPopup.setBackgroundDrawable(roundRect(COMPLETION_BG, dp(6)));
        if (Build.VERSION.SDK_INT >= 21) completionPopup.setElevation(dp(8));
    }

    private int completionPopupHeight(int rowCount) {
        int visibleRows = Math.max(1, Math.min(COMPLETION_MAX_VISIBLE_ROWS, rowCount));
        return dp(8) + visibleRows * dp(48);
    }

    private View completionRow(CompletionItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        row.setBackgroundColor(COMPLETION_BG);
        row.setOnClickListener(v -> applyCompletion(item));
        row.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) v.setBackgroundColor(COMPLETION_HOVER);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) v.setBackgroundColor(COMPLETION_BG);
            return false;
        });

        TextView label = new TextView(this);
        label.setText(item.label);
        label.setTextColor(TEXT);
        label.setTextSize(14);
        label.setTypeface(Typeface.MONOSPACE);
        label.setSingleLine(true);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        TextView kind = new TextView(this);
        kind.setText(item.kind);
        kind.setTextColor(MUTED);
        kind.setTextSize(11);
        kind.setSingleLine(true);
        row.addView(kind, new LinearLayout.LayoutParams(-2, -2));
        return row;
    }

    private ArrayList<CompletionItem> buildCompletionItems(String code, String prefix, boolean includeLocals) {
        String lower = prefix.toLowerCase(Locale.US);
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        ArrayList<CompletionItem> result = new ArrayList<>();

        for (CompletionItem snippet : PYTHON_SNIPPETS) {
            if (matchesCompletion(snippet.label, lower) && seen.add(snippet.label)) result.add(snippet);
        }
        if (includeLocals) {
            for (String symbol : collectPythonSymbols(code)) {
                if (matchesCompletion(symbol, lower) && seen.add(symbol)) result.add(new CompletionItem(symbol, symbol, "local"));
            }
        }
        for (String keyword : PYTHON_KEYWORDS) {
            if (matchesCompletion(keyword, lower) && seen.add(keyword)) result.add(new CompletionItem(keyword, keyword, "keyword"));
        }
        for (String builtin : PYTHON_BUILTINS) {
            if (matchesCompletion(builtin, lower) && seen.add(builtin)) {
                String insert = "print".equals(builtin) || "len".equals(builtin) || "range".equals(builtin)
                        || "open".equals(builtin) || "isinstance".equals(builtin)
                        ? builtin + "($0)"
                        : builtin;
                result.add(new CompletionItem(builtin, insert, "builtin"));
            }
        }

        result.sort((a, b) -> {
            boolean aExact = a.label.toLowerCase(Locale.US).startsWith(lower);
            boolean bExact = b.label.toLowerCase(Locale.US).startsWith(lower);
            if (aExact != bExact) return aExact ? -1 : 1;
            return a.label.compareToIgnoreCase(b.label);
        });
        if (result.size() > 8) return new ArrayList<>(result.subList(0, 8));
        return result;
    }

    private boolean isExpressionCompletionContext(CharSequence editable, int cursor, String prefix) {
        int prefixStart = Math.max(0, cursor - prefix.length());
        int lineStart = prefixStart;
        while (lineStart > 0 && editable.charAt(lineStart - 1) != '\n') lineStart--;
        String before = editable.subSequence(lineStart, prefixStart).toString();
        String trimmed = before.trim();
        if (trimmed.isEmpty()) return false;

        char last = trimmed.charAt(trimmed.length() - 1);
        if ("([,{=:+-*/%<>!".indexOf(last) >= 0) return true;
        if (trimmed.endsWith("return") || trimmed.endsWith("yield") || trimmed.endsWith("in")
                || trimmed.endsWith("and") || trimmed.endsWith("or") || trimmed.endsWith("not")) {
            return true;
        }
        int openParen = trimmed.lastIndexOf('(');
        int closeParen = trimmed.lastIndexOf(')');
        return openParen > closeParen;
    }

    private boolean hasLocalCompletion(List<CompletionItem> items) {
        for (CompletionItem item : items) {
            if ("local".equals(item.kind)) return true;
        }
        return false;
    }

    private List<String> collectPythonSymbols(String code) {
        LinkedHashSet<String> symbols = new LinkedHashSet<>();
        collectGroup(code, FUNCTION_PATTERN, 1, symbols);
        collectGroup(code, CLASS_PATTERN, 1, symbols);
        collectGroup(code, ASSIGNMENT_PATTERN, 1, symbols);
        Matcher imports = IMPORT_PATTERN.matcher(code);
        while (imports.find()) {
            String value = imports.group(1);
            int dot = value.lastIndexOf('.');
            symbols.add(dot >= 0 ? value.substring(dot + 1) : value);
        }
        Matcher params = FUNCTION_PARAMS_PATTERN.matcher(code);
        while (params.find()) {
            String[] names = params.group(1).split(",");
            for (String rawName : names) {
                String name = rawName.trim();
                int equals = name.indexOf('=');
                if (equals >= 0) name = name.substring(0, equals).trim();
                int colon = name.indexOf(':');
                if (colon >= 0) name = name.substring(0, colon).trim();
                if (name.startsWith("*")) name = name.replace("*", "").trim();
                if (name.matches("[A-Za-z_][A-Za-z0-9_]*")
                        && !isPythonKeyword(name)
                        && !isPythonBuiltin(name)) {
                    symbols.add(name);
                }
            }
        }
        return new ArrayList<>(symbols);
    }

    private boolean isPythonKeyword(String value) {
        for (String keyword : PYTHON_KEYWORDS) {
            if (keyword.equals(value)) return true;
        }
        return false;
    }

    private boolean isPythonBuiltin(String value) {
        for (String builtin : PYTHON_BUILTINS) {
            if (builtin.equals(value)) return true;
        }
        return false;
    }

    private void collectGroup(String code, Pattern pattern, int group, Set<String> out) {
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            String value = matcher.group(group);
            if (value != null && value.length() > 1) out.add(value);
        }
    }

    private boolean matchesCompletion(String value, String lowerPrefix) {
        String lower = value.toLowerCase(Locale.US);
        return lower.startsWith(lowerPrefix) || lower.contains(lowerPrefix);
    }

    private String completionPrefix(CharSequence editable, int cursor) {
        int start = cursor;
        while (start > 0) {
            char c = editable.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') start--;
            else break;
        }
        return editable.subSequence(start, cursor).toString();
    }

    private void applyCompletion(CompletionItem item) {
        if (editor == null || item == null) return;
        String editable = editorText();
        int cursor = Math.max(0, editorSelectionStart());
        String prefix = completionPrefix(editable, cursor);
        int start = Math.max(0, cursor - prefix.length());
        String insert = item.insertText;
        int marker = insert.indexOf("$0");
        if (marker >= 0) insert = insert.replace("$0", "");

        applyingCompletion = true;
        replaceEditorRange(start, cursor, insert);
        int nextCursor = marker >= 0 ? start + marker : start + insert.length();
        setEditorSelection(Math.max(0, Math.min(nextCursor, editorLength())));
        applyingCompletion = false;
        dismissCompletions();
    }

    private int completionX(int cursor) {
        int[] location = new int[2];
        editor.getLocationOnScreen(location);
        CharPosition position = editorPositionForOffset(cursor);
        int x = location[0] + Math.round(editor.getCharOffsetX(position.line, position.column)) - editor.getOffsetX();
        int maxX = getResources().getDisplayMetrics().widthPixels - dp(308);
        return Math.max(dp(8), Math.min(x, Math.max(dp(8), maxX)));
    }

    private int completionY(int cursor) {
        int[] location = new int[2];
        editor.getLocationOnScreen(location);
        CharPosition position = editorPositionForOffset(cursor);
        int y = location[1] + Math.round(editor.getCharOffsetY(position.line, position.column)) - editor.getOffsetY() + editor.getRowHeight() + dp(4);
        int maxY = getResources().getDisplayMetrics().heightPixels - dp(260);
        return Math.max(dp(56), Math.min(y, Math.max(dp(56), maxY)));
    }

    private void dismissCompletions() {
        activeCompletions.clear();
        updateGhostText("");
        if (completionPopup != null && completionPopup.isShowing()) completionPopup.dismiss();
    }

    private void updateGhostText(String ghost) {
        updateGhostText(ghost, false);
    }

    private void updateGhostText(String ghost, boolean ai) {
        if (!prefs.getBoolean("autocomplete_ghost", true)) {
            aiSuggestionText = "";
            aiSuggestionCursor = -1;
            clearSoraGhostText();
            return;
        }
        setSoraGhostText(ghost, ai);
    }

    private void setSoraGhostText(String ghost, boolean ai) {
        if (editor == null) return;
        if (ghost == null || ghost.isEmpty()) {
            clearSoraGhostText();
            return;
        }
        int cursor = editorSelectionStart();
        CharPosition position = editorPositionForOffset(cursor);
        String text = ghost.replace("\r\n", "\n").replace('\r', '\n');
        text = text.replace("\n", "  | ");
        if (text.length() > 160) text = text.substring(0, 160);
        if (ai) text = text + "  AI";

        InlayHintsContainer hints = new InlayHintsContainer();
        hints.add(new TextInlayHint(position.line, position.column, text));
        editor.setInlayHints(hints);
        editor.invalidate();
    }

    private void clearSoraGhostText() {
        if (editor == null) return;
        editor.setInlayHints(new InlayHintsContainer());
        editor.invalidate();
    }

    private String ghostTextFor(CompletionItem item, String prefix) {
        if (item == null || prefix == null || prefix.isEmpty()) return "";
        String insert = item.insertText.replace("$0", "");
        if (!insert.toLowerCase(Locale.US).startsWith(prefix.toLowerCase(Locale.US))) return "";
        String rest = insert.substring(Math.min(prefix.length(), insert.length()));
        int newline = rest.indexOf('\n');
        if (newline >= 0) rest = rest.substring(0, newline);
        if (rest.length() > 80) rest = rest.substring(0, 80);
        return rest;
    }

    private void scheduleAiCompletion() {
        int token = ++aiCompletionToken;
        clearAiSuggestion();
        cancelAiRetryCountdown();
        if (editor == null || !prefs.getBoolean("autocomplete_ghost", true)) return;
        AutocompleteProvider provider = currentProvider();
        if ("local".equals(provider.id)) return;
        if (provider.needsKey && providerApiKey(provider).isEmpty()) {
            Log.d(TAG_AI, "skip: missing api key for " + provider.id);
            return;
        }
        int cursor = editorSelectionStart();
        String code = editorText();
        if (cursor < 0 || cursor > code.length() || cursor != editorSelectionEnd()) return;
        String signature = provider.id + "\n" + providerModel(provider)
                + "\n" + cursor + "\n" + stableCompletionContext(code, cursor);
        if (signature.equals(aiCompletionSignature) || aiCompletionInFlight) return;
        if (aiCompletionRunnable != null) editor.removeCallbacks(aiCompletionRunnable);
        if (aiCompletionRetryAtMs > SystemClock.uptimeMillis()) {
            startAiRetryCountdown(token, signature, code, cursor, aiCompletionRetryAtMs);
            return;
        }
        aiCompletionRunnable = () -> requestAiCompletionIfStillCurrent(token, signature, code, cursor, false);
        editor.postDelayed(aiCompletionRunnable, AI_COMPLETION_IDLE_MS);
        Log.d(TAG_AI, "queued provider=" + provider.id + " model=" + providerModel(provider));
    }

    private void requestAiCompletionIfStillCurrent(int token, String signature, String code, int cursor, boolean retry) {
        if (editor == null || token != aiCompletionToken) return;
        String current = editorText();
        if (cursor != editorSelectionStart()
                || !signature.endsWith(stableCompletionContext(current, cursor))) {
            return;
        }
        aiCompletionInFlight = true;
        aiCompletionSignature = signature;
        Log.d(TAG_AI, "request start retry=" + retry);
        new Thread(() -> {
            String suggestion = "";
            String error = "";
            long retryAfterMs = 0L;
            try {
                suggestion = fetchAiCompletion(code, cursor);
                prefs.edit().remove("autocomplete_last_error").apply();
            } catch (Exception failure) {
                error = failure.getClass().getSimpleName() + ": " + failure.getMessage();
                retryAfterMs = retryDelayFor(failure, error);
                Log.d(TAG_AI, "request failed " + safeAiDiagnostic(error, 160));
            }
            final String finalSuggestion = normalizeAiSuggestion(suggestion);
            final String finalError = error;
            final long finalRetryAfterMs = retryAfterMs;
            runOnUiThread(() -> {
                aiCompletionInFlight = false;
                if (token != aiCompletionToken || editor == null || cursor != editorSelectionStart()) return;
                if (finalSuggestion.isEmpty()) {
                    boolean autoErrors = prefs.getBoolean("autocomplete_auto_errors", true);
                    if (!finalError.isEmpty()) {
                        prefs.edit().putString("autocomplete_last_error", shortAiError(finalError)).apply();
                    }
                    if (autoErrors && finalRetryAfterMs > 0L) {
                        long retryAt = SystemClock.uptimeMillis() + finalRetryAfterMs;
                        aiCompletionSignature = "";
                        startAiRetryCountdown(token, signature, code, cursor, retryAt);
                    } else if (autoErrors && !retry) {
                        aiCompletionSignature = "";
                        Log.d(TAG_AI, "empty response; retrying");
                        editor.postDelayed(() -> requestAiCompletionIfStillCurrent(token, signature, code, cursor, true), AI_COMPLETION_FALLBACK_RETRY_MS);
                    } else {
                        aiCompletionSignature = "";
                        Log.d(TAG_AI, "no suggestion after retry" + (finalError.isEmpty() ? "" : ": " + safeAiDiagnostic(finalError, 120)));
                        if (!finalError.isEmpty() && !autoErrors) {
                            Toast.makeText(MainActivity.this, "AI autocomplete failed: " + shortAiError(finalError), Toast.LENGTH_SHORT).show();
                        }
                    }
                    return;
                }
                Log.d(TAG_AI, "received lines=" + finalSuggestion.split("\\n", -1).length);
                activeCompletions.clear();
                if (completionPopup != null && completionPopup.isShowing()) completionPopup.dismiss();
                cancelAiRetryCountdown();
                aiSuggestionText = finalSuggestion;
                aiSuggestionCursor = cursor;
                updateGhostText(aiSuggestionText, true);
            });
        }, "andropy-ai-completion").start();
    }

    private void startAiRetryCountdown(int token, String signature, String code, int cursor, long retryAtMs) {
        if (editor == null || token != aiCompletionToken) return;
        aiCompletionRetryAtMs = retryAtMs;
        aiCompletionRetryToken = token;
        long remaining = Math.max(0L, retryAtMs - SystemClock.uptimeMillis());
        updateGhostText("retrying in " + Math.max(1L, (remaining + 999L) / 1000L) + "s", true);
        aiRetryCountdownRunnable = () -> {
            if (editor == null || token != aiCompletionToken || token != aiCompletionRetryToken) return;
            if (cursor != editorSelectionStart()
                    || !signature.endsWith(stableCompletionContext(editorText(), cursor))) {
                cancelAiRetryCountdown();
                return;
            }
            long left = aiCompletionRetryAtMs - SystemClock.uptimeMillis();
            if (left <= 0L) {
                aiCompletionRetryAtMs = 0L;
                updateGhostText("retrying", true);
                requestAiCompletionIfStillCurrent(token, signature, code, cursor, true);
                return;
            }
            updateGhostText("retrying in " + Math.max(1L, (left + 999L) / 1000L) + "s", true);
            editor.postDelayed(aiRetryCountdownRunnable, 1000L);
        };
        editor.postDelayed(aiRetryCountdownRunnable, Math.min(1000L, Math.max(50L, remaining)));
    }

    private void cancelAiRetryCountdown() {
        aiCompletionRetryAtMs = 0L;
        aiCompletionRetryToken = 0;
        if (editor != null && aiRetryCountdownRunnable != null) {
            editor.removeCallbacks(aiRetryCountdownRunnable);
        }
        aiRetryCountdownRunnable = null;
    }

    private long retryDelayFor(Exception failure, String errorText) {
        if (failure instanceof AiProviderException) {
            AiProviderException providerError = (AiProviderException) failure;
            if (providerError.retryAfterMs > 0L) return Math.min(providerError.retryAfterMs, AI_COMPLETION_MAX_RETRY_MS);
            if (providerError.statusCode == 429) return 10000L;
            if (providerError.statusCode >= 500) return 2500L;
        }
        String text = errorText == null ? "" : errorText.toLowerCase(Locale.US);
        if (text.contains("http 429") || text.contains("ratelimit") || text.contains("rate limit")
                || text.contains("rate_limit") || text.contains("ratelimited") || text.contains("too many requests")) {
            return 10000L;
        }
        if (text.contains("sockettimeoutexception") || text.contains("timeout") || text.contains("http 5")) {
            return 2500L;
        }
        return 0L;
    }

    private String shortAiError(String error) {
        if (error == null || error.isEmpty()) return "no suggestion";
        error = providerErrorMessage(error);
        if (error.contains("UnknownHostException")) return "DNS/network offline";
        if (error.contains("SocketTimeoutException")) return "timeout";
        if (error.contains("HTTP 401") || error.contains("HTTP 403")) return "check API key";
        if (error.contains("HTTP 429")) return "rate limited";
        return error.length() > 42 ? error.substring(0, 42) : error;
    }

    private String aiChatErrorText(Exception e) {
        String text = e == null ? "" : e.getClass().getSimpleName() + ": " + e.getMessage();
        text = providerErrorMessage(text);
        if (text.contains("UnknownHostException")) return "network is offline";
        if (text.contains("SocketTimeoutException")) return "the model timed out";
        if (text.contains("HTTP 401") || text.contains("HTTP 403")) return "check the API key for this provider";
        if (text.contains("HTTP 429")) return "rate limited by the provider";
        return compactLogText(text, 260);
    }

    private String fetchAiCompletion(String code, int cursor) throws Exception {
        AutocompleteProvider provider = currentProvider();
        String endpoint = providerEndpoint(provider);
        if (endpoint.isEmpty()) return "";

        if ("cohere".equals(provider.id)) {
            return fetchCohereCompletion(endpoint, provider, code, cursor);
        }
        if ("groq".equals(provider.id) || "openai".equals(provider.id)
                || "openrouter".equals(provider.id) || "mistral".equals(provider.id)) {
            return fetchOpenAiCompatibleCompletion(endpoint, provider, code, cursor);
        }
        return "";
    }

    private String fetchCohereCompletion(String endpoint, AutocompleteProvider provider, String code, int cursor) throws Exception {
        String apiKey = providerApiKey(provider);
        JSONObject body = new JSONObject();
        body.put("model", providerModel(provider));
        body.put("temperature", 0.15);
        body.put("max_tokens", 120);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are Aqua IDE inline autocomplete. Return only code that should be inserted at the cursor. No markdown. No explanation."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", aiCompletionPrompt(code, cursor)));
        body.put("messages", messages);

        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(AI_COMPLETION_TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int codeStatus = connection.getResponseCode();
        String response = readStreamText(codeStatus >= 200 && codeStatus < 300 ? connection.getInputStream() : connection.getErrorStream());
        long retryAfterMs = retryAfterMs(connection);
        connection.disconnect();
        if (codeStatus < 200 || codeStatus >= 300) {
            throw new AiProviderException(codeStatus, safeAiDiagnostic(response, 180), retryAfterMs);
        }
        JSONObject message = new JSONObject(response).optJSONObject("message");
        return cohereMessageText(message);
    }

    private String fetchOpenAiCompatibleCompletion(String endpoint, AutocompleteProvider provider, String code, int cursor) throws Exception {
        String apiKey = providerApiKey(provider);
        String model = providerModel(provider);

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", 0.15);
        body.put("max_tokens", 120);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are Aqua IDE inline autocomplete. Return only code that should be inserted at the cursor. No markdown. No explanation."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", aiCompletionPrompt(code, cursor)));
        body.put("messages", messages);

        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(AI_COMPLETION_TIMEOUT_MS);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        if (!apiKey.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(payload);
        }

        int codeStatus = connection.getResponseCode();
        InputStream stream = codeStatus >= 200 && codeStatus < 300 ? connection.getInputStream() : connection.getErrorStream();
        String response = readStreamText(stream);
        long retryAfterMs = retryAfterMs(connection);
        connection.disconnect();
        if (codeStatus < 200 || codeStatus >= 300) {
            throw new AiProviderException(codeStatus, safeAiDiagnostic(response, 180), retryAfterMs);
        }
        JSONObject json = new JSONObject(response);
        JSONArray choices = json.optJSONArray("choices");
        if (choices == null || choices.length() == 0) return "";
        JSONObject first = choices.optJSONObject(0);
        if (first == null) return "";
        JSONObject message = first.optJSONObject("message");
        return message == null ? first.optString("text", "") : message.optString("content", "");
    }

    private long retryAfterMs(HttpURLConnection connection) {
        if (connection == null) return 0L;
        String header = connection.getHeaderField("Retry-After");
        if (header == null || header.trim().isEmpty()) {
            header = connection.getHeaderField("X-RateLimit-Reset-After");
        }
        if (header == null || header.trim().isEmpty()) return 0L;
        String value = header.trim();
        try {
            double seconds = Double.parseDouble(value);
            return Math.max(0L, Math.round(seconds * 1000.0));
        } catch (NumberFormatException ignored) {
        }
        try {
            long retryAt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).parse(value).getTime();
            return Math.max(0L, retryAt - System.currentTimeMillis());
        } catch (Exception ignored) {
        }
        return 0L;
    }

    private String compactLogText(String value) {
        return compactLogText(value, 180);
    }

    private String compactLogText(String value, int limit) {
        if (value == null) return "";
        String compact = value.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ").trim();
        int max = Math.max(24, limit);
        return compact.length() > max ? compact.substring(0, max) : compact;
    }

    private String safeAiDiagnostic(Exception error) {
        if (error == null) return "";
        return safeAiDiagnostic(error.getClass().getSimpleName() + ": " + error.getMessage(), 180);
    }

    private String safeAiDiagnostic(String value, int limit) {
        if (value == null || value.trim().isEmpty()) return "";
        String text = providerErrorMessage(value);
        text = text.replaceAll("(?i)(bearer\\s+)[A-Za-z0-9._~+\\-/]+=*", "$1[redacted]");
        text = text.replaceAll("(?i)(api[_-]?key|x-api-key|key)\\s*[:=]\\s*['\\\"]?[^'\\\"\\s,}]+", "$1=[redacted]");
        text = text.replaceAll("(?i)(authorization\\s*[:=]\\s*)['\\\"]?[^'\\\"\\s,}]+", "$1[redacted]");
        text = text.replaceAll("\\borg_[A-Za-z0-9_]+\\b", "org_[redacted]");
        text = text.replaceAll("\\bgithub_pat_[A-Za-z0-9_]+\\b", "github_pat_[redacted]");
        text = text.replaceAll("\\b(sk|gsk|xai|AIza)[A-Za-z0-9_\\-]{18,}\\b", "$1[redacted]");
        text = text.replaceAll("(?is)(\"(?:content|prompt|message|text|input)\"\\s*:\\s*)\"(?:\\\\.|[^\"\\\\])*\"", "$1\"[redacted]\"");
        text = text.replaceAll("(?is)('(?:content|prompt|message|text|input)'\\s*:\\s*)'(?:\\\\.|[^'\\\\])*'", "$1'[redacted]'");
        return compactLogText(text, limit);
    }

    private String providerErrorMessage(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";
        String text = raw.trim();
        int jsonStart = text.indexOf('{');
        if (jsonStart >= 0) {
            try {
                JSONObject json = new JSONObject(text.substring(jsonStart));
                JSONObject error = json.optJSONObject("error");
                if (error != null) {
                    String message = error.optString("message", "");
                    String type = error.optString("type", "");
                    String code = error.optString("code", "");
                    StringBuilder out = new StringBuilder();
                    if (text.startsWith("HTTP ")) {
                        int space = text.indexOf(' ', 5);
                        out.append(space > 0 ? text.substring(0, space) : text.substring(0, Math.min(text.length(), 8))).append(": ");
                    }
                    if (!message.isEmpty()) out.append(message);
                    if (!type.isEmpty()) out.append(" (").append(type).append(')');
                    if (!code.isEmpty()) out.append(" [").append(code).append(']');
                    if (out.length() > 0) return out.toString();
                }
                String message = json.optString("message", "");
                if (!message.isEmpty()) return message;
            } catch (Exception ignored) {
            }
        }
        return text;
    }

    private String aiCompletionPrompt(String code, int cursor) {
        int prefixStart = Math.max(0, cursor - 2400);
        int suffixEnd = Math.min(code.length(), cursor + 900);
        String prefix = code.substring(prefixStart, cursor);
        String suffix = code.substring(cursor, suffixEnd);
        return "File: " + currentFileName() + "\n"
                + "Complete Python at <cursor>. Return only the inserted code.\n"
                + "<prefix>\n" + prefix + "\n</prefix>\n"
                + "<suffix>\n" + suffix + "\n</suffix>";
    }

    private String normalizeAiSuggestion(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replace("\r\n", "\n").replace("\r", "\n").trim();
        String lowerCleaned = cleaned.toLowerCase(Locale.US);
        if (lowerCleaned.startsWith("<think>")) {
            int close = lowerCleaned.indexOf("</think>");
            if (close < 0) return "";
            cleaned = cleaned.substring(close + "</think>".length()).trim();
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("(?s)^```[A-Za-z0-9_-]*\\n?", "");
            cleaned = cleaned.replaceFirst("(?s)\\n?```$", "");
        }
        String[] lines = cleaned.split("\n");
        StringBuilder out = new StringBuilder();
        int used = 0;
        for (String line : lines) {
            if (used >= AI_COMPLETION_MAX_LINES) break;
            if (line.trim().isEmpty() && used == 0) continue;
            if (out.length() > 0) out.append('\n');
            out.append(line);
            used++;
        }
        return out.toString().trim();
    }

    private String stableCompletionContext(String code, int cursor) {
        int start = Math.max(0, cursor - 420);
        int end = Math.min(code.length(), cursor + 120);
        return code.substring(start, end);
    }

    private String readStreamText(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            return body.toString();
        }
    }

    private boolean hasAiSuggestion() {
        return editor != null && aiSuggestionCursor == editorSelectionStart() && !aiSuggestionText.isEmpty();
    }

    private void clearAiSuggestion() {
        if (aiSuggestionText.isEmpty() && aiSuggestionCursor < 0) return;
        aiSuggestionText = "";
        aiSuggestionCursor = -1;
        clearSoraGhostText();
    }

    private int digitForKeyCode(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) return keyCode - KeyEvent.KEYCODE_0;
        if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) return keyCode - KeyEvent.KEYCODE_NUMPAD_0;
        return -1;
    }

    private void acceptAiSuggestion(int maxLines) {
        if (!hasAiSuggestion()) return;
        String insert = firstAiSuggestionLines(aiSuggestionText, maxLines);
        if (insert.isEmpty()) return;
        applyingCompletion = true;
        insertEditorText(aiSuggestionCursor, insert);
        setEditorSelection(aiSuggestionCursor + insert.length());
        applyingCompletion = false;
        clearAiSuggestion();
        updateGhostText("");
    }

    private String firstAiSuggestionLines(String suggestion, int maxLines) {
        String[] lines = suggestion.split("\n", -1);
        StringBuilder out = new StringBuilder();
        int limit = Math.max(1, Math.min(maxLines, lines.length));
        for (int i = 0; i < limit; i++) {
            if (i > 0) out.append('\n');
            out.append(lines[i]);
        }
        return out.toString();
    }

    private void applyPythonTypingHelp(Editable editable) {
        // Replaced by Sora's editor engine. Bracket-pair behavior will be wired
        // through Sora symbol pairs instead of mutating Editable directly.
    }

    private char matchingCloser(char inserted) {
        if (inserted == '(') return ')';
        if (inserted == '[') return ']';
        if (inserted == '{') return '}';
        if (inserted == '"') return '"';
        if (inserted == '\'') return '\'';
        return 0;
    }

    private boolean isCloser(char inserted) {
        return inserted == ')' || inserted == ']' || inserted == '}' || inserted == '"' || inserted == '\'';
    }

    private boolean isPair(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }

    private String lineIndentBefore(CharSequence editable, int offset) {
        int lineStart = Math.max(0, offset - 1);
        while (lineStart > 0 && editable.charAt(lineStart - 1) != '\n') lineStart--;

        StringBuilder indent = new StringBuilder();
        int index = lineStart;
        while (index < offset) {
            char c = editable.charAt(index);
            if (c == ' ' || c == '\t') {
                indent.append(c);
                index++;
            } else {
                break;
            }
        }
        return indent.toString();
    }

    private String nextPythonIndent(CharSequence editable, int newlineAt) {
        int previousLineStart = Math.max(0, newlineAt - 1);
        while (previousLineStart > 0 && editable.charAt(previousLineStart - 1) != '\n') previousLineStart--;

        StringBuilder indent = new StringBuilder(lineIndentBefore(editable, newlineAt));

        int lastContent = newlineAt - 1;
        while (lastContent >= previousLineStart && Character.isWhitespace(editable.charAt(lastContent))) lastContent--;
        if (lastContent >= previousLineStart && editable.charAt(lastContent) == ':') {
            indent.append("    ");
        }
        return indent.toString();
    }

    private void colorMatches(Editable editable, Pattern pattern, int color, int group) {
        Matcher matcher = pattern.matcher(editable);
        while (matcher.find()) {
            int start = matcher.start(group);
            int end = matcher.end(group);
            if (start >= 0 && end >= start) {
                editable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private void updatePanelStatus() {
        if (panelFileName == null || panelLine == null || panelOffset == null || editor == null) return;

        String text = editorText();
        int length = text.length();
        int cursor = Math.max(0, Math.min(editorSelectionStart(), length));
        int lineNumber = 1;
        int lineStart = 0;
        int totalLines = 1;

        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == '\n') {
                totalLines++;
                if (i < cursor) {
                    lineNumber++;
                    lineStart = i + 1;
                }
            }
        }

        panelFileName.setText(currentFileName().replace(".py", ""));
        panelLine.setText("Line: " + lineNumber + "/" + totalLines);
        panelOffset.setText("Line offset: " + Math.max(0, cursor - lineStart));
    }

    private String currentFileName() {
        String path = prefs.getString("file_path", "");
        if (path != null && !path.trim().isEmpty()) {
            String nameFromPath = new File(path).getName();
            if (!nameFromPath.trim().isEmpty()) return nameFromPath;
        }
        String name = prefs.getString("file_name", DEFAULT_FILE);
        if (name == null || name.trim().isEmpty()) return DEFAULT_FILE;
        return name.trim();
    }

    private String defaultPython() {
        return "from pathlib import Path\n\n"
                + "def main():\n"
                + "    app_name = \"AndroPy\"\n"
                + "    file_path = Path(\"new.py\")\n"
                + "    print(f\"Running {app_name}: {file_path.name}\")\n\n"
                + "if __name__ == \"__main__\":\n"
                + "    main()\n";
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focused = getCurrentFocus();
        if (imm != null && focused != null) imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
    }

    private boolean editorFixedWrap() {
        return prefs.getBoolean("editor_fixed_wrap", false);
    }

    private void setEditorFixedWrap(boolean fixed) {
        prefs.edit().putBoolean("editor_fixed_wrap", fixed).apply();
        setContentView(buildEditorSettingsScreen());
    }

    private float terminalTextSp() {
        return clampTerminalTextSp(prefs.getFloat("terminal_text_sp", TERMINAL_TEXT_SP_DEFAULT));
    }

    private float clampTerminalTextSp(float value) {
        return Math.max(TERMINAL_TEXT_SP_MIN, Math.min(TERMINAL_TEXT_SP_MAX, value));
    }

    private void setTerminalTextSp(float value) {
        float next = clampTerminalTextSp(value);
        prefs.edit().putFloat("terminal_text_sp", next).apply();
        if (termuxTerminalView != null) {
            termuxTerminalView.setTextSize(Math.round(sp(next)));
            termuxTerminalView.updateSize();
            termuxTerminalView.invalidate();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private GradientDrawable roundRect(int color, int radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radiusPx);
        return drawable;
    }

    private int panelWidth() {
        return dp(PANEL_WIDTH_DP);
    }

    private int projectPanelWidth() {
        return Math.min(dp(318), getResources().getDisplayMetrics().widthPixels - dp(42));
    }

    private int aiChatPanelWidth() {
        return Math.min(dp(336), getResources().getDisplayMetrics().widthPixels - dp(42));
    }

    private static final class CompletionItem {
        final String label;
        final String insertText;
        final String kind;

        CompletionItem(String label, String insertText, String kind) {
            this.label = label;
            this.insertText = insertText;
            this.kind = kind;
        }
    }

    private static final class AutocompleteProvider {
        final String id;
        final String name;
        final String detail;
        final String defaultModel;
        final String defaultEndpoint;
        final boolean needsKey;
        final boolean autoModel;
        final boolean canFetchModels;

        AutocompleteProvider(String id, String name, String detail, String defaultModel, String defaultEndpoint,
                             boolean needsKey, boolean autoModel, boolean canFetchModels) {
            this.id = id;
            this.name = name;
            this.detail = detail;
            this.defaultModel = defaultModel;
            this.defaultEndpoint = defaultEndpoint;
            this.needsKey = needsKey;
            this.autoModel = autoModel;
            this.canFetchModels = canFetchModels;
        }
    }

    private static final class PipPackageInfo {
        final String name;
        final String version;
        final long sizeBytes;
        String summary;

        PipPackageInfo(String name, String version, long sizeBytes, String summary) {
            this.name = name;
            this.version = version == null ? "" : version;
            this.sizeBytes = sizeBytes;
            this.summary = summary == null ? "" : summary;
        }
    }

    private final class BootstrapOrbView extends View {
        private final Paint spherePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF ringBounds = new RectF();
        private final float hueOffset = new Random().nextFloat() * 360f;

        BootstrapOrbView(Activity activity) {
            super(activity);
            ringPaint.setStyle(Paint.Style.STROKE);
            ringPaint.setStrokeCap(Paint.Cap.ROUND);
            ringPaint.setStrokeWidth(dp(3));
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setStrokeCap(Paint.Cap.ROUND);
            glowPaint.setStrokeWidth(dp(9));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            long now = SystemClock.uptimeMillis();
            float width = getWidth();
            float height = getHeight();
            float centerX = width / 2f;
            float centerY = height / 2f;
            float radius = Math.min(width, height) * 0.28f;
            float ringRadius = Math.min(width, height) * 0.42f;
            int hueColor = Color.HSVToColor(new float[]{(hueOffset + now / 42f) % 360f, 0.22f, 1f});
            int softHue = Color.argb(74, Color.red(hueColor), Color.green(hueColor), Color.blue(hueColor));
            int ringColor = bootstrapDownloading ? Color.WHITE : hueColor;
            int glowColor = bootstrapDownloading ? Color.argb(88, 255, 255, 255) : softHue;
            ringPaint.setStrokeWidth(bootstrapDownloading ? dp(9) : dp(3));
            glowPaint.setStrokeWidth(bootstrapDownloading ? dp(17) : dp(9));

            spherePaint.setStyle(Paint.Style.FILL);
            spherePaint.setColor(Color.rgb(34, 39, 49));
            canvas.drawCircle(centerX, centerY, radius, spherePaint);
            spherePaint.setStyle(Paint.Style.STROKE);
            spherePaint.setStrokeWidth(bootstrapDownloading ? dp(2) : dp(1));
            spherePaint.setColor(bootstrapDownloading ? Color.WHITE : Color.argb(130, Color.red(hueColor), Color.green(hueColor), Color.blue(hueColor)));
            canvas.drawCircle(centerX, centerY, radius + dp(1), spherePaint);
            spherePaint.setColor(bootstrapDownloading ? Color.WHITE : Color.argb(70, 255, 255, 255));
            spherePaint.setStrokeWidth(dp(2));
            canvas.drawCircle(centerX - radius * 0.22f, centerY - radius * 0.26f, radius * 0.46f, spherePaint);

            ringBounds.set(centerX - ringRadius, centerY - ringRadius, centerX + ringRadius, centerY + ringRadius);
            float start = (now / 8f) % 360f;
            glowPaint.setColor(glowColor);
            ringPaint.setColor(ringColor);
            canvas.drawArc(ringBounds, start, 270f, false, glowPaint);
            canvas.drawArc(ringBounds, start, 270f, false, ringPaint);
            postInvalidateOnAnimation();
        }
    }

    private final class NumberedEditor extends EditText {
        private final Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint activeLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ghostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint aiMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private String ghostText = "";
        private boolean aiGhost;
        private boolean snappingScroll;

        NumberedEditor(Activity activity) {
            super(activity);
            numberPaint.setColor(GUTTER_TEXT);
            numberPaint.setTextAlign(Paint.Align.RIGHT);
            numberPaint.setTypeface(Typeface.MONOSPACE);
            numberPaint.setTextSize(dp(15));
            dividerPaint.setColor(GUTTER_LINE);
            dividerPaint.setStrokeWidth(Math.max(1f, getResources().getDisplayMetrics().density));
            activeLinePaint.setColor(ACTIVE_LINE);
            ghostPaint.setColor(GHOST_TEXT);
            ghostPaint.setTypeface(Typeface.MONOSPACE);
            ghostPaint.setTextSize(dp(15));
            aiMarkPaint.setColor(Color.argb(150, 170, 232, 255));
            aiMarkPaint.setTypeface(Typeface.DEFAULT_BOLD);
            aiMarkPaint.setTextSize(dp(14));
            cursorPaint.setColor(Color.rgb(139, 224, 218));
            cursorPaint.setStrokeWidth(Math.max(dp(2), getResources().getDisplayMetrics().density * 2f));
        }

        void setGhostText(String value, boolean ai) {
            String next = value == null ? "" : value;
            if (next.equals(ghostText) && ai == aiGhost) return;
            ghostText = next;
            aiGhost = ai;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            syncEditorPaintSizes();
            int viewportLeft = getScrollX();
            int viewportRight = viewportLeft + getWidth();
            int codeLeft = viewportLeft + dp(46);
            int gutterX = viewportLeft + dp(34);
            int dividerX = viewportLeft + dp(45);
            Layout layout = getLayout();
            int visualLineCount = layout == null ? Math.max(1, getLineCount()) : layout.getLineCount();
            int activeLine = layout == null ? 0 : layout.getLineForOffset(Math.max(0, getSelectionStart()));
            int firstHighlightLine = -1;
            int lastHighlightLine = -1;
            if (activeLine >= 0 && activeLine < visualLineCount) {
                firstHighlightLine = activeLine;
                lastHighlightLine = activeLine;
                if (editorFixedWrap() && layout != null) {
                    firstHighlightLine = logicalVisualStart(layout, activeLine);
                    lastHighlightLine = logicalVisualEnd(layout, activeLine, visualLineCount);
                }
                for (int i = firstHighlightLine; i <= lastHighlightLine; i++) {
                    float top = lineTop(layout, i);
                    float bottom = lineBottom(layout, i);
                    if (bottom > 0 && top < getHeight()) {
                        canvas.drawRect(viewportLeft, Math.max(0, top), viewportRight, Math.min(getHeight(), bottom), activeLinePaint);
                    }
                }
            }

            int save = canvas.save();
            canvas.clipRect(codeLeft, 0, viewportRight, getHeight());
            drawEditorLayout(canvas, layout);
            drawGhostText(canvas);
            drawEditorCursor(canvas, layout);
            canvas.restoreToCount(save);

            Paint gutterPaint = new Paint();
            gutterPaint.setColor(EDITOR_BG);
            canvas.drawRect(viewportLeft, 0, codeLeft, getHeight(), gutterPaint);
            if (firstHighlightLine >= 0 && lastHighlightLine >= firstHighlightLine) {
                for (int i = firstHighlightLine; i <= lastHighlightLine; i++) {
                    float top = lineTop(layout, i);
                    float bottom = lineBottom(layout, i);
                    if (bottom > 0 && top < getHeight()) {
                        canvas.drawRect(viewportLeft, Math.max(0, top), codeLeft, Math.min(getHeight(), bottom), activeLinePaint);
                    }
                }
            }
            canvas.drawLine(dividerX, 0, dividerX, getHeight(), dividerPaint);
            if (layout != null) {
                CharSequence text = getText();
                for (int i = 0; i < visualLineCount; i++) {
                    float top = lineTop(layout, i);
                    float bottom = lineBottom(layout, i);
                    if (bottom < 0 || top > getHeight()) continue;
                    int start = layout.getLineStart(i);
                    boolean logicalStart = !editorFixedWrap()
                            || start == 0
                            || (start <= text.length() && text.charAt(start - 1) == '\n');
                    if (logicalStart) {
                        float baseline = lineBaseline(layout, i);
                        int logicalLine = editorFixedWrap() ? logicalLineForOffset(text, start) : i + 1;
                        canvas.drawText(String.valueOf(logicalLine), gutterX, baseline, numberPaint);
                    }
                }
            } else {
                int lineCount = Math.max(1, getLineCount());
                for (int i = 0; i < lineCount; i++) {
                    int baseline = getLineBounds(i, null);
                    canvas.drawText(String.valueOf(i + 1), gutterX, baseline, numberPaint);
                }
            }
        }

        @Override
        public void scrollTo(int x, int y) {
            if (!snappingScroll) {
                y = snappedVerticalScrollY(y);
            }
            snappingScroll = true;
            try {
                super.scrollTo(x, y);
            } finally {
                snappingScroll = false;
            }
        }

        private int snappedVerticalScrollY(int requestedY) {
            if (requestedY <= 0) return 0;
            Layout layout = getLayout();
            if (layout == null) {
                int lineHeight = Math.max(1, getLineHeight());
                return Math.max(0, Math.round(requestedY / (float) lineHeight) * lineHeight);
            }
            int contentY = Math.max(0, requestedY);
            int line = Math.max(0, Math.min(layout.getLineCount() - 1, layout.getLineForVertical(contentY)));
            int currentTop = layout.getLineTop(line);
            int nextTop = line + 1 < layout.getLineCount()
                    ? layout.getLineTop(line + 1)
                    : currentTop;
            int snapped = Math.abs(requestedY - nextTop) < Math.abs(requestedY - currentTop) ? nextTop : currentTop;
            return Math.max(0, snapped);
        }

        private int logicalLineForOffset(CharSequence text, int offset) {
            int limit = Math.max(0, Math.min(offset, text == null ? 0 : text.length()));
            int line = 1;
            for (int i = 0; i < limit; i++) {
                if (text.charAt(i) == '\n') line++;
            }
            return line;
        }

        private int logicalVisualStart(Layout layout, int visualLine) {
            CharSequence text = getText();
            int line = visualLine;
            while (line > 0) {
                int start = layout.getLineStart(line);
                if (start <= 0 || start > text.length() || text.charAt(start - 1) == '\n') break;
                line--;
            }
            return line;
        }

        private int logicalVisualEnd(Layout layout, int visualLine, int visualLineCount) {
            CharSequence text = getText();
            int line = visualLine;
            while (line + 1 < visualLineCount) {
                int nextStart = layout.getLineStart(line + 1);
                if (nextStart <= 0 || nextStart > text.length() || text.charAt(nextStart - 1) == '\n') break;
                line++;
            }
            return line;
        }

        private void syncEditorPaintSizes() {
            float textSize = Math.max(dp(10), getTextSize());
            numberPaint.setTextSize(textSize * 0.94f);
            ghostPaint.setTextSize(textSize);
            aiMarkPaint.setTextSize(textSize * 0.92f);
        }

        private void drawEditorLayout(Canvas canvas, Layout layout) {
            if (layout == null) return;
            int save = canvas.save();
            getPaint().setColor(TEXT);
            canvas.translate(getTotalPaddingLeft() - getScrollX(), getTotalPaddingTop() - getScrollY());
            layout.draw(canvas);
            canvas.restoreToCount(save);
        }

        private void drawEditorCursor(Canvas canvas, Layout layout) {
            if (!isFocused() || layout == null || getSelectionStart() != getSelectionEnd()) return;
            int cursor = getSelectionStart();
            if (cursor < 0 || cursor > length()) return;
            int line = layout.getLineForOffset(cursor);
            float x = getTotalPaddingLeft() + layout.getPrimaryHorizontal(cursor) - getScrollX();
            float top = lineTop(layout, line) + dp(2);
            float bottom = lineBottom(layout, line) - dp(2);
            if (bottom <= 0 || top >= getHeight()) return;
            canvas.drawLine(x, Math.max(0, top), x, Math.min(getHeight(), bottom), cursorPaint);
        }

        private float lineTop(Layout layout, int visualLine) {
            if (layout == null) return visualLine * getLineHeight() - getScrollY();
            return getTotalPaddingTop() + layout.getLineTop(visualLine) - getScrollY();
        }

        private float lineBottom(Layout layout, int visualLine) {
            if (layout == null) return lineTop(null, visualLine) + getLineHeight();
            return getTotalPaddingTop() + layout.getLineBottom(visualLine) - getScrollY();
        }

        private float lineBaseline(Layout layout, int visualLine) {
            if (layout == null) return lineBottom(null, visualLine) - getPaint().getFontMetrics().descent;
            return getTotalPaddingTop() + layout.getLineBaseline(visualLine) - getScrollY();
        }

        private void drawGhostText(Canvas canvas) {
            if (ghostText.isEmpty()) return;
            Layout layout = getLayout();
            int cursor = getSelectionStart();
            if (layout == null || cursor < 0 || cursor > length() || getSelectionStart() != getSelectionEnd()) return;
            int line = layout.getLineForOffset(cursor);
            float startX = getTotalPaddingLeft() + layout.getPrimaryHorizontal(cursor) - getScrollX();
            float startY = lineBaseline(layout, line);
            float contentLeft = getTotalPaddingLeft();
            float contentRight = getScrollX() + getWidth() - getTotalPaddingRight() - dp(8);
            float lineHeight = Math.max(1f, getLineHeight());
            float bottomLimit = getHeight() - getTotalPaddingBottom();
            ghostPaint.setColor(aiGhost ? AI_GHOST_TEXT : GHOST_TEXT);
            String[] lines = ghostText.split("\n", -1);
            float lastWidth = 0f;
            float y = startY;
            float x = startX;
            int drawnLines = 0;
            int maxDrawnLines = aiGhost ? AI_COMPLETION_MAX_LINES : 1;
            for (int i = 0; i < lines.length; i++) {
                x = i == 0 ? startX : contentLeft;
                if (x > contentRight - dp(24)) {
                    x = contentLeft;
                    y += lineHeight;
                } else if (i > 0) {
                    y += lineHeight;
                }
                String remaining = lines[i];
                if (remaining.isEmpty()) remaining = " ";
                while (!remaining.isEmpty() && drawnLines < maxDrawnLines && y <= bottomLimit) {
                    float available = Math.max(dp(32), contentRight - x);
                    int count = ghostPaint.breakText(remaining, true, available, null);
                    if (count <= 0) break;
                    String part = remaining.substring(0, count);
                    canvas.drawText(part, x, y, ghostPaint);
                    lastWidth = ghostPaint.measureText(part);
                    remaining = remaining.substring(count);
                    drawnLines++;
                    if (!remaining.isEmpty() && drawnLines < maxDrawnLines) {
                        x = contentLeft;
                        y += lineHeight;
                    }
                }
                if (drawnLines >= maxDrawnLines || y > bottomLimit) break;
            }
            if (aiGhost) {
                float markX = Math.min(contentRight - dp(10), x + lastWidth + dp(8));
                if (y <= bottomLimit) canvas.drawText("✦", markX, y, aiMarkPaint);
            }
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
            updatePanelStatus();
        }
    }

    private final class AiSendButton extends View {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint spinnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF oval = new RectF();
        private boolean replying;
        private long started;

        AiSendButton(Context context) {
            super(context);
            fillPaint.setStyle(Paint.Style.FILL);
            arrowPaint.setStyle(Paint.Style.STROKE);
            arrowPaint.setStrokeCap(Paint.Cap.ROUND);
            arrowPaint.setStrokeJoin(Paint.Join.ROUND);
            spinnerPaint.setStyle(Paint.Style.STROKE);
            spinnerPaint.setStrokeCap(Paint.Cap.ROUND);
            setWillNotDraw(false);
        }

        void setReplying(boolean replying) {
            this.replying = replying;
            setEnabled(!replying);
            if (replying) started = SystemClock.uptimeMillis();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            float cx = width * 0.5f;
            float cy = height * 0.5f;
            float radius = Math.min(width, height) * 0.5f;

            fillPaint.setColor(replying ? Color.rgb(178, 184, 196) : Color.rgb(214, 219, 230));
            canvas.drawCircle(cx, cy, radius, fillPaint);

            if (replying) {
                float inset = dp(5);
                oval.set(inset, inset, width - inset, height - inset);
                spinnerPaint.setStrokeWidth(dp(2));
                spinnerPaint.setColor(Color.rgb(232, 235, 242));
                float start = ((SystemClock.uptimeMillis() - started) % 900L) / 900f * 360f;
                canvas.drawArc(oval, start, 270f, false, spinnerPaint);
                postInvalidateDelayed(16);
                return;
            }

            arrowPaint.setColor(Color.rgb(34, 36, 42));
            arrowPaint.setStrokeWidth(dp(3));
            float stemTop = height * 0.31f;
            float stemBottom = height * 0.72f;
            canvas.drawLine(cx, stemBottom, cx, stemTop, arrowPaint);
            canvas.drawLine(cx, stemTop, width * 0.35f, height * 0.46f, arrowPaint);
            canvas.drawLine(cx, stemTop, width * 0.65f, height * 0.46f, arrowPaint);
        }
    }

    private final class AiThinkingView extends View {
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private long started = SystemClock.uptimeMillis();

        AiThinkingView(Context context) {
            super(context);
            textPaint.setTextSize(dp(15));
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            setPadding(dp(4), dp(8), dp(8), dp(8));
            setLayoutParams(new LinearLayout.LayoutParams(-1, dp(46)));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            String text = "thinking";
            float x = dp(4);
            float baseline = dp(30);
            float textWidth = textPaint.measureText(text);
            float phase = ((SystemClock.uptimeMillis() - started) % 1300L) / 1300f;
            float shineX = x - textWidth + phase * textWidth * 2.4f;
            LinearGradient gradient = new LinearGradient(
                    shineX, 0, shineX + textWidth * 0.75f, 0,
                    new int[]{Color.rgb(145, 150, 160), Color.rgb(235, 238, 246), Color.rgb(145, 150, 160)},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP);
            textPaint.setShader(gradient);
            canvas.drawText(text, x, baseline, textPaint);
            textPaint.setShader(null);
            postInvalidateDelayed(16);
        }
    }

    private final class AiShineTextView extends TextView {
        private final LinearGradient[] gradient = new LinearGradient[1];
        private long started = SystemClock.uptimeMillis();

        AiShineTextView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = getPaint();
            float textWidth = Math.max(1f, paint.measureText(getText() == null ? "" : getText().toString()));
            float phase = ((SystemClock.uptimeMillis() - started) % 1300L) / 1300f;
            float shineX = -textWidth + phase * textWidth * 2.4f;
            gradient[0] = new LinearGradient(
                    shineX, 0, shineX + textWidth * 0.75f, 0,
                    new int[]{Color.rgb(150, 154, 166), Color.rgb(238, 240, 248), Color.rgb(150, 154, 166)},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP);
            paint.setShader(gradient[0]);
            super.onDraw(canvas);
            paint.setShader(null);
            postInvalidateDelayed(16);
        }
    }

    private static final class AiFileChange {
        final String before;
        final String after;
        final boolean existedBefore;
        AiDiffMarkers markers;

        AiFileChange(String before, String after, boolean existedBefore) {
            this.before = before;
            this.after = after;
            this.existedBefore = existedBefore;
        }
    }

    private static final class AiChangeSummary {
        int total;
        int clean;
        int conflicts;
    }

    private static final class AiAgentRunState {
        final HashMap<String, String> readSnapshots = new HashMap<>();
        final HashMap<String, String> originalSnapshots = new HashMap<>();
        final HashMap<String, Boolean> originalExists = new HashMap<>();
        final HashSet<String> listedDirectories = new HashSet<>();
        boolean sawIdeContext;
        boolean localProjectBuildIntent;
        boolean webNeeded;
        int editsApplied;
        int terminalRuns;
    }

    private static final class AiFlowStep {
        String title;
        final String kind;
        final ArrayList<String> details = new ArrayList<>();
        AiEditPreview editPreview;
        boolean active;
        boolean finished;

        AiFlowStep(String title, boolean active, String kind) {
            this.title = title == null || title.trim().isEmpty() ? "Evaluating..." : title.trim();
            this.kind = kind == null ? "" : kind;
            this.active = active;
        }
    }

    private static final class AiEditPreview {
        final String fileName;
        final ArrayList<AiEditPreviewRow> rows = new ArrayList<>();

        AiEditPreview(String fileName) {
            this.fileName = fileName == null || fileName.trim().isEmpty() ? "edited file" : fileName.trim();
        }
    }

    private static final class AiEditPreviewRow {
        final int lineNumber;
        final String text;
        final int kind;

        AiEditPreviewRow(int lineNumber, String text, int kind) {
            this.lineNumber = lineNumber;
            this.text = text == null ? "" : text;
            this.kind = kind;
        }
    }

    private static final class AiHttpResponse {
        final int status;
        final String body;

        AiHttpResponse(int status, String body) {
            this.status = status;
            this.body = body == null ? "" : body;
        }

        boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    private static final class AiDiffMarkers {
        final ArrayList<Integer> addedLines = new ArrayList<>();
        final ArrayList<Integer> deletedAnchors = new ArrayList<>();
    }

    private final class DiffMarkerDrawable extends Drawable {
        private final String label;
        private final int color;
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

        DiffMarkerDrawable(String label, int color) {
            this.label = label;
            this.color = color;
            fill.setColor(color);
            text.setColor(color);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            setBounds(0, 0, dp(22), dp(18));
        }

        @Override
        public void draw(Canvas canvas) {
            RectF bounds = new RectF(getBounds());
            float barWidth = Math.max(2f, dp(3));
            RectF bar = new RectF(
                    bounds.left + dp(2),
                    bounds.top + dp(1),
                    bounds.left + dp(2) + barWidth,
                    bounds.bottom - dp(1));
            canvas.drawRoundRect(bar, barWidth, barWidth, fill);

            float size = Math.min(bounds.width(), bounds.height());
            float cx = bounds.left + bounds.width() * 0.66f;
            float cy = bounds.top + bounds.height() * 0.5f;
            text.setTextSize(size * 0.78f);
            Paint.FontMetrics metrics = text.getFontMetrics();
            float baseline = cy - (metrics.ascent + metrics.descent) * 0.5f;
            canvas.drawText(label, cx, baseline, text);
        }

        @Override public void setAlpha(int alpha) {
            fill.setAlpha(alpha);
            text.setAlpha(alpha);
        }

        @Override public void setColorFilter(android.graphics.ColorFilter colorFilter) {
            fill.setColorFilter(colorFilter);
            text.setColorFilter(colorFilter);
        }

        @Override public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override public int getIntrinsicWidth() {
            return dp(22);
        }

        @Override public int getIntrinsicHeight() {
            return dp(18);
        }
    }

    private final class AndroPyTerminalSessionClient implements TerminalSessionClient {
        @Override
        public void onTextChanged(TerminalSession changedSession) {
            if (termuxTerminalView != null) termuxTerminalView.onScreenUpdated();
        }

        @Override
        public void onTitleChanged(TerminalSession updatedSession) {
        }

        @Override
        public void onSessionFinished(TerminalSession finishedSession) {
            if (terminalReturnToEditorOnExit) {
                terminalReturnToEditorOnExit = false;
                runOnUiThread(() -> {
                    terminalVisible = false;
                    termuxSession = null;
                    setContentView(buildEditorScreen());
                    editor.clearFocus();
                    editor.postDelayed(MainActivity.this::hideKeyboard, 80);
                });
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Terminal exited", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onCopyTextToClipboard(TerminalSession session, String text) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("terminal", text));
        }

        @Override
        public void onPasteTextFromClipboard(TerminalSession session) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()
                    && clipboard.getPrimaryClip() != null
                    && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this);
                if (text != null) session.getEmulator().paste(text.toString());
            }
        }

        @Override
        public void onBell(TerminalSession session) {
        }

        @Override
        public void onColorsChanged(TerminalSession session) {
            if (termuxTerminalView != null) termuxTerminalView.onScreenUpdated();
        }

        @Override
        public void onTerminalCursorStateChange(boolean state) {
            if (termuxTerminalView != null) termuxTerminalView.setTerminalCursorBlinkerState(state, true);
        }

        @Override
        public Integer getTerminalCursorStyle() {
            return TerminalEmulator.TERMINAL_CURSOR_STYLE_BLOCK;
        }

        @Override public void logError(String tag, String message) { }
        @Override public void logWarn(String tag, String message) { }
        @Override public void logInfo(String tag, String message) { }
        @Override public void logDebug(String tag, String message) { }
        @Override public void logVerbose(String tag, String message) { }
        @Override public void logStackTraceWithMessage(String tag, String message, Exception e) { }
        @Override public void logStackTrace(String tag, Exception e) { }
    }

    private static final class AiProviderException extends IOException {
        final int statusCode;
        final long retryAfterMs;

        AiProviderException(int statusCode, String body, long retryAfterMs) {
            super("HTTP " + statusCode + (body == null || body.isEmpty() ? "" : " " + body));
            this.statusCode = statusCode;
            this.retryAfterMs = retryAfterMs;
        }
    }

    private static final class AiSideHandleView extends TextView {
        private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint faceCutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shape = new Path();
        private final Path robot = new Path();
        private final RectF iconRect = new RectF();

        AiSideHandleView(Context context) {
            super(context);
            setWillNotDraw(false);
            setIncludeFontPadding(false);
            shapePaint.setColor(Color.rgb(146, 232, 255));
            shapePaint.setStyle(Paint.Style.FILL);
            rimPaint.setColor(Color.argb(120, 245, 252, 255));
            rimPaint.setStyle(Paint.Style.STROKE);
            rimPaint.setStrokeWidth(2f);
            iconPaint.setColor(Color.rgb(20, 24, 30));
            iconPaint.setStyle(Paint.Style.FILL);
            faceCutoutPaint.setColor(Color.rgb(146, 232, 255));
            faceCutoutPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float inset = 2f;
            shape.reset();
            shape.moveTo(w - inset, inset);
            shape.lineTo(w - inset, h - inset);
            shape.cubicTo(w * 0.1f, h - inset, w * 0.1f, inset, w - inset, inset);
            shape.close();
            canvas.drawPath(shape, shapePaint);
            canvas.drawPath(shape, rimPaint);
            drawSmartToyIcon(canvas, w, h);
            super.onDraw(canvas);
        }

        private void drawSmartToyIcon(Canvas canvas, float w, float h) {
            float size = Math.min(w * 0.68f, h * 0.45f);
            float left = (w * 0.66f) - (size * 0.5f);
            float top = (h * 0.5f) - (size * 0.5f);
            iconRect.set(left, top, left + size, top + size);
            float sx = size / 24f;
            float sy = size / 24f;
            canvas.save();
            canvas.translate(iconRect.left, iconRect.top);
            canvas.scale(sx, sy);
            robot.reset();
            robot.addRoundRect(new RectF(5f, 7f, 19f, 19f), 3.2f, 3.2f, Path.Direction.CW);
            robot.addRect(6.4f, 4.5f, 8.1f, 8.1f, Path.Direction.CW);
            robot.addCircle(7.25f, 3.7f, 1.1f, Path.Direction.CW);
            robot.addRect(15.9f, 4.5f, 17.6f, 8.1f, Path.Direction.CW);
            robot.addCircle(16.75f, 3.7f, 1.1f, Path.Direction.CW);
            robot.addRoundRect(new RectF(2.5f, 11f, 5.6f, 16f), 1.3f, 1.3f, Path.Direction.CW);
            robot.addRoundRect(new RectF(18.4f, 11f, 21.5f, 16f), 1.3f, 1.3f, Path.Direction.CW);
            robot.addRoundRect(new RectF(7f, 20f, 17f, 22f), 1f, 1f, Path.Direction.CW);
            canvas.drawPath(robot, iconPaint);
            canvas.drawCircle(9.2f, 12.3f, 1.35f, faceCutoutPaint);
            canvas.drawCircle(14.8f, 12.3f, 1.35f, faceCutoutPaint);
            canvas.drawRoundRect(new RectF(8.2f, 15.4f, 15.8f, 17.1f), 0.8f, 0.8f, faceCutoutPaint);
            canvas.restore();
        }
    }

    private final class OpencamBufferView extends View {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint lensPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF frame = new RectF();
        private final RectF lens = new RectF();

        OpencamBufferView(Context context) {
            super(context);
            setBackgroundColor(Color.rgb(14, 16, 20));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setStrokeWidth(dp(1));
            gridPaint.setColor(Color.argb(70, 205, 220, 235));
            textPaint.setTypeface(Typeface.MONOSPACE);
            textPaint.setTextSize(dp(12));
            textPaint.setColor(Color.rgb(220, 228, 238));
            lensPaint.setStyle(Paint.Style.STROKE);
            lensPaint.setStrokeWidth(dp(2));
            lensPaint.setColor(Color.argb(160, 255, 215, 95));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            long now = SystemClock.uptimeMillis();
            float w = getWidth();
            float h = getHeight();
            float margin = dp(18);
            frame.set(margin, margin, w - margin, h - margin);
            float t = (now % 2400L) / 2400f;
            int left = Color.HSVToColor(new float[]{(t * 360f) % 360f, 0.34f, 0.50f});
            int right = Color.HSVToColor(new float[]{((t * 360f) + 80f) % 360f, 0.42f, 0.32f});
            fillPaint.setShader(new LinearGradient(frame.left, frame.top, frame.right, frame.bottom, left, right, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(frame, dp(10), dp(10), fillPaint);
            fillPaint.setShader(null);

            int columns = 8;
            int rows = 6;
            for (int i = 1; i < columns; i++) {
                float x = frame.left + frame.width() * i / columns;
                canvas.drawLine(x, frame.top, x, frame.bottom, gridPaint);
            }
            for (int i = 1; i < rows; i++) {
                float y = frame.top + frame.height() * i / rows;
                canvas.drawLine(frame.left, y, frame.right, y, gridPaint);
            }

            float cx = frame.left + frame.width() * (0.18f + 0.64f * t);
            float cy = frame.top + frame.height() * (0.44f + 0.16f * (float) Math.sin(t * Math.PI * 2f));
            lens.set(cx - dp(38), cy - dp(38), cx + dp(38), cy + dp(38));
            canvas.drawOval(lens, lensPaint);
            canvas.drawLine(cx - dp(52), cy, cx + dp(52), cy, lensPaint);
            canvas.drawLine(cx, cy - dp(52), cx, cy + dp(52), lensPaint);

            int frameNumber = opencamFrameCounter + (int) (now / 100);
            canvas.drawText("opencam.display.buffer", frame.left + dp(16), frame.top + dp(28), textPaint);
            canvas.drawText("source=test-buffer  640x480  frame=" + frameNumber, frame.left + dp(16), frame.top + dp(48), textPaint);
            canvas.drawText(opencamStreaming ? "stream=on" : "stream=idle", frame.left + dp(16), frame.bottom - dp(18), textPaint);
            postInvalidateDelayed(33);
        }
    }

    private final class AndroPyTerminalViewClient implements TerminalViewClient {
        @Override
        public float onScale(float scale) {
            setTerminalTextSp(terminalTextSp() * scale);
            return 1.0f;
        }

        @Override
        public void onSingleTapUp(MotionEvent e) {
            if (termuxTerminalView != null) {
                termuxTerminalView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(termuxTerminalView, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        @Override public boolean shouldBackButtonBeMappedToEscape() { return false; }
        @Override public boolean shouldEnforceCharBasedInput() { return true; }
        @Override public boolean shouldUseCtrlSpaceWorkaround() { return false; }
        @Override public boolean isTerminalViewSelected() { return terminalVisible; }
        @Override public void copyModeChanged(boolean copyMode) { }
        @Override public boolean onKeyDown(int keyCode, KeyEvent e, TerminalSession session) { return false; }
        @Override public boolean onKeyUp(int keyCode, KeyEvent e) { return false; }
        @Override public boolean onLongPress(MotionEvent event) { return false; }
        @Override public boolean readControlKey() { return extraCtrl; }
        @Override public boolean readAltKey() { return extraAlt; }
        @Override public boolean readShiftKey() { return extraShift; }
        @Override public boolean readFnKey() { return false; }
        @Override public boolean onCodePoint(int codePoint, boolean ctrlDown, TerminalSession session) { return false; }
        @Override public void onEmulatorSet() { }
        @Override public void logError(String tag, String message) { }
        @Override public void logWarn(String tag, String message) { }
        @Override public void logInfo(String tag, String message) { }
        @Override public void logDebug(String tag, String message) { }
        @Override public void logVerbose(String tag, String message) { }
        @Override public void logStackTraceWithMessage(String tag, String message, Exception e) { }
        @Override public void logStackTrace(String tag, Exception e) { }
    }
}
