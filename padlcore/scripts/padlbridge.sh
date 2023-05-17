#!/bin/bash
export PADLBRIDGE_HOME=/opt/padlbridge
export CONFIG_FILE=$1
export UPDATE_DELAY_SECS=30
export PADL_RUN_FLAG=true
export SLEEP_PID=

trap end_padl_app INT TERM

end_padl_app() {
    echo "Ending padlbridge app..."
    PADL_RUN_FLAG=false
    if [ ! -z $SLEEP_PID ]
    then
        kill $SLEEP_PID
    fi
}

check_config_file() {

    echo "Checking configuration file location."
    if [ -z ${PADL_CONFIG_FILE} ]
    then
        echo "Variable PADL_CONFIG_FILE is not set."
    else
        CONFIG_FILE="padlcore"
        echo "Trying to default configuration file provided by PADL_CONFIG_FILE (${PADL_CONFIG_FILE}) variable."
    fi

    if [ -z ${CONFIG_FILE} ]
    then
        CONFIG_FILE="${PADLBRIDGE_HOME}/conf/padlbridge.yaml"
        echo "No configuration file specified. Trying to use configuration file at default location (${CONFIG_FILE})."
    else 
        echo "Configuration file parameter provided."
    fi

}
run_padl_action() {
    local action=$1
    ${PADLBRIDGE_HOME}/core/bin/core $action ${CONFIG_FILE} 2>&1 1> >(tee) 
}

check_config_file
echo "Using configuration file located at ${CONFIG_FILE}."
while $PADL_RUN_FLAG
do
    run_padl_action run
    sleep $UPDATE_DELAY_SECS &
    SLEEP_PID=$$
    wait $SLEEP_PID
    SLEEP_PID=
done

