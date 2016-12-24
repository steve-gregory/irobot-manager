# iRobot Manager (Unofficial)

### iRobot Manager App
This is the "***unofficial***" SmartThings user created Device types for iRobot (Currently, Roomba).
The Device types work together to provide integration to the SmartThings ecosystem using iRobot's REST API.

## Author
* @steve-gregory

## Contributors
* @Elfege

## Links
#### [GitHub Project Issues Link](https://github.com/steve-gregory/irobot-manager/issues)

#### [SmartThings IDE GitHub Integration Instructions](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html)

## Things to Know

 * This requires knowledge of the iRobot Login info (blid/password). To gain access to these credentials, [follow the README on this repo](https://github.com/koalazak/dorita980)
 * iRobot Login info is stored by the applications preferences to gain access to the Cloud API.
 * Devices that use this Virtual switch will not refresh without clicking the 'Refresh' button (for now, polling not enabled -- yet!)

## Advantages
 * Know the current status of your Roomba
 * No need to enter secret info inside the application (Stored in preferences)

## Issues and Troubleshooting
 * need to use 3rd Party Polling apps for device updates, for now
 * credentials are being stored in-app. There might be a better way?
