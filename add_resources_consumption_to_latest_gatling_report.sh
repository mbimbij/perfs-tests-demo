#! /bin/bash

gatlingReportsPath="target/gatling"
lastRunPath=""
startTime=0
endTime=0

getLastRunPathIfExists(){
  local retval=$(cat "$gatlingReportsPath/lastRun.txt")
  echo "$gatlingReportsPath/$retval"
}

getStartTime(){
  echo "$(head -1 $lastRunPath/simulation.log | awk '{print $4}')"
}

getEndTime(){
  echo "$(grep "REQUEST" $lastRunPath/simulation.log | tail -1 | awk '{print $4}')"
}

addResourceConsumptionInReport(){
  awk -F';' -v "start=$startTime" -v "end=$endTime"  'BEGIN{print "timestamp;cpu;memory"}{if(($1 >= start) && ($1 <= end)){print $0}}' resources-consumption.log > $lastRunPath/resources-consumption.log
}

lastRunPath="$(getLastRunPathIfExists)"
startTime="$(getStartTime)"
endTime="$(getEndTime)"
addResourceConsumptionInReport

