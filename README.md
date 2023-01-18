# bungee-redis-communication
Plugin for server communication on a Bungeecord/Spigot network using Redis and MySQL.
This plugin can be used to make servers in a Bungeecord network talk to each other and to the Bungeecord instance without using Plugin Messaging. This means the servers can communicate without the requirement of a player to send the plugin messages through. Additionally, this plugin provides simple MySQL database API.
## API Reference

#### Get the core

```java
SkyDataCore core = SkyDataSpigot.getCore() // Spigot
SkyDataCore core = SkyDataBungee.getCore() // Bungeecord
```
### Redis

#### Publish a payload (String) from any server to all servers on network, including Bungeecord proxy

```java
core.publish(String message)
```

#### Listen for messages
You can add any information you want in the messages. You could for example have game servers advertise available game slots to the lobby or proxy. Or you could apply votifier to all back-end servers by installing Votifier on the bungeecord server and broadcasting a message from the bungeecord to all back-end servers.


```java
// Spigot
@EventHandler
public void onMessage(RedisMessageEventSpigot event) {
    String message = event.getMessage();
}

//Bungeecord
@EventHandler
public void onMessage(RedisMessageEventBungee event) {
    String message = event.getMessage();
}
```
#### Change data in the redis server
```java
core.setValue(String key, Object value, int secondsToExpire)
```

| Method | Parameters | Returns     | Description                |
| :------- | :-------- | :-------    | :------------------------- |
| setValue | String key, Object value, int expireSeconds | `void`    | Set value with expiry |
| deleteValue | String key | `void`    |  |
| setValuePermanently | String key, Object value| `void`    | Set value without expiry |
| getValue | String key, Object value, int expireSeconds | `String`    |  |
| lPushValue | String key, String... value | `void`    | Add values to head of list* |
| rPushValue | String key, String... value | `void`    | Add values to tail of list* |
| lPopValue | String key | `String`    | Get and remove value from head of list** |
| rPopValue | String key | `String`    | Get and remove value from tail of list** |
| getDelValue | String key | `String`    | Get and remove value |
| lIndexValue | String key, int index | `String`    | Get value of list at index |

*If list does not exist, one is created

**Will leave empty list if no values are present anymore

### MYSQL
The plugin comes with simple MySQL support. MySQL can be disabled in the config.yml
#### Update
```java
PreparedStatement statement = core.getMYSQLDatabase().createStatement("SET score=? FROM ? WHERE uuid=?;");
statement.setInt(1, 100);
statement.setString(2, database);
statement.setString(3, uuid.toString());
boolean result = statement.execute();
```

#### Fetch
```java
PreparedStatement statement = core.getMYSQLDatabase().createStatement("SELECT score FROM ? WHERE uuid=?;");
statement.setString(1, database);
statement.setString(2, uuid.toString());
ResultSet result = statement.executeQuery();
if (result.next()) {
    int score = result.getInt("score");
} else {
    //not found
}
```
Optionally you can execute SQL strings using `fetch(String sqlQuery)` and `executeUpdate(String sqlQuery)` but this is not recommended.