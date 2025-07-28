## Project Overview

This project was developed to manage and control item boxes that are prepared and supplied to vendors.  
The main goals of the system are to:

- Improve operational efficiency;  
- Reduce time spent on the supply and replenishment processes;  
- Increase customer satisfaction;  
- Ensure better traceability of materials and operations within the company.

By digitising and automating the handling of supply boxes, this solution supports a more streamlined workflow and enhanced visibility across the supply chain.

## How to run 

javac -encoding UTF-8 -cp ".;lib/jna-5.17.0.jar" UserCall.java RFIDLibrary.java RFIDTester.java
java -cp ".;lib/jna-5.17.0.jar" -Djava.library.path=. RFIDTester     