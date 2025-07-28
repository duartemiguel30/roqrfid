## Project Overview

This project was developed to manage and control item boxes that are prepared and supplied to vendors.  
The main goals of the system are to:

- Improve operational efficiency;  
- Reduce time spent on the supply and replenishment processes;  
- Increase customer satisfaction;  
- Ensure better traceability of materials and operations within the company.

## How to run 

javac -encoding UTF-8 -cp "lib/jna-5.17.0.jar" -d bin src/UserCall.java src/RFIDLibrary.java src/RFIDTester.java src/CaixaKanban.java src/ConexaoSQLServer.java src/ReadEpcThread.java
java -cp "bin;lib/mssql-jdbc-9.4.1.jre8.jar;lib/jna-5.17.0.jar" -Djava.library.path=lib RFIDTester       
