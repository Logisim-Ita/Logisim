# Logisim ITA
Logisim is a digital circuit simulator, [originally available here](http://www.cburch.com/logisim/).<br><br>
This is an italian fork based on the original Logisim version.<br>
<b>[DOWNLOAD AND CHANGELOG](http://logisim.altervista.org)</b><br>
<b>[CONTACT US](http://logisim.altervista.org/contacts.html)</b><br>
<b>[PLUGINS](http://logisim.altervista.org/plugins.html)</b><br>
<b>[USER TUTORIALS](http://logisim.altervista.org/userstutorial.php)</b><br>
<b>[DEVS TUTORIALS](http://logisim.altervista.org/developerstutorial.php)</b><br>
## Why you should use Logisim ITA
* No retro-compatibility problems with old .circ files
* A lot of new components and small changes
* Bug fixes and optimizations
* Constantly supported and listening to all your suggestions/reports
## Changelog
* Italian translation!
* Possibility to use french translations already done (not very well)
* Autoupdates!
* Compiled with Java 10
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
* Complete new movement system with mouse dragging
* Auto center and Auto zoom when opening new files/circuits, you can call this function with ctrl-0, double mouse wheel click or with the round button you'll see on the lower-right corner
* Almost infinite canvas, arrows will show you in what direction is the circuit if it's outside of your view
* Zoom in and out using mouse wheel where you're pointing at
* Selectable painting refresh rate, default 60Hz (Legacy was 20Hz and sometimes lagging)
* New FF layout and attributes
* FF D, S-R: Latch option in trigger list
* New Counter behavior
* Controlled Buffer / Inverter: Negate Control Input attribute
* The Shift Register will show you its internal bits even when set to serial load
* Right click on Pin, Edit Contents and set its value typing the decimal number
* Added Sel pin in Register component
* Added Preset pin in Register and Counter components
* Added Sel Active Level attribute in components with Sel pin
* Press ESC or DEL to cancel "Add Tool" action, F1 opens Library Reference
* TTY and Keyboard components can use 16-bit values (UTF-16)
* Clock custom frequency
* Priority Plexer "No Input" attribute
* Joystick facing attribute
* Sel location attribute for Bit Finder component
* Added missing tooltips
* Big fixes to Log menu and Log output file, added buttons to clear Log Table
* Added Label Color attribute for each component with Label attribute and Text Tool
* Added Label for RAM, ROM and PLA ROM
* Big fixes for Text Tool
* Increased output limit in Analyze Circuit to 32
* Analyze Circuit should calculate table/expression with any kind of component
* Fixed empty template bug introduced in Logisim 2.7.0
* Fixed input positions in wide gates with 4 inputs
* Fixed opening new file in new window with old window not used
* Fixed bugged 32b multiplier
* Changed some default value
* Anti Aliasing, Look and Feel and Fill Component's Background preference
* Graphical changes
* Includes some fix from original early version 2.7.2.255
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
All in our website [here](http://logisim.altervista.org)
## How to compile & use
Watch our tutorials on "TUTORIAL" section of our [website](http://logisim.altervista.org/developerstutorial.php)
## Translation
New strings are translated really bad (Google Translate) because i just know Italian and English.<br>You can help me translating other languages or adding a new one, if you want so, contact me at logisimit@gmail.com
