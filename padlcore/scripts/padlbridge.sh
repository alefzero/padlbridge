#!/bin/bash
export PADLBRIDGE_HOME=/opt/padlbridge
export CONFIG_FILE=$1
export UPDATE_DELAY_SECS=30
export PADL_RUN_FLAG=true
export SLEEP_PID=
export PADL_LOG_LEVEL="${PADL_LOG_LEVEL:-INFO}"

trap end_padl_app INT TERM

end_padl_app() {
    echo "Ending padlbridge app..."
    kill $PADL_PID
}

check_config_file() {

    echo "Checking configuration file location."
    if [ -z ${PADL_CONFIG_FILE} ]
    then
        echo "Variable PADL_CONFIG_FILE is not set."
    else
        CONFIG_FILE="${PADLBRIDGE_HOME}/conf/padlbridge.yaml"
        echo "Trying to default configuration file provided by PADL_CONFIG_FILE (${PADL_CONFIG_FILE}) variable."
    fi

    if [ -z ${CONFIG_FILE} ]
    then
        CONFIG_FILE="${PADLBRIDGE_HOME}/conf/padlbridge.yaml"
        echo "No configuration file specified. Trying to use configuration file at default location."
    else 
        echo "Configuration file parameter provided."
    fi

}

check_config_file
echo "Using configuration file located at ${CONFIG_FILE}."
cd ${PADLBRIDGE_HOME}
${PADLBRIDGE_HOME}/core/bin/core run ${CONFIG_FILE} 2>&1 1> >(tee) &
PADL_PID=$!
wait $PADL_PID


