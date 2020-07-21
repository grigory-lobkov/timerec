# TimeRec

Stores list of services and it's schedule. Allow clients to choose any time

![Event Calendar](https://user-images.githubusercontent.com/36440722/88071423-39df8a80-cb8d-11ea-8dbf-10e0c34479fd.png)

## Deploy process

- Copy this repository by Fork/[download](https://github.com/grigory-lobkov/timerec/archive/master.zip) sources
- Change default users ADMIN_EMAIL, ADMIN_PASSWORD, CLIENT_EMAIL, CLIENT_PASSWORD in [CreateStructure.java](https://github.com/grigory-lobkov/timerec/blob/master/src/main/java/storage/CreateStructure.java)
- Choose [database](#databases)
- Compile project, take "timerec.war" and put it to Tomcat "webapps" folder. Or [run it inside IntelliJ](#run-in-intellij)
- Open browser, open url [http://127.0.0.1/timerec/js/login.html](http://127.0.0.1/timerec/js/login.html), login as somebody, discover your site

## Run in IntelliJ

- Open "Run" - "Edit configurations..."
- Add [new Maven configuration](https://user-images.githubusercontent.com/36440722/88074383-ca6b9a00-cb90-11ea-975c-b74fc5e323db.png)
- Set "Command line" to "tomcat7:run"
- [Start it](https://user-images.githubusercontent.com/36440722/88074386-cb043080-cb90-11ea-805d-fdd533286b4a.png)

## Databases

To choose database, change `dbPool` variable to desirable implementation in [StorageFactory.java](https://github.com/grigory-lobkov/timerec/blob/master/src/main/java/storage/StorageFactory.java) and set JDBC_DRIVER, JDBC_URL, DB_USER, DB_PASSWORD there:

- H2 - [storage.connectImpl.H2ConnectionPool](https://github.com/grigory-lobkov/timerec/blob/master/src/main/java/storage/connectImpl/H2ConnectionPool.java)
- Postgres - [storage.connectImpl.PgConnectionPool](https://github.com/grigory-lobkov/timerec/blob/master/src/main/java/storage/connectImpl/PgConnectionPool.java)

## Integration

Can be integrated with Moodle, just copy Body tag content from [record.html](https://github.com/grigory-lobkov/timerec/blob/master/src/main/webapp/integration/moodle/record.html) and put it where you need on moodle page.
