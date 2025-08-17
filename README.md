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

### 4. Run the Application

Once the database is set up, you can run the application.

- You can use the desktop shortcut created by the installer.
- Alternatively, you can run the main Java class directly from your IDE or terminal:
    ```bash
    gomis\src\main\lyfjshs\gomis\Main.java
    ```

## Troubleshooting

If you run into any issues, here are a few common problems and their solutions.

- **Database Error:** This usually means the database was not properly created. Rerun the installer to create the database and set the correct connection details.
    - **Default Password:** If setup from the installer, the default database root password is `YourRootPassword123!`. You can find this in `gomis\src\main\java\lyfjshs\gomis\Database\DBConnection.java`.
- **Installer/Build Errors:** Check the `build-all.log` file for detailed error messages. Also, ensure all necessary files are present in the `resources/` directory.
- **"Null Frame" Error:** This is a known issue. If you encounter it, try recompiling the project, which can sometimes resolve the problem.

## How to Contribute

We welcome and appreciate contributions from the community! Whether you're fixing a bug, adding a new feature, or improving documentation, your help makes this project better for everyone.

### Found a Bug?

If you find a bug, please create a new issue on GitHub and use the Bug Report template. In your report, please include:

- A clear and descriptive title.
- Steps to reproduce the bug.
- The expected behavior versus the actual behavior.
- Screenshots or GIFs, if applicable.

### Important Notes & Known Issues
GOMIS is a project in its beta phase, and as such, it contains visible flaws that require attention. We have identified several key areas that need improvement before this system can be considered production-ready.

- **Weak Input Validation:** The system's input validation is not robust, which could lead to unexpected behavior and data integrity issues. This is the most critical area that future developers should address.
- **User Interface (UI) Bugs:** The UI has some known quirks, including a "Null Frame" error that can occur. Proper error handling and more detailed error messages are needed to diagnose and resolve these issues effectively.
- **General Stability:** The system is still in a developmental state, and while functional, it may exhibit unstable behavior.

## Credits and Acknowledgements

GOMIS is a capstone project created by the students of ICT CP12 - Kotlin during the academic year 2024-2025. The project was led by Gaudenz Padullon and Khier Allen Lapurga and was made possible through the valuable partnership with the guidance office, whose insights shaped its development, and under the mentorship of their specialization teacher, Sir Zander Allen Flores.

## License

This project is licensed under the **Mozilla Public License 2.0 (MPL 2.0)** - see the [LICENSE](LICENSE) file for details.

Each source code file is subject to the terms of the MPL 2.0. For more information, please refer to the license notice included in the header of each file. Third-party libraries and software used or included in this project may be under their own respective licenses. Please refer to their documentation for more information.