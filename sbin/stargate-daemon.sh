#!/usr/bin/env bash

#
# Copyright 2017 The Tsinghua University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


usage="Usage: stargate-daemon.sh [--config <conf-dir>] "

# if no args specified, show usage
if [ $# -lt 1 ]; then
  echo $usage
  exit 1
fi


if [ -z "${STARGATE_HOME}" ]; then
  export STARGATE_HOME="$(cd "`dirname "$0"`"/..; pwd)"
fi


# Load the stargate configuration
. "${STARGATE_HOME}/sbin/stargate-env.sh"


# get arguments

# Check if --config is passed as an argument. It is an optional parameter.
# Exit if the argument is not a directory.

if [ "$1" == "--config" ]
then
  shift
  conf_dir="$1"
  if [ ! -d "$conf_dir" ]
  then
    echo "ERROR : $conf_dir is not a directory"
    echo $usage
    exit 1
  else
    export STARGATE_CONF_DIR="$conf_dir"
  fi
  shift
fi

option=$1
shift
command=$1




rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
	num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
	while [ $num -gt 1 ]; do
	    prev=`expr $num - 1`
	    [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
	    num=$prev
	done
	mv "$log" "$log.$num";
    fi
}

if [ "$STARGATE_IDENT_STRING" = "" ]; then
  export STARGATE_IDENT_STRING="$USER"
fi

# get log directory
if [ "$STARGATE_LOG_DIR" = "" ]; then
  export STARGATE_LOG_DIR="${STARGATE_HOME}/logs"
fi
mkdir -p "$STARGATE_LOG_DIR"
touch "$STARGATE_LOG_DIR"/.stargate_test > /dev/null 2>&1
TEST_LOG_DIR=$?
if [ "${TEST_LOG_DIR}" = "0" ]; then
  rm -f "$STARGATE_LOG_DIR"/.stargate_test
else
  chown "$STARGATE_IDENT_STRING" "$STARGATE_LOG_DIR"
fi

if [ "$STARGATE_PID_DIR" = "" ]; then
  STARGATE_PID_DIR=/tmp
fi

# some variables
log="$STARGATE_LOG_DIR/stargate-$STARGATE_IDENT_STRING-$HOSTNAME.out"
pid="$STARGATE_PID_DIR/stargate-$STARGATE_IDENT_STRING.pid"


# Set default scheduling priority
if [ "$STARGATE_NICENESS" = "" ]; then
    export STARGATE_NICENESS=0
fi

run_command() {
  mode="$1"
  shift

  mkdir -p "$STARGATE_PID_DIR"

  if [ -f "$pid" ]; then
    TARGET_ID="$(cat "$pid")"
    if [[ $(ps -p "$TARGET_ID" -o comm=) =~ "java" ]]; then
      echo "$command running as process $TARGET_ID.  Stop it first."
      exit 1
    fi
  fi

  stargate_rotate_log "$log"
  echo "starting $command, logging to $log"

  case "$mode" in
    (class)
    nohup nice -n "$STARGATE_NICENESS" "${STARGATE_HOME}"/bin/stargate-class $command "$@" >> "$log" 2>&1 < /dev/null &
    newpid="$!"
      ;;
    (*)
      echo "unknown mode: $mode"
      exit 1
      ;;
  esac


  echo "$newpid" > "$pid"
  sleep 2
  # Check if the process has died; in that case we'll tail the log so the user can see
  if [[ ! $(ps -p "$newpid" -o comm=) =~ "java" ]]; then
    echo "failed to launch $command:"
    tail -2 "$log" | sed 's/^/  /'
    echo "full log in $log"
  fi
}


case $option in
  (start)
    run_command class "$@"
    ;;

  (stop)

    if [ -f $pid ]; then
      TARGET_ID="$(cat "$pid")"
      if [[ $(ps -p "$TARGET_ID" -o comm=) =~ "java" ]]; then
        echo "stopping $command"
        kill "$TARGET_ID" && rm -f "$pid"
      else
        echo "no $command to stop"
      fi
    else
      echo "no $command to stop"
    fi
    ;;


  (*)
    echo $usage
    exit 1
    ;;

esac