# CyUSBBootHost - Cypress USB Bootloader Host
![alt text](https://github.com/tejv/CyUSBBootHost/blob/master/gui_image.PNG)

--------------------------------------------------------------------------------
Quick Start Guide Cypress USB Bootloader Host
--------------------------------------------------------------------------------
Author: Tejender Sheoran

Email:  tejendersheoran@gmail.com, teju@cypress.com

Copyright (C) <2017>  <Tejender Sheoran>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--------------------------------------------------------------------------------
-------------------------------------------------------------------------------
Overview
--------------------------------------------------------------------------------
1. This tool boots .cyacd files.
2. Tool works on both Windows and Linux. It requires libusb driver on windows.

--------------------------------------------------------------------------------
Setup
-------------------------------------------------------------------------------
1. Get the latest version from output folder.
2. Install JRE8(Java Runtime environment). On ubuntu install javafx as well as it is not part of openjdk.
3. Install Driver 
  ### Windows
    . Install libusb win32 driver. By default CY device might bind to Windows usbhid driver. 
      You need to manually reinstall libusb driver.
    . Note if device is plugged to different port. You need to install libusb win32 driver
      for that port because by default device bind to cypress driver.
    . Easy way to do this is to use zadig tool from http://zadig.akeo.ie/
    . Download the tool and run it.
    . In Options menu click on List all devices. Then select the cy device
      and replace driver(not Install WICD driver) to libusbk.
  ### Linux
    . Make sure user has read/write permisssion to the usb device. 
    . If not, to fix this create a file or append if already exists 
      /etc/udev/rules.d/99-userusbdevices.rules
      
        with below line and replug the device.
      SUBSYSTEM=="usb",ATTR{idVendor}=="04b4",ATTR{idProduct}=="xxxx",MODE="0660",GROUP="plugdev"
      
        where 04b4 is the vendor id, xxxx is product id of usb device.
        
4. Run CyBootloaderHost-version.jar.
5. Open .cyacd file.
6. Click download button to start bootloading.

--------------------------------------------------------------------------------------------------
Steps to compile source
--------------------------------------------------------------------------------------------------
For JDk Windows is simple. Javafx is part of JDk
For linux "get synaptic packet manager
sudo apt-get install synaptic
Then in synaptic search open jdk
Select openjdk-x-jdk and mark install then apply. " or "sudo apt-get install openjdk-8-jdk"
JAVAFX is not part of openjdk in linux. So install javafx
sudo apt-get install openjfx

1. Get prebundled eclipse from
http://efxclipse.bestsolution.at/
Download compressed file and unzip it. Eclipse will run out of the box for windows/ubuntu.
2. Get scenebuilder from Gloun
http://gluonhq.com/products/scene-builder/
Get .deb for ubuntu and Close synaptic, install using software center. 
Get .exe for windows
Add controlsfx and jfoenix to scenebuilder. (small jar/fxml manager icon after library textbox).
Make sure to use same version as used by build.gradle. Once build is done these get downloaded in build directory.
Also make sure java version of scenebuilder is same as original project.
3. Install gradle on eclipse
   In eclipse help-> Install new software
   In work with select eclipse install. Then wait for list of available software to load(pending)
   Then select Buildship:Eclipse Plug-ins for Gradle
   Click next, next till finish.
4. Import existing git gradle project in eclipse.
    Make sure if project looks for gradle.properties file create it from gradle.properties.example
   In file menu click import
   Then gradle project. Then follow the steps.
   Close the welcome screen.
   Add gradle tab in lower window. Go to Window->Show View-> Others->Gradle. Add both options.
   For gradle build right click build.gradle -> Gradle -> refresh gradle project.
   Then go to gradle tasks tab.
5. Change permission to executable for jar file.
