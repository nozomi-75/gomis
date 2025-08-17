# How to Contribute

We welcome and appreciate contributions from the community! Whether you're fixing a bug, adding a new feature, or improving documentation, your help makes this project better for everyone.

## Found a Bug?

If you find a bug, please create a new issue on GitHub and use the Bug Report template. We're not that picky, but in your report, please include:

- A clear and descriptive title.
- Steps to reproduce the bug.
- The expected behavior versus the actual behavior.
- Screenshots or GIFs, if applicable.

## Important Notes & Known Issues

GOMIS is a project in its beta phase, and as such, it contains visible flaws that require attention. We have identified several key areas that need improvement before this system can be considered production-ready.

- **Weak Input Validation:** The system's input validation is not robust, which could lead to unexpected behavior and data integrity issues. This is the most critical area that future developers should address.
- **Database Security:** The current database setup is basic and not secure. The root password for MariaDB is hardcoded in the source file (`DBConnection.java`). For any serious deployment, the database configuration needs to be re-engineered to use secure credentials.
- **User Interface (UI) Bugs:** The UI has some known quirks, including a "Null Frame" error that can occur. Proper error handling and more detailed error messages are needed to diagnose and resolve these issues effectively.
- **General Stability:** The system is still in a developmental state, and while functional, it may exhibit unstable behavior.