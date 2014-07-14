/**
 *  Foscam
 *
 *  Author: danny@smartthings.com
 *  Author: brian@bevey.org
 *  Date: 5/2/14
 *
 *  Modified example Foscam device type to support dynamic input of credentials
 *  and enable / disable motion alarm to easily integrate into homemade
 *  security systems (when away, mark "alarmStatus" as "on", when present, mark
 *  "alarmStatus" as "off".  For use with email or FTP image uploading built
 *  into Foscam cameras.
 *
 *  Capability: Image Capture, Polling
 *  Custom Attributes: setStatus, alarmStatus
 *  Custom Commands: alarmOn, alarmOff, toggleAlarm, left, right, up, down,
 *                   pause, set, preset, preset1, preset2, preset3
 */

metadata {
  definition (name: "Foscam-Remote", namespace: "imbrianj", author: "brian@bevey.org") {
    capability "Image Capture"
    capability "Polling"

    attribute "setStatus",   "string"
    attribute "alarmStatus", "string"

    command "alarmOn"
    command "alarmOff"
    command "toggleAlarm"
    command "left"
    command "right"
    command "up"
    command "down"
    command "pause"
    command "set"
    command "preset"
    command "preset1"
    command "preset2"
    command "preset3"
  }

  preferences {
    input("username", "text",     title: "Username",   description: "Your Foscam username")
    input("password", "password", title: "Password",   description: "Your Foscam password")
    input("ip",       "text",     title: "IP address", description: "The IP address of your Foscam")
    input("port",     "text",     title: "Port",       description: "The port of your Foscam")
  }

  tiles {
    carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

    standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "default", label: "", action: "Image Capture.take", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
    }

    standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
      state "take", label: "", action: "Image Capture.take", icon: "st.secondary.take", nextState:"taking"
    }

    standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "take", label: "up", action: "up", icon: ""
    }

    standardTile("alarmStatus", "device.alarmStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "off", label: "off", action: "toggleAlarm", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
      state "on", label: "on", action: "toggleAlarm", icon: "st.camera.dropcam-centered",  backgroundColor: "#53A7C0"
    }

    standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "take", label: "left", action: "left", icon: ""
    }

    standardTile("pause", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "pause", label: "pause", action: "pause", icon: ""
    }

    standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "take", label: "right", action: "right", icon: ""
    }

    standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "pause", label: "", action: "pause", icon: ""
    }

    standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "down", label: "down", action: "down", icon: ""
    }

    standardTile("set", "device.setStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "set", label: "set", action: "set", icon: "",  backgroundColor: "#FFFFFF"
      state "setting", label: "set mode", action: "set", icon: "", backgroundColor: "#53A7C0"
    }

    standardTile("preset1", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset1", label: "preset 1", action: "preset1", icon: ""
    }

    standardTile("preset2", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset2", label: "preset 2", action: "preset2", icon: ""
    }

    standardTile("preset3", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "preset3", label: "preset 3", action: "preset3", icon: ""
    }

    standardTile("refresh", "device.alarmStatus", inactiveLabel: false, decoration: "flat") {
      state "default", action:"polling.poll", icon:"st.secondary.refresh"
    }

    main "alarmStatus"

    details(["cameraDetails", "take", "up", "alarmStatus", "left", "pause", "right", "blank", "down", "set", "preset1", "preset2", "preset3", "refresh"])
  }
}

private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  "image" + "_$pictureUuid" + ".jpg"
}

def take() {
  log.debug("Take a photo")

  api("snapshot", "") {
    log.debug("Image captured")

    if(it.headers.'Content-Type'.contains("image/jpeg")) {
      if(it.data) {
        storeImage(getPictureName(), it.data)
      }
    }
  }
}

def toggleAlarm() {
  if(device.currentValue("alarmStatus") == "on") {
    alarmOff()
  }

  else {
    alarmOn()
  }
}

def alarmOn() {
  api("set_alarm", "motion_armed=1") {
    log.debug("Alarm changed to: on")
    sendEvent(name: "alarmStatus", value: "on");
  }
}

def alarmOff() {
  api("set_alarm", "motion_armed=0") {
    log.debug("Alarm changed to: off")
    sendEvent(name: "alarmStatus", value: "off");
  }
}

def left() {
  api("decoder_control", "command=6") {
    log.debug("Executing 'left'")
  }
}

def right() {
  api("decoder_control", "command=4") {
    log.debug("Executing 'right'")
  }
}

def up() {
  api("decoder_control", "command=0") {
    log.debug("Executing 'up'")
  }
}

def down() {
  api("decoder_control", "command=2") {
    log.debug("Executing 'down'")
  }
}

def pause() {
  api("decoder_control", "command=1") {}
}

def preset1() {
  preset(1)
}

def preset2() {
  preset(2)
}

def preset3() {
  preset(3)
}

//go to a preset location
def preset(def num) {
  if(num == null) return

  if(device.currentValue("setStatus") == "setting") {
    setPreset(num)
  }

  else {
    log.debug("Go To Preset Location")
    //1 is 31, 2 is 33, 3 is 35
    def cmd = 30 + (num * 2) - 1

    api("decoder_control", "command=${cmd}") {}
  }
}

//set the preset number to the current location
def setPreset(def num) {
  log.debug("Set Preset")
  //1 is 30, 2 is 32, 3 is 34... 8 is 44
  int cmd = 28 + (num * 2)
  sendCmd(cmd)

  log.debug("Exit Set Mode")
  sendEvent(name: "setStatus", value: "set");
}

//toggle the the mode to set the preset
def set() {
  if(device.currentValue("setStatus") == "set") {
    log.debug("Entering Set Mode")
    sendEvent(name: "setStatus", value: "setting");
  }

  else {
    log.debug("Exit Set Mode")
    sendEvent(name: "setStatus", value: "set");
  }
}

def api(method, args = [], success = {}) {
  def methods = [
    "decoder_control": [uri: "http://${ip}:${port}/decoder_control.cgi${login()}&${args}", type: "post"],
    "snapshot":        [uri: "http://${ip}:${port}/snapshot.cgi${login()}&${args}",        type: "post"],
    "set_alarm":       [uri: "http://${ip}:${port}/set_alarm.cgi${login()}&${args}",       type: "post"],
    "reboot":          [uri: "http://${ip}:${port}/reboot.cgi${login()}&${args}",          type: "post"],
    "camera_control":  [uri: "http://${ip}:${port}/camera_control.cgi${login()}&${args}",  type: "post"],
    "get_params":      [uri: "http://${ip}:${port}/get_params.cgi${login()}",              type: "get"],
    "videostream":     [uri: "http://${ip}:${port}/videostream.cgi${login()}",             type: "get"]
  ]

  def request = methods.getAt(method)

  doRequest(request.uri, request.type, success)
}

private doRequest(uri, type, success) {
  log.debug(uri)

  if(type == "post") {
    httpPost(uri , "", success)
  }

  else if(type == "get") {
    httpGet(uri, success)
  }
}

private login() {
  return "?user=${username}&pwd=${password}"
}

def poll() {
  api("get_params", []) {
    it.data.eachLine {
      if(it.startsWith("var alarm_motion_armed=0")) {
        log.info("Polled: Alarm off")
        sendEvent(name: "alarmStatus", value: "off");
      }

      if(it.startsWith("var alarm_motion_armed=1")) {
        log.info("Polled: Alarm on")
        sendEvent(name: "alarmStatus", value: "on");
      }
    }
  }
}
