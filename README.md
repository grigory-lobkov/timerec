# Schedule Clients - Time Recorder - TimeRec

Stores list of services and it's schedule. Allow clients to choose any time

![Event Calendar](https://user-images.githubusercontent.com/36440722/88071423-39df8a80-cb8d-11ea-8dbf-10e0c34479fd.png)

## Deploy process

- Copy this repository by Fork/[download](https://github.com/grigory-lobkov/timerec/archive/master.zip) sources
- Choose [database](#databases)
- Compile project or download, take "timerec.war" and put it to Tomcat "webapps" folder. Or [run it inside IntelliJ](#run-in-intellij)
- Open browser, open url [http://127.0.0.1/timerec/js/login.html](http://127.0.0.1/timerec/js/login.html)
- Login as admin@timerec.ru or as client@timerec.ru, password: "timerec"

## Run in IntelliJ

- Open "Run" - "Edit configurations..."
- Add [new Maven configuration](https://user-images.githubusercontent.com/36440722/88074383-ca6b9a00-cb90-11ea-975c-b74fc5e323db.png)
- Set "Command line" to "tomcat7:run"
- [Start it](https://user-images.githubusercontent.com/36440722/88074386-cb043080-cb90-11ea-805d-fdd533286b4a.png)

## Databases

To choose database, set `DB_TYPE` environment variable, supported values:

- H2
- Postgres
- MariaDB

You should also set DB_URL, DB_USER, DB_PASSWORD, DB_MAX_POOL_SIZE, DB_CONNECTION_TIMEOUT environment variables.

## Integration

To choose integration type, set `INTEGRATOR_TYPE` environment variable, supported values:

- Moodle 

For this integration type copy Body tag content from [record.html](https://github.com/grigory-lobkov/timerec/blob/master/src/main/webapp/integration/moodle/record.html)
and put it where you need inside moodle page. Don't forget to configure moodle profile url through `INTEGRATOR_MOODLE_USER_PROFILE_URL` environment variable, for example: `"http://localhost/moodle/user/edit.php"`.

- MoodleDB

For this integration type copy Body tag content from [record.html](https://github.com/grigory-lobkov/timerec/blob/master/src/main/webapp/integration/moodle/record.html)
and put it where you need inside moodle page. Don't forget to configure moodle `sessions` table name by `INTEGRATOR_MOODLEDB_SESSIONS_TABLE_NAME` environment variable, for example: `"mdl_sessions"`. Don't forget to configure moodle `user` table name by `INTEGRATOR_MOODLEDB_USER_TABLE_NAME` environment variable, for example: `"mdl_user"`.

## Alerting

- by Email in profile

## Help

If you want for business relationship, write me by email grigorymail.at.mail.in.ru or call +7922122DATA
