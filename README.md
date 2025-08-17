# GOMIS

<p align="center">
    <img alt = "Apache Maven" src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white"/>
    <img alt = "Java" src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white"/>
    <img alt = "JavaFX" src="https://img.shields.io/badge/javafx-%23FF0000.svg?style=for-the-badge&logo=javafx&logoColor=white"/>
    <img alt = "MariaDB" src="https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white"/>
    <img alt = "Windows" src="https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white"/>
</p>


GOMIS is a Guidance Office Management Information System developed to be a supportive tool for school guidance offices. 

This system was originally created for the Luis Y. Ferrer Jr. Senior High School Guidance Office to meet their specific needs. **This project is currently in a beta state and is not recommended for production environments.**

## Features

- **Student Records Management:** Easily manage and access student profiles.
- **Incident Reporting:** File a detailed report of incidents or issues.
- **Counselling Appointment:** Set and manage appointments with students using the built-in calendar.
- **Good Moral Certificate Generation:** Automatically generate and print Good Moral certificates.

## Installation and Development

To get GOMIS up and running, you have a few options for building and installing the application. We recommend the all-in-one build script for a quick start.

### 1. All-in-One Quick Start (Recommended)

This is the fastest way to build and install everything you need.

- Open **PowerShell** in the main project directory. If you're using VS Code, you can press <kbd>Ctrl</kbd> + <kbd>J</kbd> to open the integrated terminal.
- Run the following command:
    ```bash
    pwsh ./build-all.ps1
    ```

    This script will automatically build the main app, the validator, the executable (.exe) file, and the installer in a single step. You can view the detailed logs at `gomis-installer/build-all.log` for troubleshooting.

### 2. Manual Maven Build

If you only need to build the Java project itself, use Maven.

- Open a terminal or command prompt in the main project directory.
- Run the following command.
    ```bash
    ./mvnw.cmd clean install
    ```

### 3. Install and Configure the Database

After building the installer, you need to run it to set up the MariaDB database.

- Run the **GOMIS installer*** that you just built. You'll find it in the `resources/Output` directory (or as specified in the build log).
- During the installation, you'll be prompted to set up the MariaDB database.

To access the database after installation, you can connect directly using PowerShell.

```pwsh
& "C:\Program Files\MariaDB 11.4\bin\mariadb.exe" -u root -p gomisdb
```

## Troubleshooting

If you run into any issues, here are a few common problems and their solutions.

- **Database Error:** This usually means the database was not properly created. Rerun the installer to create the database and set the correct connection details.
    - **Default Password:** If setup from the installer, the default database root password is `YourRootPassword123!`. You can find this in `gomis\src\main\java\lyfjshs\gomis\Database\DBConnection.java`.
- **Installer/Build Errors:** Check the `build-all.log` file for detailed error messages. Also, ensure all necessary files are present in the `resources/` directory.
- **"Null Frame" Error:** This is a known issue. If you encounter it, try recompiling the project, which can sometimes resolve the problem.

## Credits and Acknowledgements

GOMIS is a capstone project created by the students of ICT CP12 - Kotlin during the academic year 2024-2025. The project was led by Gaudenz Padullon and Khier Allen Lapurga and was made possible through the valuable partnership with the guidance office, whose insights shaped its development, and under the mentorship of their specialization teacher, Sir Zander Allen Flores.

### Licenses

This project is licensed under the **Mozilla Public License 2.0 (MPL 2.0)**. Please refer to the `LICENSE` file for full details.

This project also incorporates several third-party components, each with its own licensing terms:

* **Google Material Icons**: Licensed under the Apache License 2.0, as specified in `google-material-icons-LICENSE.txt`.
* **Inno Setup**: The installer is created with Inno Setup, which is licensed under the terms found in `inno-setup-LICENSE.txt`.
* **Launch4j**: Used for wrapping the Java application, its license details are in `launch4j-LICENSE.txt`.
* **Installer**: The general installer component is licensed under the terms found in `installer-LICENSE.txt`.
* **GOMIS Logo**: The project logo is licensed separately under CC-BY-NC-SA 4.0, with terms specified in `gomis-logo-LICENSE.txt`.