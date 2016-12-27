/**
 *  Copyright 2016 steve-gregory
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "iRobot Manager (Connect)",
    namespace: "steve-gregory",
    author: "Steve Gregory",
    description: "Integrate your iRobot with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://lh6.googleusercontent.com/EcO2sRmIxvEnp4kn_ZC2idm-jCOmQCLQmpkS0I2s2X5darJKUtA_YIhRxwVlQT_7QAwqyuYV=w1920-h988",
    iconX2Url: "https://lh6.googleusercontent.com/EcO2sRmIxvEnp4kn_ZC2idm-jCOmQCLQmpkS0I2s2X5darJKUtA_YIhRxwVlQT_7QAwqyuYV=w1920-h988",
    iconX3Url: "https://lh6.googleusercontent.com/MN7t7IzsKV03dhN-Df5gFY_qxI89r69sVSaH4lDLSel8PXix8YX2m5m5riYGTrNLEfLf7rx0=w1920-h988",
    singleInstance: true
)

preferences {
    page(name: "initializeRobots", title: "iRobot")
    page(name: "selectRobot", title: "iRobot")
}

def initializeRobots() {
    def showUninstall = username != null && password != null
    return dynamicPage(name: "initializeRobots", title: "Connect your iRobot", nextPage:"selectRobot", uninstall:showUninstall) {
        section("What is your Robots IP address _or_ username/password:") {
            input "ipaddress", "text", title: "IP Address", required:false, autoCorrect:false
            input "username", "text", title: "Username/blid", autoCorrect:false
            input "password", "password", title: "password", autoCorrect:false
        }
        section("To use iRobot, SmartThings encrypts and securely stores your iRobot credentials.") {}
    }
}

private ipAddressToPasswd() {
    def jsonStr = '{"do":"get","args":["passwd"],"id":1}'
    def loginParams = [
        uri: "https://${ipaddress}",
        path: "/umi",
        headers: [
            'User-Agent': 'aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0',
            Accept: "*/*",
            contentType: "application/json",
            "Content-Encoding": "identity",
            Connection: "close",
            Host: "${ipaddress}",
            'Accept-Language': 'en-us',
            'ASSET-ID': state.AssetID,
        ],
        body: "${jsonStr}"
    ]

    def result = [success:false, data: [], reason: ""]

    try {
      httpPost(loginParams) { resp ->
          if (resp.status == 200) {
              log.debug "response contentType: ${resp.contentType}"
              log.debug "response data: ${resp.data}"
              log.debug "response Headers:" + resp.headers.collect { "${it.name}:${it.value}" }
              result.success = true
              result.data = resp.data
          } else {
              // ERROR: any more information we can give?
              result.reason = "Bad login"
          }
      }
    } catch (groovyx.net.http.HttpResponseException e) {
        result.reason = "Error on login"
    }
    return result
}

private ipAddressToUsername() {
    def encoded_authorization = "user:${password}".bytes.encodeBase64()
    def jsonStr = '{"do":"get","args":["sys"],"id":2}'
    def loginParams = [
        uri: "https://${ipaddress}",
        path: "/umi",
        headers: [
            'User-Agent': 'aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0',
            Authorization: "${encoded_authorization}",
            Accept: '*/*',
            contentType: "application/json",
            "Content-Encoding": "identity",
            Connection: "close",
            Host: "${ipaddress}",
            'Accept-Language': 'en-us',
            'ASSET-ID': state.AssetID,
        ],
        body: "${jsonStr}"
    ]

    def result = [success:false, data: [], reason: ""]

    try {
      httpPost(loginParams) { resp ->
          if (resp.status == 200) {
              log.debug "response contentType: ${resp.contentType}"
              log.debug "response data: ${resp.data}"
              /*Example:
              {"ok":{"umi":2,"pid":2,"blid":[43,6,75,31,32,127,12,132],"sw":"v1.2.9","cfg":0,"boot":4042,"main":4313,"wifi":517,"nav":"01.08.04","ui":2996,"audio":32,"bat":"lith"},"id":2}
              */
              log.debug "response Headers:" + resp.headers.collect { "${it.name}:${it.value}" }
              result.success = true
              result.data = resp.data
              log.debug "Username collected: ${username}"
              username = resp.data.blid.collect( it.toHexString()).join("")
          } else {
              // ERROR: any more information we can give?
              result.reason = "Bad login"
          }
      }
    } catch (groovyx.net.http.HttpResponseException e) {
        result.reason = "Error on login"
    }
    return result
}
private doLogin() {




    // Path (No changes required)
    def request_host = "https://irobot.axeda.com"
    def request_path = "/services/v1/rest/Scripto/execute/AspenApiRequest"
    def request_query = "?blid=${username}&robotpwd=${password}&method=getStatus"
    def encoded_str = "${username}:${password}".bytes.encodeBase64()
    def AssetID = "ElPaso@irobot!${username}"
    def Authorization = "${encoded_str}"
    // Query manipulation

    def requestURI = "${request_host}${request_path}${request_query}"
    def httpRequest = [
        method:"GET",
        uri: "${requestURI}",
        headers: [
            'User-Agent': 'aspen%20production/2618 CFNetwork/758.3.15 Darwin/15.4.0',
            Accept: '*/*',
            Authorization: "${Authorization}",
            'Accept-Language': 'en-us',
            'ASSET-ID': "${AssetID}",
        ]
    ]
    def result = [success:false, data: [], reason: ""]
    try {
        httpGet(httpRequest) { resp ->
            log.debug "Login response Headers:" + resp.headers.collect { "${it.name}:${it.value}" }
            log.debug "Login response contentType: ${resp.contentType}"
            log.debug "Login response data: ${resp.data}"
            def robotName = resp.data.robotName
            log.debug "response data.robotName: ${robotName}"
            result.data = resp.data
            result.success = true
            log.info "Login response result success."
        }
    } catch (e) {
        log.error "something went wrong: $e"
    }
    return result
}



def selectRobot() {
    if(!username || !password) {
     /*Show a picture of (Place the robot on the home base and press the HOME button for about 2 seconds until a series of tones is played and the WIFI light flashes)*/
        password = ipAddressToPasswd()
        username = ipAddressToUsername()
    }
    def loginResult = doLogin()
    
    if(loginResult.success) {
        log.info "Login response data ${loginResult.data}"
        def robotName = loginResult.data.robotName
        def options = robotDiscovered(robotName)

        return dynamicPage(name: "selectRobot", title: "iRobot", install:true, uninstall:true) {
            section("Select which iRobot to connect") {
                input(name: "selectedRobots", type: "enum", required:true, options:options)
            }
        }
    } else {
        log.error "login result false"
        return dynamicPage(name: "selectRobot", title: "iRobot", install:false, uninstall:true, nextPage:"") {
            section("") {
                paragraph "If IP address ONLY is used: Place the robot on the home base, then press the HOME button for about 2 seconds (until a series of tones is played). Then attempt to login again"
                paragraph "If username/password ONLY is used: Verify the accuracy of your credentials and attempt to login again."
            }
       }
    }
}

def robotDiscovered(robotName) {
    log.info "Enter robotDiscovered(${robotName})"
    def devices = [["name" : "${robotName}", "dni" : "iRobot-Roomba-${robotName}"],]
    return devices
}


def installed() {
    log.info "Enter Installed"
    initialize()
}

def updated() {
    log.info "Enter Updated"
    unsubscribe()
    initialize()
}

def initialize() {

    if (selectRobot) {
        addDevice()
    }

}

//CHILD DEVICE METHODS
def addDevice() {
  def devices = getRobotDevices()
  log.trace "Adding childs $devices - $selectedRobots"
    selectedRobots.each { dni ->
            def d = getChildDevice(dni)
            if(!d) {
                    def newCar = devices.find { (it.dni) == dni }
                    d = addChildDevice(app.namespace, "iRobot Roomba", dni, null, [name:"Roomba", label:"iRobot"])
                    log.trace "created ${d.name} with id $dni"
            } else {
                    log.trace "found ${d.name} with id $dni already exists"
            }
    }
}

def getRobotDevices() {

    def result = doLogin()
    if(result.success) {
        def devices = robotDiscovered(result.data.robotName)
    }
    return devices
}
