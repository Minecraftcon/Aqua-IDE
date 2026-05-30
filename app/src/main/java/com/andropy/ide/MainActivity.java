package com.andropy.ide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Build;
import android.os.StatFs;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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

import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String PREFS = "andropy_editor";
    private static final String DEFAULT_FILE = "new.py";
    private static final String PREFIX_DIR = "usr";
    private static final String RUNTIME_BASIC_VERSION = "andropy-basic-runtime-8";
    private static final String RUNTIME_EXTENDED_VERSION = "andropy-extended-runtime-9";
    private static final String RUNTIME_BASIC_RELEASE_BASE = "https://github.com/Minecraftcon/Aqua-IDE/releases/download/runtime-v8/";
    private static final String RUNTIME_EXTENDED_RELEASE_BASE = "https://github.com/Minecraftcon/Aqua-IDE/releases/download/runtime-v9/";
    private static final String RUNTIME_BASIC_X86_64_ZIP = "aqua-runtime-x86_64-v8.zip";
    private static final String RUNTIME_BASIC_ARM64_ZIP = "aqua-runtime-arm64-v8a-v8.zip";
    private static final String RUNTIME_EXTENDED_X86_64_ZIP = "aqua-runtime-x86_64-v9.tar.zst";
    private static final String RUNTIME_EXTENDED_ARM64_ZIP = "aqua-runtime-arm64-v8a-v9.tar.zst";
    private static final int RUNTIME_BASIC = 0;
    private static final int RUNTIME_EXTENDED = 1;
    private static final int RUNTIME_MAX = 2;
    private static final int BG = Color.rgb(50, 54, 61);
    private static final int BAR = Color.rgb(35, 39, 45);
    private static final int EDITOR_BG = Color.rgb(58, 63, 72);
    private static final int GUTTER_LINE = Color.rgb(104, 111, 124);
    private static final int GUTTER_TEXT = Color.rgb(204, 212, 223);
    private static final int ACTIVE_LINE = Color.argb(52, 220, 232, 255);
    private static final int TEXT = Color.rgb(250, 252, 255);
    private static final int MUTED = Color.rgb(212, 219, 229);
    private static final int ACCENT = Color.rgb(106, 237, 156);
    private static final int KEYWORD = Color.rgb(118, 190, 255);
    private static final int STRING = Color.rgb(255, 218, 115);
    private static final int COMMENT = Color.rgb(167, 178, 191);
    private static final int NUMBER = Color.rgb(255, 153, 177);
    private static final int FUNCTION = Color.rgb(123, 242, 198);
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
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(?:\\.\\d+)?\\b");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\bdef\\s+([A-Za-z_][A-Za-z0-9_]*)");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b(and|as|assert|async|await|break|class|continue|def|del|elif|else|except|False|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|or|pass|raise|return|True|try|while|with|yield)\\b"
    );
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
    private EditText editor;
    private View scrim;
    private LinearLayout sidePanel;
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
    private View bootstrapProgressFill;
    private final StringBuilder bootstrapOutput = new StringBuilder();
    private final StringBuilder pipOutput = new StringBuilder();
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
    private boolean panelOpen;
    private boolean terminalVisible;
    private boolean fileManagerVisible;
    private File fileManagerCurrentDir;
    private String fileManagerCurrentTitle;
    private LinearLayout fileManagerScreenBody;
    private final Set<String> selectedFilePaths = new HashSet<>();
    private final ArrayList<File> pendingFileOperationSources = new ArrayList<>();
    private String pendingFileOperation;
    private boolean bootstrapShowingOutput;
    private boolean bootstrapDownloading;
    private String terminalStartupCommand;
    private String terminalStartupScript;
    private boolean terminalReturnToEditorOnExit;
    private int panelAnimationToken;
    private int changedStart;
    private int changedBefore;
    private int changedCount;
    private int selectedRuntimeProfile = RUNTIME_BASIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        selectedRuntimeProfile = prefs.getInt("runtime_profile", RUNTIME_BASIC);
        if (BuildConfig.ANDROPY_PREBUNDLED_RUNTIME) {
            selectedRuntimeProfile = RUNTIME_EXTENDED;
            prefs.edit().putInt("runtime_profile", selectedRuntimeProfile).apply();
        }
        initProjectRoots();
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

        setBootstrapProgress(0.02f, "Testing connection");
        appendBootstrapOutput("$ bootstrap-start");
        return shell;
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

    private void startBootstrap() {
        Thread bootstrapThread = new Thread(() -> {
            try {
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
            try {
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
        highlight(editor.getText());
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
        files.setOnClickListener(v -> showFileManagerRoot());
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

        editor = new NumberedEditor(this);
        editor.setText(prefs.getString("code", defaultPython()));
        editor.setTextColor(TEXT);
        editor.setHintTextColor(MUTED);
        editor.setTextSize(15);
        editor.setIncludeFontPadding(false);
        editor.setTypeface(Typeface.MONOSPACE);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(false);
        editor.setHorizontalScrollBarEnabled(false);
        editor.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setMinLines(32);
        editor.setPadding(dp(58), dp(10), dp(16), dp(20));
        editor.setBackgroundColor(EDITOR_BG);
        editor.setLineSpacing(0, 0.92f);
        editor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                changedStart = start;
                changedBefore = count;
                changedCount = after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                prefs.edit().putString("code", s.toString()).apply();
                saveOpenedFileQuietly(s.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                applyPythonTypingHelp(editable);
                highlight(editable);
                updatePanelStatus();
            }
        });
        root.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));
        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));

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
        try {
            byte[] data = readFileBytes(file, 1024 * 1024);
            String code = new String(data, StandardCharsets.UTF_8);
            prefs.edit()
                    .putString("code", code)
                    .putString("file_name", file.getName())
                    .putString("file_path", file.getAbsolutePath())
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
        content.addView(drawerRow("Settings", "Settings", "ic_settings_24", ""));

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
        termuxTerminalView.setTextSize(dp(15));
        termuxTerminalView.setTypeface(Typeface.MONOSPACE);
        termuxTerminalView.setFocusable(true);
        termuxTerminalView.setFocusableInTouchMode(true);
        root.addView(termuxTerminalView, new LinearLayout.LayoutParams(-1, 0, 1));

        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        startTerminal();
        return shell;
    }

    private void showTerminal() {
        prefs.edit().putString("code", editor == null ? "" : editor.getText().toString()).apply();
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
        setContentView(buildPipScreen());
        refreshPipPackages();
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
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(48)));

        View yellowDivider = new View(this);
        yellowDivider.setBackgroundColor(YELLOW_DIVIDER);
        root.addView(yellowDivider, new LinearLayout.LayoutParams(-1, dp(2)));

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
        pipPackageList.removeAllViews();
        pipPackageList.addView(packageMessage("Scanning installed modules..."));
        if (pipStatusText != null && pipStatusText.getText().toString().equals("Ready")) {
            pipStatusText.setText("Scanning installed modules");
        }
        new Thread(() -> {
            List<PipPackageInfo> packages = loadInstalledPipPackages();
            runOnUiThread(() -> renderPipPackages(packages));
        }, "andropy-pip-list").start();
    }

    private List<PipPackageInfo> loadInstalledPipPackages() {
        ensureProjectRoots();
        List<PipPackageInfo> packages = new ArrayList<>();
        String script = ""
                + "import importlib.metadata as md, os\n"
                + "def clean(value):\n"
                + "    return (value or '').replace('\\t', ' ').replace('\\n', ' ').strip()\n"
                + "for dist in sorted(md.distributions(), key=lambda d: (d.metadata.get('Name') or '').lower()):\n"
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
                + "    print(f'{name}\\t{version}\\t{size}\\t{summary}', flush=True)\n";
        ProcessBuilder builder = new ProcessBuilder(new File(binRoot, "python").getAbsolutePath(), "-c", script);
        builder.redirectErrorStream(true);
        applyRuntimeEnvironment(builder);
        try {
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t", 4);
                    if (parts.length >= 3) {
                        long size = 0;
                        try {
                            size = Long.parseLong(parts[2]);
                        } catch (NumberFormatException ignored) {
                        }
                        String summary = parts.length == 4 ? parts[3] : "";
                        packages.add(new PipPackageInfo(parts[0], parts[1], size, summary));
                    }
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException ignored) {
            if (ignored instanceof InterruptedException) Thread.currentThread().interrupt();
        }
        for (PipPackageInfo item : packages) {
            String remoteSummary = fetchPyPiSummary(item.name);
            if (!remoteSummary.isEmpty()) item.summary = remoteSummary;
        }
        return packages;
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

    private void applyRuntimeEnvironment(ProcessBuilder builder) {
        String libPath = getApplicationInfo().nativeLibraryDir + ":" + libRoot.getAbsolutePath();
        builder.environment().put("PREFIX", prefixRoot.getAbsolutePath());
        builder.environment().put("HOME", homeRoot.getAbsolutePath());
        builder.environment().put("PATH", binRoot.getAbsolutePath() + ":/system/bin:/system/xbin");
        builder.environment().put("LD_LIBRARY_PATH", libPath);
        builder.environment().put("TMPDIR", tmpRoot.getAbsolutePath());
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
        terminalVisible = false;
        fileManagerVisible = false;
        terminalReturnToEditorOnExit = false;
        stopTerminal();
        setContentView(buildEditorScreen());
        highlight(editor.getText());
        editor.scrollTo(0, editor.getScrollY());
    }

    private void startTerminal() {
        ensureProjectRoots();
        stopTerminal();

        String startupScript = terminalStartupScript;
        terminalStartupScript = null;
        File bash = new File(binRoot, "bash");
        File packagedBash = new File(getApplicationInfo().nativeLibraryDir, "libandropy_bash.so");
        File packagedLauncher = new File(getApplicationInfo().nativeLibraryDir, "libandropy_bash_launcher.so");
        boolean hasBash = packagedBash.canExecute() || bash.canExecute();
        String shell = hasBash && packagedLauncher.canExecute()
                ? packagedLauncher.getAbsolutePath()
                : hasBash ? bash.getAbsolutePath() : "/system/bin/sh";
        String path = binRoot.getAbsolutePath() + ":/system/bin:/system/xbin";
        String startupCommand = terminalStartupCommand;
        terminalStartupCommand = null;
        if (startupScript != null) {
            startupCommand = "exec sh " + shellQuote(startupScript);
        }
        String[] args = hasBash
                ? startupCommand == null
                ? new String[]{"--rcfile", bashRcFile().getAbsolutePath(), "-i"}
                : new String[]{"--rcfile", bashRcFile().getAbsolutePath(), "-i", "-c", startupCommand}
                : startupCommand == null ? new String[]{"-i"} : new String[]{"-i", "-c", startupCommand};
        String[] env = new String[]{
                "PREFIX=" + prefixRoot.getAbsolutePath(),
                "HOME=" + homeRoot.getAbsolutePath(),
                "ANDROPY_PREFIX_REAL=" + prefixRealRoot.getAbsolutePath(),
                "ANDROPY_HOME_REAL=" + homeRealRoot.getAbsolutePath(),
                "ANDROPY_START_REAL=" + homeRealRoot.getAbsolutePath(),
                "ANDROPY_BASH_PATH=" + packagedBash.getAbsolutePath(),
                "PATH=" + path,
                "LD_LIBRARY_PATH=" + getApplicationInfo().nativeLibraryDir + ":" + libRoot.getAbsolutePath(),
                "APT_CONFIG=" + new File(etcRoot, "apt/apt.conf").getAbsolutePath(),
                "COLORTERM=truecolor",
                "CLICOLOR=1",
                "CLICOLOR_FORCE=1",
                "FORCE_COLOR=1",
                "LS_COLORS=" + lsColors(),
                "TMPDIR=" + tmpRoot.getAbsolutePath(),
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
            writer.write("export PATH=\"$PREFIX/bin:/system/bin:/system/xbin\"\n");
            writer.write("export LD_LIBRARY_PATH=\"" + getApplicationInfo().nativeLibraryDir + ":$PREFIX/lib\"\n");
            writer.write("export TERMINFO=\"$PREFIX/share/terminfo\"\n");
            writer.write("export APT_CONFIG=\"$PREFIX/etc/apt/apt.conf\"\n");
            writer.write("export COLORTERM=truecolor\n");
            writer.write("export CLICOLOR=1\n");
            writer.write("export CLICOLOR_FORCE=1\n");
            writer.write("export FORCE_COLOR=1\n");
            writer.write("export LS_COLORS='" + lsColors() + "'\n");
            writer.write("export LANG=\"C.UTF-8\"\n");
            writer.write("export LC_ALL=\"C.UTF-8\"\n");
            writer.write("export TMPDIR=\"$PREFIX/tmp\"\n");
            writer.write("[ -r \"$PREFIX/etc/profile\" ] && . \"$PREFIX/etc/profile\"\n");
            writer.write("cd \"$ANDROPY_HOME_REAL\" 2>/dev/null || cd \"$HOME\" 2>/dev/null\n");
            writer.write("export PWD=\"$HOME\"\n");
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
                        + "export TMPDIR=\"$PREFIX/tmp\"\n"
                        + "export PATH=\"$PREFIX/bin:/system/bin:/system/xbin\"\n"
                        + "export LD_LIBRARY_PATH=\"" + getApplicationInfo().nativeLibraryDir + ":$PREFIX/lib\"\n"
                        + "export TERMINFO=\"$PREFIX/share/terminfo\"\n"
                        + "export APT_CONFIG=\"$PREFIX/etc/apt/apt.conf\"\n"
                        + "export COLORTERM=truecolor\n"
                        + "export CLICOLOR=1\n"
                        + "export CLICOLOR_FORCE=1\n"
                        + "export FORCE_COLOR=1\n"
                        + "export LS_COLORS='" + lsColors() + "'\n"
                        + "export LANG=\"C.UTF-8\"\n"
                        + "export LC_ALL=\"C.UTF-8\"\n"
                        + "umask 022\n"
                        + "for f in \"$PREFIX\"/etc/profile.d/*.sh; do [ -r \"$f\" ] && . \"$f\"; done\n");
        writeText(new File(profileRoot, "00-andropy-environment.sh"),
                "export ANDROPY_PACKAGE=\"" + getPackageName() + "\"\n"
                        + "export ANDROPY_BOOTSTRAP_VARIANT=\"andropy-native-userland\"\n"
                        + "export ANDROID_DATA=\"/data\"\n"
                        + "export ANDROID_ROOT=\"/system\"\n"
                        + "mkdir -p \"$PREFIX/tmp\" \"$PREFIX/var/tmp\" \"$PREFIX/var/run\" \"$PREFIX/var/log\" 2>/dev/null\n");
        writeText(new File(etcRoot, "andropy-bootstrap-packages"),
                "bash\nbzip2\ncommand-not-found\ncoreutils\ncurl\ndash\ndebianutils\ndiffutils\n"
                        + "dos2unix\ned\nfindutils\ngawk\ngrep\ngzip\ninetutils\nless\nlsof\nnano\n"
                        + "net-tools\npatch\nprocps\npsmisc\nsed\ntar\nunzip\nutil-linux\nxz-utils\n"
                        + "clang\nlibllvm\nlld\nllvm\nmake\npkg-config\n");
        writeText(new File(etcRoot, "apt/sources.list.disabled"),
                "# Upstream bootstrap repositories are disabled for this app build.\n"
                        + "# AndroPy packages will be published for /data/data/" + getPackageName() + "/files/usr.\n");
        writeText(new File(etcRoot, "apt/sources.list"),
                "# AndroPy package sources are disabled until the app repository is ready for:\n"
                        + "# /data/data/" + getPackageName() + "/files/usr\n");
        writeText(new File(etcRoot, "apt/apt.conf"),
                "Dir \"/data/data/" + getPackageName() + "/files/usr\";\n"
                        + "Dir::Etc \"etc/apt\";\n"
                        + "Dir::State \"var/lib/apt\";\n"
                        + "Dir::Cache \"var/cache/apt\";\n"
                        + "Dir::State::status \"/data/data/" + getPackageName() + "/files/usr/var/lib/dpkg/status\";\n"
                        + "Dir::Bin::methods \"/data/data/" + getPackageName() + "/files/usr/lib/apt/methods\";\n"
                        + "APT::Sandbox::User \"root\";\n");
        installExecutableScript("andropy-bootstrap-info",
                "#!/system/bin/sh\n"
                        + "[ -n \"$PREFIX\" ] || PREFIX=\"/data/data/" + getPackageName() + "/files/usr\"\n"
                        + "[ -n \"$HOME\" ] || HOME=\"/data/data/" + getPackageName() + "/files/home\"\n"
                        + "echo \"PREFIX=$PREFIX\"\n"
                        + "echo \"HOME=$HOME\"\n"
                        + "echo \"Variant: andropy-native-userland\"\n"
                        + "echo \"Package manager: disabled until the AndroPy repository is ready\"\n"
                        + "echo \"Packaged now: bash, GNU coreutils, clang/LLVM, make, nano, Python\"\n"
                        + "echo \"Bootstrap payload:\"\n"
                        + "cat \"$PREFIX/etc/andropy-bootstrap-packages\" 2>/dev/null\n");
        installDisabledPackageManagerCommands();
        installNativePlaceholder("pkg");
        installNativePlaceholder("apt");
        installNativePlaceholder("dpkg");
        installNativePlaceholder("clang");
        installNativePlaceholder("clang++");
        installNativePlaceholder("llvm-config");
        installNativePlaceholder("gcc");
        installNativePlaceholder("g++");
        installNativePlaceholder("make");
    }

    private void installDisabledPackageManagerCommands() {
        String script = "#!/system/bin/sh\n"
                + "echo \"AndroPy package manager is disabled in this build.\"\n"
                + "echo \"A native AndroPy repository will be added later.\"\n"
                + "exit 1\n";
        String[] commands = new String[]{"apt", "apt-get", "apt-cache", "apt-config", "apt-mark", "apt-key", "pkg"};
        for (String command : commands) installExecutableScript(command, script);
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
                    copyBundledRuntimePayload(payload);
                } else {
                    setBootstrapProgress(0.36f, "Downloading " + runtimeAbiName() + " runtime");
                    appendBootstrapOutput("$ download-runtime " + runtimeDownloadUrl());
                    downloadRuntimePayload(payload);
                }
            }
            setBootstrapProgress(0.62f, "Extracting " + runtimeAbiName() + " runtime");
            appendBootstrapOutput("$ extract-runtime " + payload.getName());
            if (payload.getName().endsWith(".tar.zst")) {
                extractTarZstFile(payload, prefixRoot);
            } else {
                extractZipFile(payload, prefixRoot);
            }
            chmodRuntimeTree(prefixRoot);
            try (FileOutputStream output = new FileOutputStream(marker)) {
                output.write(runtimeAssetVersion().getBytes(StandardCharsets.UTF_8));
            }
            payload.delete();
        } catch (IOException e) {
            appendBootstrapOutput("! runtime install failed: " + e.getMessage());
            throw new IllegalStateException("runtime install failed", e);
        }
    }

    private String runtimeAbiAssetDir() {
        return "runtime-" + runtimeAbiName();
    }

    private String runtimeAbiName() {
        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) return "arm64-v8a";
            if ("x86_64".equals(abi)) return "x86_64";
        }
        return "x86_64";
    }

    private String runtimeZipName() {
        if (selectedRuntimeProfile == RUNTIME_EXTENDED) {
            return "arm64-v8a".equals(runtimeAbiName()) ? RUNTIME_EXTENDED_ARM64_ZIP : RUNTIME_EXTENDED_X86_64_ZIP;
        }
        return "arm64-v8a".equals(runtimeAbiName()) ? RUNTIME_BASIC_ARM64_ZIP : RUNTIME_BASIC_X86_64_ZIP;
    }

    private String runtimeDownloadUrl() {
        String base = selectedRuntimeProfile == RUNTIME_EXTENDED ? RUNTIME_EXTENDED_RELEASE_BASE : RUNTIME_BASIC_RELEASE_BASE;
        return base + runtimeZipName();
    }

    private String runtimeAssetVersion() {
        return selectedRuntimeProfile == RUNTIME_EXTENDED ? RUNTIME_EXTENDED_VERSION : RUNTIME_BASIC_VERSION;
    }

    private void copyBundledRuntimePayload(File target) throws IOException {
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        try (InputStream input = getAssets().open(runtimeZipName());
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[65536];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
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
        byte[] buffer = new byte[65536];
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(partial)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                copied += read;
                if (total > 0) {
                    float fraction = copied / (float) total;
                    setBootstrapProgress(0.36f + (0.20f * fraction), "Downloading " + formatBytes(copied) + " / " + formatBytes(total));
                }
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
        try (InputStream fileInput = new FileInputStream(archiveFile);
             InputStream zstd = new ZstdInputStream(fileInput)) {
            byte[] header = new byte[512];
            String pendingLongName = null;
            String pendingLongLink = null;
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
                    try {
                        Os.link(linkTarget.getAbsolutePath(), outputFile.getAbsolutePath());
                    } catch (ErrnoException ignored) {
                        if (linkTarget.isFile()) {
                            copyFile(linkTarget, outputFile);
                        } else {
                            try {
                                Os.symlink(linkTarget.getAbsolutePath(), outputFile.getAbsolutePath());
                            } catch (ErrnoException ignoredToo) {
                            }
                        }
                    }
                    applyTarMode(outputFile, mode);
                } else {
                    ensureParentDirectory(outputFile);
                    try (OutputStream output = new FileOutputStream(outputFile)) {
                        copyExact(zstd, output, size);
                    }
                    applyTarMode(outputFile, mode);
                }
                skipTarPadding(zstd, size);
            }
        }
    }

    private String readTarTextEntry(InputStream input, long size) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(size, 65536));
        copyExact(input, output, size);
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
        byte[] buffer = new byte[65536];
        long remaining = bytes;
        while (remaining > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read == -1) throw new IOException("unexpected end of tar entry");
            output.write(buffer, 0, read);
            remaining -= read;
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
            byte[] buffer = new byte[65536];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
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

    private void runCurrentFile() {
        hideKeyboard();
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        String code = editor.getText().toString();
        String openedPath = prefs.getString("file_path", "");
        File script = openedPath == null || openedPath.isEmpty() ? new File(homeRoot, currentFileName()) : new File(openedPath);
        writeText(script, code);
        File runner = new File(homeRoot, ".andropy-run-current.sh");
        writeText(runner, "#!/system/bin/sh\n"
                + "python " + shellQuote(script.getAbsolutePath()) + "\n"
                + "status=$?\n"
                + "printf '\\n[python exit %s]\\n' \"$status\"\n"
                + "printf 'Press Enter to return to editor...'\n"
                + "IFS= read -r _\n"
                + "exit \"$status\"\n");
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
        if (fileManagerVisible) {
            if (!selectedFilePaths.isEmpty()) {
                clearFileSelection();
                refreshFileManager();
                return;
            }
            if (fileManagerCurrentDir != null) {
                navigateFileManagerBack(fileManagerCurrentDir);
            } else {
                showEditor();
            }
            return;
        }
        if (terminalVisible) {
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
        stopTerminal();
        super.onDestroy();
    }

    private void highlight(Editable editable) {
        if (highlighting || editable == null) return;
        highlighting = true;
        int cursor = editor.getSelectionStart();
        ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) editable.removeSpan(span);

        colorMatches(editable, KEYWORD_PATTERN, KEYWORD, 0);
        colorMatches(editable, NUMBER_PATTERN, NUMBER, 0);
        colorMatches(editable, FUNCTION_PATTERN, FUNCTION, 1);
        colorMatches(editable, STRING_PATTERN, STRING, 0);
        colorMatches(editable, COMMENT_PATTERN, COMMENT, 0);

        if (cursor >= 0 && cursor <= editable.length()) editor.setSelection(cursor);
        highlighting = false;
    }

    private void applyPythonTypingHelp(Editable editable) {
        if (applyingHelperEdit || editable == null || changedBefore != 0 || changedCount != 1) return;
        int insertedAt = changedStart;
        if (insertedAt < 0 || insertedAt >= editable.length()) return;

        char inserted = editable.charAt(insertedAt);
        if (isCloser(inserted) && insertedAt + 1 < editable.length() && editable.charAt(insertedAt + 1) == inserted) {
            applyingHelperEdit = true;
            editable.delete(insertedAt, insertedAt + 1);
            editor.setSelection(insertedAt + 1);
            applyingHelperEdit = false;
            return;
        }

        char closing = matchingCloser(inserted);
        if (closing != 0) {
            applyingHelperEdit = true;
            editable.insert(insertedAt + 1, String.valueOf(closing));
            editor.setSelection(insertedAt + 1);
            applyingHelperEdit = false;
            return;
        }

        if (inserted == '\n') {
            if (insertedAt > 0 && insertedAt + 1 < editable.length()
                    && isPair(editable.charAt(insertedAt - 1), editable.charAt(insertedAt + 1))) {
                String baseIndent = lineIndentBefore(editable, insertedAt);
                String innerIndent = baseIndent + "    ";
                applyingHelperEdit = true;
                editable.insert(insertedAt + 1, innerIndent + "\n" + baseIndent);
                editor.setSelection(insertedAt + 1 + innerIndent.length());
                applyingHelperEdit = false;
                return;
            }
            String indent = nextPythonIndent(editable, insertedAt);
            if (!indent.isEmpty()) {
                applyingHelperEdit = true;
                editable.insert(insertedAt + 1, indent);
                editor.setSelection(insertedAt + 1 + indent.length());
                applyingHelperEdit = false;
            }
        }
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

    private String lineIndentBefore(Editable editable, int offset) {
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

    private String nextPythonIndent(Editable editable, int newlineAt) {
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

        Editable text = editor.getText();
        int length = text == null ? 0 : text.length();
        int cursor = Math.max(0, Math.min(editor.getSelectionStart(), length));
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int panelWidth() {
        return dp(PANEL_WIDTH_DP);
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

        NumberedEditor(Activity activity) {
            super(activity);
            numberPaint.setColor(GUTTER_TEXT);
            numberPaint.setTextAlign(Paint.Align.RIGHT);
            numberPaint.setTypeface(Typeface.MONOSPACE);
            numberPaint.setTextSize(dp(15));
            dividerPaint.setColor(GUTTER_LINE);
            dividerPaint.setStrokeWidth(Math.max(1f, getResources().getDisplayMetrics().density));
            activeLinePaint.setColor(ACTIVE_LINE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int lineCount = Math.max(1, getLineCount());
            int gutterX = dp(34);
            int dividerX = dp(45);
            Layout layout = getLayout();
            int activeLine = layout == null ? 0 : layout.getLineForOffset(Math.max(0, getSelectionStart()));
            if (activeLine >= 0 && activeLine < lineCount) {
                int top;
                int bottom;
                if (layout == null) {
                    top = activeLine == 0 ? 0 : getExtendedPaddingTop();
                    bottom = top + getLineHeight();
                } else {
                    top = activeLine == 0
                            ? 0
                            : getExtendedPaddingTop() + layout.getLineTop(activeLine) - getScrollY();
                    bottom = getExtendedPaddingTop() + layout.getLineBottom(activeLine) - getScrollY();
                }
                canvas.drawRect(0, top, getWidth(), bottom, activeLinePaint);
            }
            canvas.drawLine(dividerX, 0, dividerX, getHeight(), dividerPaint);
            for (int i = 0; i < lineCount; i++) {
                int baseline = getLineBounds(i, null);
                canvas.drawText(String.valueOf(i + 1), gutterX, baseline, numberPaint);
            }
            super.onDraw(canvas);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
            updatePanelStatus();
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
                    highlight(editor.getText());
                    editor.clearFocus();
                    editor.scrollTo(0, editor.getScrollY());
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

    private final class AndroPyTerminalViewClient implements TerminalViewClient {
        @Override
        public float onScale(float scale) {
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
        @Override public boolean readControlKey() { return false; }
        @Override public boolean readAltKey() { return false; }
        @Override public boolean readShiftKey() { return false; }
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
