#!/usr/bin/env bash
# Stops dev clients/servers started by gradle (matches by process name, never by this script's own command line).
for pid in $(ps -eo pid,args | awk '/[D]evLaunchInjector|[N]eoForge.*runClient|[r]unClient|[r]unServer/ {print $1}'); do [ "$pid" != "$$" ] && kill "$pid" 2>/dev/null; done
sleep 3; ps -eo pid,args | awk '/[D]evLaunchInjector|[r]unClient|[r]unServer/' | wc -l
