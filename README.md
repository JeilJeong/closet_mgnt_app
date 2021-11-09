# LooKAT: the shopping-aid application

## Description
https://youtu.be/BNxVOXCbuxY

## Overview
1. This project is a shopping assistance application that can be used by blind(especially low vision) and non-blind users.  
2. This project was created for the purpose of submitting works for graduation.

## Feature
 1. It's an on-off connection application to quickly access online information when shopping.
 2. Recognize the outline of clothing tags by using OpenCV library.
 3. [For low vision] This used MLkit's OCR function to recognize the product number in the clothing tag.
	- [For low vision] Send the recognized product number to the server.
	- [For low vision] Use selenium and bs4 to search for product information on the server and respond results to the client.
	- [For low vision] It analyzes the results received from the server and delivers the information to the user through voice output.
	- [For low vision] Until now, only Nike products can be searched.
4. [For non-blind] This used MLkit's OCR function to recognize the product number in the clothing tag.
	 - [For non-blind] Search recognized product information using the inner browser (chrome custom tab)
	 - [For non-blind] It uses Naver Shopping web page that can compare prices as search engines.

## Environment

 - IDE: Android Studio Arctic Fox 2020.03.01 Patch 2
 - Language: Based on Java(JDK 11.0.10)
 - Plugins(dependencies)
</br>		 - MLkit-text-recognition: 16.1.2
</br>		 - Firebase-ml-vision: 24.1.0
</br>		 - Chrome custom browser: 1.3.0
</br>		 - Google TTS(Text to Speech): firebase-bom: 28.4.1
</br>		 - OpenCV: 4.5.3
 - Version
</br>		 - compileSdk 30
</br>		 - minSdkVersion: 21
</br>		 - targetSdkVersion: 30
 - Test device version
</br>		 - 11(API Level 30)

## Usage
1. First of all, you should clone this project.
2. Second, please split the directory named 'lookat_python_server', because this folder is server program written by python and does not related with build steps in android application.
3. Third, open the 'lookat_python_server' directory by the Visual Studio Code, and then run the project on 'connect.py'.
4. Fourth, When you do the above task number 3 successfully, you can watch your internal private IP address on your terminal. Copy the IP address and then paste it to 'SERVER_IP' in 'Constant.java' in 'com.graduate.lookat2.commu' pakage in android project.
5. Fifth, please check the port number, if you already use the '8080 port', you must change the port number that is any number you want. In this case, you must change the port number both in 'Constant.java' file that already said and in 'conn_constant.py' in 'lookat_python_servers' directory. If you doesn't, just go to next steps below.
6. Finally, at this time, maybe your server is running, and the remain task is build your android project(please check the above 'Environment' when you faced with the problem).
 
## License
This proejct is primarily distributed under the terms of both the MIT license and the Apache License (Version 2.0).

See LICENSE-APACHE and LICENSE-MIT for details.
