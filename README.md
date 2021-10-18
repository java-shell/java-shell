# JSHDev
<p>NEEDS REVISION</p>
<p>JSH was first designed as a way to fix some of the bad practices I noticed in most modern shells and OS designs, such as horrible error reporting.  In recent days, it has turned into a project of parallelism across platforms through the use of abstract serialization, which will be documented further down in the README. The main driving components of JSH are; <a href="#ModuleManagement">ModuleManagement</a>, <a href="#LogManager">LogManager</a>, <a href="#ConnectionManager">ConnectionManager</a>, <a href="#JSHProcesses">JSHProcesses</a>, <a href="#EventManager">EventManager</a>, <a href="#Terminal">Terminal</a>.  All of these classes will be outlined later in this README.  Note that JSH was built to be a modular system, designed to operate as either a low level system or a high level system, low level being console based, and high level being GUI based.  It can be run on all platforms that support Java, and either as superuser or as a standard user.  It needs no extra libraries, and is higly configurable.</p>
<h2>Abstract Serialization</h2>
<p>The concept of abstract serialization is simple.  All objects that should be "run" within JSH must extend the JProcess class, which in itself is a serializable class.  This is done to ensure that all processes can be broken down into a simple stream of bytes, and transported to other JSH systems to be run later.  The main use of this is in <a href="#ConnectionManager">ConnectionManager</a>.</p>
<h2>ModuleManagement</h2>
<p>Modules in JSH are extensions of JSH that can be loaded during JSH launch.  These modules are essentially independent of JSH, however they can access internals of JSH, as well as call methods from other modules.  In this way, modules can have dependencies on other modules.  A module is loaded from the "modules" directory within JSH's main directory, and a list of modules to load is kept in the same directory under the file name "module.lst."  All modules listed within this file are loaded, enabled, and executed.</br>
The ModuleManagement class is responsible for loading these modules, and managing/monitoring them through a series of methods.  In order to load a module after initialization, the method "hotLoad()" must be called. The "hotLoad()" method should be used sparingly however, as it is buggy, and if a module is incorrectly implemented it could crash the entirity of JSH *this is a pretty large bug I know, but its a quick fix and I have other parts to fix first before I worry about fixing this* </p>
<h2>LogManager</h2>
<p>In order to create a more secure environment, JSH utilizes Logger objects that act as an early line of defense against false messages, and attempt to make understanding output easier.  All JProcesses have unique Logger objects that they use to print to output, these Logger objects are also used to assist in output redirection of any process, as all Logger objects can print to any OutputStream ambiguously.</br>
The LogManager class is responsible for managing these Logger objects, as the name suggests.  All Logger objects which are created notify the LogManager class of their creation, and on creation are stored in a structure within LogManager to maintain and monitor them.  LogManager itself is not a JProcess, and as such does not contain a Logger object, it instead writes directly to the default OutputStream.  LogManager and Launch are the only classes with direct access to the default OutputStream.</p>
<h2>ConnectionManager</h2>
<p>JSH contains a system which implements a sort of clustering capability based on <a href="#Abstract-Serialization">Abstract Serialization</a>.  In this, a JProcess can be serialized, and sent to another JSH instance which will de-serialize and execute the JProcess object ambiguously.  This means that any form of JProcess can be sent, be it a Command, or a Module, or an independently coded form of JProcess.  ConnectionManager is *not* fully implemented, however when it is completed its goal will be to interface with other JSH instances and determine where to dispatch processes in such a manner that no instance will be overloaded, and all instances will be utilized equally in a pseudo socialist manner.  Each instance of JSH contains an independednt ConnectionManager, meaning that no instance is truly a host, and no instance is truly a client.  Instead all instances of JSH act as transceivers, relatively independent of all other instances surrounding them.  As such, instances can be added or removed without affecting other instances as long as all JProcesses that originated from that instance are completed and the proper information is returned to the sender.  A second feature of this is, JSH being a crossplatform program, can send or receive processes from any system that can run Java.  So a JProcess that is created on a Windows machine can be cleanly and efficiently run on a Linux or Mac machine with absolutely no interpretation.</p>
<h2>JSHProcesses</h2>
<p>Throughout this README, the class "JProcess" has been mentioned several times.  As mentioned in <a href="#Abstract-Serialization">Abstract Serialization</a>, all processes to be run in JSH must extend the JProcess class.  Upon a JProcess objects instantiation, it registers itself with JSHProcesses *which more correctly should be named JProcessManager, again my apologies I was in 8th grade* whose responsibility is managing and monitoring all JProcess objects currently active, and inactive, within a JSH instance. </p>
<h2>EventManager</h2>
<p>In the early days of JSH EventManager was created as an interface to the "evdev" linux sub-system that further interfaced with user peripherals such as the mouse and keyboard.  An event would be fired upon any keypress, mouse move, mouse click, etc..., and sent to any Listener objects registered within EventManager.  Now, EventManager is used more for internal event management, and most commonly handles ModuleEvents, which are fired in order to trigger a Module wihout needing to depend on that module.  This allows processes within JSH to "softdepend" on modules, in other words a JProcess could support a module, such as an SSH wrapper module, however if that module is not installed, the JProcess will still load and run successfully.</p>
<h2>Terminal</h2>
<p>As the name suggests, the Terminal class implements a text-based Terminal that takes user input, and provides output from JSH itself.  The Terminal class is used to execute Commands, which are JProcess objects, that similarly to modules are loaded upon launch.  Commands are stored in the "bin" directory of JSH's main directory. All Command files stored within that directory are loaded.  Several important commands that provide low-level access to files, and the JSH system itself are built into the JSH system, and loaded automatically upon Launch.  These can be disabled in the "system" configuration file which is located in the "conf" directory within JSH's main directory.</p>
<h2>Testing</h2>
<p>In order to properly test JSH, simply execute the jar file. You will be prompted for login information, and then the system will execute a Terminal, and wait for input. Output is labeled with the name of the JProcess that sent the output, and the Logger object's ID corresponding to that JProcess.  If you are on Windows, or are a non-root user, JSH will create a directory within your user home directory entitled "JSH" that will act as the home directory for JSH.  If you are on a *nix based system, JSH wil use / as the home directory for JSH.  Modules should be placed under the "modules" directory within JSH's home directory, within another directory that matches the modules reference in "module.lst."  In order to configure a module for loading, its directory name should be placed in a newline in "module.lst," with capitalization in mind.  Commands are located in the "bin" directory of JSH's home directory, and should be simply placed there, no further configuration is necessary to load them.  If a Command or Module fails to load, a corresponding error message will be printed to the screen, and a log located within directory "var/log/JSH" also under the JSH home directory.  All log files here are labeled with the date, and time of that instance of JSH's execution.  In order to test the ConnectionManager, two independent instances of JSH must be run.  A helper command, called "clusterman" is used to interact with the ConnectionManager.  Currently, clusterman has two arguments, ping <ip>, and add <ip>.</br>
  Common launch errors include</br>
-IOExceptions relating to Configuration file loading:</br>
--Fixing this is often simple, be sure that the JSH home directory is marked as Readable, and Writable by whatever user is running JSH</br>
-Module/Command loading errors</br>
--These errors are often harder to fix as they are related to the Command/Module being loaded.  However, an error will still contain a full trace of what went wrong, with correct classnames and line numbers</p>
