# Welcome to the PLCnCLI eclipse plugin repository

This repository is part of the PLCnext Toolchain and provides an Eclipse IDE plugin for programming for PLCnext Technology in high level language. The PLCnext CLI provides the entire toolchain for programming on the PLCnext Technology platform. The main repository for the CLI can be found [here](https://github.com/PLCnext/PLCnext_CLI).

## Installation & First Steps

If you want the latest release version of the PLCnext CLI, you can find it in the [PLCnext Technology controller download area](https://www.phoenixcontact.com/qr/2404267/softw).
For more information and first steps with PLCnext Technology please visit our [PLCnext Community](https://www.plcnext.help/te/Programming/Introduction.htm).

## Test a local build

The following steps describe how to build a fully functional PLCnCLI eclipse plugin locally on your machine to test the newest version or to test your own changes.

### Prerequisite

- [Java 13 or newer](http://www.oracle.com/technetwork/java/javase/downloads/index.html "Java 13 or newer")
- [Maven](https://maven.apache.org/download.cgi "Maven")

### Build steps

- `cd src/build`
- `mvn clean compile package`
- Update site can be found in `../com.phoenixcontact.plcnext.updatesite/target/com.phoenixcontact.plcnext.updatesite-XXX.zip`

## Contribute

Currently not supported. We are working on a process for contribution.

## Feedback

You can give feedback to this project in different ways:

- Ask a question in our [Forum](https://www.plcnext-community.net/index.php?option=com_easydiscuss&view=categories&Itemid=221&lang=en).
- Request a new feature via [GitHub Issues](https://github.com/PLCnext/PLCnext_CLI_Eclipse/issues).
- Vote for [Popular Feature Requests](https://github.com/PLCnext/PLCnext_CLI_Eclipse/issues?q=is%3Aopen+is%3Aissue+label%3Afeature-request+sort%3Areactions-%2B1-desc).
- File a bug in [GitHub Issues](https://github.com/PLCnext/PLCnext_CLI_Eclipse/issues).

## License

Copyright (c) Phoenix Contact Gmbh & Co KG. All rights reserved.
Licensed under the [EPL-2.0](LICENSE) License.
