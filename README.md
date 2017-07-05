# CyUSBBootHost - Cypress USB Bootloader Host
![alt text](https://github.com/tejv/CyUSBBootHost/blob/master/gui_image.png)

--------------------------------------------------------------------------------
Quick Start Guide Cypress USB Bootloader Host
--------------------------------------------------------------------------------
Author: Tejender Sheoran

Email: teju@cypress.com, tejendersheoran@gmail.com

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
2. Install latest JRE(Java Runtime environment).
3. Install Driver 
  ### Windows
    . Install libusb win32 driver. By default CY device might bind to Windows usbhid driver. 
      You need to manually reinstall libusb driver.
    . Note if device is plugged to different port. You need to install libusb win32 driver
      for that port because by default device bind to cypress driver.
    . Easy way to do this is to use zadig tool from http://zadig.akeo.ie/
    . Download the tool and run it.
    . In Options menu click on List all devices. Then select the analyzer device and replace driver(not Install WICD driver) to libusbk.
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
