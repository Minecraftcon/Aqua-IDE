# Inotify Limits

Aqua IDE has large generated Android/runtime folders. Keep editor watchers focused on source files with `.vscode/settings.json`, and raise host inotify limits when running VSCodium plus Gradle/emulator together.

Apply on the host:

```bash
sudo tee /etc/sysctl.d/60-aqua-ide-inotify.conf >/dev/null <<EOF
fs.inotify.max_user_watches = 1048576
fs.inotify.max_user_instances = 1024
fs.inotify.max_queued_events = 32768
EOF
sudo sysctl --system
```

Verify:

```bash
sysctl fs.inotify.max_user_watches fs.inotify.max_user_instances fs.inotify.max_queued_events
for fd in /proc/[0-9]*/fd/*; do target=$(readlink "$fd" 2>/dev/null) || continue; case "$target" in anon_inode:inotify*) pid=${fd#/proc/}; pid=${pid%%/*}; comm=$(cat /proc/$pid/comm 2>/dev/null); printf "%s %s\n" "$pid" "$comm";; esac; done | sort | uniq -c | sort -nr | head -40
```

Current scan on 2026-05-30 showed the largest users were VSCodium renderer process around 7278 watches and Gradle daemon around 4692 watches. The repo contains more than 130k files if generated folders are included, but about 5.7k files after pruning ignored build/runtime/debug paths.
