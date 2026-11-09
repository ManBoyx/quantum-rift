#!/usr/bin/env bash
# Interactive dev server: ./scripts/dev-server.sh start|stop|cmd "<command>"|log   (loader via $LOADER, default fabric)
cd "$(dirname "$0")/.."
L="${LOADER:-fabric}"; export JAVA_HOME="${JAVA_HOME:-$HOME/Outils/JDK/jdk-21}"
FIFO="/tmp/qr-$L.in"; LOG="$L/runs/dev.log"
case "$1" in
  start)
    d="$L/runs/server"; mkdir -p "$d"; echo "eula=true" > "$d/eula.txt"
    printf 'online-mode=false\nlevel-seed=424242\ngamemode=creative\nspawn-protection=0\nview-distance=6\n' > "$d/server.properties"
    rm -f "$FIFO"; mkfifo "$FIFO"
    nohup bash -c "exec 3<>$FIFO; ./gradlew :$L:runServer --console=plain < $FIFO > $LOG 2>&1" > /dev/null 2>&1 &
    until grep -qE 'Done \(|BUILD FAILED' "$LOG" 2>/dev/null; do sleep 2; done; grep -E 'Done \(|BUILD FAILED' "$LOG" | tail -1 ;;
  cmd) echo "$2" > "$FIFO"; sleep "${3:-2}"; tail -n 3 "$LOG" | sed 's/^\[[0-9:]*\] \[[^]]*\] [^ ]* //' ;;
  log) tail -n "${2:-20}" "$LOG" ;;
  stop) echo stop > "$FIFO"; sleep 8 ;;
esac
