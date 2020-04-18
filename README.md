# Logisim ITA
Logisim is a digital circuit simulator, [originally available here](https://www.cburch.com/logisim/).<br><br>
This is an italian fork based on the original Logisim version.<br>
<b>[DOWNLOAD AND CHANGELOG](https://logisim.altervista.org)</b><br>
<b>[CONTACT US](https://logisim.altervista.org/contacts.html)</b><br>
<b>[PLUGINS](https://logisim.altervista.org/plugins.html)</b><br>
<b>[USER TUTORIALS](https://logisim.altervista.org/userstutorial.php)</b><br>
<b>[DEVS TUTORIALS](https://logisim.altervista.org/developerstutorial.php)</b><br>
## Why you should use Logisim ITA
* No retro-compatibility problems with old .circ files
* A lot of new components and small changes
* Bug fixes and optimizations
* Constantly supported and listening to all your suggestions/reports
## Changelog
* Core Stuff:
	* Italian, Simplified Chinese and partial french translation!
	* Autoupdates!
	* Compiled with Java 14
	* Complete new movement system with mouse dragging
	* Auto center and Auto zoom when opening new files/circuits or with ctrl-0, double mouse wheel click and with the round button you'll see on the lower-right corner
	* Almost infinite canvas, arrows will show you in what direction is the circuit if it's outside of your view
	* Zoom in and out using mouse wheel where you're pointing at
	* Double click with Edit Tool to label components
	* Load libraries from folder at startup (Preferences / Fork)
	* Selectable painting refresh rate, default 60Hz (Legacy was 20Hz and sometimes lagging)
	* Anti Aliasing, Look and Feel and Fill Component's Background preference
	* Clock custom frequency
	* Press ESC or DEL to cancel "Add Tool" action, F1 opens Library Reference
	* Increased output limit in Analyze Circuit to 32
	* Analyze Circuit should calculate table/expression with any kind of component
	* Added "Clear Log" button
	* Added missing tooltips
	* Changed some default values
* New components:
	* Wiring->Programmable Generator
	* TTL gates (7400, 7402, 7404, 7408, 7432, 7447, 7485, 7486, 74165, 74283, 747266)
	* I/O->Digital Oscilloscope
	* I/O->Switch
	* I/O->Dip Switch
	* I/O->Slider
	* I/O->RGB LED
	* I/O->Buzzer
	* Memory->PLA ROM
	* Plexers->7-Segment Display Decoder
* Component changes:
	* New FF layout
	* The Shift Register will show you its internal bits even when set to serial load
	* Right click on Pin, Edit Contents and set its value typing the decimal number
	* Added Sel pin in Register
	* Added Preset pin in Register and Counter
	* TTY and Keyboard components can use 16-bit values (UTF-16)
* New Attributes:
	* Controlled Buffer / Inverter: Negate Control Input attribute
	* Added Sel Active Level attribute in components with Sel pin
	* Priority Plexer "No Input" attribute
	* Joystick "Facing" attribute
	* Bit Selector "Select location" attribute
	* Added Label Color attribute for each component with Label attribute and Text Tool
	* Added Label for RAM, ROM and PLA ROM
	* FF D, S-R: "Latch" option in Trigger attribute
	* New Counter behavior (different pin logic)
* Fixed bugs:
	* Big fixes to Log menu and Log output file
	* Big fixes for Text Tool
	* Fixed empty template bug introduced in Logisim 2.7.0
	* Fixed input positions in wide gates with 4 inputs
	* Fixed opening new file in new window with old window not used
	* Fixed bugged 32b multiplier
	* Some fix from original early version 2.7.2.255
## Bugs
* All the original Logisim's bugs we haven't fixed yet:
	* Some random blue/red line caused by bad values refresh
	* Some problem with high frequencies
	* String attribute not calling attributechanged method while writing its value
* Programmable Generator: trying to edit its values by clicking on "(click to edit)" is a bit buggy, use "Edit Contents" instead in menu by clicking with right mouse button
## Features we want to add
* Solve dirty points when rotating
* Add a new type of library
* Draw also in circuits
* Suggest us everything at logisimit@gmail.com
## Retro-compatibility
Due to a bug in the original Logisim, wide gates with 4 inputs had a bad pin positioning.
I fixed this problem but if you open an old file containing gates with those attributes, its inputs will be disconnected and a warning message will appear
## EXE and JAR downloads + microprocessor project
All in our website [here](https://logisim.altervista.org)
## How to compile & use
Watch our tutorials on "TUTORIAL" section of our [website](https://logisim.altervista.org/developerstutorial.php)
## Translation
New strings are translated really bad (Google Translate) because i just know Italian and English.<br>You can help me translating other languages or adding a new one, if you want so, contact me at logisimit@gmail.com
