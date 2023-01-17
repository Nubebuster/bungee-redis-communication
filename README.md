# bungee-redis-communication
Plugin for Spigot server communication on a Bungeecord network
## API Reference

#### Get the core (Spigot)

```java
SkyDataCore core = SkyDataSpigot.getCore() // Spigot
SkyDataCore core = SkyDataBungee.getCore() // Bungeecord
```
### Redis

#### Publish a string to all servers on network, including Bungeecord server

```java
core.publish(String message)
```

#### Listen for messages

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
TODO