<!--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# MiNiFi Java Agent Quick Start Guide

[Apache NiFi Team](dev@nifi.apache.org>)

[NiFi Homepage](http://nifi.apache.org)

# Overview

Apache NiFi MiNiFi is an Apache NiFi project, designed to collect data at its source. MiNiFi was developed with the following objectives in mind:
* Small and lightweight footprint
* Central agent management
* Data provenance generation
* NiFi integration for follow-on dataflow management and chain of custody information

# Before You Begin
MiNiFi Java Agent is supported on the following operating systems:
* Red Hat Enterprise Linux / CentOS 6 (64-bit)
* Red Hat Enterprise Linux / CentOS 7 (64-bit)
* Ubuntu Precise (12.04) (64-bit)
* Ubuntu Trusty (14.04) (64-bit)
* Debian 7
* SUSE Linux Enterprise Server (SLES) 11 SP3 (64-bit)

You can download the MiNiFi Java Agent and the MiNiFi Toolkit from the [MiNiFi download page](http://nifi.apache.org/minifi/download.html).

# Installing and Starting MiNiFi
You have several options for installing and starting MiNiFi.

## For Linux and Mac OS X Users
To install MiNiFi:
1. [Download](http://nifi.apache.org/minifi/download.html) MiNiFi.
2. Extract the file to the location from which you want to the application.

You can also install MiNiFi as a service:
1. Navigate to the MiNiFi installation directory.
2. Enter:
```
bin/minifi.sh install
```
**Note:** You can also specify a custom name for your MiNiFi installation, by specifying that name during your install command. For example, to install MiNiFi as a service and named dataflow, enter:

```
bin/minifi.sh install dataflow
```
Once you have downloaded and installed MiNiFi, you need to start MiNiF. You can start NiFi in the foreground, background, or as a service.

To launch MiNiFi in the foreground:

1. From a terminal window, navigate to the MiNiFi installation directory.Enter:

```
bin/minifi.sh run
```
To launch MiNiFi in the background:
1. From a terminal window, navigate to the MiNiFi installation directory.
2. Enter:
```
bin/minifi.sh start
```

To launch MiNiFi as a service:
1. From a terminal window, enter:
```
sudo service minifi start
```
## For Windows Users
For Windows users, navigate to the folder where MiNiFi was installed. Navigate to the /bin subdirectory and double-click the run-minifi.bat file.

This launches MiNiFi and leaves it running in the foreground. To shut down NiFi, select the window that was launched and hold the Ctrl key while pressing C.

#Working with dataflows
When you are working with a MiNiFi dataflow, you should design it, add any additional configuration your environment or use case requires, and then deploy your dataflow. MiNiFi is not designed to accommodate substantial mid-dataflow configuration.

##Setting up Your Dataflow
Before you begin, you should be aware that the following NiFi components are not supported in MiNiFi dataflows:
* Controller services
* Funnels
* Multiple source relationships for a single connection
* Process groups

Additionally, each processor requires a distinct name.

You can use the MiNiFi Toolkit, located in your MiNiFi installation directory, and any NiFi instance to set up the dataflow you want MiNiFi to run:

1. Launch NiFi
2. Create a dataflow.
3. Convert your dataflow into a template.
4. Download your template as an .xml file. For more information on working with templates, see the [Templates](http://docs.hortonworks.com/HDPDocuments/HDF2/HDF-2.0.0/bk_user-guide/content/templates.html) section in the *User Guide*.
5. From the MiNiFi Toolkit, run the following command to turn your .xml file into a .yml file:
```
config.sh transform input_file output_file
```
6. Move your new .yml file to minifi/conf.
7. Rename your .yml file config.yml.
**Note:** You can use one template at a time, per MiNiFi instance.


**Result:** Once you have your config.yml file in the minifi/conf directory, launch that instance of MiNiFi and your dataflow begins automatically.

##Using Processors Not Packaged with MiNiFi
MiNiFi is shipped with the following processors:
* UpdateAttribute
* AttributesToJSON
* Base64EncodeContent
* CompressContent
* ControlRate
* ConvertCharacterSet
* DetectDuplicate
* DistributeLoad
* DuplicateFlowFile
* EncryptContent
* EvaluateJsonPath
* EvaluateRegularExpression
* EvaluateXPath
* EvaluateXQuery
* ExecuteProcess
* ExecuteSQL
* ExecuteStreamCommand
* ExtractText
* FetchDistributedMapCache
* FetchFile
* FetchSFTP
* GenerateFlowFile
* GetFTP
* GetFile
* GetHTTP
* GetJMSQueue
* GetJMSTopic
* GetSFTP
* HandleHttpRequest
* HandleHttpResponse
* HashAttribute
* HashContent
* IdentifyMimeType
* InvokeHTTP
* ListFile
* ListSFTP
* ListenHTTP
* ListenRELP
* ListenSyslog
* ListenTCP
* ListenUDP
* LogAttribute
* MergeContent
* ModifyBytes
* MonitorActivity
* ParseSyslog
* PostHTTP
* PutDistributedMapCache
* PutEmail
* PutFTP
* PutFile
* PutJMS
* PutSFTP
* PutSQL
* PutSyslog
* QueryDatabaseTable
* ReplaceText
* ReplaceTextWithMapping
* RouteOnAttribute
* RouteOnContent
* RouteText
* ScanAttribute
* ScanContent
* SegmentContent
* SplitContent
* SplitJson
* SplitText
* SplitXml
* TailFile
* TransformXml
* UnpackContent
* ValidateXml

If you want to create a dataflow with a processor not shipped with MiNiFi, you can do so.
1. Set up your dataflow as described above.
2. Copy the desired NAR file into the MiNiFi lib directory.
3. Restart your MiNiFi instance.

# Securing your Dataflow
You can secure your MiNiFi dataflow using keystore or trust store SSL protocols, however, this information is not automatically generated. You will need to generate your security configuration information yourself.

To run a MiNiFi dataflow securely, modify the Security Properties section of your config.yml file.
1. Create your dataflow template as discussed above.
2. Move it to minifi.conf and rename config.yml.
3. Manually modify the Security Properties section of config.yml. For example:

```
Security Properties:
keystore:  
keystore type:
keystore password:
key password:
truststore:
truststore type:
truststore password:
ssl protocol: TLS
Sensitive Props:
key:
algorithm: PBEWITHMD5AND256BITAES-CBC-OPENSSL
provider: BC
```

# Managing MiNiFi
You can also perform some management tasks using MiNiFi.

## Monitoring Status
You can use the minifi.sh flowStatus option to monitor a range of aspects of your MiNiFi operational and dataflow status. You can use the flowStatus option to get information dataflow component health and functionality, a MiNiFi instance, or system diagnostics.

FlowStatus accepts the following flags and options:
* processors
  * health
  * bulletins
  * status
* connections
  * health
  * stats
* remoteProcessGroups
  * health
  * bulletins
  * status
  * authorizationIssues
  * inputPorts
* controllerServices
  * health
  * bulletins
* provenancereporting
  * health
  * bulletins
* instance
  * health
  * bulletins
  * status
* systemdiagnostics
  * heap
  * processorstats
  * contentrepositoryusage
  * flowfilerepositoryusage
  * garbagecollection

For example, this query gets the health, stats, and bulletins for the TailFile processors:

**Note:** Currently, the script only accepts one high level option at a time.

**Note:** Any connections, remote process groups or processors names that contain ":", ";" or "," will cause parsing errors when querying.

For details on the flowStatus option, see the _FlowStatus Query Option_ section of the [Administration Guide](http://nifi.apache.org/minifi/system-admin-guide.html).

## Loading a New Dataflow
You can load a new dataflow for a MiNiFi instance to run:

1. Create a new config.yml file with the new dataflow.
2. Replace the existing config.yml in minifi/conf with the new file.
3. Restart MiNiFi.

## Stopping MiNiFi

You can MiNiFi at any time.

Stopping MiNiFi:
1. From a terminal window, navigate to the MiNiFi installation directory.
2. Enter:
```
bin/minifi.sh stop
```

Stopping MiNiFi as a service:
1. From a terminal window, enter:
```
sudo service minifi stop
```
