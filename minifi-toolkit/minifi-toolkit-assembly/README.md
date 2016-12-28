<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
      https://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
# Apache NiFi - MiNiFi - Toolkit

MiNiFi is a child project effort of Apache NiFi.  The MiNiFi toolkit aids in creating MiNiFi configuration files from exported NiFi templates.

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Getting Help](#getting-help)
- [Documentation](#documentation)
- [License](#license)
- [Export Control](#export-control)

## Requirements
* JRE 1.8

## Getting Started

The latest version of the MiNiFi Toolkit Converter can be found at https://nifi.apache.org/minifi/download.html under the `MiNiFi Toolkit Binaries` section.

After downloading the binary and extracting it, to run the MiNiFi Toolkit Converter:
- Change directory to the location where you installed MiNiFi Toolkit and run it and view usage information
  - Linux / OS X
        $ ./config.sh

  - Windows
      execute bin/config.bat

- Usage Information

      Usage:

      java org.apache.nifi.minifi.toolkit.configuration.ConfigMain <command> options

      Valid commands include:
      transform: Transform template xml into MiNiFi config YAML
      validate: Validate config YAML

Note: Currently MiNiFi does not support external Input Ports or Output Ports. Any templates that contain these will fail transformation.

## Getting Help
If you have questions, you can reach out to our mailing list: dev@nifi.apache.org
([archive](https://mail-archives.apache.org/mod_mbox/nifi-dev)).
We're also often available in IRC: #nifi on
[irc.freenode.net](https://webchat.freenode.net/?channels=#nifi).

## Documentation

See https://nifi.apache.org/minifi and https://cwiki.apache.org/confluence/display/NIFI/MiNiFi for the latest documentation.

## License

Except as otherwise noted this software is licensed under the
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Export Control

This distribution includes cryptographic software. The country in which you
currently reside may have restrictions on the import, possession, use, and/or
re-export to another country, of encryption software. BEFORE using any
encryption software, please check your country's laws, regulations and
policies concerning the import, possession, or use, and re-export of encryption
software, to see if this is permitted. See <http://www.wassenaar.org/> for more
information.

The U.S. Government Department of Commerce, Bureau of Industry and Security
(BIS), has classified this software as Export Commodity Control Number (ECCN)
5D002.C.1, which includes information security software using or performing
cryptographic functions with asymmetric algorithms. The form and manner of this
Apache Software Foundation distribution makes it eligible for export under the
License Exception ENC Technology Software Unrestricted (TSU) exception (see the
BIS Export Administration Regulations, Section 740.13) for both object code and
source code.

The following provides more details on the included cryptographic software:

Apache NiFi - MiNiFi uses BouncyCastle, Jasypt, JCraft Inc., and the built-in
java cryptography libraries for SSL, SSH, and the protection
of sensitive configuration parameters. See
https://bouncycastle.org/about.html
http://www.jasypt.org/faq.html
http://jcraft.com/c-info.html
https://www.oracle.com/us/products/export/export-regulations-345813.html
for more details on each of these libraries cryptography features.
