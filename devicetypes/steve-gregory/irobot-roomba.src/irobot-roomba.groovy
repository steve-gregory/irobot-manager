/**
*  Roomba 9xx - Virtual Switch
*
*  Copyright 2016 Steve-Gregory
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*/
metadata {
    definition (name: "Roomba 9xx - Virtual Switch", namespace: "Steve-Gregory", author: "Steve-Gregory") {
        capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "Configuration"

        command "dock"
        command "refresh"
        command "resume"
        command "pause"

        attribute "batteryLevel", "number"
        attribute "jobCount", "number"
        attribute "headline", "string"
        attribute "robotName", "string"
        attribute "preferences_set", "string"
        attribute "status", "string"
    }
}
// simulator metadata
simulator {
}
//Preferences
preferences {
        section("Roomba Credentials") {
            input title: "Roomba Credentials", description: "The username/password can be retrieved via node.js & dorita980", displayDuringSetup: true, type: "paragraph", element: "paragraph"
            input "roomba_username", "text", title: "Roomba username/blid", required: true, displayDuringSetup: true
            input "roomba_password", "password", title: "Roomba password", required: true, displayDuringSetup: true
            input "roomba_host", "string", title:"Roomba Host (Default: Use the Cloud)", defaultValue:""
        }
        section("Misc.") {
            input title: "Polling Interval", description: "This feature allows you to change the frequency of polling for the robot in minutes (1-59)", displayDuringSetup: true, type: "paragraph", element: "paragraph"
            input "pollInterval", "number", title: "Polling Interval", description: "Change polling frequency (in minutes)", defaultValue:4, range: "1..59", required: true, displayDuringSetup: true
        }
}
// Settings updated
def updated() {
    //log.debug "Updated settings ${settings}..
    schedule("0 0/${settings.pollInterval} * * * ?", poll)  // 4min polling is normal for irobots
    poll()
}
// Configuration
def configure() {
    log.debug "Configuring.."
    poll()
}
//Refresh
def refresh() {
    log.debug "Executing 'refresh'"
    poll()
}
//Polling
def poll() {
    log.debug "Polling for status ----"
    sendEvent(name: "headline", value: "Polling the API", displayed: false)
    state.RoombaCmd = "getStatus"
    apiGet()
}
// UI tile definitions
tiles {

    multiAttributeTile(name:"CLEAN", type:"lighting", width: 6, height: 4, canChangeIcon: true) {
        tileAttribute("device.status", key: "PRIMARY_CONTROL") {
            attributeState "unknown", label: 'Error', icon: "st.switches.switch.off", backgroundColor: "#bc2323" // No action allowed here
            attributeState "bin-full", label: 'Bin Full', icon: "st.switches.switch.off", backgroundColor: "#bc2323" // No action allowed here
            attributeState "docked", label: 'Start Clean', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "starting"
            attributeState "docking", label: 'Docking', icon: "st.switches.switch.off", backgroundColor: "#ffa81e" // No action allowed here
            attributeState "starting", label: 'Starting Clean', icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            attributeState "cleaning", label: 'Stop Clean', action: "stop", icon: "st.switches.switch.on", backgroundColor: "#79b821"
            attributeState "pausing", label: 'Stop Clean', icon: "st.switches.switch.on", backgroundColor: "#79b821" // No action allowed here
            attributeState "paused", label: 'Send Home', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "docking"
            attributeState "resuming", label: 'Stop Clean', icon: "st.switches.switch.on", backgroundColor: "#79b821" // No action allowed here
        }
        tileAttribute("device.headline", key: "SECONDARY_CONTROL") {
           attributeState "default", label:'${currentValue}'
        }
        tileAttribute("device.batteryLevel", key: "SLIDER_CONTROL") {
        }
    }
    standardTile("DOCK", "device.status", width: 2, height: 2) {
        state "docked", label: 'Docked', backgroundColor: "#79b821" // No action allowed here
        state "docking", label: 'Docking', backgroundColor: "#ffa81e" // No action allowed here
        state "starting", label: 'UnDocking', backgroundColor: "#ffa81e" // No action allowed here
        state "cleaning", label: 'Not on Dock', backgroundColor: "#ffffff", nextState: "docking"
        state "pausing", label: 'Not on Dock', backgroundColor: "#ffffff", nextState: "docking" // No action allowed here
        state "paused", label: 'Dock', action: "dock", backgroundColor: "#ffffff", nextState: "docking"
        state "resuming", label: 'Not on Dock', backgroundColor: "#ffffff" // No action allowed here
    }
    standardTile("PAUSE", "device.status", width: 2, height: 2) {
        state "docked", label: 'Pause', backgroundColor: "#ffffff" // No action allowed here
        state "docking", label: 'Pause', backgroundColor: "#ffffff" // No action allowed here
        state "starting", label: 'Pause', backgroundColor: "#ffffff" // No action allowed here
        state "cleaning", label: 'Pause', action: "pause", backgroundColor: "#ffffff"
        state "pausing", label: 'Pausing..', backgroundColor: "#79b821" // No action allowed here
        state "paused", label: 'Paused', backgroundColor: "#79b821" // No action allowed here
        state "resuming", label: 'Pause', backgroundColor: "#ffffff" // No action allowed here
    }
    standardTile("RESUME", "device.status", width: 2, height: 2) {
        state "docked", label: 'Resume', backgroundColor: "#ffffff" // No action allowed here
        state "docking", label: 'Resume', backgroundColor: "#ffffff" // No action allowed here
        state "starting", label: 'Resume', backgroundColor: "#ffffff" // No action allowed here
        state "cleaning", label: 'Resume', backgroundColor: "#ffffff" // No action allowed here
        state "pausing", label: 'Resume', backgroundColor: "#79b821" // No action allowed here
        state "paused", label: 'Resume', action: "resume", backgroundColor: "#ffffff"
        state "resuming", label: 'Resuming..', backgroundColor: "#79b821" // No action allowed here
    }
    standardTile("refresh", "device.status", width: 6, height: 2, decoration: "flat") {
        state "default", label:'Refresh', action:"refresh.refresh", icon:"st.secondary.refresh"
    }
    valueTile("job_history", "device.job_count", width: 1, height: 2, decoration: "flat") {
        state "default", label:'Number of Cleaning jobs: ${currentValue}'
    }
    main "CLEAN"
    details(["STATUS",
             "CLEAN", "DOCK", "PAUSE", "RESUME",
             "refresh"])
}
// Switch methods
def on() {
    def status = device.latestValue("status")
    log.debug "On based on state - ${status}"
    if(status == "paused") {
        resume()
    } else {
        start()
    }
}
def off() {
    def status = device.latestValue("status")
    log.debug "Off based on state - ${status}"
    if(status == "paused") {
        dock()
    } else {
        stop()
    }
}
// Actions
def start() {
    sendEvent(name: "status", value: "starting")
    state.RoombaCmd = "start"
    apiGet()
    runIn(30, poll)
}
def stop() {
    sendEvent(name: "status", value: "stopping")
    state.RoombaCmd = "stop"
    apiGet()
    runIn(30, poll)
}
def dock() {
    sendEvent(name: "status", value: "docking")
    state.RoombaCmd = "dock"
    apiGet()
    runIn(30, poll)
}
def pause() {
    sendEvent(name: "status", value: "pausing")
    state.RoombaCmd = "pause"
    apiGet()
    runIn(30, poll)
}
def resume() {
    sendEvent(name: "status", value: "resuming")
    state.RoombaCmd = "resume"
    apiGet()
    runIn(30, poll)
}
// API methods
def parse(description) {
    def msg = parseLanMessage(description)
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
}
def apiGet() {
    def request_query = ""
    def request_host = ""
    def encoded_str = "${roomba_username}:${roomba_password}".bytes.encodeBase64()

    //Handle prefrences
    if("${roomba_host}" == "" || "${roomba_host}" == "null") {
        request_host = "https://irobot.axeda.com"
    } else {
        log.debug "Using Roomba Host: ${roomba_host}"
        request_host = "${roomba_host}"
    }

    //Validation before calling the API
    if(!roomba_username || !roomba_password) {
        def new_status = "Username/Password not set. Configure required before using device."
        sendEvent(name: "headline", value: new_status, displayed: false)
        sendEvent(name: "preferences_set", value: "missing", displayed: false)
        return
    } else if(state.preferences_set != "missing") {
        sendEvent(name: "preferences_set", value: "ready", displayed: false)
    }

    state.AssetID = "ElPaso@irobot!${roomba_username}"
    state.Authorization = "${encoded_str}"

    // Path (No changes required)
    def request_path = "/services/v1/rest/Scripto/execute/AspenApiRequest"
    // Query manipulation
    if( state.RoombaCmd == "getStatus" || state.RoombaCmd == "accumulatedHistorical" || state.RoombaCmd == "missionHistory") {
        request_query = "?blid=${roomba_username}&robotpwd=${roomba_password}&method=${state.RoombaCmd}"
    } else {
        request_query = "?blid=${roomba_username}&robotpwd=${roomba_password}&method=multipleFieldSet&value=%7B%0A%20%20%22remoteCommand%22%20:%20%22${state.RoombaCmd}%22%0A%7D"
    }

    def requestURI = "${request_host}${request_path}${request_query}"
    def httpRequest = [
        method:"GET",
        uri: "${requestURI}",
        headers: [
            'User-Agent': 'aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0',
            Accept: '*/*',
            'Accept-Language': 'en-us',
            'ASSET-ID': state.AssetID,
        ]
    ]
    try {
        httpGet(httpRequest) { resp ->
            resp.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            log.debug "response contentType: ${resp.contentType}"
            log.debug "response data: ${resp.data}"
            parseResponseByCmd(resp, state.RoombaCmd)
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
}
def parseResponseByCmd(resp, command) {
    //Parsing
    def data = resp.data
    if(command == "getStatus") {
        setStatus(data)
    } else if(command == "accumulatedHistorical" ) {
        //readSummaryInfo -- same as getStatus but easier to parse
    } else if(command == "missionHistory") {
        //readMissionHistory -- get results about last 30 jobs -- Out of scope for device-type?
    }
}
def setStatus(data) {
    //TODO: Mine other data here later? add support for "percent completion"?
    def rstatus = data.robot_status
    def robotName = data.robotName
    def mission = data.mission
    def runstats = data.bbrun
    def cschedule = data.cleanSchedule
    def pmaint = data.preventativeMaintenance
    def robot_status = new groovy.json.JsonSlurper().parseText(rstatus)
    def robot_history = new groovy.json.JsonSlurper().parseText(mission)
    def runtime_stats = new groovy.json.JsonSlurper().parseText(runstats)
    def schedule = new groovy.json.JsonSlurper().parseText(cschedule)
    def maintenance = new groovy.json.JsonSlurper().parseText(pmaint)
    log.debug "Robot Status = ${robot_status}"
    log.debug "Robot History = ${robot_history}"
    log.debug "Runtime stats= ${runtime_stats}"
    log.debug "Robot schedule= ${schedule}"
    log.debug "Robot Maintenance= ${maintenance}"
    def current_cycle = robot_status['cycle']
    def current_charge = robot_status['batPct']
    def current_phase = robot_status['phase']
    def num_mins_running = robot_status['mssnM']
    def flags = robot_status['flags']  // Flag 1/5 == almost-full bin? not sure what this means?
    def readyCode = robot_status['notReady']  // 16 - bin is full, other 'helpful' errors to display?
    def num_cleaning_jobs = robot_history['nMssn']
    def num_dirt_detected = runtime_stats['nScrubs']
    def total_job_time = runtime_stats['hr']
    

    def new_status = get_robot_status(current_phase, current_cycle, current_charge, readyCode)
    def roomba_value = get_robot_enum(current_phase, readyCode)
    log.debug("Robot updates -- ${roomba_value} + ${new_status}")
    //Set the state object
    if(roomba_value == "cleaning") {
        state.switch = "on"
    } else {
        state.switch = "off"
    }

    //send events, display final event
    sendEvent(name: "robotName", value: robotName, displayed: false)
    sendEvent(name: "jobCount", value: num_cleaning_jobs, displayed: false)
    sendEvent(name: "batteryLevel", value: current_charge, displayed: false)
    sendEvent(name: "headline", value: new_status, displayed: false)
    sendEvent(name: "status", value: roomba_value)
}
def get_robot_enum(current_phase, readyCode) {
    if(readyCode == 16) {
        return "bin-full"
    } else if(current_phase == "charge") {
        return "docked"
    } else if(current_phase == "hmUsrDock") {
        return "docking"
    } else if(current_phase == "pause" || current_phase == "stop") {
        return "paused"
    } else if(current_phase == "run") {
        return "cleaning"
    } else {
        log.error "Unknown phase - Raw 'robot_status': ${status}. Add to 'get_robot_enum'"
        return "unknown"
    }
}
def get_robot_status(current_phase, current_cycle, current_charge, readyCode) {
    log.debug "Enter get_robot_status"

    def robotName = device.latestValue("robotName")

    if(readyCode == 16) {
      return "${robotName} bin is full. Empty bin to continue."
    } else if(current_phase == "charge") {
        if (current_charge == 100) {
            return "${robotName} is Docked/Fully Charged"
        } else {
            return "${robotName} is Docked/Charging"
        }
    } else if(current_phase == "hmUsrDock") {
        return "${robotName} is returning home"
    } else if(current_phase == "run") {
        return "${robotName} is cleaning (${current_cycle} cycle)"
    } else if(current_phase == "pause" || current_phase == "stop") {
        return "Paused - 'Dock' or 'Resume'?"
    }

    log.error "Unknown phase - ${current_phase}."
    return "Error - refresh to continue. Code changes required if problem persists."
}
