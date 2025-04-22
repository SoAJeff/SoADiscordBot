# Mysql to Postgres conversion tool
This tool is provided as an example for migrating data from an existing installation of the Java Discord bot to the new bot, since it makes use of a new database platform.  It should not be required for users to use, but was used for migrating the production environment.  It is provided as a reference only.  The following tweaks were necessary to be run after the migration was completed:

- The database schema used in Mysql and Java seemed to set Unix Epoch time to be that of the timezone that the server was running in rather than UTC.  In my case, this was a 5 hour difference.  The following query corrected that:
```
UPDATE users AS u
SET left_server = u.left_server + INTERVAL '5 hours'
WHERE u.left_server = '1969-12-31 19:00:00';
```

- For each user entry, the Python/PSQL instance expects that the 'known name' parameter be an empty string if one is not set, rather than NULL:
```
UPDATE users as u
SET known_name = ''
WHERE u.known_name is NULL;
```

- For each recent action entry, it is expected that the `original_value` and `new_value` parameters are set to an empty string if they are not required for that entry:
```
UPDATE recent_actions as r
SET original_value= ''
WHERE r.original_value is NULL;

UPDATE recent_actions as r
SET new_value= ''
WHERE r.new_value is NULL;
```

The following configuration file template is required to operate this tool:

```
MYSQL_HOST = ""
MYSQL_USER = ""
MYSQL_PASSWORD = ""
MYSQL_DB = ""

PSQL_URI = ""
```